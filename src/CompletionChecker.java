import java.util.Arrays;

/**
 * // -------------------------------------------------------------------------
 * /** Holds the task chain (with a cursor on the current task), the reminder
 * chain, and the history of Moves that powers undo, redo, and the replay.
 *
 * Positions are 0-based. Every task action is recorded as a Move in the history
 * chain; appliedCount says how many of those Moves are currently applied
 * (Moves past appliedCount have been undone and can be redone). Recording a new
 * action discards the undone Moves. Reminder actions are not recorded, so undo
 * does not affect reminders.
 *
 * In the Big-O notes, n is the number of tasks, m is the number of Moves in the
 * history, and r is the number of reminders.
 */
public class CompletionChecker
{
    // ~ Fields ................................................................
    private LinkedChain<Task> tasks;
    private LinkedChain<Move> history;
    private int appliedCount;
    private LinkedChain<String> reminders;

    // ~ Constructors ..........................................................
    /**
     * Creates a checker with no tasks, no reminders, and an empty history.
     * O(1)
     */
    public CompletionChecker()
    {
        tasks = new LinkedChain<>();
        history = new LinkedChain<>();
        appliedCount = 0;
        reminders = new LinkedChain<>();
    }


    // ~ Public Methods: tasks .................................................
    /**
     * Adds a task to the end and records an add. O(m)
     *
     * @param task the task to add
     * @throws IllegalArgumentException if task is null
     */
    public void addTask(Task task)
    {
        if (task == null)
        {
            throw new IllegalArgumentException("task cannot be null");
        }
        addTask(task, tasks.size());
    }


    /**
     * Inserts a task so it ends up at position and records an add. The current
     * task stays current. O(n + m)
     *
     * @param task the task to add
     * @param position where the task should end up (0 to size inclusive)
     * @throws IllegalArgumentException if task is null
     * @throws IndexOutOfBoundsException if position is out of range
     */
    public void addTask(Task task, int position)
    {
        if (task == null)
        {
            throw new IllegalArgumentException("task cannot be null");
        }
        if (position < 0 || position > tasks.size())
        {
            throw new IndexOutOfBoundsException(
                "position " + position + ", size " + tasks.size());
        }
        int cursorBefore = tasks.getCursorIndex();
        tasks.add(position, task);
        if (!tasks.hasCursor())
        {
            tasks.moveCursorToFront();
        }
        record(new Move("add", task, -1, position, cursorBefore,
            tasks.getCursorIndex()));
    }


    /**
     * The task at the cursor. O(1)
     *
     * @return the current task
     * @throws IllegalStateException if there are no tasks
     */
    public Task getCurrentTask()
    {
        if (tasks.isEmpty() || !tasks.hasCursor())
        {
            throw new IllegalStateException("there are no tasks");
        }
        return tasks.getCursorEntry();
    }


    /**
     * O(1)
     *
     * @return the cursor position, or -1 if there are no tasks
     */
    public int getCurrentPosition()
    {
        return tasks.isEmpty() ? -1 : tasks.getCursorIndex();
    }


    /**
     * O(1)
     *
     * @return the number of tasks
     */
    public int size()
    {
        return tasks.size();
    }


    /**
     * O(1)
     *
     * @return true if there are no tasks
     */
    public boolean isEmpty()
    {
        return tasks.isEmpty();
    }


    /**
     * Moves the cursor to the next task, wrapping to the first after the last.
     * Not recorded. O(1)
     *
     * @throws IllegalStateException if there are no tasks
     */
    public void nextTask()
    {
        requireTasks();
        if (!tasks.advanceCursor())
        {
            tasks.moveCursorToFront();
        }
    }


    /**
     * Removes and returns the current task. The next task becomes current
     * (wrapping to the first if the last was removed). Records a complete.
     * O(m)
     *
     * @return the completed task
     * @throws IllegalStateException if there are no tasks
     */
    public Task completeCurrent()
    {
        return removeCurrent("complete");
    }


    /**
     * Same as completeCurrent, but records a delete. O(m)
     *
     * @return the deleted task
     * @throws IllegalStateException if there are no tasks
     */
    public Task deleteCurrent()
    {
        return removeCurrent("delete");
    }


    /**
     * The tasks after the current one, in order. O(n)
     *
     * @return the upcoming tasks (empty if none, or if there are no tasks)
     */
    public Task[] getUpcoming()
    {
        if (tasks.isEmpty())
        {
            return new Task[0];
        }
        Task[] all = getTasks();
        return Arrays.copyOfRange(all, tasks.getCursorIndex() + 1, all.length);
    }


    /**
     * Moves the current task spots places later. The next task becomes
     * current. Records a postpone. O(n + m)
     *
     * @param spots how many places later (1 up to the number of upcoming tasks)
     * @throws IllegalStateException if there are no tasks
     * @throws IllegalArgumentException if spots is less than 1 or past the end
     */
    public void postponeCurrent(int spots)
    {
        requireTasks();
        if (spots < 1 || spots > upcomingCount())
        {
            throw new IllegalArgumentException(
                "spots must be from 1 to " + upcomingCount());
        }
        int cursor = tasks.getCursorIndex();
        Task task = tasks.removeAtCursor();
        tasks.add(cursor + spots, task);
        record(new Move("postpone", task, cursor, cursor + spots, cursor,
            tasks.getCursorIndex()));
    }


    /**
     * Moves the task spotsAhead places after the current one to the cursor
     * and makes it current. Records a pull forward. O(n + m)
     *
     * @param spotsAhead 1 up to the number of upcoming tasks
     * @throws IllegalStateException if there are no tasks
     * @throws IllegalArgumentException if spotsAhead is out of range
     */
    public void pullForward(int spotsAhead)
    {
        requireTasks();
        if (spotsAhead < 1 || spotsAhead > upcomingCount())
        {
            throw new IllegalArgumentException(
                "spotsAhead must be from 1 to " + upcomingCount());
        }
        int cursor = tasks.getCursorIndex();
        int from = cursor + spotsAhead;
        Task task = tasks.remove(from);
        tasks.insertBeforeCursor(task);
        record(new Move("pull forward", task, from, cursor, cursor,
            tasks.getCursorIndex()));
    }


    /**
     * O(1)
     *
     * @return true if appliedCount is above 0
     */
    public boolean canUndo()
    {
        return appliedCount > 0;
    }


    /**
     * O(1)
     *
     * @return true if appliedCount is below the history size
     */
    public boolean canRedo()
    {
        return appliedCount < history.size();
    }


    /**
     * Reverses the last applied Move and restores the cursor to where it was
     * before that Move. O(n + m)
     *
     * @return the Move that was undone
     * @throws IllegalStateException if there is nothing to undo
     */
    public Move undo()
    {
        if (!canUndo())
        {
            throw new IllegalStateException("nothing to undo");
        }
        Move move = history.get(appliedCount - 1);
        switch (move.getType())
        {
            case "add":
                tasks.remove(move.getToIndex());
                break;
            case "complete":
            case "delete":
                tasks.add(move.getFromIndex(), move.getTask());
                break;
            default: // postpone, pull forward
                Task moved = tasks.remove(move.getToIndex());
                tasks.add(move.getFromIndex(), moved);
                break;
        }
        appliedCount--;
        restoreCursor(move.getCursorBefore());
        return move;
    }


    /**
     * Re-applies the next undone Move and restores the cursor to where it was
     * after that Move. O(n + m)
     *
     * @return the Move that was redone
     * @throws IllegalStateException if there is nothing to redo
     */
    public Move redo()
    {
        if (!canRedo())
        {
            throw new IllegalStateException("nothing to redo");
        }
        Move move = history.get(appliedCount);
        switch (move.getType())
        {
            case "add":
                tasks.add(move.getToIndex(), move.getTask());
                break;
            case "complete":
            case "delete":
                tasks.remove(move.getFromIndex());
                break;
            default: // postpone, pull forward
                Task moved = tasks.remove(move.getFromIndex());
                tasks.add(move.getToIndex(), moved);
                break;
        }
        appliedCount++;
        restoreCursor(move.getCursorAfter());
        return move;
    }


    /**
     * One numbered line per applied Move. O(m)
     *
     * @return the replay, or "No actions today." if there are none
     */
    public String getReplay()
    {
        if (appliedCount == 0)
        {
            return "No actions today.";
        }
        StringBuilder replay = new StringBuilder();
        for (int i = 0; i < appliedCount; i++)
        {
            if (i > 0)
            {
                replay.append("\n");
            }
            replay.append(i + 1).append(". ").append(history.get(i));
        }
        return replay.toString();
    }


    /**
     * The task with the most postpone Moves among the applied Moves. Tasks are
     * compared by identity, because two different tasks may share a name. If
     * tasks tie, the one postponed first wins. O(m^2)
     *
     * @return the most postponed task, or null if none were postponed
     */
    public Task mostPostponed()
    {
        Task best = null;
        int bestCount = 0;
        for (int i = 0; i < appliedCount; i++)
        {
            Move first = history.get(i);
            if (!"postpone".equals(first.getType()))
            {
                continue;
            }
            int count = 0;
            for (int j = 0; j < appliedCount; j++)
            {
                Move other = history.get(j);
                if ("postpone".equals(other.getType())
                    && other.getTask() == first.getTask())
                {
                    count++;
                }
            }
            if (count > bestCount)
            {
                bestCount = count;
                best = first.getTask();
            }
        }
        return best;
    }


    /**
     * O(n)
     *
     * @return all tasks in order
     */
    public Task[] getTasks()
    {
        return tasks.toArray(new Task[0]);
    }


    // ~ Public Methods: reminders .............................................
    /**
     * Trims the text and adds it to the end of the reminders. Not recorded.
     * O(1)
     *
     * @param text the reminder
     * @throws IllegalArgumentException if text is null or blank
     */
    public void addReminder(String text)
    {
        if (text == null || text.trim().isEmpty())
        {
            throw new IllegalArgumentException("reminder cannot be blank");
        }
        reminders.addToEnd(text.trim());
    }


    /**
     * Removes and returns the reminder at position. Not recorded. O(r)
     *
     * @param position the 0-based position
     * @return the removed reminder
     * @throws IndexOutOfBoundsException if position is out of range
     */
    public String removeReminder(int position)
    {
        return reminders.remove(position);
    }


    /**
     * O(r)
     *
     * @return all reminders in order
     */
    public String[] getReminders()
    {
        return reminders.toArray(new String[0]);
    }


    // ~ Private Methods .......................................................
    /**
     * Records a Move: discards any undone Moves, adds this one to the end of
     * the history, and counts it as applied. O(m)
     */
    private void record(Move move)
    {
        history.removeFrom(appliedCount);
        history.addToEnd(move);
        appliedCount++;
    }


    /**
     * Removes the current task for a complete or delete and records it.
     */
    private Task removeCurrent(String type)
    {
        requireTasks();
        int cursor = tasks.getCursorIndex();
        Task task = tasks.removeAtCursor();
        if (!tasks.hasCursor() && !tasks.isEmpty())
        {
            tasks.moveCursorToFront(); // removed the last task, so wrap
        }
        int cursorAfter = tasks.isEmpty() ? -1 : tasks.getCursorIndex();
        record(new Move(type, task, cursor, -1, cursor, cursorAfter));
        return task;
    }


    /**
     * Puts the cursor at index, or on the first task if index is not valid.
     * Does nothing if there are no tasks.
     */
    private void restoreCursor(int index)
    {
        if (tasks.isEmpty())
        {
            return;
        }
        if (index >= 0 && index < tasks.size())
        {
            tasks.moveCursorTo(index);
        }
        else
        {
            tasks.moveCursorToFront();
        }
    }


    private void requireTasks()
    {
        if (tasks.isEmpty())
        {
            throw new IllegalStateException("there are no tasks");
        }
    }


    /**
     * The number of tasks after the current one.
     */
    private int upcomingCount()
    {
        return tasks.size() - tasks.getCursorIndex() - 1;
    }
}

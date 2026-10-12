
/**
 * // -------------------------------------------------------------------------
 * /** Holds the task chain (with a cursor on the current task) and the reminder
 * chain. 
 * Prototype version: undo/redo, postpone, pull forward, replay, and
 * mostPostponed are not built yet.
 * 
 * @author scottpost
 * @version Oct 5, 2026
 */
public class CompletionChecker
{
    // ~ Fields ................................................................
    private LinkedChain<Task> tasks;
    private LinkedChain<String> reminders;

    // ~ Constructors ..........................................................
    public CompletionChecker()
    {
        tasks = new LinkedChain<>();
        reminders = new LinkedChain<>();
    }


    // ~Public Methods ........................................................
    /**
     * Adds a task to the end, records add, throws IllegalArgumentException if
     * null.
     * 
     * @param task
     *            the task being added
     * @throws IllegalArgumentException
     *             if task is null
     */
    public void addTask(Task task)
    {
        if (task == null)
        {
            throw new IllegalArgumentException("Task can't be null");
        }
        tasks.addToEnd(task);
        if (!tasks.hasCursor())
        {
            tasks.moveCursorToFront();
        }

    }


    /**
     * Inserts task at specified position, throws IllegalArgumentException if
     * null.
     * 
     * @param task
     *            the task being added
     * @param position
     *            the specified position
     * @throws IllegalArgumentException
     *             if task is null
     * @throws IndexOutOfBoundsException
     *             if position is out of range
     */
    public void addTask(Task task, int position)
    {
        if (task == null)
        {
            throw new IllegalArgumentException("Task can't be null.");
        }
        if (position < 0 || position > tasks.size())
        {
            throw new IndexOutOfBoundsException(
                "Position must be from 0 to " + tasks.size() + ".");
        }
        tasks.add(position, task);
        if (!tasks.hasCursor())
        {
            tasks.moveCursorToFront();
        }
    }


    /**
     * The task at the cursor. Throws IllegalStateException if there are no
     * tasks.
     * 
     * @return Task the task at the current position (cursor).
     * @throws IllegalStateException
     *             if there are no tasks
     */
    public Task getCurrentTask()
    {
        if (tasks.isEmpty())
        {
            throw new IllegalStateException("There are no tasks.");
        }
        return tasks.getCursorEntry();
    }


    /**
     * Returns the zero-based position of the current task.
     *
     * @return the cursor position, or -1 if there are no tasks
     */
    public int getCurrentPosition()
    {
        return tasks.getCursorIndex();
    }


    /**
     * Returns the number of tasks.
     *
     * @return the number of tasks
     */
    public int size()
    {
        return tasks.size();
    }


    /**
     * Returns whether there are no tasks.
     *
     * @return true if there are no tasks
     */
    public boolean isEmpty()
    {
        return tasks.isEmpty();
    }


    /**
     * Moves to the next task, wrapping to the first after the last.
     *
     * @throws IllegalStateException
     *             if there are no tasks
     */
    public void nextTask()
    {
        if (tasks.isEmpty())
        {
            throw new IllegalStateException("There are no tasks.");
        }
        if (!tasks.advanceCursor())
        {
            tasks.moveCursorToFront();
        }
    }


    /**
     * Removes and returns the current task as completed. The next task becomes
     * current, wrapping to the first if the last was removed.
     *
     * @return the completed task
     * @throws IllegalStateException
     *             if there are no tasks
     */
    public Task completeCurrent()
    {
        Task done = removeCurrent();
        // TODO: record Move ("complete")
        return done;
    }


    /**
     * Removes and returns the current task as deleted. The next task becomes
     * current, wrapping to the first if the last was removed.
     *
     * @return the deleted task
     * @throws IllegalStateException
     *             if there are no tasks
     */
    public Task deleteCurrent()
    {
        Task gone = removeCurrent();
        // TODO: record Move ("delete")
        return gone;
    }


    /**
     * Shared removal for complete and delete.
     *
     * @return the removed task
     * @throws IllegalStateException
     *             if there are no tasks
     */
    private Task removeCurrent()
    {
        if (tasks.isEmpty())
        {
            throw new IllegalStateException("There are no tasks.");
        }
        Task removed = tasks.removeAtCursor();
        if (!tasks.hasCursor() && !tasks.isEmpty())
        {
            tasks.moveCursorToFront();
        }
        return removed;
    }


    /**
     * Trims and adds a reminder to the end of the reminder list.
     *
     * @param text
     *            the reminder
     * @throws IllegalArgumentException
     *             if text is null or blank
     */
    public void addReminder(String text)
    {
        if (text == null || text.trim().isEmpty())
        {
            throw new IllegalArgumentException("Reminder can't be blank.");
        }
        reminders.addToEnd(text.trim());
    }


    /**
     * Returns all reminders in order.
     *
     * @return the reminders, oldest first
     */
    public String[] getReminders()
    {
        return reminders.toArray(new String[reminders.size()]);
    }
}

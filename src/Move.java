/**
 * // -------------------------------------------------------------------------
 * /** One recorded action: enough information to undo it and redo it. A Move
 * never changes after it is made.
 *
 * Positions are 0-based. Add Moves use fromIndex -1 (the task came from
 * nowhere); complete and delete Moves use toIndex -1 (the task went nowhere).
 * A cursor value of -1 means there was no cursor (no tasks).
 */
public class Move
{
    // ~ Fields ................................................................
    private final String type;
    private final Task task;
    private final int fromIndex;
    private final int toIndex;
    private final int cursorBefore;
    private final int cursorAfter;

    // ~ Constructors ..........................................................
    /**
     * Stores all six values.
     *
     * @param type "add", "complete", "delete", "postpone", or "pull forward"
     * @param task the task the action was applied to
     * @param fromIndex where the task was before the action
     * @param toIndex where the task was after the action
     * @param cursorBefore the cursor position before the action
     * @param cursorAfter the cursor position after the action
     * @throws IllegalArgumentException if task is null or type is not one of
     *             the five types
     */
    public Move(
        String type,
        Task task,
        int fromIndex,
        int toIndex,
        int cursorBefore,
        int cursorAfter)
    {
        if (task == null)
        {
            throw new IllegalArgumentException("task cannot be null");
        }
        if (!"add".equals(type) && !"complete".equals(type)
            && !"delete".equals(type) && !"postpone".equals(type)
            && !"pull forward".equals(type))
        {
            throw new IllegalArgumentException("unknown move type: " + type);
        }
        this.type = type;
        this.task = task;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.cursorBefore = cursorBefore;
        this.cursorAfter = cursorAfter;
    }


    // ~ Public Methods ........................................................
    /**
     * @return the type of action
     */
    public String getType()
    {
        return type;
    }


    /**
     * @return the task the action was applied to
     */
    public Task getTask()
    {
        return task;
    }


    /**
     * @return the task's position before the action
     */
    public int getFromIndex()
    {
        return fromIndex;
    }


    /**
     * @return the task's position after the action
     */
    public int getToIndex()
    {
        return toIndex;
    }


    /**
     * @return the cursor position before the action
     */
    public int getCursorBefore()
    {
        return cursorBefore;
    }


    /**
     * @return the cursor position after the action
     */
    public int getCursorAfter()
    {
        return cursorAfter;
    }


    /**
     * One replay line. Positions are shown starting from 1, for example
     * Postponed "Gym" from #2 to #4.
     *
     * @return the replay line
     */
    @Override
    public String toString()
    {
        String name = "\"" + task.getName() + "\"";
        switch (type)
        {
            case "add":
                return "Added " + name + " at #" + (toIndex + 1);
            case "complete":
                return "Completed " + name;
            case "delete":
                return "Deleted " + name;
            case "postpone":
                return "Postponed " + name + " from #" + (fromIndex + 1)
                    + " to #" + (toIndex + 1);
            default:
                return "Pulled forward " + name + " from #" + (fromIndex + 1)
                    + " to #" + (toIndex + 1);
        }
    }
}

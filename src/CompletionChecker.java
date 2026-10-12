
/**
 * // -------------------------------------------------------------------------
 * /** Write a one-sentence summary of your class here. Follow it with
 * additional details about its purpose, what abstraction it represents, and how
 * to use it.
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
        tasks.add(position, task);
    }


    /**
     * The task at the cursor. Throws IllegalStateException if there are no
     * tasks.
     * 
     * @return Task the task at the current position (cursor).
     */
    public Task getCurrentTask()
    {
        return Task;
    }

}

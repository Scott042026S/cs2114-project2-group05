/**
 * // -------------------------------------------------------------------------
 * /** Tests CompletionChecker
 * 
 * @author scottpost
 * @version Oct 12, 2026
 */
public class CompletionCheckerTest
    extends student.TestCase
{
    private CompletionChecker checker;
    private Task taskA;
    private Task taskB;
    private Task taskC;

    /**
     * Creates a fresh checker and three tasks before each test.
     */
    public void setUp()
    {
        checker = new CompletionChecker();
        taskA = new Task("A", "");
        taskB = new Task("B", "");
        taskC = new Task("C", "");
    }

    // ----- constructor -----


    /**
     * A new checker has no tasks, no cursor, and no reminders.
     */
    public void testConstructor()
    {
        assertEquals(0, checker.size());
        assertTrue(checker.isEmpty());
        assertEquals(-1, checker.getCurrentPosition());
        assertEquals(0, checker.getReminders().length);
    }

    // ----- addTask(Task) -----


    /**
     * Adding A then B keeps A as the current task.
     */
    public void testAddTask()
    {
        checker.addTask(taskA);
        checker.addTask(taskB);
        assertEquals(2, checker.size());
        assertSame(taskA, checker.getCurrentTask());
        checker.nextTask();
        assertSame(taskB, checker.getCurrentTask());
    }


    /**
     * Adding null throws and changes nothing.
     */
    public void testAddTaskNull()
    {
        try
        {
            checker.addTask(null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        assertEquals(0, checker.size());
    }

    // ----- addTask(Task, int) -----


    /**
     * Inserting B at 1 in [A, C] gives [A, B, C].
     */
    public void testAddTaskAtPosition()
    {
        checker.addTask(taskA);
        checker.addTask(taskC);
        checker.addTask(taskB, 1);
        assertEquals(3, checker.size());
        assertSame(taskA, checker.getCurrentTask());
        checker.nextTask();
        assertSame(taskB, checker.getCurrentTask());
        checker.nextTask();
        assertSame(taskC, checker.getCurrentTask());
    }


    /**
     * Inserting before the current task keeps the same current task.
     */
    public void testAddTaskBeforeCurrent()
    {
        checker.addTask(taskA);
        checker.addTask(taskC, 0);
        assertSame(taskA, checker.getCurrentTask());
        assertEquals(1, checker.getCurrentPosition());
    }


    /**
     * Positions past the end or below 0, or a null task, throw and change
     * nothing.
     */
    public void testAddTaskAtBadPosition()
    {
        checker.addTask(taskA);
        try
        {
            checker.addTask(taskB, 9);
            fail("Expected IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException e)
        {
            // expected
        }
        try
        {
            checker.addTask(taskB, -1);
            fail("Expected IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException e)
        {
            // expected
        }
        try
        {
            checker.addTask(null, 0);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        assertEquals(1, checker.size());
    }

    // ----- getCurrentTask() -----


    /**
     * The first task added is current.
     */
    public void testGetCurrentTask()
    {
        checker.addTask(taskA);
        assertSame(taskA, checker.getCurrentTask());
    }


    /**
     * No tasks throws IllegalStateException.
     */
    public void testGetCurrentTaskEmpty()
    {
        try
        {
            checker.getCurrentTask();
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    // ----- getCurrentPosition() -----


    /**
     * Position follows the cursor.
     */
    public void testGetCurrentPosition()
    {
        checker.addTask(taskA);
        checker.addTask(taskB);
        assertEquals(0, checker.getCurrentPosition());
        checker.nextTask();
        assertEquals(1, checker.getCurrentPosition());
    }


    /**
     * No tasks gives -1.
     */
    public void testGetCurrentPositionEmpty()
    {
        assertEquals(-1, checker.getCurrentPosition());
    }

    // ----- size() / isEmpty() -----


    /**
     * Two tasks: size 2, not empty.
     */
    public void testSizeAndIsEmpty()
    {
        checker.addTask(taskA);
        checker.addTask(taskB);
        assertEquals(2, checker.size());
        assertFalse(checker.isEmpty());
    }


    /**
     * Completing the only task leaves size 0, empty, position -1.
     */
    public void testSizeAndIsEmptyAfterLastRemoved()
    {
        checker.addTask(taskA);
        checker.completeCurrent();
        assertEquals(0, checker.size());
        assertTrue(checker.isEmpty());
        assertEquals(-1, checker.getCurrentPosition());
    }

    // ----- nextTask() -----


    /**
     * Next from A goes to B; next from the last wraps to the first.
     */
    public void testNextTask()
    {
        checker.addTask(taskA);
        checker.addTask(taskB);
        checker.nextTask();
        assertSame(taskB, checker.getCurrentTask());
        checker.nextTask();
        assertSame(taskA, checker.getCurrentTask());
        assertEquals(0, checker.getCurrentPosition());
    }


    /**
     * Next with no tasks throws IllegalStateException.
     */
    public void testNextTaskEmpty()
    {
        try
        {
            checker.nextTask();
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    // ----- completeCurrent() -----


    /**
     * Completing A in [A, B, C] returns A and makes B current.
     */
    public void testCompleteCurrent()
    {
        checker.addTask(taskA);
        checker.addTask(taskB);
        checker.addTask(taskC);
        assertSame(taskA, checker.completeCurrent());
        assertEquals(2, checker.size());
        assertSame(taskB, checker.getCurrentTask());
    }


    /**
     * Completing the last task wraps the cursor to the first.
     */
    public void testCompleteCurrentLastWraps()
    {
        checker.addTask(taskA);
        checker.addTask(taskB);
        checker.nextTask();
        assertSame(taskB, checker.completeCurrent());
        assertSame(taskA, checker.getCurrentTask());
        assertEquals(0, checker.getCurrentPosition());
    }


    /**
     * Completing with no tasks throws IllegalStateException.
     */
    public void testCompleteCurrentEmpty()
    {
        try
        {
            checker.completeCurrent();
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    // ----- deleteCurrent() -----


    /**
     * Deleting A in [A, B] returns A and makes B current.
     */
    public void testDeleteCurrent()
    {
        checker.addTask(taskA);
        checker.addTask(taskB);
        assertSame(taskA, checker.deleteCurrent());
        assertEquals(1, checker.size());
        assertSame(taskB, checker.getCurrentTask());
    }


    /**
     * Deleting with no tasks throws IllegalStateException.
     */
    public void testDeleteCurrentEmpty()
    {
        try
        {
            checker.deleteCurrent();
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    // ----- addReminder(String) -----


    /**
     * A reminder is stored trimmed.
     */
    public void testAddReminder()
    {
        checker.addReminder("  Call mom  ");
        assertEquals(1, checker.getReminders().length);
        assertEquals("Call mom", checker.getReminders()[0]);
    }


    /**
     * Blank or null reminders throw and change nothing.
     */
    public void testAddReminderBlank()
    {
        try
        {
            checker.addReminder("   ");
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        try
        {
            checker.addReminder(null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        assertEquals(0, checker.getReminders().length);
    }

    // ----- getReminders() -----


    /**
     * Reminders come back in the order they were added.
     */
    public void testGetReminders()
    {
        checker.addReminder("Call mom");
        checker.addReminder("Buy milk");
        String[] list = checker.getReminders();
        assertEquals(2, list.length);
        assertEquals("Call mom", list[0]);
        assertEquals("Buy milk", list[1]);
    }


    /**
     * No reminders gives an empty array.
     */
    public void testGetRemindersEmpty()
    {
        assertEquals(0, checker.getReminders().length);
    }

}

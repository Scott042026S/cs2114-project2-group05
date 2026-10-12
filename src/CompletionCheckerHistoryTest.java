/**
 * // -------------------------------------------------------------------------
 * /** Tests the CompletionChecker methods that work with the history and the
 * task order: getUpcoming, postponeCurrent, pullForward, canUndo, canRedo, undo,
 * redo, getReplay, mostPostponed, removeReminder, and getTasks.
 */
public class CompletionCheckerHistoryTest
    extends student.TestCase
{
    private CompletionChecker checker;
    private Task taskA;
    private Task taskB;
    private Task taskC;
    private Task taskD;

    /**
     * Creates a fresh checker and four tasks before each test.
     */
    public void setUp()
    {
        checker = new CompletionChecker();
        taskA = new Task("A", "");
        taskB = new Task("B", "");
        taskC = new Task("C", "");
        taskD = new Task("D", "");
    }


    /**
     * Adds A, B, C, D in order.
     */
    private void addFour()
    {
        checker.addTask(taskA);
        checker.addTask(taskB);
        checker.addTask(taskC);
        checker.addTask(taskD);
    }


    /**
     * The task names in order, for example "ABCD".
     */
    private String order()
    {
        StringBuilder names = new StringBuilder();
        for (Task task : checker.getTasks())
        {
            names.append(task.getName());
        }
        return names.toString();
    }

    // ----- getTasks() -----


    /**
     * All tasks come back in order.
     */
    public void testGetTasks()
    {
        addFour();
        assertEquals("ABCD", order());
    }


    /**
     * No tasks gives an empty array.
     */
    public void testGetTasksEmpty()
    {
        assertEquals(0, checker.getTasks().length);
    }

    // ----- getUpcoming() -----


    /**
     * The tasks after the current one are listed in order.
     */
    public void testGetUpcoming()
    {
        addFour();
        assertEquals(3, checker.getUpcoming().length);
        assertSame(taskB, checker.getUpcoming()[0]);
        checker.nextTask();
        assertEquals(2, checker.getUpcoming().length);
        assertSame(taskC, checker.getUpcoming()[0]);
    }


    /**
     * On the last task, or with no tasks, upcoming is empty.
     */
    public void testGetUpcomingEmpty()
    {
        assertEquals(0, checker.getUpcoming().length);
        checker.addTask(taskA);
        assertEquals(0, checker.getUpcoming().length);
    }

    // ----- postponeCurrent(int) -----


    /**
     * Postponing A by 2 in [A, B, C, D] gives [B, C, A, D] with B current.
     */
    public void testPostponeCurrent()
    {
        addFour();
        checker.postponeCurrent(2);
        assertEquals("BCAD", order());
        assertSame(taskB, checker.getCurrentTask());
        assertEquals(0, checker.getCurrentPosition());
    }


    /**
     * Spots below 1 or past the end throw, and nothing changes or is recorded.
     */
    public void testPostponeCurrentBadSpots()
    {
        addFour();
        int[] bad = { 0, -1, 4 };
        for (int spots : bad)
        {
            try
            {
                checker.postponeCurrent(spots);
                fail("Expected IllegalArgumentException");
            }
            catch (IllegalArgumentException e)
            {
                // expected
            }
        }
        assertEquals("ABCD", order());
        assertFalse(checker.getReplay().contains("Postponed"));
    }


    /**
     * Postponing with no tasks throws IllegalStateException.
     */
    public void testPostponeCurrentEmpty()
    {
        try
        {
            checker.postponeCurrent(1);
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    // ----- pullForward(int) -----


    /**
     * Pulling forward 2 in [A, B, C] gives [C, A, B] with C current.
     */
    public void testPullForward()
    {
        checker.addTask(taskA);
        checker.addTask(taskB);
        checker.addTask(taskC);
        checker.pullForward(2);
        assertEquals("CAB", order());
        assertSame(taskC, checker.getCurrentTask());
        assertEquals(0, checker.getCurrentPosition());
    }


    /**
     * Zero, negative, or too large throws and changes nothing.
     */
    public void testPullForwardBadSpots()
    {
        checker.addTask(taskA);
        checker.addTask(taskB);
        int[] bad = { 0, -1, 2 };
        for (int spots : bad)
        {
            try
            {
                checker.pullForward(spots);
                fail("Expected IllegalArgumentException");
            }
            catch (IllegalArgumentException e)
            {
                // expected
            }
        }
        assertEquals("AB", order());
    }


    /**
     * Pulling forward with no tasks throws IllegalStateException.
     */
    public void testPullForwardEmpty()
    {
        try
        {
            checker.pullForward(1);
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    // ----- canUndo() / canRedo() -----


    /**
     * After one action, undo is possible and redo is not. After an undo, redo
     * is possible and undo is not.
     */
    public void testCanUndoAndCanRedo()
    {
        checker.addTask(taskA);
        assertTrue(checker.canUndo());
        assertFalse(checker.canRedo());
        checker.undo();
        assertFalse(checker.canUndo());
        assertTrue(checker.canRedo());
    }


    /**
     * A new checker can do neither.
     */
    public void testCanUndoAndCanRedoNew()
    {
        assertFalse(checker.canUndo());
        assertFalse(checker.canRedo());
    }

    // ----- undo() / redo() -----


    /**
     * Undoing an add removes the task; redoing puts it back where it was.
     */
    public void testUndoRedoAdd()
    {
        checker.addTask(taskA);
        checker.addTask(taskB, 0);
        assertEquals("BA", order());
        checker.undo();
        assertEquals("A", order());
        assertSame(taskA, checker.getCurrentTask());
        checker.undo();
        assertTrue(checker.isEmpty());
        assertEquals(-1, checker.getCurrentPosition());
        checker.redo();
        assertEquals("A", order());
        checker.redo();
        assertEquals("BA", order());
        assertSame(taskA, checker.getCurrentTask());
        assertEquals(1, checker.getCurrentPosition());
    }


    /**
     * Undoing a complete puts the task back at its old spot and restores the
     * cursor; redoing completes it again.
     */
    public void testUndoRedoComplete()
    {
        addFour();
        checker.nextTask();
        checker.completeCurrent();
        assertEquals("ACD", order());
        assertSame(taskC, checker.getCurrentTask());
        checker.undo();
        assertEquals("ABCD", order());
        assertSame(taskB, checker.getCurrentTask());
        assertEquals(1, checker.getCurrentPosition());
        checker.redo();
        assertEquals("ACD", order());
        assertSame(taskC, checker.getCurrentTask());
    }


    /**
     * Completing the last task wraps the cursor; undo puts the cursor back on
     * the restored task.
     */
    public void testUndoCompleteOfLastTaskRestoresCursor()
    {
        checker.addTask(taskA);
        checker.addTask(taskB);
        checker.nextTask();
        checker.completeCurrent();
        assertSame(taskA, checker.getCurrentTask());
        checker.undo();
        assertEquals("AB", order());
        assertSame(taskB, checker.getCurrentTask());
        checker.redo();
        assertSame(taskA, checker.getCurrentTask());
    }


    /**
     * Completing the only task, then undoing and redoing it, works.
     */
    public void testUndoRedoOnlyTask()
    {
        checker.addTask(taskA);
        checker.completeCurrent();
        assertTrue(checker.isEmpty());
        checker.undo();
        assertSame(taskA, checker.getCurrentTask());
        checker.redo();
        assertTrue(checker.isEmpty());
        assertEquals(-1, checker.getCurrentPosition());
    }


    /**
     * Undoing and redoing a delete.
     */
    public void testUndoRedoDelete()
    {
        addFour();
        checker.deleteCurrent();
        assertEquals("BCD", order());
        checker.undo();
        assertEquals("ABCD", order());
        assertSame(taskA, checker.getCurrentTask());
        checker.redo();
        assertEquals("BCD", order());
    }


    /**
     * Undoing and redoing a postpone.
     */
    public void testUndoRedoPostpone()
    {
        addFour();
        checker.postponeCurrent(2);
        checker.undo();
        assertEquals("ABCD", order());
        assertSame(taskA, checker.getCurrentTask());
        checker.redo();
        assertEquals("BCAD", order());
        assertSame(taskB, checker.getCurrentTask());
    }


    /**
     * Undoing and redoing a pull forward.
     */
    public void testUndoRedoPullForward()
    {
        checker.addTask(taskA);
        checker.addTask(taskB);
        checker.addTask(taskC);
        checker.pullForward(2);
        checker.undo();
        assertEquals("ABC", order());
        assertSame(taskA, checker.getCurrentTask());
        checker.redo();
        assertEquals("CAB", order());
        assertSame(taskC, checker.getCurrentTask());
    }


    /**
     * A new action after an undo discards the undone Moves.
     */
    public void testNewActionAfterUndoClearsRedo()
    {
        addFour();
        checker.completeCurrent();
        checker.undo();
        assertTrue(checker.canRedo());
        checker.deleteCurrent();
        assertFalse(checker.canRedo());
        assertEquals(5, checker.getReplay().split("\n").length);
    }


    /**
     * Undo or redo with nothing available throws IllegalStateException.
     */
    public void testUndoRedoNothing()
    {
        try
        {
            checker.undo();
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
        try
        {
            checker.redo();
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }


    /**
     * Undo does not affect reminders.
     */
    public void testUndoDoesNotTouchReminders()
    {
        checker.addReminder("Call mom");
        checker.addTask(taskA);
        checker.undo();
        assertEquals(1, checker.getReminders().length);
    }

    // ----- getReplay() -----


    /**
     * One numbered line per applied Move; an undone Move is not listed.
     */
    public void testGetReplay()
    {
        checker.addTask(taskA);
        checker.completeCurrent();
        String[] lines = checker.getReplay().split("\n");
        assertEquals(2, lines.length);
        assertTrue(lines[0].startsWith("1. "));
        assertTrue(lines[1].startsWith("2. "));
        checker.undo();
        assertEquals(1, checker.getReplay().split("\n").length);
    }


    /**
     * No actions gives the empty message.
     */
    public void testGetReplayEmpty()
    {
        assertEquals("No actions today.", checker.getReplay());
    }

    // ----- mostPostponed() -----


    /**
     * The task postponed the most times is returned.
     */
    public void testMostPostponed()
    {
        checker.addTask(taskA);
        checker.addTask(taskB);
        checker.addTask(taskC);
        checker.postponeCurrent(1);
        checker.nextTask();
        checker.postponeCurrent(1);
        assertSame(taskA, checker.mostPostponed());
    }


    /**
     * No postpones gives null.
     */
    public void testMostPostponedNone()
    {
        checker.addTask(taskA);
        assertNull(checker.mostPostponed());
    }


    /**
     * Two different tasks with the same name are counted separately.
     */
    public void testMostPostponedDuplicateNames()
    {
        Task gym1 = new Task("Gym", "");
        Task gym2 = new Task("Gym", "");
        checker.addTask(gym1);
        checker.addTask(gym2);
        checker.addTask(taskA);
        checker.postponeCurrent(1);
        checker.postponeCurrent(1);
        checker.nextTask();
        checker.postponeCurrent(1);
        assertSame(gym2, checker.mostPostponed());
    }

    // ----- removeReminder(int) -----


    /**
     * Removing index 0 returns that reminder and keeps the rest in order.
     */
    public void testRemoveReminder()
    {
        checker.addReminder("a");
        checker.addReminder("b");
        assertEquals("a", checker.removeReminder(0));
        assertEquals(1, checker.getReminders().length);
        assertEquals("b", checker.getReminders()[0]);
    }


    /**
     * An index past the end or below 0 throws.
     */
    public void testRemoveReminderBadPosition()
    {
        checker.addReminder("a");
        try
        {
            checker.removeReminder(5);
            fail("Expected IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException e)
        {
            // expected
        }
        try
        {
            checker.removeReminder(-1);
            fail("Expected IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException e)
        {
            // expected
        }
        assertEquals(1, checker.getReminders().length);
    }
}

/**
 * // -------------------------------------------------------------------------
 * /** Tests Move: stored values, validation, and replay lines.
 */
public class MoveTest
    extends student.TestCase
{
    private Task gym;

    /**
     * Creates a task to use in the Moves.
     */
    public void setUp()
    {
        gym = new Task("Gym", "");
    }


    /**
     * The constructor stores all six values and the getters return them.
     */
    public void testConstructorAndGetters()
    {
        Move move = new Move("postpone", gym, 2, 4, 2, 2);
        assertEquals("postpone", move.getType());
        assertSame(gym, move.getTask());
        assertEquals(2, move.getFromIndex());
        assertEquals(4, move.getToIndex());
        assertEquals(2, move.getCursorBefore());
        assertEquals(2, move.getCursorAfter());
    }


    /**
     * Getters give the same values every time (a Move never changes).
     */
    public void testGettersAreStable()
    {
        Move move = new Move("add", gym, -1, 0, -1, 0);
        assertEquals(move.getToIndex(), move.getToIndex());
        assertEquals(-1, move.getFromIndex());
        assertEquals(-1, move.getCursorBefore());
    }


    /**
     * All five valid types are accepted.
     */
    public void testAllFiveTypesAreAccepted()
    {
        String[] types = { "add", "complete", "delete", "postpone",
            "pull forward" };
        for (String type : types)
        {
            assertEquals(type, new Move(type, gym, 0, 0, 0, 0).getType());
        }
    }


    /**
     * A null task throws.
     */
    public void testConstructorRejectsNullTask()
    {
        try
        {
            new Move("add", null, 0, 0, 0, 0);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }


    /**
     * An unknown or null type throws.
     */
    public void testConstructorRejectsBadType()
    {
        try
        {
            new Move("rename", gym, 0, 0, 0, 0);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        try
        {
            new Move(null, gym, 0, 0, 0, 0);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }


    /**
     * A postpone prints positions starting from 1.
     */
    public void testToStringPostpone()
    {
        Move move = new Move("postpone", gym, 1, 3, 1, 1);
        assertEquals("Postponed \"Gym\" from #2 to #4", move.toString());
    }


    /**
     * Each other type has its own wording.
     */
    public void testToStringOtherTypes()
    {
        assertEquals("Added \"Gym\" at #3",
            new Move("add", gym, -1, 2, 0, 0).toString());
        assertEquals("Completed \"Gym\"",
            new Move("complete", gym, 0, -1, 0, 0).toString());
        assertEquals("Deleted \"Gym\"",
            new Move("delete", gym, 0, -1, 0, 0).toString());
        assertEquals("Pulled forward \"Gym\" from #4 to #2",
            new Move("pull forward", gym, 3, 1, 1, 1).toString());
    }
}

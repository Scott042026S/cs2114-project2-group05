public class TaskTest
    extends student.TestCase
{
    /**
     * Tests trimming, getters, and toString.
     */
    public void testConstructorTrimsAndGettersFormat()
    {
        Task task = new Task(" Laundry ", " Wash clothes ");

        assertEquals("Laundry", task.getName());
        assertEquals("Wash clothes", task.getDescription());
        assertEquals("Laundry", task.toString());
    }


    /**
     * Tests that a null name is rejected.
     */
    public void testConstructorRejectsNullName()
    {
        try
        {
            new Task(null, "");
            fail("A null name should throw an exception.");
        }
        catch (IllegalArgumentException exception)
        {
            // Expected exception.
        }
    }


    /**
     * Tests that a blank name is rejected.
     */
    public void testConstructorRejectsBlankName()
    {
        try
        {
            new Task(" \t ", "");
            fail("A blank name should throw an exception.");
        }
        catch (IllegalArgumentException exception)
        {
            // Expected exception.
        }
    }


    /**
     * Tests that names longer than 60 characters are rejected.
     */
    public void testConstructorRejectsLongName()
    {
        try
        {
            new Task(repeat('a', 61), "");
            fail("A long name should throw an exception.");
        }
        catch (IllegalArgumentException exception)
        {
            // Expected exception.
        }
    }


    /**
     * Tests that descriptions longer than 200 characters are rejected.
     */
    public void testConstructorRejectsLongDescription()
    {
        try
        {
            new Task("A", repeat('d', 201));
            fail("A long description should throw an exception.");
        }
        catch (IllegalArgumentException exception)
        {
            // Expected exception.
        }
    }


    /**
     * Tests names and descriptions at their exact length limits.
     */
    public void testGettersAcceptExactLimits()
    {
        Task task = new Task(repeat('a', 60), repeat('d', 200));

        assertEquals(60, task.getName().length());
        assertEquals(200, task.getDescription().length());
        assertEquals(task.getName(), task.toString());
    }


    /**
     * Tests that a null description becomes an empty string.
     */
    public void testNullDescription()
    {
        Task task = new Task("A", null);

        assertEquals("", task.getDescription());
    }


    /**
     * Tests that duplicate names can belong to separate objects.
     */
    public void testDuplicateNamesRemainDifferentTasks()
    {
        Task first = new Task("A", "");
        Task second = new Task("A", "");

        assertNotSame(first, second);
    }


    /**
     * Builds a string containing a repeated character.
     *
     * @param value
     *            the character to repeat
     * @param count
     *            the number of repetitions
     * @return the resulting string
     */
    private String repeat(char value, int count)
    {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < count; i++)
        {
            text.append(value);
        }

        return text.toString();
    }
}

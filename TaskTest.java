public class TaskTest
{
    @Test
    public void constructorTrimsAndGettersFormat()
    {
        Task task = new Task(" Laundry ", " Wash clothes ");
        assertEquals("Laundry", task.getName());
        assertEquals("Wash clothes", task.getDescription());
        assertEquals("Laundry", task.toString());
    }


    @Test
    public void constructorRejectsNullName()
    {
        assertThrows(IllegalArgumentException.class, () -> new Task(null, ""));
    }


    @Test
    public void constructorRejectsBlankName()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Task(" \t ", ""));
    }


    @Test
    public void constructorRejectsLongName()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Task(repeat('a', 61), ""));
    }


    @Test
    public void constructorRejectsLongDescription()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Task("A", repeat('d', 201)));
    }


    @Test
    public void gettersAcceptExactLimitsAndNullDescription()
    {
        Task max = new Task(repeat('a', 60), repeat('d', 200));
        assertEquals(60, max.getName().length());
        assertEquals(200, max.getDescription().length());
        assertEquals(max.getName(), max.toString());
        assertEquals("", new Task("A", null).getDescription());
    }


    @Test
    public void duplicateNamesRemainDifferentTasks()
    {
        assertNotSame(new Task("A", ""), new Task("A", ""));
    }


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

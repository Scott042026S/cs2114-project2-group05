import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for CompletionCheckerApp: prompts, menu actions, invalid-input
 * recovery, and clean exit. Input is a scripted String (one line per Enter) and
 * output is captured, so no keyboard is needed. The private read methods are
 * tested through run().
 */
class CompletionCheckerAppTest {

    // Menu numbers (must match CompletionCheckerApp)
    private static final String ADD = "1";
    private static final String ADD_AT = "2";
    private static final String COMPLETE = "3";
    private static final String DELETE = "4";
    private static final String POSTPONE = "5";
    private static final String PULL = "6";
    private static final String NEXT = "7";
    private static final String VIEW = "8";
    private static final String UNDO = "9";
    private static final String REDO = "10";
    private static final String ADD_REMINDER = "11";
    private static final String VIEW_REMINDERS = "12";
    private static final String REMOVE_REMINDER = "13";
    private static final String END_DAY = "14";

    private static final String NO_TASKS = "You have no tasks. Add one first.";

    private CompletionChecker checker;

    @BeforeEach
    void setUp() {
        checker = new CompletionChecker();
    }

    /** Runs the app on the given input lines and returns everything it printed. */
    private String run(String... lines) {
        String script = lines.length == 0 ? "" : String.join("\n", lines) + "\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true);
        new CompletionCheckerApp(new Scanner(script), checker, out).run();
        return bytes.toString();
    }

    private static int count(String text, String part) {
        int total = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + 1)) {
            total++;
        }
        return total;
    }

    private String[] taskNames() {
        Task[] tasks = checker.getTasks();
        String[] names = new String[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            names[i] = tasks[i].getName();
        }
        return names;
    }

    // ---------- Constructor ----------

    @Test
    void constructorAcceptsValidArguments() {
        assertDoesNotThrow(() -> new CompletionCheckerApp(new Scanner("14\n"), checker));
    }

    @Test
    void constructorRejectsNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> new CompletionCheckerApp(null, checker));
        assertThrows(IllegalArgumentException.class, () -> new CompletionCheckerApp(new Scanner("x"), null));
        assertThrows(IllegalArgumentException.class,
                () -> new CompletionCheckerApp(new Scanner("x"), checker, null));
    }

    // ---------- run() ----------

    @Test
    void addCompleteEndDayPrintsReplayAndExitsCleanly() {
        String output = run(ADD, "Laundry", "Wash clothes", COMPLETE, END_DAY);

        assertTrue(output.contains("Added \"Laundry\"."));
        assertTrue(output.contains("Current task (1 of 1): Laundry"));
        assertTrue(output.contains("Wash clothes"));
        assertTrue(output.contains("Completed \"Laundry\"."));
        assertTrue(output.contains("Your day:"));
        assertTrue(output.contains("1. "));
        assertTrue(output.contains("2. "));
        assertTrue(output.contains("No tasks were postponed."));
        assertTrue(checker.isEmpty());
    }

    @Test
    void endDayReportsTheMostPostponedTask() {
        String output = run(
                ADD, "Gym", "", ADD, "Mail", "", ADD, "Chores", "",
                POSTPONE, "1",   // [Mail, Gym, Chores], Mail is current
                NEXT,            // Gym is current again
                POSTPONE, "1",   // [Mail, Chores, Gym]
                END_DAY);

        assertTrue(output.contains("Most postponed task: Gym"));
    }

    @Test
    void unknownChoiceAndLettersAreRejectedThenTheAppContinues() {
        String output = run("99", "abc", END_DAY);

        assertTrue(output.contains("Choose a number from the menu."));
        assertTrue(output.contains("Please enter a whole number."));
        assertTrue(output.contains("No actions today."));
    }

    @Test
    void inputEndingEarlyDoesNotCrash() {
        String midPrompt = assertDoesNotThrow(() -> run(ADD, "Laundry"));
        assertTrue(midPrompt.contains("Input ended."));
        assertTrue(checker.isEmpty());

        String noInput = assertDoesNotThrow(() -> run());
        assertTrue(noInput.contains("Input ended."));
        assertTrue(noInput.contains("No actions today."));
    }

    @Test
    void currentTaskIsShownWithItsPosition() {
        String output = run(ADD, "A", "", ADD, "B", "", NEXT, END_DAY);

        assertTrue(output.contains("Current task (1 of 2): A"));
        assertTrue(output.contains("Current task (2 of 2): B"));
    }

    // ---------- readMenuChoice() (through run) ----------

    @Test
    void validMenuNumberRunsThatAction() {
        String output = run(VIEW, END_DAY);

        assertTrue(output.contains("You have no tasks."));
    }

    @Test
    void menuChoiceZeroNegativeLettersAndBlankAreAllReRequested() {
        String output = run("0", "-1", "x", "", "99999999999", END_DAY);

        assertEquals(2, count(output, "Choose a number from the menu."));
        assertEquals(3, count(output, "Please enter a whole number."));
        assertTrue(output.contains("Your day:"));
    }

    // ---------- readText() (through run) ----------

    @Test
    void validNameIsAcceptedAndTrimmed() {
        run(ADD, "  Laundry  ", "", END_DAY);

        assertArrayEquals(new String[] {"Laundry"}, taskNames());
    }

    @Test
    void blankNameIsRejectedThenReAsked() {
        String output = run(ADD, "", "   ", "Laundry", "", END_DAY);

        assertEquals(2, count(output, "Task name can't be blank."));
        assertArrayEquals(new String[] {"Laundry"}, taskNames());
    }

    @Test
    void nameOverSixtyCharactersIsRejectedButSixtyIsAccepted() {
        String sixtyOne = "x".repeat(61);
        String sixty = "y".repeat(60);
        String output = run(ADD, sixtyOne, sixty, "", END_DAY);

        assertEquals(1, count(output, "Keep names to 60 characters."));
        assertArrayEquals(new String[] {sixty}, taskNames());
    }

    @Test
    void descriptionOverTwoHundredCharactersIsRejectedAndBlankIsAllowed() {
        String output = run(ADD, "Laundry", "d".repeat(201), "", END_DAY);

        assertEquals(1, count(output, "Keep descriptions to 200 characters."));
        assertEquals(1, checker.size());
        assertEquals("", checker.getCurrentTask().getDescription());
    }

    @Test
    void blankReminderIsRejectedThenReAsked() {
        String output = run(ADD_REMINDER, "", "  Call mom ", END_DAY);

        assertEquals(1, count(output, "Reminder can't be blank."));
        assertArrayEquals(new String[] {"Call mom"}, checker.getReminders());
    }

    // ---------- readInt() (through run) ----------

    @Test
    void addAtPositionInsertsWhereTheUserAsked() {
        run(ADD, "A", "", ADD, "C", "", ADD_AT, "B", "", "2", END_DAY);

        assertArrayEquals(new String[] {"A", "B", "C"}, taskNames());
    }

    @Test
    void addPositionOutOfRangeOrNotANumberIsReAsked() {
        String output = run(ADD, "A", "", ADD, "C", "", ADD_AT, "B", "", "0", "9", "two", "2", END_DAY);

        assertEquals(2, count(output, "Enter a position from 1 to 3."));
        assertTrue(output.contains("Please enter a whole number."));
        assertArrayEquals(new String[] {"A", "B", "C"}, taskNames());
    }

    @Test
    void postponeMovesTheCurrentTaskLater() {
        String output = run(ADD, "A", "", ADD, "B", "", ADD, "C", "", POSTPONE, "2", END_DAY);

        assertTrue(output.contains("Postponed \"A\" by 2 places."));
        assertArrayEquals(new String[] {"B", "C", "A"}, taskNames());
        assertEquals("B", checker.getCurrentTask().getName());
    }

    @Test
    void postponeNumberNotOnTheUpcomingListIsReAsked() {
        String output = run(ADD, "A", "", ADD, "B", "", ADD, "C", "", POSTPONE, "0", "3", "x", "2", END_DAY);

        assertEquals(2, count(output, "Enter a number from 1 to 2."));
        assertTrue(output.contains("Please enter a whole number."));
        assertArrayEquals(new String[] {"B", "C", "A"}, taskNames());
    }

    @Test
    void pullForwardBringsALaterTaskToTheFront() {
        String output = run(ADD, "A", "", ADD, "B", "", ADD, "C", "", PULL, "2", END_DAY);

        assertTrue(output.contains("Pulled \"C\" forward."));
        assertArrayEquals(new String[] {"C", "A", "B"}, taskNames());
        assertEquals("C", checker.getCurrentTask().getName());
    }

    @Test
    void pullForwardNumberNotOnTheUpcomingListIsReAsked() {
        String output = run(ADD, "A", "", ADD, "B", "", ADD, "C", "", PULL, "9", "2", END_DAY);

        assertEquals(1, count(output, "Enter a number from 1 to 2."));
        assertArrayEquals(new String[] {"C", "A", "B"}, taskNames());
    }

    @Test
    void removeReminderOutOfRangeIsReAsked() {
        String output = run(ADD_REMINDER, "Call mom", REMOVE_REMINDER, "5", "1", END_DAY);

        assertEquals(1, count(output, "Enter a number from 1 to 1."));
        assertTrue(output.contains("Removed reminder: Call mom"));
        assertEquals(0, checker.getReminders().length);
    }

    // ---------- Menu-level checks in run() ----------

    @Test
    void actionsThatNeedTasksAreRefusedWhenThereAreNone() {
        String output = run(COMPLETE, DELETE, POSTPONE, PULL, NEXT, END_DAY);

        assertEquals(5, count(output, NO_TASKS));
        assertFalse(checker.canUndo());
    }

    @Test
    void postponeAndPullOnTheLastTaskAreRefusedWithoutPrompting() {
        String output = run(ADD, "A", "", POSTPONE, PULL, END_DAY);

        assertTrue(output.contains("This is already your last task."));
        assertTrue(output.contains("No later tasks to pull forward."));
        assertFalse(checker.getReplay().contains("Postponed"));
    }

    @Test
    void undoAndRedoWithNothingAvailableArePoliteNoOps() {
        String output = run(UNDO, REDO, END_DAY);

        assertTrue(output.contains("Nothing to undo."));
        assertTrue(output.contains("Nothing to redo."));
    }

    @Test
    void undoRestoresACompletedTaskAndRedoCompletesItAgain() {
        String output = run(ADD, "A", "", COMPLETE, UNDO, END_DAY);
        assertTrue(output.contains("Undid:"));
        assertArrayEquals(new String[] {"A"}, taskNames());

        setUp();
        String again = run(ADD, "A", "", COMPLETE, UNDO, REDO, END_DAY);
        assertTrue(again.contains("Redid:"));
        assertTrue(checker.isEmpty());
    }

    @Test
    void deleteRemovesTheCurrentTask() {
        String output = run(ADD, "A", "", ADD, "B", "", DELETE, END_DAY);

        assertTrue(output.contains("Deleted \"A\"."));
        assertArrayEquals(new String[] {"B"}, taskNames());
    }

    @Test
    void nextWrapsAroundToTheFirstTask() {
        run(ADD, "A", "", ADD, "B", "", NEXT, NEXT, END_DAY);

        assertEquals("A", checker.getCurrentTask().getName());
    }

    @Test
    void viewTasksListsThemInOrder() {
        String output = run(ADD, "A", "", ADD, "B", "", VIEW, END_DAY);

        assertTrue(output.contains("1. A"));
        assertTrue(output.contains("2. B"));
    }

    @Test
    void reminderCommandsWorkAndHandleTheEmptyCase() {
        String empty = run(VIEW_REMINDERS, REMOVE_REMINDER, END_DAY);
        assertEquals(2, count(empty, "You have no reminders."));

        setUp();
        String output = run(ADD_REMINDER, "Call mom", VIEW_REMINDERS, END_DAY);
        assertTrue(output.contains("Reminder added."));
        assertTrue(output.contains("1. Call mom"));
    }

    @Test
    void reminderChangesAreNotUndone() {
        run(ADD_REMINDER, "Call mom", UNDO, END_DAY);

        assertEquals(1, checker.getReminders().length);
    }

    // ---------- main() ----------

    private String runMain(String stdin) {
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(stdin.getBytes()));
            System.setOut(new PrintStream(bytes, true));
            CompletionCheckerApp.main(new String[0]);
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
        return bytes.toString();
    }

    @Test
    void mainStartsTheAppAndExitsOnEndDay() {
        String output = runMain(END_DAY + "\n");

        assertTrue(output.contains("No actions today."));
    }

    @Test
    void mainSurvivesInputThatEndsWithoutEndDay() {
        String output = assertDoesNotThrow(() -> runMain(""));

        assertTrue(output.contains("Input ended."));
    }
}

import java.io.PrintStream;
import java.util.Scanner;

/**
 * Console front end for CompletionChecker.
 *
 * Shows the current task and a menu, reads the user's choice, calls
 * CompletionChecker, and prints the result. Every bad input is caught here
 * (readMenuChoice, readInt, readText, or a check in run()) before it reaches the
 * other classes, so the user never sees a crash. Positions shown to the user
 * start at 1; CompletionChecker uses 0-based positions, so this class converts.
 */
public class CompletionCheckerApp {

    // Menu numbers
    private static final int ADD_TASK = 1;
    private static final int ADD_TASK_AT = 2;
    private static final int COMPLETE = 3;
    private static final int DELETE = 4;
    private static final int POSTPONE = 5;
    private static final int PULL_FORWARD = 6;
    private static final int NEXT_TASK = 7;
    private static final int VIEW_TASKS = 8;
    private static final int UNDO = 9;
    private static final int REDO = 10;
    private static final int ADD_REMINDER = 11;
    private static final int VIEW_REMINDERS = 12;
    private static final int REMOVE_REMINDER = 13;
    private static final int END_DAY = 14;

    private static final int MAX_NAME_LENGTH = 60;
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    private static final String NO_TASKS = "You have no tasks. Add one first.";

    private final Scanner input;
    private final CompletionChecker checker;
    private final PrintStream out;

    /** Thrown internally when the input runs out (for example Ctrl+D or a short script). */
    private static class InputEnded extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    /** Stores both and prints to System.out. Throws IllegalArgumentException if either is null. */
    public CompletionCheckerApp(Scanner input, CompletionChecker checker) {
        this(input, checker, System.out);
    }

    /** Same, but prints to the given stream (lets tests capture the output). */
    public CompletionCheckerApp(Scanner input, CompletionChecker checker, PrintStream out) {
        if (input == null || checker == null || out == null) {
            throw new IllegalArgumentException("input, checker, and output cannot be null");
        }
        this.input = input;
        this.checker = checker;
        this.out = out;
    }

    /**
     * Loops: show the current task and menu, read a choice, call
     * CompletionChecker, print the result. On "end day" prints the replay and
     * the most-postponed task, then stops. If the input runs out, the day is
     * ended the same way.
     */
    public void run() {
        try {
            boolean running = true;
            while (running) {
                showCurrentTask();
                showMenu();
                int choice = readMenuChoice();
                try {
                    running = handle(choice);
                } catch (InputEnded e) {
                    throw e;
                } catch (RuntimeException e) {
                    // Second line of defense: the checks below should prevent this.
                    out.println("Something went wrong: " + e.getMessage());
                }
            }
        } catch (InputEnded e) {
            out.println();
            out.println("Input ended.");
            endDay();
        }
    }

    // ------------------------------------------------------------------
    // Menu actions
    // ------------------------------------------------------------------

    /** Runs one menu action. Returns false when the day has ended. */
    private boolean handle(int choice) {
        switch (choice) {
            case ADD_TASK -> addTask(false);
            case ADD_TASK_AT -> addTask(true);
            case COMPLETE -> {
                if (noTasks()) break;
                out.println("Completed \"" + checker.completeCurrent() + "\".");
                if (checker.isEmpty()) out.println("No tasks left.");
            }
            case DELETE -> {
                if (noTasks()) break;
                out.println("Deleted \"" + checker.deleteCurrent() + "\".");
                if (checker.isEmpty()) out.println("No tasks left.");
            }
            case POSTPONE -> postpone();
            case PULL_FORWARD -> pullForward();
            case NEXT_TASK -> {
                if (noTasks()) break;
                checker.nextTask();
            }
            case VIEW_TASKS -> viewTasks();
            case UNDO -> {
                if (!checker.canUndo()) {
                    out.println("Nothing to undo.");
                } else {
                    out.println("Undid: " + checker.undo());
                }
            }
            case REDO -> {
                if (!checker.canRedo()) {
                    out.println("Nothing to redo.");
                } else {
                    out.println("Redid: " + checker.redo());
                }
            }
            case ADD_REMINDER -> {
                String text = readText("Reminder: ", "Reminder can't be blank.", null, Integer.MAX_VALUE);
                checker.addReminder(text);
                out.println("Reminder added.");
            }
            case VIEW_REMINDERS -> viewReminders();
            case REMOVE_REMINDER -> removeReminder();
            case END_DAY -> {
                endDay();
                return false;
            }
            default -> out.println("Choose a number from the menu.");
        }
        return true;
    }

    private void addTask(boolean chooseSpot) {
        String name = readText("Task name: ", "Task name can't be blank.",
                "Keep names to " + MAX_NAME_LENGTH + " characters.", MAX_NAME_LENGTH);
        String description = readText("Description (optional): ", null,
                "Keep descriptions to " + MAX_DESCRIPTION_LENGTH + " characters.", MAX_DESCRIPTION_LENGTH);
        Task task = new Task(name, description);

        if (chooseSpot) {
            int last = checker.size() + 1;
            int position = readInt("Position (1 to " + last + "): ", 1, last, "position");
            checker.addTask(task, position - 1);
        } else {
            checker.addTask(task);
        }
        out.println("Added \"" + name + "\".");
    }

    private void postpone() {
        if (noTasks()) return;
        Task[] upcoming = checker.getUpcoming();
        if (upcoming.length == 0) {
            out.println("This is already your last task.");
            return;
        }
        String name = checker.getCurrentTask().getName();
        printNumbered("Upcoming tasks:", upcoming);
        int spots = readInt("Move \"" + name + "\" back how many places (1 to " + upcoming.length + ")? ",
                1, upcoming.length, "number");
        checker.postponeCurrent(spots);
        out.println("Postponed \"" + name + "\" by " + spots + (spots == 1 ? " place." : " places."));
    }

    private void pullForward() {
        if (noTasks()) return;
        Task[] upcoming = checker.getUpcoming();
        if (upcoming.length == 0) {
            out.println("No later tasks to pull forward.");
            return;
        }
        printNumbered("Upcoming tasks:", upcoming);
        int which = readInt("Pull which task forward (1 to " + upcoming.length + ")? ",
                1, upcoming.length, "number");
        checker.pullForward(which);
        out.println("Pulled \"" + upcoming[which - 1] + "\" forward.");
    }

    private void viewTasks() {
        Task[] tasks = checker.getTasks();
        if (tasks.length == 0) {
            out.println("You have no tasks.");
            return;
        }
        printNumbered("Your tasks:", tasks);
    }

    private void viewReminders() {
        String[] reminders = checker.getReminders();
        if (reminders.length == 0) {
            out.println("You have no reminders.");
            return;
        }
        printNumbered("Your reminders:", reminders);
    }

    private void removeReminder() {
        String[] reminders = checker.getReminders();
        if (reminders.length == 0) {
            out.println("You have no reminders.");
            return;
        }
        printNumbered("Your reminders:", reminders);
        int which = readInt("Remove which reminder (1 to " + reminders.length + ")? ",
                1, reminders.length, "number");
        out.println("Removed reminder: " + checker.removeReminder(which - 1));
    }

    private void endDay() {
        out.println();
        out.println("Your day:");
        out.println(checker.getReplay());
        Task most = checker.mostPostponed();
        if (most == null) {
            out.println("No tasks were postponed.");
        } else {
            out.println("Most postponed task: " + most);
        }
    }

    /** Prints the no-tasks message and returns true if there are no tasks. */
    private boolean noTasks() {
        if (checker.isEmpty()) {
            out.println(NO_TASKS);
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Display
    // ------------------------------------------------------------------

    private void showCurrentTask() {
        out.println();
        if (checker.isEmpty()) {
            out.println("No tasks yet.");
            return;
        }
        Task current = checker.getCurrentTask();
        out.println("Current task (" + (checker.getCurrentPosition() + 1) + " of "
                + checker.size() + "): " + current.getName());
        if (!current.getDescription().isEmpty()) {
            out.println("  " + current.getDescription());
        }
    }

    private void showMenu() {
        out.println("What would you like to do?");
        out.println("  1. Add a task to the end");
        out.println("  2. Add a task at a position");
        out.println("  3. Complete the current task");
        out.println("  4. Delete the current task");
        out.println("  5. Postpone the current task");
        out.println("  6. Pull a later task forward");
        out.println("  7. Go to the next task");
        out.println("  8. View all tasks");
        out.println("  9. Undo");
        out.println(" 10. Redo");
        out.println(" 11. Add a reminder");
        out.println(" 12. View reminders");
        out.println(" 13. Remove a reminder");
        out.println(" 14. End the day");
    }

    private void printNumbered(String heading, Object[] items) {
        out.println(heading);
        for (int i = 0; i < items.length; i++) {
            out.println("  " + (i + 1) + ". " + items[i]);
        }
    }

    // ------------------------------------------------------------------
    // Input reading and validation
    // ------------------------------------------------------------------

    private String readLine() {
        if (!input.hasNextLine()) {
            throw new InputEnded();
        }
        return input.nextLine();
    }

    /** Reads until the input is a number on the menu. */
    private int readMenuChoice() {
        while (true) {
            out.print("Choose an option: ");
            out.flush();
            String line = readLine().trim();
            try {
                int choice = Integer.parseInt(line);
                if (choice >= ADD_TASK && choice <= END_DAY) {
                    return choice;
                }
                out.println("Choose a number from the menu.");
            } catch (NumberFormatException e) {
                out.println("Please enter a whole number.");
            }
        }
    }

    /** Reads until the input is a whole number from min to max. noun is "position" or "number". */
    private int readInt(String prompt, int min, int max, String noun) {
        while (true) {
            out.print(prompt);
            out.flush();
            String line = readLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                out.println("Enter a " + noun + " from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                out.println("Please enter a whole number.");
            }
        }
    }

    /**
     * Reads and trims. Re-prompts if the text is blank (unless blankMessage is
     * null, in which case blank is allowed and returns "") or longer than maxLength.
     */
    private String readText(String prompt, String blankMessage, String tooLongMessage, int maxLength) {
        while (true) {
            out.print(prompt);
            out.flush();
            String text = readLine().trim();
            if (text.isEmpty()) {
                if (blankMessage == null) {
                    return "";
                }
                out.println(blankMessage);
            } else if (text.length() > maxLength) {
                out.println(tooLongMessage);
            } else {
                return text;
            }
        }
    }

    /** Creates a CompletionChecker and an app on System.in, then runs it. */
    public static void main(String[] args) {
        new CompletionCheckerApp(new Scanner(System.in), new CompletionChecker()).run();
    }
}

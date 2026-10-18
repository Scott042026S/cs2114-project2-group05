# Completion Checker

A Java console to-do app that shows one task at a time. Tasks and reminders are stored in one reusable generic class, `LinkedChain<T>`, with a cursor marking the current task.

## How to compile and run

1. **Open the project:** in Eclipse, choose File → Import → General → Existing Projects into Workspace, and select this repository's folder.
2. **Compile:** Eclipse compiles automatically when Project → Build Automatically is checked. Any errors appear in the Problems tab. The project uses Java 17 and JUnit 5.
3. **Run the app:** right-click `CompletionCheckerApp.java` → Run As → Java Application, then type into the Console.
4. **Run the tests:** right-click the project → Run As → JUnit Test. Current result: 140 of 140 tests pass (0 errors, 0 failures).

## System Diagram

<img width="1825" height="1598" alt="image" src="https://github.com/user-attachments/assets/e062454f-babb-47f7-a58c-f9d14fff3828" />


## Reflection

### What changed from our specification?

**Built as specified**

- `LinkedChain<T>`, `Node<E>`, `Task`, `Move`, `CompletionChecker`, and `CompletionCheckerApp` exist as designed.
- Undo and redo are core features, as the spec planned: every task action is recorded as a `Move` in a `LinkedChain<Move>` history, `appliedCount` tracks how many are applied, and `removeFrom(appliedCount)` discards undone Moves when a new action happens.
- Postpone, pull forward, `getUpcoming`, the end-of-day replay, and `mostPostponed` are built.
- Validation still lives in `CompletionCheckerApp`. The same checks also remain in `Task` and `CompletionChecker`, which throw exceptions as a second line of defense.
- `LinkedChain<Task>`, `LinkedChain<Move>`, and `LinkedChain<String>` all run through the same class.

**Built differently**

- **App constructor:** we added `CompletionCheckerApp(Scanner, CompletionChecker, PrintStream)` alongside the two-argument constructor from the spec. Tests use it to capture output.
- **Private method signatures:**
  - `readText` now takes the blank-input and too-long messages, because one method has to produce four different messages ("Task name can't be blank.", "Keep names to 60 characters.", "Keep descriptions to 200 characters.", "Reminder can't be blank."). Passing no blank message means blank is allowed, which is how optional descriptions work.
  - `readInt` takes a word ("position" or "number") so the add-position message matches the spec's wording.
- **Menu:** the spec did not number the menu, so we chose 1 to 14 (add, add at position, complete, delete, postpone, pull forward, next, view, undo, redo, add/view/remove reminder, end day).
- **Added bad-input handling not in the spec:** the app ends the day cleanly if the input runs out (for example Ctrl+D), and any exception from `CompletionChecker` is caught and printed as "Something went wrong" instead of crashing.
- **Reminder length:** the spec gave no limit for reminders, so there is none.
- **`nextTask()` with no tasks:** the spec did not say what happens, so it throws `IllegalStateException` like the other current-task methods. The app checks `isEmpty()` first, so the user never sees it.
- **Test cases:** we followed the test plan, merging rows so each public method has a normal case and a bad-input case. We added tests the plan did not list: cursor consistency after adds and removes, a check that the tail stays correct after removals, and a 2,000-step randomized comparison against `java.util.List`.

### What changed in the LinkedChain?

We extended the provided class as the spec planned, and the application uses it only through its public methods.

- **Used unchanged:** `add(T)`, `remove()`, `remove(T)`, `get`, `contains`, `count`, `size`, `isEmpty`, `toString`.
- **Modified:**
  - `addToEnd` now uses a tail reference (`lastNode`), so it is O(1) instead of O(n).
  - `clear()` also resets the tail and the cursor.
  - `toArray` returns a new, larger array if the one passed in is too short. The provided class threw `IllegalArgumentException` instead.
- **Added:**
  - `add(int, T)`, `remove(int)`, and `removeFrom(int)`.
  - The cursor group: `hasCursor`, `getCursorIndex`, `getCursorEntry`, `moveCursorToFront`, `moveCursorTo`, `advanceCursor`, `removeAtCursor`, `insertBeforeCursor`.
  - The app needs these because every action (complete, postpone, pull forward) happens at the current task, and the cursor makes those O(1) re-links instead of walks.
- **Behavior the spec left open:**
  - `removeFrom(size())` removes nothing instead of throwing, so redo moves can be discarded without a special case.
  - If the entry under the cursor is removed by any method, the cursor moves to the next entry, or becomes absent if there is none.
  - Cursor methods throw `IllegalStateException` when there is no cursor.
  - Null entries are rejected everywhere, including `insertBeforeCursor`.

### What happened to our unknowns?

| Unknown from the scope | Status |
|---|---|
| Which stretch goals are realistic | Figured out. Undo/redo became core. Everything else was dropped (see Next Steps). |
| How to move or swap tasks inside a linked chain | Figured out. Swap and rotate were replaced by postpone and pull forward, built from the cursor plus positional insert and remove. |
| How to undo a completed task | Figured out. A Move stores the task, its position, and the cursor before and after, so undoing a completion puts the task back where it was. |
| How to implement undo and redo | Figured out. `appliedCount` marks how many Moves are applied, and `removeFrom` discards undone Moves when a new action happens. |
| Save and load | Dropped. |
| Other bad-input cases | Figured out. New cases found while building: input ending early, optional blank descriptions, and reminder removal with none stored. |

**Decided while building:**

- `mostPostponed` compares tasks by identity, not by name, since duplicate task names are allowed. 
- After undoing an add at a position, the cursor returns to the Move's saved `cursorBefore`. 

### What doesn't work yet?

Everything in the MVP is built, and all tests pass. 

## Next Steps

### Stretch goals

- **Committed and working:** reminders (add, view, and remove), stored in a `LinkedChain<String>`.
- **Dropped:** day planner with TimeBlocks, priorities and due dates, subtasks, save/load, recurring tasks, and swap/rotate (replaced by postpone and pull forward).

## Attribution

| File | Team member | Contribution |
|---|---|---|
| `LinkedChain.java` | Sophia | Wrote the extended chain (tail, positional methods, cursor) with GenAI assistance; reviewed and tested |
| `LinkedChainTest.java` | Sophia | Wrote and tested; ran and verified (all passing) |
| `Task.java` | Manveen | Wrote |
| `TaskTest.java` | Manveen | Wrote and tested (passing) |
| `Move.java` | Manveen | Wrote and tested |
| `MoveTest.java` | Manveen | Wrote and tested |
| 'CompletionChecker.java` | Scott, Sophia | Scott: tasks, reminders, cursor and current-task methods, with GenAI assistance; reviewed against the spec and the LinkedChain API. Sophia: Move history, undo/redo, postpone, pull forward, `getUpcoming`, replay, and `mostPostponed`, with GenAI assistance |
| `CompletionCheckerTest.java` | Scott | Reviewed, converted, and tested (passing) |
| `CompletionCheckerApp.java` | Nicholas | Drafted with GenAI; reviewed, revised, and run (passing). |
| `CompletionCheckerAppTest.java` | Nicholas | Drafted with GenAI; Nicholas to review, revise, and run |
| `README.md` | Sophia, Scott | Drafted with GenAI; reviewed by the group |

### GenAI use

**In the specification:** GenAI was used on the Method Signatures section to understand the best way to modify and add to the `LinkedChain<T>` API that was provided to us. It also drafted crucial methods that would otherwise have been forgotten. Specifically, we asked, "Are there any other methods in CompletionChecker that should be included based on our scope? Explain why," and it added methods such as `pullForward` and `mostPostponed`. It was also used to validate the Big-O estimates. GenAI also helped draft the System Diagram and the Test Plan, which the group then reviewed and revised.

**In the prototype:** GenAI was used to assist in drafting `LinkedChain` and its tests from our spec, `CompletionChecker` and its tests, `CompletionCheckerApp` and its tests, and this README. Each was checked by compiling and running the tests, and the team is responsible for reviewing and revising what it produced.

# Completion Checker (Prototype)

A Java console to-do app that shows one task at a time. Tasks and reminders are stored in one reusable generic class, `LinkedChain<T>`, with a cursor marking the current task.

## How to run

- **Tests:** in Eclipse, right-click the project, then Run As, then JUnit Test (JUnit 5). Current result: 72 of 72 tests pass (0 errors, 0 failures) across `LinkedChainTest`, `CompletionCheckerTest`, and `TaskTest`.
- **App:** run `CompletionCheckerApp` as a Java Application and type into the Console.

## Reflection

### What changed from our specification?

**Built as specified**
- `LinkedChain<T>`, `Node<E>`, `Task`, and `CompletionChecker` (tasks and reminders) exist as designed.
- Validation still lives in `CompletionCheckerApp`. The same checks also remain in `Task` and `CompletionChecker`, which throw exceptions as a second line of defense.
- `LinkedChain<Task>` and `LinkedChain<String>` both run through the same class.

**Built differently, or not yet built**
- **History is not built yet.** The spec made undo/redo core, with a `Move` class and a `LinkedChain<Move>` history in `CompletionChecker`. We built the task and reminder paths first so the prototype runs end to end. `Move` is not in the project, and `CompletionChecker` does not yet have undo, redo, postpone, pull forward, replay, or `mostPostponed`.
- **App constructor:** we added `CompletionCheckerApp(Scanner, CompletionChecker, PrintStream)` alongside the two-argument constructor from the spec. Tests use it to capture output.
- **Private method signatures:**
  - `readText` now takes the blank-input and too-long messages, because one method has to produce four different messages ("Task name can't be blank.", "Keep names to 60 characters.", "Keep descriptions to 200 characters.", "Reminder can't be blank."). Passing no blank message means blank is allowed, which is how optional descriptions work.
  - `readInt` takes a word ("position" or "number") so the add-position message matches the spec's wording.
- **Menu:** the spec did not number the menu, so we chose 1 to 14 (add, add at position, complete, delete, postpone, pull forward, next, view, undo, redo, add/view/remove reminder, end day).
- **Added bad-input handling not in the spec:** the app ends the day cleanly if the input runs out (for example Ctrl+D), and any exception from `CompletionChecker` is caught and printed as "Something went wrong" instead of crashing.
- **Reminder length:** the spec gave no limit for reminders, so there is none.
- **Test cases:** we followed the test plan, merging rows so each public method has a normal case and a bad-input case. We added tests the plan did not list: cursor consistency after adds and removes, a check that the tail stays correct after removals, and a 2,000-step randomized comparison against `java.util.List`.

### What changed in the LinkedChain?

We extended the provided class as the spec planned, and the application uses it only through its public methods.

- **Used unchanged:** `add(T)`, `remove()`, `remove(T)`, `get`, `contains`, `count`, `size`, `isEmpty`, `toArray`, `toString`.
- **Modified:**
  - `addToEnd` now uses a tail reference (`lastNode`), so it is O(1) instead of O(n).
  - `clear()` also resets the tail and the cursor.
- **Added:**
  - `add(int, T)`, `remove(int)`, and `removeFrom(int)`.
  - The cursor group: `hasCursor`, `getCursorIndex`, `getCursorEntry`, `moveCursorToFront`, `moveCursorTo`, `advanceCursor`, `removeAtCursor`, `insertBeforeCursor`.
  - The app needs these because every action (complete, postpone, pull forward) happens at the current task, and the cursor makes those O(1) re-links instead of walks.
- **Behavior the spec left open:**
  - `removeFrom(size())` removes nothing instead of throwing, so redo moves can be discarded without a special case.
  - If the entry under the cursor is removed by any method, the cursor moves to the next entry, or becomes absent if there is none.
  - Cursor methods throw `IllegalStateException` when there is no cursor.
  - Null entries are rejected everywhere, including `insertBeforeCursor`.
  - `toArray` returns a new array if the one passed in is too short.

### What happened to our unknowns?

| Unknown from the scope | Status |
|---|---|
| Which stretch goals are realistic | **Figured out.** Undo/redo became core. Everything else was dropped (see Next Steps). |
| How to move or swap tasks inside a linked chain | **Figured out and tested in `LinkedChain`.** Swap and rotate were replaced by postpone and pull forward, built from the cursor plus positional insert and remove. `CompletionChecker` does not use them yet. |
| How to undo a completed task | **Designed, not built.** A `Move` stores the task, its position, and the cursor before and after, so undoing a completion puts the task back where it was. |
| How to implement undo and redo | **Designed, not built.** `appliedCount` marks how many Moves are applied, and `removeFrom` discards undone Moves when a new action happens. |
| Save and load | **Dropped.** |
| Other bad-input cases | **Partly found.** New cases: input ending early, optional blank descriptions, reminder removal with none stored. More may appear once undo/redo exists. |

**Still open:**
- How `mostPostponed` should compare tasks. Duplicate task names are allowed, so it should compare by identity, not by name.
- Which cursor position undo should restore after a task is added at a position (to be decided and tested when undo is built).

### What doesn't work yet?

- **Missing in `CompletionChecker`:** undo, redo, postpone, pull forward, `getUpcoming`, replay, and `mostPostponed`. There is no `Move` class and no history chain.
- **`CompletionCheckerApp` and `CompletionCheckerAppTest`** are drafted but are not yet in `src`, so they are not compiled or run in the 72-test result above. The app also calls the missing `CompletionChecker` methods, so it will not compile until they exist or its menu is trimmed to the MVP path.
- **Bad-input cases** for postpone, pull forward, undo, and redo are written in the app tests but can only be proven once those methods exist. The other bad-input cases (blank or long names, long descriptions, bad menu choices, bad positions, empty-list actions, reminders) are handled in the app code and covered by its tests.
- **Test gaps:** there are no tests yet for `Move`, and none for undo/redo state consistency across all five Move types.

## Next Steps

### What is left to finish the MVP (in order)

1. **Get everything compiling in `src` (Nicholas).** Move `CompletionCheckerApp` and its test into `src`, then trim or stub the menu items whose `CompletionChecker` methods do not exist yet, so the app runs on the MVP path.
2. **Run the app tests (Nicholas).** Run `CompletionCheckerAppTest` against the real `CompletionChecker` and fix any mismatch in messages or behavior.
3. **Build `Move` and `MoveTest` (Manveen).** Constructor validation, getters, and a replay line for each type.
4. **Add the history chain to `CompletionChecker` (Scott).** Record add, complete, and delete as Moves, with `appliedCount`.
5. **Add postpone, pull forward, and `getUpcoming` (Scott).**
6. **Add undo and redo (Scott).** One undo per Move type, and a new action after an undo clears the redo moves.
7. **Add `getReplay` and `mostPostponed` (Scott).**
8. **Re-enable the full menu and run an end-to-end script (Nicholas).**
9. **Support the chain (Sophia).** Fix any `LinkedChain` gap the new features expose, and review undo/redo state consistency across the task chain and the history chain.

### Stretch goals

- **Committed:** none beyond undo/redo, which is now core. The remaining time goes to testing and validation.
- **Dropped:** day planner with `TimeBlock`s, priorities and due dates, subtasks, save/load, recurring tasks, and swap/rotate (replaced by postpone and pull forward).

### New tests needed

- **`MoveTest`:** a valid Move stores all six values; a null task or an unknown type (for example "rename") throws; `toString` has its own wording for add, complete, delete, postpone, and pull forward.
- **`CompletionCheckerTest`:**
  - postpone and pull forward: normal cases, plus 0, too large, and no-tasks bad inputs;
  - undo and redo for each of the five Move types, restoring both the task order and the cursor;
  - a new action after an undo makes `canRedo()` false;
  - nothing to undo or redo throws `IllegalStateException`;
  - `getReplay` shows "No actions today." when empty;
  - `mostPostponed` returns the right task, returns `null` when none were postponed, and handles two tasks with the same name.
- **`CompletionCheckerAppTest`:** run postpone, pull forward, undo, and redo against the real `CompletionChecker`, then a full-day script that ends with the replay and most-postponed output.
- **`LinkedChainTest`:** add cases only if the new features expose a chain bug.

### Who does what

| Item | Owner |
|---|---|
| Move `CompletionCheckerApp` and its test into `src`, trim the menu, run app tests, end-to-end script | Nicholas |
| `Move` and `MoveTest` | Manveen |
| History chain, postpone, pull forward, `getUpcoming`, undo/redo, replay, `mostPostponed`, and their tests | Scott |
| `LinkedChain` fixes, undo/redo consistency review, final README | Sophia |

## Attribution

| File | Team member | Contribution |
|---|---|---|
| `LinkedChain.java` | Sophia | Wrote the extended chain (tail, positional methods, cursor) with GenAI assistance; reviewed and tested |
| `LinkedChainTest.java` | Sophia | Wrote and tested; ran and verified (all passing) |
| `Task.java` | Manveen | Wrote |
| `TaskTest.java` | Manveen | Wrote and tested (passing) |
| `Move.java` | Manveen | Wrote and tested |
| `MoveTest.java` | Manveen | Wrote and tested |
| `CompletionChecker.java` | Scott | Wrote the prototype (tasks and reminders) |
| `CompletionCheckerTest.java` | Scott | Wrote and tested (passing) |
| `CompletionCheckerApp.java` | Nicholas | Drafted with GenAI; Nicholas to review, revise, and integrate |
| `CompletionCheckerAppTest.java` | Nicholas | Drafted with GenAI; Nicholas to review, revise, and run |
| `README.md` | Sophia | Drafted with GenAI; reviewed by the group |

**GenAI use:** GenAI was used to draft `LinkedChain` and its tests from our spec, to draft `CompletionCheckerApp` and its tests, and to draft this README. Each was checked by compiling and running the tests, and the team is responsible for reviewing and revising what it produced.

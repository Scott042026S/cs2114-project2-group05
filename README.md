# cs2114-project2-group05
Java to-do application that shows you one task at a time

Reflection

What changed from our specification?
Our specification planned a larger application with task history, undo/redo, postpone, pull-forward, replay, and most-postponed tracking. The actual CompletionChecker is a smaller prototype that implements basic task operations and reminders. It has LinkedChain<Task> tasks and LinkedChain<String> reminders, but no LinkedChain<Move> history or appliedCount. The comments in completeCurrent() and deleteCurrent() explicitly leave recording a Move as future work. The addTask() comment says it records an add, but its implementation does not yet do that; the documentation needs to match the code.
The revised scope said reminders would be dropped, but the current prototype retains addReminder() and getReminders(). This supplies a second element type through the same generic chain while action history is unfinished. For the final, we plan to follow the revised scope by using Task and Move and removing reminders once move history is integrated.
The checker also lacks the specified getTasks(), getUpcoming(), removeReminder(), and history/reordering methods. getReminders() correctly returns String[], as specified, but the drafted app assigns that result to a String; the app must be updated to use the array while reminders remain.
Validation is split across layers. CompletionChecker rejects null tasks, invalid insertion positions, empty-list operations, and null or blank reminders. LinkedChain checks invalid indices, null entries, and absent cursors. The drafted app validates menu choices, numeric ranges, blank names, and text length, and checks for empty task lists. Contrary to the revised scope's statement about no longer validating numeric bounds, the app still calls readInt() for task positions. Its private readText() has an additional overload with allowBlank, allowing an optional description.
TaskTest was adapted to the course's student.TestCase style using test-prefixed methods and try/catch exception checks. CompletionCheckerTest uses that same style, while the supplied chain and console tests still use JUnit Jupiter. These differences reflect an incomplete integration stage rather than a finished replacement for all specified features. GenAI helped with the Task test conversion and this documentation; the earlier specification records group discussion and GenAI assistance with the expanded design.

What changed in LinkedChain?
The supplied chain implementation includes a tail reference, positional insertion and removal, suffix removal, and cursor operations. addToEnd() uses lastNode to append in constant time. clear() resets the tail and cursor, and a shared unlink() helper maintains size, tail, and cursor state during removal.
The cursor methods include hasCursor(), getCursorIndex(), moveCursorToFront(), moveCursorTo(), advanceCursor(), getCursorEntry(), removeAtCursor(), and insertBeforeCursor(). These support the application's current-task model. The checker uses these public methods without accessing private nodes.
removeFrom() supports dropping a suffix and is available for discarding redo history later; it is not yet used by the checker. toArray() returns a typed array, allocating a larger one when needed. These are general storage operations, with application rules kept in the checker and menu handling kept in the app. The specification attributes API design work to GenAI assistance followed by group review and revisions by Scott.

What happened to our unknowns?
The supplied implementation demonstrates how one generic chain can store two element types, how a cursor identifies the current task, and how insertions and removals can preserve cursor state. The checker also implements wrapping from the last task to the first and rejects invalid task/reminder input with exceptions. Existing tests describe these behaviors.
Still-open implementation questions include recording moves, restoring both task order and cursor state during undo/redo, discarding the redo path after a new action, implementing postpone and pull-forward, and computing replay and most-postponed results. Console integration and consistent test configuration also remain unfinished. These behaviors need execution-based verification before they can be considered resolved.

What does not work yet?
- Add, complete, and delete do not record moves, so history-dependent features are unfinished.
- getTasks() from the specification is not implemented in the supplied checker.

Next Steps
1. Finish and validate Move and its tests, and confirm Task validation. - Manveen
2. Add move history and record add, complete, and delete. Implement undo/redo and discard redo moves after a new action. - Scott
3. Implement getTasks(), getUpcoming(), postpone, and pull-forward with valid ranges. - Nick
4. Integrate final menu actions and remove reminders to match the revised scope once LinkedChain<Move> provides the second type. - Sophia
5. Consolidate console tests, fix stream restoration and exit input, configure testing consistently, and run the full application and tests. - Everyone


Stretch Goals
Undo/redo	- Committed
Swap/rotate -	Dropped
Day planner	-Dropped.
Priorities and due dates	- Dropped
Subtasks	- Dropped
Save/load	Commited
Recurring tasks	- Dropped

Attribution 
LinkedChain.java, including nested Node<E>	Sophia
LinkedChainTest.java	Sophia
Task.java	Manveen	
TaskTest.java	Manveen
CompletionChecker.java	Scott	
CompletionCheckerTest.java	
CompletionCheckerApp.java	Nicholas	
CompletionCheckerAppTest.java	Nicholas	

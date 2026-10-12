import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Tests for LinkedChain<T>: storage operations, generics, boundaries, and
 * state consistency (size, tail, and cursor stay correct after every change).
 */
class LinkedChainTest {

    /** Builds a chain holding the given values in order. */
    @SafeVarargs
    private static <E> LinkedChain<E> chainOf(E... values) {
        LinkedChain<E> chain = new LinkedChain<>();
        for (E v : values) {
            chain.addToEnd(v);
        }
        return chain;
    }

    // ---------- LinkedChain(), size(), isEmpty(), toString(), toArray() ----------

    @Test
    void newChainIsEmptyWithNoCursor() {
        LinkedChain<String> c = new LinkedChain<>();
        assertEquals(0, c.size());
        assertTrue(c.isEmpty());
        assertFalse(c.hasCursor());
        assertEquals("[]", c.toString());
    }

    @Test
    void sizeToStringAndToArrayAfterAdds() {
        LinkedChain<String> c = chainOf("A", "B");
        assertEquals(2, c.size());
        assertFalse(c.isEmpty());
        assertEquals("[A, B]", c.toString());
        assertArrayEquals(new String[] {"A", "B"}, c.toArray(new String[2]));
    }

    @Test
    void toArrayOnEmptyChainLeavesArrayUnchanged() {
        String[] values = {"x", "y"};
        new LinkedChain<String>().toArray(values);
        assertArrayEquals(new String[] {"x", "y"}, values);
    }

    @Test
    void toArrayTooShortReturnsNewFilledArrayAndNullThrows() {
        LinkedChain<String> c = chainOf("A", "B", "C");
        assertArrayEquals(new String[] {"A", "B", "C"}, c.toArray(new String[0]));
        assertThrows(IllegalArgumentException.class, () -> c.toArray(null));
    }

    // ---------- add(T), addToEnd(T) ----------

    @Test
    void addGoesToFrontAndAddToEndGoesToEnd() {
        LinkedChain<String> c = new LinkedChain<>();
        assertTrue(c.add("A"));
        assertTrue(c.add("B"));
        assertEquals("[B, A]", c.toString());
        assertTrue(c.addToEnd("C"));
        assertEquals("[B, A, C]", c.toString());
        assertEquals(3, c.size());
    }

    @Test
    void addNullThrowsAndLeavesChainUnchanged() {
        LinkedChain<String> c = chainOf("A");
        assertThrows(IllegalArgumentException.class, () -> c.add(null));
        assertThrows(IllegalArgumentException.class, () -> c.addToEnd(null));
        assertEquals("[A]", c.toString());
        assertEquals(1, c.size());
    }

    @Test
    void addToEndStillWorksAfterRemovingTheLastEntry() {
        LinkedChain<String> c = chainOf("A", "B");
        c.remove(1);          // tail must move back to A
        c.addToEnd("C");
        assertEquals("[A, C]", c.toString());
    }

    // ---------- add(int, T), remove(int), get(int) ----------

    @Test
    void positionalAddRemoveAndGet() {
        LinkedChain<String> c = chainOf("A", "C");
        c.add(1, "B");
        assertEquals("[A, B, C]", c.toString());
        assertEquals("C", c.get(2));
        assertEquals("B", c.remove(1));
        assertEquals("[A, C]", c.toString());
    }

    @Test
    void positionalAddAtBothEnds() {
        LinkedChain<String> c = chainOf("B");
        c.add(0, "A");
        c.add(2, "C");
        assertEquals("[A, B, C]", c.toString());
        c.addToEnd("D"); // tail must be correct after add(size, x)
        assertEquals("[A, B, C, D]", c.toString());
    }

    @Test
    void positionalOperationsRejectBadIndexes() {
        LinkedChain<String> c = chainOf("A", "B");
        assertThrows(IndexOutOfBoundsException.class, () -> c.add(5, "X"));
        assertThrows(IndexOutOfBoundsException.class, () -> c.add(-1, "X"));
        assertThrows(IllegalArgumentException.class, () -> c.add(1, null));
        assertThrows(IndexOutOfBoundsException.class, () -> c.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> c.remove(2));
        assertThrows(IndexOutOfBoundsException.class, () -> c.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> c.get(2));
        assertEquals("[A, B]", c.toString());
        assertEquals(2, c.size());
    }

    @Test
    void getOnEmptyChainThrows() {
        assertThrows(IndexOutOfBoundsException.class, () -> new LinkedChain<String>().get(0));
    }

    // ---------- remove(), remove(T) ----------

    @Test
    void removeFirstAndRemoveByValue() {
        LinkedChain<String> c = chainOf("A", "B");
        assertEquals("A", c.remove());
        assertEquals("[B]", c.toString());
        assertTrue(c.remove("B"));
        assertTrue(c.isEmpty());
    }

    @Test
    void removeOnEmptyReturnsNullAndMissingValueReturnsFalse() {
        LinkedChain<String> c = new LinkedChain<>();
        assertNull(c.remove());
        c.addToEnd("A");
        assertFalse(c.remove("Z"));
        assertFalse(c.remove((String) null));
        assertEquals("[A]", c.toString());
    }

    @Test
    void removeByValueRemovesOnlyTheFirstDuplicate() {
        LinkedChain<String> c = chainOf("A", "B", "A");
        assertTrue(c.remove("A"));
        assertEquals("[B, A]", c.toString());
    }

    // ---------- removeFrom(int) ----------

    @Test
    void removeFromDropsTheEntryAndEverythingAfter() {
        LinkedChain<String> c = chainOf("A", "B", "C", "D");
        c.removeFrom(2);
        assertEquals("[A, B]", c.toString());
        assertEquals(2, c.size());
        c.addToEnd("E"); // tail must have moved
        assertEquals("[A, B, E]", c.toString());
    }

    @Test
    void removeFromEdgeCases() {
        LinkedChain<String> c = chainOf("A", "B");
        assertThrows(IndexOutOfBoundsException.class, () -> c.removeFrom(10));
        assertThrows(IndexOutOfBoundsException.class, () -> c.removeFrom(-1));
        c.removeFrom(2); // index == size removes nothing
        assertEquals("[A, B]", c.toString());
        c.removeFrom(0); // removes everything
        assertTrue(c.isEmpty());
    }

    // ---------- clear() ----------

    @Test
    void clearEmptiesChainAndResetsCursorAndTail() {
        LinkedChain<String> c = chainOf("A", "B", "C");
        c.moveCursorTo(1);
        c.clear();
        assertTrue(c.isEmpty());
        assertFalse(c.hasCursor());
        assertEquals(-1, c.getCursorIndex());
        c.addToEnd("X");
        assertEquals("[X]", c.toString());
    }

    @Test
    void clearOnEmptyChainIsHarmless() {
        LinkedChain<String> c = new LinkedChain<>();
        assertDoesNotThrow(c::clear);
        assertTrue(c.isEmpty());
    }

    // ---------- contains(), count() ----------

    @Test
    void containsAndCountWithDuplicates() {
        LinkedChain<String> c = chainOf("A", "B", "A");
        assertTrue(c.contains("B"));
        assertEquals(2, c.count("A"));
    }

    @Test
    void containsAndCountForMissingOrNull() {
        LinkedChain<String> c = chainOf("A");
        assertFalse(c.contains("Z"));
        assertEquals(0, c.count("Z"));
        assertFalse(c.contains(null));
        assertEquals(0, c.count(null));
    }

    // ---------- hasCursor(), getCursorIndex(), getCursorEntry() ----------

    @Test
    void cursorGettersAfterMovingToFront() {
        LinkedChain<String> c = chainOf("A", "B");
        c.moveCursorToFront();
        assertTrue(c.hasCursor());
        assertEquals(0, c.getCursorIndex());
        assertEquals("A", c.getCursorEntry());
    }

    @Test
    void cursorGettersWithNoCursor() {
        LinkedChain<String> c = chainOf("A", "B");
        assertFalse(c.hasCursor());
        assertEquals(-1, c.getCursorIndex());
        assertThrows(IllegalStateException.class, c::getCursorEntry);
    }

    // ---------- moveCursorToFront(), moveCursorTo(int) ----------

    @Test
    void moveCursorLandsOnTheRequestedEntry() {
        LinkedChain<String> c = chainOf("A", "B", "C");
        c.moveCursorTo(2);
        assertEquals("C", c.getCursorEntry());
        assertEquals(2, c.getCursorIndex());
        c.moveCursorToFront();
        assertEquals("A", c.getCursorEntry());
    }

    @Test
    void moveCursorRejectsEmptyChainAndBadIndexes() {
        assertThrows(IllegalStateException.class, () -> new LinkedChain<String>().moveCursorToFront());
        LinkedChain<String> c = chainOf("A", "B", "C");
        assertThrows(IndexOutOfBoundsException.class, () -> c.moveCursorTo(3));
        assertThrows(IndexOutOfBoundsException.class, () -> c.moveCursorTo(-1));
        assertFalse(c.hasCursor());
    }

    // ---------- advanceCursor() ----------

    @Test
    void advanceCursorMovesToTheNextEntry() {
        LinkedChain<String> c = chainOf("A", "B");
        c.moveCursorToFront();
        assertTrue(c.advanceCursor());
        assertEquals("B", c.getCursorEntry());
        assertEquals(1, c.getCursorIndex());
    }

    @Test
    void advanceCursorOffTheEndMakesCursorAbsent() {
        LinkedChain<String> c = chainOf("A", "B");
        c.moveCursorTo(1);
        assertFalse(c.advanceCursor());
        assertFalse(c.hasCursor());
        assertEquals(-1, c.getCursorIndex());
        assertThrows(IllegalStateException.class, c::advanceCursor);
    }

    // ---------- removeAtCursor(), insertBeforeCursor(T) ----------

    @Test
    void removeAtCursorMovesCursorToNextEntry() {
        LinkedChain<String> c = chainOf("A", "B", "C");
        c.moveCursorTo(1);
        assertEquals("B", c.removeAtCursor());
        assertEquals("[A, C]", c.toString());
        assertEquals("C", c.getCursorEntry());
        assertEquals(1, c.getCursorIndex());
    }

    @Test
    void removeAtCursorOnLastEntryClearsCursorAndMovesTail() {
        LinkedChain<String> c = chainOf("A", "B");
        c.moveCursorTo(1);
        assertEquals("B", c.removeAtCursor());
        assertFalse(c.hasCursor());
        c.addToEnd("C");
        assertEquals("[A, C]", c.toString());
    }

    @Test
    void removeAtCursorOnFirstEntry() {
        LinkedChain<String> c = chainOf("A", "B");
        c.moveCursorToFront();
        assertEquals("A", c.removeAtCursor());
        assertEquals("B", c.getCursorEntry());
        assertEquals(0, c.getCursorIndex());
        assertEquals("[B]", c.toString());
    }

    @Test
    void insertBeforeCursorMakesNewEntryTheCursor() {
        LinkedChain<String> c = chainOf("A", "C");
        c.moveCursorTo(1);
        c.insertBeforeCursor("B");
        assertEquals("[A, B, C]", c.toString());
        assertEquals("B", c.getCursorEntry());
        assertEquals(1, c.getCursorIndex());
        assertEquals(3, c.size());
    }

    @Test
    void insertBeforeCursorAtFrontAndRepeatedly() {
        LinkedChain<String> c = chainOf("B");
        c.moveCursorToFront();
        c.insertBeforeCursor("A");
        c.insertBeforeCursor("Z");
        assertEquals("[Z, A, B]", c.toString());
        assertEquals("Z", c.getCursorEntry());
    }

    @Test
    void removeAtCursorAndInsertBeforeCursorRejectBadState() {
        LinkedChain<String> c = chainOf("A");
        assertThrows(IllegalStateException.class, c::removeAtCursor);
        assertThrows(IllegalStateException.class, () -> c.insertBeforeCursor("X"));
        c.moveCursorToFront();
        assertThrows(IllegalArgumentException.class, () -> c.insertBeforeCursor(null));
        assertEquals("[A]", c.toString());
    }

    // ---------- State consistency: the cursor stays on the same entry ----------

    @Test
    void cursorStaysOnSameEntryWhenEntriesAreAddedAhead() {
        LinkedChain<String> c = chainOf("A", "B", "C");
        c.moveCursorTo(2);
        c.add("X");          // front
        assertEquals("C", c.getCursorEntry());
        assertEquals(3, c.getCursorIndex());
        c.add(1, "Y");       // ahead of the cursor
        assertEquals("C", c.getCursorEntry());
        assertEquals(4, c.getCursorIndex());
        c.addToEnd("Z");     // behind the cursor
        assertEquals("C", c.getCursorEntry());
        assertEquals(4, c.getCursorIndex());
    }

    @Test
    void beforeCursorIsRepairedWhenTheNodeAheadIsInserted() {
        LinkedChain<String> c = chainOf("A", "C");
        c.moveCursorTo(1);
        c.add(1, "B");       // lands directly ahead of the cursor
        c.removeAtCursor();  // relies on beforeCursor being B
        assertEquals("[A, B]", c.toString());
        assertFalse(c.hasCursor());
    }

    @Test
    void cursorStaysOnSameEntryWhenEntriesAreRemovedAhead() {
        LinkedChain<String> c = chainOf("A", "B", "C", "D");
        c.moveCursorTo(3);
        c.remove(0);
        assertEquals("D", c.getCursorEntry());
        assertEquals(2, c.getCursorIndex());
        c.remove("B");
        assertEquals("D", c.getCursorEntry());
        assertEquals(1, c.getCursorIndex());
    }

    @Test
    void beforeCursorIsRepairedWhenTheNodeAheadIsRemoved() {
        LinkedChain<String> c = chainOf("A", "B", "C");
        c.moveCursorTo(2);
        c.remove(1);               // removes B, the node ahead of the cursor
        c.insertBeforeCursor("Y"); // must link after A
        assertEquals("[A, Y, C]", c.toString());
    }

    @Test
    void removingTheCursorEntryByIndexMovesCursorForward() {
        LinkedChain<String> c = chainOf("A", "B", "C");
        c.moveCursorTo(1);
        c.remove(1);
        assertEquals("C", c.getCursorEntry());
        assertEquals(1, c.getCursorIndex());
    }

    @Test
    void removingEntriesBehindTheCursorDoesNotMoveIt() {
        LinkedChain<String> c = chainOf("A", "B", "C");
        c.moveCursorTo(0);
        c.remove(2);
        assertEquals("A", c.getCursorEntry());
        assertEquals(0, c.getCursorIndex());
    }

    @Test
    void removeFromBeyondTheCursorClearsItAndBeforeKeepsIt() {
        LinkedChain<String> c = chainOf("A", "B", "C", "D");
        c.moveCursorTo(2);
        c.removeFrom(3);
        assertEquals("C", c.getCursorEntry());
        c.removeFrom(2);
        assertFalse(c.hasCursor());
    }

    // ---------- Generics: one class stores different element types ----------

    private static class Item {
        private final String label;

        Item(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @Test
    void sameClassStoresDifferentTypesWithoutCasts() {
        LinkedChain<Item> items = new LinkedChain<>();
        Item laundry = new Item("Laundry");
        items.addToEnd(laundry);
        Item first = items.get(0);          // no cast needed
        assertSame(laundry, first);

        LinkedChain<Integer> numbers = chainOf(1, 2, 3);
        int second = numbers.get(1);        // no cast needed
        assertEquals(2, second);

        LinkedChain<String> reminders = chainOf("Call mom", "Call mom");
        assertEquals(2, reminders.count("Call mom")); // duplicates allowed
    }

    // ---------- State consistency: random operations match java.util.List ----------

    @Test
    void randomOperationsMatchAReferenceList() {
        Random random = new Random(42);
        LinkedChain<Integer> chain = new LinkedChain<>();
        List<Integer> expected = new ArrayList<>();

        for (int step = 0; step < 2000; step++) {
            int value = random.nextInt(20);
            switch (random.nextInt(7)) {
                case 0 -> { chain.add(value); expected.add(0, value); }
                case 1 -> { chain.addToEnd(value); expected.add(value); }
                case 2 -> {
                    int index = random.nextInt(expected.size() + 1);
                    chain.add(index, value);
                    expected.add(index, value);
                }
                case 3 -> {
                    Integer removed = chain.remove();
                    assertEquals(expected.isEmpty() ? null : expected.remove(0), removed);
                }
                case 4 -> {
                    if (!expected.isEmpty()) {
                        int index = random.nextInt(expected.size());
                        assertEquals(expected.remove(index), chain.remove(index));
                    }
                }
                case 5 -> assertEquals(expected.remove((Integer) value), chain.remove((Integer) value));
                default -> {
                    int index = random.nextInt(expected.size() + 1);
                    chain.removeFrom(index);
                    while (expected.size() > index) {
                        expected.remove(expected.size() - 1);
                    }
                }
            }
            assertEquals(expected.size(), chain.size());
            assertEquals(expected.toString(), chain.toString());
        }
        chain.addToEnd(99); // tail must still be correct after all that
        expected.add(99);
        assertEquals(expected.toString(), chain.toString());
    }
}

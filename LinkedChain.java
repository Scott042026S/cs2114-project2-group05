import java.util.Arrays;

/**
 * A generic singly linked chain with a tail reference and an optional cursor.
 *
 * The chain stores any element type T in the order the caller placed it. It
 * contains no task, menu, or undo rules; those live in the application classes.
 * Duplicates are allowed, null entries are not.
 *
 * Positions are 0-based. Fields:
 *   firstNode       head of the chain (null when empty)
 *   lastNode        tail of the chain (null when empty)
 *   numberOfEntries current size, updated by every add and remove
 *   cursorNode      the entry the cursor is on (null when there is no cursor)
 *   beforeCursor    the node just ahead of cursorNode (null if the cursor is on
 *                   the first entry or there is no cursor); makes the cursor
 *                   operations O(1)
 *   cursorIndex     position of the cursor, or -1 when there is no cursor
 *
 * Cursor rule: if the entry under the cursor is removed (by any method), the
 * cursor moves to the next entry, or becomes absent if there is none. Entries
 * added or removed elsewhere keep the cursor on the same entry.
 */
public class LinkedChain<T> {

    /** One element plus a reference to the next node. Private to LinkedChain. */
    private static class Node<E> {
        private E data;
        private Node<E> next;

        private Node(E data, Node<E> next) {
            this.data = data;
            this.next = next;
        }
    }

    private Node<T> firstNode;
    private Node<T> lastNode;
    private int numberOfEntries;

    private Node<T> cursorNode;
    private Node<T> beforeCursor;
    private int cursorIndex;

    /** Creates an empty chain with no cursor. O(1) */
    public LinkedChain() {
        cursorIndex = -1;
    }

    // ------------------------------------------------------------------
    // Adding
    // ------------------------------------------------------------------

    /** Adds to the front. Throws IllegalArgumentException if null. O(1) */
    public boolean add(T newEntry) {
        requireNonNull(newEntry);
        insertAt(0, newEntry);
        return true;
    }

    /** Adds to the end using lastNode. Throws IllegalArgumentException if null. O(1) */
    public boolean addToEnd(T newEntry) {
        requireNonNull(newEntry);
        Node<T> node = new Node<>(newEntry, null);
        if (lastNode == null) {
            firstNode = node;
        } else {
            lastNode.next = node;
        }
        lastNode = node;
        numberOfEntries++;
        return true;
    }

    /**
     * Inserts so the entry ends up at index (0 to size inclusive).
     * Throws IndexOutOfBoundsException if out of range, IllegalArgumentException if null.
     * O(n)
     */
    public void add(int index, T newEntry) {
        requireNonNull(newEntry);
        if (index < 0 || index > numberOfEntries) {
            throw new IndexOutOfBoundsException("Index " + index + ", size " + numberOfEntries);
        }
        insertAt(index, newEntry);
    }

    // ------------------------------------------------------------------
    // Removing
    // ------------------------------------------------------------------

    /** Removes and returns the first entry, or null if empty. O(1) */
    public T remove() {
        if (firstNode == null) {
            return null;
        }
        return unlink(null, firstNode, 0);
    }

    /**
     * Removes and returns the entry at index.
     * Throws IndexOutOfBoundsException if out of range. O(n)
     */
    public T remove(int index) {
        checkIndex(index);
        Node<T> prev = (index == 0) ? null : nodeAt(index - 1);
        Node<T> node = (prev == null) ? firstNode : prev.next;
        return unlink(prev, node, index);
    }

    /** Removes the first equal entry. Returns whether one was found. O(n) */
    public boolean remove(T anEntry) {
        if (anEntry == null) {
            return false;
        }
        Node<T> prev = null;
        Node<T> current = firstNode;
        int index = 0;
        while (current != null) {
            if (current.data.equals(anEntry)) {
                unlink(prev, current, index);
                return true;
            }
            prev = current;
            current = current.next;
            index++;
        }
        return false;
    }

    /**
     * Removes the entry at index and everything after it. An index equal to
     * size() removes nothing (so callers can discard redo moves without a
     * special case). Throws IndexOutOfBoundsException if index is negative or
     * greater than size(). O(n)
     */
    public void removeFrom(int index) {
        if (index < 0 || index > numberOfEntries) {
            throw new IndexOutOfBoundsException("Index " + index + ", size " + numberOfEntries);
        }
        if (index == numberOfEntries) {
            return;
        }
        if (index == 0) {
            clear();
            return;
        }
        Node<T> prev = nodeAt(index - 1);
        prev.next = null;
        lastNode = prev;
        numberOfEntries = index;
        if (cursorNode != null && cursorIndex >= index) {
            clearCursor();
        }
    }

    /** Removes everything and resets the tail and the cursor. O(1) */
    public void clear() {
        firstNode = null;
        lastNode = null;
        numberOfEntries = 0;
        clearCursor();
    }

    // ------------------------------------------------------------------
    // Reading
    // ------------------------------------------------------------------

    /** Returns the entry at index. Throws IndexOutOfBoundsException if out of range. O(n) */
    public T get(int index) {
        checkIndex(index);
        return nodeAt(index).data;
    }

    /** True if an equal entry is present. O(n) */
    public boolean contains(T anEntry) {
        return count(anEntry) > 0;
    }

    /** Number of equal entries. O(n) */
    public int count(T anEntry) {
        if (anEntry == null) {
            return 0;
        }
        int total = 0;
        for (Node<T> current = firstNode; current != null; current = current.next) {
            if (current.data.equals(anEntry)) {
                total++;
            }
        }
        return total;
    }

    /** Number of entries. O(1) */
    public int size() {
        return numberOfEntries;
    }

    /** True if size is 0. O(1) */
    public boolean isEmpty() {
        return numberOfEntries == 0;
    }

    /**
     * Fills values first to last and returns it. If values is too short, a new
     * array of the same type is returned instead. Slots past size() are left
     * unchanged. Throws IllegalArgumentException if values is null. O(n)
     */
    public T[] toArray(T[] values) {
        if (values == null) {
            throw new IllegalArgumentException("values cannot be null");
        }
        T[] result = (values.length < numberOfEntries)
                ? Arrays.copyOf(values, numberOfEntries)
                : values;
        int i = 0;
        for (Node<T> current = firstNode; current != null; current = current.next) {
            result[i++] = current.data;
        }
        return result;
    }

    /** Returns [a, b, c], first to last. O(n) */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (Node<T> current = firstNode; current != null; current = current.next) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append(", ");
            }
        }
        return sb.append("]").toString();
    }

    // ------------------------------------------------------------------
    // Cursor
    // ------------------------------------------------------------------

    /** True if a cursor is set. O(1) */
    public boolean hasCursor() {
        return cursorNode != null;
    }

    /** Cursor position, or -1 if none. O(1) */
    public int getCursorIndex() {
        return cursorIndex;
    }

    /** Puts the cursor on the first entry. Throws IllegalStateException if empty. O(1) */
    public void moveCursorToFront() {
        if (firstNode == null) {
            throw new IllegalStateException("Chain is empty");
        }
        cursorNode = firstNode;
        beforeCursor = null;
        cursorIndex = 0;
    }

    /** Puts the cursor at index. Throws IndexOutOfBoundsException if out of range. O(n) */
    public void moveCursorTo(int index) {
        checkIndex(index);
        if (index == 0) {
            moveCursorToFront();
            return;
        }
        beforeCursor = nodeAt(index - 1);
        cursorNode = beforeCursor.next;
        cursorIndex = index;
    }

    /**
     * Moves the cursor one entry later. Returns false if it was on the last
     * entry (the cursor then becomes absent). Throws IllegalStateException if
     * there is no cursor. O(1)
     */
    public boolean advanceCursor() {
        requireCursor();
        if (cursorNode.next == null) {
            clearCursor();
            return false;
        }
        beforeCursor = cursorNode;
        cursorNode = cursorNode.next;
        cursorIndex++;
        return true;
    }

    /** Returns the entry at the cursor. Throws IllegalStateException if no cursor. O(1) */
    public T getCursorEntry() {
        requireCursor();
        return cursorNode.data;
    }

    /**
     * Removes and returns the cursor entry. The cursor moves to the next entry
     * (or becomes absent if it was the last). Throws IllegalStateException if
     * no cursor. O(1)
     */
    public T removeAtCursor() {
        requireCursor();
        return unlink(beforeCursor, cursorNode, cursorIndex);
    }

    /**
     * Inserts at the cursor's position; the new entry becomes the cursor entry
     * and the old cursor entry moves one place later. Throws
     * IllegalStateException if no cursor, IllegalArgumentException if null. O(1)
     */
    public void insertBeforeCursor(T newEntry) {
        requireNonNull(newEntry);
        requireCursor();
        Node<T> node = new Node<>(newEntry, cursorNode);
        if (beforeCursor == null) {
            firstNode = node;
        } else {
            beforeCursor.next = node;
        }
        numberOfEntries++;
        cursorNode = node;
        // beforeCursor and cursorIndex stay the same: the new node sits where the cursor was.
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private void requireNonNull(T entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Entry cannot be null");
        }
    }

    private void requireCursor() {
        if (cursorNode == null) {
            throw new IllegalStateException("No cursor is set");
        }
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= numberOfEntries) {
            throw new IndexOutOfBoundsException("Index " + index + ", size " + numberOfEntries);
        }
    }

    private void clearCursor() {
        cursorNode = null;
        beforeCursor = null;
        cursorIndex = -1;
    }

    /** Returns the node at index. The caller has already checked the range. */
    private Node<T> nodeAt(int index) {
        Node<T> current = firstNode;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current;
    }

    /** Links a new node in so it ends up at index (0 to size) and keeps the cursor on the same entry. */
    private void insertAt(int index, T newEntry) {
        Node<T> node;
        if (index == 0) {
            node = new Node<>(newEntry, firstNode);
            firstNode = node;
        } else {
            Node<T> prev = nodeAt(index - 1);
            node = new Node<>(newEntry, prev.next);
            prev.next = node;
        }
        if (node.next == null) {
            lastNode = node;
        }
        numberOfEntries++;

        if (cursorNode != null && index <= cursorIndex) {
            if (index == cursorIndex) {
                beforeCursor = node; // new node now sits directly ahead of the cursor
            }
            cursorIndex++;
        }
    }

    /**
     * Unlinks node (which sits at index, with prev ahead of it) and returns its
     * data. Every removal goes through here so the tail, size, and cursor stay
     * consistent.
     */
    private T unlink(Node<T> prev, Node<T> node, int index) {
        if (prev == null) {
            firstNode = node.next;
        } else {
            prev.next = node.next;
        }
        if (node == lastNode) {
            lastNode = prev;
        }
        numberOfEntries--;

        if (cursorNode != null) {
            if (index < cursorIndex) {
                if (index == cursorIndex - 1) {
                    beforeCursor = prev; // the node ahead of the cursor was removed
                }
                cursorIndex--;
            } else if (index == cursorIndex) {
                cursorNode = node.next;
                if (cursorNode == null) {
                    clearCursor();
                } else {
                    beforeCursor = prev;
                }
            }
        }

        T data = node.data;
        node.next = null;
        return data;
    }
}

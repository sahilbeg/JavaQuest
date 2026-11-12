package jq.m04.heaps;

/**
 * A min-heap of ints stored in an array.
 * The parent of index i is (i - 1) / 2. Its children are 2 * i + 1 and 2 * i + 2.
 * Build it by hand on the int[] below. Do not use PriorityQueue, any list, or any sort.
 */
public class MinHeap {

    private int[] items = new int[4];
    private int size;

    public void add(int value) {
        // TODO 1: if the array is full, copy it into one twice as big.
        // TODO 2: put value at index size, add 1 to size, then sift it up while it is smaller than its parent.
    }

    public int poll() {
        // TODO 3: throw IllegalStateException when empty.
        // TODO 4: keep items[0], move the last item to index 0, shrink size, sift down, return the kept value.
        return 0;
    }

    public int peek() {
        // TODO 5: throw IllegalStateException when empty; otherwise return items[0].
        return 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}

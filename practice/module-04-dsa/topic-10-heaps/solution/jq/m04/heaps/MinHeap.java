package jq.m04.heaps;

/**
 * A min-heap of ints stored in an array.
 * The parent of index i is (i - 1) / 2. Its children are 2 * i + 1 and 2 * i + 2.
 */
public class MinHeap {

    private int[] items = new int[4];
    private int size;

    public void add(int value) {
        if (size == items.length) {
            int[] bigger = new int[items.length * 2];
            for (int i = 0; i < size; i++) {
                bigger[i] = items[i];
            }
            items = bigger;
        }
        items[size] = value;
        size++;
        siftUp(size - 1);
    }

    public int poll() {
        if (size == 0) {
            throw new IllegalStateException("The heap is empty");
        }
        int smallest = items[0];
        size--;
        items[0] = items[size];
        siftDown(0);
        return smallest;
    }

    public int peek() {
        if (size == 0) {
            throw new IllegalStateException("The heap is empty");
        }
        return items[0];
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (items[index] >= items[parent]) {
                return;
            }
            swap(index, parent);
            index = parent;
        }
    }

    private void siftDown(int index) {
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int smallest = index;
            if (left < size && items[left] < items[smallest]) {
                smallest = left;
            }
            if (right < size && items[right] < items[smallest]) {
                smallest = right;
            }
            if (smallest == index) {
                return;
            }
            swap(index, smallest);
            index = smallest;
        }
    }

    private void swap(int a, int b) {
        int temp = items[a];
        items[a] = items[b];
        items[b] = temp;
    }
}

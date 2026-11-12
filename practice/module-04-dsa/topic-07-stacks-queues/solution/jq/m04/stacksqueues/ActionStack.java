package jq.m04.stacksqueues;

public class ActionStack {
    private String[] items = new String[4];
    private int size;

    public void push(String action) {
        if (size == items.length) {
            String[] bigger = new String[items.length * 2];
            for (int i = 0; i < size; i++) {
                bigger[i] = items[i];
            }
            items = bigger;
        }
        items[size] = action;
        size++;
    }

    public String pop() {
        if (size == 0) {
            throw new IllegalStateException("Nothing to undo");
        }
        size--;
        String top = items[size];
        items[size] = null;
        return top;
    }

    public String peek() {
        if (size == 0) {
            return null;
        }
        return items[size - 1];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
}

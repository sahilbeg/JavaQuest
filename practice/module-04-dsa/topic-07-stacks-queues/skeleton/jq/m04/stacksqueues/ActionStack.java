package jq.m04.stacksqueues;

public class ActionStack {
    private String[] items = new String[4];
    private int size;

    public void push(String action) {
        // TODO 1: if the array is full, copy it into one twice as long; then store action at items[size] and add 1 to size
    }

    public String pop() {
        // TODO 2: throw IllegalStateException when empty; otherwise remove and return the top item
        return "";
    }

    public String peek() {
        // TODO 3: return the top item without removing it, or null when empty
        return "";
    }

    public boolean isEmpty() {
        // TODO 4: return true when no actions are stored
        return false;
    }

    public int size() {
        // TODO 4: return how many actions are stored
        return -1;
    }
}

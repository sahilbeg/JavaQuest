package jq.m05.synchronization;

public class UnlockCounter {
    private int count;

    public void increment() {
        // TODO 1: add 1 to count so no update is lost when many threads call this at once
    }

    public int get() {
        // TODO 2: return count safely
        return 0;
    }
}

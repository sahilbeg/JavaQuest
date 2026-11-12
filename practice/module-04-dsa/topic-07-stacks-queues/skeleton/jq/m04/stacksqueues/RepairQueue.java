package jq.m04.stacksqueues;

import java.util.ArrayDeque;

public class RepairQueue {
    private final ArrayDeque<String> queue = new ArrayDeque<>();

    public void report(String bikeId) {
        // TODO 5: add bikeId at the back of the queue
    }

    public String next() {
        // TODO 6: remove and return the bike at the front, or null when the queue is empty
        return "";
    }

    public int waiting() {
        // TODO 6: return how many bikes are waiting
        return -1;
    }
}

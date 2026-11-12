package jq.m04.stacksqueues;

import java.util.ArrayDeque;

public class RepairQueue {
    private final ArrayDeque<String> queue = new ArrayDeque<>();

    public void report(String bikeId) {
        queue.offer(bikeId);
    }

    public String next() {
        return queue.poll();
    }

    public int waiting() {
        return queue.size();
    }
}

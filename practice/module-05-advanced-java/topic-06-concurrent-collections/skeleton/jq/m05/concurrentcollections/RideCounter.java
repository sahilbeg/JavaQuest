package jq.m05.concurrentcollections;

import java.util.concurrent.atomic.AtomicInteger;

public class RideCounter {

    private final AtomicInteger rides = new AtomicInteger();

    public void record() {
        // TODO 4: add 1 to rides safely with incrementAndGet()
    }

    public int total() {
        // TODO 5: return the current value of rides
        return 0;
    }
}

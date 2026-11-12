package jq.m05.concurrentcollections;

import java.util.concurrent.atomic.AtomicInteger;

public class RideCounter {

    private final AtomicInteger rides = new AtomicInteger();

    public void record() {
        rides.incrementAndGet();
    }

    public int total() {
        return rides.get();
    }
}

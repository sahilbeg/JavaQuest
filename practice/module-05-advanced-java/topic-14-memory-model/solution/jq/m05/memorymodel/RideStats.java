package jq.m05.memorymodel;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class RideStats {

    private final LongAdder rides = new LongAdder();
    private final AtomicInteger peakOnline = new AtomicInteger();

    public void rideStarted(int ridersOnline) {
        rides.increment();
        peakOnline.accumulateAndGet(ridersOnline, Math::max);
    }

    public long rides() {
        return rides.sum();
    }

    public int peakOnline() {
        return peakOnline.get();
    }
}

package jq.m05.memorymodel;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class RideStats {

    // TODO 7: volatile does not make rides++ safe. Replace with: private final LongAdder rides = new LongAdder();
    private volatile long rides;
    // TODO 8: "if (n > peakOnline) peakOnline = n" can lose the bigger number.
    //         Replace with: private final AtomicInteger peakOnline = new AtomicInteger();
    private volatile int peakOnline;

    public void rideStarted(int ridersOnline) {
        // TODO 9: count the ride with rides.increment(), and keep the biggest ridersOnline
        //         with peakOnline.accumulateAndGet(ridersOnline, Math::max)
    }

    public long rides() {
        // TODO 10: return rides.sum()
        return 0;
    }

    public int peakOnline() {
        // TODO 11: return peakOnline.get()
        return 0;
    }
}

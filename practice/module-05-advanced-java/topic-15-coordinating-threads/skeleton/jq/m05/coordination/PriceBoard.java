package jq.m05.coordination;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PriceBoard {

    private final Map<String, Long> perMinuteCents = new HashMap<>();
    // TODO 8: add a field: private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public PriceBoard() {
        perMinuteCents.put("CLASSIC", 15L);
        perMinuteCents.put("EBIKE", 30L);
        perMinuteCents.put("CARGO", 25L);
    }

    public long fareCents(String bikeType, int minutes) {
        // TODO 9: under the READ lock: look up the rate (unknown type: throw IllegalArgumentException)
        //         and return 100 + rate * minutes. Unlock in finally.
        return 0;
    }

    public Map<String, Long> snapshot() {
        // TODO 10: under the READ lock, return new TreeMap<>(perMinuteCents)
        return new TreeMap<>();
    }

    public void setRate(String bikeType, long centsPerMinute) {
        // TODO 11: under the WRITE lock, put the new rate
    }

    public void raiseAll(long extraCents) {
        // TODO 12: under the WRITE lock, add extraCents to every rate:
        //          perMinuteCents.replaceAll((type, rate) -> rate + extraCents)
    }
}

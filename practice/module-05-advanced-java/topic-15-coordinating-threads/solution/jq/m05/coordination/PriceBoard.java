package jq.m05.coordination;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PriceBoard {

    private final Map<String, Long> perMinuteCents = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public PriceBoard() {
        perMinuteCents.put("CLASSIC", 15L);
        perMinuteCents.put("EBIKE", 30L);
        perMinuteCents.put("CARGO", 25L);
    }

    public long fareCents(String bikeType, int minutes) {
        lock.readLock().lock();
        try {
            Long rate = perMinuteCents.get(bikeType);
            if (rate == null) {
                throw new IllegalArgumentException("Unknown bike type: " + bikeType);
            }
            return 100 + rate * minutes;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Map<String, Long> snapshot() {
        lock.readLock().lock();
        try {
            return new TreeMap<>(perMinuteCents);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setRate(String bikeType, long centsPerMinute) {
        lock.writeLock().lock();
        try {
            perMinuteCents.put(bikeType, centsPerMinute);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void raiseAll(long extraCents) {
        lock.writeLock().lock();
        try {
            perMinuteCents.replaceAll((type, rate) -> rate + extraCents);
        } finally {
            lock.writeLock().unlock();
        }
    }
}

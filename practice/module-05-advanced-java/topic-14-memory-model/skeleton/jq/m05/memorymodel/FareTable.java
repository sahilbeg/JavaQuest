package jq.m05.memorymodel;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class FareTable {

    private static final AtomicInteger CREATED = new AtomicInteger();

    private final Map<String, Long> perMinuteCents;

    private FareTable() {
        CREATED.incrementAndGet();
        perMinuteCents = Map.of("CLASSIC", 15L, "EBIKE", 30L, "CARGO", 25L);
    }

    // TODO 12: add a private static final class Holder with: static final FareTable INSTANCE = new FareTable();

    public static FareTable get() {
        // TODO 13: return Holder.INSTANCE, so every thread gets the one table, built once
        return new FareTable();
    }

    public static int timesCreated() {
        return CREATED.get();
    }

    public long fareCents(String bikeType, int minutes) {
        Long rate = perMinuteCents.get(bikeType);
        if (rate == null) {
            throw new IllegalArgumentException("Unknown bike type: " + bikeType);
        }
        return 100 + rate * minutes;
    }
}

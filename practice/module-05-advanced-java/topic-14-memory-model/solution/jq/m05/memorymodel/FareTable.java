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

    private static final class Holder {
        static final FareTable INSTANCE = new FareTable();
    }

    public static FareTable get() {
        return Holder.INSTANCE;
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

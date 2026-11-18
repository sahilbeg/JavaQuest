package jq.m09.resilience;

import java.util.random.RandomGenerator;

/** Exponential backoff with a cap and full jitter: wait a random time from 0 up to min(cap, base * 2^(attempt - 1)). */
public final class Backoff {

    private final long baseMillis;
    private final long capMillis;
    private final RandomGenerator random;

    public Backoff(long baseMillis, long capMillis, RandomGenerator random) {
        if (baseMillis < 1 || capMillis < baseMillis) {
            throw new IllegalArgumentException("need baseMillis >= 1 and capMillis >= baseMillis");
        }
        this.baseMillis = baseMillis;
        this.capMillis = capMillis;
        this.random = random;
    }

    /** The largest wait after failed attempt number {@code attempt} (1 = the first try failed). */
    public long ceilingMillis(int attempt) {
        if (attempt < 1) {
            throw new IllegalArgumentException("attempt starts at 1");
        }
        long ceiling = baseMillis;
        // Double one step at a time and stop at the cap. A shift like base << (attempt - 1) overflows past 63 doublings.
        for (int i = 1; i < attempt && ceiling < capMillis; i++) {
            ceiling = ceiling > capMillis / 2 ? capMillis : ceiling * 2;
        }
        return Math.min(ceiling, capMillis);
    }

    /** Full jitter: a random wait from 0 to the ceiling, both included. */
    public long delayMillis(int attempt) {
        return random.nextLong(ceilingMillis(attempt) + 1);
    }
}

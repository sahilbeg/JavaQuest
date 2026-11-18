package jq.m09.resilience;

import java.util.random.RandomGenerator;

/** Exponential backoff with a cap and full jitter: wait a random time from 0 up to min(cap, base * 2^(attempt - 1)). */
public final class Backoff {

    private final long baseMillis;
    private final long capMillis;
    private final RandomGenerator random;

    public Backoff(long baseMillis, long capMillis, RandomGenerator random) {
        // TODO 15: throw IllegalArgumentException unless baseMillis >= 1 and capMillis >= baseMillis
        this.baseMillis = baseMillis;
        this.capMillis = capMillis;
        this.random = random;
    }

    /** The largest wait after failed attempt number {@code attempt} (1 = the first try failed). */
    public long ceilingMillis(int attempt) {
        // TODO 16: throw IllegalArgumentException when attempt < 1. Return min(cap, base * 2^(attempt - 1)),
        //          without overflow for huge attempts, and quickly even for Integer.MAX_VALUE.
        return 0;
    }

    /** Full jitter: a random wait from 0 to the ceiling, both included. */
    public long delayMillis(int attempt) {
        // TODO 17: return random.nextLong(ceiling + 1)
        return 0;
    }
}

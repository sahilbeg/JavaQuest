package jq.m09.resilience;

import java.util.function.LongSupplier;

/**
 * A token bucket: it holds at most {@code capacity} tokens and gains {@code refillPerSecond} tokens a second.
 * Each request spends one whole token. Time comes from {@code nowMillis}, so tests can move it by hand.
 */
public final class TokenBucket {

    /** Tip: count tokens in thousandths, so time that has not yet made a whole token is never thrown away. */
    private static final long MILLI = 1000;

    private final long capacity;
    private final long refillPerSecond;
    private final LongSupplier nowMillis;
    private long milliTokens;        // thousandths of a token: 1000 = one whole token
    private long lastRefillMillis;   // the time of the last refill

    public TokenBucket(long capacity, long refillPerSecond, LongSupplier nowMillis) {
        // TODO 1: throw IllegalArgumentException when capacity < 1 or refillPerSecond < 1.
        //         Start full (capacity whole tokens), and remember nowMillis.getAsLong() as the last refill time.
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.nowMillis = nowMillis;
    }

    /** Spends one token and returns true, or returns false when no whole token is left. */
    public synchronized boolean tryAcquire() {
        // TODO 3: refill(), then spend one whole token if there is one
        return false;
    }

    /** Whole tokens available right now, rounded down. */
    public synchronized long availableTokens() {
        // TODO 4: refill(), then return the whole tokens
        return 0;
    }

    /** 0 when a whole token is ready; otherwise the milliseconds until one is, rounded up. */
    public synchronized long millisUntilNextToken() {
        // TODO 5: refill(), then work out how long the missing part of one token takes at refillPerSecond. Round up.
        return 0;
    }

    private void refill() {
        // TODO 2: add the tokens earned since the last refill: each elapsed millisecond earns refillPerSecond thousandths.
        //         Never go above capacity, even after a very long gap. Then move the last refill time to now.
    }
}

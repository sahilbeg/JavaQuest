package jq.m09.resilience;

import java.util.function.LongSupplier;

/**
 * A token bucket: it holds at most {@code capacity} tokens and gains {@code refillPerSecond} tokens a second.
 * Each request spends one whole token. Time comes from {@code nowMillis}, so tests can move it by hand.
 */
public final class TokenBucket {

    /** Tokens are counted in thousandths, so time that has not yet made a whole token is never thrown away. */
    private static final long MILLI = 1000;

    private final long capacity;
    private final long refillPerSecond;
    private final LongSupplier nowMillis;
    private long milliTokens;
    private long lastRefillMillis;

    public TokenBucket(long capacity, long refillPerSecond, LongSupplier nowMillis) {
        if (capacity < 1 || refillPerSecond < 1) {
            throw new IllegalArgumentException("capacity and refillPerSecond must be at least 1");
        }
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.nowMillis = nowMillis;
        this.milliTokens = capacity * MILLI;
        this.lastRefillMillis = nowMillis.getAsLong();
    }

    /** Spends one token and returns true, or returns false when no whole token is left. */
    public synchronized boolean tryAcquire() {
        refill();
        if (milliTokens < MILLI) {
            return false;
        }
        milliTokens -= MILLI;
        return true;
    }

    /** Whole tokens available right now, rounded down. */
    public synchronized long availableTokens() {
        refill();
        return milliTokens / MILLI;
    }

    /** 0 when a whole token is ready; otherwise the milliseconds until one is, rounded up. */
    public synchronized long millisUntilNextToken() {
        refill();
        long missing = MILLI - milliTokens;
        if (missing <= 0) {
            return 0;
        }
        return (missing + refillPerSecond - 1) / refillPerSecond;
    }

    private void refill() {
        long now = nowMillis.getAsLong();
        long elapsed = now - lastRefillMillis;
        if (elapsed <= 0) {
            return;
        }
        long full = capacity * MILLI;
        // One elapsed millisecond adds refillPerSecond thousandths of a token. Clamping first avoids overflow on long gaps.
        long added = Math.min(elapsed, full) * refillPerSecond;
        milliTokens = Math.min(full, milliTokens + added);
        lastRefillMillis = now;
    }
}

package jq.m09.resilience;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/** One token bucket per key (a rider id, an API key or an IP address), so one heavy user cannot use up everyone's share. */
public final class RateLimiter {

    private final long capacity;
    private final long refillPerSecond;
    private final LongSupplier nowMillis;
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimiter(long capacity, long refillPerSecond, LongSupplier nowMillis) {
        // TODO 6: throw IllegalArgumentException when capacity < 1 or refillPerSecond < 1
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.nowMillis = nowMillis;
    }

    /** true: let the request in. false: answer 429 Too Many Requests. */
    public boolean tryAcquire(String key) {
        // TODO 7: find this key's bucket (make a new, full one the first time you see the key) and try to take a token
        return false;
    }

    /** The value for a Retry-After header: whole seconds until this key has a token again, rounded up. 0 when it has one now. */
    public long retryAfterSeconds(String key) {
        // TODO 8: turn the bucket's millisUntilNextToken() into whole seconds, rounded up
        return 0;
    }
}

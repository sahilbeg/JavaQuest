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
        if (capacity < 1 || refillPerSecond < 1) {
            throw new IllegalArgumentException("capacity and refillPerSecond must be at least 1");
        }
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.nowMillis = nowMillis;
    }

    /** true: let the request in. false: answer 429 Too Many Requests. */
    public boolean tryAcquire(String key) {
        return bucketFor(key).tryAcquire();
    }

    /** The value for a Retry-After header: whole seconds until this key has a token again, rounded up. 0 when it has one now. */
    public long retryAfterSeconds(String key) {
        long millis = bucketFor(key).millisUntilNextToken();
        return (millis + 999) / 1000;
    }

    private TokenBucket bucketFor(String key) {
        return buckets.computeIfAbsent(key, k -> new TokenBucket(capacity, refillPerSecond, nowMillis));
    }
}

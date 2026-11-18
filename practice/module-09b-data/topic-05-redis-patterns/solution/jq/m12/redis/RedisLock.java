package jq.m12.redis;

import java.util.Optional;
import java.util.function.Supplier;

/** A lock that many servers share through one Redis, with a lease and a fencing token. */
public final class RedisLock {

    private final MiniRedis redis;
    private final Supplier<String> tokens;

    public RedisLock(MiniRedis redis, Supplier<String> tokens) {
        this.redis = redis;
        this.tokens = tokens;
    }

    public Optional<Lease> tryAcquire(String name, long leaseMillis) {
        if (leaseMillis < 1) {
            throw new IllegalArgumentException("a lease must last at least 1 ms, got " + leaseMillis);
        }
        String token = tokens.get();
        // SET and INCR in one atomic step: a holder can never pause between taking the lock and its fencing token
        return redis.atomically(r -> {
            if (r.set("lock:" + name, token, SetOptions.nxPx(leaseMillis)) == null) {
                return Optional.<Lease>empty();
            }
            long fencingToken = r.incr("fence:" + name);
            return Optional.of(new Lease(name, token, fencingToken));
        });
    }

    public boolean release(Lease lease) {
        String key = "lock:" + lease.name();
        return redis.atomically(r -> {
            if (lease.token().equals(r.get(key))) {
                return r.del(key) == 1;
            }
            return false;
        });
    }
}

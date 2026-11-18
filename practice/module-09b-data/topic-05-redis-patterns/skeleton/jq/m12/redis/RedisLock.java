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

    /** SET lock:name token NX PX leaseMillis. On success, a lease with the next fencing token; otherwise empty. */
    public Optional<Lease> tryAcquire(String name, long leaseMillis) {
        // TODO 17: leaseMillis < 1 throws IllegalArgumentException. Take ONE token from tokens.get() on every call.
        //          Then, inside ONE redis.atomically(r -> ...): SET "lock:" + name with SetOptions.nxPx(leaseMillis);
        //          a null reply means someone holds it: empty. Only after a successful SET, INCR "fence:" + name
        //          for the fencing token. Both in one step, so nobody can take the lock between your SET and your INCR.
        return Optional.empty();
    }

    /** Deletes the lock only if it still holds this lease's token, as one atomic step. */
    public boolean release(Lease lease) {
        // TODO 18: inside redis.atomically(r -> ...), GET the lock key; if it equals lease.token(), DEL it and
        //          answer true. Any other value, or no key at all, answers false and deletes nothing.
        return false;
    }
}

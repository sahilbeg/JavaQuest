package jq.m12.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RedisLockTest {

    private static final String JOB = "billing-2026-11-18";

    private final MutableClock clock = new MutableClock();
    private final MiniRedis redis = new MiniRedis(clock);
    private final Supplier<String> tokens = new Supplier<>() {
        private int next = 1;

        @Override
        public String get() {
            return "token-" + next++;
        }
    };
    private final RedisLock locks = new RedisLock(redis, tokens);

    /** Checks that tryAcquire gave a lease, with a readable message if it did not. */
    private static Lease held(Optional<Lease> lease, String message) {
        assertTrue(lease.isPresent(), message);
        return lease.get();
    }

    @Test
    @DisplayName("The first server gets the lock with a token and fencing token 1; a second server is refused")
    void onlyOneHolder() {
        Optional<Lease> first = locks.tryAcquire(JOB, 30_000);
        assertTrue(first.isPresent(), "the lock is free, so tryAcquire should return a lease");
        assertEquals(new Lease(JOB, "token-1", 1), first.get(),
                "the lease should carry the name, the first token from the source, and fencing token 1");
        assertEquals("token-1", redis.get("lock:" + JOB), "the token should be stored in the key lock:" + JOB);
        assertEquals(30, redis.ttl("lock:" + JOB), "the lock key should expire with the 30 s lease");

        assertTrue(locks.tryAcquire(JOB, 30_000).isEmpty(), "while the lock is held, a second tryAcquire should return empty");
        assertEquals("token-1", redis.get("lock:" + JOB), "a refused tryAcquire must not overwrite the holder's token");
        assertTrue(locks.tryAcquire("rebalance-trucks", 30_000).isPresent(), "a lock with another name is a separate lock");
        assertThrows(IllegalArgumentException.class, () -> locks.tryAcquire(JOB, 0), "a lease of 0 ms is not allowed");
    }

    @Test
    @DisplayName("Releasing with the right token frees the lock, and releasing twice answers false")
    void releaseFreesLock() {
        Lease lease = held(locks.tryAcquire(JOB, 30_000), "the lock is free, so tryAcquire should return a lease");
        assertTrue(locks.release(lease), "the holder's release should answer true");
        assertEquals(-2, redis.ttl("lock:" + JOB), "after release, the lock key should be gone");
        assertFalse(locks.release(lease), "a second release has nothing to delete, so it should answer false");
        assertTrue(locks.tryAcquire(JOB, 30_000).isPresent(), "once released, another server may take the lock");
    }

    @Test
    @DisplayName("The lease runs out at exactly its length, and then another server may take the lock")
    void leaseExpires() {
        held(locks.tryAcquire(JOB, 10_000), "the lock is free, so tryAcquire should return a lease");
        clock.advanceMillis(9_999);
        assertTrue(locks.tryAcquire(JOB, 10_000).isEmpty(), "at 9.999 s of a 10 s lease the lock is still held");
        clock.advanceMillis(1);
        Optional<Lease> next = locks.tryAcquire(JOB, 10_000);
        assertTrue(next.isPresent(), "at exactly 10 s the lease has run out, so a new server should get the lock");
        assertEquals(2, next.get().fencingToken(), "the second holder ever should get fencing token 2");
    }

    @Test
    @DisplayName("After a long pause, the old holder's release must not delete the new holder's lock")
    void staleReleaseIsRefused() {
        Lease serverA = held(locks.tryAcquire(JOB, 30_000), "the lock is free, so server A should get a lease");
        clock.advanceMillis(31_000);   // server A freezes in a long garbage-collection pause
        Lease serverB = held(locks.tryAcquire(JOB, 30_000), "A's lease ran out, so server B should get the lock");

        assertFalse(locks.release(serverA), "server A's lease ran out; its release must answer false");
        assertEquals(serverB.token(), redis.get("lock:" + JOB),
                "server B still holds the lock: A's release must not delete a key that holds someone else's token");
        assertTrue(locks.release(serverB), "server B's own release should work");
    }

    @Test
    @DisplayName("Fencing tokens grow by one per holder, and the store refuses a write with an older token")
    void fencing() {
        FencedStore ledger = new FencedStore();
        Lease serverA = held(locks.tryAcquire(JOB, 30_000), "the lock is free, so server A should get a lease");
        assertTrue(locks.tryAcquire(JOB, 30_000).isEmpty(), "a refused tryAcquire should not use up a fencing token");
        clock.advanceMillis(31_000);
        Lease serverB = held(locks.tryAcquire(JOB, 30_000), "A's lease ran out, so server B should get the lock");
        assertEquals(1, serverA.fencingToken(), "the first holder gets fencing token 1");
        assertEquals(2, serverB.fencingToken(), "the next holder gets 2, even though a refused attempt happened in between");

        assertTrue(ledger.write(serverB.fencingToken(), "charge rental-17"), "B's write with token 2 should be accepted");
        assertTrue(ledger.write(serverB.fencingToken(), "charge rental-18"), "B may write again with the same token 2");
        assertFalse(ledger.write(serverA.fencingToken(), "charge rental-17"),
                "A wakes up and writes with token 1, older than 2: the store must refuse it");
        assertEquals(List.of("charge rental-17", "charge rental-18"), ledger.entries(), "only B's two writes should be kept");
    }
}

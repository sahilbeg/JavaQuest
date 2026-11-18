package jq.m09.resilience;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenBucketTest {

    private final FakeClock clock = new FakeClock(5_000);

    @Test
    @DisplayName("A new bucket starts full, lets exactly 'capacity' requests through, then says no")
    void startsFull() {
        TokenBucket bucket = new TokenBucket(3, 1, clock);

        assertEquals(3, bucket.availableTokens(), "a new bucket should start full: 3 tokens for capacity 3");
        assertTrue(bucket.tryAcquire(), "request 1 of 3 should get a token");
        assertTrue(bucket.tryAcquire(), "request 2 of 3 should get a token");
        assertTrue(bucket.tryAcquire(), "request 3 of 3 should get a token");
        assertFalse(bucket.tryAcquire(), "request 4 should be refused: the bucket is empty and no time has passed");
        assertEquals(0, bucket.availableTokens(), "after 3 requests and no time, 0 tokens should be left");

        assertThrows(IllegalArgumentException.class, () -> new TokenBucket(0, 1, clock),
                "a capacity of 0 could never let anything through: throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new TokenBucket(3, 0, clock),
                "a refill of 0 per second would never refill: throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Part of a token is kept: 300 ms then 200 ms at 2 tokens a second make one whole token")
    void keepsPartialRefill() {
        TokenBucket bucket = new TokenBucket(1, 2, clock);
        assertTrue(bucket.tryAcquire(), "the first request should take the only token");

        clock.advance(300);
        assertFalse(bucket.tryAcquire(), "after 300 ms at 2 tokens a second, only 0.6 of a token has built up: refuse");

        clock.advance(200);
        assertTrue(bucket.tryAcquire(),
                "300 ms + 200 ms = 500 ms, which is one whole token at 2 a second. Did you throw away the 0.6 at the last check?");
        assertFalse(bucket.tryAcquire(), "that token is spent, so the next request at the same moment should be refused");
    }

    @Test
    @DisplayName("Capacity 1, refill 1 a second: refused at 999 ms, allowed at exactly 1000 ms")
    void capacityOneBoundary() {
        TokenBucket bucket = new TokenBucket(1, 1, clock);
        assertTrue(bucket.tryAcquire(), "the first request should get the only token");

        clock.advance(999);
        assertFalse(bucket.tryAcquire(), "after 999 ms, the token is not complete yet");

        clock.advance(1);
        assertTrue(bucket.tryAcquire(), "after exactly 1000 ms, one whole token is back");
    }

    @Test
    @DisplayName("A long quiet gap refills the bucket only up to its capacity, never above it")
    void longGapNeverExceedsCapacity() {
        TokenBucket bucket = new TokenBucket(5, 10, clock);
        for (int i = 1; i <= 5; i++) {
            assertTrue(bucket.tryAcquire(), "request " + i + " of 5 should get a token from the full bucket");
        }

        clock.advance(24L * 60 * 60 * 1000);   // a whole day passes
        assertEquals(5, bucket.availableTokens(), "after a day the bucket is full again, but capacity 5 is the most it can hold");
        for (int i = 1; i <= 5; i++) {
            assertTrue(bucket.tryAcquire(), "burst request " + i + " of 5 should get a token");
        }
        assertFalse(bucket.tryAcquire(), "request 6 of the burst should be refused: capacity is the biggest burst");
    }

    @Test
    @DisplayName("millisUntilNextToken says how long until a whole token is ready, rounded up")
    void millisUntilNextToken() {
        TokenBucket bucket = new TokenBucket(2, 3, clock);
        assertEquals(0, bucket.millisUntilNextToken(), "a full bucket has a token ready now: 0 ms");
        bucket.tryAcquire();
        bucket.tryAcquire();

        assertEquals(334, bucket.millisUntilNextToken(),
                "an empty bucket at 3 tokens a second needs 1000 / 3 = 333.3 ms, which rounds UP to 334");
        clock.advance(100);
        assertEquals(234, bucket.millisUntilNextToken(), "100 ms later, 0.3 of a token is there; 0.7 more takes 233.3 ms, so 234");
        clock.advance(233);
        assertEquals(1, bucket.millisUntilNextToken(), "333 ms in, 0.999 of a token is there; the last bit needs 1 ms, not 0");
        clock.advance(1);
        assertEquals(0, bucket.millisUntilNextToken(), "334 ms in, a whole token is ready");
        assertTrue(bucket.tryAcquire(), "and a request should now get it");
    }
}

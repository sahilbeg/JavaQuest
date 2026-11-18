package jq.m09.resilience;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateLimiterTest {

    private final FakeClock clock = new FakeClock(0);

    @Test
    @DisplayName("Each key has its own bucket: a partner app that floods cannot use up a rider's share")
    void keysAreSeparate() {
        RateLimiter limiter = new RateLimiter(3, 1, clock);

        for (int i = 1; i <= 3; i++) {
            assertTrue(limiter.tryAcquire("key-partner-app"), "request " + i + " of 3 from the partner app should be allowed");
        }
        assertFalse(limiter.tryAcquire("key-partner-app"), "the partner app's 4th request at once should be refused (429)");
        for (int i = 1; i <= 3; i++) {
            assertTrue(limiter.tryAcquire("rider-7"), "rider-7's request " + i + " should be allowed: rider-7 has a separate, full bucket");
        }
        assertFalse(limiter.tryAcquire("rider-7"), "rider-7's 4th request at once should be refused too");

        assertThrows(IllegalArgumentException.class, () -> new RateLimiter(0, 1, clock),
                "capacity 0 makes no sense: throw IllegalArgumentException");
    }

    @Test
    @DisplayName("retryAfterSeconds gives whole seconds for Retry-After, rounded up, and each key refills on its own")
    void retryAfterSeconds() {
        RateLimiter limiter = new RateLimiter(2, 1, clock);
        assertEquals(0, limiter.retryAfterSeconds("rider-7"), "a key with tokens left can go now: 0 seconds");
        assertTrue(limiter.tryAcquire("rider-7"), "rider-7's first request should be allowed");
        assertTrue(limiter.tryAcquire("rider-7"), "rider-7's second request should be allowed");
        assertFalse(limiter.tryAcquire("rider-7"), "rider-7's third request at once should be refused");

        assertEquals(1, limiter.retryAfterSeconds("rider-7"), "the next token comes in 1000 ms, which is 1 second");
        assertEquals(0, limiter.retryAfterSeconds("rider-8"), "rider-8 has not sent anything, so rider-8 can go now");
        clock.advance(999);
        assertEquals(1, limiter.retryAfterSeconds("rider-7"),
                "1 ms is still missing. Round UP to 1 second: Retry-After: 0 would invite the app to hammer us at once");
        clock.advance(1);
        assertEquals(0, limiter.retryAfterSeconds("rider-7"), "after exactly 1000 ms a token is back: 0 seconds");
        assertTrue(limiter.tryAcquire("rider-7"), "and rider-7's request should be allowed again");
        assertFalse(limiter.tryAcquire("rider-7"), "only one token came back in one second, so the next is refused");
    }
}

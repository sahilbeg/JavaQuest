package jq.m09.resilience;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BackoffTest {

    @Test
    @DisplayName("The ceiling doubles from the base after each failed attempt, and stops at the cap")
    void ceilingsDoubleUpToTheCap() {
        Backoff backoff = new Backoff(200, 5_000, new FakeRandom());
        long[] expected = {200, 400, 800, 1_600, 3_200, 5_000, 5_000};
        for (int attempt = 1; attempt <= expected.length; attempt++) {
            assertEquals(expected[attempt - 1], backoff.ceilingMillis(attempt),
                    "ceiling after attempt " + attempt + " should be min(5000, 200 * 2^" + (attempt - 1) + ")");
        }
        assertThrows(IllegalArgumentException.class, () -> backoff.ceilingMillis(0), "attempts are counted from 1, so 0 is an error");
    }

    @Test
    @DisplayName("Huge attempt numbers still give the cap, never a negative or tiny number from overflow")
    void hugeAttemptsStayAtTheCap() {
        Backoff backoff = new Backoff(200, 5_000, new FakeRandom());
        for (int attempt : new int[] {63, 64, 65, 100, 1_000, Integer.MAX_VALUE}) {
            long ceiling = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> backoff.ceilingMillis(attempt),
                    "ceilingMillis(" + attempt + ") should finish quickly: stop doubling once you reach the cap");
            assertEquals(5_000, ceiling, "attempt " + attempt + " should give the cap, 5000. In Java, 1L << 64 is 1, not a huge number");
        }
    }

    @Test
    @DisplayName("Full jitter: the delay is random.nextLong(ceiling + 1), anywhere from 0 to the ceiling")
    void fullJitterAsksForTheRightBound() {
        FakeRandom random = new FakeRandom();   // always picks the biggest allowed value
        Backoff backoff = new Backoff(200, 5_000, random);

        assertEquals(200, backoff.delayMillis(1), "the biggest jittered wait after attempt 1 is the ceiling, 200");
        assertEquals(400, backoff.delayMillis(2), "the biggest jittered wait after attempt 2 is 400");
        assertEquals(5_000, backoff.delayMillis(9), "the biggest jittered wait after attempt 9 is the cap, 5000");
        assertEquals(List.of(201L, 401L, 5_001L), random.bounds,
                "call random.nextLong(ceiling + 1) once per delay: the + 1 lets the ceiling itself be picked");
    }

    @Test
    @DisplayName("With a real seeded Random, every delay lies between 0 and its ceiling, and they differ")
    void seededDelaysStayInRange() {
        Backoff backoff = new Backoff(100, 2_000, new Random(42));
        boolean someAboveZero = false;
        for (int attempt = 1; attempt <= 30; attempt++) {
            long delay = backoff.delayMillis(attempt);
            long ceiling = backoff.ceilingMillis(attempt);
            assertTrue(delay >= 0 && delay <= ceiling,
                    "the delay after attempt " + attempt + " was " + delay + ", but it must be from 0 to " + ceiling);
            someAboveZero |= delay > 0;
        }
        assertTrue(someAboveZero, "30 random delays should not all be 0: pick a random value up to the ceiling");

        assertThrows(IllegalArgumentException.class, () -> new Backoff(0, 2_000, new Random(42)),
                "a base of 0 ms makes no sense: throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new Backoff(500, 100, new Random(42)),
                "a cap below the base makes no sense: throw IllegalArgumentException");
    }
}

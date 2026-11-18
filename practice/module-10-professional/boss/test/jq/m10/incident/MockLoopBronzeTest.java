package jq.m10.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MockLoopBronzeTest {

    private static final Duration LIMIT = Duration.ofSeconds(2);

    private static long bestRun(int... days) {
        return assertTimeoutPreemptively(LIMIT, () -> CodingRound.bestRun(days),
                "bestRun should answer a handful of days at once");
    }

    @Test
    @DisplayName("Bronze: the candidate's own example still works: 500, -200, 700, -1000, 300 gives 1000")
    void happyPath() {
        assertEquals(1000, bestRun(500, -200, 700, -1000, 300),
                "500 - 200 + 700 = 1000 is the best run. Adding -1000 and 300 only makes it smaller");
    }

    @Test
    @DisplayName("Bronze: a best run that ends on the last day counts")
    void lastDayCounts() {
        assertEquals(500, bestRun(-100, 200, 300),
                "200 + 300 = 500, and that run ends on the last day. Do the loops stop one day early?");
        assertEquals(700, bestRun(700),
                "One day is a run on its own: {700} gives 700, not 0");
    }

    @Test
    @DisplayName("Bronze: when every day loses money, the best run is the smallest loss")
    void allLosingDays() {
        assertEquals(-50, bestRun(-300, -50, -200),
                "A run has at least one day, so the best is the single day of -50. "
                        + "0 would be an empty run, which the README does not allow");
        assertEquals(-1, bestRun(-1),
                "{-1} has one run, worth -1");
    }

    @Test
    @DisplayName("Bronze: no days at all throws IllegalArgumentException")
    void noDays() {
        assertThrows(IllegalArgumentException.class, () -> CodingRound.bestRun(new int[0]),
                "With no days there is no run, so bestRun must throw IllegalArgumentException, not return 0");
    }

    @Test
    @DisplayName("Bronze: big days add up in long and do not overflow")
    void noOverflow() {
        assertEquals(4_000_000_001L, bestRun(2_000_000_000, 2_000_000_000, 1),
                "2,000,000,000 + 2,000,000,000 + 1 = 4,000,000,001, more than an int can hold (2,147,483,647). "
                        + "An int total wraps round to a negative number. Add up in long");
    }
}

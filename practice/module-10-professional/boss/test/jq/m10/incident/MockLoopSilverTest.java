package jq.m10.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MockLoopSilverTest {

    private static final Duration LIMIT = Duration.ofSeconds(3);
    private static final int DAYS = 1_000_000;

    private static long timedBestRun(int[] days) {
        return assertTimeoutPreemptively(LIMIT, () -> CodingRound.bestRun(days),
                "The follow-up: a million days must take one pass, O(n). "
                        + "Trying every start with every end is about 500 billion steps and runs out of time");
    }

    @Test
    @DisplayName("Silver: a million days that go up 3 and down 1 finish in time")
    void upAndDown() {
        int[] days = new int[DAYS];
        for (int i = 0; i < DAYS; i++) {
            days[i] = i % 2 == 0 ? 3 : -1;
        }
        assertEquals(1_000_001L, timedBestRun(days),
                "3, -1, 3, -1, ...: the best run is everything except the final -1: "
                        + "500,000 x 3 - 499,999 = 1,000,001");
    }

    @Test
    @DisplayName("Silver: a million losing days with one good stretch in the middle finish in time")
    void oneGoodStretch() {
        int[] days = new int[DAYS];
        Arrays.fill(days, -1);
        days[0] = 7;
        for (int i = 400_000; i < 401_000; i++) {
            days[i] = 5;
        }
        assertEquals(5_000L, timedBestRun(days),
                "1,000 days of 5 in a row give 5,000. Day 0 alone gives only 7, and every -1 makes a run smaller");
    }

    @Test
    @DisplayName("Silver: a million big days finish in time and do not overflow")
    void bigYear() {
        int[] days = new int[DAYS];
        Arrays.fill(days, 2_000_000);
        assertEquals(2_000_000_000_000L, timedBestRun(days),
                "1,000,000 days of 2,000,000 cents add up to 2,000,000,000,000 cents");
    }
}

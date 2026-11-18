package jq.m09.performance;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LatencyStatsTest {

    /** 1 to 100 in a scrambled order: 37 has no common factor with 100, so i * 37 % 100 visits every value once. */
    private static long[] oneToHundredShuffled() {
        long[] millis = new long[100];
        for (int i = 0; i < 100; i++) {
            millis[i] = (i * 37L) % 100 + 1;
        }
        return millis;
    }

    @Test
    @DisplayName("The median of ten samples is the 5th smallest, by nearest rank")
    void medianOfTen() {
        long[] millis = {90, 20, 70, 10, 50, 30, 100, 40, 80, 60};
        assertEquals(50, LatencyStats.percentile(millis, 50),
                "sorted: 10 20 30 40 50 60 70 80 90 100. Rank = ceil(50 x 10 / 100) = 5, and the 5th value is 50");
        assertEquals(90, LatencyStats.percentile(millis, 90), "rank = ceil(90 x 10 / 100) = 9, the 9th value is 90");
        assertEquals(30, LatencyStats.percentile(millis, 21),
                "rank = ceil(21 x 10 / 100) = ceil(2.1) = 3, so p21 is the 3rd value, 30. Round the rank UP");
    }

    @Test
    @DisplayName("p95, p99 and p100 of the numbers 1 to 100 are 95, 99 and 100")
    void highPercentilesOfHundred() {
        long[] millis = oneToHundredShuffled();
        assertEquals(95, LatencyStats.percentile(millis, 95), "with 100 samples, p95 is the 95th smallest, 95");
        assertEquals(99, LatencyStats.percentile(millis, 99), "with 100 samples, p99 is the 99th smallest, 99");
        assertEquals(100, LatencyStats.percentile(millis, 100), "p100 is the slowest sample, 100");
        assertEquals(1, LatencyStats.percentile(millis, 1), "p1 is rank ceil(1 x 100 / 100) = 1, the fastest sample");
    }

    @Test
    @DisplayName("With few samples, p99 is simply the slowest one, and one sample answers every percentile")
    void smallSamples() {
        long[] ten = {12, 15, 11, 14, 13, 19, 18, 17, 16, 240};
        assertEquals(240, LatencyStats.percentile(ten, 99),
                "rank = ceil(99 x 10 / 100) = ceil(9.9) = 10, so p99 of 10 samples is the slowest one, 240");
        long[] one = {42};
        assertEquals(42, LatencyStats.percentile(one, 1), "with one sample, p1 is that sample");
        assertEquals(42, LatencyStats.percentile(one, 50), "with one sample, p50 is that sample");
        assertEquals(42, LatencyStats.percentile(one, 100), "with one sample, p100 is that sample");
    }

    @Test
    @DisplayName("summarize shows the slow tail that an average hides")
    void summaryShowsTheTail() {
        long[] millis = new long[100];
        java.util.Arrays.fill(millis, 100);
        millis[13] = 5000;
        millis[77] = 5000;
        assertEquals(new LatencySummary(100, 100, 100, 5000, 5000), LatencyStats.summarize(millis),
                "98 requests took 100 ms and 2 took 5000 ms. p50 and p95 are 100, but p99 (rank 99) is 5000, and so is the max."
                        + " The average, 198 ms, would hide both slow requests");
    }

    @Test
    @DisplayName("The caller's array keeps its order after percentile and summarize")
    void doesNotSortTheCallersArray() {
        long[] millis = {300, 100, 200, 50};
        LatencyStats.percentile(millis, 50);
        LatencyStats.summarize(millis);
        assertArrayEquals(new long[] {300, 100, 200, 50}, millis,
                "the samples were sorted in place. Sort a copy (Arrays.copyOf) so the caller's data keeps its order");
        assertEquals(100, LatencyStats.percentile(millis, 50), "p50 of 50, 100, 200, 300 is rank 2, which is 100");
    }

    @Test
    @DisplayName("No samples: percentile refuses, summarize gives all zeros, and a percent outside 1 to 100 is refused")
    void emptyAndInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> LatencyStats.percentile(new long[0], 50),
                "a percentile of no samples is undefined, so percentile should throw IllegalArgumentException");
        assertEquals(new LatencySummary(0, 0, 0, 0, 0), LatencyStats.summarize(new long[0]),
                "summarize of no samples should be all zeros, so a quiet dashboard does not crash");
        long[] millis = {10, 20, 30};
        assertThrows(IllegalArgumentException.class, () -> LatencyStats.percentile(millis, 0),
                "percent 0 is outside 1 to 100, so percentile should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> LatencyStats.percentile(millis, 101),
                "percent 101 is outside 1 to 100, so percentile should throw IllegalArgumentException");
    }
}

package jq.m04.twopointers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StretchesTest {

    private static final Duration FAST = Duration.ofSeconds(2);

    @Test
    @DisplayName("pairWithTotal finds the two fares that add up to the target")
    void pairFound() {
        int[] fares = {150, 250, 400, 550, 700, 900};
        assertArrayEquals(new int[] {2, 4}, Stretches.pairWithTotal(fares, 1100),
                "In {150, 250, 400, 550, 700, 900}, only 400 + 700 make 1100, so return {2, 4}");
        assertArrayEquals(new int[] {0, 5}, Stretches.pairWithTotal(fares, 1050),
                "150 + 900 make 1050, so return {0, 5}");
        assertArrayEquals(new int[] {0, 1}, Stretches.pairWithTotal(new int[] {300, 300, 400}, 600),
                "Two different fares can have the same value: 300 at index 0 and 300 at index 1 make 600");
    }

    @Test
    @DisplayName("pairWithTotal returns an empty array when no pair works, and never uses one fare twice")
    void pairMissing() {
        assertArrayEquals(new int[0], Stretches.pairWithTotal(new int[] {150, 250, 400}, 1000),
                "No two of {150, 250, 400} make 1000, so return an empty array");
        assertArrayEquals(new int[0], Stretches.pairWithTotal(new int[] {250, 500, 900}, 1000),
                "500 + 500 is 1000, but there is only one 500. Loop while left < right, not left <= right");
        assertArrayEquals(new int[0], Stretches.pairWithTotal(new int[0], 100),
                "An empty array has no pair, so return an empty array");
        assertArrayEquals(new int[0], Stretches.pairWithTotal(new int[] {500}, 1000),
                "One fare cannot make a pair on its own, so return an empty array");
    }

    @Test
    @DisplayName("pairWithTotal checks 200,000 fares in one pass")
    void pairFast() {
        int[] fares = new int[200_000];
        for (int i = 0; i < fares.length; i++) {
            fares[i] = 2 * i;
        }
        int[] answer = assertTimeoutPreemptively(FAST, () -> Stretches.pairWithTotal(fares, 99_999),
                "200,000 fares should take one pass. Two nested loops try 20 billion pairs and time out");
        assertEquals(0, answer.length,
                "Every fare is even, so no two can make the odd number 99,999. Return an empty array");
        int[] found = assertTimeoutPreemptively(FAST, () -> Stretches.pairWithTotal(fares, 6),
                "The search should finish at once");
        assertEquals(2, found.length, "0 + 6 and 2 + 4 both make 6, so a pair should come back");
        assertEquals(6, fares[found[0]] + fares[found[1]], "The two fares you return should add up to 6");
        assertTrue(found[0] < found[1], "Return the smaller index first");
    }

    @Test
    @DisplayName("bestWindow finds the busiest k minutes in a row")
    void bestWindowWorks() {
        int[] rides = {2, 5, 1, 8, 3, 0, 4};
        assertEquals(14, Stretches.bestWindow(rides, 3),
                "The busiest 3 minutes in a row are 5, 1, 8, which make 14 rides");
        assertEquals(8, Stretches.bestWindow(rides, 1),
                "With k = 1, the best window is the single busiest minute, 8");
        assertEquals(23, Stretches.bestWindow(rides, 7),
                "With k equal to the length, the only window is the whole array, 23 rides");
        assertEquals(5, Stretches.bestWindow(new int[] {1, 1, 1, 4}, 2),
                "The best window can be the last one: 1 + 4 = 5. Check the window after the final slide too");
    }

    @Test
    @DisplayName("bestWindow returns 0 when no window of size k fits")
    void bestWindowEdges() {
        assertEquals(0, Stretches.bestWindow(new int[] {3, 4}, 3), "A 3-minute window cannot fit in 2 minutes, so return 0");
        assertEquals(0, Stretches.bestWindow(new int[] {3, 4}, 0), "k = 0 is not a window, so return 0");
        assertEquals(0, Stretches.bestWindow(new int[0], 1), "An empty day has no window, so return 0");
    }

    @Test
    @DisplayName("bestWindow slides over a 200,000-minute log without re-adding each window")
    void bestWindowFast() {
        int[] rides = new int[200_000];
        Arrays.fill(rides, 1);
        for (int i = 150_000; i < 150_100; i++) {
            rides[i] = 5;
        }
        int best = assertTimeoutPreemptively(FAST, () -> Stretches.bestWindow(rides, 100_000),
                "Slide the window: add one value and subtract one value per step. Re-adding 100,000 values each time is too slow");
        assertEquals(100_400, best,
                "The best 100,000-minute window holds all 100 busy minutes: 100,000 + 100 * 4 = 100,400");
    }

    @Test
    @DisplayName("longestStretchWithin finds the longest run whose total is at most the limit")
    void longestWorks() {
        int[] visits = {10, 5, 20, 5, 5, 30, 5};
        assertEquals(4, Stretches.longestStretchWithin(visits, 35),
                "5 + 20 + 5 + 5 = 35 fits a 35-minute limit exactly, so the longest run has 4 visits");
        assertEquals(2, Stretches.longestStretchWithin(visits, 15),
                "With a 15-minute limit, the best runs are 10 + 5 or 5 + 5, so 2 visits");
        assertEquals(7, Stretches.longestStretchWithin(visits, 1000),
                "With a huge limit, every visit fits, so the answer is the whole array, 7");
    }

    @Test
    @DisplayName("longestStretchWithin handles empty arrays, values over the limit and zero minutes")
    void longestEdges() {
        assertEquals(0, Stretches.longestStretchWithin(new int[0], 30), "No visits means a run of 0");
        assertEquals(0, Stretches.longestStretchWithin(new int[] {40, 50}, 30),
                "Every visit is longer than 30 minutes on its own, so nothing fits: return 0");
        assertEquals(2, Stretches.longestStretchWithin(new int[] {0, 0, 3, 0}, 0),
                "With a limit of 0, only the zero-minute visits fit. The longest run is the first two");
        assertEquals(1, Stretches.longestStretchWithin(new int[] {9, 50, 9}, 10),
                "Shrink until the window fits again: after 50, the window empties, then 9 fits on its own");
    }

    @Test
    @DisplayName("longestStretchWithin runs in one pass over 200,000 visits")
    void longestFast() {
        int[] visits = new int[200_000];
        Arrays.fill(visits, 1);
        int longest = assertTimeoutPreemptively(FAST, () -> Stretches.longestStretchWithin(visits, 50_000),
                "Each pointer should only move forward. Restarting from every left index is too slow");
        assertEquals(50_000, longest, "200,000 one-minute visits with a 50,000-minute limit: the longest run is 50,000");
    }

    @Test
    @DisplayName("prefixSums builds running totals and rangeTotal reads any range from them")
    void prefixWorks() {
        long[] prefix = Stretches.prefixSums(new int[] {4, 0, 7, 2, 9});
        assertArrayEquals(new long[] {0, 4, 4, 11, 13, 22}, prefix,
                "prefixSums of {4, 0, 7, 2, 9} should be {0, 4, 4, 11, 13, 22}: one cell longer, starting with 0");
        assertEquals(9, Stretches.rangeTotal(prefix, 1, 3), "Hours 1 to 3 hold 0 + 7 + 2 = 9 rides");
        assertEquals(22, Stretches.rangeTotal(prefix, 0, 4), "Hours 0 to 4 are the whole day, 22 rides");
        assertEquals(7, Stretches.rangeTotal(prefix, 2, 2), "Hour 2 alone holds 7 rides");
        assertArrayEquals(new long[] {0}, Stretches.prefixSums(new int[0]),
                "The prefix array of an empty array is just {0}");
        long[] big = Stretches.prefixSums(new int[] {2_000_000_000, 2_000_000_000});
        assertEquals(4_000_000_000L, Stretches.rangeTotal(big, 0, 1),
                "Totals can pass the int limit. Keep them in long: 2,000,000,000 twice is 4,000,000,000");
    }

    @Test
    @DisplayName("rangeTotal throws IllegalArgumentException for a range that does not exist")
    void prefixBadRanges() {
        long[] prefix = Stretches.prefixSums(new int[] {4, 0, 7, 2, 9});
        assertThrows(IllegalArgumentException.class, () -> Stretches.rangeTotal(prefix, 3, 1),
                "from 3 to 1 runs backwards, so throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> Stretches.rangeTotal(prefix, -1, 2),
                "from = -1 is before the first hour, so throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> Stretches.rangeTotal(prefix, 2, 5),
                "There are only 5 hours (0 to 4), so to = 5 should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("rangeTotal answers 200,000 questions about 200,000 hours at once")
    void prefixFast() {
        int[] rides = new int[200_000];
        Arrays.fill(rides, 1);
        long checked = assertTimeoutPreemptively(FAST, () -> {
            long[] prefix = Stretches.prefixSums(rides);
            long sum = 0;
            for (int i = 0; i < rides.length; i++) {
                sum += Stretches.rangeTotal(prefix, i, rides.length - 1);
            }
            return sum;
        }, "Build the prefix array once. Each rangeTotal should be one subtraction, not a loop");
        assertEquals(20_000_100_000L, checked,
                "With 1 ride every hour, the range from i to the end holds 200,000 - i rides. Those add up to 20,000,100,000");
    }

    @Test
    @DisplayName("countStretchesSummingTo counts every run that adds up to k, even with negative values")
    void countWorks() {
        assertEquals(1, Stretches.countStretchesSummingTo(new int[] {3, 4}, 3),
                "{3} alone makes 3. A run that starts at index 0 needs the seed seen.put(0L, 1)");
        assertEquals(4, Stretches.countStretchesSummingTo(new int[] {1, -1, 1, -1}, 0),
                "{1, -1} twice, {-1, 1} once and the whole array make 0, so the count is 4");
        assertEquals(4, Stretches.countStretchesSummingTo(new int[] {2, -1, 1, 2}, 2),
                "{2}, {2, -1, 1}, {-1, 1, 2} and the last {2} make 2. A sliding window misses some because of the -1");
        assertEquals(3, Stretches.countStretchesSummingTo(new int[] {3, 4, -2, 1, 3}, 3),
                "{3}, {4, -2, 1} and the last {3} make 3, so the count is 3");
        assertEquals(0, Stretches.countStretchesSummingTo(new int[0], 0), "An empty array has no runs, so the count is 0");
    }

    @Test
    @DisplayName("countStretchesSummingTo counts billions of runs in one pass, in a long")
    void countFast() {
        int[] netBikes = new int[200_000];
        long count = assertTimeoutPreemptively(FAST, () -> Stretches.countStretchesSummingTo(netBikes, 0),
                "Use running totals and a HashMap for one pass. Trying every start and end is too slow");
        assertEquals(20_000_100_000L, count,
                "200,000 zeros hold 200,000 * 200,001 / 2 = 20,000,100,000 runs that add up to 0. Count them in a long");
    }
}

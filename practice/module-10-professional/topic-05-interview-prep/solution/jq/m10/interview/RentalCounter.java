package jq.m10.interview;

import java.util.Arrays;

/**
 * Problem 2 of the mock coding round: how many rentals started in the last 300 seconds?
 * 300 buckets, one per second of the window, reused in a circle. O(1) memory.
 */
public class RentalCounter {

    public static final int WINDOW_SECONDS = 300;

    private final long[] bucketSecond = new long[WINDOW_SECONDS];
    private final int[] bucketCount = new int[WINDOW_SECONDS];
    private long latest = 0;

    public RentalCounter() {
        Arrays.fill(bucketSecond, -1);
    }

    /** Counts one rental that started at this second. O(1). */
    public void record(long second) {
        checkTime(second);
        int i = (int) (second % WINDOW_SECONDS);
        if (bucketSecond[i] != second) {   // the bucket holds an old second: start it again
            bucketSecond[i] = second;
            bucketCount[i] = 0;
        }
        bucketCount[i]++;
    }

    /** Rentals that started from second - 299 to second, both included. O(300), which is O(1). */
    public int countRecent(long second) {
        checkTime(second);
        int total = 0;
        for (int i = 0; i < WINDOW_SECONDS; i++) {
            if (bucketSecond[i] >= 0 && second - bucketSecond[i] < WINDOW_SECONDS) {
                total += bucketCount[i];
            }
        }
        return total;
    }

    private void checkTime(long second) {
        if (second < 0) {
            throw new IllegalArgumentException("a second cannot be negative: " + second);
        }
        if (second < latest) {
            throw new IllegalArgumentException("time went backwards: " + second + " after " + latest);
        }
        latest = second;
    }
}

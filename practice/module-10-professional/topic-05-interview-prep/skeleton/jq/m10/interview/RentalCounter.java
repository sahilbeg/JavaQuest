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
        // TODO 6: find the bucket for this second: second % WINDOW_SECONDS.
        // TODO 7: if that bucket holds a different second, it is old: set its second and reset its count to 0.
        // TODO 8: add one to the bucket's count.
    }

    /** Rentals that started from second - 299 to second, both included. O(300), which is O(1). */
    public int countRecent(long second) {
        checkTime(second);
        // TODO 9: add up the counts of every bucket whose second is in the window:
        //         bucketSecond[i] >= 0 and second - bucketSecond[i] < WINDOW_SECONDS.
        return 0;
    }

    private void checkTime(long second) {
        // TODO 10: throw IllegalArgumentException if second is negative, or smaller than latest.
        //          Otherwise remember it: latest = second.
    }
}

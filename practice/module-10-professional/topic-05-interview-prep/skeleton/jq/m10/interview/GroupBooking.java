package jq.m10.interview;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Problem 3 of the mock coding round: the biggest group booking a station can promise.
 * This is "largest rectangle in a histogram". Aim for O(n) with a monotonic stack.
 */
public class GroupBooking {

    /**
     * The most bike-hours one booking can get: pick consecutive hours and a number of bikes
     * that is free in every one of them. O(n) time, O(n) space.
     */
    public static long mostBikeHours(int[] freeBikes) {
        // TODO 11: throw IllegalArgumentException if any hour has a negative number of free bikes.
        // TODO 12: keep a stack of hour indexes whose free bikes go up from bottom to top.
        //          Walk i from 0 to freeBikes.length (one step past the end, using 0 bikes there).
        //          While the top hour has >= the bikes at i, pop it: it is the lowest hour of a block
        //          that runs from just after the new top (or -1) to just before i.
        //          Its bike-hours are (long) its bikes * (i - left - 1). Keep the best. Then push i.
        return 0;
    }
}

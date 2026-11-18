package jq.m10.interview;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Problem 3 of the mock coding round: the biggest group booking a station can promise.
 * This is "largest rectangle in a histogram", solved with a monotonic stack.
 */
public class GroupBooking {

    /**
     * The most bike-hours one booking can get: pick consecutive hours and a number of bikes
     * that is free in every one of them. O(n) time, O(n) space.
     */
    public static long mostBikeHours(int[] freeBikes) {
        for (int bikes : freeBikes) {
            if (bikes < 0) {
                throw new IllegalArgumentException("free bikes cannot be negative: " + bikes);
            }
        }
        Deque<Integer> stack = new ArrayDeque<>();   // hour indexes; their free bikes go up from bottom to top
        long best = 0;
        for (int i = 0; i <= freeBikes.length; i++) {
            int bikes = i == freeBikes.length ? 0 : freeBikes[i];   // a 0 at the end empties the stack
            while (!stack.isEmpty() && freeBikes[stack.peek()] >= bikes) {
                int lowest = stack.pop();                           // this hour limits the block
                int left = stack.isEmpty() ? -1 : stack.peek();     // first hour to the left with fewer bikes
                long hours = i - left - 1;
                best = Math.max(best, (long) freeBikes[lowest] * hours);
            }
            stack.push(i);
        }
        return best;
    }
}

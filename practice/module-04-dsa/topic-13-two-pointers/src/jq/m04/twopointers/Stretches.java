package jq.m04.twopointers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Stretches {

    public static int[] pairWithTotal(int[] sortedFares, int target) {
        // TODO 1: put left at index 0 and right at the last index. While left < right,
        //         return {left, right} when the two fares add up to target. If the sum is
        //         too small, move left up. If it is too big, move right down.
        //         Return new int[0] when the pointers meet without a match.
        return new int[] {-1, -1};
    }

    public static int bestWindow(int[] ridesPerMinute, int k) {
        // TODO 2: return 0 when k < 1 or k is longer than the array.
        // TODO 3: add up the first k values. Then slide: add the value coming in,
        //         subtract the value going out (index i - k), and keep the best total.
        return -1;
    }

    public static int longestStretchWithin(int[] minutes, int limit) {
        // TODO 4: grow the window with a right pointer and keep a running total.
        //         While the total is over limit, take minutes[left] out and move left up.
        //         After each step, the window right - left + 1 fits, so keep the longest.
        return -1;
    }

    public static long[] prefixSums(int[] values) {
        // TODO 5: make a long[] one cell longer than values. prefix[0] is 0 and
        //         prefix[i + 1] = prefix[i] + values[i].
        return new long[0];
    }

    public static long rangeTotal(long[] prefix, int from, int to) {
        // TODO 6: throw IllegalArgumentException when from < 0, from > to,
        //         or to is past the last value (to >= prefix.length - 1).
        // TODO 7: return prefix[to + 1] - prefix[from], the total from index from to index to.
        return -1;
    }

    public static long countStretchesSummingTo(int[] values, int k) {
        // TODO 8: make a Map<Long, Integer> of running totals seen so far, and seed it with
        //         seen.put(0L, 1). For each value, add it to running, add
        //         seen.getOrDefault(running - k, 0) to count, then seen.merge(running, 1, Integer::sum).
        Map<Long, Integer> seen = new HashMap<>();
        return -1;
    }

    public static void main(String[] args) {
        int[] fares = {150, 250, 400, 550, 700, 900};
        System.out.println("Fares that add up to 1100 sit at " + Arrays.toString(pairWithTotal(fares, 1100)));

        int[] rides = {2, 5, 1, 8, 3, 0, 4};
        System.out.println("Busiest 3 minutes: " + bestWindow(rides, 3) + " rides");

        int[] visits = {10, 5, 20, 5, 5, 30, 5};
        System.out.println("Most visits in a 35-minute shift: " + longestStretchWithin(visits, 35));

        long[] prefix = prefixSums(new int[] {4, 0, 7, 2, 9});
        System.out.println("Rides from hour 1 to hour 3: " + rangeTotal(prefix, 1, 3));

        int[] netBikes = {1, -1, 1, -1};
        System.out.println("Stretches that net 0 bikes: " + countStretchesSummingTo(netBikes, 0));
    }
}

package jq.m04.twopointers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Stretches {

    public static int[] pairWithTotal(int[] sortedFares, int target) {
        int left = 0;
        int right = sortedFares.length - 1;
        while (left < right) {
            long sum = (long) sortedFares[left] + sortedFares[right];
            if (sum == target) {
                return new int[] {left, right};
            } else if (sum < target) {
                left++;
            } else {
                right--;
            }
        }
        return new int[0];
    }

    public static int bestWindow(int[] ridesPerMinute, int k) {
        if (k < 1 || k > ridesPerMinute.length) {
            return 0;
        }
        int window = 0;
        for (int i = 0; i < k; i++) {
            window += ridesPerMinute[i];
        }
        int best = window;
        for (int i = k; i < ridesPerMinute.length; i++) {
            window += ridesPerMinute[i] - ridesPerMinute[i - k];
            best = Math.max(best, window);
        }
        return best;
    }

    public static int longestStretchWithin(int[] minutes, int limit) {
        int left = 0;
        long total = 0;
        int best = 0;
        for (int right = 0; right < minutes.length; right++) {
            total += minutes[right];
            while (total > limit) {
                total -= minutes[left];
                left++;
            }
            best = Math.max(best, right - left + 1);
        }
        return best;
    }

    public static long[] prefixSums(int[] values) {
        long[] prefix = new long[values.length + 1];
        for (int i = 0; i < values.length; i++) {
            prefix[i + 1] = prefix[i] + values[i];
        }
        return prefix;
    }

    public static long rangeTotal(long[] prefix, int from, int to) {
        if (from < 0 || to >= prefix.length - 1 || from > to) {
            throw new IllegalArgumentException("No such range: " + from + " to " + to);
        }
        return prefix[to + 1] - prefix[from];
    }

    public static long countStretchesSummingTo(int[] values, int k) {
        Map<Long, Integer> seen = new HashMap<>();
        seen.put(0L, 1);
        long running = 0;
        long count = 0;
        for (int value : values) {
            running += value;
            count += seen.getOrDefault(running - k, 0);
            seen.merge(running, 1, Integer::sum);
        }
        return count;
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

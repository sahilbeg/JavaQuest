package jq.m04.backtracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoadPlanner {

    public static List<List<String>> promoSets(List<String> promos) {
        List<List<String>> result = new ArrayList<>();
        subsets(promos, 0, new ArrayList<>(), result);
        return result;
    }

    private static void subsets(List<String> promos, int start, List<String> current, List<List<String>> result) {
        // TODO 1: record a COPY of current in result: result.add(new ArrayList<>(current)).
        // TODO 2: for each index i from start to the end: choose promos.get(i) (add it to current),
        //         explore with subsets(promos, i + 1, current, result),
        //         then un-choose it (remove the last item of current).
    }

    public static List<List<String>> visitOrders(List<String> stations) {
        List<List<String>> result = new ArrayList<>();
        orders(stations, new boolean[stations.size()], new ArrayList<>(), result);
        return result;
    }

    private static void orders(List<String> stations, boolean[] used, List<String> current, List<List<String>> result) {
        // TODO 3: when current holds every station, record a copy of it and return.
        // TODO 4: otherwise loop i from 0 over every station that is not used yet.
        //         Mark it used and add it, explore, then remove it and mark it unused again.
    }

    public static List<List<Integer>> loadsForCapacity(int[] bikeSlots, int capacity) {
        // TODO 5: sort a copy of bikeSlots (bikeSlots.clone()), so the caller's array keeps its order.
        int[] sorted = bikeSlots;
        List<List<Integer>> result = new ArrayList<>();
        loads(sorted, 0, capacity, new ArrayList<>(), result);
        return result;
    }

    private static void loads(int[] sorted, int start, int remaining, List<Integer> current, List<List<Integer>> result) {
        // TODO 6: when remaining is 0, record a copy of current and return.
        // TODO 7: loop i from start. Skip sorted[i] when i > start and it equals sorted[i - 1].
        //         Stop the loop (break) when sorted[i] > remaining.
        //         Choose sorted[i], explore with loads(sorted, i + 1, ...), then un-choose it.
    }

    public static List<List<Integer>> refillPlans(int[] crateSizes, int bikesNeeded) {
        int[] sorted = crateSizes.clone();
        Arrays.sort(sorted);
        List<List<Integer>> result = new ArrayList<>();
        plans(sorted, 0, bikesNeeded, new ArrayList<>(), result);
        return result;
    }

    private static void plans(int[] sorted, int start, int remaining, List<Integer> current, List<List<Integer>> result) {
        // TODO 8: like loads, but a crate size can be used again: explore with plans(sorted, i, ...),
        //         not i + 1. Crate sizes are all different, so there is nothing to skip.
    }

    public static void main(String[] args) {
        System.out.println("Promo sets: " + promoSets(List.of("WEEKEND", "STUDENT", "FIRSTRIDE")));
        System.out.println("Visit orders: " + visitOrders(List.of("Harbor", "Uptown", "Depot")));
        System.out.println("Loads for 7 slots: " + loadsForCapacity(new int[] {2, 3, 2, 5, 3}, 7));
        System.out.println("Refill plans for 8 bikes: " + refillPlans(new int[] {5, 2, 3}, 8));
    }
}

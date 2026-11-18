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
        result.add(new ArrayList<>(current));
        for (int i = start; i < promos.size(); i++) {
            current.add(promos.get(i));
            subsets(promos, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    public static List<List<String>> visitOrders(List<String> stations) {
        List<List<String>> result = new ArrayList<>();
        orders(stations, new boolean[stations.size()], new ArrayList<>(), result);
        return result;
    }

    private static void orders(List<String> stations, boolean[] used, List<String> current, List<List<String>> result) {
        if (current.size() == stations.size()) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = 0; i < stations.size(); i++) {
            if (used[i]) {
                continue;
            }
            used[i] = true;
            current.add(stations.get(i));
            orders(stations, used, current, result);
            current.remove(current.size() - 1);
            used[i] = false;
        }
    }

    public static List<List<Integer>> loadsForCapacity(int[] bikeSlots, int capacity) {
        int[] sorted = bikeSlots.clone();
        Arrays.sort(sorted);
        List<List<Integer>> result = new ArrayList<>();
        loads(sorted, 0, capacity, new ArrayList<>(), result);
        return result;
    }

    private static void loads(int[] sorted, int start, int remaining, List<Integer> current, List<List<Integer>> result) {
        if (remaining == 0) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < sorted.length; i++) {
            if (i > start && sorted[i] == sorted[i - 1]) {
                continue;
            }
            if (sorted[i] > remaining) {
                break;
            }
            current.add(sorted[i]);
            loads(sorted, i + 1, remaining - sorted[i], current, result);
            current.remove(current.size() - 1);
        }
    }

    public static List<List<Integer>> refillPlans(int[] crateSizes, int bikesNeeded) {
        int[] sorted = crateSizes.clone();
        Arrays.sort(sorted);
        List<List<Integer>> result = new ArrayList<>();
        plans(sorted, 0, bikesNeeded, new ArrayList<>(), result);
        return result;
    }

    private static void plans(int[] sorted, int start, int remaining, List<Integer> current, List<List<Integer>> result) {
        if (remaining == 0) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < sorted.length; i++) {
            if (sorted[i] > remaining) {
                break;
            }
            current.add(sorted[i]);
            plans(sorted, i, remaining - sorted[i], current, result);
            current.remove(current.size() - 1);
        }
    }

    public static void main(String[] args) {
        System.out.println("Promo sets: " + promoSets(List.of("WEEKEND", "STUDENT", "FIRSTRIDE")));
        System.out.println("Visit orders: " + visitOrders(List.of("Harbor", "Uptown", "Depot")));
        System.out.println("Loads for 7 slots: " + loadsForCapacity(new int[] {2, 3, 2, 5, 3}, 7));
        System.out.println("Refill plans for 8 bikes: " + refillPlans(new int[] {5, 2, 3}, 8));
    }
}

package jq.m04.searching;

public class Search {

    private static int steps;

    public static int lastSteps() {
        return steps;
    }

    public static int linear(int[] values, int target) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == target) {
                return i;
            }
        }
        return -1;
    }

    public static int binary(int[] sorted, int target) {
        steps = 0;
        int low = 0;
        int high = sorted.length - 1;
        while (low <= high) {
            steps++;
            int mid = low + (high - low) / 2;
            if (sorted[mid] == target) {
                return mid;
            } else if (sorted[mid] < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }

    public static int binaryRecursive(int[] sorted, int target) {
        return search(sorted, target, 0, sorted.length - 1);
    }

    private static int search(int[] sorted, int target, int low, int high) {
        if (low > high) {
            return -1;
        }
        int mid = low + (high - low) / 2;
        if (sorted[mid] == target) {
            return mid;
        } else if (sorted[mid] < target) {
            return search(sorted, target, mid + 1, high);
        } else {
            return search(sorted, target, low, mid - 1);
        }
    }

    public static int firstAtLeast(int[] sortedMinutes, int minutes) {
        steps = 0;
        int low = 0;
        int high = sortedMinutes.length;
        while (low < high) {
            steps++;
            int mid = low + (high - low) / 2;
            if (sortedMinutes[mid] >= minutes) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }
        return low;
    }
}

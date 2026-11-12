package jq.m04.searching;

public class Search {

    private static int steps;

    public static int lastSteps() {
        return steps;
    }

    public static int linear(int[] values, int target) {
        // TODO 1: check each item from index 0; return its index on a match, or -1 after the loop
        return -1;
    }

    public static int binary(int[] sorted, int target) {
        // TODO 2: set steps to 0, then loop while low <= high, adding 1 to steps on every pass
        // TODO 3: use mid = low + (high - low) / 2 and move low or high; return -1 if not found
        return -1;
    }

    public static int binaryRecursive(int[] sorted, int target) {
        // TODO 4: return search(sorted, target, 0, sorted.length - 1);
        return -1;
    }

    private static int search(int[] sorted, int target, int low, int high) {
        // TODO 5: return -1 when low > high; otherwise check mid and recurse into one half
        return -1;
    }

    public static int firstAtLeast(int[] sortedMinutes, int minutes) {
        // TODO 6: binary search for the first index whose value is >= minutes, or the length if none
        // TODO 7: set steps to 0 at the start and add 1 on every loop pass, like binary
        steps = 0;
        return -1;
    }
}

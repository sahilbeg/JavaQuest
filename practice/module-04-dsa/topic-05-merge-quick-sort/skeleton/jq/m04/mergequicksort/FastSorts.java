package jq.m04.mergequicksort;

import java.util.Arrays;

public class FastSorts {

    public static int[] merge(int[] left, int[] right) {
        // TODO 1: make a result array of length left.length + right.length
        // TODO 2: walk both arrays with two indexes; copy the smaller front item (use <= to keep ties in order)
        // TODO 3: copy whatever is left over in either array, then return the result
        return new int[0];
    }

    public static void mergeSort(int[] a) {
        // TODO 4: stop when a has fewer than 2 items
        // TODO 5: copy the two halves with Arrays.copyOfRange, mergeSort each half
        // TODO 6: merge the halves and copy the merged items back into a
    }

    /**
     * Lomuto partition of a[low..high], both ends included.
     * The pivot is a[high]. Afterwards every item left of the pivot is <= pivot,
     * and every item right of it is > pivot. Returns the pivot's final index.
     */
    public static int partition(int[] a, int low, int high) {
        // TODO 7: pivot = a[high]; i = low
        // TODO 8: for j from low to high - 1: if a[j] <= pivot, swap a[i] and a[j], then i++
        // TODO 9: swap a[i] and a[high], then return i
        return -1;
    }

    public static void quickSort(int[] a) {
        // TODO 10: call quickSort(a, 0, a.length - 1)
    }

    public static void quickSort(int[] a, int low, int high) {
        // TODO 11: stop when low >= high; otherwise partition, then quicksort both sides of the pivot
    }

    public static void main(String[] args) {
        int[] minutes = {42, 7, 19, 7, 30, 3};
        mergeSort(minutes);
        System.out.println(Arrays.toString(minutes));
        int[] more = {25, 12, 40, 12, 5};
        quickSort(more);
        System.out.println(Arrays.toString(more));
    }
}

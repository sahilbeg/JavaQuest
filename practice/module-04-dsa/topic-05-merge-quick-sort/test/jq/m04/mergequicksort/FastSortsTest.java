package jq.m04.mergequicksort;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FastSortsTest {

    @Test
    @DisplayName("merge joins two sorted arrays of different lengths")
    void mergeUnevenLengths() {
        int[] merged = FastSorts.merge(new int[] {3, 9, 20}, new int[] {1, 4, 9, 12, 30});
        assertArrayEquals(new int[] {1, 3, 4, 9, 9, 12, 20, 30}, merged,
                "merge([3, 9, 20], [1, 4, 9, 12, 30]) should give all 8 items in order. Got "
                        + Arrays.toString(merged) + ". Remember to copy the leftovers.");
        assertArrayEquals(new int[] {5, 6}, FastSorts.merge(new int[0], new int[] {5, 6}),
                "merge([], [5, 6]) should give [5, 6]: one side can be empty");
    }

    @Test
    @DisplayName("mergeSort sorts ride minutes that include duplicates")
    void mergeSortWithDuplicates() {
        int[] minutes = {42, 7, 19, 7, 30, 3, 19, 1};
        FastSorts.mergeSort(minutes);
        assertArrayEquals(new int[] {1, 3, 7, 7, 19, 19, 30, 42}, minutes,
                "mergeSort should sort the array in place, smallest first. Got " + Arrays.toString(minutes));
    }

    @Test
    @DisplayName("quickSort sorts ride minutes that include duplicates")
    void quickSortWithDuplicates() {
        int[] minutes = {25, 12, 40, 12, 5, 25, 60, 0};
        FastSorts.quickSort(minutes);
        assertArrayEquals(new int[] {0, 5, 12, 12, 25, 25, 40, 60}, minutes,
                "quickSort should sort the array in place, smallest first. Got " + Arrays.toString(minutes));
        int[] sorted = {1, 2, 3, 4, 5, 6};
        FastSorts.quickSort(sorted);
        assertArrayEquals(new int[] {1, 2, 3, 4, 5, 6}, sorted,
                "quickSort on an already sorted array should leave it sorted");
    }

    @Test
    @DisplayName("quickSort(a, low, high) sorts only the range from low to high")
    void quickSortSubRange() {
        int[] a = {99, 42, 7, 30, 7, 1, -5};
        FastSorts.quickSort(a, 1, 4);
        assertArrayEquals(new int[] {99, 7, 7, 30, 42, 1, -5}, a,
                "quickSort(a, low, high) must only touch indexes low..high. quickSort(a, 1, 4) should give "
                        + "[99, 7, 7, 30, 42, 1, -5]. Got " + Arrays.toString(a));
    }

    @Test
    @DisplayName("Both sorts handle an empty array and a single item")
    void emptyAndOne() {
        int[] empty = {};
        FastSorts.mergeSort(empty);
        FastSorts.quickSort(empty);
        assertEquals(0, empty.length, "Sorting an empty array should do nothing and not crash");
        int[] one = {17};
        FastSorts.mergeSort(one);
        FastSorts.quickSort(one);
        assertArrayEquals(new int[] {17}, one, "Sorting [17] should leave [17]");
    }

    @Test
    @DisplayName("partition puts smaller or equal items left of the pivot and bigger items right")
    void partitionProperty() {
        int[] a = {9, 30, 4, 18, 2, 18, 12};
        int p = FastSorts.partition(a, 0, a.length - 1);
        assertEquals(3, p,
                "partition with pivot 12 (the last item) should return index 3: three items (9, 4, 2) are <= 12");
        assertEquals(12, a[p], "The pivot 12 should sit at the index partition returns");
        for (int i = 0; i < p; i++) {
            assertTrue(a[i] <= 12, "Item " + a[i] + " at index " + i + " is left of the pivot, so it must be <= 12");
        }
        for (int i = p + 1; i < a.length; i++) {
            assertTrue(a[i] > 12, "Item " + a[i] + " at index " + i + " is right of the pivot, so it must be > 12");
        }
    }

    @Test
    @DisplayName("partition only touches the range from low to high")
    void partitionSubRange() {
        int[] a = {99, 7, 3, 5, 0};
        int p = FastSorts.partition(a, 1, 3);
        assertEquals(2, p, "partition(a, 1, 3) with pivot 5 should return 2: only 3 is <= 5 inside the range");
        assertArrayEquals(new int[] {99, 3, 5, 7, 0}, a,
                "Items outside low..high must not move. Got " + Arrays.toString(a));
    }
}

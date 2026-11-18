package jq.m10.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GroupBookingTest {

    @Test
    @DisplayName("The classic example gives 10 bike-hours, and one hour on its own is a block")
    void classicExample() {
        assertEquals(10, GroupBooking.mostBikeHours(new int[] {2, 1, 5, 6, 2, 3}),
                "Free bikes {2, 1, 5, 6, 2, 3}: 5 bikes in hours 2 and 3 give 5 x 2 = 10 bike-hours, the most");
        assertEquals(7, GroupBooking.mostBikeHours(new int[] {7}), "One hour with 7 free bikes gives 7 x 1 = 7");
    }

    @Test
    @DisplayName("No hours gives 0, and an hour with no free bikes splits the day")
    void emptyAndZeros() {
        assertEquals(0, GroupBooking.mostBikeHours(new int[0]), "No hours means no booking: 0 bike-hours");
        assertEquals(0, GroupBooking.mostBikeHours(new int[] {0, 0}), "No free bikes in any hour: 0 bike-hours");
        assertEquals(6, GroupBooking.mostBikeHours(new int[] {3, 3, 0, 5}),
                "{3, 3, 0, 5}: the 0 splits the day. 3 bikes for 2 hours (6) beats 5 bikes for 1 hour (5)");
    }

    @Test
    @DisplayName("Rising, falling and flat days all find the best block")
    void risingFallingFlat() {
        assertEquals(9, GroupBooking.mostBikeHours(new int[] {1, 2, 3, 4, 5}),
                "{1, 2, 3, 4, 5}: 3 bikes for the last 3 hours give 9, more than 4 x 2 = 8 or 5 x 1 = 5");
        assertEquals(9, GroupBooking.mostBikeHours(new int[] {5, 4, 3, 2, 1}),
                "{5, 4, 3, 2, 1}: 3 bikes for the first 3 hours give 9. Blocks that end at the last hour count too");
        assertEquals(12, GroupBooking.mostBikeHours(new int[] {4, 4, 4}),
                "{4, 4, 4}: 4 bikes for all 3 hours give 12. Equal neighbours belong to the same block");
    }

    @Test
    @DisplayName("Negative counts throw IllegalArgumentException, and big answers do not overflow")
    void negativesAndOverflow() {
        assertThrows(IllegalArgumentException.class, () -> GroupBooking.mostBikeHours(new int[] {3, -1, 4}),
                "A station cannot have -1 free bikes, so throw IllegalArgumentException");
        assertEquals(4_000_000_000L, GroupBooking.mostBikeHours(new int[] {2_000_000_000, 2_000_000_000}),
                "2,000,000,000 x 2 is 4,000,000,000, more than an int holds. Multiply as long: (long) bikes * hours");
    }

    @Test
    @DisplayName("200,000 hours finish in time")
    void longDaysAreFast() {
        int n = 200_000;
        int[] rising = new int[n];
        int[] flat = new int[n];
        for (int i = 0; i < n; i++) {
            rising[i] = i + 1;
            flat[i] = 50_000;
        }
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            assertEquals(10_000_100_000L, GroupBooking.mostBikeHours(rising),
                    "Free bikes 1, 2, ..., 200,000: the best block is 100,000 bikes for the last 100,001 hours");
            assertEquals(10_000_000_000L, GroupBooking.mostBikeHours(flat),
                    "50,000 free bikes for all 200,000 hours give 10,000,000,000 bike-hours");
        }, "200,000 hours should take one pass with a monotonic stack, O(n). "
                + "Trying every block, or growing a block from each hour, is O(n^2) and times out");
    }
}

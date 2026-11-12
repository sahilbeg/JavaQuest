package jq.m04.dp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class PassesTest {

    @Test
    @Timeout(value = 2, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    @DisplayName("fib gives the right small values and answers fib(80) fast")
    void fibonacci() {
        assertEquals(0L, Passes.fib(0), "fib(0) should be 0");
        assertEquals(1L, Passes.fib(1), "fib(1) should be 1");
        assertEquals(55L, Passes.fib(10), "fib(10) should be 55");
        assertEquals(23416728348467685L, Passes.fib(80),
                "fib(80) should be 23416728348467685. Plain recursion never finishes this; use a memo or a table of long");
    }

    @Test
    @DisplayName("A week pass beats five day passes")
    void weekPassWins() {
        assertEquals(10, Passes.cheapestCover(new int[] {1, 2, 3, 4, 5}, 3, 10, 25),
                "5 ride days in a row: one week pass ($10) is cheaper than 5 day passes ($15)");
        assertEquals(16, Passes.cheapestCover(new int[] {1, 4, 6, 7, 8, 20}, 3, 10, 25),
                "Days 1, 4, 6, 7, 8, 20 should cost $16: a week pass for days 1 to 7 or 4 to 10, plus day passes");
    }

    @Test
    @DisplayName("A month pass beats three week passes")
    void monthPassWins() {
        int[] days = new int[20];
        for (int i = 0; i < 20; i++) {
            days[i] = i + 1;
        }
        assertEquals(25, Passes.cheapestCover(days, 3, 10, 25),
                "20 ride days in a row: one month pass ($25) is cheaper than 3 week passes ($30)");
    }

    @Test
    @DisplayName("Small or empty months use day passes or nothing")
    void smallCases() {
        assertEquals(0, Passes.cheapestCover(new int[0], 3, 10, 25),
                "No ride days means no passes, so the cost is 0");
        assertEquals(6, Passes.cheapestCover(new int[] {3, 200}, 3, 10, 25),
                "Two lonely ride days should cost two day passes: 3 + 3 = 6");
    }

    @Test
    @Timeout(value = 2, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    @DisplayName("Riding every day of the year is solved fast")
    void wholeYear() {
        int[] days = new int[365];
        for (int i = 0; i < 365; i++) {
            days[i] = i + 1;
        }
        assertEquals(310, Passes.cheapestCover(days, 3, 10, 25),
                "365 ride days should cost $310: 12 month passes cover 360 days, then one week pass covers the last 5");
    }

    @Test
    @DisplayName("waysToDock counts the ways to fill slots that take 1 or 2 bikes")
    void docking() {
        assertEquals(1, Passes.waysToDock(0), "waysToDock(0) should be 1: there is one way to dock nothing");
        assertEquals(1, Passes.waysToDock(1), "waysToDock(1) should be 1: one single slot");
        assertEquals(2, Passes.waysToDock(2), "waysToDock(2) should be 2: 1+1 or 2");
        assertEquals(8, Passes.waysToDock(5), "waysToDock(5) should be 8: waysToDock(4) + waysToDock(3) = 5 + 3");
    }
}

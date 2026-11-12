package jq.m01.primitivetypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RideStatsTest {

    @Test
    @DisplayName("distanceKm() keeps the full 2.5 km of the ride")
    void distanceKeepsDecimal() {
        assertEquals(2.5, RideStats.distanceKm(), 0.001,
                "distanceKm() should return 2.5. Store it in a double so the .5 is not lost");
    }

    @Test
    @DisplayName("isMember() says the rider is a member")
    void riderIsMember() {
        assertTrue(RideStats.isMember(),
                "isMember() should return true, stored in a boolean variable");
    }

    @Test
    @DisplayName("roundedDownKm() cuts off the decimal part")
    void roundedDownCutsDecimal() {
        assertEquals(2, RideStats.roundedDownKm(2.5),
                "roundedDownKm(2.5) should be 2: cast km with (int)");
        assertEquals(3, RideStats.roundedDownKm(3.9),
                "roundedDownKm(3.9) should be 3: a cast to int cuts off .9 without rounding up");
    }

    @Test
    @DisplayName("averageMinutes() keeps the half minute")
    void averageKeepsHalf() {
        assertEquals(22.5, RideStats.averageMinutes(45, 2), 0.001,
                "averageMinutes(45, 2) should be 22.5. Cast totalMinutes to double before you divide");
    }

    @Test
    @DisplayName("averageMinutes() works for an even split too")
    void averageEvenSplit() {
        assertEquals(20.0, RideStats.averageMinutes(60, 3), 0.001,
                "averageMinutes(60, 3) should be 20.0: 60 minutes over 3 rides");
    }
}

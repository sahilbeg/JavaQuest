package jq.m02.records;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RideLogTest {

    @Test
    @DisplayName("A ride with negative minutes stores 0 minutes")
    void negativeMinutesBecomeZero() {
        Ride ride = new Ride("B-1042", "classic", -5);
        assertEquals(0, ride.minutes(),
                "new Ride(\"B-1042\", \"classic\", -5).minutes() should be 0. Set minutes = 0 in the compact constructor when it is negative");
        assertEquals(12, new Ride("B-1042", "classic", 12).minutes(),
                "A ride with 12 minutes should keep 12 minutes. Only change negative values");
    }

    @Test
    @DisplayName("fare() adds the unlock fee to the price per minute for each bike type")
    void fareByType() {
        assertEquals(2.5, new Ride("B-1042", "classic", 10).fare(), 0.001,
                "classic 10 minutes should be 2.5: unlock fee 1.00 plus 10 minutes at 0.15");
        assertEquals(4.0, new Ride("B-2001", "ebike", 10).fare(), 0.001,
                "ebike 10 minutes should be 4.0: unlock fee 1.00 plus 10 minutes at 0.30");
        assertEquals(3.5, new Ride("B-3300", "cargo", 10).fare(), 0.001,
                "cargo 10 minutes should be 3.5: unlock fee 1.00 plus 10 minutes at 0.25");
        assertEquals(1.0, new Ride("B-3300", "cargo", -8).fare(), 0.001,
                "cargo with -8 minutes should be 1.0: the minutes become 0, so only the unlock fee is left");
    }

    @Test
    @DisplayName("Two rides with the same values are equal")
    void equalRides() {
        Ride a = new Ride("B-1042", "classic", 12);
        Ride b = new Ride("B-1042", "classic", 12);
        assertEquals(a, b, "Two rides with the same bikeId, type and minutes should be equal. Keep Ride a record");
        assertEquals(a.hashCode(), b.hashCode(), "Equal rides should have the same hashCode");
    }

    @Test
    @DisplayName("totalMinutes and totalFare add up every ride")
    void totals() {
        Ride[] rides = {
            new Ride("B-1042", "classic", 10),
            new Ride("B-2001", "ebike", 20),
            new Ride("B-3300", "cargo", 4)
        };
        assertEquals(34, RideLog.totalMinutes(rides),
                "totalMinutes should be 34: 10 + 20 + 4");
        assertEquals(11.5, RideLog.totalFare(rides), 0.001,
                "totalFare should be 11.5: 2.5 (classic 10) + 7.0 (ebike 20) + 2.0 (cargo 4)");
        assertEquals(0, RideLog.totalMinutes(new Ride[0]), "totalMinutes of an empty array should be 0");
    }

    @Test
    @DisplayName("longest returns the ride with the most minutes, or null when there are none")
    void longest() {
        Ride shortRide = new Ride("B-1042", "classic", 10);
        Ride longRide = new Ride("B-2001", "ebike", 45);
        Ride midRide = new Ride("B-3300", "cargo", 20);
        assertSame(longRide, RideLog.longest(new Ride[] {shortRide, longRide, midRide}),
                "longest should return the 45-minute ride on B-2001");
        assertNull(RideLog.longest(new Ride[0]), "longest of an empty array should be null");
    }
}

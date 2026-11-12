package jq.m02.classesobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BikeTest {

    private static Bike bike(String id, String type) {
        Bike b = new Bike();
        b.id = id;
        b.type = type;
        return b;
    }

    @Test
    @DisplayName("addRide adds the minutes to the bike's total")
    void addRideAddsUp() {
        Bike b = bike("B-1042", "classic");
        b.addRide(20);
        b.addRide(25);
        assertEquals(45, b.minutesRidden,
                "After addRide(20) and addRide(25), minutesRidden should be 45. Add minutes to minutesRidden");
    }

    @Test
    @DisplayName("Two bikes keep their own minutes")
    void bikesAreIndependent() {
        Bike a = bike("B-1042", "classic");
        Bike c = bike("B-2001", "ebike");
        a.addRide(30);
        assertEquals(0, c.minutesRidden,
                "Riding bike B-1042 should not change bike B-2001. Use the field minutesRidden, not a static variable");
        assertEquals(30, a.minutesRidden, "Bike B-1042 should have 30 minutes after addRide(30)");
    }

    @Test
    @DisplayName("describe shows id, type and minutes ridden")
    void describeText() {
        Bike b = bike("B-1042", "classic");
        b.addRide(45);
        assertEquals("B-1042 (classic), 45 min ridden", b.describe(),
                "describe() should return \"B-1042 (classic), 45 min ridden\". Check the brackets, the comma and the spaces");
    }

    @Test
    @DisplayName("A classic bike costs the unlock fee plus 0.15 a minute")
    void classicFare() {
        assertEquals(2.5, bike("B-1042", "classic").fare(10), 0.001,
                "fare(10) on a classic bike should be 2.5: unlock fee 1.00 plus 10 minutes at 0.15");
    }

    @Test
    @DisplayName("E-bikes and cargo bikes use their own per-minute price")
    void otherFares() {
        assertEquals(4.0, bike("B-2001", "ebike").fare(10), 0.001,
                "fare(10) on an ebike should be 4.0: unlock fee 1.00 plus 10 minutes at 0.30");
        assertEquals(6.0, bike("B-3300", "cargo").fare(20), 0.001,
                "fare(20) on a cargo bike should be 6.0: unlock fee 1.00 plus 20 minutes at 0.25");
    }
}

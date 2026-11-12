package jq.m02.encapsulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EBikeTest {

    @Test
    @DisplayName("The constructor stores the id and clamps the starting battery")
    void constructorClamps() {
        EBike normal = new EBike("B-1042", 75);
        assertEquals("B-1042", normal.getId(), "getId() should return the id given to the constructor");
        assertEquals(75, normal.getBattery(), "new EBike(\"B-1042\", 75) should start at 75");
        assertEquals(100, new EBike("B-1043", 140).getBattery(),
                "new EBike(\"B-1043\", 140) should start at 100: call setBattery in the constructor");
        assertEquals(0, new EBike("B-1044", -10).getBattery(),
                "new EBike(\"B-1044\", -10) should start at 0: call setBattery in the constructor");
    }

    @Test
    @DisplayName("setBattery keeps the value between 0 and 100")
    void setBatteryClamps() {
        EBike bike = new EBike("B-1042", 50);
        bike.setBattery(60);
        assertEquals(60, bike.getBattery(), "setBattery(60) should store 60");
        bike.setBattery(140);
        assertEquals(100, bike.getBattery(), "setBattery(140) should store 100, the highest allowed value");
        bike.setBattery(-5);
        assertEquals(0, bike.getBattery(), "setBattery(-5) should store 0, the lowest allowed value");
    }

    @Test
    @DisplayName("charge adds percent but never goes above 100")
    void chargeStopsAtFull() {
        EBike bike = new EBike("B-1042", 50);
        bike.charge(30);
        assertEquals(80, bike.getBattery(), "50 plus charge(30) should be 80");
        bike.charge(50);
        assertEquals(100, bike.getBattery(), "80 plus charge(50) should stop at 100");
    }

    @Test
    @DisplayName("ride uses 1 percent per 2 whole minutes and never goes below 0")
    void rideDrains() {
        EBike bike = new EBike("B-1042", 50);
        bike.ride(20);
        assertEquals(40, bike.getBattery(), "50 minus ride(20) should be 40: 20 minutes use 10 percent");
        bike.ride(5);
        assertEquals(38, bike.getBattery(), "40 minus ride(5) should be 38: 5 / 2 is 2 whole percent");
        bike.ride(200);
        assertEquals(0, bike.getBattery(), "ride(200) uses 100 percent, so the battery should stop at 0");
    }

    @Test
    @DisplayName("canUnlock is true only when the battery is at least 20")
    void canUnlockAtTwenty() {
        assertTrue(new EBike("B-1042", 20).canUnlock(), "a bike at exactly 20% should unlock");
        assertTrue(new EBike("B-1043", 90).canUnlock(), "a bike at 90% should unlock");
        assertFalse(new EBike("B-1044", 19).canUnlock(), "a bike at 19% should not unlock");
    }
}

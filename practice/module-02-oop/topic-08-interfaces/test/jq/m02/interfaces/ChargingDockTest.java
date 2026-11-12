package jq.m02.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChargingDockTest {

    @Test
    @DisplayName("An e-bike and a scooter remember their starting battery")
    void startingBattery() {
        assertEquals(55, new EBike("B-1042", 55).batteryLevel(),
                "new EBike(\"B-1042\", 55).batteryLevel() should be 55");
        assertEquals(30, new Scooter("S-7", 30).batteryLevel(),
                "new Scooter(\"S-7\", 30).batteryLevel() should be 30");
    }

    @Test
    @DisplayName("Battery stays between 0 and 100")
    void batteryIsClamped() {
        assertEquals(100, new EBike("B-1042", 150).batteryLevel(),
                "An EBike made with 150 should store 100, the highest battery level");
        assertEquals(0, new Scooter("S-7", -5).batteryLevel(),
                "A Scooter made with -5 should store 0, the lowest battery level");
        EBike bike = new EBike("B-1042", 90);
        bike.charge(25);
        assertEquals(100, bike.batteryLevel(),
                "Charging 90 by 25 should stop at 100, never 115");
    }

    @Test
    @DisplayName("needsCharge is true only below 20 percent")
    void needsCharge() {
        assertTrue(new EBike("B-1042", 19).needsCharge(), "An EBike at 19 should need a charge");
        assertFalse(new EBike("B-1042", 20).needsCharge(), "An EBike at 20 should not need a charge");
        assertTrue(new Scooter("S-7", 5).needsCharge(), "A Scooter at 5 should need a charge");
    }

    @Test
    @DisplayName("chargeAll charges e-bikes and scooters in the same dock")
    void chargeAllMixed() {
        EBike bike = new EBike("B-1042", 10);
        Scooter scooter = new Scooter("S-7", 40);
        Chargeable[] dock = {bike, scooter};
        ChargingDock.chargeAll(dock, 30);
        assertEquals(40, bike.batteryLevel(), "The EBike should go from 10 to 40 after chargeAll(dock, 30)");
        assertEquals(70, scooter.batteryLevel(), "The Scooter should go from 40 to 70 after chargeAll(dock, 30)");
    }

    @Test
    @DisplayName("countNeedingCharge counts every item below 20 percent")
    void countNeedingCharge() {
        Chargeable[] dock = {
            new EBike("B-1042", 5),
            new Scooter("S-7", 15),
            new EBike("B-1043", 80),
            new Scooter("S-8", 20)
        };
        assertEquals(2, ChargingDock.countNeedingCharge(dock),
                "Only the items at 5 and 15 are below 20, so the count should be 2");
    }
}

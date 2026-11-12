package jq.m02.polymorphism;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FleetReportTest {

    private Bike[] fleet() {
        return new Bike[] {
            new Bike("B-1042"),
            new EBike("B-2001", 15),
            new CargoBike("B-3005"),
            new EBike("B-2002", 90),
            new EBike("B-2003", 5)
        };
    }

    @Test
    @DisplayName("totalFare adds each bike's own fare")
    void totalFare() {
        assertEquals(18.0, FleetReport.totalFare(fleet(), 10), 0.001,
                "totalFare(fleet, 10) should be 18.0: 2.5 (classic) + 4.0 + 3.5 (cargo) + 4.0 + 4.0 (three e-bikes). Call b.fare(minutes) on each bike");
        assertEquals(0.0, FleetReport.totalFare(new Bike[0], 10), 0.001,
                "totalFare of an empty array should be 0.0");
    }

    @Test
    @DisplayName("countEBikes counts only the e-bikes")
    void countEBikes() {
        assertEquals(3, FleetReport.countEBikes(fleet()),
                "countEBikes should be 3: B-2001, B-2002 and B-2003 are e-bikes. Use b instanceof EBike");
    }

    @Test
    @DisplayName("lowBatteryIds lists e-bikes below the threshold")
    void lowBatteryIds() {
        assertEquals("B-2001, B-2003", FleetReport.lowBatteryIds(fleet(), 20),
                "lowBatteryIds(fleet, 20) should be \"B-2001, B-2003\": batteries 15 and 5 are below 20, separated by \", \"");
        assertEquals("", FleetReport.lowBatteryIds(fleet(), 5),
                "lowBatteryIds(fleet, 5) should be \"\": no battery is below 5 (5 is not below 5)");
    }

    @Test
    @DisplayName("cheapest returns the bike with the lowest price per minute")
    void cheapest() {
        Bike[] bikes = { new EBike("B-2001", 50), new CargoBike("B-3005"), new Bike("B-1042") };
        assertSame(bikes[2], FleetReport.cheapest(bikes),
                "cheapest should return the classic bike B-1042, which costs 0.15 per minute");
        Bike[] noClassic = { new EBike("B-2001", 50), new CargoBike("B-3005") };
        assertSame(noClassic[1], FleetReport.cheapest(noClassic),
                "cheapest should return the cargo bike B-3005: 0.25 is lower than the e-bike's 0.30");
    }

    @Test
    @DisplayName("cheapest returns null for an empty array")
    void cheapestEmpty() {
        assertNull(FleetReport.cheapest(new Bike[0]),
                "cheapest of an empty array should be null, because there is no bike to return");
    }
}

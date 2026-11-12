package jq.m02.constructors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BikeTest {

    @Test
    @DisplayName("Bike(id, type) stores both values")
    void fullBikeConstructor() {
        Bike bike = new Bike("B-1042", "ebike");
        assertEquals("B-1042", bike.id, "new Bike(\"B-1042\", \"ebike\") should set id to B-1042. Use this.id = id;");
        assertEquals("ebike", bike.type, "new Bike(\"B-1042\", \"ebike\") should set type to ebike. Use this.type = type;");
    }

    @Test
    @DisplayName("Bike(id) makes a classic bike with that id")
    void shortBikeConstructor() {
        Bike bike = new Bike("B-2001");
        assertEquals("B-2001", bike.id, "new Bike(\"B-2001\") should set id to B-2001. Call this(id, \"classic\");");
        assertEquals("classic", bike.type, "new Bike(\"B-2001\") should give type classic. Call this(id, \"classic\");");
    }

    @Test
    @DisplayName("Station(name) gets the name and 12 docks")
    void shortStationConstructor() {
        Station s = new Station("Riverside");
        assertEquals("Riverside", s.name, "new Station(\"Riverside\") should set name to Riverside");
        assertEquals(12, s.docks, "new Station(\"Riverside\") should have 12 docks, like every Spoke kiosk");
    }

    @Test
    @DisplayName("Station(name, docks) stores both values")
    void fullStationConstructor() {
        Station s = new Station("Depot", 20);
        assertEquals("Depot", s.name, "new Station(\"Depot\", 20) should set name to Depot");
        assertEquals(20, s.docks, "new Station(\"Depot\", 20) should have 20 docks");
    }

    @Test
    @DisplayName("freeDocks counts the empty docks")
    void freeDocksCount() {
        assertEquals(3, new Station("Harbor").freeDocks(9),
                "Harbor has 12 docks and 9 bikes, so freeDocks(9) should be 3");
        assertEquals(14, new Station("Depot", 20).freeDocks(6),
                "Depot has 20 docks and 6 bikes, so freeDocks(6) should be 14");
    }
}

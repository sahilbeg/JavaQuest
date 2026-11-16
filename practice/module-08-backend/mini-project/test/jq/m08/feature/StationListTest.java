package jq.m08.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

class StationListTest extends StationApiTestBase {

    @Test
    @DisplayName("GET /api/v1/stations lists all five stations, sorted by name")
    void sortedByName() throws Exception {
        MvcResult r = ok("/api/v1/stations");
        assertEquals(List.of("Depot", "Harbor", "Old Mill", "Riverside", "Uptown"), names(r),
                "the list should hold every station, sorted A to Z by name, not in the order they were saved");
    }

    @Test
    @DisplayName("Each station in the list has exactly the six fields of the design")
    void sixFields() throws Exception {
        MvcResult r = ok("/api/v1/stations");
        @SuppressWarnings("unchecked")
        Map<String, Object> first = (Map<String, Object>) json(r, "$[0]");
        assertEquals(Set.of("id", "name", "docks", "bikesAvailable", "ebikesAvailable", "docksFree"), first.keySet(),
                "each station should have exactly these JSON fields, spelled the same way");
    }

    @Test
    @DisplayName("Riverside counts bikes of any type, e-bikes alone, and free docks")
    void riversideCounts() throws Exception {
        MvcResult r = ok("/api/v1/stations");
        String riverside = "$[?(@.id == 'riverside')]";
        assertEquals(List.of("Riverside"), json(r, riverside + ".name"), "riverside should be in the list with the name Riverside");
        assertEquals(List.of(12), json(r, riverside + ".docks"), "Riverside has 12 docks");
        assertEquals(List.of(4), json(r, riverside + ".bikesAvailable"),
                "Riverside has 4 AVAILABLE bikes (2 CLASSIC, 1 EBIKE, 1 CARGO). The e-bike in MAINTENANCE does not count");
        assertEquals(List.of(1), json(r, riverside + ".ebikesAvailable"),
                "Riverside has 1 AVAILABLE EBIKE. The other e-bike is in MAINTENANCE");
        assertEquals(List.of(7), json(r, riverside + ".docksFree"),
                "Riverside has 12 docks and 5 docked bikes (4 AVAILABLE + 1 MAINTENANCE), so 7 docks are free");
    }

    @Test
    @DisplayName("A bike in maintenance still takes a dock, but is not available")
    void maintenanceTakesADock() throws Exception {
        MvcResult r = ok("/api/v1/stations");
        String uptown = "$[?(@.id == 'uptown')]";
        assertEquals(List.of(0), json(r, uptown + ".bikesAvailable"), "Uptown's only bike is in MAINTENANCE, so 0 bikes are available");
        assertEquals(List.of(11), json(r, uptown + ".docksFree"),
                "the MAINTENANCE bike at Uptown still sits in a dock, so 12 - 1 = 11 docks are free");
    }

    @Test
    @DisplayName("An empty station has every dock free")
    void emptyStation() throws Exception {
        MvcResult r = ok("/api/v1/stations");
        String depot = "$[?(@.id == 'depot')]";
        assertEquals(List.of(0), json(r, depot + ".bikesAvailable"), "Depot has no bikes, so 0 are available");
        assertEquals(List.of(0), json(r, depot + ".ebikesAvailable"), "Depot has no bikes, so 0 e-bikes are available");
        assertEquals(List.of(12), json(r, depot + ".docksFree"),
                "Depot has no bikes docked, so all 12 docks are free. Rented bikes have no station, so they never count");
    }

    @Test
    @DisplayName("The numbers are live: renting a bike changes them on the next request")
    void liveNumbers() throws Exception {
        ok("/api/v1/stations");
        Bike bike = bikes.findById("B-1003").orElseThrow();
        bike.rent();
        bikes.save(bike);

        MvcResult r = ok("/api/v1/stations");
        String riverside = "$[?(@.id == 'riverside')]";
        assertEquals(List.of(3), json(r, riverside + ".bikesAvailable"),
                "after B-1003 is rented, Riverside has 3 AVAILABLE bikes. Count in the database on every request");
        assertEquals(List.of(0), json(r, riverside + ".ebikesAvailable"), "B-1003 was Riverside's only AVAILABLE e-bike");
        assertEquals(List.of(8), json(r, riverside + ".docksFree"),
                "a rented bike leaves its dock, so Riverside now has 8 free docks");
    }
}

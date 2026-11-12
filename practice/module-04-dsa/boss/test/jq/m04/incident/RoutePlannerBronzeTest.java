package jq.m04.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoutePlannerBronzeTest {

    @Test
    @DisplayName("Bronze: full stations are skipped")
    void skipsFullStations() {
        RoutePlanner planner = new RoutePlanner(Map.of("Harbor", 0, "Uptown", 0, "Riverside", 0, "Old Mill", 4));
        planner.addLane("Harbor", "Uptown");
        planner.addLane("Uptown", "Riverside");
        planner.addLane("Riverside", "Old Mill");
        assertEquals(Optional.of("Old Mill"), planner.nearestFree("Harbor"),
                "Harbor, Uptown and Riverside have 0 free docks, so the nearest free station is Old Mill");
        assertEquals(Optional.of("Old Mill"), planner.nearestFree("Old Mill"),
                "a rider already at a station with free docks stays there");
    }

    @Test
    @DisplayName("Bronze: no free station anywhere gives an empty result")
    void nothingFree() {
        RoutePlanner planner = new RoutePlanner(Map.of("Harbor", 0, "Uptown", 0));
        planner.addLane("Harbor", "Uptown");
        assertEquals(Optional.empty(), planner.nearestFree("Harbor"), "every station is full");
    }
}

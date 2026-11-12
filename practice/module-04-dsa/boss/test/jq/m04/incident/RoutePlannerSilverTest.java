package jq.m04.incident;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoutePlannerSilverTest {

    @Test
    @DisplayName("Silver: a station without dock data counts as full")
    void missingStationIsFull() {
        RoutePlanner planner = new RoutePlanner(Map.of("Uptown", 0, "Harbor", 2));
        planner.addLane("Depot", "Uptown");
        planner.addLane("Uptown", "Harbor");
        Optional<String> result = assertDoesNotThrow(() -> planner.nearestFree("Depot"),
                "Depot is not in the free-docks map yet; treat it as 0 free docks with getOrDefault");
        assertEquals(Optional.of("Harbor"), result, "Depot and Uptown have no free docks, Harbor has 2");
    }
}

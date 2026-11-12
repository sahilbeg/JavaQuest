package jq.m04.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoutePlannerGoldTest {

    @Test
    @DisplayName("Gold: among equally near stations, the most free docks wins")
    void mostFreeDocks() {
        RoutePlanner planner = new RoutePlanner(Map.of("Harbor", 0, "Uptown", 2, "Riverside", 6, "Old Mill", 9));
        planner.addLane("Harbor", "Uptown");
        planner.addLane("Harbor", "Riverside");
        planner.addLane("Riverside", "Old Mill");
        assertEquals(Optional.of("Riverside"), planner.nearestFree("Harbor"),
                "Uptown and Riverside are both one lane away; Riverside has 6 free docks, Uptown 2. Old Mill is further away");
    }

    @Test
    @DisplayName("Gold: equal free docks go to the name that comes first")
    void alphabeticalTie() {
        RoutePlanner planner = new RoutePlanner(Map.of("Harbor", 0, "Uptown", 3, "Riverside", 3));
        planner.addLane("Harbor", "Uptown");
        planner.addLane("Harbor", "Riverside");
        assertEquals(Optional.of("Riverside"), planner.nearestFree("Harbor"),
                "Uptown and Riverside both have 3 free docks; Riverside comes first alphabetically");
    }
}

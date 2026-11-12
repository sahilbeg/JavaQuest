package jq.m04.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TruckPlannerTest {

    private static final List<Station> MORNING = List.of(
            new Station("Riverside", 12, 6),
            new Station("Harbor", 2, 9),
            new Station("Uptown", 10, 6),
            new Station("Old Mill", 5, 8));

    @Test
    @DisplayName("surplus is bikes minus target")
    void surplus() {
        assertEquals(6, new Station("Riverside", 12, 6).surplus(), "12 bikes, target 6");
        assertEquals(-7, new Station("Harbor", 2, 9).surplus(), "2 bikes, target 9: 7 needed");
    }

    @Test
    @DisplayName("the plan matches the KB page")
    void morningPlan() {
        assertEquals(List.of(
                new Trip("Riverside", "Harbor", 5),
                new Trip("Uptown", "Old Mill", 3),
                new Trip("Riverside", "Harbor", 1),
                new Trip("Uptown", "Harbor", 1)), TruckPlanner.plan(MORNING, 5),
                "largest giver to largest taker, at most 5 bikes per trip, ties by name");
    }

    @Test
    @DisplayName("planning stops when nobody has bikes to give")
    void notEnoughBikes() {
        List<Station> stations = List.of(
                new Station("Harbor", 0, 6),
                new Station("Uptown", 12, 10),
                new Station("Depot", 4, 4));
        assertEquals(List.of(new Trip("Uptown", "Harbor", 2)), TruckPlanner.plan(stations, 5),
                "Uptown can spare only 2; Depot is balanced and takes no part");
    }

    @Test
    @DisplayName("the report lists trips and a total")
    void report() {
        String expected = """
                Riverside -> Harbor: 5 bikes
                Uptown -> Old Mill: 3 bikes
                Riverside -> Harbor: 1 bike
                Uptown -> Harbor: 1 bike
                Moved 10 bikes in 4 trips""";
        assertEquals(expected, TruckPlanner.report(TruckPlanner.plan(MORNING, 5)),
                "compare with the report in the KB page, line by line; one bike is \"1 bike\"");
        assertEquals("Moved 0 bikes in 0 trips", TruckPlanner.report(List.of()), "no trips");
    }
}

package jq.m03.lambdas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RideSortsTest {

    private static List<Ride> sample() {
        List<Ride> rides = new ArrayList<>();
        rides.add(new Ride("b-1042", "Riverside", "ebike", 25));
        rides.add(new Ride("B-2001", "Harbor", "classic", 8));
        rides.add(new Ride("b-3300", "Uptown", "cargo", 40));
        rides.add(new Ride("B-1500", "Harbor", "ebike", 3));
        rides.add(new Ride("B-0777", "Old Mill", "classic", 12));
        return rides;
    }

    private static List<String> ids(List<Ride> rides) {
        List<String> out = new ArrayList<>();
        for (Ride r : rides) {
            out.add(r.bikeId());
        }
        return out;
    }

    @Test
    @DisplayName("byMinutes returns the rides from shortest to longest")
    void byMinutes() {
        List<Ride> rides = sample();
        List<Ride> sorted = RideSorts.byMinutes(rides);
        assertEquals(List.of("B-1500", "B-2001", "B-0777", "b-1042", "b-3300"), ids(sorted),
                "byMinutes should order the rides 3, 8, 12, 25, 40 minutes. Sort the copy and return it");
    }

    @Test
    @DisplayName("byMinutes leaves the original list in its old order")
    void byMinutesKeepsOriginal() {
        List<Ride> rides = sample();
        RideSorts.byMinutes(rides);
        assertEquals(ids(sample()), ids(rides),
                "byMinutes should sort a new list. The list you were given must keep its old order");
    }

    @Test
    @DisplayName("byStationThenMinutes sorts by station name, then by minutes")
    void byStationThenMinutes() {
        List<Ride> sorted = RideSorts.byStationThenMinutes(sample());
        assertEquals(List.of("B-1500", "B-2001", "B-0777", "b-1042", "b-3300"), ids(sorted),
                "Expected Harbor (3 min), Harbor (8 min), Old Mill, Riverside, Uptown. Use comparing(Ride::station).thenComparing(Ride::minutes)");
    }

    @Test
    @DisplayName("removeShort removes rides shorter than the limit from the list")
    void removeShort() {
        List<Ride> rides = sample();
        RideSorts.removeShort(rides, 10);
        assertEquals(List.of("b-1042", "b-3300", "B-0777"), ids(rides),
                "removeShort(rides, 10) should remove the 8 and 3 minute rides and keep the rest in order");
        RideSorts.removeShort(rides, 12);
        assertEquals(3, rides.size(),
                "removeShort(rides, 12) should keep a 12 minute ride: only rides with fewer minutes than the limit go");
    }

    @Test
    @DisplayName("upperIds lists every bike id in upper case, in list order")
    void upperIds() {
        assertEquals(List.of("B-1042", "B-2001", "B-3300", "B-1500", "B-0777"), RideSorts.upperIds(sample()),
                "upperIds should turn b-1042 into B-1042 and keep the rides in the same order");
    }
}

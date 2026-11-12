package jq.m03.comparatorsiterators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RideSorterTest {

    private static final Ride UPTOWN_25 = new Ride("B-1042", "Uptown", "ebike", 25);
    private static final Ride HARBOR_95 = new Ride("B-2001", "Harbor", "classic", 95);
    private static final Ride HARBOR_12 = new Ride("B-3300", "Harbor", "cargo", 12);
    private static final Ride RIVERSIDE_40 = new Ride("B-4100", "Riverside", "classic", 40);

    private static List<Ride> report() {
        return List.of(UPTOWN_25, HARBOR_95, HARBOR_12, RIVERSIDE_40);
    }

    private static List<String> ids(List<Ride> rides) {
        List<String> ids = new ArrayList<>();
        for (Ride r : rides) {
            ids.add(r.bikeId());
        }
        return ids;
    }

    @Test
    @DisplayName("Ride compares by minutes as its natural order")
    void naturalOrder() {
        assertTrue(HARBOR_12.compareTo(UPTOWN_25) < 0,
                "a 12-minute ride compared with a 25-minute ride should give a negative number");
        assertTrue(HARBOR_95.compareTo(UPTOWN_25) > 0,
                "a 95-minute ride compared with a 25-minute ride should give a positive number");
    }

    @Test
    @DisplayName("sortByMinutes returns a new list from shortest to longest")
    void sortsByMinutes() {
        List<Ride> original = report();
        assertEquals(List.of("B-3300", "B-1042", "B-4100", "B-2001"), ids(RideSorter.sortByMinutes(original)),
                "sortByMinutes should order the rides 12, 25, 40, 95 minutes");
    }

    @Test
    @DisplayName("sortByStationThenMinutes groups by station, then sorts each group by minutes")
    void sortsByStationThenMinutes() {
        assertEquals(List.of("B-3300", "B-2001", "B-4100", "B-1042"), ids(RideSorter.sortByStationThenMinutes(report())),
                "expected Harbor 12, Harbor 95, Riverside 40, Uptown 25: compare stations first, minutes only on a tie");
    }

    @Test
    @DisplayName("longestFirst puts the longest ride at the top")
    void longestFirst() {
        assertEquals(List.of("B-2001", "B-4100", "B-1042", "B-3300"), ids(RideSorter.longestFirst(report())),
                "longestFirst should order the rides 95, 40, 25, 12 minutes: use reversed() on a minutes comparator");
    }

    @Test
    @DisplayName("removeAbove deletes rides over the limit and counts them")
    void removesAbove() {
        List<Ride> rides = new ArrayList<>(report());
        int removed = RideSorter.removeAbove(rides, 30);
        assertEquals(2, removed, "removeAbove(rides, 30) should remove 2 rides: the 95 and 40 minute ones");
        assertEquals(List.of("B-1042", "B-3300"), ids(rides),
                "after removeAbove(rides, 30) only the 25 and 12 minute rides should stay, in their old order");
    }
}

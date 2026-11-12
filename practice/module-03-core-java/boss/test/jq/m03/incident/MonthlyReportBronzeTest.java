package jq.m03.incident;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MonthlyReportBronzeTest {

    @Test
    @DisplayName("Bronze: test rides are removed without crashing")
    void removesTestRides() {
        List<Ride> rides = new ArrayList<>(List.of(
                new Ride("TEST-1", "Harbor", "classic", 5),
                new Ride("B-1042", "Harbor", "ebike", 20),
                new Ride("TEST-2", "Uptown", "cargo", 3),
                new Ride("TEST-3", "Uptown", "cargo", 4),
                new Ride("B-2001", "Riverside", "classic", 25)));
        int removed = assertDoesNotThrow(() -> MonthlyReport.removeTestRides(rides),
                "removeTestRides must not remove from the list inside its own for-each loop: use removeIf or an Iterator");
        assertEquals(3, removed, "three rides have ids that start with TEST-");
        assertEquals(List.of("B-1042", "B-2001"), rides.stream().map(Ride::bikeId).toList(),
                "only the real rides should be left, in their original order");
    }
}

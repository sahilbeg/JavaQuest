package jq.m03.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MonthlyReportGoldTest {

    private static final List<Ride> RIDES = List.of(
            new Ride("B-1", "Riverside", "classic", 30),
            new Ride("B-2", "Harbor", "ebike", 50),
            new Ride("B-3", "Uptown", "cargo", 10),
            new Ride("B-4", "Old Mill", "classic", 30),
            new Ride("B-5", "Uptown", "classic", 10));

    @Test
    @DisplayName("Gold: the busiest stations come first")
    void busiestFirst() {
        assertEquals(List.of("Harbor", "Old Mill", "Riverside"), MonthlyReport.topStations(RIDES, 3),
                "Harbor has 50 minutes; Old Mill and Riverside tie on 30, so alphabetical order decides");
    }

    @Test
    @DisplayName("Gold: asking for more stations than exist returns them all")
    void allStations() {
        assertEquals(List.of("Harbor", "Old Mill", "Riverside", "Uptown"), MonthlyReport.topStations(RIDES, 10),
                "Uptown has 20 minutes, the fewest, so it comes last");
    }
}

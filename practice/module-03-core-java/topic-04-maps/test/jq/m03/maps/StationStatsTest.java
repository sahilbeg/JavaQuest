package jq.m03.maps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StationStatsTest {

    private static final List<Ride> RIDES = List.of(
            new Ride("B-1042", "Harbor", "ebike", 25),
            new Ride("B-2001", "Uptown", "classic", 12),
            new Ride("B-1042", "Harbor", "ebike", 20),
            new Ride("B-3300", "Riverside", "cargo", 30),
            new Ride("B-7777", "Old Mill", "ebike", 8));

    @Test
    @DisplayName("minutesPerStation adds up the minutes for each station")
    void minutesPerStationTotals() {
        TreeMap<String, Integer> totals = StationStats.minutesPerStation(RIDES);
        assertEquals(Integer.valueOf(45), totals.get("Harbor"),
                "Harbor should total 45 minutes: 25 + 20 from two rides. Add to the old total instead of replacing it");
        assertEquals(Map.of("Harbor", 45, "Uptown", 12, "Riverside", 30, "Old Mill", 8), totals,
                "minutesPerStation should hold all four stations: Harbor 45, Old Mill 8, Riverside 30, Uptown 12");
    }

    @Test
    @DisplayName("ridesPerType counts how many rides used each bike type")
    void ridesPerTypeCounts() {
        assertEquals(Map.of("ebike", 3, "classic", 1, "cargo", 1), StationStats.ridesPerType(RIDES),
                "ridesPerType should be ebike 3, classic 1, cargo 1. Add 1 per ride with getOrDefault(type, 0) + 1");
    }

    @Test
    @DisplayName("busiestStation returns the station with the most minutes")
    void busiestIsHarbor() {
        assertEquals("Harbor", StationStats.busiestStation(RIDES),
                "busiestStation should be Harbor with 45 minutes, more than Riverside's 30");
    }

    @Test
    @DisplayName("busiestStation breaks a tie in alphabetical order and returns null for no rides")
    void busiestTieAndEmpty() {
        List<Ride> tie = List.of(
                new Ride("B-1", "Uptown", "classic", 30),
                new Ride("B-2", "Riverside", "classic", 30));
        assertEquals("Riverside", StationStats.busiestStation(tie),
                "Riverside and Uptown both have 30 minutes, so Riverside wins because it comes first alphabetically");
        assertNull(StationStats.busiestStation(List.of()),
                "busiestStation should return null when there are no rides");
    }

    @Test
    @DisplayName("report lists every station alphabetically, one line each")
    void reportLines() {
        String expected = "Harbor: 45 min\nOld Mill: 8 min\nRiverside: 30 min\nUptown: 12 min";
        assertEquals(expected, StationStats.report(RIDES),
                "report should have one line \"Station: N min\" per station, sorted, joined with \\n and no extra line at the end");
    }
}

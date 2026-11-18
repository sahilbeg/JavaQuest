package jq.m09.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RideHistoryReportTest {

    private static final List<Rider> RIDERS = List.of(
            new Rider("rider-7", "Ada"), new Rider("rider-8", "Ben"), new Rider("rider-9", "Cleo"));

    @Test
    @DisplayName("build writes one line per ride and a total line, exactly like the old code")
    void smallReport() {
        List<Ride> rides = List.of(
                new Ride("rental-17", "rider-7", 12, 460),
                new Ride("rental-18", "rider-9", 3, 145),
                new Ride("rental-19", "rider-7", 10, 350));
        String expected = """
                rental-17 Ada 12 min 460 cents
                rental-18 Cleo 3 min 145 cents
                rental-19 Ada 10 min 350 cents
                total 3 rides 955 cents
                """;
        assertEquals(expected, RideHistoryReport.build(rides, RIDERS),
                "each line is: rentalId, name, minutes \"min\", fare \"cents\", then a new line. The last line is the total");
    }

    @Test
    @DisplayName("A rider who is not in the list shows as \"unknown rider\", and no rides gives only the total line")
    void unknownRiderAndNoRides() {
        List<Ride> rides = List.of(new Ride("rental-20", "rider-404", 5, 175));
        assertEquals("rental-20 unknown rider 5 min 175 cents\ntotal 1 rides 175 cents\n", RideHistoryReport.build(rides, RIDERS),
                "rider-404 is not in the riders list, so the line should say \"unknown rider\", like the old code");
        assertEquals("total 0 rides 0 cents\n", RideHistoryReport.build(List.of(), RIDERS),
                "with no rides, the report is just the total line");
    }

    @Test
    @DisplayName("build reads each rider once, not once per ride")
    void readsTheRiderListOnce() {
        List<Rider> riders = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            riders.add(new Rider("rider-" + i, "R" + i));
        }
        List<Ride> rides = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            rides.add(new Ride("rental-" + i, "rider-" + (i * 31 % 100), 10, 250));
        }
        WatchedList<Rider> watched = new WatchedList<>(riders);
        String report = RideHistoryReport.build(rides, watched);

        assertTrue(report.endsWith("total 200 rides 50000 cents\n"),
                "the report should end with the total line \"total 200 rides 50000 cents\"");
        assertTrue(watched.reads <= 2L * riders.size(),
                "build read a rider " + watched.reads + " times for 100 riders and 200 rides. Scanning the list for every ride"
                        + " is O(rides x riders). Put the riders in a HashMap once, then each lookup is O(1)");
    }

    @Test
    @DisplayName("A report of 150,000 rides and 50,000 riders is built in well under 3 seconds")
    void bigReportIsFast() {
        List<Rider> riders = new ArrayList<>();
        for (int i = 0; i < 50_000; i++) {
            riders.add(new Rider("rider-" + i, "Rider-" + i));
        }
        List<Ride> rides = new ArrayList<>();
        long expectedTotal = 0;
        for (int i = 0; i < 150_000; i++) {
            int minutes = 1 + i % 30;
            long fare = 100 + 15L * minutes;
            rides.add(new Ride("rental-" + i, "rider-" + (i * 7919L % 50_000), minutes, fare));
            expectedTotal += fare;
        }
        WatchedList<Ride> watchedRides = new WatchedList<>(rides);
        WatchedList<Rider> watchedRiders = new WatchedList<>(riders);

        String report = assertTimeoutPreemptively(Duration.ofSeconds(3),
                () -> RideHistoryReport.build(watchedRides, watchedRiders),
                "the report took more than 3 seconds. Look up riders in a HashMap, not by scanning the list,"
                        + " and append to one StringBuilder instead of using += on a String");

        String[] lines = report.split("\n");
        assertEquals(150_001, lines.length, "150,000 ride lines plus one total line");
        assertEquals("rental-1 Rider-7919 2 min 130 cents", lines[1], "ride 1 belongs to rider-7919, whose name is Rider-7919");
        assertEquals("total 150000 rides " + expectedTotal + " cents", lines[150_000], "the last line is the total of every fare");
    }
}

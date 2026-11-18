package jq.m04.greedyintervals;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DockSchedulerTest {

    private static Interval w(int start, int end) {
        return new Interval(start, end);
    }

    private static List<Interval> listOf(Interval... windows) {
        return new ArrayList<>(List.of(windows));
    }

    @Test
    @DisplayName("Windows overlap when each starts before the other ends, and touching windows do not")
    void overlaps() {
        assertTrue(w(600, 660).overlaps(w(630, 700)), "600-660 and 630-700 share minutes 630 to 659, so they overlap");
        assertTrue(w(630, 700).overlaps(w(600, 660)), "Overlap works both ways: 630-700 overlaps 600-660 too");
        assertTrue(w(600, 720).overlaps(w(630, 650)), "630-650 sits inside 600-720, so they overlap");
        assertFalse(w(600, 660).overlaps(w(660, 720)),
                "600-660 ends just before minute 660, so it does NOT overlap 660-720. Windows are half-open");
        assertFalse(w(800, 860).overlaps(w(600, 660)), "800-860 and 600-660 are far apart, so they do not overlap");
    }

    @Test
    @DisplayName("Overlapping bookings merge into busy periods, sorted by start")
    void mergeOverlaps() {
        List<Interval> bookings = listOf(w(900, 960), w(600, 660), w(630, 700), w(800, 850), w(840, 870));
        assertEquals(List.of(w(600, 700), w(800, 870), w(900, 960)), DockScheduler.mergeBusyPeriods(bookings),
                "Sort by start first: 600-660 and 630-700 merge into 600-700, 800-850 and 840-870 into 800-870,"
                        + " and 900-960 stays alone");
    }

    @Test
    @DisplayName("A short booking inside a long one does not cut the busy period short")
    void mergeKeepsLaterEnd() {
        List<Interval> bookings = listOf(w(600, 720), w(630, 650), w(700, 710));
        assertEquals(List.of(w(600, 720)), DockScheduler.mergeBusyPeriods(bookings),
                "630-650 and 700-710 sit inside 600-720, so the busy period is still 600-720."
                        + " Keep the LATER end, with Math.max");
    }

    @Test
    @DisplayName("Back-to-back bookings stay separate, and empty or single lists are handled")
    void mergeEdges() {
        assertEquals(List.of(w(600, 660), w(660, 720)), DockScheduler.mergeBusyPeriods(listOf(w(660, 720), w(600, 660))),
                "600-660 and 660-720 only touch at minute 660, so they do not overlap and stay as two windows");
        assertEquals(List.of(w(700, 730)), DockScheduler.mergeBusyPeriods(listOf(w(700, 730))),
                "One booking is one busy period");
        assertEquals(List.of(), DockScheduler.mergeBusyPeriods(listOf()), "No bookings give no busy periods");
    }

    @Test
    @DisplayName("mergeBusyPeriods leaves the list it was given alone, even an unmodifiable one")
    void mergeDoesNotChangeInput() {
        List<Interval> bookings = listOf(w(800, 850), w(600, 660), w(630, 700));
        assertEquals(List.of(w(600, 700), w(800, 850)), DockScheduler.mergeBusyPeriods(bookings),
                "800-850, 600-660 and 630-700 should merge into 600-700 and 800-850");
        assertEquals(List.of(w(800, 850), w(600, 660), w(630, 700)), bookings,
                "The list you were given must keep its order. Sort a copy: new ArrayList<>(bookings)");
        List<Interval> fixed = List.of(w(800, 850), w(600, 660));
        assertDoesNotThrow(() -> DockScheduler.mergeBusyPeriods(fixed),
                "List.of(...) cannot be sorted in place. Copy it into a new ArrayList before sorting");
    }

    @Test
    @DisplayName("mostBookings sorts by end time and beats 'earliest start' and 'shortest first'")
    void mostBookings() {
        assertEquals(3, DockScheduler.mostBookings(listOf(w(600, 900), w(610, 620), w(630, 640), w(650, 660))),
                "Take 610-620, 630-640 and 650-660 for 3 bookings. Picking the earliest start, 600-900, blocks all the others");
        assertEquals(2, DockScheduler.mostBookings(listOf(w(690, 720), w(600, 700), w(710, 800))),
                "Take 600-700 and 710-800 for 2 bookings. Picking the shortest, 690-720, blocks both others");
    }

    @Test
    @DisplayName("mostBookings lets a booking start the minute the last one ends, and handles no requests")
    void mostBookingsEdges() {
        assertEquals(3, DockScheduler.mostBookings(listOf(w(720, 780), w(600, 660), w(660, 720))),
                "600-660, 660-720 and 720-780 only touch, so all 3 fit");
        assertEquals(1, DockScheduler.mostBookings(listOf(w(600, 700), w(600, 700))),
                "Two copies of the same window overlap, so only 1 fits");
        assertEquals(0, DockScheduler.mostBookings(listOf()), "No requests give 0 bookings");
    }

    @Test
    @DisplayName("docksNeeded finds the most bookings happening at the same time")
    void docksNeeded() {
        assertEquals(3, DockScheduler.docksNeeded(listOf(w(600, 700), w(630, 720), w(650, 680), w(710, 800))),
                "At minute 650 three bookings are active (600-700, 630-720 and 650-680), so 3 docks are needed");
        assertEquals(3, DockScheduler.docksNeeded(listOf(w(600, 660), w(600, 660), w(600, 660))),
                "Three bookings with the same window all happen at once, so 3 docks are needed");
    }

    @Test
    @DisplayName("A booking that starts the minute another ends reuses its dock")
    void docksNeededTouching() {
        assertEquals(1, DockScheduler.docksNeeded(listOf(w(660, 720), w(600, 660), w(720, 780))),
                "600-660, 660-720 and 720-780 only touch, so 1 dock is enough. Free the dock before you use one");
        assertEquals(1, DockScheduler.docksNeeded(listOf(w(600, 660))), "One booking needs 1 dock");
        assertEquals(0, DockScheduler.docksNeeded(listOf()), "No bookings need 0 docks");
    }

    @Test
    @DisplayName("A maintenance window merges with every booking it overlaps")
    void insertMerges() {
        List<Interval> booked = List.of(w(600, 660), w(700, 760), w(800, 860), w(900, 960));
        assertEquals(List.of(w(600, 860), w(900, 960)), DockScheduler.insertMaintenanceWindow(booked, w(650, 810)),
                "650-810 overlaps 600-660, 700-760 and 800-860, so they become one window 600-860. 900-960 stays");
        assertEquals(List.of(w(600, 660), w(700, 720), w(800, 860)),
                DockScheduler.insertMaintenanceWindow(List.of(w(600, 660), w(800, 860)), w(700, 720)),
                "700-720 fits in the gap, so it goes between the others, in start order");
    }

    @Test
    @DisplayName("A maintenance window can go first, last, touch others or join an empty day")
    void insertEdges() {
        List<Interval> booked = List.of(w(600, 660), w(800, 860));
        assertEquals(List.of(w(500, 550), w(600, 660), w(800, 860)), DockScheduler.insertMaintenanceWindow(booked, w(500, 550)),
                "500-550 comes before every booking, so it goes first");
        assertEquals(List.of(w(600, 660), w(800, 860), w(900, 930)), DockScheduler.insertMaintenanceWindow(booked, w(900, 930)),
                "900-930 comes after every booking, so it goes last");
        assertEquals(List.of(w(600, 660), w(660, 800), w(800, 860)), DockScheduler.insertMaintenanceWindow(booked, w(660, 800)),
                "660-800 only touches 600-660 and 800-860, so nothing merges");
        assertEquals(List.of(w(700, 720)), DockScheduler.insertMaintenanceWindow(List.of(), w(700, 720)),
                "On an empty day the maintenance window is the only window");
    }

    @Test
    @DisplayName("100000 bookings are scheduled in well under 2 seconds")
    void bigDay() {
        List<Interval> bookings = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            int start = (i * 37) % 1400;
            bookings.add(w(start, start + 1 + (i % 40)));
        }
        int docks = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> DockScheduler.docksNeeded(bookings),
                "docksNeeded took over 2 seconds. Sort the starts and ends once, then sweep; do not compare every pair");
        int accepted = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> DockScheduler.mostBookings(bookings),
                "mostBookings took over 2 seconds. Sort once by end, then make one pass");
        assertEquals(EXPECTED_DOCKS, docks, "The most bookings active at once in this big day is " + EXPECTED_DOCKS);
        assertEquals(EXPECTED_BOOKINGS, accepted, "The most non-overlapping bookings in this big day is " + EXPECTED_BOOKINGS);
    }

    private static final int EXPECTED_DOCKS = 1503;
    private static final int EXPECTED_BOOKINGS = 211;
}

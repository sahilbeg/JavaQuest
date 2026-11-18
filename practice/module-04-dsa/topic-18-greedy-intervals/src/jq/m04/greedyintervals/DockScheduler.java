package jq.m04.greedyintervals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DockScheduler {

    public static List<Interval> mergeBusyPeriods(List<Interval> bookings) {
        // TODO 2: copy bookings into a new ArrayList and sort the copy by start.
        //         Never sort the list you were given: it may be unmodifiable.
        // TODO 3: walk the sorted copy. If a booking overlaps the last merged window, replace that window
        //         with one that ends at the LATER of the two ends. Otherwise add the booking as a new window.
        return new ArrayList<>();
    }

    public static int mostBookings(List<Interval> requests) {
        // TODO 4: sort a copy by END time. Take a request when it starts at or after
        //         the end of the last request you took, and count it.
        return -1;
    }

    public static int docksNeeded(List<Interval> bookings) {
        // TODO 5: put every start in one int[] and every end in another, and sort both.
        //         For each start: first free a dock for every end that is <= this start,
        //         then use a dock. Return the most docks in use at once.
        return -1;
    }

    public static List<Interval> insertMaintenanceWindow(List<Interval> booked, Interval window) {
        // TODO 6: booked is sorted and has no overlaps. Copy the windows that end at or before
        //         window.start(), merge every window that overlaps the new one into it,
        //         add the merged window, then copy the rest.
        return new ArrayList<>();
    }

    public static void main(String[] args) {
        List<Interval> bookings = List.of(
                new Interval(600, 700), new Interval(630, 720), new Interval(650, 680), new Interval(710, 800));
        System.out.println("Busy periods: " + mergeBusyPeriods(bookings));
        System.out.println("Most bookings: " + mostBookings(bookings));
        System.out.println("Docks needed: " + docksNeeded(bookings));
        System.out.println("With maintenance: " + insertMaintenanceWindow(
                List.of(new Interval(600, 660), new Interval(800, 860)), new Interval(700, 720)));
    }
}

package jq.m04.greedyintervals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DockScheduler {

    // Sort by start, then extend the last merged window while the next one overlaps it.
    // O(n log n) time for the sort, O(n) space for the copy and the answer.
    public static List<Interval> mergeBusyPeriods(List<Interval> bookings) {
        List<Interval> sorted = new ArrayList<>(bookings);
        sorted.sort(Comparator.comparingInt(Interval::start));
        List<Interval> merged = new ArrayList<>();
        for (Interval booking : sorted) {
            if (!merged.isEmpty() && merged.get(merged.size() - 1).overlaps(booking)) {
                Interval last = merged.remove(merged.size() - 1);
                merged.add(new Interval(last.start(), Math.max(last.end(), booking.end())));
            } else {
                merged.add(booking);
            }
        }
        return merged;
    }

    // Sort by end, then take every request that starts at or after the end of the last one taken.
    // O(n log n) time, O(n) space for the copy.
    public static int mostBookings(List<Interval> requests) {
        List<Interval> byEnd = new ArrayList<>(requests);
        byEnd.sort(Comparator.comparingInt(Interval::end));
        int count = 0;
        int freeFrom = Integer.MIN_VALUE;
        for (Interval request : byEnd) {
            if (request.start() >= freeFrom) {
                count++;
                freeFrom = request.end();
            }
        }
        return count;
    }

    // Sweep: sorted starts and sorted ends. A booking that starts at the same minute another ends
    // reuses that dock, because the ends are checked first. O(n log n) time, O(n) space.
    public static int docksNeeded(List<Interval> bookings) {
        int n = bookings.size();
        int[] starts = new int[n];
        int[] ends = new int[n];
        for (int i = 0; i < n; i++) {
            starts[i] = bookings.get(i).start();
            ends[i] = bookings.get(i).end();
        }
        Arrays.sort(starts);
        Arrays.sort(ends);
        int inUse = 0;
        int most = 0;
        int e = 0;
        for (int s = 0; s < n; s++) {
            while (ends[e] <= starts[s]) {
                inUse--;
                e++;
            }
            inUse++;
            most = Math.max(most, inUse);
        }
        return most;
    }

    // One pass over a sorted list: copy what ends before the window, merge what overlaps it,
    // then copy the rest. O(n) time, O(n) space for the answer.
    public static List<Interval> insertMaintenanceWindow(List<Interval> booked, Interval window) {
        List<Interval> result = new ArrayList<>();
        int i = 0;
        while (i < booked.size() && booked.get(i).end() <= window.start()) {
            result.add(booked.get(i));
            i++;
        }
        int start = window.start();
        int end = window.end();
        while (i < booked.size() && booked.get(i).overlaps(new Interval(start, end))) {
            start = Math.min(start, booked.get(i).start());
            end = Math.max(end, booked.get(i).end());
            i++;
        }
        result.add(new Interval(start, end));
        while (i < booked.size()) {
            result.add(booked.get(i));
            i++;
        }
        return result;
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

package jq.m03.comparatorsiterators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RideSorter {

    public static List<Ride> sortByMinutes(List<Ride> rides) {
        List<Ride> sorted = new ArrayList<>(rides);
        Collections.sort(sorted);
        return sorted;
    }

    public static List<Ride> sortByStationThenMinutes(List<Ride> rides) {
        List<Ride> sorted = new ArrayList<>(rides);
        sorted.sort(new ByStationThenMinutes());
        return sorted;
    }

    public static List<Ride> longestFirst(List<Ride> rides) {
        List<Ride> sorted = new ArrayList<>(rides);
        sorted.sort(new ByMinutes().reversed());
        return sorted;
    }

    public static int removeAbove(List<Ride> rides, int maxMinutes) {
        int removed = 0;
        Iterator<Ride> it = rides.iterator();
        while (it.hasNext()) {
            Ride ride = it.next();
            if (ride.minutes() > maxMinutes) {
                it.remove();
                removed++;
            }
        }
        return removed;
    }
}

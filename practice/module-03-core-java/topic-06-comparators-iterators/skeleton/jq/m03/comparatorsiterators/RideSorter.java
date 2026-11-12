package jq.m03.comparatorsiterators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RideSorter {

    public static List<Ride> sortByMinutes(List<Ride> rides) {
        List<Ride> sorted = new ArrayList<>(rides);
        // TODO 4: sort the copy in natural order with Collections.sort
        return sorted;
    }

    public static List<Ride> sortByStationThenMinutes(List<Ride> rides) {
        List<Ride> sorted = new ArrayList<>(rides);
        // TODO 5: sort the copy with a ByStationThenMinutes object
        return sorted;
    }

    public static List<Ride> longestFirst(List<Ride> rides) {
        List<Ride> sorted = new ArrayList<>(rides);
        // TODO 6: sort the copy with new ByMinutes().reversed()
        return sorted;
    }

    public static int removeAbove(List<Ride> rides, int maxMinutes) {
        // TODO 7: walk rides with an Iterator, call remove() on rides over maxMinutes, count them
        return 0;
    }
}

package jq.m10.interview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Problem 1 of the mock coding round: where was a bike at a given second?
 * Each bike keeps its sightings in time order, so a lookup can be a binary search.
 */
public class RideTimeline {

    private record Sighting(long second, String station) {}

    private final Map<String, List<Sighting>> sightings = new HashMap<>();

    /** Remembers that bikeId was at station at this second. Times for one bike must go up. O(1) amortised. */
    public void put(String bikeId, long second, String station) {
        // TODO 1: get this bike's list (computeIfAbsent with a new ArrayList).
        // TODO 2: if the list is not empty and second <= the last sighting's second, throw IllegalArgumentException.
        // TODO 3: add new Sighting(second, station) to the end of the list.
    }

    /** The station of the latest sighting at or before second, or null if there is none. O(log n). */
    public String get(String bikeId, long second) {
        // TODO 4: an unknown bike has no sightings: return null.
        // TODO 5: binary search the list for the LAST sighting whose second is <= second.
        //         Return its station, or null when every sighting is later than second.
        return null;
    }
}

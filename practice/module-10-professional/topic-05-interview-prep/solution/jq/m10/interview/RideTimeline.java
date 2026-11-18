package jq.m10.interview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Problem 1 of the mock coding round: where was a bike at a given second?
 * Each bike keeps its sightings in time order, so a lookup is a binary search.
 */
public class RideTimeline {

    private record Sighting(long second, String station) {}

    private final Map<String, List<Sighting>> sightings = new HashMap<>();

    /** Remembers that bikeId was at station at this second. Times for one bike must go up. O(1) amortised. */
    public void put(String bikeId, long second, String station) {
        List<Sighting> list = sightings.computeIfAbsent(bikeId, id -> new ArrayList<>());
        if (!list.isEmpty()) {
            long last = list.get(list.size() - 1).second();
            if (second <= last) {
                throw new IllegalArgumentException(
                        "times for " + bikeId + " must go up: got " + second + " after " + last);
            }
        }
        list.add(new Sighting(second, station));
    }

    /** The station of the latest sighting at or before second, or null if there is none. O(log n). */
    public String get(String bikeId, long second) {
        List<Sighting> list = sightings.get(bikeId);
        if (list == null) {
            return null;
        }
        int low = 0;
        int high = list.size() - 1;
        int found = -1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (list.get(mid).second() <= second) {
                found = mid;          // a candidate; a later one may also fit
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return found == -1 ? null : list.get(found).station();
    }
}

package jq.m06.debugging;

import java.util.HashMap;
import java.util.Map;

public class StationIndex {
    private final Map<String, Integer> freeDocks = new HashMap<>();

    public StationIndex() {
        freeDocks.put("Riverside", 12);
        freeDocks.put("Old Mill", 7);
        freeDocks.put("Harbor", 3);
        freeDocks.put("Uptown", 0);
    }

    public int docksAt(String station) {
        return freeDocks.getOrDefault(station, 0);
    }
}

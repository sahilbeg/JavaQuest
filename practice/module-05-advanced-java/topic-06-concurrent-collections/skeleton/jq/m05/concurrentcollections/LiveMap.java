package jq.m05.concurrentcollections;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class LiveMap {

    private final Map<String, Integer> docks = new ConcurrentHashMap<>();

    public void dockEvent(String station, int change) {
        // TODO 1: add change to the station's count in one atomic step with docks.merge(...)
    }

    public int free(String station) {
        // TODO 2: return the station's count, or 0 when the station is unknown
        return 0;
    }

    public Map<String, Integer> snapshot() {
        // TODO 3: return a TreeMap copy of docks, so stations come out in alphabetical order
        return new TreeMap<>();
    }
}

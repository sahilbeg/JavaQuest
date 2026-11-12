package jq.m05.concurrentcollections;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class LiveMap {

    private final Map<String, Integer> docks = new ConcurrentHashMap<>();

    public void dockEvent(String station, int change) {
        docks.merge(station, change, Integer::sum);
    }

    public int free(String station) {
        return docks.getOrDefault(station, 0);
    }

    public Map<String, Integer> snapshot() {
        return new TreeMap<>(docks);
    }
}

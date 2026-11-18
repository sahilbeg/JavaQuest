package jq.m05.memorymodel;

import java.util.List;

// TODO 3: make the class final, so no subclass can add a field that changes
public class StationSnapshot {

    // TODO 4: make every field final
    private String station;
    private int freeDocks;
    private List<String> bikeIds;

    public StationSnapshot(String station, int freeDocks, List<String> bikeIds) {
        this.station = station;
        this.freeDocks = freeDocks;
        // TODO 5: store a copy with List.copyOf(bikeIds), so the caller's list cannot change the snapshot
        this.bikeIds = bikeIds;
    }

    public String station() {
        return station;
    }

    public int freeDocks() {
        return freeDocks;
    }

    public List<String> bikeIds() {
        return bikeIds;
    }

    public StationSnapshot withFreeDocks(int newFreeDocks) {
        // TODO 6: return a NEW StationSnapshot with newFreeDocks, and leave this one unchanged
        return this;
    }
}

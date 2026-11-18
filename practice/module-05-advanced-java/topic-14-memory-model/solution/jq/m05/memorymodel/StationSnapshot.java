package jq.m05.memorymodel;

import java.util.List;

public final class StationSnapshot {

    private final String station;
    private final int freeDocks;
    private final List<String> bikeIds;

    public StationSnapshot(String station, int freeDocks, List<String> bikeIds) {
        this.station = station;
        this.freeDocks = freeDocks;
        this.bikeIds = List.copyOf(bikeIds);
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
        return new StationSnapshot(station, newFreeDocks, bikeIds);
    }
}

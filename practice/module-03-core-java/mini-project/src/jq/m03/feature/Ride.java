package jq.m03.feature;

public record Ride(String bikeId, String station, String type, int minutes) {

    public double fare() {
        // TODO 1: 1.00 plus minutes times 0.15 (classic), 0.30 (ebike) or 0.25 (cargo)
        return 0;
    }
}

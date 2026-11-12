package jq.m02.records;

public record Ride(String bikeId, String type, int minutes) {

    public Ride {
        // TODO 1: if minutes is negative, set the minutes parameter to 0 (no "this.")
    }

    public double fare() {
        // TODO 2: pick the rate from type (classic 0.15, ebike 0.30, cargo 0.25)
        // and return 1.00 + minutes * rate
        return 0;
    }
}

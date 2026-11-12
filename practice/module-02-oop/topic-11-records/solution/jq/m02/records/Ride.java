package jq.m02.records;

public record Ride(String bikeId, String type, int minutes) {

    public Ride {
        if (minutes < 0) {
            minutes = 0;
        }
    }

    public double fare() {
        double rate = 0.15;
        if (type.equals("ebike")) {
            rate = 0.30;
        } else if (type.equals("cargo")) {
            rate = 0.25;
        }
        return 1.00 + minutes * rate;
    }
}

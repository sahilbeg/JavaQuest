package jq.m03.feature;

public record Ride(String bikeId, String station, String type, int minutes) {

    public double fare() {
        double price = switch (type) {
            case "ebike" -> 0.30;
            case "cargo" -> 0.25;
            default -> 0.15;
        };
        return 1.00 + minutes * price;
    }
}

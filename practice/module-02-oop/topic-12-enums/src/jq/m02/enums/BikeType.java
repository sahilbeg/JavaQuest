package jq.m02.enums;

public enum BikeType {
    CLASSIC(0.15), EBIKE(0.30), CARGO(0.25);

    private double price;

    BikeType(double price) {
        // TODO 1: store the price in the price field
    }

    public double pricePerMinute() {
        // TODO 2: return the price field
        return 0.0;
    }
}

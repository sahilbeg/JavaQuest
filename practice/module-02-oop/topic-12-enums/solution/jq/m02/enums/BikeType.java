package jq.m02.enums;

public enum BikeType {
    CLASSIC(0.15), EBIKE(0.30), CARGO(0.25);

    private final double price;

    BikeType(double price) {
        this.price = price;
    }

    public double pricePerMinute() {
        return price;
    }
}

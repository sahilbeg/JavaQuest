package jq.m02.feature;

public enum BikeType {
    CLASSIC(0.15), EBIKE(0.30), CARGO(0.25);

    private final double pricePerMinute;

    BikeType(double pricePerMinute) {
        this.pricePerMinute = pricePerMinute;
    }

    public double pricePerMinute() {
        return pricePerMinute;
    }
}

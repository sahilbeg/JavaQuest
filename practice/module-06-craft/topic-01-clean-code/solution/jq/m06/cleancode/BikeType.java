package jq.m06.cleancode;

public enum BikeType {
    CLASSIC(15), EBIKE(30), CARGO(25);

    private final long centsPerMinute;

    BikeType(long centsPerMinute) {
        this.centsPerMinute = centsPerMinute;
    }

    public long centsPerMinute() {
        return centsPerMinute;
    }
}

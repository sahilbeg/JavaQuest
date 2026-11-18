package jq.m06.oodesign;

/** The kinds of bike at a Spoke station. Each kind knows its own per-minute rate. */
public enum BikeType {
    CLASSIC(15),
    EBIKE(30),
    CARGO(25);

    private final long perMinuteCents;

    BikeType(long perMinuteCents) {
        this.perMinuteCents = perMinuteCents;
    }

    public long perMinuteCents() {
        return perMinuteCents;
    }
}

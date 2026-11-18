package jq.m09.hexagonal;

/** Core (domain): the three kinds of bike, and what a minute on each costs. */
public enum BikeType {
    CLASSIC(15),
    EBIKE(30),
    CARGO(25);

    /** Every ride pays this to unlock, in cents. */
    public static final long UNLOCK_CENTS = 100;

    private final long centsPerMinute;

    BikeType(long centsPerMinute) {
        this.centsPerMinute = centsPerMinute;
    }

    public long centsPerMinute() {
        return centsPerMinute;
    }

    /** The fare in whole cents: 100 to unlock, plus the price of each minute. */
    public long fareCents(int minutes) {
        return UNLOCK_CENTS + centsPerMinute * minutes;
    }
}

package jq.m09.messaging;

/** The three kinds of Spoke bike, with their price per minute in cents. */
public enum BikeType {
    CLASSIC(15),
    EBIKE(30),
    CARGO(25);

    /** Every ride costs this much to unlock, on top of the minutes. */
    public static final long UNLOCK_CENTS = 100;

    private final long centsPerMinute;

    BikeType(long centsPerMinute) {
        this.centsPerMinute = centsPerMinute;
    }

    public long centsPerMinute() {
        return centsPerMinute;
    }

    /** The fare for a ride of this many minutes: 100 cents to unlock, plus the per-minute price. */
    public long fareCents(int minutes) {
        return UNLOCK_CENTS + minutes * centsPerMinute;
    }
}

package jq.m09.feature;

/** The fare rule every Spoke service shares. */
public final class Fares {

    public static final long UNLOCK_CENTS = 100;

    private Fares() {
    }

    /** 100 cents to unlock, plus the per-minute price for the bike type times the minutes. */
    public static long expectedCents(BikeType type, int minutes) {
        long perMinute = switch (type) {
            case CLASSIC -> 15;
            case EBIKE -> 30;
            case CARGO -> 25;
        };
        return UNLOCK_CENTS + perMinute * minutes;
    }
}

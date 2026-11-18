package jq.m09.feature;

/** The fare rule every Spoke service shares. */
public final class Fares {

    public static final long UNLOCK_CENTS = 100;

    private Fares() {
    }

    /** 100 cents to unlock, plus the per-minute price for the bike type times the minutes. */
    public static long expectedCents(BikeType type, int minutes) {
        // TODO 1: per minute, CLASSIC costs 15 cents, EBIKE 30 and CARGO 25.
        // Return UNLOCK_CENTS plus the per-minute price times the minutes.
        return 0;
    }
}

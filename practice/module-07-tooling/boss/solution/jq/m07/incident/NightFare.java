package jq.m07.incident;

import java.time.Clock;
import java.time.LocalTime;

/** Riders pay a 50 cent night surcharge from 22:00 up to, but not including, 06:00. */
public final class NightFare {

    public static final long SURCHARGE_CENTS = 50;
    private static final LocalTime NIGHT_STARTS = LocalTime.of(22, 0);
    private static final LocalTime NIGHT_ENDS = LocalTime.of(6, 0);

    private final Clock clock;

    public NightFare(Clock clock) {
        this.clock = clock;
    }

    public long surchargeCents() {
        LocalTime now = LocalTime.now(clock);
        boolean night = !now.isBefore(NIGHT_STARTS) || now.isBefore(NIGHT_ENDS);
        return night ? SURCHARGE_CENTS : 0;
    }
}

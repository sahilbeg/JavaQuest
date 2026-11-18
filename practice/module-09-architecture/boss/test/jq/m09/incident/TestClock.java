package jq.m09.incident;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** A clock that only moves when a check moves it. */
final class TestClock extends Clock {

    static final Instant EIGHT_AM = Instant.parse("2026-11-18T08:00:00Z");

    private volatile Instant now = EIGHT_AM;

    /** Sets the time to 8:00:00 plus the given milliseconds. */
    void at(long millisAfterEight) {
        now = EIGHT_AM.plusMillis(millisAfterEight);
    }

    void advance(Duration duration) {
        now = now.plus(duration);
    }

    @Override
    public Instant instant() {
        return now;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }
}

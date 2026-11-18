package jq.m11.feature;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** A clock that only moves when a check moves it. It starts at 10:00:00. */
final class TestClock extends Clock {

    static final Instant TEN_AM = Instant.parse("2026-11-20T10:00:00Z");

    private Instant now = TEN_AM;

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

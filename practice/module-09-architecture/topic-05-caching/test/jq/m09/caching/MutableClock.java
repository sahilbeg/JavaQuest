package jq.m09.caching;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** A clock the checks move by hand, so time never depends on the real clock. */
final class MutableClock extends Clock {

    private Instant now = Instant.parse("2026-11-18T08:00:00Z");

    void advance(Duration step) {
        now = now.plus(step);
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

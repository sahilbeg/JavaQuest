package jq.m09.resilience;

import java.util.function.LongSupplier;

/** A clock the checks move by hand, in milliseconds. It never moves on its own. */
final class FakeClock implements LongSupplier {

    private long nowMillis;

    FakeClock(long startMillis) {
        this.nowMillis = startMillis;
    }

    void advance(long millis) {
        nowMillis += millis;
    }

    @Override
    public long getAsLong() {
        return nowMillis;
    }
}

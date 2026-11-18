package jq.m09.resilience;

import java.time.Duration;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Stops calling a provider that keeps failing. CLOSED: calls go through and failures in a row are counted.
 * OPEN: calls fail fast. HALF_OPEN: one trial call decides whether to close again or stay open.
 */
public final class CircuitBreaker {

    public enum State { CLOSED, OPEN, HALF_OPEN }

    private final int failureThreshold;
    private final long openMillis;
    private final LongSupplier nowMillis;

    private State state = State.CLOSED;
    private int consecutiveFailures;
    private long openedAtMillis;
    private boolean trialInFlight;

    public CircuitBreaker(int failureThreshold, Duration openDuration, LongSupplier nowMillis) {
        if (failureThreshold < 1) {
            throw new IllegalArgumentException("failureThreshold must be at least 1");
        }
        if (openDuration.isNegative() || openDuration.isZero()) {
            throw new IllegalArgumentException("openDuration must be more than zero");
        }
        this.failureThreshold = failureThreshold;
        this.openMillis = openDuration.toMillis();
        this.nowMillis = nowMillis;
    }

    /** The current state. An OPEN breaker whose open time is over reports HALF_OPEN. */
    public synchronized State state() {
        if (state == State.OPEN && nowMillis.getAsLong() - openedAtMillis >= openMillis) {
            state = State.HALF_OPEN;
        }
        return state;
    }

    public <T> T call(Supplier<T> action) {
        synchronized (this) {
            State now = state();
            if (now == State.OPEN) {
                throw new CallNotPermittedException("circuit breaker is OPEN: failing fast without calling the provider");
            }
            if (now == State.HALF_OPEN) {
                if (trialInFlight) {
                    throw new CallNotPermittedException("circuit breaker is HALF_OPEN and its one trial call is still running");
                }
                trialInFlight = true;
            }
        }
        // The provider is called outside the lock, so a slow call never blocks other threads' decisions.
        T result;
        try {
            result = action.get();
        } catch (RuntimeException e) {
            onFailure();
            throw e;
        }
        onSuccess();
        return result;
    }

    private synchronized void onSuccess() {
        if (state == State.HALF_OPEN) {
            state = State.CLOSED;
            trialInFlight = false;
        }
        consecutiveFailures = 0;
    }

    private synchronized void onFailure() {
        if (state == State.HALF_OPEN) {
            open();
        } else if (state == State.CLOSED) {
            consecutiveFailures++;
            if (consecutiveFailures >= failureThreshold) {
                open();
            }
        }
    }

    private void open() {
        state = State.OPEN;
        openedAtMillis = nowMillis.getAsLong();
        consecutiveFailures = 0;
        trialInFlight = false;
    }
}

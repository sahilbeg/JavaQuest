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
        // TODO 9: throw IllegalArgumentException when failureThreshold < 1, or openDuration is zero or negative
        this.failureThreshold = failureThreshold;
        this.openMillis = openDuration.toMillis();
        this.nowMillis = nowMillis;
    }

    /** The current state. An OPEN breaker whose open time is over reports HALF_OPEN. */
    public synchronized State state() {
        // TODO 10: when OPEN and at least openMillis have passed since openedAtMillis, move to HALF_OPEN
        return state;
    }

    public <T> T call(Supplier<T> action) {
        // TODO 11: OPEN: throw CallNotPermittedException without calling the action.
        //          HALF_OPEN: let one trial call through; while it runs, throw CallNotPermittedException for any other call.
        // TODO 12: call action.get(). On success, record it and return the answer.
        //          On a RuntimeException, record the failure and throw the same exception on.
        return null;
    }

    private synchronized void onSuccess() {
        // TODO 13: a successful trial closes the breaker. Any success resets the count of failures in a row.
    }

    private synchronized void onFailure() {
        // TODO 14: a failed trial opens the breaker again, from now. When CLOSED, count the failure;
        //          at failureThreshold in a row, open the breaker from now.
    }
}

package jq.m09.resilience;

import java.util.function.Supplier;

/** Calls an action, and tries again after a backoff wait when it fails in a way that may pass next time. */
public final class Retry {

    private final int maxAttempts;
    private final Backoff backoff;
    private final Sleeper sleeper;

    public Retry(int maxAttempts, Backoff backoff, Sleeper sleeper) {
        // TODO 18: throw IllegalArgumentException when maxAttempts < 1
        this.maxAttempts = maxAttempts;
        this.backoff = backoff;
        this.sleeper = sleeper;
    }

    /** Timeouts, 429 and 5xx may pass next time. Every other failure would fail the same way again. */
    public static boolean isRetryable(RuntimeException e) {
        // TODO 19: true for a ProviderTimeoutException, and for a ProviderException whose status is 429 or 500 and above
        return false;
    }

    public <T> T call(Supplier<T> action) {
        // TODO 20: try action.get() up to maxAttempts times in all. Return the first answer.
        //          On a RuntimeException: throw it at once when it is not retryable, or when this was the last attempt.
        //          Otherwise sleeper.sleep(backoff.delayMillis(attempt)) and try again.
        return null;
    }
}

package jq.m09.resilience;

import java.util.function.Supplier;

/** Calls an action, and tries again after a backoff wait when it fails in a way that may pass next time. */
public final class Retry {

    private final int maxAttempts;
    private final Backoff backoff;
    private final Sleeper sleeper;

    public Retry(int maxAttempts, Backoff backoff, Sleeper sleeper) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        this.maxAttempts = maxAttempts;
        this.backoff = backoff;
        this.sleeper = sleeper;
    }

    /** Timeouts, 429 and 5xx may pass next time. Every other failure would fail the same way again. */
    public static boolean isRetryable(RuntimeException e) {
        if (e instanceof ProviderTimeoutException) {
            return true;
        }
        if (e instanceof ProviderException provider) {
            return provider.status() == 429 || provider.status() >= 500;
        }
        return false;
    }

    public <T> T call(Supplier<T> action) {
        for (int attempt = 1; ; attempt++) {
            try {
                return action.get();
            } catch (RuntimeException e) {
                if (!isRetryable(e) || attempt >= maxAttempts) {
                    throw e;
                }
                sleeper.sleep(backoff.delayMillis(attempt));
            }
        }
    }
}

package jq.m09.resilience;

/** Waits between retries. Production uses {@link #real()}; the checks pass a fake that only writes the delays down. Given to you. */
@FunctionalInterface
public interface Sleeper {

    void sleep(long millis);

    static Sleeper real() {
        return millis -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("interrupted while waiting to retry", e);
            }
        };
    }
}

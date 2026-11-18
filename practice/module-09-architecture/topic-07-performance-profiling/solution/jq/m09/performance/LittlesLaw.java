package jq.m09.performance;

/**
 * Little's law: in-flight requests = arrival rate x time in the system (L = lambda x W).
 * Counts of things you must provide round UP; the rate you can promise rounds DOWN.
 */
public final class LittlesLaw {

    private LittlesLaw() {}

    /** How many requests are being served at the same moment, on average, rounded up. */
    public static long inFlight(long requestsPerSecond, long latencyMillis) {
        requireNotNegative(requestsPerSecond, latencyMillis);
        return Math.ceilDiv(requestsPerSecond * latencyMillis, 1000L);
    }

    /**
     * How many threads keep each one busy at most targetUtilisationPercent of the time.
     * That is inFlight divided by (target / 100), rounded up. Do the division on the exact value, not on the rounded inFlight.
     */
    public static long threadsNeeded(long requestsPerSecond, long latencyMillis, int targetUtilisationPercent) {
        requireNotNegative(requestsPerSecond, latencyMillis);
        if (targetUtilisationPercent < 1 || targetUtilisationPercent > 100) {
            throw new IllegalArgumentException("target utilisation must be 1 to 100 percent, got " + targetUtilisationPercent);
        }
        return Math.ceilDiv(requestsPerSecond * latencyMillis * 100, 1000L * targetUtilisationPercent);
    }

    /** The most requests per second that this many threads can finish, rounded down. lambda = L / W. */
    public static long maxRequestsPerSecond(int threads, long latencyMillis) {
        if (threads < 0 || latencyMillis <= 0) {
            throw new IllegalArgumentException("need threads >= 0 and latencyMillis > 0");
        }
        return threads * 1000L / latencyMillis;
    }

    private static void requireNotNegative(long requestsPerSecond, long latencyMillis) {
        if (requestsPerSecond < 0 || latencyMillis < 0) {
            throw new IllegalArgumentException("rate and latency cannot be negative");
        }
    }
}

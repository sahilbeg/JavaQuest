package jq.m09.performance;

/**
 * Little's law: in-flight requests = arrival rate x time in the system (L = lambda x W).
 * Counts of things you must provide round UP; the rate you can promise rounds DOWN.
 */
public final class LittlesLaw {

    private LittlesLaw() {}

    /** How many requests are being served at the same moment, on average, rounded up. */
    public static long inFlight(long requestsPerSecond, long latencyMillis) {
        // TODO 5: throw IllegalArgumentException if either argument is negative.
        //         Latency is in milliseconds, so L = requestsPerSecond * latencyMillis / 1000, rounded UP (Math.ceilDiv).
        return 0;
    }

    /**
     * How many threads keep each one busy at most targetUtilisationPercent of the time.
     * That is inFlight divided by (target / 100), rounded up. Do the division on the exact value, not on the rounded inFlight.
     */
    public static long threadsNeeded(long requestsPerSecond, long latencyMillis, int targetUtilisationPercent) {
        // TODO 6: throw IllegalArgumentException if a rate or latency is negative, or if the target is not 1 to 100.
        //         threads = ceiling of requestsPerSecond * latencyMillis * 100 / (1000 * target).
        return 0;
    }

    /** The most requests per second that this many threads can finish, rounded down. lambda = L / W. */
    public static long maxRequestsPerSecond(int threads, long latencyMillis) {
        // TODO 7: throw IllegalArgumentException if threads is negative or latencyMillis is 0 or less.
        //         rate = threads * 1000 / latencyMillis, rounded DOWN (plain long division).
        return 0;
    }
}

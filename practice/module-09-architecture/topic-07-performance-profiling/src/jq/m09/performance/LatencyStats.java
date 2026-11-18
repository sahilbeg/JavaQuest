package jq.m09.performance;

import java.util.Arrays;

/** Percentiles of response times, in whole milliseconds, by the nearest-rank method. */
public final class LatencyStats {

    private LatencyStats() {}

    /**
     * The percent-th percentile of the samples, by nearest rank. percent is 1 to 100.
     * The caller's array must not change.
     */
    public static long percentile(long[] millis, int percent) {
        // TODO 1: throw IllegalArgumentException when percent is below 1 or above 100, or when millis is empty.
        // TODO 2: sort a COPY of millis (Arrays.copyOf). Never sort the caller's array: it is their data.
        // TODO 3: rank = the ceiling of percent * n / 100, with Math.ceilDiv on longs. Return sorted[rank - 1].
        return 0;
    }

    /** count, p50, p95, p99 and max of the samples. An empty array gives all zeros. */
    public static LatencySummary summarize(long[] millis) {
        // TODO 4: for an empty array, return new LatencySummary(0, 0, 0, 0, 0).
        //         Otherwise return the count, percentile 50, 95 and 99, and the max (the slowest sample).
        return null;
    }
}

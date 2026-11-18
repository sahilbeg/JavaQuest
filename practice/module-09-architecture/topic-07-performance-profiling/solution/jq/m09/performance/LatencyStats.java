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
        if (percent < 1 || percent > 100) {
            throw new IllegalArgumentException("percent must be 1 to 100, got " + percent);
        }
        if (millis.length == 0) {
            throw new IllegalArgumentException("no samples: a percentile of nothing is undefined");
        }
        long[] sorted = Arrays.copyOf(millis, millis.length);
        Arrays.sort(sorted);
        long rank = Math.ceilDiv((long) percent * sorted.length, 100L);
        return sorted[(int) rank - 1];
    }

    /** count, p50, p95, p99 and max of the samples. An empty array gives all zeros. */
    public static LatencySummary summarize(long[] millis) {
        if (millis.length == 0) {
            return new LatencySummary(0, 0, 0, 0, 0);
        }
        return new LatencySummary(millis.length,
                percentile(millis, 50), percentile(millis, 95), percentile(millis, 99), percentile(millis, 100));
    }
}

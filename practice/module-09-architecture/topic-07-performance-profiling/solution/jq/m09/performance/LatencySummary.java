package jq.m09.performance;

/** The numbers the latency dashboard shows: how many samples, three percentiles and the slowest one. Finished for you. */
public record LatencySummary(int count, long p50, long p95, long p99, long max) {}

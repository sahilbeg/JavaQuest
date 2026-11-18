package jq.m11.feature;

/**
 * What one version did during the canary.
 *
 * @param errorRatePercent share of requests that failed, in percent: 1.5 means 1.5%
 * @param p99Millis        99th percentile latency in milliseconds
 */
public record Metrics(double errorRatePercent, long p99Millis) {
}

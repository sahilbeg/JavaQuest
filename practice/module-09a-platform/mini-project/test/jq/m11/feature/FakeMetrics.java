package jq.m11.feature;

/** Canary and baseline numbers that a check sets. Healthy by default: 1.0% errors and 200 ms on both. */
final class FakeMetrics implements MetricsSource {

    Metrics baseline = new Metrics(1.0, 200);
    Metrics canary = new Metrics(1.0, 200);

    FakeMetrics canary(double errorRatePercent, long p99Millis) {
        canary = new Metrics(errorRatePercent, p99Millis);
        return this;
    }

    @Override
    public Metrics baseline() {
        return baseline;
    }

    @Override
    public Metrics canary() {
        return canary;
    }
}

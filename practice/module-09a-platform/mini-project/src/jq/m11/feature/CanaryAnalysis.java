package jq.m11.feature;

/** Compares the canary with the baseline, using the thresholds of an ANALYSIS stage. */
public final class CanaryAnalysis {

    private CanaryAnalysis() {
    }

    public static AnalysisResult evaluate(Stage analysis, Metrics baseline, Metrics canary) {
        // TODO 4: fail when the canary's error rate is more than analysis.maxErrorRateIncrease() points above
        // the baseline's (reason contains "error rate"), or when its p99 is more than
        // analysis.maxP99IncreasePercent() percent above the baseline's (reason contains "p99"). Exactly at a limit passes.
        return new AnalysisResult(true, "not checked");
    }
}

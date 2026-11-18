package jq.m11.feature;

/** Compares the canary with the baseline, using the thresholds of an ANALYSIS stage. */
public final class CanaryAnalysis {

    private CanaryAnalysis() {
    }

    public static AnalysisResult evaluate(Stage analysis, Metrics baseline, Metrics canary) {
        double increase = canary.errorRatePercent() - baseline.errorRatePercent();
        if (increase > analysis.maxErrorRateIncrease()) {
            return new AnalysisResult(false, "error rate " + canary.errorRatePercent() + "% is more than "
                    + analysis.maxErrorRateIncrease() + " points above the baseline's " + baseline.errorRatePercent() + "%");
        }
        if (canary.p99Millis() * 100 > baseline.p99Millis() * (100L + analysis.maxP99IncreasePercent())) {
            return new AnalysisResult(false, "p99 " + canary.p99Millis() + " ms is more than "
                    + analysis.maxP99IncreasePercent() + "% above the baseline's " + baseline.p99Millis() + " ms");
        }
        return new AnalysisResult(true, "error rate " + canary.errorRatePercent() + "% vs " + baseline.errorRatePercent()
                + "%, p99 " + canary.p99Millis() + " ms vs " + baseline.p99Millis() + " ms");
    }
}

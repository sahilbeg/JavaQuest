package jq.m11.feature;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CanaryAnalysisTest {

    private static final Stage ANALYSIS = Stage.analysis("analysis", 0.5, 20);
    private static final Metrics BASELINE = new Metrics(1.0, 200);

    @Test
    @DisplayName("Canary analysis passes exactly at both thresholds: 0.5 points more errors and 20% slower p99")
    void passesAtTheBoundary() {
        assertTrue(CanaryAnalysis.evaluate(ANALYSIS, BASELINE, new Metrics(1.5, 240)).passed(),
                "1.5% vs 1.0% is exactly 0.5 points, and 240 ms is exactly 20% above 200 ms: both are allowed");
        assertTrue(CanaryAnalysis.evaluate(ANALYSIS, BASELINE, new Metrics(0.25, 150)).passed(),
                "a canary that is better than the baseline passes");
    }

    @Test
    @DisplayName("Canary analysis fails just past either threshold, and the reason says which one")
    void failsPastTheBoundary() {
        AnalysisResult errors = CanaryAnalysis.evaluate(ANALYSIS, BASELINE, new Metrics(1.75, 200));
        assertFalse(errors.passed(), "1.75% is 0.75 points above 1.0%, more than the 0.5 allowed");
        assertTrue(String.valueOf(errors.reason()).contains("error rate"),
                "the reason should say the error rate was too high, but was: " + errors.reason());
        AnalysisResult slow = CanaryAnalysis.evaluate(ANALYSIS, BASELINE, new Metrics(1.0, 241));
        assertFalse(slow.passed(), "241 ms is more than 20% above 200 ms (the limit is 240 ms)");
        assertTrue(String.valueOf(slow.reason()).contains("p99"),
                "the reason should say the p99 was too high, but was: " + slow.reason());
    }
}

package jq.m11.feature;

/**
 * One stage of a pipeline. Settings that do not apply to a stage's type are 0.
 *
 * @param canaryPercent         CANARY: the share of traffic the new version gets, 1 to 50
 * @param approvalMinutes       APPROVAL: how long the approval stays open, at least 1
 * @param maxErrorRateIncrease  ANALYSIS: how many percentage points the canary's error rate may be above the baseline's
 * @param maxP99IncreasePercent ANALYSIS: how many percent the canary's p99 latency may be above the baseline's
 */
public record Stage(String name, StageType type, int canaryPercent, int approvalMinutes,
                    double maxErrorRateIncrease, int maxP99IncreasePercent) {

    /** A stage with no settings: build, test, image, deploy, smoke or promote. */
    public static Stage of(String name, StageType type) {
        return new Stage(name, type, 0, 0, 0, 0);
    }

    public static Stage canary(String name, int percent) {
        return new Stage(name, StageType.CANARY, percent, 0, 0, 0);
    }

    public static Stage approval(String name, int minutes) {
        return new Stage(name, StageType.APPROVAL, 0, minutes, 0, 0);
    }

    public static Stage analysis(String name, double maxErrorRateIncrease, int maxP99IncreasePercent) {
        return new Stage(name, StageType.ANALYSIS, 0, 0, maxErrorRateIncrease, maxP99IncreasePercent);
    }
}

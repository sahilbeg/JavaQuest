package jq.m11.feature;

/** What a StageExecutor says about one stage it ran. */
public record StageResult(boolean succeeded, String detail) {

    public static StageResult ok(String detail) {
        return new StageResult(true, detail);
    }

    public static StageResult failed(String detail) {
        return new StageResult(false, detail);
    }
}

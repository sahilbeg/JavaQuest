package jq.m11.feature;

/** The verdict of canary analysis, with a reason a person can read in the audit log. */
public record AnalysisResult(boolean passed, String reason) {
}

package jq.m11.feature;

public enum RunStatus {
    /** Started, or resumed, and moving through its stages. */
    RUNNING,
    /** Paused at an approval stage until someone approves. */
    WAITING_FOR_APPROVAL,
    /** Every stage succeeded: the new version is live. */
    SUCCEEDED,
    /** A stage failed, or an approval expired. Can be resumed. */
    FAILED,
    /** Canary analysis failed and the canary was rolled back. Final: ship a fix in a new run. */
    ROLLED_BACK
}

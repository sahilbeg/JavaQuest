package jq.m11.feature;

/** Every kind of decision the engine writes to the audit log. */
public enum Decision {
    STAGE_SUCCEEDED,
    STAGE_FAILED,
    APPROVAL_REQUESTED,
    APPROVED,
    APPROVAL_EXPIRED,
    CANARY_PASSED,
    CANARY_FAILED,
    ROLLED_BACK,
    RESUMED,
    RUN_SUCCEEDED
}

package jq.m12.pipelines;

/** What happened to one task for one logical date. */
public enum TaskState {
    /** The task has not run for this date yet. */
    NONE,
    /** The task's action finished without throwing. */
    SUCCESS,
    /** Every attempt threw: the first try and all its retries. */
    FAILED,
    /** A task it depends on did not succeed, so its action was never called. */
    UPSTREAM_FAILED
}

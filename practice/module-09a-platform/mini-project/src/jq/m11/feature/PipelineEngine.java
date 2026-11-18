package jq.m11.feature;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/** Runs a pipeline's stages in order, with an approval gate, canary analysis, an audit log and resume. */
public final class PipelineEngine {

    private final Pipeline pipeline;
    private final StageExecutor executor;
    private final MetricsSource metrics;
    private final Clock clock;

    public PipelineEngine(Pipeline pipeline, StageExecutor executor, MetricsSource metrics, Clock clock) {
        this.pipeline = pipeline;
        this.executor = executor;
        this.metrics = metrics;
        this.clock = clock;
    }

    /** Starts a new run and moves it as far as it can go. */
    public PipelineRun start() {
        PipelineRun run = new PipelineRun(pipeline);
        advance(run);
        return run;
    }

    /** Moves a run through its stages, in order, until it waits, fails, rolls back or finishes. */
    private void advance(PipelineRun run) {
        run.setStatus(RunStatus.RUNNING);
        for (Stage stage : pipeline.stages()) {
            // TODO 5: skip a stage that already SUCCEEDED.
            // TODO 6: APPROVAL pauses the run: stage WAITING, remember the clock's time, run WAITING_FOR_APPROVAL,
            // write APPROVAL_REQUESTED, and return. ANALYSIS asks CanaryAnalysis: a pass writes CANARY_PASSED;
            // a fail writes CANARY_FAILED, calls executor.rollBack(pipeline.canaryBefore(stage)), writes
            // ROLLED_BACK for the canary stage, sets the run ROLLED_BACK and returns.
            // TODO 7: any other stage goes to executor.execute(stage). Success writes STAGE_SUCCEEDED. A failure,
            // or an exception, writes STAGE_FAILED, fails the stage and the run, and returns.
        }
        // TODO 8: every stage has succeeded: set the run SUCCEEDED and write RUN_SUCCEEDED with the pipeline's name.
    }

    /** Approves the run's waiting approval stage, then carries on, unless the approval has expired. */
    public void approve(PipelineRun run, String approver) {
        // TODO 9: refuse a blank approver (IllegalArgumentException) and a run that is not WAITING_FOR_APPROVAL
        // (IllegalStateException). At or after approvalRequestedAt + approvalMinutes, the approval has expired:
        // fail the stage and the run, and write APPROVAL_EXPIRED. Otherwise mark the stage SUCCEEDED, write
        // APPROVED with the approver's name as the detail, and advance.
    }

    /** Runs a failed run again from its failed stage. Stages that succeeded are not run again. */
    public void resume(PipelineRun run) {
        // TODO 10: only a FAILED run may be resumed (IllegalStateException otherwise). Set the failed stage back
        // to PENDING, write RESUMED for it, and advance. advance skips every stage that already SUCCEEDED.
    }
}

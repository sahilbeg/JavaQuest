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

    /** Approves the run's waiting approval stage, then carries on, unless the approval has expired. */
    public void approve(PipelineRun run, String approver) {
        if (approver == null || approver.isBlank()) {
            throw new IllegalArgumentException("an approval needs the approver's name");
        }
        if (run.status() != RunStatus.WAITING_FOR_APPROVAL) {
            throw new IllegalStateException("the run is " + run.status() + ", not waiting for approval");
        }
        Stage gate = waitingStage(run);
        Instant now = clock.instant();
        Instant deadline = run.approvalRequestedAt().plus(Duration.ofMinutes(gate.approvalMinutes()));
        if (!now.isBefore(deadline)) {
            run.setStageStatus(gate.name(), StageStatus.FAILED);
            run.setStatus(RunStatus.FAILED);
            run.record(now, gate.name(), Decision.APPROVAL_EXPIRED,
                    approver + " approved after the approval expired at " + deadline);
            return;
        }
        run.setStageStatus(gate.name(), StageStatus.SUCCEEDED);
        run.record(now, gate.name(), Decision.APPROVED, approver);
        advance(run);
    }

    /** Runs a failed run again from its failed stage. Stages that succeeded are not run again. */
    public void resume(PipelineRun run) {
        if (run.status() != RunStatus.FAILED) {
            throw new IllegalStateException("only a FAILED run can be resumed, but this one is " + run.status());
        }
        for (Stage stage : pipeline.stages()) {
            if (run.stageStatus(stage.name()) == StageStatus.FAILED) {
                run.setStageStatus(stage.name(), StageStatus.PENDING);
                run.record(clock.instant(), stage.name(), Decision.RESUMED, "resumed from " + stage.name());
                break;
            }
        }
        advance(run);
    }

    private void advance(PipelineRun run) {
        run.setStatus(RunStatus.RUNNING);
        for (Stage stage : pipeline.stages()) {
            if (run.stageStatus(stage.name()) == StageStatus.SUCCEEDED) {
                continue;
            }
            switch (stage.type()) {
                case APPROVAL -> {
                    run.setStageStatus(stage.name(), StageStatus.WAITING);
                    run.setApprovalRequestedAt(clock.instant());
                    run.setStatus(RunStatus.WAITING_FOR_APPROVAL);
                    run.record(clock.instant(), stage.name(), Decision.APPROVAL_REQUESTED,
                            "expires after " + stage.approvalMinutes() + " minutes");
                    return;
                }
                case ANALYSIS -> {
                    AnalysisResult result = CanaryAnalysis.evaluate(stage, metrics.baseline(), metrics.canary());
                    if (!result.passed()) {
                        run.setStageStatus(stage.name(), StageStatus.FAILED);
                        run.record(clock.instant(), stage.name(), Decision.CANARY_FAILED, result.reason());
                        Stage canary = pipeline.canaryBefore(stage);
                        executor.rollBack(canary);
                        run.record(clock.instant(), canary.name(), Decision.ROLLED_BACK, "all traffic back on the old version");
                        run.setStatus(RunStatus.ROLLED_BACK);
                        return;
                    }
                    run.setStageStatus(stage.name(), StageStatus.SUCCEEDED);
                    run.record(clock.instant(), stage.name(), Decision.CANARY_PASSED, result.reason());
                }
                default -> {
                    StageResult result;
                    try {
                        result = executor.execute(stage);
                    } catch (RuntimeException e) {
                        result = StageResult.failed("threw " + e);
                    }
                    if (!result.succeeded()) {
                        run.setStageStatus(stage.name(), StageStatus.FAILED);
                        run.record(clock.instant(), stage.name(), Decision.STAGE_FAILED, result.detail());
                        run.setStatus(RunStatus.FAILED);
                        return;
                    }
                    run.setStageStatus(stage.name(), StageStatus.SUCCEEDED);
                    run.record(clock.instant(), stage.name(), Decision.STAGE_SUCCEEDED, result.detail());
                }
            }
        }
        run.setStatus(RunStatus.SUCCEEDED);
        run.record(clock.instant(), pipeline.name(), Decision.RUN_SUCCEEDED, "every stage succeeded");
    }

    private Stage waitingStage(PipelineRun run) {
        for (Stage stage : pipeline.stages()) {
            if (run.stageStatus(stage.name()) == StageStatus.WAITING) {
                return stage;
            }
        }
        throw new IllegalStateException("no stage is waiting for approval");
    }
}

package jq.m11.feature;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** One run of a pipeline: where it is, what each stage did, and its audit log. The engine changes it. */
public final class PipelineRun {

    private final Pipeline pipeline;
    private final Map<String, StageStatus> stages = new LinkedHashMap<>();
    private final List<AuditEntry> audit = new ArrayList<>();
    private RunStatus status = RunStatus.RUNNING;
    private Instant approvalRequestedAt;

    public PipelineRun(Pipeline pipeline) {
        this.pipeline = pipeline;
        for (Stage stage : pipeline.stages()) {
            stages.put(stage.name(), StageStatus.PENDING);
        }
    }

    public Pipeline pipeline() {
        return pipeline;
    }

    public RunStatus status() {
        return status;
    }

    public StageStatus stageStatus(String stageName) {
        StageStatus s = stages.get(stageName);
        if (s == null) {
            throw new IllegalArgumentException("no stage called " + stageName);
        }
        return s;
    }

    /** The audit log, oldest first. */
    public List<AuditEntry> audit() {
        return List.copyOf(audit);
    }

    /** When the current approval was asked for, or null. */
    public Instant approvalRequestedAt() {
        return approvalRequestedAt;
    }

    void setStatus(RunStatus status) {
        this.status = status;
    }

    void setStageStatus(String stageName, StageStatus status) {
        stageStatus(stageName);
        stages.put(stageName, status);
    }

    void setApprovalRequestedAt(Instant at) {
        this.approvalRequestedAt = at;
    }

    void record(Instant at, String stage, Decision decision, String detail) {
        audit.add(new AuditEntry(at, stage, decision, detail));
    }
}

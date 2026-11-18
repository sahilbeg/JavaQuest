package jq.m11.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PipelineEngineTest {

    private final TestClock clock = new TestClock();
    private final FakeExecutor executor = new FakeExecutor();
    private final FakeMetrics metrics = new FakeMetrics();
    private final PipelineEngine engine = new PipelineEngine(Pipelines.spoke(), executor, metrics, clock);

    private static List<Decision> decisions(PipelineRun run) {
        return run.audit().stream().map(AuditEntry::decision).toList();
    }

    private static AuditEntry last(PipelineRun run) {
        List<AuditEntry> audit = run.audit();
        assertTrue(!audit.isEmpty(), "the audit log should not be empty");
        return audit.get(audit.size() - 1);
    }

    private PipelineRun startAndApprove() {
        PipelineRun run = engine.start();
        clock.advance(Duration.ofMinutes(5));
        engine.approve(run, "kakashi");
        return run;
    }

    @Test
    @DisplayName("A run goes through the stages in order and pauses at the approval gate")
    void pausesAtApproval() {
        PipelineRun run = engine.start();
        assertEquals(Pipelines.BEFORE_APPROVAL, executor.executed,
                "start should run build, test, image, deploy-staging and smoke-check, in that order, then stop at the gate");
        assertEquals(RunStatus.WAITING_FOR_APPROVAL, run.status(), "the run should wait for approval");
        assertEquals(StageStatus.WAITING, run.stageStatus("approval"), "the approval stage is WAITING");
        assertEquals(StageStatus.SUCCEEDED, run.stageStatus("smoke-check"), "smoke-check ran and succeeded");
        assertEquals(StageStatus.PENDING, run.stageStatus("canary"), "the canary must not start before approval");
        assertEquals(TestClock.TEN_AM, run.approvalRequestedAt(), "the approval was asked for at 10:00, the clock's time");
    }

    @Test
    @DisplayName("Happy path: after approval, the canary gets 10%, analysis passes, and the release is promoted")
    void happyPath() {
        PipelineRun run = startAndApprove();
        List<String> all = new ArrayList<>(Pipelines.BEFORE_APPROVAL);
        all.addAll(List.of("canary", "promote"));
        assertEquals(all, executor.executed,
                "after approval the executor runs canary, then promote; the engine itself does the approval and the analysis");
        assertEquals(10, executor.stagesSeen.get(5).canaryPercent(), "the canary stage carries its 10% to the executor");
        assertEquals(RunStatus.SUCCEEDED, run.status(), "every stage succeeded, so the run SUCCEEDED");
        for (Stage stage : Pipelines.spoke().stages()) {
            assertEquals(StageStatus.SUCCEEDED, run.stageStatus(stage.name()), stage.name() + " should be SUCCEEDED");
        }
        assertTrue(executor.rolledBack.isEmpty(), "a healthy canary is never rolled back");
    }

    @Test
    @DisplayName("Every decision is in the audit log, in order, with the clock's time")
    void auditLog() {
        PipelineRun run = startAndApprove();
        assertEquals(List.of(
                        Decision.STAGE_SUCCEEDED, Decision.STAGE_SUCCEEDED, Decision.STAGE_SUCCEEDED,
                        Decision.STAGE_SUCCEEDED, Decision.STAGE_SUCCEEDED, Decision.APPROVAL_REQUESTED,
                        Decision.APPROVED, Decision.STAGE_SUCCEEDED, Decision.CANARY_PASSED,
                        Decision.STAGE_SUCCEEDED, Decision.RUN_SUCCEEDED),
                decisions(run), "the audit log should hold one entry per decision, oldest first");
        List<AuditEntry> audit = run.audit();
        assertEquals("build", audit.get(0).stage(), "the first entry is about the build stage");
        assertEquals(TestClock.TEN_AM, audit.get(0).at(), "entries before the approval were written at 10:00");
        AuditEntry approved = audit.get(6);
        assertEquals("approval", approved.stage(), "the APPROVED entry is about the approval stage");
        assertEquals("kakashi", approved.detail(), "the APPROVED entry's detail is who approved");
        assertEquals(TestClock.TEN_AM.plus(Duration.ofMinutes(5)), approved.at(), "kakashi approved at 10:05");
        assertEquals("spoke-api", last(run).stage(), "RUN_SUCCEEDED is written with the pipeline's name");
    }

    @Test
    @DisplayName("A failed stage stops the run: no later stage runs")
    void failedStageStops() {
        executor.fail("test", 1);
        PipelineRun run = engine.start();
        assertEquals(List.of("build", "test"), executor.executed, "after test fails, nothing else may run");
        assertEquals(RunStatus.FAILED, run.status(), "a failed stage fails the run");
        assertEquals(StageStatus.FAILED, run.stageStatus("test"), "test is FAILED");
        assertEquals(StageStatus.PENDING, run.stageStatus("image"), "image never ran, so it is still PENDING");
        AuditEntry entry = last(run);
        assertEquals(Decision.STAGE_FAILED, entry.decision(), "the last audit entry is STAGE_FAILED");
        assertEquals("test", entry.stage(), "STAGE_FAILED names the stage");
        assertEquals("test failed: 3 tests red", entry.detail(), "STAGE_FAILED keeps the executor's detail");
    }

    @Test
    @DisplayName("A stage that throws counts as a failed stage, and the exception's message goes in the audit log")
    void throwingStageFails() {
        executor.throwOn("image", new IllegalStateException("registry ghcr.io timed out"));
        PipelineRun run = assertDoesNotThrowRun();
        assertEquals(RunStatus.FAILED, run.status(), "an exception from the executor fails the run, it does not crash the engine");
        assertEquals(StageStatus.FAILED, run.stageStatus("image"), "image is FAILED");
        assertEquals(List.of("build", "test", "image"), executor.executed, "nothing runs after image");
        assertTrue(String.valueOf(last(run).detail()).contains("registry ghcr.io timed out"),
                "the STAGE_FAILED detail should contain the exception's message, but was: " + last(run).detail());
    }

    private PipelineRun assertDoesNotThrowRun() {
        try {
            return engine.start();
        } catch (RuntimeException e) {
            throw new AssertionError("start() must catch an exception from the executor and fail the stage, but it threw " + e, e);
        }
    }

    @Test
    @DisplayName("An approval just before it expires is accepted")
    void approvalJustInTime() {
        PipelineRun run = engine.start();
        clock.advance(Duration.ofMinutes(60).minusSeconds(1));
        engine.approve(run, "kakashi");
        assertEquals(StageStatus.SUCCEEDED, run.stageStatus("approval"), "approved at 10:59:59, before the 60 minutes ran out");
        assertEquals(RunStatus.SUCCEEDED, run.status(), "the run carries on to the end");
    }

    @Test
    @DisplayName("An approval at or after the expiry fails the run, and the canary never starts")
    void approvalExpired() {
        PipelineRun run = engine.start();
        clock.advance(Duration.ofMinutes(60));
        engine.approve(run, "kakashi");
        assertEquals(RunStatus.FAILED, run.status(), "at exactly 11:00 the 60-minute approval has expired, so the run FAILED");
        assertEquals(StageStatus.FAILED, run.stageStatus("approval"), "the approval stage is FAILED");
        assertEquals(StageStatus.PENDING, run.stageStatus("canary"), "the canary must not start on an expired approval");
        assertEquals(Pipelines.BEFORE_APPROVAL, executor.executed, "nothing runs after an expired approval");
        AuditEntry entry = last(run);
        assertEquals(Decision.APPROVAL_EXPIRED, entry.decision(), "the audit log says the approval expired");
        assertEquals(TestClock.TEN_AM.plus(Duration.ofMinutes(60)), entry.at(), "written at 11:00, when the late approval came");
    }

    @Test
    @DisplayName("Approving a run that is not waiting, or with no approver name, is refused")
    void approveGuards() {
        executor.fail("test", 1);
        PipelineRun failed = engine.start();
        assertThrows(IllegalStateException.class, () -> engine.approve(failed, "kakashi"),
                "a FAILED run is not waiting for approval; throw IllegalStateException");
        PipelineRun waiting = engine.start();
        assertEquals(RunStatus.WAITING_FOR_APPROVAL, waiting.status(), "the second run passes test and waits at the gate");
        assertThrows(IllegalArgumentException.class, () -> engine.approve(waiting, " "),
                "an approval must say who approved; throw IllegalArgumentException for a blank name");
        assertEquals(RunStatus.WAITING_FOR_APPROVAL, waiting.status(), "a refused approval changes nothing");
    }

    @Test
    @DisplayName("A canary with too many errors is rolled back automatically, and promote never runs")
    void rollBackOnErrors() {
        metrics.canary(1.75, 200);
        PipelineRun run = startAndApprove();
        assertEquals(RunStatus.ROLLED_BACK, run.status(), "the canary's error rate is 0.75 points above baseline, so the run is ROLLED_BACK");
        assertEquals(List.of("canary"), executor.rolledBack, "the engine must call rollBack with the canary stage");
        assertEquals(StageStatus.FAILED, run.stageStatus("analysis"), "the analysis stage is FAILED");
        assertEquals(StageStatus.PENDING, run.stageStatus("promote"), "promote must never run after a failed analysis");
        assertEquals(0, executor.runs("promote"), "promote must never run after a failed analysis");
        List<Decision> d = decisions(run);
        assertEquals(List.of(Decision.CANARY_FAILED, Decision.ROLLED_BACK), d.subList(d.size() - 2, d.size()),
                "the audit log ends with CANARY_FAILED, then ROLLED_BACK");
        assertTrue(String.valueOf(run.audit().get(d.size() - 2).detail()).contains("error rate"),
                "the CANARY_FAILED entry's detail should give the reason");
        assertEquals("canary", last(run).stage(), "ROLLED_BACK names the canary stage");
    }

    @Test
    @DisplayName("Analysis uses the YAML's thresholds: a p99 of 240 ms is promoted and 241 ms is rolled back")
    void analysisThresholdsAtTheBoundary() {
        metrics.canary(1.5, 240);
        PipelineRun atLimit = startAndApprove();
        assertEquals(RunStatus.SUCCEEDED, atLimit.status(), "1.5% errors and 240 ms are exactly at the limits, so the release is promoted");

        FakeExecutor second = new FakeExecutor();
        PipelineEngine engine2 = new PipelineEngine(PipelineLoader.load(Pipelines.SPOKE_YAML), second,
                new FakeMetrics().canary(1.0, 241), clock);
        PipelineRun over = engine2.start();
        engine2.approve(over, "kakashi");
        assertEquals(RunStatus.ROLLED_BACK, over.status(), "241 ms is above 240 ms, so the canary is rolled back");
        assertEquals(List.of("canary"), second.rolledBack, "rollBack is called with the canary stage");
    }

    @Test
    @DisplayName("Resume runs a failed run again from the failed stage, without re-running stages that succeeded")
    void resumeFromFailedStage() {
        executor.fail("smoke-check", 1);
        PipelineRun run = engine.start();
        assertEquals(RunStatus.FAILED, run.status(), "smoke-check failed once, so the run FAILED");
        engine.resume(run);
        assertEquals(1, executor.runs("build"), "build already succeeded, so resume must not run it again");
        assertEquals(1, executor.runs("deploy-staging"), "deploy-staging already succeeded, so resume must not run it again");
        assertEquals(2, executor.runs("smoke-check"), "resume runs the failed smoke-check again");
        assertEquals(RunStatus.WAITING_FOR_APPROVAL, run.status(), "after smoke-check passes, the run waits at the gate");
        List<AuditEntry> audit = run.audit();
        AuditEntry resumed = audit.stream().filter(e -> e.decision() == Decision.RESUMED).findFirst()
                .orElseThrow(() -> new AssertionError("resume should write a RESUMED entry to the audit log"));
        assertEquals("smoke-check", resumed.stage(), "the RESUMED entry names the stage the run resumed from");
    }

    @Test
    @DisplayName("Resume after an expired approval asks for approval again, with a new expiry")
    void resumeAfterExpiry() {
        PipelineRun run = engine.start();
        clock.advance(Duration.ofMinutes(90));
        engine.approve(run, "kakashi");
        assertEquals(RunStatus.FAILED, run.status(), "the approval expired");
        engine.resume(run);
        assertEquals(RunStatus.WAITING_FOR_APPROVAL, run.status(), "resume should ask for approval again");
        Instant again = TestClock.TEN_AM.plus(Duration.ofMinutes(90));
        assertEquals(again, run.approvalRequestedAt(), "the new approval was asked for at 11:30, so it expires at 12:30");
        assertEquals(Pipelines.BEFORE_APPROVAL, executor.executed, "no stage before the gate runs again");
        clock.advance(Duration.ofMinutes(10));
        engine.approve(run, "itachi");
        assertEquals(RunStatus.SUCCEEDED, run.status(), "approved at 11:40, within the new 60 minutes, so the release goes out");
    }

    @Test
    @DisplayName("Only a FAILED run can be resumed: not one that is waiting, succeeded or rolled back")
    void resumeGuards() {
        PipelineRun waiting = engine.start();
        assertThrows(IllegalStateException.class, () -> engine.resume(waiting),
                "a run waiting for approval cannot be resumed; throw IllegalStateException");
        engine.approve(waiting, "kakashi");
        assertThrows(IllegalStateException.class, () -> engine.resume(waiting),
                "a SUCCEEDED run cannot be resumed; throw IllegalStateException");
        assertEquals(1, executor.runs("promote"), "resuming a finished run must not promote again");

        PipelineEngine bad = new PipelineEngine(Pipelines.spoke(), new FakeExecutor(), new FakeMetrics().canary(5.0, 200), clock);
        PipelineRun rolledBack = bad.start();
        bad.approve(rolledBack, "kakashi");
        assertThrows(IllegalStateException.class, () -> bad.resume(rolledBack),
                "a ROLLED_BACK release is final: ship a fix in a new run; throw IllegalStateException");
    }
}

# FEAT-11: One-click deploys

Shipping `spoke-api` today means following a 14-step wiki page by hand. Jiraiya wants any engineer to ship safely with one click. Build the engine behind that button, in package `jq.m11.feature`. It reads a pipeline from YAML and runs its stages in order. It waits for an approval, checks a canary, and promotes or rolls back on its own. Every decision goes into an audit log.

Ready to use: `StageType`, `Stage`, `Pipeline`, `PipelineException`, `StageResult`, `StageExecutor`, `Metrics`, `MetricsSource`, `AnalysisResult`, `Decision`, `AuditEntry`, `StageStatus`, `RunStatus` and `PipelineRun`. Fill in the numbered TODOs in `PipelineLoader`, `CanaryAnalysis` and `PipelineEngine`. Read the time only from the engine's `Clock`.

Spoke's pipeline:

```yaml
name: spoke-api
stages:
  - name: build
    type: build
  - name: test
    type: test
  - name: image
    type: image
  - name: deploy-staging
    type: deploy
  - name: smoke-check
    type: smoke
  - name: approval
    type: approval
    expiresAfterMinutes: 60
  - name: canary
    type: canary
    percent: 10
  - name: analysis
    type: analysis
    maxErrorRateIncrease: 0.5
    maxP99IncreasePercent: 20
  - name: promote
    type: promote
```

## 1. `PipelineLoader.load(String yaml)` (TODOs 1 to 3)

Parse with SnakeYAML's safe loader, `new Yaml(new SafeConstructor(new LoaderOptions())).load(yaml)`, and return a `Pipeline`. Every problem throws `PipelineException`, and its message **contains** the word given here:

| Problem | The message contains |
|---|---|
| `null` or blank text | `empty` |
| text that is not valid YAML (SnakeYAML throws a `YAMLException`) | `YAML` |
| the top level is not a map | `name` |
| `name` missing, not text, or blank | `name` |
| `stages` missing, not a list, or an empty list | `stages` |
| stage number N (counting from 1) is not a map, or has no `name` | `stage N`, for example `stage 1` |
| a stage has no `type` | `type` |
| a stage's `type` is not one of the nine below | the stage's name **and** the unknown type, such as `build` and `compile` |
| two stages have the same name | that name |
| a `canary` stage's `percent` is missing, not a whole number, or outside 1 to 50 | `percent` |
| an `approval` stage's `expiresAfterMinutes` is missing, not a whole number, or below 1 | `expiresAfterMinutes` |
| an `analysis` stage has no `canary` stage before it | `canary` |
| an `analysis` stage's `maxErrorRateIncrease` is missing, not a number, or below 0 | `maxErrorRateIncrease` |
| an `analysis` stage's `maxP99IncreasePercent` is missing, not a whole number, or below 0 | `maxP99IncreasePercent` |

- The types are written in lowercase: `build`, `test`, `image`, `deploy`, `smoke`, `approval`, `canary`, `analysis`, `promote`. They map to `StageType` by name.
- "A whole number" means SnakeYAML gave you an `Integer`: `10` is one, `10.5` and `ten` are not. `maxErrorRateIncrease` may be a whole number or a decimal, so `1` means `1.0`.
- Make each stage with `Stage.of(name, type)`, `Stage.canary(name, percent)`, `Stage.approval(name, minutes)` or `Stage.analysis(name, maxErrorRateIncrease, maxP99IncreasePercent)`. Keep the file's order. Ignore keys you do not need.

## 2. `CanaryAnalysis.evaluate(analysis, baseline, canary)` (TODO 4)

The baseline is the old version, measured over the same minutes as the canary. The canary **fails** when:

1. `canary.errorRatePercent() - baseline.errorRatePercent()` is greater than `analysis.maxErrorRateIncrease()`. The reason contains `error rate`.
2. Or `canary.p99Millis() * 100` is greater than `baseline.p99Millis() * (100 + analysis.maxP99IncreasePercent())`. The reason contains `p99`.

Check them in that order. Otherwise it passes. **Exactly at a limit passes.** Take a baseline of 1.0% and 200 ms, with limits of 0.5 points and 20%. A canary at 1.5% and 240 ms passes. One at 1.75%, or at 241 ms, fails.

## 3. `PipelineEngine` (TODOs 5 to 10)

`new PipelineEngine(pipeline, executor, metrics, clock)`. `start()` makes a new `PipelineRun`, moves it as far as it can go, and returns it. A run starts `RUNNING` with every stage `PENDING`.

**Moving a run forward** (TODOs 5 to 8). Set the run `RUNNING`, then go through the stages in order. A stage runs only if every stage before it has succeeded:

- A stage already `SUCCEEDED` is skipped. It is never run twice.
- **`APPROVAL`**: set the stage `WAITING`, save the clock's time with `setApprovalRequestedAt`, set the run `WAITING_FOR_APPROVAL`, write `APPROVAL_REQUESTED`, and stop.
- **`ANALYSIS`**: call `CanaryAnalysis.evaluate(stage, metrics.baseline(), metrics.canary())`.
  - Passed: set the stage `SUCCEEDED`, write `CANARY_PASSED` with the reason as its detail, and go on.
  - Failed: set the stage `FAILED`, write `CANARY_FAILED` with the reason. Then call `executor.rollBack(pipeline.canaryBefore(stage))`, write `ROLLED_BACK` **for the canary stage**, set the run `ROLLED_BACK`, and stop. Promote never runs.
- **Any other type**: call `executor.execute(stage)`.
  - Succeeded: set the stage `SUCCEEDED`, write `STAGE_SUCCEEDED` with the result's detail, and go on.
  - Failed, or `execute` threw: set the stage `FAILED`, write `STAGE_FAILED`, set the run `FAILED`, and stop. The detail is the result's detail, or for an exception, text that contains the exception's message. The engine never lets the exception out.
- After the last stage: set the run `SUCCEEDED` and write `RUN_SUCCEEDED`, with **the pipeline's name** as its stage.

**`approve(run, approver)`** (TODO 9):

- A `null` or blank `approver` throws `IllegalArgumentException`. A run that is not `WAITING_FOR_APPROVAL` throws `IllegalStateException`. Neither changes the run.
- The approval expires `approvalMinutes` after `run.approvalRequestedAt()`. If the clock is **at or after** that moment, the approval has expired: set the stage `FAILED` and the run `FAILED`, and write `APPROVAL_EXPIRED`. Nothing else runs.
- Otherwise set the stage `SUCCEEDED`, write `APPROVED` with **the approver's name as the detail**, and move the run forward.

**`resume(run)`** (TODO 10):

- Only a `FAILED` run can be resumed. A run that is waiting, succeeded or rolled back throws `IllegalStateException`. A rolled-back release is final: the fix ships in a new run.
- Set the failed stage back to `PENDING`, write `RESUMED` for that stage, and move the run forward. Stages that succeeded are skipped, so resuming never repeats work. A failed approval asks for approval again, with a new time and so a new expiry.

**The audit log.** Write each entry with `run.record(clock.instant(), stageName, decision, detail)`. The stage is the stage the decision is about. For Spoke's pipeline, a run approved by `kakashi` writes these decisions, in this order:

`STAGE_SUCCEEDED` (build), `STAGE_SUCCEEDED` (test), `STAGE_SUCCEEDED` (image), `STAGE_SUCCEEDED` (deploy-staging), `STAGE_SUCCEEDED` (smoke-check), `APPROVAL_REQUESTED` (approval), `APPROVED` (approval, detail `kakashi`), `STAGE_SUCCEEDED` (canary), `CANARY_PASSED` (analysis), `STAGE_SUCCEEDED` (promote), `RUN_SUCCEEDED` (spoke-api).

## How the checks work

A fake `StageExecutor` records each stage it runs and can fail or throw on command. A fake `MetricsSource` gives a baseline of 1.0% errors and 200 ms, and a canary the check chooses. A fake clock starts at 10:00 and moves only when a check moves it.

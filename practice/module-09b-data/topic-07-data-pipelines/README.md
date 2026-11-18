# Data pipelines: a mini DAG scheduler

Every night someone runs three scripts by hand to build yesterday's ride report. When one fails, nobody notices. Build `MiniScheduler`, a tiny Airflow: it checks a DAG of tasks, runs them in a valid order for a **logical date**, retries failures, skips tasks whose upstream failed, backfills missing days and repairs a failed day.

No class reads the clock or sleeps. Dates are `LocalDate` values that the checks pass in. Retry waits are written down, never slept.

Given to you, finished:

- `TaskAction`: the work of one task. `void run(LocalDate logicalDate) throws Exception`. Any exception means that attempt failed.
- `TaskSpec(name, upstream, maxRetries, retryDelay, action)`: one task. `TaskSpec.of("load_report", action, "extract_rides")` makes a task with no retries that needs `extract_rides` first. `.withRetries(2, Duration.ofMinutes(5))` allows 2 retries, 5 minutes apart.
- `TaskState`: `NONE` (not run for that date yet), `SUCCESS`, `FAILED`, `UPSTREAM_FAILED`.
- `RetryWait(task, logicalDate, delay)`: one recorded wait before a retry.
- `InvalidDagException`: an `IllegalArgumentException` for a DAG that cannot run.

Start with `Dag`: the scheduler runs nothing until `order()` works.

## 1. `Dag(id, tasks)`: check the DAG once, when it is made

Throw `InvalidDagException` with a clear message when:

- **TODO 1:** the task list is empty.
- **TODO 2:** two tasks have the same name. Name the duplicate in the message.
- **TODO 3:** a task lists an upstream name that is not a task of this DAG. Name **both** the task and the unknown name, for example `task load_report depends on unknown task extract_ride`.
- **TODO 6:** there is a cycle, including a task that depends on itself. Name every task that can never start, and no other task. Kahn's algorithm finds exactly these: they are the tasks it never adds to the order.

**TODO 4 and 5, `order()`:** every task name, in the order the scheduler runs them. Use Kahn's algorithm from the topological sort ticket in module 04: a task is ready when all its upstream tasks are placed.

**The tie-break rule:** whenever more than one task is ready, the next one is the ready task whose name comes first alphabetically (`String.compareTo`). This is checked among **all** ready tasks, including ones that just became ready. Ready `b_extract` and `z_vacuum`: take `b_extract`. That makes `a_clean` ready, and `a_clean` goes before `z_vacuum`. A `PriorityQueue<String>` does exactly this.

## 2. `MiniScheduler(dag)`

`state(task, date)`, `attempts(task, date)`, `waits()` and `record(...)` are given.

### TODO 10 and 11, `runOne(name, date)`: one task for one date

- If **any** upstream task is not `SUCCESS` for this date, record `UPSTREAM_FAILED` and return. Never call its action. This passes down the chain: a task below an `UPSTREAM_FAILED` task is `UPSTREAM_FAILED` too.
- Otherwise call `task.action().run(date)`. Count **every** call in `attempts`. Attempts add up over all runs of that date.
- It returns normally: record `SUCCESS`.
- It throws any `Exception` (checked ones too, like `IOException`): if this was attempt `1 + maxRetries`, record `FAILED`. Otherwise add `new RetryWait(name, date, task.retryDelay())` to `waits`, then try again. So `maxRetries` 2 means at most 3 attempts and 2 waits. There is never a wait after the last attempt.

### TODO 7, `run(date)`

Call `runOne` for every task in `dag.order()`. Return `true` when every task ended `SUCCESS`. A failure does not stop tasks on other branches: a task that does not depend on the failed one still runs.

### TODO 12, `succeeded(date)`

`true` only when every task of the DAG is `SUCCESS` for that date. A date that never ran is not succeeded.

### TODO 8, `backfill(from, to)`

- Throw `IllegalArgumentException` when `from` is after `to`. `from` equal to `to` is one date.
- Go through every date from `from` to `to`, **both included**, oldest first.
- Skip a date that already `succeeded`. Give every other date a full `run(date)`, even if that date failed before.
- A failing date does not stop the backfill: carry on with the next date.
- Return the dates you ran, in order. A second identical backfill after a fully successful one runs nothing and returns an empty list.

### TODO 9, `rerunFailed(date)`

- Throw `IllegalStateException` when the DAG never ran for that date.
- Call `runOne`, in `dag.order()`, only for tasks whose state is `FAILED` or `UPSTREAM_FAILED`. Leave `SUCCESS` tasks alone: never call them again.
- Return `succeeded(date)`.

Press **Submit** in JavaQuest, or just save the file in your IDE. The checks run on their own.

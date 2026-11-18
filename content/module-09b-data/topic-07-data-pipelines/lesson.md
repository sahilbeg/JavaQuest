## Hook

A bakery makes bread every night. The dough must rise before it is shaped, and shaped before it is baked. If the oven breaks, nobody sells yesterday's raw dough as bread. They notice, fix the oven and bake that batch again.

Spoke's nightly ride report has no such baker. Someone runs three scripts by hand. When one fails, the next one runs anyway on half the data, and the morning numbers are quietly wrong.

In this ticket you learn how data teams run work like this every night. It runs in order, is safe to retry, and is loud when something is wrong.

## Concept

### What a data pipeline is

```callout jargon
A **data pipeline** is a chain of steps that moves data from where it is made to where it is used, changing it on the way. Each step is a **job** or **task**.
```

Spoke's production database, Postgres, holds every rental. It is built for many small, fast reads and writes: "start rental-17", "end rental-17". Jiraiya wants something different: "fares per bike type, per day, for the last year". That question reads millions of rows.

Asking it of the production database at 9 a.m. would slow every rider's unlock. So the data is copied, every night, into a separate place built for big questions. The steps:

1. **Extract** yesterday's rides from Postgres.
2. **Clean** them: drop test rides, fix bad bike types.
3. **Load** one row per bike type into the `daily_ride_report` table.
4. **Check** the totals add up.

Today three of those are scripts that a person runs by hand, in the right order, hopefully. The rest of this page is how to make them run themselves.

### Batch and streaming

There are two ways to process data.

```callout jargon
**Batch** processing works on a fixed, finished set of data in one go, such as "all of yesterday's rides". **Stream processing** handles each event as it arrives, forever, such as "a GPS ping every 10 seconds".
```

| | Batch | Streaming |
|---|---|---|
| Data | a finished chunk: an hour, a day | an endless flow of events |
| Answer arrives | minutes to hours later | within seconds |
| A failed run | run that chunk again | must resume exactly where it stopped |
| Typical tools | SQL in a warehouse, Spark, an orchestrator like Airflow | Kafka Streams, Apache Flink, Spark Structured Streaming |
| Spoke example | yesterday's fare report | "bikes in use right now" on the live map |

Pick batch unless someone needs the answer within minutes. Batch is simpler to build, test and rerun. Nobody needs last year's fare totals within 2 seconds. The live map does need fresh positions, so it reads the `ride-events` stream from [Messaging](/t/module-09-architecture/topic-04-messaging).

Many companies run both. A big-data pattern called the **Lambda architecture** does exactly that: a fast stream for fresh, rough numbers, plus a nightly batch for exact ones. The **Kappa architecture** keeps only the stream, and replays it to recompute.

### ETL and ELT

```callout jargon
**ETL** means extract, transform, load: take data out, change it on the way, then store the result. **ELT** means extract, load, transform: store the raw data first, then change it inside the destination, usually with SQL.
```

Old-style ETL cleaned the data in a separate program before loading. If you later wanted a new column, the raw data was gone. You had to extract everything again.

Modern teams mostly do **ELT**. They copy raw rides into the warehouse untouched. Then SQL turns raw tables into clean ones, and clean ones into reports. A popular tool for that SQL step is **dbt**. Each model is a `SELECT` saved in a file, and dbt builds the tables in the right order.

Why the switch? Warehouses became cheap to store in and fast to compute in. And keeping the raw data means any mistake in a transform can be fixed by running it again.

### Warehouses and lakes

```callout jargon
A **data warehouse** is a database built for big analytic questions over many rows, such as BigQuery, Snowflake or Amazon Redshift. A **data lake** is cheap file storage, such as Amazon S3, holding raw and cleaned data as files.
```

Warehouses store data **by column**, not by row. "Sum of fares" then reads only the fare column, not every field of every ride. That makes big totals fast.

A lake keeps files, often in **Parquet**, a file format that also stores data by column and compresses it well. Engines such as Spark, Trino or the warehouse itself can query those files.

| | Production Postgres | Warehouse | Lake |
|---|---|---|---|
| Built for | many tiny reads and writes | big questions with SQL | storing lots of data cheaply |
| Spoke example | "end rental-17 now" | "fares per day for a year" | raw ride files kept for 5 years |

A common setup puts raw Parquet files in S3 and queries them from the warehouse. That mix is called a **lakehouse**. Whatever the setup, the rule stays: reports never run on the production database.

### Partitioning data by date

```callout jargon
A **partition** is one slice of a table or folder, usually one day of data. A query that filters on the partition column reads only the slices it needs. Skipping the others is called **partition pruning**.
```

In the lake, each day is its own folder:

```text
s3://spoke-lake/rides/ride_date=2026-11-16/part-0.parquet
s3://spoke-lake/rides/ride_date=2026-11-17/part-0.parquet
s3://spoke-lake/rides/ride_date=2026-11-18/part-0.parquet
```

In BigQuery or Snowflake, you declare the table partitioned by `ride_date` instead. Either way, "rides on 17 November" opens one slice, not five years of rides.

Date partitions do a second job. **One nightly run owns exactly one partition.** The run for 2026-11-17 writes the 2026-11-17 slice and nothing else. That one rule makes the next card possible.

### Idempotent jobs: overwrite, never append

You met **idempotent** in [Messaging](/t/module-09-architecture/topic-04-messaging): doing it twice has the same effect as doing it once. Pipelines need it even more than consumers do, because every job will run twice one day. A timeout, a retry or a fix and a rerun all repeat it.

Picture the load step. It writes the report rows, then the connection times out before it says "done". The scheduler cannot know the rows landed, so it retries.

```animate
title: Append twice, overwrite twice
kind: code
code: |
  long appendedCents = 0;
  appendedCents = appendedCents + 1460;
  appendedCents = appendedCents + 1460;
  System.out.println("append: " + appendedCents);
  long overwrittenCents = 0;
  overwrittenCents = 1460;
  overwrittenCents = 1460;
  System.out.println("overwrite: " + overwrittenCents);
steps:
  - line: 1
    vars: { appendedCents: 0 }
    say: The report's fare total for 2026-11-17 starts empty.
  - line: 2
    vars: { appendedCents: 1460 }
    say: The load job appends the day's fares, 1,460 cents. Then the connection times out before the job reports success.
  - line: 3
    vars: { appendedCents: 2920 }
    say: The scheduler retries, as it should. Appending again adds the same rides a second time.
  - line: 4
    out: "append: 2920"
    say: The report now says Spoke earned double. Nobody gets an error.
  - line: 6
    vars: { overwrittenCents: 1460 }
    say: The idempotent job replaces the whole 2026-11-17 partition with the day's rows.
  - line: 7
    vars: { overwrittenCents: 1460 }
    say: The retry replaces it again, with the same rows. Nothing changes.
  - line: 8
    out: "overwrite: 1460"
    say: Run it once, twice or ten times. The answer is always 1,460.
```

The rule: **a daily job replaces its whole partition, never appends to it.** Its output depends only on its input data and its date, not on how many times it ran.

### Syntax: overwrite one partition

```sql
BEGIN;
DELETE FROM daily_ride_report WHERE ride_date = DATE '2026-11-17';
INSERT INTO daily_ride_report (ride_date, bike_type, rides, fare_cents)
SELECT ride_date, bike_type, COUNT(*), SUM(fare_cents)
FROM clean_rides
WHERE ride_date = DATE '2026-11-17'
GROUP BY ride_date, bike_type;
COMMIT;
```

Delete the day's rows and insert the new ones in **one transaction**. A reader then sees the old day or the new day, never an empty gap. The date is always a parameter of the job, never "today".

Warehouses have shortcuts for the same idea. BigQuery and Spark can overwrite one partition in a single statement. Many teams use `MERGE`, an upsert keyed by date and bike type, for the same effect.

### Backfills

```callout jargon
A **backfill** runs a job again for past dates: to fill days it missed, or to rebuild days after fixing a bug.
```

Say the clean step dropped every CARGO ride for two weeks. You fix the bug. Now 1 to 14 November are wrong. A backfill reruns the pipeline for those 14 logical dates.

A backfill is only safe if the jobs are idempotent and take their date as a parameter. Then rebuilding 7 November is simply "run the job for 7 November". It replaces that partition and touches nothing else.

Three habits keep backfills calm:

- **Oldest first.** Some jobs read the day before, such as "riders active this week".
- **Limit how many run at once.** Fourteen days in parallel can swamp the source database. Two or three at a time is kinder.
- **Skip what already worked.** A day whose run fully succeeded does not need to run again, unless you are fixing it.

### Late data and watermarks

Some data arrives late. A bike that was offline overnight syncs its last ride at 07:00. A phone with no signal sends "ride ended" an hour after the ride ended.

```callout jargon
**Event time** is when something really happened. **Processing time** is when your code sees it. A **watermark** is a stream's estimate: "all events before this event time have probably arrived".
```

A batch job copes in two ways. It waits: the 2026-11-17 run starts at 02:00 on the 18th, giving stragglers two hours. Or it looks back: every night it also rebuilds the last three days, which overwriting makes safe.

A stream keeps a watermark, often "the latest event time seen, minus 5 minutes". When the watermark passes the end of a time window, that window's count is final. An event older than the watermark is **late**. It is dropped, or written to a separate late-data table for the next batch run to fix. The Try it section lets you move the watermark.

### Why cron is not enough

```callout jargon
**cron** is the classic Unix scheduler. A line such as `0 2 * * *` runs a command at 02:00 every day.
```

The obvious fix for Spoke is three cron lines: extract at 02:00, clean at 02:30, load at 03:00. That is how many teams start. Here is why it breaks:

- **No dependencies.** Clean starts at 02:30 whether extract finished or not. A slow night means clean reads half the data.
- **No retries.** A timeout at 02:01 is final.
- **No memory.** Nothing records which dates succeeded, so nobody knows what to rerun.
- **No backfills.** Rerunning last week means hand-editing dates in scripts.
- **Silent failures.** A failed job writes to a log file nobody reads. The morning numbers are just wrong.

Those five gaps are exactly Jiraiya's ticket. An orchestrator fills all of them.

### DAGs and how a scheduler orders them

```callout jargon
A **DAG** (directed acyclic graph) is a set of tasks with arrows that say "this must finish first", and no loops. An **orchestrator** is a program that runs DAGs on a schedule, in order, and records what happened.
```

You ordered jobs like this in [Topological sort](/t/module-04-dsa/topic-15-topological-sort). An orchestrator uses the same Kahn's algorithm: a task is **ready** when every task upstream of it has succeeded. A loop would mean no task could ever start, so an orchestrator refuses a DAG with a cycle.

```callout jargon
**Upstream** tasks must finish before a task starts. **Downstream** tasks wait for it. When a task fails for good, its downstream tasks are marked **upstream_failed** and never run.
```

```animate
title: One night of daily_ride_report
kind: scene
nodes:
  - { id: sched, label: Scheduler, emoji: "🗓️", value: "2026-11-17", row: 0, col: 0 }
  - { id: extract, label: extract_rides, emoji: "📥", value: waiting, row: 0, col: 1 }
  - { id: clean, label: clean_rides, emoji: "🧹", value: waiting, row: 0, col: 2 }
  - { id: load, label: load_report, emoji: "📊", value: waiting, row: 0, col: 3 }
  - { id: archive, label: archive_logs, emoji: "🗄️", value: waiting, row: 1, col: 1 }
links:
  - { from: extract, to: clean }
  - { from: clean, to: load }
steps:
  - say: The night of the 18th, the scheduler starts a run for logical date 2026-11-17. extract_rides and archive_logs have no upstream, so both are ready.
    focus: [extract, archive]
  - say: extract_rides times out on its first try. It has retries left, so it is up for retry in 5 minutes. archive_logs succeeds meanwhile.
    send: { from: sched, to: extract, label: "attempt 1" }
    set: { extract: "up for retry", archive: success }
  - say: The second attempt works. Now clean_rides is ready, because its only upstream succeeded.
    send: { from: sched, to: extract, label: "attempt 2" }
    set: { extract: success }
  - say: clean_rides fails three times, its first try and two retries. It is failed for good.
    send: { from: sched, to: clean, label: "attempts 1-3" }
    set: { clean: failed }
  - say: load_report is never called. Building a report from uncleaned rides is how wrong numbers reach the morning meeting. It is marked upstream_failed.
    set: { load: upstream_failed }
    focus: [load]
  - say: The run is failed, and an alert fires. In the morning, someone fixes clean_rides and reruns only the failed tasks of 2026-11-17.
    set: { sched: "run failed" }
```

### Airflow: DAGs, tasks and operators

**Apache Airflow** is the most widely used orchestrator. It started at Airbnb in 2014 and is free and open source. Managed versions exist too: Google Cloud Composer and Amazon MWAA.

You describe a DAG in a **Python** file. Airflow's parts:

- The **scheduler** reads the DAG files, decides which runs and tasks are due, and queues them.
- **Workers** run the tasks.
- The **metadata database** records every DAG run and every task's state.
- The **web UI** shows the grid of runs, logs and retries, and has buttons to clear and rerun.

```callout jargon
An **operator** is a template for one kind of task: `PythonOperator` calls a Python function, `BashOperator` runs a shell command, and SQL operators run a query. A **task** is one use of an operator inside a DAG, with its own `task_id`.
```

Dependencies use `>>`, read "then": `extract >> clean` means clean runs after extract. `extract >> [count, total]` fans out to two tasks that can run side by side.

By default a task runs only when **all** its upstream tasks succeeded. Its `trigger_rule` can change that. `one_failed`, for example, suits a "send an alert" task.

### Syntax: an Airflow DAG

```python
from datetime import datetime, timedelta

from airflow import DAG
from airflow.operators.python import PythonOperator

from spoke_jobs import extract_rides, clean_rides, load_report, check_totals

with DAG(
    dag_id="daily_ride_report",
    schedule="@daily",                     # one run per day of data
    start_date=datetime(2026, 11, 1),      # the first logical date
    catchup=False,                         # do not create runs for missed days on its own
    max_active_runs=1,                     # one date at a time
    default_args={
        "retries": 2,                          # 3 attempts in all
        "retry_delay": timedelta(minutes=5),
        "execution_timeout": timedelta(minutes=30),
    },
) as dag:
    extract = PythonOperator(task_id="extract_rides", python_callable=extract_rides)
    clean = PythonOperator(task_id="clean_rides", python_callable=clean_rides)
    load = PythonOperator(task_id="load_report", python_callable=load_report, pool="warehouse")
    check = PythonOperator(task_id="check_totals", python_callable=check_totals)

    extract >> clean >> load >> check
```

This is Airflow 2 style, still the most common in companies. Airflow 3 moved the imports: `DAG` comes from `airflow.sdk`, and `PythonOperator` from `airflow.providers.standard.operators.python`. The ideas are the same.

`default_args` apply to every task. A task's own arguments, like `pool="warehouse"`, add to them or override them.

### The logical date is not the run time

```callout jargon
The **logical date** of a run is the day of data it processes. In older Airflow it was called the **execution date**. The **data interval** is the stretch of time that data covers.
```

A daily run cannot report on 17 November until 17 November is over. So the run for logical date 2026-11-17 covers 00:00 on the 17th to 00:00 on the 18th, and starts just after.

```java run
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        LocalDate logicalDate = LocalDate.of(2026, 11, 17);
        LocalDateTime intervalStart = logicalDate.atStartOfDay();
        LocalDateTime intervalEnd = intervalStart.plusDays(1);

        System.out.println("logical date:  " + logicalDate);
        System.out.println("data interval: " + intervalStart + " to " + intervalEnd);
        System.out.println("earliest run:  " + intervalEnd);

        LocalDate backfilled = LocalDate.of(2026, 11, 3);   // rebuilt today, during a backfill
        System.out.println("a backfill of " + backfilled + " still reads rides from " + backfilled.atStartOfDay()
                + " to " + backfilled.plusDays(1).atStartOfDay());
    }
}
```

So every task reads its date from the run, never from the clock. In a PythonOperator, the parameter `ds` holds it as text, such as `"2026-11-17"`. Call `datetime.now()` inside a task, and a backfill of 3 November quietly processes today instead.

```callout warning
Airflow 3 changed the default for time-based schedules: the logical date is the moment the run was due, with no data interval. Teams that want "the day before" pick an interval timetable or compute it. Check which one your Airflow uses.
```

### Catchup and backfill in Airflow

```callout jargon
**Catchup** means the scheduler creates a run for every interval between `start_date` and now that has not run yet.
```

Say you deploy `daily_ride_report` on 18 November, with `start_date` 1 November.

- With `catchup=True`, Airflow at once creates 17 runs, one per logical date from 1 to 17 November. That was the default in Airflow 2.
- With `catchup=False`, it creates only the latest one. That is the default in Airflow 3.

Leave catchup on with a `start_date` a year ago, and 365 runs flood the source database. `max_active_runs` caps how many run at once.

For a deliberate rebuild, ask for a backfill:

```bash
airflow dags backfill --start-date 2026-11-01 --end-date 2026-11-14 daily_ride_report
```

That is the Airflow 2 command. Airflow 3 uses `airflow backfill create`, or the Backfill button in the UI. Either way, each date becomes its own run with its own logical date.

To repair a single bad night, **clear** its failed tasks in the UI. Airflow reruns them and everything downstream. Tasks that succeeded stay as they are.

### Retries and timeouts

A task's settings decide what one bad attempt costs:

| Setting | What it does |
|---|---|
| `retries` | extra attempts after the first; 2 means 3 in all |
| `retry_delay` | the wait before each retry, such as 5 minutes |
| `retry_exponential_backoff` | doubles the wait each time, as in [Resilience](/t/module-09-architecture/topic-09-resilience) |
| `execution_timeout` | a task running longer than this is stopped and counts as failed |
| `dagrun_timeout` | the same limit, for a whole DAG run |

Retries cure blips: a timeout, a database restart, a busy API. They cannot cure a bug, so cap them, two or three is common. Retries are only safe because each task is idempotent.

Older Airflow also had an **SLA** setting: "this task should be done by 06:00". Missing it only sent an alert; it never stopped the task. Airflow 3 removed SLAs and is replacing them with deadline alerts.

```callout jargon
An **SLA** (service level agreement) is a promise about time or quality, such as "the ride report is ready by 06:00 every day".
```

### Sensors and pools

```callout jargon
A **sensor** is a task that waits for something to be true, such as "a file exists" or "another DAG's run succeeded", then succeeds. A **pool** is a named set of slots that limits how many tasks may use one resource at once.
```

Spoke's payments partner drops a settlement file in S3 at some time each night. A sensor checks every 5 minutes, and the reconcile task starts when the file lands. Give a sensor a `timeout`, or a missing file makes it wait forever.

A waiting sensor can hold a worker slot doing nothing. With `mode="reschedule"`, it gives the slot back between checks. **Deferrable** operators go further: a small background service called the **triggerer** does the waiting for them.

Pools protect shared things. Five DAGs might all read the read replica of the rides database, the copy from [Replication and sharding](/t/module-09b-data/topic-06-replication-sharding). Put those tasks in a pool `rides_replica` with 2 slots, and at most 2 queries hit it at once. The rest queue.

### XCom: small values only

```callout jargon
**XCom** ("cross-communication") is how one Airflow task hands a small value to another. The values are stored in Airflow's metadata database.
```

A check task can push a row count, a file path or a date. A later task pulls it.

The trap: pushing the data itself. 2 million ride rows in XCom go into the metadata database that the scheduler depends on, and slow every DAG. Write the data to the lake or warehouse instead, and pass its **path**: `s3://spoke-lake/rides/ride_date=2026-11-17/`.

A good Airflow task follows a handful of rules, and all of them come back to idempotence:

- It takes its date from the run, never the clock.
- It replaces its own partition, never appends.
- It writes all or nothing: a crash halfway leaves no half-written day visible.
- It passes paths and counts through XCom, not data.
- It does one clear job, so a retry repeats only that job.

### Dagster, Prefect and other choices

Airflow is not the only orchestrator.

| Tool | The idea | Good when |
|---|---|---|
| **Airflow** | DAGs of tasks, scheduled by time | most companies; huge community and ready-made operators |
| **Dagster** | you declare data **assets**, such as the `daily_ride_report` table, and how each is built | you think in tables, partitions and freshness more than in tasks |
| **Prefect** | plain Python functions marked as flows and tasks | workflows whose shape changes at run time |
| **Argo Workflows** | each step is a container on Kubernetes | teams already all-in on Kubernetes |
| **AWS Step Functions** | a managed state machine | small AWS-only flows |

Each one gives you dependencies, retries, history, backfills and alerts. Plain cron gives you none of them. In an interview, naming the gaps of cron matters more than naming a tool.

### Data-quality checks that fail the run

A pipeline that runs every night but loads wrong numbers is worse than one that stops. People trust the report and decide things with it. So the last tasks of a DAG test the data, and **fail the run** when it looks wrong.

| Check | Spoke example | Catches |
|---|---|---|
| Row count | at least 1 row, and within 50% of the same weekday last week | an empty extract; a doubled load |
| No nulls | every report row has a `bike_type` | a broken join or parse |
| Freshness | the newest ride in the lake ended on the logical date | a stuck extract that reloaded old data |
| Totals reconcile | report fares = sum of the day's ride fares = the payments partner's settlement | lost or duplicated rides |

The Worked example builds the reconcile check. Tools exist for this: **dbt tests** (`not_null`, `unique`), **Great Expectations** and **Soda**. Dagster calls them asset checks.

```callout jargon
**Lineage** is the map of which jobs read which tables and write which others. **OpenLineage** is an open standard for recording it.
```

When a check fails on `daily_ride_report`, lineage answers two questions fast. Which upstream table broke it? And which dashboards are now showing bad numbers? dbt and Airflow can both emit lineage, and a tool such as Marquez draws the graph.

### In an interview

Spot this topic when you hear "every night", "daily report", "rerun", "late events" or "job scheduler".

For "design a daily reporting pipeline", walk it in this order:

1. **Batch or stream?** How fresh must the answer be? Daily totals: batch.
2. **ELT into a warehouse**, partitioned by date. Never query production for reports.
3. **Idempotent jobs.** Each run replaces one date partition, so retries and backfills are safe.
4. **An orchestrator** with a DAG, retries, timeouts and alerts. Say why cron is not enough.
5. **Late data:** wait, or rebuild the last few days.
6. **Quality checks** that fail the run: counts, nulls, freshness, reconciled totals.

Classic questions:

- "How do you make a job safe to retry?"
- "Batch or streaming for X?"
- "How does a scheduler decide what runs first?" (topological sort)
- "How do you handle events that arrive late?"
- "Design a job scheduler like cron or Airflow."

Coding rounds use the same order logic: "course schedule" and "order tasks with dependencies" are both topological sort.

## Worked example

Jiraiya's ticket says the morning numbers were quietly wrong. Here is one way that happens, and the fix. A load step is retried after a timeout, once appending and once overwriting. A reconcile check runs after each.

```java run
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Main {
    record Ride(String rentalId, LocalDate day, String bikeType, int minutes) {}
    record ReportRow(LocalDate day, String bikeType, long rides, long fareCents) {}

    static long fareCents(Ride r) {
        long perMinute = switch (r.bikeType()) {
            case "CLASSIC" -> 15;
            case "EBIKE" -> 30;
            default -> 25;                       // CARGO
        };
        return 100 + perMinute * r.minutes();
    }

    // The warehouse table daily_ride_report, partitioned by day.
    static final Map<LocalDate, List<ReportRow>> report = new TreeMap<>();

    /** The transform: one row per bike type, for one logical date only. */
    static List<ReportRow> buildRows(List<Ride> rides, LocalDate day) {
        Map<String, long[]> byType = new TreeMap<>();
        for (Ride r : rides) {
            if (r.day().equals(day)) {
                long[] t = byType.computeIfAbsent(r.bikeType(), k -> new long[2]);
                t[0]++;
                t[1] += fareCents(r);
            }
        }
        List<ReportRow> rows = new ArrayList<>();
        byType.forEach((type, t) -> rows.add(new ReportRow(day, type, t[0], t[1])));
        return rows;
    }

    static void loadByAppending(List<Ride> rides, LocalDate day) {
        report.computeIfAbsent(day, d -> new ArrayList<>()).addAll(buildRows(rides, day));
    }

    static void loadByOverwriting(List<Ride> rides, LocalDate day) {
        report.put(day, buildRows(rides, day));  // replaces the whole partition
    }

    /** A data-quality check: the report must add up to the rides it came from. */
    static void checkTotals(List<Ride> rides, LocalDate day) {
        long expected = rides.stream().filter(r -> r.day().equals(day)).mapToLong(Main::fareCents).sum();
        long actual = report.getOrDefault(day, List.of()).stream().mapToLong(ReportRow::fareCents).sum();
        if (actual != expected) {
            throw new IllegalStateException("check_totals failed for " + day + ": report " + actual + " cents, rides " + expected + " cents");
        }
        System.out.println("check_totals passed for " + day + ": " + actual + " cents");
    }

    public static void main(String[] args) {
        LocalDate nov17 = LocalDate.of(2026, 11, 17);
        List<Ride> rides = List.of(
                new Ride("rental-17", nov17, "EBIKE", 12),
                new Ride("rental-18", nov17, "CLASSIC", 20),
                new Ride("rental-19", nov17, "CARGO", 10),
                new Ride("rental-20", nov17, "EBIKE", 5));

        System.out.println("-- appending: the load times out after writing, and the scheduler retries --");
        loadByAppending(rides, nov17);
        loadByAppending(rides, nov17);           // the retry
        try {
            checkTotals(rides, nov17);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }

        report.clear();
        System.out.println("-- overwriting: the same timeout and retry --");
        loadByOverwriting(rides, nov17);
        loadByOverwriting(rides, nov17);         // the retry
        checkTotals(rides, nov17);
        report.get(nov17).forEach(System.out::println);
    }
}
```

It prints:

```text
-- appending: the load times out after writing, and the scheduler retries --
check_totals failed for 2026-11-17: report 2920 cents, rides 1460 cents
-- overwriting: the same timeout and retry --
check_totals passed for 2026-11-17: 1460 cents
ReportRow[day=2026-11-17, bikeType=CARGO, rides=1, fareCents=350]
ReportRow[day=2026-11-17, bikeType=CLASSIC, rides=1, fareCents=400]
ReportRow[day=2026-11-17, bikeType=EBIKE, rides=2, fareCents=710]
```

Step by step:

1. **`report` is a `Map` from day to rows.** Each key is one partition of the warehouse table.
2. **`buildRows` filters on the logical date first.** The job only ever looks at its own day. Rental-17, a 12-minute EBIKE ride, is 100 + 30 × 12 = 460 cents.
3. **`loadByAppending` adds rows to the partition.** Run twice, the 2026-11-17 slice holds every row twice: CARGO, CLASSIC, EBIKE, then all three again.
4. **`checkTotals` reconciles.** It sums the source rides (1,460 cents) and the report (2,920). They differ, so it throws, and the task fails. Without it, the doubled number would reach Jiraiya's dashboard.
5. **`loadByOverwriting` uses `put`.** It replaces the whole partition. The retry writes the same three rows over the same three rows.
6. **The check passes**, and the rows show 4 rides: 1 CARGO, 1 CLASSIC, 2 EBIKE.

Each run reads the n rides once and keeps one entry per bike type in a `TreeMap`. With t bike types that is O(n log t) time, which is O(n) for Spoke's three types, and O(t) extra space.

## Try it

A stream of "ride ended" events arrives out of order. The watermark trails the latest end time seen by the allowed lateness. Events older than it are late.

```java run
import java.util.List;

public class Main {
    record RideEnd(String rentalId, int minute) {}   // when the ride really ended: 08:00 + minute

    public static void main(String[] args) {
        int allowedLatenessMinutes = 5;              // try 0, then 10
        List<RideEnd> inArrivalOrder = List.of(
                new RideEnd("rental-17", 2),
                new RideEnd("rental-18", 4),
                new RideEnd("rental-19", 11),
                new RideEnd("rental-20", 7),         // a slow phone connection
                new RideEnd("rental-21", 13),
                new RideEnd("rental-22", 3),         // the bike was offline for 10 minutes
                new RideEnd("rental-23", 15));

        int maxSeen = Integer.MIN_VALUE;
        for (RideEnd e : inArrivalOrder) {
            int watermark = maxSeen - allowedLatenessMinutes;   // "everything before this has arrived"
            String verdict = maxSeen != Integer.MIN_VALUE && e.minute() < watermark ? "LATE" : "on time";
            System.out.printf("%s ended 08:%02d  -> %s%n", e.rentalId(), e.minute(), verdict);
            maxSeen = Math.max(maxSeen, e.minute());
        }
        System.out.printf("watermark now: 08:%02d%n", maxSeen - allowedLatenessMinutes);
    }
}
```

1. Run it. Only rental-22 is late: it ended at 08:03, but the watermark had already reached 08:08.
2. Set `allowedLatenessMinutes` to `0`. Now rental-20 is late too. What does a tight watermark buy you, and what does it cost?
3. Set it to `10`. Nothing is late, but the watermark trails further behind. Why would a "rides per 10 minutes" chart then update later?

## Recap

- A data pipeline moves data from production into a warehouse or lake, usually in batch. Stream only when the answer is needed within minutes. Most teams now load raw data first and transform it with SQL (ELT).
- Partition tables by date, and make every job idempotent: it replaces its own date partition and never appends. That makes retries, backfills and late-data reruns safe.
- Cron runs commands at fixed times but knows nothing about order, retries, history or backfills. An orchestrator runs a DAG in topological order, retries, marks downstream tasks upstream_failed, and records every run.
- In Airflow, operators make tasks, `>>` sets dependencies, and each run has a logical date that is not the time it runs. Know catchup, backfills, retries, timeouts, sensors, pools and small XComs.
- Quality checks on counts, nulls, freshness and reconciled totals fail the run loudly, and lineage shows what broke and who is affected.

## Common mistakes

**1. Appending in a daily job.**

```java nocompile
report.computeIfAbsent(day, d -> new ArrayList<>()).addAll(buildRows(rides, day));   // runs again on every retry
```

```text
check_totals failed for 2026-11-17: report 2920 cents, rides 1460 cents
```

Every retry or rerun adds the same rides again, and the report doubles. Fix: replace the whole date partition, `report.put(day, rows)`, or in SQL `DELETE` that date and `INSERT` in one transaction.

**2. Reading the clock inside a task.**

```java nocompile
LocalDate day = LocalDate.now().minusDays(1);   // "yesterday", according to the clock
```

There is no error. A backfill of 1 to 14 November, run on 18 November, rebuilds 17 November fourteen times. Fix: take the date from the run: the logical date in Airflow (`ds`), or a parameter of your job.

**3. Chaining jobs by clock time.**

```bash
0 2 * * *   extract_rides.sh
30 2 * * *  clean_rides.sh
0 3 * * *   load_report.sh
```

On a slow night, extract is still running at 02:30, and clean reads half the rides. Nothing fails, so nobody notices. Fix: one DAG with `extract >> clean >> load`, where each task starts only when its upstream succeeded.

**4. A pipeline with no data checks.**

```python
extract >> clean >> load            # green every night, even when extract copied 0 rows
```

The DAG shows green while the report says Spoke had no rides on Tuesday. Fix: end the DAG with checks that raise an error and fail the run: row counts, nulls, freshness and totals that reconcile.

**5. Passing the data itself through XCom.**

```python
ti.xcom_push(key="rides", value=all_rides_for_the_day)   # 2 million rows into Airflow's own database
```

Airflow's metadata database bloats, and the scheduler slows for every DAG. Fix: write the rides to the lake or warehouse, and push only the path and the row count.

**6. Leaving catchup on with an old start_date.**

```python
DAG(dag_id="daily_ride_report", schedule="@daily", start_date=datetime(2025, 11, 1), catchup=True)
```

On deploy, Airflow creates a run for every day since that start date: more than a year of runs. They all hit the rides database. Fix: `catchup=False`, a recent `start_date`, and an explicit backfill with a capped `max_active_runs` when history is needed.

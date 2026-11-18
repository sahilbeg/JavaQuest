## Hook

A bank has 8 tellers. One busy morning, 300 customers crowd the counter at once. The tellers do not get faster. They get slower, because they keep switching between shouting customers. A queue at the door, letting 8 through at a time, would have served everyone sooner.

Spoke's database had that morning today. At 8 a.m. the API ran out of database connections. A migration locked the `rides` table for a minute. And nobody is sure the backups would actually restore.

This ticket is about running Postgres for real: pools, plans, cleanup, partitions, JSONB, safe migrations, backups and replicas.

## Concept

### A connection is a whole process

```callout jargon
A **database connection** is one open line between your app and the database. In Postgres, every connection gets its own operating-system **process**, called a **backend**.
```

Postgres starts a new process for every connection. Each process takes memory, often several megabytes, and more while it sorts or hashes. Opening one costs a network handshake, a login check and a process start: milliseconds, not microseconds.

So connections are a limited, expensive thing. Postgres caps them with the setting `max_connections`, 100 by default. Three of those are kept back for admins (`superuser_reserved_connections`). Go over, and Postgres refuses the next one with an error like this:

```text
FATAL:  sorry, too many clients already
```

Raising `max_connections` to 2,000 is not the fix. Two thousand processes fight over the same few CPU cores. They wait on each other's locks and evict each other from memory. The database gets slower for everyone.

### Connection pools and HikariCP

You met connection pools in [JDBC](/t/module-08-backend/topic-08-jdbc): a pool opens a few connections once and lends them out. Spring Boot's pool is **HikariCP**. These are its settings that matter in production:

| Setting | Default | What it does |
|---|---|---|
| `maximumPoolSize` | 10 | the most connections this app may hold |
| `minimumIdle` | same as the maximum | how many to keep open when quiet |
| `connectionTimeout` | 30 s | how long a request waits to borrow one before failing |
| `maxLifetime` | 30 min | retire each connection after this, before the network or database drops it |

In Spring Boot they are set as `spring.datasource.hikari.maximum-pool-size` and so on.

The 30-second `connectionTimeout` is a trap at rush hour. A request that cannot get a connection waits half a minute, holding a web thread. Many teams lower it to 2 to 5 seconds, so a full pool fails fast. That is the timeout lesson from [Resilience](/t/module-09-architecture/topic-09-resilience), applied to the database.

### Why small pools win

A database server can only do as many things at once as it has CPU cores. Add a little for queries waiting on the disk. Extra connections do not add speed. They queue **inside** the database, where queueing is most expensive.

The PostgreSQL wiki, and HikariCP's guide "About Pool Sizing", give a starting formula:

```text
connections = (CPU cores × 2) + effective disks
```

For Spoke's database server with 8 cores and fast SSDs, that is about 16 to 20 active connections **in total**, across all apps.

Treat it as a starting point, then measure. It assumes each transaction keeps the database busy. If your transactions sit idle, waiting on a slow payment call while holding a connection, you need more connections. Better still, stop holding a connection during outside calls.

The surprise for most people: a queue of requests waiting in the **app** for a connection is fine. The app's queue is cheap. The database's internal fight is not.

### Pool math across servers

Each pool sounds small. Multiply by the number of copies of the app:

```text
total connections = copies of the app × maximumPoolSize
```

Spoke runs 30 copies of the API on Kubernetes, from [Running on Kubernetes](/t/module-09-architecture/topic-10-kubernetes). With Spring Boot's default pool of 10, that is **300** connections, three times Postgres's default limit. Add the autoscaler adding copies at rush hour, the nightly pipeline, and a person in a SQL console.

Write a **connection budget**: who gets how many, and what happens at the maximum number of copies. The team raised the autoscaler's cap from 30 to 45 copies for the morning rush. Here is a budget that fits that, with no pooler in between:

| Client | Copies at most | Pool | Connections |
|---|---|---|---|
| spoke-api | 45 | 4 | 180 |
| pipeline jobs | 2 | 5 | 10 |
| people and admin tools | | | 7 |
| **total** | | | **197**, just under 200 |

The rule: plan for the **maximum** number of copies, never the usual one. The Worked example shows what happens when nobody does.

### PgBouncer and transaction pooling

When there are many app copies, give them all one shared pool **outside** the apps.

```callout jargon
**PgBouncer** is a small, fast connection pooler that sits between the apps and Postgres. Apps connect to it as if it were Postgres. It lends them a few real database connections.
```

PgBouncer has three modes:

| Mode | A real connection is lent for | Sharing |
|---|---|---|
| session | the client's whole connection | little; like no pooler |
| **transaction** | one transaction, then returned | high: 450 clients can share 20 connections |
| statement | one statement | highest; multi-statement transactions are not allowed |

Transaction mode is the common choice. Its catch: the next transaction may run on a **different** real connection. So anything that should last for a whole session breaks:

- `SET statement_timeout = '5s'` on its own: it sticks to one real connection, then leaks to other clients. Use `SET LOCAL` inside the transaction instead.
- Temporary tables, session advisory locks, and `LISTEN`/`NOTIFY`.
- **Prepared statements**, in PgBouncer before version 1.21. The Postgres JDBC driver prepares a statement on the server after 5 uses (`prepareThreshold`). With an older PgBouncer, set `prepareThreshold=0`.

Cloud versions exist too, such as Amazon RDS Proxy.

### statement_timeout and lock_timeout

Postgres has its own timeouts, which protect it from your worst queries:

| Setting | Cancels a statement that... |
|---|---|
| `statement_timeout` | runs longer than this |
| `lock_timeout` | waits longer than this for a lock |
| `idle_in_transaction_session_timeout` | ends a session left idle inside an open transaction |

Set them per database user, so every connection gets them:

```sql
ALTER ROLE spoke_api SET statement_timeout = '5s';
ALTER ROLE spoke_api SET idle_in_transaction_session_timeout = '30s';
```

A cancelled statement fails with `ERROR: canceling statement due to statement timeout`. The app sees an exception at once, rather than a thread stuck for minutes. Reports and migrations log in as other roles with their own, longer limits.

An idle transaction is worse than it looks. It holds its locks, and it stops cleanup, as the autovacuum card shows.

### Reading EXPLAIN ANALYZE

```callout jargon
A **query plan** is the step-by-step recipe Postgres chooses to answer a query. `EXPLAIN` shows the plan with the planner's guesses. `EXPLAIN ANALYZE` also **runs** the query and adds what really happened.
```

This is what Postgres prints for "rides that started between 08:00 and 09:00, with their end station", trimmed. The numbers are an illustration, but the format is Postgres's own:

```text
Hash Join  (cost=9.75..2210.42 rows=812 width=40) (actual time=0.210..14.880 rows=790 loops=1)
  Hash Cond: (r.end_station_id = s.id)
  ->  Index Scan using rides_started_at_idx on rides r  (cost=0.43..2189.10 rows=812 width=32) (actual time=0.025..13.950 rows=790 loops=1)
        Index Cond: ((started_at >= '2026-11-17 08:00:00+00'::timestamp with time zone) AND (started_at < '2026-11-17 09:00:00+00'::timestamp with time zone))
  ->  Hash  (cost=5.00..5.00 rows=300 width=16) (actual time=0.150..0.151 rows=300 loops=1)
        Buckets: 1024  Batches: 1  Memory Usage: 23kB
        ->  Seq Scan on stations s  (cost=0.00..5.00 rows=300 width=16) (actual time=0.005..0.060 rows=300 loops=1)
Planning Time: 0.320 ms
Execution Time: 15.010 ms
```

Read it from the most indented line up. Each line is a step that feeds the one above it.

- **`cost=0.43..2189.10`**: the planner's guess, in made-up units. The first number is the cost before the first row, the second the cost for all rows.
- **`rows=812`**: the planner's guess of how many rows the step returns. `width` is the average row size in bytes.
- **`actual time=0.025..13.950`**: real milliseconds, to the first row and to the last.
- **`rows=790 loops=1`**: the real row count, and how many times the step ran. Multiply by `loops` for the true total.

Add `BUFFERS` (`EXPLAIN (ANALYZE, BUFFERS)`) to see how many pages came from memory and how many from disk. Careful: `ANALYZE` really runs the statement. Wrap an `UPDATE` or `DELETE` in `BEGIN` and `ROLLBACK`.

### Scan types

How a plan reads a table:

| Node | What it does | Good when |
|---|---|---|
| **Seq Scan** | reads every row of the table | small tables, or most rows are needed |
| **Index Scan** | walks the index, then fetches each row from the table | a few rows out of many |
| **Index Only Scan** | answers from the index alone | every needed column is in the index |
| **Bitmap Index Scan** + **Bitmap Heap Scan** | collects matching row positions first, then reads the table pages in order | a medium number of rows, or two indexes combined |

A Seq Scan is not always bad. The plan above scans all 300 stations, because reading one small table straight through beats jumping around an index. You saw how indexes are built in [How indexes work](/t/module-09b-data/topic-02-index-internals).

An Index Only Scan shows a line `Heap Fetches: 0` when it never touched the table. A large number there means the table's **visibility map** is out of date: vacuum has not marked those pages as fully visible yet.

### Join types

How a plan joins two tables:

| Join | How | Good when |
|---|---|---|
| **Nested Loop** | for each row on the outside, look up matches on the inside | the outside is small and the inside has an index |
| **Hash Join** | build a hash table from the smaller side, then probe it with each row of the other | bigger inputs, joining on `=` |
| **Merge Join** | walk two inputs sorted on the join key side by side | both are already sorted, often by an index |

The plan above hashed the 300 stations (`Memory Usage: 23kB`), then looked up each ride's end station in that table. `Batches: 1` means the hash table fit in memory. More batches mean it spilled to disk, and raising `work_mem` for that query may help.

There is no best join. The planner picks one from its row estimates. So the next card is the one that matters most.

### When estimates lie

Compare each node's estimated `rows` with its actual `rows`. Above, 812 against 790: a good guess. Trouble starts when they differ by 100 times or more.

Say the planner expects 5 rides and chooses a Nested Loop: 5 index lookups, cheap. The real answer is 400,000 rides, so the loop runs 400,000 lookups, and a 20 ms query takes minutes. The plan was right for the guess. The guess was wrong.

```callout jargon
**Statistics** are summaries Postgres keeps about each column: how many distinct values, the most common ones, and how values are spread. The planner guesses row counts from them. The `ANALYZE` command refreshes them.
```

Common causes of a bad guess:

- **Stale statistics** after a bulk load or a big delete. Run `ANALYZE rides;`. Autovacuum also analyzes, but maybe not soon enough.
- **Columns that depend on each other**, such as station and city. The planner assumes they are independent. `CREATE STATISTICS` teaches it they are not.
- **Values hidden inside expressions or JSON**, which get default guesses.

### pg_stat_statements: find the slowest queries

Which queries should you tune first? Do not guess. Ask Postgres.

```callout jargon
**pg_stat_statements** is a Postgres extension that records every distinct query shape, with how often it ran and how long it took in total.
```

```sql
SELECT calls, round(total_exec_time) AS total_ms, round(mean_exec_time, 1) AS mean_ms, query
FROM pg_stat_statements
ORDER BY total_exec_time DESC
LIMIT 10;
```

It groups queries that differ only in their values, so `WHERE id = 17` and `WHERE id = 18` count as one. It must be listed in `shared_preload_libraries` and created with `CREATE EXTENSION pg_stat_statements;`. Managed Postgres usually has it ready.

Sort by **total** time, not mean time. A 2 ms query run 3 million times a day costs more than a 4-second report run once. Speeding up the first by half saves far more.

Two more helpers: `log_min_duration_statement` logs every query slower than a limit, and `auto_explain` logs the plans of slow queries.

### Autovacuum, dead tuples and bloat

From [Inside transactions](/t/module-09b-data/topic-03-transaction-internals): Postgres never changes a row in place. An `UPDATE` writes a new version and marks the old one dead. A `DELETE` only marks it dead.

```callout jargon
A **dead tuple** is an old row version no transaction can see any more. **Vacuum** marks its space as reusable. **Bloat** is space wasted on dead tuples that has not been reused.
```

`rides` gets many updates: start, end, fare. Without vacuum, a table of 1 million live rows could hold millions of dead ones, and every scan would read them.

**Autovacuum** runs vacuum for you. By default it starts when about 20% of a table's rows are dead (`autovacuum_vacuum_scale_factor = 0.2`). For a 100-million-row table, that is 20 million dead rows: far too late. Lower it for big, busy tables:

```sql
ALTER TABLE rides SET (autovacuum_vacuum_scale_factor = 0.01);
```

The classic trap is a **long-open transaction**. Vacuum cannot remove a row version that any open transaction might still need. One session left idle in a transaction for six hours blocks cleanup of every table, and bloat grows everywhere.

Plain vacuum does not shrink the file; it makes space reusable. `VACUUM FULL` shrinks it, but locks the table for the whole rewrite. Teams use the `pg_repack` extension to do it online instead.

### Partitioning big tables

```callout jargon
**Table partitioning** splits one big table into smaller tables, called partitions, by a key such as the month. Queries still use the parent table's name.
```

`rides` grows by millions of rows a year. Split it by month, and three things get better:

- **Partition pruning.** A query with `started_at` in November reads only the November partition. The plan shows only that partition.
- **Cheap deletes of old data.** Dropping the 2021 partitions is instant. `DELETE` of 30 million old rows takes hours, writes WAL, and leaves 30 million dead tuples for vacuum.
- **Smaller indexes and vacuums,** one partition at a time.

The costs:

- The primary key must include the partition key, so it becomes `(id, started_at)`.
- A query **without** `started_at`, like "every ride of rider-7", must look in every partition.
- Thousands of partitions slow down planning. Monthly is a sensible size for rides.
- Someone must create next month's partition in time. The `pg_partman` extension, or a scheduled job, does it.

Data pipelines use the same idea, as you saw in [Data pipelines](/t/module-09b-data/topic-07-data-pipelines).

### Syntax: monthly partitions

```sql
CREATE TABLE rides (
    id          bigint      NOT NULL,
    rider_id    text        NOT NULL,
    bike_id     text        NOT NULL,
    started_at  timestamptz NOT NULL,
    fare_cents  bigint,
    PRIMARY KEY (id, started_at)
) PARTITION BY RANGE (started_at);

CREATE TABLE rides_2026_11 PARTITION OF rides
    FOR VALUES FROM ('2026-11-01') TO ('2026-12-01');

ALTER TABLE rides DETACH PARTITION rides_2021_11;   -- older than five years
DROP TABLE rides_2021_11;
```

The upper bound `TO` is **not** included, so months never overlap. A row with no matching partition fails to insert, unless you add a `DEFAULT` partition. Since Postgres 14, `DETACH PARTITION ... CONCURRENTLY` avoids blocking queries while it detaches.

### JSONB: when it fits

```callout jargon
**JSONB** is a Postgres column type that stores JSON in a binary form that can be indexed and searched. A **GIN index** (generalized inverted index) indexes every key and value inside it.
```

```sql
CREATE TABLE bike_telemetry (bike_id text, sent_at timestamptz, details jsonb);
CREATE INDEX bike_telemetry_details_idx ON bike_telemetry USING GIN (details);

SELECT bike_id FROM bike_telemetry WHERE details @> '{"battery": "low"}';
```

`->` returns a JSON value, `->>` returns text, `@>` means "contains", and `?` asks "has this key". A GIN index speeds up `@>` and `?`; a normal B-tree index does not.

JSONB fits data whose shape varies or that you only store:

- e-bike sensor readings, which differ by bike model;
- raw webhook payloads from the payments partner, kept for audits;
- per-rider app settings.

The trap is **hiding your relational model inside JSON**. Put `rider_id` and `fare_cents` in a JSON blob, and you lose foreign keys, column types, `NOT NULL` checks and good statistics. Every update rewrites the whole value. Rule of thumb: if you filter, join or sum by it, give it a real column.

### Online schema changes

Most `ALTER TABLE` commands need the strongest lock, **ACCESS EXCLUSIVE**. It blocks even plain `SELECT`s. Usually it is held for milliseconds. The danger is the **lock queue**:

```animate
title: One ALTER waits, and everyone waits behind it
kind: scene
nodes:
  - { id: report, label: Report query, emoji: "📊", value: "reading rides", row: 0, col: 0 }
  - { id: rides, label: rides table, emoji: "🗄️", value: "shared lock", row: 0, col: 2 }
  - { id: alter, label: Migration, emoji: "🛠️", value: idle, row: 1, col: 1 }
  - { id: rider, label: Rider queries, emoji: "🚲", value: "0 waiting", row: 1, col: 3 }
steps:
  - say: A 60-second report is reading rides. It holds a shared lock, and rider queries share it happily.
    send: { from: rider, to: rides, label: "SELECT ok" }
  - say: A migration runs ALTER TABLE rides. It needs ACCESS EXCLUSIVE, which conflicts with the report's lock, so it waits.
    send: { from: alter, to: rides, label: "ALTER TABLE" }
    set: { alter: "waiting for lock" }
  - say: A rider tries to end rental-17. Its SELECT would not conflict with the report, but Postgres queues it behind the waiting ALTER.
    send: { from: rider, to: rides, label: "SELECT" }
    set: { rider: "1 waiting" }
  - say: Every new rider query joins the queue. For a whole minute, nobody can start or end a ride.
    set: { rider: "60 waiting" }
    focus: [rider]
  - say: The report finishes. The ALTER takes its lock for one second, then the queue drains.
    set: { report: done, alter: done, rider: "0 waiting" }
  - say: With lock_timeout 2s, the ALTER would have given up after 2 seconds, the queue would have drained, and a retry would run later.
    focus: [alter]
```

That is Kakashi's second problem. The fix: tell the migration to give up quickly, then retry.

```sql
SET lock_timeout = '2s';
```

If the lock is not granted in 2 seconds, the `ALTER` fails with `canceling statement due to lock timeout`. The queue drains, and a script tries again a little later. The Try it section shows the difference.

Changes that are safe, and how to do them:

- **Add a column with a constant default:** instant since Postgres 11, even `NOT NULL DEFAULT 'CLASSIC'`. No rewrite.
- **Add an index:** `CREATE INDEX CONCURRENTLY`. It does not block writes, but cannot run inside a transaction. If it fails, it leaves an INVALID index: drop it and try again.
- **Make an existing column NOT NULL:** add a `CHECK ... NOT VALID` constraint, then `VALIDATE` it, which allows writes. Then `SET NOT NULL` skips its full-table scan.

For renames and type changes, use expand and contract from [Shipping to production](/t/module-07-tooling/topic-07-shipping-to-production): add the new thing, move the code, then remove the old.

### Syntax: a safe migration

```sql
-- V41__rental_bike_id_index.sql: CONCURRENTLY alone, with nothing else in the script
CREATE INDEX CONCURRENTLY rental_bike_id_idx ON rental (bike_id);

-- V42__rental_bike_type_not_null.sql
SET lock_timeout = '2s';
ALTER TABLE rental ADD CONSTRAINT rental_bike_type_not_null CHECK (bike_type IS NOT NULL) NOT VALID;
ALTER TABLE rental VALIDATE CONSTRAINT rental_bike_type_not_null;
ALTER TABLE rental ALTER COLUMN bike_type SET NOT NULL;
ALTER TABLE rental DROP CONSTRAINT rental_bike_type_not_null;
```

These run on the plain `rental` table. On a partitioned table such as `rides`, Postgres refuses `CONCURRENTLY` on the parent. There you create the index `ON ONLY rides`, build it `CONCURRENTLY` on each partition, then `ALTER INDEX ... ATTACH PARTITION` each one.

Put `CREATE INDEX CONCURRENTLY` in its own migration. Flyway runs it outside a transaction by itself, but refuses a script that mixes it with other statements. Liquibase needs `runInTransaction: false`.

### Backups: logical and physical

```callout jargon
A **logical backup** saves the data as SQL or a portable file, with `pg_dump`. A **physical backup** copies the database's files, plus the **WAL** (write-ahead log, the record of every change).
```

| | `pg_dump` (logical) | Base backup + WAL archive (physical) |
|---|---|---|
| What | one consistent snapshot of the data | a file copy, plus every change after it |
| Restore to | the moment the dump started | **any moment**, to the second |
| Good for | small databases, moving to a new version | production |
| Restore speed | slow: rebuilds every row and index | faster: copies files, replays WAL |

Point-in-time recovery, **PITR**, is the reason to go physical. Someone runs `DELETE FROM rides` without a `WHERE` at 14:32:00. You restore last night's base backup on a new server, then replay the archived WAL up to `recovery_target_time = '2026-11-17 14:31:59'`. The rides come back as they were one second before.

Tools such as **pgBackRest**, **WAL-G** and **Barman** take the base backups, ship the WAL to object storage like S3, and run the restores.

### RPO, RTO and testing restores

```callout jargon
**RPO** (recovery point objective) is how much data you may lose, measured in time. **RTO** (recovery time objective) is how long you may be down while you restore.
```

Spoke's numbers: an RPO of 5 minutes, because a lost rental is a lost fare. An RTO of 1 hour.

- A nightly `pg_dump` alone gives an RPO of up to 24 hours. It misses the RPO.
- WAL archiving ships changes every few minutes or faster. It meets it.
- Only a timed restore tells you the RTO. If restoring 2 TB takes 5 hours, a 1-hour RTO is a wish.

**A backup you have never restored is only a hope.** Backups fail quietly: a full disk, a wrong password, a WAL gap. Run a restore drill on a schedule. Restore to a scratch server, check row counts and the latest ride, and time it. Keep the backups in a separate account, so one mistake or attacker cannot delete the database and its backups together.

### Read replicas

A **read replica** is a copy of the database that follows the primary by replaying its WAL, as in [Replication and sharding](/t/module-09b-data/topic-06-replication-sharding). Send it the heavy reads: reports, exports, the nightly pipeline. The primary then serves rider writes.

Remember its limits:

- **Replication lag.** A replica may be a second or more behind. A rider who just ended a ride, then reads from a replica, may see it still active. Send read-your-own-writes requests to the primary.
- **Query conflicts.** A long query on a replica can be cancelled with `canceling statement due to conflict with recovery`. The replica had to apply a change, such as a vacuum, that removed rows the query still needed. `hot_standby_feedback = on` avoids this, at the cost of more bloat on the primary.
- **A replica is not a backup.** A `DELETE FROM rides` replays on the replica within a second.

### Managed Postgres: what RDS and Cloud SQL do for you

Most companies rent Postgres instead of running their own servers: **Amazon RDS** or **Aurora**, **Google Cloud SQL**, **Azure Database for PostgreSQL**.

They do a lot for you:

- the hardware, the operating system and minor-version patches;
- automated backups with PITR, kept for up to 35 days on RDS;
- a standby in another data centre, with automatic **failover** when the primary dies;
- read replicas and monitoring at the click of a button.

They do **not** do your part of the job:

- your schema, indexes and slow queries;
- pool sizes and the connection budget;
- safe migrations and lock timeouts;
- autovacuum settings for your busiest tables;
- testing that **your** restore works within **your** RTO.

You also give some things up: no superuser, only the extensions they support, and maintenance windows they choose.

### In an interview

Spot this topic when you hear "the database is slow", "too many connections" or "this table is huge". Also "change the schema without downtime" and "what if we lose the database?".

Answer in this order:

1. **Measure first.** `pg_stat_statements` for the costly queries, then `EXPLAIN (ANALYZE, BUFFERS)` for each. Compare estimated and actual rows.
2. **Connections.** Small pools, a written budget for the maximum number of app copies, PgBouncer in transaction mode when there are many copies.
3. **Big tables.** Partition by time, drop old partitions, and tune autovacuum per table.
4. **Migrations.** `lock_timeout` and retries, `CREATE INDEX CONCURRENTLY`, expand and contract.
5. **Safety.** Base backups plus WAL for PITR, stated RPO and RTO, and restore drills.

Classic questions:

- "Why not just raise max_connections?"
- "What is a seq scan, and when is it fine?"
- "How do you add a column to a 500-million-row table without downtime?"
- "What is the difference between a replica and a backup?"
- "How would you find the slowest queries?"


## Worked example

Kakashi's first problem: at 8 a.m. the API ran out of database connections. Here is the arithmetic, with Spoke's real settings.

```java run
public class Main {
    public static void main(String[] args) {
        int maxConnections = 200;        // raised from Postgres's default of 100 last year
        int reserved = 3;                // superuser_reserved_connections: kept for admins
        int poolPerPod = 5;              // spring.datasource.hikari.maximum-pool-size

        for (int pods : new int[] {30, 45}) {
            int wanted = pods * poolPerPod;
            String verdict = wanted <= maxConnections - reserved ? "fits" : "TOO MANY: new pods cannot connect";
            System.out.printf("%d pods x %d = %d connections of %d usable -> %s%n",
                    pods, poolPerPod, wanted, maxConnections - reserved, verdict);
        }

        // Little's law: connections busy = transactions per second x seconds each one holds a connection
        double perSecond = 400;          // the whole API at 8 a.m.
        double secondsHeld = 0.015;      // a typical transaction holds its connection 15 ms
        System.out.printf("busy on average: %.0f connections%n", perSecond * secondsHeld);
        System.out.printf("if one slow query holds 0.5 s at 40 a second: %.0f more%n", 40 * 0.5);
    }
}
```

It prints:

```text
30 pods x 5 = 150 connections of 197 usable -> fits
45 pods x 5 = 225 connections of 197 usable -> TOO MANY: new pods cannot connect
busy on average: 6 connections
if one slow query holds 0.5 s at 40 a second: 20 more
```

Step by step:

1. **Usable connections are 197**, not 200. Three are reserved for admins.
2. **At 30 pods it fits**, with 47 to spare. That is the usual day, and why nobody noticed.
3. **At 8 a.m. the autoscaler went to 45 pods,** the cap the team had just raised from 30 for the morning rush. Each new pod opened its pool, and the last ones got `sorry, too many clients already`. They failed their readiness checks while the rush grew.
4. **Little's law** (from [Resilience](/t/module-09-architecture/topic-09-resilience)) says how many connections are really busy: 400 transactions a second × 0.015 s = **6** on average. Nearly all 225 connections sat idle.
5. **A slow query changes everything.** At 40 calls a second, holding 0.5 s each, it keeps 20 connections busy on its own. Fix the query before you grow the pool.

The fixes, in order:

- Put **PgBouncer** in transaction mode in front of Postgres, with about 20 real connections. Every pod may then keep its pool of 5: PgBouncer queues them cheaply.
- Lower Hikari's `connectionTimeout` to a few seconds, so a full pool fails fast instead of holding threads for 30 s.
- Write the connection budget for the **maximum** pods the autoscaler may create, and check it in code review.

## Try it

Kakashi's second problem: a migration locked `rides` for a minute. A report query holds a lock until 60 s. The migration asks for its exclusive lock at 1 s. A rider query arrives every second.

```java run
public class Main {
    public static void main(String[] args) {
        int reportEnds = 60;     // a report query started at 0 s; its lock on rides lasts until 60 s
        int alterAsksAt = 1;     // the migration asks for its exclusive lock at 1 s
        int lockTimeout = 0;     // seconds; 0 means wait forever. Try 2.
        int alterTakes = 1;      // once it has the lock, the ALTER itself takes 1 s

        // When does the ALTER stop blocking others: it gives up, or it gets the lock and finishes.
        boolean givesUp = lockTimeout > 0 && alterAsksAt + lockTimeout < reportEnds;
        int blockedUntil = givesUp ? alterAsksAt + lockTimeout : reportEnds + alterTakes;

        int blocked = 0;
        int longestWait = 0;
        for (int t = 0; t <= 90; t++) {               // one rider query on rides every second
            if (t >= alterAsksAt && t < blockedUntil) { // it queues behind the waiting ALTER
                blocked++;
                longestWait = Math.max(longestWait, blockedUntil - t);
            }
        }
        System.out.println(givesUp ? "ALTER gave up at " + blockedUntil + " s: retry it later"
                                   : "ALTER finished at " + blockedUntil + " s");
        System.out.println("rider queries stuck: " + blocked + ", longest wait: " + longestWait + " s");
    }
}
```

1. Run it. With no lock timeout, 60 rider queries are stuck, the longest for 60 s. The `ALTER` itself needed only 1 s.
2. Set `lockTimeout` to `2`. Only 2 queries wait, for at most 2 s. What must the migration script do after giving up?
3. Set `reportEnds` to `1`, so nothing holds the lock. Does the timeout cost anything when the table is quiet?

## Recap

- Every Postgres connection is a process, so connections are scarce. Keep pools small, about cores × 2 to start, and budget them for the maximum number of app copies. Put PgBouncer in transaction mode in front when there are many.
- `statement_timeout`, `lock_timeout` and `idle_in_transaction_session_timeout` stop runaway queries. Find the costly queries with `pg_stat_statements`, then read `EXPLAIN ANALYZE`: scan and join types, and estimated against actual rows.
- Updates leave dead tuples; autovacuum cleans them, unless a long-open transaction blocks it. Partition huge tables by month, so pruning skips old data and old months can be dropped instantly.
- Use JSONB for data whose shape varies, with a GIN index, but keep what you filter, join or sum in real columns. Change schemas with `lock_timeout` and retries, `CREATE INDEX CONCURRENTLY`, and expand and contract.
- Physical backups plus WAL give point-in-time recovery. Know your RPO and RTO, and prove them with restore drills. Replicas offload reads but are not backups. Managed Postgres runs the servers, not your queries.

## Common mistakes

**1. Raising max_connections instead of fixing the pools.**

```sql
ALTER SYSTEM SET max_connections = 2000;   -- 2,000 processes on 8 cores
```

The errors stop, but the database gets slower for everyone: processes fight for CPU, locks and memory. Fix: small pools, a connection budget, and PgBouncer in transaction mode.

**2. Session state behind PgBouncer in transaction mode.**

```sql
SET statement_timeout = '30s';   -- meant for this report session only
```

The setting sticks to one real connection, which the next client then gets. A rider request inherits a 30-second timeout meant for a report. Fix: `SET LOCAL` inside the transaction, or set it per role with `ALTER ROLE`.

**3. A migration with no lock timeout.**

```sql
ALTER TABLE rides ADD COLUMN promo_code text;   -- waits behind a 60-second report
```

```text
ALTER finished at 61 s
rider queries stuck: 60, longest wait: 60 s
```

The command itself takes milliseconds, but it queues for the lock, and every rider query queues behind it. Fix: `SET lock_timeout = '2s'` first, and retry on failure.

**4. Deleting old rows from a huge table.**

```sql
DELETE FROM rides WHERE started_at < '2021-12-01';   -- 30 million rows
```

It runs for hours, writes a mountain of WAL, makes replicas lag, and leaves 30 million dead tuples for vacuum. Fix: partition by month and `DETACH`, then `DROP`, the old partitions.

**5. A relational model hidden in JSONB.**

```sql
CREATE TABLE rides (id bigint PRIMARY KEY, data jsonb);   -- rider, bike, fare all inside data
```

No foreign keys, no `NOT NULL`, no column types, and poor row estimates for every filter on `data`. Fix: real columns for what you filter, join and sum; JSONB only for the parts whose shape really varies.

**6. Never testing a restore.**

```bash
pg_dump spoke > /backups/spoke.sql   # runs every night; nobody has ever restored one
```

The day you need it, the file may be empty or the WAL archive may have a gap. Or the restore takes 6 hours against a 1-hour RTO. Fix: scheduled restore drills to a scratch server, with row checks and a timer.

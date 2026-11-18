## Hook

Two dispatchers plan the morning from photos of the Harbor rack. Each photo was taken at 08:05 and shows 6 bikes. The rule says Harbor keeps at least 5.

Each dispatcher thinks, "6 minus my one is 5. Fine." Each sends a truck for a different bike. Both photos were true when they were taken. Harbor still ends up with 4.

That is exactly what happened inside Spoke's database. Each transaction read a **snapshot**, a photo of the data. In this ticket you go inside the database to see how those photos are made, why they are so useful, and when they lie.

## Concept

### Where module 8 left off

In [Transactions](/t/module-08-backend/topic-10-transactions) you learned ACID and `@Transactional`. In [Concurrent writes](/t/module-08-backend/topic-15-concurrent-writes) you met the five isolation anomalies, the isolation levels, locking and deadlocks.

That was the outside view: what the database promises. This ticket is the inside view: how PostgreSQL keeps those promises. Three machines do the work:

| Machine | The promise it keeps |
|---|---|
| **write-ahead log** | durability: a committed change survives a crash |
| **MVCC**, many versions of each row | isolation without readers and writers blocking each other |
| **vacuum** | cleaning up the old versions MVCC leaves behind |

Once you see the machines, the Harbor puzzle explains itself. So does a lot of strange production behaviour.

### Pages and the buffer pool

PostgreSQL stores each table in files on disk. The files are split into **pages** of 8 KB each, and each page holds many rows. Indexes, from [How indexes work](/t/module-09b-data/topic-02-index-internals), are stored in pages too.

The database never edits a row in place on disk. It loads the whole page into memory, changes it there, and writes the page back later. The memory area that holds pages is the **buffer pool**. PostgreSQL calls it `shared_buffers`.

```callout jargon
A **page** is the fixed-size block a database reads and writes, 8 KB in PostgreSQL. The **buffer pool** is the memory that holds copies of pages. A page changed in memory but not yet written back is **dirty**.
```

Here is the problem. A busy morning changes thousands of pages, scattered all over the files. Writing each one to disk at every commit would be slow. Not writing them means a power cut loses them. The write-ahead log solves both.

### The write-ahead log

The **write-ahead log** (**WAL**) is one long file that only ever grows at the end. Every change gets a short record there, such as "page 812 of `bike`: B-1042 now at truck-3".

The design rests on one rule: **the log record reaches the disk before the changed page.** At commit, PostgreSQL forces the WAL to disk. The dirty pages can follow minutes later.

```animate
title: What a commit really writes
kind: scene
nodes:
  - { id: app, label: Spoke API, emoji: "🖥️", value: "UPDATE bike" }
  - { id: mem, label: Buffer pool, emoji: "🧠", value: "page 812 clean", col: 2 }
  - { id: wal, label: WAL file, emoji: "📜", value: "...", row: 1, col: 1 }
  - { id: table, label: Table file, emoji: "🗄️", value: "page 812 old", row: 1, col: 3 }
steps:
  - say: The API moves B-1042 to truck-3. PostgreSQL changes page 812 in memory only. The page is now dirty.
    send: { from: app, to: mem, label: "UPDATE" }
    set: { mem: "page 812 dirty" }
  - say: It also appends a small record describing the change to the WAL, still in memory.
    send: { from: mem, to: wal, label: "WAL record" }
  - say: The API says COMMIT. PostgreSQL appends a commit record and forces the WAL onto the disk with fsync.
    send: { from: app, to: wal, label: "COMMIT" }
    set: { wal: "...change, commit (on disk)" }
  - say: Only now does the API hear "committed". The table file on disk still holds the old page.
    send: { from: wal, to: app, label: "COMMIT ok", back: true }
    focus: [table]
  - say: Minutes later, a background writer or a checkpoint writes the dirty page to the table file.
    send: { from: mem, to: table, label: "write page 812" }
    set: { mem: "page 812 clean", table: "page 812 new" }
```

Why is this faster? A commit adds a few hundred bytes to the end of one file. That is a **sequential write**, the fastest kind of disk write. Writing scattered 8 KB pages would mean many **random writes**.

```callout jargon
**fsync** is the system call that tells the operating system to push a file's data out of its memory cache onto the physical disk, and wait until it is really there. Without it, "written" data can sit in the cache and vanish in a power cut.
```

### Commit latency is fsync latency

A commit cannot answer until its fsync finishes. So the time a commit takes depends mostly on the disk, not on your SQL.

- A fast local SSD finishes an fsync in a fraction of a millisecond. A network disk in the cloud is usually slower.
- **Group commit**: when 50 transactions commit at the same moment, one fsync can cover all their commit records. Busy databases rely on this.
- Many tiny transactions are slow for this reason. Inserting 1,000 GPS pings in 1,000 transactions means 1,000 waits. One transaction with 1,000 inserts means one.

PostgreSQL lets you trade safety for speed, per transaction:

```sql
SET LOCAL synchronous_commit = off;   -- this transaction only
```

The commit now answers before the fsync. A crash can lose the last fraction of a second of such commits, though the database stays consistent. That can suit GPS pings. It never suits payments or rentals.

```callout warning
`synchronous_commit = off` is not the same as `fsync = off`. Turning fsync off for the whole server can **corrupt** the database in a crash, not just lose recent commits. Never do it in production.
```

### Crash recovery and checkpoints

The power fails at 08:07. Some committed changes exist only in the WAL; their pages never reached the table files. On restart, PostgreSQL runs **crash recovery**: it reads the WAL and **redoes** every change it finds, in order. When it finishes, every committed transaction is back.

What about transactions that never committed? Their changes may be replayed too, but they have no commit record. MVCC, on the next cards, simply treats their row versions as never having existed.

How far back must recovery read? Not to the beginning of time, thanks to **checkpoints**. A checkpoint writes every dirty page to the table files, then writes a mark in the WAL. Recovery only replays the WAL after the last mark.

```callout jargon
A **checkpoint** is the moment when every dirty page has been written to the table files. WAL from before it is no longer needed for crash recovery. PostgreSQL starts one every 5 minutes by default, or sooner after a lot of writes.
```

The trade-off: frequent checkpoints mean short recovery but more disk writing. Rare checkpoints mean less writing but a longer restart. After each checkpoint, the first change to any page logs the whole page, so a page half-written during a crash can be repaired.

The WAL has two more jobs. Replicas copy the primary by replaying its WAL, and backups use archived WAL to restore to any moment. Later tickets cover both.

### MVCC: every row has versions

Now the second machine. When PostgreSQL runs an `UPDATE`, it does **not** overwrite the row. It writes a **new version** of the row and marks the old version as ended. A `DELETE` only marks the version as ended.

Each version, called a **tuple**, carries two hidden numbers:

- **xmin**: the id of the transaction that created this version.
- **xmax**: the id of the transaction that ended it, by an update or a delete. It is 0 while nobody has.

Here is the EBIKE fare row after two fare changes:

| cents_per_minute | xmin | xmax | what happened |
|---|---|---|---|
| 30 | 100 | 205 | created by transaction 100, replaced by 205 |
| 35 | 205 | 310 | created by 205, replaced by 310 |
| 40 | 310 | 0 | created by 310, still current |

```callout jargon
**MVCC** (multi-version concurrency control) means the database keeps several versions of a row, so each transaction can read the version that fits it. A **transaction id** is a number each writing transaction gets; newer ones get bigger numbers.
```

All three versions sit in the table until a cleanup removes the old ones. That cleanup is vacuum, near the end of this page.

### Snapshots: which version you see

A **snapshot** records which transactions had committed at the moment it was taken. A version is visible to you when:

1. the transaction that **created** it had committed when your snapshot was taken, and
2. the transaction that **ended** it had **not** committed by then, or there is none.

Step through the rule for a snapshot taken after 100 and 205 committed, while 310 was still running.

```animate
title: Which EBIKE fare does this snapshot see?
kind: code
code: |
  java.util.Set<Integer> done = java.util.Set.of(100, 205);
  boolean sees30 = done.contains(100) && !done.contains(205);
  boolean sees35 = done.contains(205) && !done.contains(310);
  boolean sees40 = done.contains(310);
  System.out.println(sees30 + " " + sees35 + " " + sees40);
steps:
  - line: 1
    vars: { done: "{100, 205}" }
    say: Our snapshot was taken when 100 and 205 had committed. Transaction 310 was still running.
  - line: 2
    vars: { sees30: false }
    say: Version 30 was created by 100, which had committed. But 205 ended it, and 205 had committed too. It is history.
  - line: 3
    vars: { sees35: true }
    say: Version 35 was created by 205, committed. Its ender, 310, had not committed yet. To us it is alive.
  - line: 4
    vars: { sees40: false }
    say: Version 40 was created by 310, still running when the photo was taken. It does not exist for us. Nobody has ended it.
  - line: 5
    out: "false true false"
    say: Exactly one version passes, 35. Other snapshots, taken at other moments, would pick other versions.
```

At most one version of a row passes the rule for any snapshot: none, if the row was deleted. No locks were needed to decide which.

### Readers never wait

Because old versions stay around, a reader never has to wait for a writer. It reads the version its snapshot allows. A writer never waits for a reader either. This is the big win of MVCC.

H2, the database this course runs, also uses MVCC, though its details differ from PostgreSQL's. Run this: a writer changes the fare and has not committed yet.

```java run
import java.sql.*;

public class Main {
    static final String URL = "jdbc:h2:mem:mvcc;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=500";

    public static void main(String[] args) throws SQLException {
        try (Connection writer = DriverManager.getConnection(URL);
             Connection reader = DriverManager.getConnection(URL)) {
            writer.createStatement().execute("""
                CREATE TABLE fare (bike_type VARCHAR(10) PRIMARY KEY, cents_per_minute BIGINT);
                INSERT INTO fare VALUES ('EBIKE', 30);
                """);
            writer.setAutoCommit(false);
            writer.createStatement().executeUpdate("UPDATE fare SET cents_per_minute = 35 WHERE bike_type = 'EBIKE'");
            System.out.println("writer changed EBIKE to 35, not committed yet");
            System.out.println("writer sees " + read(writer));
            System.out.println("reader sees " + read(reader) + " (no waiting)");
            writer.commit();
            System.out.println("writer committed");
            System.out.println("reader sees " + read(reader));
        }
    }

    static long read(Connection db) throws SQLException {
        ResultSet row = db.createStatement().executeQuery("SELECT cents_per_minute FROM fare WHERE bike_type = 'EBIKE'");
        row.next();
        return row.getLong(1);
    }
}
```

The reader got 30 at once, while the new 35 sat uncommitted. No dirty read, and no waiting.

One kind of waiting remains. **Two writers on the same row** still queue: the second waits for the first to commit or roll back. A card below shows what happens next.

### READ COMMITTED and REPEATABLE READ in PostgreSQL

The isolation level decides **when the snapshot is taken**. That one choice explains most of the table from Concurrent writes.

| Level | When the snapshot is taken | What you notice |
|---|---|---|
| `READ COMMITTED` (default) | a new one at the start of **every statement** | two SELECTs in one transaction can see different data |
| `REPEATABLE READ` | one, at the **first statement** of the transaction, kept until it ends | the whole transaction sees one frozen moment |

One snapshot per transaction has a name: **snapshot isolation**. PostgreSQL's `REPEATABLE READ` is snapshot isolation. It even stops phantoms, more than the SQL standard asks.

```callout jargon
**Snapshot isolation** means every read in a transaction sees the database as it was at one moment, plus the transaction's own changes.
```

Snapshot isolation is perfect for reports. A month-end report that runs ten queries sees one consistent moment. Without it, a ride could end between query 3 and query 7 and appear in one total but not the other.

Note the snapshot starts at the first statement, not at `BEGIN`. A transaction that begins, waits, then reads, gets a snapshot from when it read.

### When two writers hit the same row

Ana's transaction updates Harbor's station row. Before she commits, Ben's transaction tries to update the same row. Ben waits. What happens when Ana commits depends on Ben's level.

- **READ COMMITTED**: Ben's `UPDATE` re-reads the **newest** version of the row and checks its `WHERE` again. If it still matches, he updates that newest version. So `SET docked = docked - 1` works on Ana's result, and no update is lost.
- **REPEATABLE READ**: Ben's snapshot is older than Ana's change. Updating a row his snapshot cannot see would break the frozen moment, so PostgreSQL fails Ben's transaction with SQLState **`40001`**, a serialization failure. Ben must retry the whole transaction.

If Ana rolls back instead, Ben simply goes ahead in both cases.

H2 behaves like this too, in our runs. Under `REPEATABLE READ` it failed the second writer with SQLState `40001`, though its message says "Deadlock detected". So under `REPEATABLE READ` and above, a retry loop is part of the code, not an extra.

### Syntax: choosing the isolation level

```java nocompile
connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
```

Call it before the transaction's first statement. In SQL, and in Spring:

```sql
BEGIN ISOLATION LEVEL REPEATABLE READ;
-- or, as the first statement of a transaction:
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
```

```java nocompile
@Transactional(isolation = Isolation.SERIALIZABLE)
public void moveBike(String bikeId, String fromStation) { ... }
```

`SHOW default_transaction_isolation;` tells you what a PostgreSQL server starts with. Set the level per transaction, for the few that need it. Raising it for the whole server makes every transaction pay.

### Write skew: right checks, wrong result

Back to Harbor. Both dispatchers ran under `REPEATABLE READ`. Here is why snapshot isolation did not save them.

Snapshot isolation only notices a clash when **two transactions write the same row**. Ana moved B-1042. Ben moved B-1043. Different rows, so nothing clashed. Each decision read the set "bikes at Harbor", and each write changed that set. Neither snapshot could show the other's move.

```callout jargon
**Write skew** is when two transactions read the same data, each makes a decision from it, and each writes a **different** row. Each write is fine alone; together they break a rule.
```

Learn the shape, because it hides in many places:

- Two mechanics each see the other is on call, and both go home (from Concurrent writes).
- Two riders each see dock 4 at Uptown free for 09:00, and both reserve it as new rows.
- Two sign-ups each see the name `ada` is free, with no unique constraint, and both take it.

The shape is: **read a set, decide, write something that changes the set**. The worked example walks through Harbor step by step. The next three cards are the three fixes.

### Fix 1: SERIALIZABLE, with retries

PostgreSQL's `SERIALIZABLE` level uses **SSI** (serializable snapshot isolation). It runs like `REPEATABLE READ`, with snapshots and no extra blocking. On top, it tracks **who read what that someone else then wrote**.

When it spots a pattern that might not match any one-at-a-time order, it fails one transaction with SQLState **`40001`**. For Harbor, one dispatcher commits; the other gets `40001`, retries, sees 5 bikes, and refuses.

```callout jargon
**SSI** (serializable snapshot isolation) is snapshot isolation plus tracking of read-write dependencies. It fails a transaction instead of making others wait. A **false positive** is a failure where the order was actually safe.
```

- **Pros**: no rule to remember per code path. Every write skew the database can see is caught.
- **Cons**: some false positives, more failed transactions under load, and **every** caller must retry the whole transaction. Use the retry loop from [Concurrent writes](/t/module-08-backend/topic-15-concurrent-writes), on `40001`.

Not every database does this. In our runs, H2's `SERIALIZABLE` let the Harbor write skew through. Test the anomaly you care about on the database you really run.

### Fix 2: lock what the decision reads

The decision read "the bikes at Harbor". If every move out of Harbor first locks one shared row, moves queue up one by one. The natural row is Harbor's own row in the `station` table:

```sql
BEGIN;                                                     -- READ COMMITTED
SELECT id FROM station WHERE id = 'harbor' FOR UPDATE;     -- wait here for the other dispatcher
SELECT count(*) FROM bike WHERE station = 'harbor';        -- a fresh snapshot: sees their move
UPDATE bike SET station = 'truck-3' WHERE id = 'B-1043';   -- only if count - 1 >= 5
COMMIT;
```

Ben now waits at line 2 until Ana commits. His count runs after that, with a fresh `READ COMMITTED` snapshot, so it sees 5 and he refuses.

Three details make or break this fix:

- **The level matters.** Under `REPEATABLE READ`, Ben's snapshot may be older than his lock, and he still sees 6. Common mistake 2 shows a real run.
- **`FOR SHARE` is not enough.** Two share locks do not block each other, so both dispatchers pass at once.
- **Every path must lock.** One forgotten code path that moves bikes without the lock reopens the hole.

PostgreSQL refuses `FOR UPDATE` together with `count(*)`, because a count has no single row to lock. Locking the parent row avoids that.

### Fix 3: let a constraint hold the rule

Give `station` a `docked` counter, and let the database guard the minimum:

```sql
ALTER TABLE station ADD COLUMN docked INT NOT NULL;
ALTER TABLE station ADD CONSTRAINT keep_five_bikes CHECK (docked >= 5);
-- every move out of Harbor, in the same transaction as the bike update:
UPDATE station SET docked = docked - 1 WHERE id = 'harbor';
```

Now both moves **write the same row**. Write skew has become an ordinary same-row clash, and the database's normal rules handle it. Under `READ COMMITTED`, Ben's update waits, re-reads 5, makes 4, and the `CHECK` fails. Under `REPEATABLE READ`, he gets `40001`.

This trick has a name: **materialising the conflict**. You turn an invisible clash over a set into a visible clash over one row. The cost is keeping the counter correct on every path, so keep it in the same transaction as the bike move.

PostgreSQL has a stronger tool for bookings: an **exclusion constraint** can refuse two reservations of dock 4 whose time ranges overlap.

### MySQL's InnoDB does it differently

You will meet MySQL too. Its InnoDB engine uses the same ideas, built another way:

- **Clustered rows**: each row lives inside the primary key's B-tree, as you saw in How indexes work. Secondary indexes point to the primary key.
- **Undo log, not new tuples**: InnoDB updates the row in place and keeps the old version in an **undo log**. Old snapshots rebuild old versions from it. A **purge** thread cleans up instead of vacuum.
- **Redo log**: InnoDB's name for its write-ahead log.
- **`REPEATABLE READ` by default**. Plain `SELECT`s read the snapshot. But `UPDATE`, `DELETE` and `SELECT ... FOR UPDATE` read and lock the **newest** committed version. So there is no `40001` on a concurrent update, and a read-then-write can still lose an update.
- **Gap and next-key locks**: a locking read over a range also locks the **gaps** between index entries. A **next-key lock** is a row lock plus the gap before it. This stops other transactions inserting into the range, so it blocks phantoms. It also causes surprising waits and deadlocks on inserts.

Same SQL, different behaviour. Always check which engine and level you are on.

### Dead tuples, vacuum and bloat

Every `UPDATE` in PostgreSQL leaves a version behind. Once no snapshot can ever see it again, it is a **dead tuple**: wasted space. A busy morning of ride updates leaves thousands.

**VACUUM** finds dead tuples and marks their space free for new rows. It does not usually shrink the file; it makes room for reuse. **Autovacuum** is a background worker that runs it on each table once enough rows have changed.

```callout jargon
A **dead tuple** is a row version no transaction can see any more. **Bloat** is a table or index much bigger than its live data, because dead space piles up faster than vacuum frees it.
```

Bloat hurts in a quiet way. A table with 100 MB of live rides but 900 MB of pages makes every scan read ten times too much. Indexes bloat too.

- **`VACUUM FULL`** rewrites the table compactly, but holds a lock that blocks **all** reads and writes until it finishes. Tools such as `pg_repack` compact a table with only brief locks.
- **Transaction id wraparound**: transaction ids are 32-bit numbers and eventually wrap around. Vacuum "freezes" old rows so they stay visible. If it falls behind for a very long time, PostgreSQL stops accepting writes to protect your data.

A later ticket, Postgres in production, covers tuning autovacuum.

### Why long-running transactions hurt

Vacuum may only remove a version that **no running snapshot** could still need. So one transaction left open for an hour pins every version that died in that hour. That means every table, not just the ones it touched.

What one forgotten transaction does:

- **Vacuum cannot clean.** Dead tuples pile up across the database. Tables and indexes bloat, and queries slow down.
- **Locks stay held.** Rows it changed stay locked. A schema change such as `ALTER TABLE` queues behind it, and every other query then queues behind the `ALTER`.
- **Replicas suffer.** A long query on a replica can hold up the replay of the primary's WAL, so the replica lags. Or the replica cancels the query instead. If the replica reports its snapshots back, the primary's vacuum is held back instead.

The usual cause is code, not people: a transaction opened, then an HTTP call or a user's wait inside it. PostgreSQL shows such a session as **idle in transaction**.

Guards: keep transactions short, set `idle_in_transaction_session_timeout` and `statement_timeout`, and alert on the oldest `xact_start` in `pg_stat_activity`.

### Two-phase commit, and why services avoid it

What if one transaction must cover two databases? Databases offer **two-phase commit** (**2PC**). In PostgreSQL, each side runs `PREPARE TRANSACTION 'move-17'`: its changes are made durable and locked, but not yet visible. A coordinator then sends `COMMIT PREPARED 'move-17'` to every side, or a rollback if any side said no. In Java, the standard for it is called XA.

You saw in [Microservices](/t/module-09-architecture/topic-03-microservices) why services avoid it. The inside view adds one more reason. A prepared transaction is a long-running transaction that **survives a restart**. If the coordinator dies, its locks stay held and vacuum stays blocked until someone resolves it by hand.

That is why PostgreSQL ships with prepared transactions switched off: `max_prepared_transactions` is 0 by default. Between services, use a saga with local transactions, plus the outbox from [Messaging](/t/module-09-architecture/topic-04-messaging).

### In an interview

Database internals questions test whether you can explain behaviour, not just name levels. Spot them when you hear "durable", "crash", "isolation", "concurrent", "MVCC", "why is my table so big".

- **"How does a database survive a crash without losing commits?"** The WAL: log first, fsync at commit, pages later, redo from the last checkpoint.
- **"What is MVCC, and why do readers not block writers?"** Row versions with creator and ender ids, snapshots, and the visibility rule.
- **"What does snapshot isolation not prevent?"** Write skew. Tell the on-call mechanics or the Harbor story, then give the three fixes.
- **"READ COMMITTED versus REPEATABLE READ in PostgreSQL?"** A snapshot per statement versus per transaction; what happens on a concurrent update.
- **"Our table is ten times bigger than its data. Why?"** Dead tuples and bloat, autovacuum falling behind, often because of a long-running transaction.

For system design, say which isolation level each write path needs, and how you retry `40001`.

## Worked example

Here is the Harbor incident, step by step, as it ran under `REPEATABLE READ`. Harbor's minimum is 5, and it held 6 bikes.

```animate
title: Write skew at Harbor
kind: scene
nodes:
  - { id: ana, label: "Ana's transaction", emoji: "🧑‍💼", value: "not started" }
  - { id: db, label: "Harbor in the database", emoji: "🗄️", value: "6 bikes", col: 2 }
  - { id: ben, label: "Ben's transaction", emoji: "🧑‍💼", value: "not started", col: 4 }
steps:
  - say: At 08:05 both dispatchers open a move. Each transaction counts Harbor's bikes, and each count takes a snapshot.
    send: [{ from: ana, to: db, label: "count" }, { from: ben, to: db, label: "count" }]
    set: { ana: "snapshot: 6", ben: "snapshot: 6" }
  - say: Ana checks 6 - 1 >= 5. True. She moves B-1042 to truck-3, writing B-1042's row.
    send: { from: ana, to: db, label: "UPDATE B-1042" }
  - say: Ben checks his own snapshot, 6 - 1 >= 5. True. He moves B-1043, a different row.
    send: { from: ben, to: db, label: "UPDATE B-1043" }
  - say: Ana commits. Nobody else wrote B-1042, so there is no conflict.
    send: { from: ana, to: db, label: "COMMIT" }
    set: { db: "5 bikes", ana: committed }
  - say: Ben commits. Nobody else wrote B-1043 either. Snapshot isolation only compares rows written by both.
    send: { from: ben, to: db, label: "COMMIT" }
    set: { db: "4 bikes", ben: committed }
  - say: Harbor has 4. Both checks were true for their own snapshot. That is write skew.
    focus: [db]
```

Read the timeline as a table, too:

| Time | Ana | Ben | Harbor really holds |
|---|---|---|---|
| 08:05:00.10 | count: 6 (snapshot) | | 6 |
| 08:05:00.12 | | count: 6 (snapshot) | 6 |
| 08:05:00.15 | move B-1042 | | 6 (Ana not committed) |
| 08:05:00.16 | | move B-1043 | 6 |
| 08:05:00.20 | commit | | 5 |
| 08:05:00.21 | | commit | **4** |

**What each fix changes.**

1. **`SERIALIZABLE`** in PostgreSQL: one transaction, here Ben's, fails with `40001`, because each read what the other wrote. His retry counts 5 and refuses.
2. **Lock Harbor's row** under `READ COMMITTED`: Ben waits at his lock until Ana commits. His count then sees 5.
3. **A `docked` counter with a `CHECK`**: both transactions write Harbor's row, so the clash is now visible.

Spoke chose fix 3. The counter also makes "how many bikes at Harbor?" a single-row read for the app. Here it is on H2. Ben's transaction still believes Harbor has 6:

```java
import java.sql.*;

public class Main {
    static final String URL = "jdbc:h2:mem:harbor;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=500";

    public static void main(String[] args) throws SQLException {
        try (Connection setup = DriverManager.getConnection(URL);
             Connection ana = DriverManager.getConnection(URL);
             Connection ben = DriverManager.getConnection(URL)) {
            setup.createStatement().execute("""
                CREATE TABLE station (
                    id     VARCHAR(20) PRIMARY KEY,
                    docked INT NOT NULL,
                    CONSTRAINT keep_five_bikes CHECK (docked >= 5)
                );
                INSERT INTO station VALUES ('harbor', 6);
                """);
            ana.setAutoCommit(false);                      // both stay at READ COMMITTED
            ben.setAutoCommit(false);
            System.out.println("Ana sees " + docked(ana) + ", Ben sees " + docked(ben));

            takeOne(ana);
            ana.commit();
            System.out.println("Ana moved a bike. Harbor has " + docked(setup));
            try {
                takeOne(ben);                              // Ben still believes Harbor has 6
                ben.commit();
                System.out.println("Ben moved a bike");
            } catch (SQLException e) {
                ben.rollback();
                System.out.println("Ben refused with SQLState " + e.getSQLState());
            }
            System.out.println("Harbor has " + docked(setup));
        }
    }

    static int docked(Connection db) throws SQLException {
        ResultSet row = db.createStatement().executeQuery("SELECT docked FROM station WHERE id = 'harbor'");
        row.next();
        return row.getInt(1);
    }

    static void takeOne(Connection db) throws SQLException {
        db.createStatement().executeUpdate("UPDATE station SET docked = docked - 1 WHERE id = 'harbor'");
        // the real app also runs: UPDATE bike SET station = 'truck-3' WHERE id = ?
    }
}
```

It prints:

```text
Ana sees 6, Ben sees 6
Ana moved a bike. Harbor has 5
Ben refused with SQLState 23513
Harbor has 5
```

Line by line:

1. **The `CHECK` constraint** writes the rule into the table, where no code path can skip it.
2. **Both read 6.** Ben's earlier read is stale by the time he writes, exactly as in the incident.
3. **`docked = docked - 1`** computes from the newest version of the row, not from Ben's earlier read. Under `READ COMMITTED` that gives 5 - 1 = 4.
4. **`23513`** is the SQLState for a check violation. The app catches it and tells Ben "Harbor is at its minimum".
5. **The rollback** undoes Ben's whole move, including his bike update.

## Try it

This program replays the incident with two connections under `REPEATABLE READ`. Run it, then fix it.

```java run
import java.sql.*;

public class Main {
    static final String URL = "jdbc:h2:mem:dispatch;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=500";
    static final int MINIMUM = 5;

    public static void main(String[] args) throws SQLException {
        try (Connection setup = DriverManager.getConnection(URL);
             Connection ana = DriverManager.getConnection(URL);
             Connection ben = DriverManager.getConnection(URL)) {
            setup.createStatement().execute("""
                CREATE TABLE station (id VARCHAR(20) PRIMARY KEY);
                INSERT INTO station VALUES ('harbor');
                CREATE TABLE bike (id VARCHAR(10) PRIMARY KEY, station VARCHAR(20));
                INSERT INTO bike VALUES ('B-1042', 'harbor'), ('B-1043', 'harbor'), ('B-1044', 'harbor'),
                                        ('B-1045', 'harbor'), ('B-1046', 'harbor'), ('B-1047', 'harbor');
                """);
            for (Connection dispatcher : new Connection[] {ana, ben}) {
                dispatcher.setAutoCommit(false);
                dispatcher.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            }

            long anaSees = count(ana);                     // Ana's snapshot: 6 bikes
            long benSees = count(ben);                     // Ben's snapshot: also 6
            System.out.println("Ana sees " + anaSees + ", Ben sees " + benSees);
            if (anaSees - 1 >= MINIMUM) move(ana, "B-1042");
            if (benSees - 1 >= MINIMUM) move(ben, "B-1043");
            ana.commit();
            ben.commit();
            System.out.println("Harbor now has " + count(setup) + " bikes. Minimum: " + MINIMUM);
        }
    }

    // Fix it: call this first in each transaction, and switch to READ_COMMITTED
    static void lockHarbor(Connection db) throws SQLException {
        db.createStatement().executeQuery("SELECT id FROM station WHERE id = 'harbor' FOR UPDATE").next();
    }

    static long count(Connection db) throws SQLException {
        ResultSet row = db.createStatement().executeQuery("SELECT COUNT(*) FROM bike WHERE station = 'harbor'");
        row.next();
        return row.getLong(1);
    }

    static void move(Connection db, String bikeId) throws SQLException {
        db.createStatement().executeUpdate("UPDATE bike SET station = 'truck-3' WHERE id = '" + bikeId + "'");
    }
}
```

1. Run it. Harbor ends with 4 bikes, though both checks passed.
2. Change both connections to `TRANSACTION_SERIALIZABLE`. On H2 the skew still gets through. PostgreSQL would fail one transaction with `40001`.
3. Apply fix 2. Use `TRANSACTION_READ_COMMITTED`. Let Ana call `lockHarbor`, count, move and commit. Then let Ben do the same. Ben now counts 5 and must refuse.
4. Call `lockHarbor(ben)` while Ana still holds the lock. Both connections share one thread, so Ben cannot wait for Ana: H2 gives up after `LOCK_TIMEOUT`. On a real server, Ben would simply wait.

## Recap

- The write-ahead log makes commits durable: log record first, fsync at commit, pages later. Commit latency is fsync latency. Crash recovery redoes the WAL from the last checkpoint.
- PostgreSQL's MVCC keeps versions of each row, marked with the transaction that created them (xmin) and ended them (xmax). A snapshot decides which version you see, so readers never wait for writers.
- `READ COMMITTED` takes a snapshot per statement; `REPEATABLE READ` takes one per transaction, which is snapshot isolation. A concurrent update makes `REPEATABLE READ` fail with `40001`.
- Snapshot isolation lets write skew through. Fix it with `SERIALIZABLE` plus retries, a `FOR UPDATE` lock on the row the decision needs, or a constraint.
- Dead tuples need vacuum, and long-running or prepared transactions block it, causing bloat, held locks and lagging replicas. Between services, prefer sagas to two-phase commit.

## Common mistakes

**1. Trusting a snapshot for a decision about a set.**

```java nocompile
long docked = countBikesAt("harbor");          // a snapshot, perhaps already stale
if (docked - 1 >= 5) {
    moveBike("B-1043", "truck-3");             // writes a different row from the other dispatcher
}
```

Symptom: in the Try it run, both checks passed and Harbor ended with 4 bikes. No error appeared anywhere, under `REPEATABLE READ` or H2's `SERIALIZABLE`. Fix: make the rule visible to the database. Use a constraint on a counter row, a `FOR UPDATE` lock on the parent row, or PostgreSQL's `SERIALIZABLE` with retries.

**2. Locking the parent row under REPEATABLE READ, after the snapshot was taken.**

```java nocompile
ben.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
countAllBikes(ben);       // first statement: the snapshot is taken here
lockHarbor(ben);          // waits until Ana commits...
countBikesAt(ben, "harbor");   // ...but still reads the old snapshot
```

Symptom, from a real H2 run of exactly this order:

```text
Ben locked Harbor and counts 6
Ben moves a bike
```

The lock worked, but the snapshot came from before Ana's commit. Fix: use the lock under `READ COMMITTED`, where the count after the lock takes a fresh snapshot. Or update the locked row too, which turns a clash into a `40001`.

**3. Running SERIALIZABLE without a retry loop.**

```java nocompile
@Transactional(isolation = Isolation.SERIALIZABLE)
public void moveBike(String bikeId, String fromStation) { ... }   // nobody catches 40001
```

Symptom: at rush hour, some dispatchers get a 500 error, and the logs show serialization failures with SQLState `40001`. The database did its job; the code did not finish it. Fix: wrap the whole transaction in a retry loop, capped at about 3 attempts with a short random wait. Retry only `40001` and `40P01`.

**4. Holding a transaction open across a slow call.**

```java nocompile
@Transactional
public void endRide(String rentalId) {
    Rental rental = rentals.lockForUpdate(rentalId);
    paymentClient.charge(rental.fareCents());   // an HTTP call, up to 30 seconds
    rental.markPaid();
}
```

Symptom: when the payment provider slows down, sessions pile up as "idle in transaction". Row locks are held, vacuum falls behind and tables bloat. Fix: do slow calls outside the transaction. Commit "ride ended", call the provider, then record the payment in a second short transaction, or use a saga.

**5. Reaching for VACUUM FULL at rush hour.**

```sql
VACUUM FULL rental;
```

Symptom: every query that touches `rental` freezes until the rewrite finishes, which on a big table can take many minutes. `VACUUM FULL` holds a lock that blocks all reads and writes. Fix: let autovacuum keep up, and find what blocked it, often a long transaction. To compact a bloated table online, use a tool such as `pg_repack`.

**6. Turning fsync off to make commits faster.**

```sql
-- postgresql.conf
fsync = off
```

Symptom: everything is fast, until a power cut or a crash. Then the database can come back corrupted, with no way to tell which pages are wrong. Fix: keep `fsync = on`. For data you can afford to lose, such as GPS pings, use `SET LOCAL synchronous_commit = off`. Or batch many rows into one commit.

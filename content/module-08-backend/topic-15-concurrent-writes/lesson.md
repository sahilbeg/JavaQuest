## Hook

Two shop assistants share one stockroom. A customer asks each of them for the last red bike at the same moment. Both check the shelf, and both see one bike. Both say "It's yours!", take payment, and walk to the shelf. One of them comes back empty-handed.

Since Spoke added a second server, that happens with bikes at rush hour. Each server checks that B-1042 is free, then rents it. In this ticket you learn why, and four ways to make sure exactly one request wins.

## Concept

### Why Java locks stop helping

In [synchronization](/t/module-05-advanced-java/topic-02-synchronization) you stopped two threads from renting one bike with `synchronized`. That lock lives in the memory of one JVM.

Spoke now runs two servers behind a load balancer. They are two separate JVMs, often on two machines. A lock in server A's memory means nothing to server B.

The one thing both servers share is the database. So the database must settle the race.

```callout jargon
A **race condition** is a bug where the result depends on which of two actions happens first. **Concurrent** means at the same time, or overlapping in time.
```

### Check, then act: the race

Here is the code each server ran. It reads the bike's status, decides in Java, then writes.

```animate
title: Two servers rent B-1042 at once
kind: scene
nodes:
  - { id: srva, label: Server A, emoji: "🖥️", value: "rider r-17" }
  - { id: db, label: Database, emoji: "🗄️", value: "B-1042 AVAILABLE", col: 2 }
  - { id: srvb, label: Server B, emoji: "🖥️", value: "rider r-23", col: 4 }
steps:
  - say: Two riders tap Unlock on B-1042. The load balancer sends one request to each server.
    focus: [srva, srvb]
  - say: Server A reads the bike. It is AVAILABLE.
    send: { from: srva, to: db, label: "SELECT status" }
    set: { srva: "saw AVAILABLE" }
  - say: A moment later, server B reads it too. Nothing has changed yet, so it also sees AVAILABLE.
    send: { from: srvb, to: db, label: "SELECT status" }
    set: { srvb: "saw AVAILABLE" }
  - say: Server A rents the bike to r-17 and answers 201.
    send: { from: srva, to: db, label: "UPDATE rider = r-17" }
    set: { db: "B-1042 r-17" }
  - say: Server B acts on its old answer. It overwrites the rider with r-23 and also answers 201.
    send: { from: srvb, to: db, label: "UPDATE rider = r-23" }
    set: { db: "B-1042 r-23" }
  - say: Both riders paid, and both were told "Ride started". The check was true when it ran, but stale when the write landed.
    focus: [db]
```

This shape is called **check-then-act**. The gap between the check and the act can be a millisecond, but at rush hour, a millisecond is enough.

### The lost update

A cousin of that race hit the fare table. Two ops editors raise the EBIKE fare at the same time. Each reads the price, adds to it in Java, and writes it back.

```animate
title: One change disappears
kind: code
code: |
  long fare = 30;
  long seenByA = fare;
  long seenByB = fare;
  fare = seenByA + 5;
  fare = seenByB + 2;
  System.out.println(fare);
steps:
  - line: 1
    vars: { fare: 30 }
    say: The EBIKE fare in the database is 30 cents a minute.
  - line: 2
    vars: { seenByA: 30 }
    say: Editor A opens the fare page and reads 30.
  - line: 3
    vars: { seenByB: 30 }
    say: Editor B opens it too, before A saves. B also reads 30.
  - line: 4
    vars: { fare: 35 }
    say: A saves 30 + 5. The fare is 35.
  - line: 5
    vars: { fare: 32 }
    say: B saves 30 + 2, computed from its old copy. A's change is gone without a trace.
  - line: 6
    out: "32"
    say: The fare should be 37. It is 32. Nobody saw an error.
```

The same thing happens with two real database connections, each in its own transaction:

```java run
import java.sql.*;

public class Main {
    static final String URL = "jdbc:h2:mem:lost;DB_CLOSE_DELAY=-1";

    public static void main(String[] args) throws SQLException {
        try (Connection setup = DriverManager.getConnection(URL);
             Connection serverA = DriverManager.getConnection(URL);
             Connection serverB = DriverManager.getConnection(URL)) {
            setup.createStatement().execute("""
                CREATE TABLE fare (bike_type VARCHAR(10) PRIMARY KEY, cents_per_minute BIGINT);
                INSERT INTO fare VALUES ('EBIKE', 30);
                """);
            serverA.setAutoCommit(false);
            serverB.setAutoCommit(false);

            long seenByA = read(serverA);              // editor A opens the page: 30
            long seenByB = read(serverB);              // editor B opens it too: 30
            write(serverA, seenByA + 5);               // A saves 35
            serverA.commit();
            write(serverB, seenByB + 2);               // B saves 32, from its old 30
            serverB.commit();

            System.out.println("A read " + seenByA + ", B read " + seenByB);
            System.out.println("EBIKE now costs " + read(setup) + " cents a minute");
        }
    }

    static long read(Connection db) throws SQLException {
        ResultSet row = db.createStatement().executeQuery("SELECT cents_per_minute FROM fare WHERE bike_type = 'EBIKE'");
        row.next();
        return row.getLong(1);
    }

    static void write(Connection db, long cents) throws SQLException {
        db.createStatement().executeUpdate("UPDATE fare SET cents_per_minute = " + cents + " WHERE bike_type = 'EBIKE'");
    }
}
```

Transactions did not help. Each one was atomic, but they overlapped. That is what **isolation**, the I in ACID, is about.

### Five ways overlapping transactions go wrong

These are called **isolation anomalies**. Each has a Spoke example.

| Anomaly | In plain words | Spoke example |
|---|---|---|
| **Dirty read** | You read a change that is not committed yet, and then it is rolled back. | A report counts a rental that was cancelled a second later. |
| **Non-repeatable read** | You read one row twice in a transaction and get two answers. | A receipt reads the EBIKE fare as 30, then as 35 when it adds tax. |
| **Phantom** | You run the same search twice and new rows appear. | "Bikes at Harbor" finds 7, then 8 when the receipt prints. |
| **Lost update** | Two read-change-write cycles overlap, and one change vanishes. | The fare ends at 32 instead of 37. |
| **Write skew** | Two transactions each check a rule, then each change a different row, together breaking it. | "At least one mechanic on call": two mechanics each see the other is on call and both go home. |

Write skew is the sneakiest. Neither transaction changed the same row, so nothing seems to clash.

### Isolation levels

The **isolation level** decides which anomalies a database lets through. Higher levels block more, but cost more waiting and more failed transactions. This table is for PostgreSQL, a very common production database.

| Level (PostgreSQL) | Dirty read | Non-repeatable read | Phantom | Lost update | Write skew |
|---|---|---|---|---|---|
| `READ COMMITTED` (the default) | blocked | possible | possible | possible | possible |
| `REPEATABLE READ` | blocked | blocked | blocked | the second writer gets an error | possible |
| `SERIALIZABLE` | blocked | blocked | blocked | blocked | blocked: one transaction fails |

At the higher levels, the database does not wait its way out of a clash. It makes one transaction fail with a **serialization failure**, SQLState `40001`. Your code must then run that whole transaction again.

Other databases differ in the details. H2, which this course uses, also defaults to `READ COMMITTED`. Its `REPEATABLE READ` stops non-repeatable reads, as this run shows.

```java run
import java.sql.*;

public class Main {
    static final String URL = "jdbc:h2:mem:iso;DB_CLOSE_DELAY=-1";

    public static void main(String[] args) throws SQLException {
        try (Connection report = DriverManager.getConnection(URL);
             Connection editor = DriverManager.getConnection(URL)) {
            report.createStatement().execute("""
                CREATE TABLE fare (bike_type VARCHAR(10) PRIMARY KEY, cents_per_minute BIGINT);
                INSERT INTO fare VALUES ('EBIKE', 30);
                """);
            report.setAutoCommit(false);
            int[] levels = {Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ};
            String[] names = {"READ COMMITTED", "REPEATABLE READ"};
            for (int i = 0; i < levels.length; i++) {
                report.setTransactionIsolation(levels[i]);
                long first = read(report);
                editor.createStatement().executeUpdate("UPDATE fare SET cents_per_minute = cents_per_minute + 5");
                long second = read(report);
                report.commit();
                System.out.println(names[i] + ": read " + first + ", then " + second);
            }
        }
    }

    static long read(Connection db) throws SQLException {
        ResultSet row = db.createStatement().executeQuery("SELECT cents_per_minute FROM fare WHERE bike_type = 'EBIKE'");
        row.next();
        return row.getLong(1);
    }
}
```

Under `READ COMMITTED` the report saw 30, then 35. Under `REPEATABLE READ` the editor raised the fare to 40, but the report kept seeing 35 until it committed.

```callout warning
In our runs, H2's `SERIALIZABLE` still let the two-mechanics write skew through, where PostgreSQL would fail one transaction. Never trust a level's name alone: test the anomaly you care about on the database you really use.
```

MySQL is the big exception to "READ COMMITTED by default". Its InnoDB engine defaults to `REPEATABLE READ`, so the same code can behave differently on MySQL and PostgreSQL. Always check which level your database starts with.

Most teams keep `READ COMMITTED` and protect the few risky writes with the four tools below.

### Syntax: the atomic conditional update

```sql
UPDATE bike SET status = 'RENTED', rider_id = ? WHERE id = ? AND status = 'AVAILABLE'
```

Move the check **into** the write. The database runs one statement on one row at a time, under a row lock. So the check and the act happen as one step, and nothing can sneak between them.

Then read the result. `executeUpdate()` returns the number of rows changed.

- **1 row:** you rented the bike. Answer `201 Created`.
- **0 rows:** the bike was not available any more. Answer `409 Conflict`.

```java run
import java.sql.*;

public class Main {
    static final String URL = "jdbc:h2:mem:desk;DB_CLOSE_DELAY=-1";

    public static void main(String[] args) throws SQLException {
        try (Connection serverA = DriverManager.getConnection(URL);
             Connection serverB = DriverManager.getConnection(URL)) {
            serverA.createStatement().execute("""
                CREATE TABLE bike (id VARCHAR(10) PRIMARY KEY, status VARCHAR(12), rider_id VARCHAR(20));
                INSERT INTO bike VALUES ('B-1042', 'AVAILABLE', NULL);
                """);
            int a = rent(serverA, "B-1042", "r-17");
            int b = rent(serverB, "B-1042", "r-23");
            System.out.println("server A changed " + a + " row -> " + (a == 1 ? "201 Created" : "409 Conflict"));
            System.out.println("server B changed " + b + " rows -> " + (b == 1 ? "201 Created" : "409 Conflict"));
        }
    }

    static int rent(Connection db, String bikeId, String riderId) throws SQLException {
        try (PreparedStatement update = db.prepareStatement(
                "UPDATE bike SET status = 'RENTED', rider_id = ? WHERE id = ? AND status = 'AVAILABLE'")) {
            update.setString(1, riderId);
            update.setString(2, bikeId);
            return update.executeUpdate();
        }
    }
}
```

What if both UPDATEs arrive at the very same instant? The first one locks the row. The second waits for that lock. When the first commits, the second checks its `WHERE` again, finds `RENTED`, and changes 0 rows. We checked that H2 behaves the same way.

The cost: one statement, no extra reads, no retries. Use it whenever the rule fits in one `WHERE`.

### Syntax: optimistic locking with a version

```sql
UPDATE fare SET cents_per_minute = ?, version = version + 1 WHERE bike_type = ? AND version = ?
```

Some changes need a person to read first, think, then save. The fare editor is one. You cannot keep a lock while someone stares at a screen.

So add a `version` column. Every save does two things: it checks the version the editor read, and it adds 1.

1. Both editors read the fare at version 1.
2. Editor A saves with `AND version = 1`. One row changes, and the row is now at version 2.
3. Editor B saves with `AND version = 1`. No row matches, so 0 rows change.
4. B's app shows "someone changed this first", reloads, and lets B try again.

```callout jargon
**Optimistic locking** takes no lock while you work. It hopes nobody else changes the row, and checks at save time. **Pessimistic locking** locks the row first, expecting a clash.
```

It is the database version of the `If-Match` header from [REST design](/t/module-08-backend/topic-02-rest-design): the ETag and the version number are the same idea.

### Syntax: @Version in JPA

```java
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
class Fare {
    @Id
    String bikeType;
    long centsPerMinute;
    @Version
    long version;
}
```

With `@Version` on a field, Hibernate adds the version check and the `+ 1` to every update it writes. When the update changes 0 rows, it throws an `OptimisticLockException`.

In a Spring app you catch Spring's wrapper for it, `OptimisticLockingFailureException`. Then answer `409 Conflict`, or reload and try again.

### Syntax: pessimistic locking with FOR UPDATE

```sql
SELECT status FROM bike WHERE id = ? FOR UPDATE
```

`FOR UPDATE` reads the row **and locks it** until your transaction ends. Another transaction that wants to lock or change that row must wait. Plain reads still work.

```java run
import java.sql.*;

public class Main {
    static final String URL = "jdbc:h2:mem:pessimistic;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=500";

    public static void main(String[] args) throws SQLException {
        try (Connection serverA = DriverManager.getConnection(URL);
             Connection serverB = DriverManager.getConnection(URL)) {
            serverA.createStatement().execute("""
                CREATE TABLE bike (id VARCHAR(10) PRIMARY KEY, status VARCHAR(12));
                INSERT INTO bike VALUES ('B-1042', 'AVAILABLE');
                """);
            serverA.setAutoCommit(false);
            serverB.setAutoCommit(false);

            ResultSet row = serverA.createStatement().executeQuery("SELECT status FROM bike WHERE id = 'B-1042' FOR UPDATE");
            row.next();
            System.out.println("A locked B-1042 and sees " + row.getString(1));

            ResultSet plain = serverB.createStatement().executeQuery("SELECT status FROM bike WHERE id = 'B-1042'");
            plain.next();
            System.out.println("B's plain read still works: " + plain.getString(1));

            try {
                serverB.createStatement().executeQuery("SELECT status FROM bike WHERE id = 'B-1042' FOR UPDATE");
                System.out.println("B locked it too");
            } catch (SQLException e) {
                System.out.println("B gave up waiting: " + e.getMessage().split(";")[0]);
            }
            serverA.commit();
        }
    }
}
```

A real server B would wait until A commits, then see `RENTED`. Here both connections run on one thread, so B can only wait. `LOCK_TIMEOUT=500` makes it give up after half a second.

In Spring Data JPA, ask for the same lock on a repository method:

```java
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Entity
class Bike {
    @Id
    String id;
    String status;
}

interface BikeRepository extends JpaRepository<Bike, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bike b where b.id = :id")
    Optional<Bike> findForUpdate(@Param("id") String id);
}
```

Two extra words change what happens when the row is already locked. PostgreSQL and MySQL 8 both support them.

```sql
SELECT id FROM repair_job WHERE status = 'WAITING' ORDER BY id LIMIT 1 FOR UPDATE SKIP LOCKED;
SELECT status FROM bike WHERE id = ? FOR UPDATE NOWAIT;
```

- `SKIP LOCKED` skips rows someone else has locked, instead of waiting. Five mechanics' phones can each grab a different waiting repair job, with no queue forming. This is the classic way to build a job queue in a database.
- `NOWAIT` fails at once instead of waiting. Use it when "busy, try again" is a better answer than a frozen request.

Use `FOR UPDATE` when several steps must see the row unchanged, such as reading a wallet, checking the balance and writing a payment. Keep the locked transaction short: everyone else waits behind it.

### A unique constraint as a lock

The rule "a rider has at most one active rental" spans many rows, so no single-row `WHERE` can guard it. A unique constraint can.

Give the rental table an `active_rider` column. It holds the rider's id while the ride is on, and `NULL` after it ends. Make it `UNIQUE`. A unique column may hold many `NULL`s, because `NULL` never equals `NULL`.

```java run
import java.sql.*;

public class Main {
    public static void main(String[] args) throws SQLException {
        try (Connection db = DriverManager.getConnection("jdbc:h2:mem:ledger");
             Statement sql = db.createStatement()) {
            sql.execute("""
                CREATE TABLE rental (
                    id           VARCHAR(20) PRIMARY KEY,
                    rider_id     VARCHAR(20) NOT NULL,
                    active_rider VARCHAR(20),
                    CONSTRAINT one_active_rental_per_rider UNIQUE (active_rider)
                )""");
            String[] steps = {
                "INSERT INTO rental VALUES ('rental-17', 'r-17', 'r-17')",
                "INSERT INTO rental VALUES ('rental-18', 'r-17', 'r-17')",
                "UPDATE rental SET active_rider = NULL WHERE id = 'rental-17'",
                "INSERT INTO rental VALUES ('rental-18', 'r-17', 'r-17')"
            };
            for (String step : steps) {
                try {
                    sql.executeUpdate(step);
                    System.out.println("ok      " + step);
                } catch (SQLException e) {
                    System.out.println(e.getSQLState() + "   " + step);
                }
            }
        }
    }
}
```

The second insert fails with `23505`, a unique violation. The database checks the index under a lock, so two servers can never both insert. Catch the `SQLException`, check `"23505".equals(e.getSQLState())`, and answer `409 Conflict`. Checking the SQLState works with every driver; H2's driver also throws the subclass `SQLIntegrityConstraintViolationException`, but PostgreSQL's does not.

PostgreSQL can say this more directly with a **partial unique index**, which only covers rows matching a `WHERE`:

```sql
CREATE UNIQUE INDEX one_active_rental_per_rider ON rental(rider_id) WHERE ended_at IS NULL;
```

H2 has no partial indexes, which is why this course uses the `active_rider` column trick. It works on PostgreSQL, MySQL, Oracle and H2. SQL Server is the odd one out: its UNIQUE allows only one NULL.

### Deadlocks

Locks bring a new danger. Server A locks B-1042, then wants B-1088. Server B locks B-1088, then wants B-1042. Each waits for the other, forever.

```callout jargon
A **deadlock** is two or more transactions each waiting for a lock the other holds, so none can move. The database spots the cycle and kills one of them.
```

The killed transaction is rolled back. Here is what H2 says, from a real run with two threads:

```text
Deadlock detected. The current transaction was rolled back. Details: "BIKE"; SQL statement:
UPDATE bike SET status='MAINTENANCE' WHERE id='B-1042' [40001-240]
```

Note "the current transaction was rolled back". Every statement before it is gone too. So you cannot just repeat the failed statement. You must run the **whole transaction** again, from its first read.

Two habits make deadlocks rare:

- Lock rows in the same order everywhere, for example by bike id, smallest first.
- Keep transactions short, with no network calls or user waits inside them.

### Syntax: retry the whole transaction

```java nocompile
for (int attempt = 1; ; attempt++) {
    try {
        // every read and write of the transaction, from the start
        connection.commit();
        return result;
    } catch (SQLException e) {
        connection.rollback();
        boolean retryable = "40001".equals(e.getSQLState()) || "40P01".equals(e.getSQLState());
        if (!retryable || attempt >= maxAttempts) {
            throw e;
        }
    }
}
```

Retry exactly two SQLStates. `40001` is a serialization failure, and H2 also uses it for deadlocks. `40P01` is PostgreSQL's deadlock code. Other codes starting with `40` mean the outcome is unclear, so do not retry them blindly. A unique violation fails the same way every time, so retrying it is pointless.

Cap the attempts, often at 3. Add a short random wait between tries, as in [REST design](/t/module-08-backend/topic-02-rest-design)'s backoff and jitter.

### Which one to choose

| Situation | Tool | Why |
|---|---|---|
| The rule fits in one row's `WHERE` | conditional `UPDATE`, check the row count | cheapest: one statement, no lock held between steps, no retries |
| A person reads, edits, then saves | optimistic locking (`version`, `@Version`) | no lock held while they think; clashes are rare |
| Several steps must see rows unchanged, and clashes are common | pessimistic locking (`FOR UPDATE`, `@Lock`) | waiting is cheaper than failing and retrying |
| A rule across many rows ("one active rental") | `UNIQUE` constraint | the database checks it atomically |
| A rule the tools above cannot express | `SERIALIZABLE` plus a retry loop | the database finds the clash, you retry |

Whichever you choose, the decision lives in the database. Never in a check that ran earlier in Java.

### In an interview

Concurrency control comes up in system design and in "two users do X at once" questions. Spot it when you hear double booking, overselling, inventory, balances, counters, "two servers", or "at the same time".

- **Book the last seat** (or ticket, room, bike) without overselling: conditional update or a unique constraint.
- **Two people edit the same document**: optimistic locking with a version, then reload or merge.
- **Transfer money between two accounts**: one transaction, rows locked in a fixed order to avoid deadlocks, and a retry loop.
- **A like counter**: `UPDATE post SET likes = likes + 1`, never read-add-write in the app.
- **Which isolation level, and what does it prevent?** Name the anomalies, and say the default is `READ COMMITTED` in PostgreSQL.

## Worked example

Itachi's ticket mentions a fare change that vanished. Here is the fare editor with optimistic locking, from read to `409 Conflict` to a successful retry.

```java run
import java.sql.*;

public class Main {
    static final String URL = "jdbc:h2:mem:fares;DB_CLOSE_DELAY=-1";

    record Fare(long cents, long version) {}

    public static void main(String[] args) throws SQLException {
        try (Connection serverA = DriverManager.getConnection(URL);
             Connection serverB = DriverManager.getConnection(URL)) {
            serverA.createStatement().execute("""
                CREATE TABLE fare (bike_type VARCHAR(10) PRIMARY KEY, cents_per_minute BIGINT NOT NULL, version BIGINT NOT NULL);
                INSERT INTO fare VALUES ('EBIKE', 30, 1);
                """);

            Fare editorA = read(serverA);
            Fare editorB = read(serverB);
            System.out.println("Both editors see " + editorA);

            System.out.println("Editor A saves 35: " + answer(save(serverA, 35, editorA.version())));
            System.out.println("Editor B saves 32: " + answer(save(serverB, 32, editorB.version())));

            Fare fresh = read(serverB);
            System.out.println("Editor B reloads and sees " + fresh);
            System.out.println("Editor B saves 37: " + answer(save(serverB, 37, fresh.version())));
            System.out.println("Final: " + read(serverA));
        }
    }

    static Fare read(Connection db) throws SQLException {
        try (PreparedStatement select = db.prepareStatement(
                "SELECT cents_per_minute, version FROM fare WHERE bike_type = ?")) {
            select.setString(1, "EBIKE");
            try (ResultSet row = select.executeQuery()) {
                row.next();
                return new Fare(row.getLong(1), row.getLong(2));
            }
        }
    }

    static boolean save(Connection db, long cents, long expectedVersion) throws SQLException {
        try (PreparedStatement update = db.prepareStatement(
                "UPDATE fare SET cents_per_minute = ?, version = version + 1 WHERE bike_type = ? AND version = ?")) {
            update.setLong(1, cents);
            update.setString(2, "EBIKE");
            update.setLong(3, expectedVersion);
            return update.executeUpdate() == 1;
        }
    }

    static String answer(boolean saved) {
        return saved ? "200 OK" : "409 Conflict, someone changed it first";
    }
}
```

It prints:

```text
Both editors see Fare[cents=30, version=1]
Editor A saves 35: 200 OK
Editor B saves 32: 409 Conflict, someone changed it first
Editor B reloads and sees Fare[cents=35, version=2]
Editor B saves 37: 200 OK
Final: Fare[cents=37, version=3]
```

Line by line:

1. `read` returns the price **and** the version. The app keeps the version, for example in a hidden form field or an `ETag` header.
2. `save` sends the version it read. `version = version + 1` moves the row on, so any other copy of version 1 is now stale.
3. Editor A's save matches version 1 and changes 1 row: `200 OK`.
4. Editor B's save also says version 1, but the row is at 2. 0 rows change, so `save` returns `false`, and the API answers `409 Conflict`.
5. B reloads, sees A's 35, and decides on 37. This save carries version 2 and works.

A's change was never lost. B was told, and B decided with the real numbers. Each save costs one statement and holds no lock while anyone thinks.

## Try it

This is the conditional update from earlier. Run it, then break it on purpose.

```java run
import java.sql.*;

public class Main {
    static final String URL = "jdbc:h2:mem:tryit;DB_CLOSE_DELAY=-1";

    public static void main(String[] args) throws SQLException {
        try (Connection serverA = DriverManager.getConnection(URL);
             Connection serverB = DriverManager.getConnection(URL)) {
            serverA.createStatement().execute("""
                CREATE TABLE bike (id VARCHAR(10) PRIMARY KEY, status VARCHAR(12), rider_id VARCHAR(20));
                INSERT INTO bike VALUES ('B-1042', 'AVAILABLE', NULL), ('B-1088', 'MAINTENANCE', NULL);
                """);
            System.out.println("A, r-17: " + rent(serverA, "B-1042", "r-17") + " row(s)");
            System.out.println("B, r-23: " + rent(serverB, "B-1042", "r-23") + " row(s)");
            ResultSet row = serverA.createStatement().executeQuery("SELECT rider_id FROM bike WHERE id = 'B-1042'");
            row.next();
            System.out.println("B-1042 belongs to " + row.getString(1));
        }
    }

    static int rent(Connection db, String bikeId, String riderId) throws SQLException {
        try (PreparedStatement update = db.prepareStatement(
                "UPDATE bike SET status = 'RENTED', rider_id = ? WHERE id = ? AND status = 'AVAILABLE'")) {
            update.setString(1, riderId);
            update.setString(2, bikeId);
            return update.executeUpdate();
        }
    }
}
```

1. Delete `AND status = 'AVAILABLE'` from the SQL. Who owns B-1042 now, and how many rows did B change?
2. Put it back, and make server B rent `B-1088` instead. Why 0 rows?
3. Add a third rental of B-1042 by server A for `r-42`. What do you predict before you run it?

## Recap

- Locks in Java only guard one JVM. With two servers, the database must decide who wins.
- Check-then-act (read, decide in Java, write) lets two requests both win. Lost updates and write skew are what happens to the data.
- `READ COMMITTED` is the usual default and allows lost updates. Higher isolation levels block more anomalies but make transactions fail, so you retry them.
- Put the decision in the write: a conditional `UPDATE` with a row-count check, a `version` column (`@Version`), `SELECT ... FOR UPDATE` (`@Lock`), or a `UNIQUE` constraint.
- A deadlock or serialization failure rolls back the whole transaction. Retry all of it, a few times, only for SQLState `40001` or `40P01`.

## Common mistakes

**1. Checking in Java, then writing.**

```java nocompile
if (bikeIsAvailable(connection, "B-1042")) {          // both servers see AVAILABLE...
    rentWithoutCondition(connection, "B-1042", riderId); // ...then both write
}
```

```text
server A: 201 Created for r-17
server B: 201 Created for r-23
B-1042 belongs to r-23
```

Both riders were told "Ride started". The check was already stale when the write ran. Fix: one conditional update, `WHERE id = ? AND status = 'AVAILABLE'`, and use its row count.

**2. Ignoring the row count.**

```java nocompile
update.executeUpdate();          // 0 rows changed: the bike was already taken
return ResponseEntity.status(201).build();
```

Symptom: the rider sees "Ride started", but the bike belongs to someone else. No error appears anywhere. Fix: `if (update.executeUpdate() == 0) { answer 409 Conflict }`. A conditional or versioned update is only half the fix without this check.

**3. Using FOR UPDATE with auto-commit on.**

```java nocompile
// auto-commit is still on: each statement is its own transaction
serverA.createStatement().executeQuery("SELECT status FROM bike WHERE id = 'B-1042' FOR UPDATE");
serverB.createStatement().executeQuery("SELECT status FROM bike WHERE id = 'B-1042' FOR UPDATE");
```

```text
B locked B-1042 too: A's lock ended with A's one-statement transaction
```

A lock lasts until the transaction ends. With auto-commit, that is the end of the `SELECT` itself. Fix: `setAutoCommit(false)`, or `@Transactional`, around the lock and the writes that depend on it.

**4. Retrying only the statement that failed.**

```java nocompile
try {
    withdraw(connection, "r-17", 460);
    deposit(connection, "spoke", 460);       // deadlock here
} catch (SQLException e) {
    deposit(connection, "spoke", 460);       // the withdraw was rolled back too!
    connection.commit();
}
```

```text
Deadlock detected. The current transaction was rolled back. Details: "WALLET"; SQL statement:
```

The error says the whole transaction was rolled back, so the withdraw is gone. Retrying only the deposit makes money from nothing. Fix: roll back and rerun every step from the start, as in the retry loop above.

**5. Fixing it with synchronized.**

```java nocompile
public synchronized boolean rent(String bikeId, String riderId) {
    // check, then write
}
```

Symptom: the tests on your laptop pass, because they run one server. In production with two servers, double rentals continue. Fix: let the database decide, with any of the tools on this page.

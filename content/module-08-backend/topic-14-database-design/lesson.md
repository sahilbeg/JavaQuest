## Hook

Picture a school library with no rules. Anyone can shelve any book anywhere. A book can be listed on a shelf that was torn down last year. To find one title, you walk every shelf.

Spoke's database has drifted that way. Bike B-1099 says it is docked at "atlantis". The ride history page reads every rental ever made, just to show one rider's rides. In this ticket you give the database rules it enforces itself, a catalogue that finds rows fast, and a safe way to change both.

## Concept

### Let the database guard the rules

Your Java code checks that a station exists before it docks a bike. That helps, but it is not enough.

- Spoke now runs two servers, and both write to the same database.
- An engineer may fix data by hand with a SQL script, skipping your Java code.
- Next year another service, in another language, may write rentals too.

Every one of those paths goes through the database. So the database is the one place where a rule always holds. A rule that every row must obey is called an **invariant**.

```callout jargon
An **invariant** is a rule that must always be true, such as "every bike's station exists". A **constraint** is an invariant written into the schema, so the database refuses any change that breaks it.
```

Keep the checks in Java too. They give riders friendly messages. The constraint is the safety net that catches everything else.

### Keys: primary and foreign

You met both in the SQL basics ticket. Here they are again, with one new idea.

- A **primary key** names exactly one row. It is unique and never NULL.
- A **foreign key** is a column that must hold a primary key of another table, or NULL.

```callout jargon
A **natural key** is a real-world value, such as a bike's painted id `B-1042`. A **surrogate key** is a made-up number with no meaning, such as an auto-increment `id`. Surrogate keys never change, so most tables use them.
```

A foreign key also guards deletes. If bike B-1042 still points at Harbor, the database refuses to delete Harbor. That stops a bike pointing at a station that no longer exists.

### Syntax: constraints

```sql
CREATE TABLE station (
    id    VARCHAR(20) PRIMARY KEY,
    name  VARCHAR(40) NOT NULL,
    docks INT NOT NULL,
    CONSTRAINT uq_station_name UNIQUE (name),
    CONSTRAINT ck_station_docks CHECK (docks > 0)
);
```

| Constraint | Refuses | Spoke example |
|---|---|---|
| `NOT NULL` | a missing value | a station with no name |
| `UNIQUE` | the same value twice | two stations both called Harbor |
| `CHECK (...)` | a row where the test is false | a station with 0 docks, a bike of type SCOOTER |
| `REFERENCES t(id)` | a value that `t` does not have | a bike docked at "atlantis" |

You can write a constraint on its column, like `name VARCHAR(40) NOT NULL UNIQUE`. Or write it on its own line with `CONSTRAINT <name>`. A named constraint shows its name in the error, so you know which rule broke.

Run this and watch the database refuse six bad rows. The Java around the SQL is JDBC, which the JDBC ticket teaches in full. A `Connection` is an open line to the database, a `Statement` sends SQL down it, and a `ResultSet` holds the rows that come back.

```java run
import java.sql.*;

public class Main {
    public static void main(String[] args) throws SQLException {
        try (Connection db = DriverManager.getConnection("jdbc:h2:mem:rules");
             Statement sql = db.createStatement()) {
            sql.execute("""
                CREATE TABLE station (
                    id    VARCHAR(20) PRIMARY KEY,
                    name  VARCHAR(40) NOT NULL,
                    docks INT NOT NULL,
                    CONSTRAINT uq_station_name UNIQUE (name),
                    CONSTRAINT ck_station_docks CHECK (docks > 0)
                );
                CREATE TABLE bike (
                    id         VARCHAR(10) PRIMARY KEY,
                    type       VARCHAR(10) NOT NULL,
                    station_id VARCHAR(20),
                    CONSTRAINT ck_bike_type CHECK (type IN ('CLASSIC', 'EBIKE', 'CARGO')),
                    CONSTRAINT fk_bike_station FOREIGN KEY (station_id) REFERENCES station(id)
                );
                INSERT INTO station VALUES ('harbor', 'Harbor', 12);
                """);
            String[] attempts = {
                "INSERT INTO station VALUES ('harbor', 'Harbor Pier', 12)",
                "INSERT INTO station VALUES ('depot', NULL, 20)",
                "INSERT INTO station VALUES ('depot', 'Harbor', 20)",
                "INSERT INTO station VALUES ('depot', 'Depot', 0)",
                "INSERT INTO bike VALUES ('B-1099', 'SCOOTER', 'harbor')",
                "INSERT INTO bike VALUES ('B-1099', 'EBIKE', 'atlantis')",
                "INSERT INTO bike VALUES ('B-1042', 'EBIKE', 'harbor')"
            };
            for (String attempt : attempts) {
                try {
                    sql.execute(attempt);
                    System.out.println("saved:           " + attempt);
                } catch (SQLException e) {
                    System.out.println("refused (" + e.getSQLState() + "): " + attempt);
                }
            }
        }
    }
}
```

The number in brackets is the **SQLState**, a standard five-character error code. Most databases share these codes, so your Java code can react to them.

| SQLState | Means |
|---|---|
| `23502` | NOT NULL broken |
| `23505` | UNIQUE or primary key broken |
| `23503`, `23506` | foreign key broken. H2 uses `23506` for a missing parent row; PostgreSQL uses `23503` for both cases |
| `23513` | CHECK broken (H2's code; PostgreSQL uses `23514`) |

H2's JDBC driver throws each of these as an `SQLIntegrityConstraintViolationException`, a subclass of `SQLException`. Other drivers may throw a plain `SQLException`, so the SQLState is the safer thing to check.

### Normalization: one fact in one place

Here is a table someone made in a hurry. Each ride row also copies the station's name and the rider's email.

```text
ride_id | rider | rider_email      | start     | start_name | bikes
--------+-------+------------------+-----------+------------+----------------
1       | r-17  | ada@example.com  | harbor    | Harbor     | B-1042
2       | r-17  | ada@example.com  | old-mill  | Old Mill   | B-1076
3       | r-23  | lin@example.com  | harbor    | Harbour    | B-1088, B-1031
```

Look closely. Ride 3 spells the station "Harbour". The same fact was written twice, and the copies drifted apart. This table has three classic problems.

- **Update problem:** Ada changes her email. You must change every one of her rows, and missing one leaves two emails.
- **Insert problem:** Depot opens, but you cannot record its name until someone rides from it.
- **Delete problem:** delete Lin's only ride, and you also lose her email.

```callout jargon
**Normalization** means splitting tables so each fact is stored in exactly one place. A copy of a fact stored somewhere else is called **redundant** data.
```

### 1NF, 2NF and 3NF in plain words

The **normal forms** are three checks, each stricter than the one before.

| Form | Rule in plain words | Broken above by |
|---|---|---|
| 1NF | One value per cell. No lists inside a cell. | `bikes` holds "B-1088, B-1031" |
| 2NF | Every column depends on the **whole** key. | with a key of (ride, bike), `rider` depends on the ride alone |
| 3NF | Columns depend on the key, not on another column. | `start_name` depends on `start`, not on the ride |

A famous summary: every column must depend on "the key, the whole key, and nothing but the key".

The fix is the Spoke schema you already know. Riders, stations, bikes and rentals each get their own table. A rental stores only ids: `rider_id`, `bike_id`, `start_station_id`. A station's name lives in one row of `station`, so it can never drift.

### When to break the rules on purpose

Normalized tables need joins to answer questions. Joins cost time, and on a very busy page that can matter.

**Denormalizing** means storing a copy on purpose, to make reads faster. Spoke's live map could keep a `free_bikes` count on each station row, instead of counting bikes on every view.

- Do it only after you measured that the join is too slow.
- Decide which copy is the truth, and how the other copy stays updated.
- Reports and analytics databases often denormalize heavily, because they read far more than they write.

Start normalized. Denormalize one column at a time, with a reason.

### A table scan reads everything

Without help, a database answers `WHERE rider_id = 'r-17'` the slow way. It reads every row in the table and keeps the ones that match.

```callout jargon
A **full table scan** reads every row of a table. It takes O(n) time: twice as many rentals means twice the work.
```

Spoke has a few thousand rentals today and will have millions next year. The ride history page gets slower every week, exactly as Tsunade's ticket says.

### An index is a sorted catalogue

An **index** is a separate, sorted structure that points at rows. It works like the index at the back of a book. You look up "TCP", read "page 211", and turn straight there.

Most databases build indexes as a **B-tree**. Remember the binary search tree from [trees and BSTs](/t/module-04-dsa/topic-09-trees-bst). Each node held one key and had two children. A B-tree node holds hundreds of sorted keys and has hundreds of children.

```callout jargon
A **B-tree** is a search tree whose nodes are wide (many keys each) and whose height stays tiny. One node fills one block of disk, so each step down costs one disk read.
```

Why wide? Each step down a tree costs one read from disk, and a disk read is slow. A BST over a million rows is about 20 levels deep. A B-tree with 500 keys per node needs only 3 levels for 125 million rows.

```animate
title: Finding r-17's rentals with an index
kind: scene
nodes:
  - { id: query, label: "WHERE rider_id = 'r-17'", emoji: "🔎" }
  - { id: root, label: Root node, emoji: "🌳", value: "r-0 … r-299", row: 0, col: 2 }
  - { id: branch, label: Branch node, emoji: "🌿", value: "r-1 … r-199", row: 1, col: 2 }
  - { id: leaf, label: Leaf node, emoji: "🍃", value: "r-17: 10 rows", row: 2, col: 2 }
  - { id: table, label: rental table, emoji: "🗄️", value: "3000 rows", row: 2, col: 4 }
steps:
  - say: The rider app asks for r-17's rentals. The database checks its index on rider_id first.
    focus: [query]
  - say: The root holds sorted keys, like a wide BST node. A few comparisons pick the child whose range holds r-17.
    send: { from: query, to: root, label: "find r-17" }
  - say: One step down. The branch node narrows the range again.
    send: { from: root, to: branch, label: "go left" }
  - say: The leaf holds every r-17 entry, side by side, each with the address of its row.
    send: { from: branch, to: leaf, label: "go to leaf" }
  - say: Only now does it touch the table, and only for those ten rows. Three steps instead of 3000 reads.
    send: { from: leaf, to: table, label: "fetch 10 rows" }
    set: { table: "10 rows read" }
```

### Syntax: CREATE INDEX

```sql
CREATE INDEX idx_rental_rider ON rental(rider_id);
```

- `idx_rental_rider` is the index's name. A prefix like `idx_` and the table name make it easy to find later.
- `ON rental(rider_id)` says which table and which column to sort by.
- `CREATE UNIQUE INDEX` also forbids duplicates. A `UNIQUE` constraint is built from one.
- Every primary key gets an index automatically.

Lookup cost drops from O(n) for a scan to O(log n) for the index. The base of that log is the node width, around 500, so the tree stays shallow.

### What an index costs

An index is not free. Think of it as a second, sorted copy of some columns.

- **Space:** O(n) extra, one entry per row. Big tables can have indexes bigger than the table itself.
- **Slower writes:** every `INSERT`, `DELETE` and every `UPDATE` of an indexed column must also update each index, O(log n) per index.
- **No help for some queries:** the sorted order is useless for a function on the column, such as `LOWER(rider_id) = 'r-17'`. The same goes for a pattern starting with `%`, such as `LIKE '%17'`.

So index the columns your important queries filter, join or sort on. Do not index every column "just in case".

### Which columns deserve an index

An index pays off when it narrows the search a lot. How much a value narrows things is called **selectivity**.

```callout jargon
A column has **high selectivity** when most values are rare, like `rider_id`: one rider owns a tiny slice of the rentals. **Low selectivity** means few distinct values, like `status`, with only AVAILABLE, RENTED and MAINTENANCE.
```

`WHERE status = 'AVAILABLE'` may match half the bikes. Jumping through an index to half the rows is slower than one straight scan, so databases often ignore such an index. Index `rider_id`, not `status`, unless the rare value is the one you search for.

Foreign key columns usually deserve an index, because joins and deletes look them up. MySQL creates one for you. PostgreSQL does not, so add `CREATE INDEX ... ON bike(station_id)` yourself. Without it, deleting a station scans every bike to check none still points at it.

### Adding an index to a busy table

Building an index reads the whole table. On a big table that takes minutes. In PostgreSQL, a plain `CREATE INDEX` blocks every `INSERT`, `UPDATE` and `DELETE` on the table until it finishes. Reads still work, but riders cannot start rentals.

```sql
CREATE INDEX CONCURRENTLY idx_rental_rider_started ON rental(rider_id, started_at);
```

`CONCURRENTLY` builds the index while writes carry on. It takes longer, and it cannot run inside a transaction, so the migration tool must run that script on its own. On a table that is already busy, always add indexes this way.

### Composite indexes: the leftmost prefix

A **composite index** sorts by several columns, in order. `ON rental(rider_id, started_at)` sorts by rider, then by start time within each rider.

```callout analogy
A phone book is sorted by last name, then first name. Finding everyone named "Tan" is fast. Finding everyone whose first name is "Ada" means reading the whole book.
```

The index helps any query that uses its columns from the left.

| Query filters on | Can `(rider_id, started_at)` jump straight there? |
|---|---|
| `rider_id = ?` | yes, the leftmost column |
| `rider_id = ? ORDER BY started_at` | yes, and the rows come out already sorted |
| `rider_id = ? AND started_at > ?` | yes, both columns |
| `started_at > ?` alone | no, it skips the leftmost column |

This is the **leftmost prefix** rule. Put the column you test with `=` first, then the one you sort or range over.

```callout jargon
A **covering index** holds every column a query needs, so the database answers from the index alone and never visits the table.
```

### Syntax: EXPLAIN

```sql
EXPLAIN SELECT id, started_at FROM rental WHERE rider_id = 'r-17' ORDER BY started_at;
```

`EXPLAIN` shows the **query plan**: how the database intends to find the rows, without guessing. `EXPLAIN ANALYZE` also runs the query and counts the rows it really read. In H2, look for the comment under `FROM`.

```text
/* PUBLIC.RENTAL.tableScan */                               every row read
/* PUBLIC.IDX_RENTAL_RIDER_STARTED: RIDER_ID = 'r-17' */    jumps to r-17 in the index
/* PUBLIC.IDX_RENTAL_STARTED_RIDER */ ... /* index sorted */  walks the whole index in order
```

The third line is the trap from the composite index card. The index was `(started_at, rider_id)`, the wrong order. H2 still used it to avoid sorting, but read all of it. No `: RIDER_ID =` after the name means no jump.

PostgreSQL and MySQL print plans differently, with words like `Seq Scan` and `Index Scan`. The idea is the same: check the plan before and after you add an index.

### Schema migrations: versioned scripts

The schema changes as Spoke grows. Every laptop, test run and server must end up with the same tables. Typing `ALTER TABLE` by hand on production does not scale.

```callout jargon
A **migration** is one numbered script that changes the schema, such as `V2__rentals.sql`. A **migration tool**, such as Flyway or Liquibase, runs each script once, in order, and records which ones ran.
```

With Flyway, the scripts live in your repo:

```text
src/main/resources/db/migration/
  V1__stations_and_bikes.sql
  V2__rentals.sql
  V3__index_rental_rider_started.sql
```

At startup, Flyway reads its history table, `flyway_schema_history`. It runs only the versions not listed there, lowest first, and records each one with a **checksum**.

```callout jargon
A **checksum** is a short number computed from a file's contents. Change one character, and the checksum changes. It proves a file is the same one that ran before.
```

### Never edit an applied migration

Once `V2__rentals.sql` has run anywhere, treat it as frozen. Editing it does nothing on databases that already ran it, so their schemas quietly differ from fresh ones.

The checksum catches this. The migration tool sees the stored checksum no longer matches the file, and refuses to start. The fix is always the same: put the change in a new migration, `V4__...`.

### Running a migration in one transaction

A migration tool does two things for each script: run its SQL, then record it in the history table. Both must happen, or neither. Otherwise a script could run without being recorded, and run again next time.

```callout jargon
A **transaction** is a group of changes that are kept together or undone together. To **commit** keeps them. To **roll back** undoes them. **Auto-commit** is JDBC's default mode, where every statement is its own tiny transaction, kept at once.
```

So turn auto-commit off, do the steps, then commit. If any step throws, roll back. This program opens a station and writes a log row about it, as one unit. The second station's log row breaks a rule, so its station row is undone too.

```java run
import java.sql.*;

public class Main {
    public static void main(String[] args) throws SQLException {
        try (Connection db = DriverManager.getConnection("jdbc:h2:mem:tx");
             Statement sql = db.createStatement()) {
            sql.execute("""
                CREATE TABLE station (id VARCHAR(20) PRIMARY KEY, name VARCHAR(40) NOT NULL);
                CREATE TABLE station_log (station_id VARCHAR(20) NOT NULL, note VARCHAR(100) NOT NULL);
                """);
            openStation(db, "harbor", "Harbor", "Harbor's first day");
            try {
                openStation(db, "depot", "Depot", null);   // the log row breaks NOT NULL
            } catch (SQLException e) {
                System.out.println("depot failed, SQLState " + e.getSQLState());
            }
            ResultSet rows = sql.executeQuery("SELECT s.name, l.note FROM station s JOIN station_log l ON l.station_id = s.id");
            while (rows.next()) {
                System.out.println(rows.getString(1) + ": " + rows.getString(2));
            }
            ResultSet count = sql.executeQuery("SELECT COUNT(*) FROM station");
            count.next();
            System.out.println("stations: " + count.getInt(1));
        }
    }

    static void openStation(Connection db, String id, String name, String note) throws SQLException {
        boolean oldAutoCommit = db.getAutoCommit();
        db.setAutoCommit(false);                                  // start one transaction
        try (PreparedStatement station = db.prepareStatement("INSERT INTO station (id, name) VALUES (?, ?)");
             PreparedStatement log = db.prepareStatement("INSERT INTO station_log (station_id, note) VALUES (?, ?)")) {
            station.setString(1, id);                             // fill the first ?
            station.setString(2, name);                           // fill the second ?
            station.executeUpdate();
            log.setString(1, id);
            log.setString(2, note);                               // an apostrophe is fine here
            log.executeUpdate();
            db.commit();                                          // keep both rows
        } catch (SQLException e) {
            db.rollback();                                        // undo both rows
            throw e;
        } finally {
            db.setAutoCommit(oldAutoCommit);                      // put the connection back as it was
        }
    }
}
```

It prints `depot failed, SQLState 23502`, then `Harbor: Harbor's first day`, then `stations: 1`. Depot's station row was rolled back with its log row.

- `prepareStatement(sql)` takes SQL with `?` marks where values go. `setString(1, ...)` and `setInt`, `setLong` fill them in, counting from 1.
- `executeUpdate()` runs it and returns how many rows changed.
- `finally` puts auto-commit back, whether the work succeeded or failed.

```callout warning
Never glue a value into SQL with `+`. The description "index for a rider's ride history" has an apostrophe, which ends the SQL text early. H2 answers `Syntax error in SQL statement`. A `?` parameter keeps any value as plain data.
```

The JDBC and transactions tickets later go deeper. This is all a migration tool needs.

PostgreSQL can roll back a failed `CREATE TABLE`. H2, MySQL and Oracle commit each schema change at once, so a migration that fails halfway can leave half its tables behind. Keep each migration small, ideally one change.

### Expand, then contract

Spoke runs several servers. During a deploy, old and new code run side by side against the same database. So a schema change must work with both versions.

Say rentals need `end_station_id NOT NULL`. Adding that in one step fails, because existing rows have no value for it. Instead, split the change:

1. **Expand:** add the column as nullable. Old code ignores it, new code writes it.
2. **Migrate:** backfill old rows in batches, so no long lock blocks riders.
3. **Contract:** once every row and every server is updated, add `NOT NULL`, or drop the old column.

Each step is its own migration and its own deploy. Renaming a column works the same way: add the new one, write both, backfill, switch reads, then drop the old one.

```callout jargon
**Zero downtime** means users never see the app go down during a deploy. **Backfill** means filling a new column for rows that existed before it.
```

### N+1 queries

A page lists five stations with their bike counts. The obvious code runs one query for the stations, then one more per station. That is N + 1 queries for N stations.

```java run
import java.sql.*;

public class Main {
    static int queries = 0;

    static ResultSet run(Statement sql, String query) throws SQLException {
        queries++;
        return sql.executeQuery(query);
    }

    public static void main(String[] args) throws SQLException {
        try (Connection db = DriverManager.getConnection("jdbc:h2:mem:nplus1");
             Statement outer = db.createStatement();
             Statement inner = db.createStatement()) {
            outer.execute("""
                CREATE TABLE station (id VARCHAR(20) PRIMARY KEY, name VARCHAR(40));
                CREATE TABLE bike (id VARCHAR(10) PRIMARY KEY, station_id VARCHAR(20));
                INSERT INTO station VALUES ('harbor', 'Harbor'), ('old-mill', 'Old Mill'),
                    ('riverside', 'Riverside'), ('uptown', 'Uptown'), ('depot', 'Depot');
                INSERT INTO bike VALUES ('B-1042', 'harbor'), ('B-1088', 'harbor'), ('B-1076', 'old-mill'),
                    ('B-1023', 'riverside'), ('B-1031', 'uptown');
                """);

            System.out.println("N+1 way:");
            ResultSet stations = run(outer, "SELECT id, name FROM station ORDER BY name");
            while (stations.next()) {
                ResultSet count = run(inner, "SELECT COUNT(*) FROM bike WHERE station_id = '" + stations.getString("id") + "'");
                count.next();
                System.out.println("  " + stations.getString("name") + ": " + count.getInt(1));
            }
            System.out.println("  queries: " + queries);

            queries = 0;
            System.out.println("One query with a join:");
            ResultSet rows = run(outer, """
                SELECT s.name, COUNT(b.id) AS bikes FROM station s LEFT JOIN bike b ON b.station_id = s.id
                GROUP BY s.name ORDER BY s.name""");
            while (rows.next()) {
                System.out.println("  " + rows.getString("name") + ": " + rows.getInt("bikes"));
            }
            System.out.println("  queries: " + queries);
        }
    }
}
```

Both print the same counts. The first sends 6 queries, the second 1. Each query is a round trip to the database server, so with 500 stations the gap is 501 to 1.

```callout jargon
An **ORM** (object-relational mapper) is a library that turns rows into Java objects and back, so you rarely write SQL. JPA, with Hibernate, is Java's standard one.
```

N+1 hides easily inside an ORM, where touching `station.getBikes()` in a loop quietly fires a query each time. A later ticket, [JPA and Hibernate](/t/module-08-backend/topic-09-jpa-hibernate), shows how to fetch them together.

### In an interview

Database design shows up in system design rounds and in "this endpoint is slow" debugging questions. Spot it when the question says slow query, millions of rows, schema, "design the tables for...", or a deploy that changes a column.

- **Design the tables** for a bike-share, a library or a ride app. Name the keys, the foreign keys and the constraints, and normalize to 3NF.
- **Why is this query slow?** Ask for the plan, spot the full table scan, add the right index, and state the write cost.
- **Which index for this query?** Apply the leftmost prefix rule: equality columns first, then the range or sort column.
- **How do you rename a column with no downtime?** Expand, backfill, switch, contract.
- **Fix the N+1** in this loop with a join or a batched `IN (...)` query.

## Worked example

Tsunade's ticket: the ride history page runs this query, and it is slow.

```sql
SELECT id, started_at FROM rental WHERE rider_id = ? ORDER BY started_at
```

**Step 1: measure.** Run the plan before you change anything. Guessing which index helps is how teams end up with ten useless ones.

**Step 2: choose the index.** The query tests `rider_id` with `=`, then sorts by `started_at`. By the leftmost prefix rule, the index is `(rider_id, started_at)`.

**Step 3: ship it as a migration.** It goes in a new file, `V3__index_rental_rider_started.sql`, never into V2.

```sql
CREATE INDEX idx_rental_rider_started ON rental(rider_id, started_at);
```

**Step 4: measure again.** This program builds 3000 rentals for 300 riders, then compares the plan before and after.

```java run
import java.sql.*;

public class Main {
    public static void main(String[] args) throws SQLException {
        try (Connection db = DriverManager.getConnection("jdbc:h2:mem:plans");
             Statement sql = db.createStatement()) {
            sql.execute("CREATE TABLE rental (id INT PRIMARY KEY, rider_id VARCHAR(20), started_at TIMESTAMP)");
            for (int i = 1; i <= 3000; i++) {
                sql.execute("INSERT INTO rental VALUES (" + i + ", 'r-" + (i % 300) + "', TIMESTAMP '2026-11-01 08:00:00' + " + i + " * INTERVAL '1' MINUTE)");
            }
            String query = "SELECT id, started_at FROM rental WHERE rider_id = 'r-17' ORDER BY started_at";

            System.out.println("Before the index:");
            explain(sql, query);

            sql.execute("CREATE INDEX idx_rental_rider_started ON rental(rider_id, started_at)");
            System.out.println("After the index:");
            explain(sql, query);
        }
    }

    static void explain(Statement sql, String query) throws SQLException {
        try (ResultSet plan = sql.executeQuery("EXPLAIN ANALYZE " + query)) {
            plan.next();
            for (String line : plan.getString(1).split("\n")) {
                if (line.contains("/*")) {
                    System.out.println("  " + line.strip());
                }
            }
        }
    }
}
```

It prints:

```text
Before the index:
  /* PUBLIC.RENTAL.tableScan */
  /* scanCount: 3001 */
After the index:
  /* PUBLIC.IDX_RENTAL_RIDER_STARTED: RIDER_ID = 'r-17' */
  /* scanCount: 11 */
```

Line by line:

1. `EXPLAIN ANALYZE` runs the query and returns its plan as one text value.
2. The program keeps only the comment lines, which say how the rows were found.
3. Before: `tableScan`, and `scanCount: 3001` rows looked at, to return 10.
4. After: the index name, then `RIDER_ID = 'r-17'`, which means it jumped straight to r-17. It looked at 11 entries: the 10 matches and one more to see where r-17 ends.
5. There is no sort step in the plan, because the index already keeps each rider's rows in `started_at` order.

At 3 million rentals, the scan would read 3 million rows. The index lookup would still read about a dozen entries plus 3 tree levels.

## Try it

This is the constraint program from earlier, trimmed. Run it, then try the changes below.

```java run
import java.sql.*;

public class Main {
    public static void main(String[] args) throws SQLException {
        try (Connection db = DriverManager.getConnection("jdbc:h2:mem:tryit");
             Statement sql = db.createStatement()) {
            sql.execute("""
                CREATE TABLE station (
                    id    VARCHAR(20) PRIMARY KEY,
                    name  VARCHAR(40) NOT NULL UNIQUE,
                    docks INT NOT NULL CHECK (docks > 0)
                );
                INSERT INTO station VALUES ('harbor', 'Harbor', 12);
                """);
            String attempt = "INSERT INTO station VALUES ('depot', 'Depot', 20)";
            try {
                sql.execute(attempt);
                System.out.println("saved");
            } catch (SQLException e) {
                System.out.println("refused, SQLState " + e.getSQLState());
            }
        }
    }
}
```

1. Change the docks to `-3`. Which SQLState do you get?
2. Change the name to `'Harbor'`. Which rule fires now?
3. Add a rule that a station has at most 40 docks: `CHECK (docks BETWEEN 1 AND 40)`. Try 41.
4. Add a `bike` table with `station_id VARCHAR(20) REFERENCES station(id)`, and insert a bike at `'atlantis'`.

## Recap

- Constraints (`NOT NULL`, `UNIQUE`, `CHECK`, foreign keys) make the database refuse bad rows from every path, not only from your Java code.
- Normalize so each fact lives in one place (1NF, 2NF, 3NF). Denormalize a column only when a measured read is too slow.
- An index is a B-tree: a wide, flat search tree. It turns an O(n) table scan into an O(log n) lookup, and costs space and slower writes.
- A composite index helps queries that use its columns from the left. Check with `EXPLAIN` before and after.
- Change schemas with numbered migrations that run once each. Never edit an applied one, and use expand-then-contract for changes running servers depend on.

## Common mistakes

**1. Wrapping the indexed column in a function.**

```sql
-- on this ticket's Spoke rental table, after CREATE INDEX idx_rental_rider ON rental(rider_id)
EXPLAIN SELECT * FROM rental WHERE LOWER(rider_id) = 'r-17';
```

```text
/* PUBLIC.RENTAL.tableScan */
```

The index is sorted by `rider_id`, not by `LOWER(rider_id)`, so the database cannot jump into it. Fix: store ids in one case and compare the column directly, `WHERE rider_id = 'r-17'`. PostgreSQL can also index the expression itself: `CREATE INDEX ... ON rental(LOWER(rider_id))`.

**2. Putting a composite index's columns in the wrong order.**

```sql
CREATE INDEX idx_rental_started_rider ON rental(started_at, rider_id);
EXPLAIN SELECT id, started_at FROM rental WHERE rider_id = 'r-17' ORDER BY started_at;
```

```text
/* PUBLIC.IDX_RENTAL_STARTED_RIDER */
...
/* index sorted */
```

The index is used, but only to avoid sorting. There is no `: RIDER_ID =`, so it walks every entry. Fix: equality column first, `ON rental(rider_id, started_at)`.

**3. Adding a NOT NULL column to a table that already has rows.**

```sql
ALTER TABLE rental ADD COLUMN end_station_id VARCHAR(20) NOT NULL;
```

```text
NULL not allowed for column "END_STATION_ID"; SQL statement:
INSERT INTO "PUBLIC"."RENTAL_COPY_3_0"("ID", "RIDER_ID") OVERRIDING SYSTEM VALUE SELECT "ID", "RIDER_ID" FROM "PUBLIC"."RENTAL" [23502-240]
```

The old rows have no value for the new column. Fix: expand then contract. Add it nullable, backfill the old rows, then add `NOT NULL` in a later migration.

**4. Editing a migration that already ran.**

```sql
-- V2__rentals.sql, edited after it ran on every server
CREATE TABLE rental (
    id         VARCHAR(20) PRIMARY KEY,
    rider_id   VARCHAR(20) NOT NULL,
    promo_code VARCHAR(20)          -- added later, in the old file
);
```

Symptom: fresh test databases get `promo_code`, production never does, and Flyway refuses to start because V2's checksum changed. Fix: put the edit back, and add `V4__add_promo_code.sql` with `ALTER TABLE rental ADD COLUMN promo_code VARCHAR(20)`.

**5. Checking a rule only in Java.**

```java nocompile
if (stationRepository.exists(stationId)) {
    bikeRepository.dock(bikeId, stationId);   // a hand-run SQL script skips this check entirely
}
```

Symptom: B-1099 docked at "atlantis", written by a script that never ran your Java. Fix: keep the Java check for friendly messages, and add `REFERENCES station(id)` so the database refuses the row whatever path it comes from.

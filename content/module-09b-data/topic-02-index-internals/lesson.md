## Hook

A city library has two million books. Finding one by walking every shelf takes a week. The catalogue room finds it in a minute. You open the right drawer, then the right card, then walk to one shelf.

Spoke's ride table has 40 million rows. Looking up a rider's rides is instant, because an index points straight at them. But Kakashi's report, "every ride on one bike between two times", reads every row. In [database design](/t/module-08-backend/topic-14-database-design) you met indexes and EXPLAIN from the outside. This ticket opens the index up and shows how it really works.

## Concept

### Pages: the unit a database reads

A database never reads one row from disk. It reads a whole **page**, a fixed-size block that holds many rows or index entries.

```callout jargon
A **page** is the fixed-size block a database stores and reads as one unit. PostgreSQL pages are 8 KB. MySQL's InnoDB engine uses 16 KB pages. The **buffer pool** (PostgreSQL calls it **shared buffers**) is the part of memory that keeps recently used pages.
```

An 8 KB page holds about 80 rides of 100 bytes each. So the 40 million rides fill about 500,000 pages.

To find one row, the database must find the right page. Reading the right page is cheap. Reading the wrong pages is what makes a query slow. Every idea in this ticket is about reading fewer pages.

### Why disk reads decide the speed

In [how computers work](/t/module-00-foundations/topic-01-how-computers-work) you saw that each kind of storage is far slower than the one above it. For databases, the gap is huge.

| Where the page is | Rough time to read it |
|---|---|
| Buffer pool (RAM) | about 0.1 µs |
| SSD | about 100 µs, a thousand times slower |
| Spinning hard disk | about 10 ms, a hundred thousand times slower |

A CPU compares millions of keys in the time one SSD read takes. So the cost of a query is roughly **the number of pages it reads that are not in memory**. Counting comparisons, as in [Big-O](/t/module-04-dsa/topic-01-big-o), is not enough. Count pages.

Kakashi's report reads all 500,000 pages. Even at 100 µs each, that is 50 seconds of reading, and it pushes useful pages out of the buffer pool too.

### From a binary tree to a B+ tree

A [binary search tree](/t/module-04-dsa/topic-09-trees-bst) has two children per node. Finding one key among 100 million rows takes about 27 steps down. If each step is a page read, that is 27 slow reads.

The fix is to make each node **wide**: fill a whole page with keys. A node with 500 keys has 501 children.

```callout jargon
**Fan-out** is the number of children a node has. A **B+ tree** is a search tree with a big fan-out, where every key and its row pointer live in the bottom nodes, called **leaves**. The upper nodes, called **branches** (or internal nodes), hold only **separator keys** that guide a search down. The leaves are linked left to right.
```

Most database indexes are B+ trees. People often just say "B-tree", and in PostgreSQL and InnoDB they mean this kind. In the original B-tree, branch nodes held rows too. Keeping all data in the leaves lets branch nodes hold more keys, which raises the fan-out.

### How tall is the tree?

Each level multiplies how many keys the tree can reach by the fan-out. So the height is about **log base fan-out of N**.

```java run
public class Main {
    public static void main(String[] args) {
        long rows = 100_000_000L;
        for (int fanOut : new int[] {2, 100, 500}) {
            int levels = 0;
            long reach = 1;
            while (reach < rows) {
                reach *= fanOut;
                levels++;
            }
            System.out.println("fan-out " + fanOut + ": " + levels + " levels to reach 100 million rows");
        }
    }
}
```

It prints:

```text
fan-out 2: 27 levels to reach 100 million rows
fan-out 100: 4 levels to reach 100 million rows
fan-out 500: 3 levels to reach 100 million rows
```

Where does 500 come from? An 8 KB page divided by about 16 bytes per entry (an 8-byte key plus a pointer) is about 500. With 500 keys per node, 500³ is 125 million, so three or four levels cover Spoke's rides.

Better still, the top levels are tiny. The root is one page, and the next level is a few hundred pages. They stay in the buffer pool, so a lookup usually costs one or two real disk reads.

### Searching a B+ tree

A search starts at the root. In each branch, it compares the key with the separators and follows one child. In the leaf, it finds the key and the address of its row.

```animate
title: Finding rider-7's rides in a B+ tree
kind: scene
nodes:
  - { id: root, label: Root page, emoji: "🌳", value: "rider-3 | rider-6", row: 0, col: 2 }
  - { id: branch, label: Branch page, emoji: "🌿", value: "rider-7 | rider-9", row: 1, col: 3 }
  - { id: leaf, label: Leaf page, emoji: "🍃", value: "rider-7, rider-8", row: 2, col: 3 }
  - { id: next, label: Next leaf, emoji: "🍃", value: "rider-9", row: 2, col: 5 }
  - { id: heap, label: Table pages, emoji: "🗄️", value: "40 million rides", row: 2, col: 0 }
steps:
  - say: The search reads the root page. rider-7 sorts after rider-6, so it follows the rightmost child.
    focus: [root]
    send: { from: root, to: branch, label: ">= rider-6" }
  - say: One more page. rider-7 is at least rider-7 but below rider-9, so it follows the middle child.
    send: { from: branch, to: leaf, label: "rider-7 .. rider-9" }
  - say: The leaf holds rider-7's entries, each with the address of a row. Three pages read so far.
    focus: [leaf]
  - say: Only now does it fetch the rows themselves, one table page per ride, not all 500,000 pages.
    send: { from: leaf, to: heap, label: "fetch rider-7's rows" }
    set: { heap: "a few pages read" }
  - say: A range such as rider-7 to rider-9 keeps going sideways along the leaf link. No climbing back up.
    send: { from: leaf, to: next, label: "next leaf" }
```

A search reads exactly one page per level: the tree's **height**. Every leaf sits at the same depth, so every lookup costs the same. Inside a page, a binary search over the sorted keys finds the right slot in a few CPU steps. Time: O(log n) page reads, with the log to the base of the fan-out.

### Range scans walk the linked leaves

Because the leaves are sorted and linked, a range query is cheap. "Every key from A to B" means: search down to A once, then walk right along the leaves until a key passes B.

The cost is the height, plus one page for each further leaf. For k matching keys, that is O(log n + k / entries per leaf) page reads. The leaves also hand the rows back already in key order, so `ORDER BY` on that key needs no extra sort.

A range on a hash structure would need to check every bucket. That linked-leaf walk is why B+ trees win for ranges, sorting and "the next 20 after this one".

### Inserts and page splits

A new key goes into the leaf where a search for it ends. If the leaf has room, that is the whole job. If the leaf is full, it **splits**.

```animate
title: A full leaf splits
kind: scene
nodes:
  - { id: parent, label: Parent branch, emoji: "🌿", value: "B-1040 | B-1060", row: 0, col: 2 }
  - { id: leaf, label: Full leaf, emoji: "🍃", value: "B-1041 B-1042 B-1044 B-1047", row: 1, col: 1 }
  - { id: right, label: New right leaf, emoji: "🍃", value: "", row: 1, col: 3 }
  - { id: key, label: New key, emoji: "🔑", value: "B-1043", row: 2, col: 0 }
steps:
  - say: Bike B-1043 joins the fleet. Its key belongs in this leaf, but the leaf holds its maximum of four keys.
    send: { from: key, to: leaf, label: "insert B-1043" }
  - say: Five keys do not fit. The leaf splits in two. The left half keeps the first two keys.
    set: { leaf: "B-1041 B-1042", right: "B-1043 B-1044 B-1047" }
    show: [right]
  - say: The new right leaf is linked in after the left one, so range scans still walk in order.
    send: { from: leaf, to: right, label: "next" }
  - say: The parent gets a copy of the right leaf's first key as a new separator.
    send: { from: right, to: parent, label: "B-1043 goes up" }
    set: { parent: "B-1040 | B-1043 | B-1060" }
  - say: If the parent overflows, it splits too. A root that splits makes a new root, and only then does the tree grow taller.
    focus: [parent]
```

```callout jargon
A **page split** moves half of a full page's entries into a new page and adds a separator to the parent. **Fill factor** is how full pages are kept on purpose, leaving room for later inserts.
```

Splits keep every leaf at the same depth, so the tree stays balanced on its own. The cost: a split writes three pages instead of one, and the two halves start half empty. Keys that always arrive at the end, like a growing ride id or a timestamp, fill the rightmost leaf and split it again and again. PostgreSQL and InnoDB spot this and split the rightmost page unevenly, leaving the old page nearly full and the new one almost empty. So pages stay full, which is why an ever-growing key is cheap to insert. Random keys, like random UUIDs, split pages all over the tree and leave them half full.

### Clustered and secondary indexes

Where do the rows themselves live? Two big designs exist.

```callout jargon
A **clustered index** stores the full rows inside its own leaves, sorted by its key. A table can have only one. A **secondary index** stores its key plus a pointer to the row. A **heap** is a table whose rows sit in no particular order.
```

- **PostgreSQL:** every table is a heap. Every index, the primary key included, is secondary. Its leaves hold the key and the row's address, called a TID (the page number and the slot in it). A lookup reads the index pages, then one heap page per row.
- **MySQL InnoDB:** the table *is* a B+ tree, clustered on the primary key. A secondary index's leaves hold the key plus the **primary key value**. So a secondary lookup searches two trees: the secondary index, then the primary key tree.

Two lessons follow for InnoDB. A short primary key keeps every secondary index small, because each entry carries a copy of it. And a growing primary key, like an auto-increment id, inserts at the end instead of splitting pages all over.

### Covering indexes and index-only scans

If an index holds every column a query needs, the database can skip the table entirely. That is called an **index-only scan**.

```callout jargon
A **covering index** holds every column one query reads: the columns it filters on and the columns it returns. An **index-only scan** answers a query from the index pages alone.
```

Kakashi's report returns only ride ids and minutes. PostgreSQL 11 and later let you carry extra columns in the leaves, without making them part of the sort key.

```sql
CREATE INDEX idx_ride_bike_time ON ride (bike_id, started_at) INCLUDE (id, minutes);
```

Now `SELECT id, minutes ... WHERE bike_id = ? AND started_at BETWEEN ? AND ?` can skip the heap, and the plan shows `Index Only Scan`. Note that `id` had to go in `INCLUDE` too. A PostgreSQL index entry holds the row's address (its TID), but that address is not a column the query can read. Without `id` in the index, every row's id would need a heap read. PostgreSQL also checks a small map of which pages are fully cleaned up. A table with many recent changes can still need some heap reads.

InnoDB is different: every secondary index entry already carries the primary key value, as a real column. So there, an index on `(bike_id, started_at)` covers a query that returns only those columns and `id`.

The price: a wider index means bigger leaves, fewer entries per page, and more to update on every write.

### Composite indexes and the leftmost prefix

A **composite index** sorts by several columns, like a phone book sorted by surname, then first name. You met the leftmost-prefix rule in database design. Here is why it holds.

In an index on `(bike_id, started_at)`, the leaves list every ride of B-1041 together, sorted by time. Then every ride of B-1042, sorted by time, and so on.

```text
B-1041    2026-10-01 00:41
B-1041    2026-10-01 01:41
...
B-1042    2026-10-05 00:42   <- jump here: bike = 'B-1042', time >= 5 October
B-1042    2026-10-05 01:42
...
B-1042    2026-10-05 23:42   <- stop here: the next entry is past the end of 5 October
B-1042    2026-10-06 00:42
...
```

`bike = 'B-1042' AND time on one day` is one contiguous slice of leaves: one search down, then a short walk. `time on one day` alone is not one slice. Its entries are scattered across every bike's section, so the index cannot jump to them.

```callout jargon
The **leftmost-prefix rule** says a composite index can narrow a search using its columns from the left, as long as each one is tested with =. The first column tested with a range is the last one that helps.
```

### Column order: equality first, range last

Now flip the index to `(started_at, bike_id)`. Entries are sorted by time first. For one day, every bike's rides are mixed together.

The index still helps: it jumps to the start of the day and walks to the end. But it reads every bike's rides that day and throws most away. After a range on the first column, the second column is no longer in order, so it cannot narrow the walk.

The rule for picking column order:

1. **Columns tested with `=` go first.** Their order between each other matters little for this query. Pick the one other queries also need.
2. **The one column tested with a range goes last.** `<`, `>`, `BETWEEN`, `LIKE 'abc%'` and `ORDER BY` all count as ranges.
3. **Columns after the range only help a covering index**, not the search.

So for Kakashi's report the answer is `(bike_id, started_at)`. The worked example measures both orders.

PostgreSQL 18 and MySQL 8 can sometimes **skip scan**: when the first column has only a few values, they search once per value. It is a nice extra, not a reason to get the order wrong.

### Selectivity: when a full scan wins

An index is not always faster. Each row found through a secondary index may cost one random page read. A full scan reads every page, but in order, which disks and read-ahead love.

```callout jargon
**Selectivity** is the share of rows a condition keeps. `rider_id = 'rider-7'` keeps 1 row in 100,000: highly selective. `type = 'CLASSIC'` keeps half the bikes: not selective. **Cardinality** is how many different values a column has.
```

The **query planner** guesses how many rows each condition keeps, from statistics it gathers about each column. If a query matches a large share of the table, often more than a few percent, the planner picks a full scan on purpose. That is the right call, not a bug.

So index columns that queries use to narrow things a lot. When the planner guesses wrong, the usual cause is stale statistics. PostgreSQL refreshes them with `ANALYZE`, and its autovacuum process runs that for you.

### Why an index is not used: functions and casts

An index is sorted by the column's stored value. Any query that changes the value before comparing it cannot use that order. Spoke's rider table has an index on `email`, and these plans are from real H2 runs.

```sql
EXPLAIN SELECT id FROM rider WHERE email = 'Rider7@spoke.example';        -- uses the index
EXPLAIN SELECT id FROM rider WHERE LOWER(email) = 'rider7@spoke.example'; -- does not
```

```text
/* PUBLIC.IDX_RIDER_EMAIL: EMAIL = 'Rider7@spoke.example' */
/* PUBLIC.RIDER.tableScan */
```

The index is sorted by `email`, not by `LOWER(email)`. So the database must compute `LOWER` for every row. The fix in PostgreSQL is an **expression index**, sorted by the function's result: `CREATE INDEX ON rider (lower(email))`. Or store emails in lower case to begin with.

A hidden function does the same damage. `phone` is a `VARCHAR`, but this query compares it with a number.

```sql
EXPLAIN SELECT id FROM rider WHERE phone = 07007;
```

```text
/* PUBLIC.RIDER.tableScan */
WHERE "PHONE" = 7007
```

H2 turned every phone into a number to compare, an **implicit cast**. That is a function on the column, so the index on `phone` was skipped. Worse, `07007` became `7007`. MySQL casts quietly in the same way. PostgreSQL refuses instead, with `operator does not exist: character varying = integer`, which is the safer failure. Fix: compare with the column's own type, `phone = '07007'`, and bind parameters with the right Java type.

### More ways to lose an index: LIKE and OR

`LIKE` with a fixed start is a range. `LIKE` with a leading `%` is not. In PostgreSQL, the prefix trick works only with the `C` collation or an index built with `text_pattern_ops` (or `varchar_pattern_ops`). These plans are real H2 output, each comment joined onto one line.

```text
email LIKE 'Rider7%'          ->  /* PUBLIC.IDX_RIDER_EMAIL: EMAIL >= 'Rider7' AND EMAIL < 'Rider8' */
email LIKE '%@spoke.example'  ->  /* PUBLIC.RIDER.tableScan */
```

A phone book helps you find names starting with "Tan". It cannot find names ending in "son". For "contains" searches, use a full-text index or a trigram index (PostgreSQL's `pg_trgm`), not a B+ tree.

`OR` across two different columns is the last classic.

```text
email = 'Rider7@spoke.example' OR name = 'Name9'     ->  /* PUBLIC.RIDER.tableScan */
```

Each index can only find one side. H2 gave up and scanned. Rewriting it as two queries joined with `UNION` let H2 use `IDX_RIDER_EMAIL` for one half and `IDX_RIDER_NAME` for the other. PostgreSQL can combine two indexes on its own, with a `BitmapOr`, and MySQL has an "index merge". Always check the plan rather than guessing.

### Every index costs writes

An index is a second B+ tree that must stay in step with the table. Each insert into `ride` also inserts one entry into **every** index on it.

| Indexes on ride | Trees touched by one insert |
|---|---|
| primary key only | 1 |
| + rider, + bike and time | 3 |
| + six "just in case" indexes | 9 |

Each one costs page reads, page writes, splits, and space in the buffer pool and on disk. Every index also slows `UPDATE` when an indexed column changes, and every `DELETE`.

Spoke writes 5,000 ride starts in the morning rush, plus a GPS ping every 10 seconds per ride. So keep indexes that real queries need, and drop unused ones. PostgreSQL counts how often each index is used, in the `pg_stat_user_indexes` view.

### Hash indexes

A **hash index** puts each key in a bucket chosen by a hash function, like the [hash tables](/t/module-04-dsa/topic-08-hash-tables) behind Java's `HashMap`. Finding one key costs about one page read, whatever the size.

```callout jargon
A **hash index** finds rows by hashing the key into a bucket. It answers only `=`. It cannot do ranges, sorting or prefixes, because neighbouring keys land in unrelated buckets.
```

PostgreSQL has `CREATE INDEX ... USING hash`, and it is safe to use since version 10. In practice a B+ tree is almost as fast for `=`, and it also does ranges. So B+ trees stay the default. Hash structures shine inside the engine instead: hash joins, InnoDB's adaptive hash index, and key-value stores like Redis.

### LSM trees: memtable and SSTables

A B+ tree updates pages in place, all over the disk. That is many random writes. Some systems must take in huge write streams, like Spoke's GPS pings. They use a different shape: the **LSM tree**.

```callout jargon
An **LSM tree** (log-structured merge tree) collects writes in memory, then writes them to disk as big sorted files that are never changed. The in-memory part is the **memtable**. Each file on disk is an **SSTable** (sorted string table). A **write-ahead log** keeps a copy of each write on disk, so a crash cannot lose the memtable.
```

```animate
title: A GPS ping goes into an LSM tree
kind: scene
nodes:
  - { id: ping, label: GPS ping, emoji: "📍", row: 0, col: 0 }
  - { id: wal, label: Write-ahead log, emoji: "📜", row: 1, col: 0 }
  - { id: mem, label: Memtable (RAM), emoji: "🧠", value: "sorted, 3 MB", row: 0, col: 2 }
  - { id: l0, label: New SSTables, emoji: "📄", value: "2 files", row: 1, col: 3 }
  - { id: l1, label: Older SSTables, emoji: "📚", value: "8 files", row: 2, col: 3 }
steps:
  - say: A ping for bike B-1042 arrives. It is appended to the log, a fast write to the end of one file.
    send: { from: ping, to: wal, label: "append" }
  - say: Then it goes into the memtable, a sorted map in memory. The write is done. No page was read or rewritten.
    send: { from: ping, to: mem, label: "put" }
    set: { mem: "sorted, 4 MB, full" }
  - say: When the memtable is full, it is written out in one go as a new SSTable, a sorted file that never changes.
    send: { from: mem, to: l0, label: "flush" }
    set: { mem: "empty", l0: "3 files" }
  - say: In the background, compaction merges small files into bigger ones, keeping only the newest value of each key.
    send: { from: l0, to: l1, label: "merge" }
    set: { l0: "0 files", l1: "9 files" }
```

Writes are fast because every disk write is a long sequential one. An update is just a newer entry for the same key. A delete is a **tombstone**, a marker that says "this key is gone".

### Compaction, bloom filters and amplification

Reads are the hard part. A key may sit in the memtable or in any SSTable. A read checks the memtable, then the newest files first, and stops at the first hit.

Two tricks keep reads fast:

- **Compaction** merges SSTables in the background, like merge sort's merge step. It drops overwritten values and old tombstones. Fewer files means fewer places to look.
- A **bloom filter** per SSTable answers "is this key maybe here?" from a small bit array in memory. "Definitely not" lets the read skip that file without touching the disk.

```callout jargon
A **bloom filter** is a bit array plus a few hash functions. It can say "definitely not in the set" or "maybe in the set". It never misses a key that is there, but sometimes says "maybe" for a key that is not, a **false positive**.
```

Every storage engine trades three costs, called **amplification**:

- **Write amplification:** bytes written to disk per byte the app wrote. LSM compaction rewrites the same data several times.
- **Read amplification:** pages or files read per lookup. An LSM read may check several files.
- **Space amplification:** disk used per byte of live data. Old versions wait on disk until compaction.

### B-tree or LSM tree?

| | B+ tree | LSM tree |
|---|---|---|
| Writes | random page writes in place | sequential, very fast |
| Point reads | one path, height pages | memtable plus maybe several files |
| Range reads | excellent, linked leaves | good, but merges several files |
| Used by | PostgreSQL, MySQL InnoDB, Oracle, SQL Server | Cassandra, RocksDB, LevelDB, ScyllaDB, HBase |

Pick an LSM engine for write-heavy streams: events, time series, logs, Spoke's GPS pings. Pick a B+ tree for mixed reads and writes with ranges and joins, like rides and payments. The NoSQL modelling ticket, later in this module, returns to Cassandra.

### Reading EXPLAIN in H2

`EXPLAIN` shows the plan. `EXPLAIN ANALYZE` also runs the query and counts the rows it scanned. In H2, the plan is the query, rewritten, with comments under `FROM`. Here is the real plan for Kakashi's report on the worked example's data, after adding the right index.

```text
SELECT
    "ID"
FROM "PUBLIC"."RIDE"
    /* PUBLIC.IDX_RIDE_BIKE_STARTED: STARTED_AT < TIMESTAMP '2026-10-06 00:00:00'
        AND BIKE_ID = 'B-1042'
        AND STARTED_AT >= TIMESTAMP '2026-10-05 00:00:00'
     */
    /* scanCount: 25 */
WHERE ("STARTED_AT" < TIMESTAMP '2026-10-06 00:00:00')
    AND ("BIKE_ID" = 'B-1042')
    AND ("STARTED_AT" >= TIMESTAMP '2026-10-05 00:00:00')
```

- The first comment names the **access path**. An index name followed by conditions means a jump into that index. `PUBLIC.RIDE.tableScan` means every row.
- `scanCount` (only with `ANALYZE`) is how many rows H2 looked at, plus one final step. A full scan of 20,000 rides shows 20,001. Here 25 means H2 looked at exactly the 24 rides it returned.
- The `WHERE` lines still list every test. H2 rechecks them on each row the index hands over.

### Reading EXPLAIN ANALYZE in PostgreSQL

Spoke runs PostgreSQL in production. Its plan is a tree of steps, read from the most indented line outwards. PostgreSQL prints plans like this (an illustration, trimmed; your numbers will differ):

```text
Seq Scan on ride  (cost=0.00..1043910.00 rows=20 width=8) (actual time=0.512..3105.220 rows=24 loops=1)
  Filter: ((bike_id = 'B-1042') AND (started_at >= '2026-10-05 00:00') AND (started_at < '2026-10-06 00:00'))
  Rows Removed by Filter: 39999976
Execution Time: 3105.301 ms

Index Scan using idx_ride_bike_time on ride  (cost=0.57..96.10 rows=20 width=8) (actual time=0.031..0.052 rows=24 loops=1)
  Index Cond: ((bike_id = 'B-1042') AND (started_at >= '2026-10-05 00:00') AND (started_at < '2026-10-06 00:00'))
Execution Time: 0.068 ms
```

- **The node type:** `Seq Scan` reads the whole table. `Index Scan` searches the index, then fetches rows from the heap. `Index Only Scan` skips the heap. `Bitmap Heap Scan` collects matches from one or more indexes first, then reads heap pages in order.
- **`cost=start..total`** is the planner's guess, in made-up units. **`rows=20`** is its guess of rows returned.
- **`actual time` and actual `rows`** are what really happened. A big gap between estimated and actual rows means stale statistics, and often a bad plan.
- **`Rows Removed by Filter`** is the waste: rows read and then thrown away. 40 million removed to return 24 is Kakashi's whole problem.
- `EXPLAIN (ANALYZE, BUFFERS)` adds how many pages came from memory (`shared hit`) and how many from disk (`read`). That is the number this ticket says matters most.

```callout warning
EXPLAIN ANALYZE really runs the query. On an UPDATE or DELETE, it really changes data. Wrap it in BEGIN; ... ROLLBACK; when you analyse a write.
```

### In an interview

Indexes come up in system design rounds and in "why is this query slow?" questions. Spot them from words like "slow query", "millions of rows", "lookup by", "range of dates" or "write-heavy".

- **"Why is this query slow, and how do you fix it?"** Read the plan, find the full scan, and design a composite index: equality columns first, the range last.
- **"Why do databases use B+ trees, not binary trees or hash tables?"** Pages and fan-out give three or four levels; linked leaves give ranges and sorting.
- **"Clustered or secondary? Why should a primary key be short and growing?"** The InnoDB two-tree lookup, and page splits from random keys.
- **"Design storage for a write-heavy system", like metrics or chat messages.** An LSM tree, with its memtable, SSTables, compaction and bloom filters, and the amplification trade-offs.
- **"Why didn't my index get used?"** A function or cast on the column, a leading `%`, an `OR`, a skipped leftmost column, or low selectivity.

Always state the cost in pages: "height reads to find the start, then one page per further leaf".

## Worked example

Kakashi's report is "every ride on one bike between two times". Here it is on 20,000 rides in H2, one ride per minute, shared across 60 bikes. The program runs `EXPLAIN ANALYZE` three times: with no index, with the index in the wrong order, and in the right order.

```java run
import java.sql.*;

public class Main {
    static final String REPORT = """
        SELECT id FROM ride
        WHERE bike_id = 'B-1042'
          AND started_at >= TIMESTAMP '2026-10-05 00:00:00'
          AND started_at <  TIMESTAMP '2026-10-06 00:00:00'
        """;

    public static void main(String[] args) throws SQLException {
        try (Connection db = DriverManager.getConnection("jdbc:h2:mem:plans");
             Statement sql = db.createStatement()) {
            sql.execute("""
                CREATE TABLE ride (id INT PRIMARY KEY, bike_id VARCHAR(10) NOT NULL, started_at TIMESTAMP NOT NULL);
                INSERT INTO ride
                SELECT x, 'B-' || (1000 + MOD(x, 60)), DATEADD('MINUTE', x, TIMESTAMP '2026-10-01 00:00:00')
                FROM SYSTEM_RANGE(1, 20000);
                """);
            plan(sql, "no index");
            sql.execute("CREATE INDEX idx_time_bike ON ride(started_at, bike_id)");
            plan(sql, "(started_at, bike_id)");
            sql.execute("DROP INDEX idx_time_bike");
            sql.execute("CREATE INDEX idx_bike_time ON ride(bike_id, started_at)");
            plan(sql, "(bike_id, started_at)");
            try (ResultSet rows = sql.executeQuery("SELECT COUNT(*) FROM (" + REPORT + ")")) {
                rows.next();
                System.out.println("rides found: " + rows.getInt(1));
            }
        }
    }

    static void plan(Statement sql, String label) throws SQLException {
        try (ResultSet rows = sql.executeQuery("EXPLAIN ANALYZE " + REPORT)) {
            rows.next();
            String plan = rows.getString(1);
            String index = plan.lines().filter(line -> line.contains("/* PUBLIC.")).findFirst().orElse("");
            String scanned = plan.lines().filter(line -> line.contains("scanCount")).findFirst().orElse("");
            System.out.println(label + " -> " + inside(index) + ", " + inside(scanned));
        }
    }

    /** The words inside one plan comment, up to the first colon. */
    static String inside(String line) {
        String text = line.substring(line.indexOf("/*") + 2).replace("*/", "").trim();
        return text.startsWith("scanCount") ? text : text.split(":")[0];
    }
}
```

It prints:

```text
no index -> PUBLIC.RIDE.tableScan, scanCount: 20001
(started_at, bike_id) -> PUBLIC.IDX_TIME_BIKE, scanCount: 1441
(bike_id, started_at) -> PUBLIC.IDX_BIKE_TIME, scanCount: 25
rides found: 24
```

Line by line:

1. **No index:** H2 reads all 20,000 rides to return 24. At Spoke's 40 million rows, that is the report Kakashi complained about.
2. **`(started_at, bike_id)`:** H2 jumps to the start of 5 October and walks to its end. That day has 1,440 rides from all 60 bikes. H2 looks at all 1,440 (1,441 with the final step) and keeps 24. Better, but the waste grows with every bike Spoke adds.
3. **`(bike_id, started_at)`:** H2 jumps straight to B-1042 on 5 October and walks that bike's slice to the end of the day. It looks at the 24 rides it returns, and nothing else.

The cost of the right index is `height` pages to reach the first entry, plus one page per further leaf. That is a handful of pages instead of 500,000. In PostgreSQL you would create it with `CREATE INDEX CONCURRENTLY`, as database design taught, so riders can keep starting rides while it builds.

## Try it

This program builds 500 riders with indexes on `email` and `phone`, then prints the access path and scan count for one query. Change `query`, run it, and predict each answer first.

1. `WHERE LOWER(email) = 'rider7@spoke.example'`. Does the index survive a function?
2. `WHERE email LIKE 'Rider7%'`, then `WHERE email LIKE '%7@spoke.example'`.
3. `WHERE phone = '07007'`, then `WHERE phone = 07007`. Look at the WHERE line H2 prints too, by removing the filter.
4. `WHERE email = 'Rider7@spoke.example' OR phone = '0700'`. Then try the same two tests joined with `UNION`.

```java run
import java.sql.*;

public class Main {
    public static void main(String[] args) throws SQLException {
        try (Connection db = DriverManager.getConnection("jdbc:h2:mem:tryit");
             Statement sql = db.createStatement()) {
            sql.execute("""
                CREATE TABLE rider (id VARCHAR(10) PRIMARY KEY, email VARCHAR(40) NOT NULL,
                                    phone VARCHAR(20) NOT NULL, name VARCHAR(20) NOT NULL);
                INSERT INTO rider
                SELECT 'rider-' || x, 'Rider' || x || '@spoke.example', '0700' || x, 'Name' || x
                FROM SYSTEM_RANGE(0, 499);
                CREATE INDEX idx_rider_email ON rider(email);
                CREATE INDEX idx_rider_phone ON rider(phone);
                """);
            String query = "SELECT id FROM rider WHERE email = 'Rider7@spoke.example'";
            try (ResultSet rows = sql.executeQuery("EXPLAIN ANALYZE " + query)) {
                rows.next();
                rows.getString(1).lines()
                        .filter(line -> line.contains("/*"))
                        .forEach(line -> System.out.println(line.trim()));
            }
        }
    }
}
```

## Recap

- Databases read whole pages, and a page read from disk costs thousands of times more than a comparison. So count pages, not rows.
- A B+ tree has wide nodes, so its height is about log base fan-out of N: three or four levels for 100 million rows. A lookup reads one page per level, and a range walks the linked leaves.
- Full pages split, which keeps the tree balanced. PostgreSQL keeps rows in a heap with secondary indexes; InnoDB clusters rows in the primary key tree. A covering index answers from the index alone.
- In a composite index, put equality columns first and the one range column last. Functions, implicit casts, a leading `%` and `OR` across columns stop an index being used, and every index slows writes.
- LSM trees take writes in memory and flush sorted files, using compaction and bloom filters to keep reads fast. EXPLAIN shows which path a query really takes.

## Common mistakes

**1. Putting the range column first in a composite index.**

```sql
CREATE INDEX idx_time_bike ON ride(started_at, bike_id);
```

```text
(started_at, bike_id) -> PUBLIC.IDX_TIME_BIKE, scanCount: 1441
```

The index is used, so it looks fine. But it reads every bike's rides in the time range: 1,440 entries for 24 results. Fix: `ON ride(bike_id, started_at)`, which looked at only the 24.

**2. Wrapping the indexed column in a function.**

```sql
SELECT id FROM rider WHERE LOWER(email) = 'rider7@spoke.example';
```

```text
/* PUBLIC.RIDER.tableScan */
/* scanCount: 501 */
```

The index is sorted by `email`, not by `LOWER(email)`, so every row is read. Fix: store emails in lower case and compare directly, or add an expression index in PostgreSQL: `CREATE INDEX ON rider (lower(email))`.

**3. Comparing a text column with a number.**

```sql
SELECT id FROM rider WHERE phone = 07007;
```

```text
/* PUBLIC.RIDER.tableScan */
WHERE "PHONE" = 7007
```

The database casts every phone to a number, so the index on `phone` is skipped, and the leading zero is lost. Fix: `phone = '07007'`, and use `setString` for text parameters in JDBC.

**4. Searching with a leading wildcard.**

```sql
SELECT id FROM rider WHERE email LIKE '%@spoke.example';
```

```text
/* PUBLIC.RIDER.tableScan */
```

A B+ tree is sorted from the first character, so "ends with" cannot jump anywhere. Fix: search by a prefix when you can. For "contains" searches, use a full-text or trigram index.

**5. Adding an index for every column, just in case.**

```sql
CREATE INDEX idx_ride_minutes ON ride(minutes);
CREATE INDEX idx_ride_fare ON ride(fare_cents);
CREATE INDEX idx_ride_bike ON ride(bike_id);
```

Symptom: the morning rush gets slower, and the database disk keeps growing. Every ride insert now updates three more B+ trees, and no report uses them. Fix: index for real queries only, and check `pg_stat_user_indexes` for indexes that are never scanned.

**6. Trusting EXPLAIN without ANALYZE.**

Symptom: the plan says `rows=11`, but the query takes seconds. Plain `EXPLAIN` shows only the planner's guesses. Fix: run `EXPLAIN ANALYZE` to see the actual rows and time, and refresh statistics with `ANALYZE ride` when guesses and reality differ a lot.

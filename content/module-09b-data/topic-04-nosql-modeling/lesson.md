## Hook

A library sorts its books by subject, author and title. You can find anything, but each search means walking the shelves. A bike shop's parts wall is different. Every drawer is labelled with the exact part mechanics ask for most: "brake pads, EBIKE". One glance, one drawer.

The library is a relational database: flexible, good at any question. The parts wall is a NoSQL table: built for the few questions you know in advance, and fast at exactly those.

Jiraiya's question is which wall Spoke's ride history belongs on. To answer it, you need to know how each kind of NoSQL database thinks.

## Concept

### Why NoSQL exists

Relational databases, like PostgreSQL, run on one main server for writes. When that server is full, the usual answer is a bigger server: **scaling up**. At some size, there is no bigger server.

**NoSQL** databases were built for that size. Most give you three things:

- **Scale-out**: data is split across many machines, and you add machines to grow. Each machine holds some **partitions** of the data.
- **Predictable latency for known questions**: a question that reads one partition takes about the same time at 1,000 rows or 10 billion.
- **A flexible schema**: two items in one table can have different fields, with no `ALTER TABLE`.

```callout jargon
**NoSQL** is a loose name for databases that are not relational tables queried with SQL. **Scaling out** means adding machines; **scaling up** means a bigger machine. A **partition** is one slice of the data, stored together on one machine and its copies.
```

The four big families are **key-value**, **document**, **wide-column** and **graph** stores. This ticket models data in the first three, and names the fourth.

### What you give up

The speed is not free. To spread data over many machines and keep every question to one partition, most NoSQL stores drop things SQL gives you:

| You give up | What it means for Spoke |
|---|---|
| **Joins** | "rides with the rider's name" must store the name in the ride, or make two calls |
| **Ad-hoc queries** | "average fare by station and weekday" needs data exported elsewhere, or a full scan |
| **Multi-item transactions**, or you get limited, costlier ones | moving credit between two riders is no longer one easy `BEGIN ... COMMIT` |
| **Strong consistency by default** | a read straight after a write may return the old value |
| **Constraints** | no foreign keys, no `CHECK`, often no unique constraint beyond the key |

The deepest cost is the second one. In SQL you design the data, then ask any question. In NoSQL you design **for** your questions. A new question can mean a new table and a data migration.

### Access patterns first

SQL design is **normalise first**: model riders, rides and stations, each fact in one place, as in [Database design](/t/module-08-backend/topic-14-database-design). Questions come later, helped by indexes.

NoSQL design is **access patterns first**. You start by writing down every question the app will ask, with how often and how fast.

```callout jargon
An **access pattern** is one specific way the app reads or writes data, such as "get a rider's last 50 rides, newest first". **Denormalising** means copying data into several places, so each question finds everything in one read.
```

Spoke's ride history list:

| # | Access pattern | Rate at the morning rush | Target |
|---|---|---|---|
| 1 | a rider's last 50 rides, newest first | thousands a second | under 10 ms |
| 2 | rides that started at a station in a time range | a few a second | under 100 ms |
| 3 | add a finished ride | 5,000 starts in the rush | under 20 ms |
| 4 | one ride by its id | rare | under 50 ms |

Then you design keys so each pattern reads **one partition**, in the order it needs. The rest of this page shows how, store by store.

### Key-value stores

The simplest NoSQL store is a giant map. You `put` a value under a key, and `get` it back by that exact key. The store does not look inside the value.

- **Examples**: Redis, Memcached, and DynamoDB used with only a partition key.
- **Good at**: lookups by one known key, in well under a millisecond in memory. Sessions, feature flags, a bike's latest position.
- **Bad at**: anything else. "All bikes at Harbor" is impossible unless you also store a key for exactly that question.

```text
put  bike:B-1042:position   {"lat": 51.507, "lon": -0.087, "battery": 64}
get  bike:B-1042:position
```

The key's shape, such as `bike:B-1042:position`, is your whole data model. A later ticket, Redis patterns, builds counters, leaderboards and locks on this idea.

### Document stores: MongoDB

A **document store** keeps each record as a **document**: a JSON-like tree of fields, lists and nested objects. Documents live in **collections**. MongoDB, the best-known one, stores them in a binary JSON format called BSON.

```json
{
  "_id": "rental-17",
  "riderId": "rider-7",
  "bikeId": "B-1042",
  "bikeType": "EBIKE",
  "from": { "stationId": "harbor", "name": "Harbor" },
  "to":   { "stationId": "old-mill", "name": "Old Mill" },
  "startedAt": "2026-11-18T08:10:00Z",
  "minutes": 12,
  "fareCents": 460
}
```

Unlike a key-value store, the database understands the fields. You can query and index them: an index on `{ riderId: 1, startedAt: -1 }` serves "a rider's rides, newest first". MongoDB indexes look a lot like the B-trees from [How indexes work](/t/module-09b-data/topic-02-index-internals).

Two facts shape every MongoDB design:

- **A write to one document is atomic.** Multi-document transactions exist, but cost more. So design so that most writes touch one document.
- **The schema is flexible**, but your code still expects fields. A validator on the collection can enforce required fields.

### Embed or reference

The core MongoDB decision: put related data **inside** the document (**embed**), or store it separately and keep its id (**reference**).

| Embed when | Reference when |
|---|---|
| the data is read together with its parent, every time | it is read on its own, or by many parents |
| it belongs to one parent only | it is shared, like a station used by every ride |
| it has a small, known maximum size | it can grow without limit |
| it rarely changes after it is written | it changes often, and copies would go stale |

The ride document above embeds `from` and `to` with their names. That is deliberate: a receipt must show the station name as it was on the day. It references the rider by `riderId`, because rider details change and are shared by thousands of rides.

Joins exist in MongoDB, through `$lookup`, but they are slower than one-document reads. If a screen needs a join on every view, the model is probably wrong.

### Traps: unbounded arrays and the 16 MB limit

The tempting design is one rider document with every ride inside:

```json
{ "_id": "rider-7", "name": "Ada", "rides": [ { "rentalId": "rental-12", "..." : "..." }, { "...": "..." } ] }
```

It works on day one. Then the `rides` array grows forever, and three things go wrong:

- **Every new ride rewrites a bigger document**, so writes slow down as the rider grows.
- **Every read drags the whole history** over the network, even for the profile screen.
- **The hard limit**: a MongoDB document can be at most **16 MB**. A heavy rider's document eventually fails to save.

```callout jargon
An **unbounded array** is a list inside a document with no maximum length. It is the classic document-store trap.
```

The fix is to keep rides in their own `rides` collection, one document each, referencing the rider. If the profile screen shows the last 3 rides, also embed a small copy of just those 3. That is called the **subset pattern**.

### Cassandra: partition key and clustering columns

A **wide-column store**, such as Apache Cassandra, stores rows in tables, but splits them across machines by a **partition key**.

- The **partition key** is hashed to a position on a ring of machines, much like the [consistent hashing](/t/module-09-architecture/topic-06-system-design) you met in system design. All rows with the same partition key live together on the same machines.
- The **clustering columns** sort the rows **inside** a partition, on disk. Reading "the first 50 rows of this partition" is one short sequential read.

```callout jargon
The **partition key** decides which machines hold a row. **Clustering columns** decide the order of rows within a partition. Together they make the **primary key**, which must be unique.
```

Cassandra writes through a log-structured merge tree, the **LSM tree** from How indexes work. Writes are appends, so it takes huge write volumes well.

The rule that drives everything: **a query must give the full partition key**. It can then narrow and order by the clustering columns. A query that does not name a partition would have to ask every machine. Cassandra refuses it unless you add `ALLOW FILTERING`, which you should treat as a warning sign.

### Syntax: a Cassandra table

```sql
CREATE TABLE rides_by_rider (
    rider_id   text,
    started_at timestamp,
    rental_id  text,
    from_station text,
    to_station   text,
    minutes    int,
    fare_cents bigint,
    PRIMARY KEY ((rider_id), started_at, rental_id)
) WITH CLUSTERING ORDER BY (started_at DESC, rental_id ASC);

SELECT * FROM rides_by_rider WHERE rider_id = 'rider-7' LIMIT 50;
```

Cassandra's query language, **CQL**, looks like SQL on purpose. Read the primary key from the inside out:

- `(rider_id)`, in the inner brackets, is the **partition key**: all of one rider's rides live together.
- `started_at, rental_id` are **clustering columns**: rows are sorted by start time, newest first, as `CLUSTERING ORDER BY` says. `rental_id` breaks ties, so two rides starting in the same second stay unique.
- The `SELECT` names one partition and takes the first 50 rows. No sort happens at query time; the rows are already in order on disk.

### One table per query

`rides_by_rider` answers pattern 1. It cannot answer pattern 2, "rides at Harbor between 08:00 and 09:00", because that query does not name a rider. In Cassandra, the answer is a **second table**, keyed for that question:

```sql
CREATE TABLE rides_by_station_day (
    station_id text,
    day        date,
    started_at timestamp,
    rental_id  text,
    rider_id   text,
    minutes    int,
    PRIMARY KEY ((station_id, day), started_at, rental_id)
) WITH CLUSTERING ORDER BY (started_at ASC, rental_id ASC);
```

Every finished ride is now written **twice**, once to each table. That is denormalisation, on purpose. Storage is cheap; a slow query at rush hour is not.

Keeping copies in step is your job. Cassandra's **logged batch** makes sure both writes eventually apply, though not at the same instant. Some teams instead write one table and feed the others from a stream of changes, as in the [change data capture](/t/module-09-architecture/topic-04-messaging) card of Messaging.

### Wide partitions and buckets

Look again at `rides_by_station_day`. Why is `day` in the partition key? Imagine it were not:

```sql
PRIMARY KEY ((station_id), started_at, rental_id)    -- every Harbor ride, forever
```

Harbor is busy. Every ride that ever starts there lands in **one** partition, which grows without end. A partition lives on the same few machines, so those machines fill up and slow down. Reads and repairs of a giant partition get slower and slower. This is a **wide partition**, Cassandra's version of the unbounded array.

```callout jargon
**Bucketing** means adding a time slice, such as a day or a month, to the partition key, so each partition stays a bounded size.
```

With `(station_id, day)`, each partition holds one station's rides for one day: a few thousand rows at most. A common rule of thumb is to keep partitions well under 100 MB.

The cost of buckets: "Harbor between 23:00 and 01:00" now crosses two partitions, so the app makes two queries. Pick a bucket size that fits most queries in one bucket.

`rides_by_rider` has no bucket. A very active rider takes maybe 700 rides a year, which stays small for decades. If riders took millions of rides, it would need a bucket too.

### Tombstones

Cassandra never edits files in place, so a delete cannot simply remove a row. It writes a marker called a **tombstone** that says "this row is deleted". Reads must skip over tombstones. **Compaction**, the background merge of the LSM tree's files, removes them later.

```callout jargon
A **tombstone** is a marker written by a delete, or by data whose time to live (TTL) has expired. It stays for at least `gc_grace_seconds`, 10 days by default, so every copy of the data learns about the delete.
```

The trap is a table used as a queue: write a row, process it, delete it. Soon the partition is mostly tombstones, and every read wades through thousands of them. By default, Cassandra warns after 1,000 tombstones in one read and fails the read after 100,000.

Design so that you rarely delete individual rows. Let whole old buckets expire, or drop old tables. A queue belongs in a message broker.

### DynamoDB: partition key and sort key

Amazon **DynamoDB** is a managed key-value and document store. Each item has a primary key made of:

- a **partition key**, hashed to choose the partition, and
- an optional **sort key**, which orders items with the same partition key.

Items that share a partition key form an **item collection**, stored together and sorted by the sort key. That is the same shape as a Cassandra partition with clustering columns.

```callout jargon
An **item** is one record in DynamoDB, a set of named attributes, at most **400 KB** including the attribute names. An **item collection** is all items with the same partition key.
```

Text sort keys are compared character by character. So build them to sort the way you read. ISO timestamps such as `2026-11-18T08:10` sort in time order, because the biggest unit comes first. One more rule: a key that starts with the whole of another sorts after it. `2026-11-18T09:00#rental-19` comes after `2026-11-18T09:00`.

### Query, not scan

DynamoDB has two ways to read many items, and the difference is everything.

- A **Query** names **one** partition key value, with an optional condition on the sort key: `=`, `<`, `>`, `BETWEEN` or `begins_with`. It reads only that item collection, in sort key order, either direction.
- A **Scan** reads **every item in the table**, page by page, up to 1 MB per page.

```animate
title: Query versus Scan
kind: scene
nodes:
  - { id: app, label: Rider app, emoji: "📱", value: "My rides" }
  - { id: p1, label: "Partition 1", emoji: "🗂️", value: "rider-3, rider-7", row: 1, col: 0 }
  - { id: p2, label: "Partition 2", emoji: "🗂️", value: "rider-9, rider-12", row: 1, col: 2 }
  - { id: p3, label: "Partition 3", emoji: "🗂️", value: "rider-4, rider-15", row: 1, col: 4 }
steps:
  - say: rider-7 opens My rides. A Query names the partition key RIDER#rider-7. Hashing it points straight at partition 1.
    send: { from: app, to: p1, label: "Query RIDER#rider-7" }
    focus: [p1]
  - say: Inside the partition, items are sorted by sort key. The Query reads the newest 50 and stops. One partition, one short read.
    send: { from: p1, to: app, label: "50 rides", back: true }
  - say: Now a Scan with a filter, "rides by rider-7". It has no partition key, so it must read every partition.
    send: [{ from: app, to: p1, label: Scan }, { from: app, to: p2, label: Scan }, { from: app, to: p3, label: Scan }]
    focus: [p1, p2, p3]
  - say: It reads every item, then the filter throws most away. You pay for every item read, not just the ones returned.
    set: { p1: "read all", p2: "read all", p3: "read all" }
  - say: The Query stays fast as the table grows. The Scan gets slower and costlier with every new rider.
    focus: [app]
```

A **filter expression** runs **after** the read. It saves network, but not time or cost. If a screen needs a Scan, it needs a new key or index instead.

### Secondary indexes: GSI and LSI

A Query can only use the table's own keys. For a second access pattern, DynamoDB offers two kinds of secondary index.

| | Global secondary index (**GSI**) | Local secondary index (**LSI**) |
|---|---|---|
| Keys | any partition key and sort key | the **same** partition key, a different sort key |
| When created | any time | only when the table is created |
| Reads | eventually consistent only | can be strongly consistent |
| Capacity | its own, separate from the table | shared with the table |
| Size limit | none per key | an item collection may hold at most 10 GB |

A GSI is like DynamoDB keeping a second copy of the table, keyed differently, updated **asynchronously** after each write. That is why a read from it can briefly miss a write. It is the managed version of Cassandra's second table.

Most designs use GSIs. LSIs only help when you need another sort order within the same partition, and you must know that on day one.

### Single-table design

Many DynamoDB experts put **every entity in one table**, with generic key names `PK` and `SK`. The key values carry the meaning:

| PK | SK | What the item is |
|---|---|---|
| `RIDER#rider-7` | `PROFILE` | rider-7's profile |
| `RIDER#rider-7` | `RIDE#2026-11-17T08:05#rental-15` | one of rider-7's rides |
| `RIDER#rider-7` | `RIDE#2026-11-18T08:10#rental-17` | another, newer ride |
| `RIDE#rental-17` | `RIDE#rental-17` | the ride by its id, for pattern 4 |

```callout jargon
**Single-table design** stores many kinds of item in one DynamoDB table, using prefixes such as `RIDER#` and `RIDE#` in generic `PK` and `SK` keys. Items that are read together share a partition key.
```

Why bother? One Query on `PK = RIDER#rider-7` can return the profile **and** the rides together, in one round trip. With `begins_with(SK, 'RIDE#')` it returns only the rides. A tiny "join" happens for free, because you stored the data pre-joined.

The cost is readability. The table makes sense only next to its list of access patterns. Write that list down, and keep it with the code.

### Hot partitions and capacity

Scale-out only works if traffic spreads over many partitions. A **hot partition** is one partition key value that gets far more traffic than the rest.

Imagine storing every GPS ping under `PK = DAY#2026-11-18`. At 08:00, ten cities at about 170 pings a second each send roughly 1,700 writes a second, all to one partition. In DynamoDB, each partition serves at most 3,000 read units and 1,000 write units a second. Past that, requests to it are **throttled**, even when the table as a whole has capacity to spare.

```animate
title: A hot partition at 08:00
kind: scene
nodes:
  - { id: rush, label: "GPS pings", emoji: "📡", value: "1,700 a second" }
  - { id: hot, label: "DAY#2026-11-18", emoji: "🔥", value: idle, row: 1, col: 0 }
  - { id: b1, label: "BIKE#B-1042#2026-11-18", emoji: "🗂️", value: idle, row: 1, col: 2 }
  - { id: b2, label: "BIKE#B-1088#2026-11-18", emoji: "🗂️", value: idle, row: 1, col: 4 }
steps:
  - say: With PK = DAY#2026-11-18, every ping from every city has the same partition key.
    send: { from: rush, to: hot, label: "every write" }
    set: { hot: "1,700 writes/s" }
  - say: One partition takes at most 1,000 write units a second. It throttles the rest. The other partitions do nothing.
    set: { hot: "throttling" }
    focus: [hot]
  - say: Key the pings by bike and day instead. Thousands of bikes means thousands of partition keys.
    send: [{ from: rush, to: b1, label: "1 every 10 s" }, { from: rush, to: b2, label: "1 every 10 s" }]
    set: { hot: idle, b1: "light load", b2: "light load" }
  - say: Each partition takes a tiny share. Pick partition keys with many values, used evenly.
    focus: [b1, b2]
```

DynamoDB has two capacity modes:

- **On-demand**: pay per request, with no planning. Good for new or spiky tables, costlier per request at steady load.
- **Provisioned**: you set read and write units a second, often with auto scaling. Cheaper for steady, predictable traffic. Go over and requests are throttled.

Neither mode saves a hot partition. Only a better key does, such as adding a bucket or a random suffix to spread one busy key over several.

### Consistency settings

Every serious NoSQL store keeps **copies** of each partition on several machines, so a machine can die without losing data. Copies take a moment to agree, so each read and write chooses how many copies to wait for.

- **DynamoDB**: reads are **eventually consistent** by default, and may miss a write from a moment ago. Ask for a **strongly consistent read** with `ConsistentRead`, on the table or an LSI, never a GSI. It costs twice the read capacity.
- **Cassandra**: each query sets a **consistency level**. `ONE` waits for one copy. `QUORUM` waits for a majority, 2 of 3 copies. `ALL` waits for every copy.

```callout jargon
The **replication factor** is how many copies are kept, often 3. A **quorum** is a majority of them. If writes and reads both use `QUORUM`, every read group overlaps every write group, so a read sees the latest successful write.
```

With 3 copies, 2 + 2 > 3, so the groups must share a machine. Spoke's "my rides" can live with eventual consistency, but the screen right after "Ride ended" should show the new ride. Use a strong read there, or simply show the ride the app already has. A later ticket, Replication and sharding, takes quorums apart in depth.

### Graph and time-series stores

Two more families are worth knowing by name.

- **Graph databases**, such as Neo4j, store **nodes** and **relationships** and are fast at following many hops. "Which stations do riders who start at Harbor ride to next, three hops out?" is a graph question. SQL can answer it with recursive queries, but slowly.
- **Time-series databases**, such as TimescaleDB, a PostgreSQL extension, or InfluxDB, store values over time. They split data by time, compress it hard, roll old data up into averages, and drop it after a retention period.

Spoke's GPS pings are a perfect time-series case. Riverton sends about 30 pings a second, and about 170 at 08:00. Pings are only appended, never updated, and read by bike and time range.

### When Postgres is still the right answer

Back to Jiraiya's ticket. Is a NoSQL store needed for "a rider's last 50 rides in under 10 ms"? Do the numbers first.

- Spoke has 100,000 riders. Even 40 million rides is a modest PostgreSQL table.
- An index on `(rider_id, started_at DESC)` turns the query into one short index range read. That is a few milliseconds, the same shape as a DynamoDB Query.
- "In every city" is mostly about **distance**. A database 3,000 km away costs far more than 10 ms in network time. The fix is a read replica or a cache near each city, which Postgres supports too.

Choose PostgreSQL when you need transactions, constraints and joins, when questions change often, and when one primary can take the writes. Choose a NoSQL store when the access patterns are few and stable, and the write volume or data size outgrows one primary. Or when you need active copies in several regions.

```callout tip
Gojo says: "We might need DynamoDB later" is not a reason to move today. Measure the real query first, with the real index.
```

### Polyglot persistence

Big systems rarely use one database. **Polyglot persistence** means using several kinds, each for the data it suits best:

| Spoke data | Store | Why |
|---|---|---|
| rentals, payments, bikes, stations | PostgreSQL | transactions and constraints protect money |
| sessions, latest bike positions, rate limits | Redis | sub-millisecond key lookups |
| GPS pings | a time-series store | append-only, by time, rolled up and expired |
| ride history at very large scale | DynamoDB or Cassandra | a few fixed access patterns, huge volume |

Every extra store has a price. Each needs backups, monitoring, upgrades, on-call skills and security reviews. Data copied between stores must be kept in step, often with an outbox or change data capture. Add a store only when the one you have truly cannot do the job.

### In an interview

NoSQL modelling shows up in system design rounds. Spot it when you hear "billions of rows", "global", "low latency at any scale", "news feed", "chat history" or "time series". Or simply "which database would you use?".

- **"Design the data model for a chat app's message history."** Partition by conversation, sort by time, bucket by month for busy chats.
- **"SQL or NoSQL for X?"** Start from the access patterns and the consistency needs, not from fashion. Say what you give up.
- **"Model a user's timeline or order history in DynamoDB."** `PK = USER#id`, `SK = ORDER#timestamp#id`, a Query with `begins_with`, newest first.
- **"How do you avoid hot partitions?"** Keys with many distinct values, buckets, and random suffixes for very hot keys.
- **"What is a quorum?"** R + W > N, with numbers.

The strongest answers name both designs, then pick one and say why. "Postgres with an index now, DynamoDB if writes outgrow one primary" is a senior answer.

## Worked example

Model Spoke's two hardest patterns in both stores, with exact keys.

**Pattern 1: a rider's last 50 rides, newest first.**

Cassandra uses the `rides_by_rider` table from the Syntax card:

```sql
SELECT rental_id, started_at, from_station, to_station, fare_cents
FROM rides_by_rider
WHERE rider_id = 'rider-7'
LIMIT 50;
```

DynamoDB, in a single table:

```text
Item:   PK = RIDER#rider-7      SK = RIDE#2026-11-18T08:10#rental-17
Query:  KeyConditionExpression  PK = :pk AND begins_with(SK, :prefix)
        :pk = "RIDER#rider-7"    :prefix = "RIDE#"
        ScanIndexForward = false      (newest first)
        Limit = 50
```

Both read one partition, already sorted, and stop after 50 items. The cost does not grow with the number of riders.

**Pattern 2: rides that started at a station in a time range.**

Cassandra uses `rides_by_station_day`, bucketed by day:

```sql
SELECT rental_id, started_at, rider_id
FROM rides_by_station_day
WHERE station_id = 'harbor' AND day = '2026-11-18'
  AND started_at >= '2026-11-18 08:00:00+0000'
  AND started_at <  '2026-11-18 09:00:00+0000';
```

DynamoDB adds a GSI, `GSI1`, with its own keys stored on each ride item:

```text
Item:   PK = RIDER#rider-7   SK = RIDE#2026-11-18T08:10#rental-17
        GSI1PK = STATION#harbor#2026-11-18
        GSI1SK = 2026-11-18T08:10#rental-17
Query on GSI1:
        GSI1PK = "STATION#harbor#2026-11-18"
        AND GSI1SK BETWEEN "2026-11-18T08:00" AND "2026-11-18T09:00"
```

Line by line:

1. **The day is inside `GSI1PK`**, the same bucket as Cassandra's `day`. Harbor's history never becomes one giant, hot partition.
2. **The rental id ends each sort key.** Two rides can start in the same minute; the id keeps both keys unique.
3. **`BETWEEN` includes both ends, but compares text.** `2026-11-18T09:00#rental-19` sorts **after** `2026-11-18T09:00`, so a ride starting at exactly 09:00 is left out. Here that gives the half-open hour you want. Know which one your bounds give.
4. **The GSI is eventually consistent.** A ride that ended a second ago may be missing from the station view. For an ops dashboard, that is fine.
5. **Every ride is written once** in DynamoDB; the GSI copy is maintained for you. In Cassandra, the app writes both tables.

**The verdict for Jiraiya.** At 100,000 riders, PostgreSQL meets the 10 ms goal. It needs an index on `(rider_id, started_at DESC)` and a replica near each city. It keeps transactions, joins and ad-hoc reports. The DynamoDB design above is ready if ride writes ever outgrow one primary. Moving now would trade away joins and constraints for scale Spoke does not yet need.

## Try it

This program models a DynamoDB table and its GSI with Java's sorted maps. A `TreeMap` keeps its keys sorted as text, just like a partition's sort keys.

```java run
import java.util.*;

public class Main {
    // A model of one DynamoDB table: partition key -> items sorted by sort key
    static final Map<String, NavigableMap<String, String>> table = new HashMap<>();
    // A model of its GSI1: GSI1PK -> items sorted by GSI1SK, kept up to date on every put
    static final Map<String, NavigableMap<String, String>> gsi1 = new HashMap<>();

    static void put(String pk, String sk, String item, String gsi1pk, String gsi1sk) {
        table.computeIfAbsent(pk, k -> new TreeMap<>()).put(sk, item);
        if (gsi1pk != null) {
            gsi1.computeIfAbsent(gsi1pk, k -> new TreeMap<>()).put(gsi1sk, item);
        }
    }

    // Query: ONE partition, sort keys that begin with a prefix, newest first, at most limit items
    static List<String> queryNewestFirst(String pk, String prefix, int limit) {
        NavigableMap<String, String> partition = table.getOrDefault(pk, new TreeMap<>());
        List<String> items = new ArrayList<>();
        for (String item : partition.subMap(prefix, true, prefix + Character.MAX_VALUE, false).descendingMap().values()) {
            if (items.size() == limit) break;
            items.add(item);
        }
        return items;
    }

    // Query on the GSI: ONE partition, sort key BETWEEN from AND to (both ends included)
    static Collection<String> queryBetween(String gsi1pk, String from, String to) {
        return gsi1.getOrDefault(gsi1pk, new TreeMap<>()).subMap(from, true, to, true).values();
    }

    public static void main(String[] args) {
        put("RIDER#rider-7", "PROFILE", "rider-7 profile: Ada, home harbor", null, null);
        put("RIDER#rider-7", "RIDE#2026-11-16T17:40#rental-12", "rental-12 uptown -> harbor", "STATION#uptown#2026-11-16", "2026-11-16T17:40#rental-12");
        put("RIDER#rider-7", "RIDE#2026-11-17T08:05#rental-15", "rental-15 harbor -> depot", "STATION#harbor#2026-11-17", "2026-11-17T08:05#rental-15");
        put("RIDER#rider-7", "RIDE#2026-11-18T08:10#rental-17", "rental-17 harbor -> old-mill", "STATION#harbor#2026-11-18", "2026-11-18T08:10#rental-17");
        put("RIDER#rider-9", "RIDE#2026-11-18T08:40#rental-18", "rental-18 harbor -> uptown", "STATION#harbor#2026-11-18", "2026-11-18T08:40#rental-18");
        put("RIDER#rider-3", "RIDE#2026-11-18T09:00#rental-19", "rental-19 harbor -> depot", "STATION#harbor#2026-11-18", "2026-11-18T09:00#rental-19");

        System.out.println("rider-7, last 2 rides:");
        queryNewestFirst("RIDER#rider-7", "RIDE#", 2).forEach(item -> System.out.println("  " + item));

        System.out.println("harbor, 08:00 to 09:00 on 2026-11-18:");
        queryBetween("STATION#harbor#2026-11-18", "2026-11-18T08:00", "2026-11-18T09:00")
            .forEach(item -> System.out.println("  " + item));
    }
}
```

1. Change the prefix from `"RIDE#"` to `""` and the limit to 5. Which extra item appears, and why is it last?
2. Change the upper bound to `"2026-11-18T09:00~"`. `~` sorts after `#`, so does rental-19 appear now?
3. Store a ride with the time written as `18/11/2026 08:30`. Where does it sort, and why do ISO timestamps matter?
4. Write the key for a new pattern: "a bike's rides, newest first". Which partition key and sort key would you choose?

## Recap

- NoSQL stores scale out and give predictable latency for known questions. You give up joins, ad-hoc queries, easy multi-item transactions, constraints and, often, strong consistency by default.
- Design access patterns first: list every question, then choose keys so each one reads a single partition, in order. Denormalise on purpose.
- MongoDB: embed what is read together and bounded; reference what is shared or grows. Unbounded arrays hit the 16 MB document limit.
- Cassandra: partition key plus clustering columns, one table per query, buckets against wide partitions, and few deletes because of tombstones. DynamoDB: partition and sort keys, Query not Scan, GSIs, single-table `PK`/`SK` design, no hot partitions, items up to 400 KB.
- Choose consistency per read (`QUORUM`, `ConsistentRead`). Postgres with the right index is often still the answer, and polyglot persistence has a running cost.

## Common mistakes

**1. Modelling the entities first, then hoping the queries fit.**

```sql
CREATE TABLE rides (rental_id text PRIMARY KEY, rider_id text, station_id text, started_at timestamp);
SELECT * FROM rides WHERE rider_id = 'rider-7';
```

Symptom: Cassandra refuses the query, because it does not name the partition key, and suggests `ALLOW FILTERING`. Adding it makes every machine read every row. Fix: list the access patterns first, then build one table per pattern, such as `rides_by_rider` keyed by `rider_id`.

**2. An unbounded array inside a document.**

```json
{ "_id": "rider-7", "rides": [ "...every ride ever, forever..." ] }
```

Symptom: saves get slower as the rider rides more, and every profile read drags the whole history. One day a save fails, because the document passed 16 MB. Fix: a separate `rides` collection, one document per ride, referencing the rider. Embed a small subset of the latest rides if a screen needs it.

**3. A partition key that is the same for everyone at the same time.**

```text
PK = DAY#2026-11-18    SK = 08:10:05#B-1042        (every GPS ping, every city)
```

Symptom: at 08:00, ping writes are throttled while the table's dashboard shows plenty of spare capacity. Every ping hits one partition. Fix: a partition key with many values used evenly, such as `BIKE#B-1042#2026-11-18`: one bike, one day.

**4. Using Scan with a filter for a screen.**

```text
Scan  table = spoke   FilterExpression: riderId = :r
```

Symptom: "My rides" works in testing and slows down every week in production, with a bill to match. The Scan reads and charges for every item in the table; the filter drops them afterwards. Fix: a Query on a partition key that names the rider.

**5. Bucketing a station's rides by nothing.**

```sql
PRIMARY KEY ((station_id), started_at, rental_id)
```

Symptom: after a year, queries on Harbor time out and repairs struggle, while quiet stations are fine. Harbor's partition holds every ride it ever saw. Fix: put a time bucket in the partition key, `((station_id, day), started_at, rental_id)`.

**6. Moving to NoSQL before measuring.**

```text
Plan: move all ride history to DynamoDB.   Reason: "My rides feels slow in a city far from the database."
```

Symptom: the team plans a migration, but the database spends only a few milliseconds on the query. The rest is the trip to a database in another region, and DynamoDB in that same region would be just as far. Fix: measure where the time goes first, from the server and the database. Then put a read replica or cache near each city. Move stores only when measurements say one primary truly cannot cope.

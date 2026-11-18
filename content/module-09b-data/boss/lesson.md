## What happened

It is 1 November. Finance runs October's month-end report, and Kakashi's phone lights up three times before lunch.

1. **Wrong totals.** The revenue report lists only three of Riverton's five stations. Depot and Uptown are gone, so the stations no longer add up to the month.
2. **A database on its knees.** The report query runs for minutes. While it runs, unlocks and returns slow down for every rider.
3. **A broken export.** The city council gets a file of every October ride. It has some rides twice, and others not at all.

Your job: fix the report code so all three are gone before the next month-end.

```callout jargon
A **month-end report** is a summary that finance builds once a month, such as revenue per station. An **export** is a copy of data written out for someone else, here a file for the city council. A **page** is one slice of a long result, fetched one at a time.
```

### Two stations vanished

The report starts from `station` and LEFT JOINs `ride`, so that every station keeps a row. Depot had no rides, so its joined row is all NULLs on the ride side:

```text
s.id    r.id   r.started_at   EXTRACT(YEAR FROM r.started_at)
depot   null   null           null
```

Then the old SQL filters the month in `WHERE`: `EXTRACT(YEAR FROM r.started_at) = ?`. NULL compared with 2026 is not true, so `WHERE` throws Depot's row away. The LEFT JOIN kept it, and the `WHERE` undid that. The join now behaves like an inner join.

Uptown vanished the same way. Its only ride was in September, so the filter dropped that row too.

```callout jargon
An **inner join** keeps only rows that match on both sides. A **LEFT JOIN** keeps every row of the left table, and fills the right side with NULL when nothing matches.
```

A filter on the right table belongs in `ON`. There it decides which rides join, and a station with none still keeps its NULL row. Two smaller traps hide behind it:

- `COUNT(*)` counts rows, and the NULL row is a row. Depot would show 1 ride. `COUNT(r.id)` skips NULLs, so it says 0.
- `SUM` over no values is NULL, not 0. `COALESCE(SUM(r.fare_cents), 0)` turns that NULL into 0.

### Why the report ground the database

The month filter wraps `started_at` in a function. An index stores the column's values in sorted order. It cannot look up "values whose `EXTRACT(MONTH ...)` is 10", because that is a different value, in a different order.

H2 did find an index to use: the one behind `REFERENCES station(id)`, on `station_id` alone. It finds every ride a station ever had. So for one month, the report reads the station's whole history and tests each ride by hand. Spoke has years of rides.

The fix has two parts:

- A **composite index** on `(station_id, started_at)`. Inside one station, rides sit in time order, so October is one unbroken slice.
- A **half-open range** on the bare column: `started_at >= '2026-10-01 00:00'` and `started_at < '2026-11-01 00:00'`. The index can jump to the slice's start, and stop at its end.

```callout jargon
A **composite index** is one index on two or more columns, sorted by the first, then the second. A **half-open range** includes its start and excludes its end. A filter the index can use directly is called **sargable**: it compares the bare column, not a function of it.
```

Half-open beats `BETWEEN`. `BETWEEN` includes both ends, so a ride at exactly 00:00 on 1 November would count in October and in November.

### Why the export repeated and lost rides

The export asks for 500 rows at a time with `LIMIT 500 OFFSET n`. `OFFSET` means "skip the first n rows". But the rows keep changing while the export runs, because a ride is saved when it **ends**.

```animate
title: OFFSET paging while rides arrive
kind: scene
nodes:
  - { id: council, label: Council export, emoji: "📄", row: 0, col: 0 }
  - { id: db, label: ride table, emoji: "🗄️", value: "rides 1 to 10, by start time", row: 0, col: 3 }
  - { id: rider, label: A long ride ends, emoji: "🚲", row: 2, col: 3 }
steps:
  - say: The export asks for page 1, five rides. It gets rides 1 to 5, which started from 06:00 to 10:00.
    send: { from: council, to: db, label: "LIMIT 5 OFFSET 0" }
    set: { council: "has 1, 2, 3, 4, 5" }
  - say: A rider who unlocked at 06:30 docks now. Their ride is saved with started_at 06:30, so it sorts second.
    send: { from: rider, to: db, label: "INSERT, started 06:30" }
    set: { db: "1, NEW, 2, 3, 4, 5, 6 ..." }
  - say: The export asks for page 2 by skipping five rows. But ride 5 is now the sixth row.
    send: { from: council, to: db, label: "LIMIT 5 OFFSET 5" }
  - say: Page 2 starts with ride 5 again. The council gets ride 5 twice, and the new ride never.
    set: { council: "5 twice!" }
  - say: A deleted ride does the opposite. Every row moves up one place, and one ride is skipped.
    focus: [db]
  - say: The fix is a cursor. Page 2 asks for rides after "10:00, ride 5". New rows elsewhere cannot move that point.
    send: { from: council, to: db, label: "after (10:00, 5)" }
    set: { council: "6, 7, 8, 9, 10" }
```

A cursor does not promise everything, though. A ride saved after the export began, but sorting behind the cursor, is not in this file. An example is a ride that started before the cursor point and was saved only when it ended. It comes in the next run. If the file must be complete, export after the month has closed, or read every page from one snapshot.

```callout jargon
**Keyset pagination** fetches the next page by "rows after the last one I saw", using the sort columns, instead of "skip n rows". The saved position is a **cursor**. It needs a unique order, so the id breaks ties between rides that start in the same second.
```

## What you need

You learned each idea in an earlier ticket. Here they meet in one incident.

- Joins, `ON` versus `WHERE`, NULL rules and aggregates: [Advanced SQL](/t/module-09b-data/topic-01-advanced-sql), and the basics in [SQL basics](/t/module-08-backend/topic-07-sql-basics).
- How an index is sorted, the leftmost prefix rule, why a function hides a column, and `EXPLAIN`: [Index internals](/t/module-09b-data/topic-02-index-internals).
- `PreparedStatement`, `setObject` and reading rows: [JDBC](/t/module-08-backend/topic-08-jdbc).
- Long queries that hurt everyone else on a busy database: [Postgres in production](/t/module-09b-data/topic-08-postgres-in-production).
- Cursors instead of OFFSET: [REST design](/t/module-08-backend/topic-02-rest-design).

### The classes

| Class | What it is |
|---|---|
| `MonthEndReport(DataSource)` | the finance report: `revenueByStation(YearMonth)`, `migrate(DataSource)`, and the ready `explainRevenue(YearMonth)` |
| `StationRevenue(stationId, stationName, rides, revenueCents)` | one line of the report; `revenueCents` is a `Long`, so a NULL shows as `null` |
| `CouncilExport(DataSource)` | the council's export: `page(YearMonth, cursor, pageSize)` |
| `ExportRow(id, riderId, stationId, startedAt, fareCents)` | one ride in the export |
| `ExportPage(rows, nextCursor)` | one page, and where the next one starts |

### Syntax: reading a plan in H2

`EXPLAIN` before a query asks the database how it will run it, without running it. H2 answers with the query, rewritten, and a comment under each table saying how it reads that table:

```sql
EXPLAIN SELECT ... FROM station s LEFT JOIN ride r ON r.station_id = s.id AND r.started_at >= ? AND r.started_at < ? ...
```

This is part of H2 2.4's real answer for the fixed report:

```text
LEFT OUTER JOIN "PUBLIC"."RIDE" "R"
    /* PUBLIC.RIDE_STATION_STARTED_IDX: STARTED_AT >= ?1
        AND STARTED_AT < ?2
        AND STATION_ID = S.ID
     */
```

The comment names the index, then the conditions it uses to find rows. For the broken report it says `/* PUBLIC.CONSTRAINT_INDEX_2: STATION_ID = S.ID */`: an index for the station only, with the month tested row by row. A comment ending in `.tableScan` means every row is read.

### Syntax: a keyset page

```sql
SELECT id, rider_id, station_id, started_at, fare_cents
FROM ride
WHERE started_at >= ? AND started_at < ?                 -- the month
  AND (started_at > ? OR (started_at = ? AND id > ?))    -- after the cursor
ORDER BY started_at, id
LIMIT ?
```

The first page leaves out the cursor line. Each later page fills it from the last row of the page before. The `OR` handles ties: rides that start in the same second as the cursor still come, if their id is bigger.

## The tiers

1. **Bronze** resolves the incident. Every station gets a line, with 0 rides and 0 cents when it had none. Totals count only the month's rides. Lines come biggest revenue first, ties by station id.
2. **Silver**: `migrate` creates `ride_station_started_idx` on `(station_id, started_at)`, and running it twice is safe. The month is a half-open range on the bare `started_at`, in the `ON`. H2's plan must read `ride` through that index, for the station and the month. The numbers do not change.
3. **Gold**: the export pages by a cursor on `(started_at, id)`. Rides that arrive or vanish mid-export never make another ride repeat or go missing. Seven rides in the same second all come out once.

The exact rules, including the cursor format, are in the README.

## Recap

- A filter on a LEFT JOIN's right table goes in `ON`. In `WHERE` it drops the NULL rows, and the join quietly becomes an inner join.
- After a LEFT JOIN, count a column of the right table, not `*`, and wrap a `SUM` in `COALESCE(..., 0)`.
- Always break ties in `ORDER BY` with a unique column. Without it, the order of equal rows is up to the database.
- An index helps only a bare column. Turn "month = 10" into a half-open range, and give the query a composite index: the `=` column first, then the range column.
- `OFFSET` pages shift when rows arrive or leave. A cursor on a unique sort key, such as `(started_at, id)`, never moves.

## Common mistakes

**1. Moving the filter into `ON` but keeping `COUNT(*)`.**

```sql
COUNT(*) AS rides,
SUM(r.fare_cents) AS revenue_cents
```

```text
Depot had no rides. COUNT(*) counts the NULL row a LEFT JOIN makes, so it says 1: count r.id instead ==> expected: <0> but was: <1>
```

Every station now has a line, so it looks fixed. But an empty month shows each station with `rides=1, revenueCents=null`. Fix: `COUNT(r.id)` and `COALESCE(SUM(r.fare_cents), 0)`.

**2. Adding the index but keeping `EXTRACT`.**

```sql
ON r.station_id = s.id
AND EXTRACT(YEAR FROM r.started_at) = ?
AND EXTRACT(MONTH FROM r.started_at) = ?
```

```text
H2 reads the ride table with: PUBLIC.CONSTRAINT_INDEX_2: STATION_ID = S.ID. It should say PUBLIC.RIDE_STATION_STARTED_IDX: with STARTED_AT >= ?1 AND STARTED_AT < ?2 AND STATION_ID = S.ID. Three things must all be true: ...
```

The new index is there, but only its first column helps, and the foreign key's index does that job as well. The month is still tested ride by ride. Fix: `r.started_at >= ? AND r.started_at < ?`.

**3. Using `BETWEEN` or `<=` for the month.**

```sql
AND r.started_at BETWEEN ? AND ?
```

```text
... BETWEEN would count the ride at 00:00 on 1 November too ==> expected: <StationRevenue[stationId=harbor, stationName=Harbor, rides=2, revenueCents=650]> but was: <StationRevenue[stationId=harbor, stationName=Harbor, rides=3, revenueCents=900]>
```

A ride at exactly midnight would count in two months, and finance's year would not add up. Fix: `>=` the first moment of the month and `<` the first moment of the next.

**4. Putting the range in `WHERE` instead of `ON`.** Silver's plan check fails with the same `CONSTRAINT_INDEX_2: STATION_ID = S.ID` message, and bronze still loses Depot and Uptown. A `WHERE` condition runs after the join, so H2 cannot use it to look up rides. Fix: the range goes in the `ON`, next to `r.station_id = s.id`.

**5. A cursor on `started_at` alone.**

```sql
AND started_at > ?
```

```text
all 7 rides start at 08:00:00, so the order is by id: 1 to 7, each once, but the export gave [1, 2, 3]. 'started_at > cursor' alone skips the rest of a tie: use started_at > t OR (started_at = t AND id > id) ==> expected: <[1, 2, 3, 4, 5, 6, 7]> but was: <[1, 2, 3]>
```

The first page ends on a ride at 08:00:00, and "after 08:00:00" skips the other four rides of that second. Fix: `started_at > t OR (started_at = t AND id > lastId)`, with `ORDER BY started_at, id`.

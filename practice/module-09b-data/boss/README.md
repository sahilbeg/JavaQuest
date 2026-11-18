# INC-12: Month-end reports grind the database

On 1 November, finance ran October's month-end report. Two stations were missing and the totals looked wrong. The report query held the database for minutes. Then the ride export for the city council came out with some rides twice and others not at all.

Fix the report code in package `jq.m12.incident`. Change `MonthEndReport` and `CouncilExport`. `StationRevenue`, `ExportRow`, `ExportPage` and `ReportException` are ready. Keep `explainRevenue` and `toRevenue` as they are.

## The tables

The checks build a fresh H2 database in memory for every check, with this schema and the five Riverton stations:

```sql
CREATE TABLE station (id VARCHAR(20) PRIMARY KEY, name VARCHAR(40) NOT NULL);
CREATE TABLE ride (
  id         BIGINT PRIMARY KEY,
  rider_id   VARCHAR(20) NOT NULL,
  station_id VARCHAR(20) NOT NULL REFERENCES station(id),   -- where the ride started
  started_at TIMESTAMP   NOT NULL,                          -- Riverton local time
  minutes    INT         NOT NULL,
  fare_cents BIGINT      NOT NULL);
CREATE INDEX ride_rider_idx ON ride(rider_id);
```

Station ids are `riverside`, `old-mill`, `harbor`, `uptown` and `depot`. A month runs from 00:00 on its first day up to, but not including, 00:00 on the first day of the next month.

## Bronze: every station, with the right totals (`MonthEndReport`, TODO 1)

`revenueByStation(YearMonth month)` returns one `StationRevenue(stationId, stationName, rides, revenueCents)` **for every station**, in this order: `revenueCents` from biggest to smallest, then `stationId` A to Z for ties.

- A station with no rides in the month still gets a line, with `rides` 0 and `revenueCents` 0.
- `rides` counts only that month's rides at that station. `revenueCents` adds up their `fare_cents`.
- `revenueCents` is a `Long`, so a NULL from SQL shows up as `null`. The checks want `0`, never `null`.

The broken SQL has three bugs. The month filter on `ride` sits in `WHERE`, `COUNT(*)` counts a row that is not a ride, and `SUM` over no rows is NULL. The order also has no tie-break.

## Silver: the report must use an index (`MonthEndReport`, TODOs 2 and 3)

- `MonthEndReport.migrate(DataSource)` creates the index **`ride_station_started_idx` on `ride(station_id, started_at)`**, in that column order. Running `migrate` twice must not fail.
- `REVENUE_SQL` compares the bare `started_at` column with a half-open range: `r.started_at >= ?` (the first moment of the month) `AND r.started_at < ?` (the first moment of the next month). Never `EXTRACT(... FROM started_at)`, and never `BETWEEN`. `bind` passes the two moments as `LocalDateTime` values, in that order.
- The check runs `explainRevenue(month)` and reads H2's comment on how it reads `ride`. It must start with `PUBLIC.RIDE_STATION_STARTED_IDX:` and name both `STARTED_AT >=` and `STARTED_AT <`, as well as `STATION_ID =`. The working plan says `/* PUBLIC.RIDE_STATION_STARTED_IDX: STARTED_AT >= ?1 AND STARTED_AT < ?2 AND STATION_ID = S.ID */`.
- H2 uses a condition to look rows up in the joined table only when it sits in the LEFT JOIN's `ON`. So silver needs the bronze fix too.
- The results do not change: a ride at exactly 00:00 on 1 October counts for October; a ride at exactly 00:00 on 1 November does not.

The `REFERENCES station(id)` gives H2 an index on `station_id` alone, named `CONSTRAINT_INDEX_...`. It finds a station's rides from every month, so it is not enough.

## Gold: an export that survives new rides (`CouncilExport`, TODO 4)

`page(YearMonth month, String cursor, int pageSize)` returns one `ExportPage(rows, nextCursor)` of the month's rides.

- Rows are ordered by `started_at`, then by `id`. Only rides in the month count (the same half-open range).
- `cursor` is `null` for the first page. Otherwise it is a cursor from an earlier page, and the page holds only rides **after** it in that order: `started_at > t`, or `started_at = t` and `id > id`.
- **Cursor format:** the last row's `startedAt` as `LocalDateTime.toString()`, a `|`, then its `id`. For example `2026-10-01T17:00|12`. Read it back with `LocalDateTime.parse` and `Long.parseLong`.
- `nextCursor` is the cursor of the page's last row when the page is full (`pageSize` rows). It is `null` when the page has fewer than `pageSize` rows. A full last page is followed by one empty page with `null`.
- Do not use `OFFSET`. Rides keep arriving while the export runs: a ride is saved when it **ends**, so a long ride that started at 06:30 arrives after the 08:00 rides. Rides can also be deleted mid-export.
- The promise: every ride that was in the table when the export began, and is not deleted, comes out **exactly once**. No ride comes out twice.

The checks insert and delete rides between page fetches, and put seven rides in the same second across a page boundary.

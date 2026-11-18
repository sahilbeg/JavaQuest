# Advanced SQL: Jiraiya's ride reports

Jiraiya wants reports he can trust. Fill in the six constants in `RideAnalytics.java`. Each one is a single SQL query, written as a Java string.

The methods under the constants are given. Each one runs your query with JDBC, fills in its `?` marks, and turns every row into a small record. You only write SQL.

Every constant starts as `"SELECT 1 WHERE FALSE"`. That is valid SQL that returns no rows and one column, so each check fails with a message until you replace it.

## The tables

The checks build a fresh in-memory H2 database for every test. The full sample data is at the top of `test/jq/m12/sql/SpokeData.java`: 8 riders, 5 bikes, 25 rides and 7 lanes.

```sql
rider(id, name)                      -- id like 'rider-1'
bike(id, type)                       -- type is 'CLASSIC', 'EBIKE' or 'CARGO'
ride(id, rider_id, bike_id, start_station_id, started_at, minutes, fare_cents)
station_link(from_id, to_id)         -- a one-way bike lane between two stations
```

- `ride.rider_id` is **NULL for a guest ride**, paid at the kiosk without an account. The sample data has three guest rides, and one of them is on an EBIKE.
- `started_at` is a `TIMESTAMP`. `fare_cents` is a `BIGINT`.
- A two-way lane is two rows, one per direction. The lanes form loops, so a station can be reached in more than one way.

## What to write

The checks compare the columns, their order, and the order of the rows. The given methods check the number of columns and of `?` marks, and say so when one is off.

### TODO 1: `TOP_RIDERS_PER_STATION_MONTH`

The top `n` riders at each start station, in each month.

- **Columns:** `station_id` (the ride's `start_station_id`), `month`, `rider_id`, `rides`, `place`.
- `month` is the month as text, such as `2026-10`. In H2, write `FORMATDATETIME(started_at, 'yyyy-MM')`. Name the column `ride_month` or similar, because `MONTH` is a reserved word in H2.
- `rides` is how many rides that rider started at that station in that month.
- **Leave out guest rides** (`rider_id IS NULL`).
- `place` is `RANK()` by `rides`, most first, restarting for every station and month. **Tied riders share a place, and the next place is skipped:** 4, 4, 3 rides give places 1, 1, 3.
- **One `?` mark, for `n`.** Keep every row whose place is at most `n`. So a tie at the cut-off keeps every tied rider, and a station can list more than `n` riders.
- **Sort** by `station_id`, then `month`, then `place`, then `rider_id`.

### TODO 2: `DAYS_BETWEEN_RIDES`

How many days pass between each rider's rides.

- **Columns:** `rider_id`, the ride `id`, `gap_days`.
- One row per ride that has a rider. **Leave out guest rides.**
- `gap_days` is `DATEDIFF('DAY', previous started_at, this started_at)`, where "previous" is the same rider's ride just before this one, by `started_at`. `DATEDIFF('DAY', ...)` counts the midnights in between, so two rides on the same date give 0.
- A rider's first ride has no previous ride, so its `gap_days` is **NULL**.
- **Sort** by `rider_id`, then `started_at`, then `id`. No `?` marks.

### TODO 3: `DAILY_REVENUE`

Revenue day by day, with running totals.

- **Columns:** `ride_day`, `day_cents`, `running_cents`, `last_three_cents`.
- `ride_day` is the ride's date, `CAST(started_at AS DATE)`. Name it `ride_day`, because `DAY` is a reserved word in H2.
- One row per day that had at least one ride. Days with no rides have no row.
- **Guest rides count.** Revenue is every fare, with or without an account.
- `day_cents` is the total `fare_cents` that day.
- `running_cents` is the total of every day so far, this day included.
- `last_three_cents` is the total of this day and the **two ride-days before it** (rows, not calendar days). The first two rows add up fewer days.
- **Sort** by `ride_day`. No `?` marks.

H2 cannot match `GROUP BY CAST(started_at AS DATE)` with the same expression inside `OVER (ORDER BY ...)`. Work out `ride_day` in a CTE first, then group by that column.

### TODO 4: `RIDERS_WITHOUT_EBIKE`

Every rider who has never ridden an EBIKE, including riders with no rides at all.

- **Columns:** `id`, `name`, from the `rider` table.
- **Sort** by `id`. No `?` marks.
- It must be **NULL-safe**. A guest rode an EBIKE, so a list of "e-bike riders" holds a NULL. `id NOT IN (that list)` is then never true, and the answer is empty.

### TODO 5: `RIDER_TIERS`

Every rider, with how many rides they took and a tier.

- **Columns:** the rider `id`, `rides`, `tier`.
- Every rider appears, **even with no rides**. A rider with no rides has `rides` 0.
- `tier` is `'NEW'` for 0 rides, `'REGULAR'` for 1 to 4 rides, and `'COMMUTER'` for 5 or more.
- **Sort** by rider `id`. No `?` marks.

### TODO 6: `REACHABLE_STATIONS`

Every station a rider can reach from a start station in 1 to `maxHops` hops, following `station_link` from `from_id` to `to_id`.

- **Columns:** `station_id`, `hops`.
- `hops` is the **fewest** hops needed. Each station appears **once**.
- **Never list the start station**, even when a loop leads back to it.
- **Sort** by `hops`, then `station_id`.
- **Three `?` marks, in this order:** 1 = the start station, 2 = `maxHops`, 3 = the start station again (to leave it out).
- Use `WITH RECURSIVE`. Stop growing a path once it has `maxHops` hops, or the loops never end.
- A start station with no lanes gives no rows. The method rejects `maxHops` below 1 before your SQL runs.

## How to check your SQL

When a check fails, it shows the rows it expected and the rows your query returned, as records such as `TopRider[stationId=harbor, month=2026-10, ...]`. If your SQL has a typo, the message quotes H2's own error.

Each query may run for at most 3 seconds. A recursive query with no stop condition is cancelled with `Statement was canceled or the session timed out`.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

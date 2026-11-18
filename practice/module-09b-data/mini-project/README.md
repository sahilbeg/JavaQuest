# FEAT-12: The rider statements data mart

Riders get a statement each month: how many rides, how much they paid, their longest ride, where they rank in their city, and how this month compares with last. Jiraiya wants the statements built every night from the raw rides, and right even when the job runs twice.

Build the nightly job in package `jq.m12.feature`. Change `MonthRange`, `StatementJob` and `QualityChecks`. `StatementJobException` and `DataQualityException` are ready.

## The tables

The checks build a fresh H2 database in memory for every check, with these two tables:

```sql
CREATE TABLE ride (                       -- the raw rides, one row per ride, never changed by the job
  id         BIGINT PRIMARY KEY,
  rider_id   VARCHAR(20) NOT NULL,        -- 'rider-7'
  city       VARCHAR(20) NOT NULL,        -- 'riverton' or 'bramford'
  bike_type  VARCHAR(10) NOT NULL,        -- 'CLASSIC', 'EBIKE' or 'CARGO'
  started_at TIMESTAMP WITH TIME ZONE NOT NULL,   -- an instant, saved in UTC
  minutes    INT NOT NULL,
  fare_cents BIGINT NOT NULL);

CREATE TABLE rider_monthly_statement (    -- the data mart the job builds
  statement_month VARCHAR(7)  NOT NULL,   -- '2026-10', from YearMonth.toString()
  city            VARCHAR(20) NOT NULL,
  rider_id        VARCHAR(20) NOT NULL,
  ride_count      INT    NOT NULL,
  total_cents     BIGINT NOT NULL,
  longest_minutes INT    NOT NULL,
  city_rank       INT    NOT NULL,
  change_cents    BIGINT,                 -- NULL when there is nothing to compare with
  PRIMARY KEY (statement_month, city, rider_id));
```

## Stage 1: months in Riverton time, and the totals (`MonthRange`, `StatementJob`, TODOs 1 and 2)

- A statement month runs from **midnight on its first day in Riverton** up to, but not including, midnight on the first day of the next month in Riverton. Riverton's zone is `MonthRange.RIVERTON`, `America/New_York`. Every city's statements use it.
- `MonthRange.of(YearMonth)` returns that range as two `OffsetDateTime` values in UTC: `start` (included) and `end` (excluded). October 2026 is `2026-10-01T04:00Z` to `2026-11-01T04:00Z`. November 2026 is `2026-11-01T04:00Z` to `2026-12-01T05:00Z`, because the clocks go back on 1 November.
- A ride belongs to the month when `started_at >= start AND started_at < end`. Bind `start` and `end` with `setObject`. H2 compares `TIMESTAMP WITH TIME ZONE` values as instants.
- `runMonth(YearMonth)` writes **one row per city and rider** who has at least one ride in the month. A rider who rode in both cities gets two rows.
  - `statement_month`: the month as `YearMonth.toString()`, such as `2026-10`.
  - `ride_count`: their rides in that city that month. `total_cents`: the sum of those rides' `fare_cents`. `longest_minutes`: the largest `minutes`.

## Stage 2: rank and change (`StatementJob`, TODO 3)

- `city_rank`: the rider's place in their city that month, by `total_cents`, biggest first. **Ties share a rank, and the next rank skips**, as `RANK()` does: totals 1000, 700, 700, 300 rank 1, 2, 2, 4. Each city ranks on its own.
- `change_cents`: this month's `total_cents` minus the same rider's total **in the same city in the calendar month before**, read from the raw rides.
  - It is `NULL` when the rider had no rides in that city in the month before. That holds for a rider's first month, and for a rider who skipped last month: October never compares with August.
  - It can be 0 or negative.
  - It must be right even when last month's statements were never built.

## Stage 3: safe to rerun (`StatementJob`, TODO 4)

`runMonth` does all of this in **one transaction** on one connection:

1. Turn off auto-commit.
2. Delete every row of that `statement_month`. Other months are never touched.
3. Insert the month's rows.
4. Run `QualityChecks.verify(connection, range)`.
5. Commit.

On any failure, roll back, so the month keeps the rows it had before the run. Then throw. A `StatementJobException` (a `DataQualityException` is one) is rethrown as it is. Anything else, such as an `SQLException`, is wrapped in a new `StatementJobException`.

Running a month twice gives identical rows. A rerun after the raw rides changed shows the new data, and a rider with no rides left loses their row.

## Stage 4: data-quality checks (`QualityChecks`, TODO 5)

`QualityChecks.verify(Connection, MonthRange)` runs three checks on the given connection, in this order. At the first failure it throws `new DataQualityException(name, detail)`, whose message names the check. `check()` returns the name.

| Order | Name (constant) | It fails when |
|---|---|---|
| 1 | `no-negative-fares` (`NO_NEGATIVE_FARES`) | a ride in the month's range has `fare_cents < 0` |
| 2 | `totals-match` (`TOTALS_MATCH`) | the sum of `total_cents` over the month's statement rows differs from the sum of `fare_cents` over the month's rides (an empty sum counts as 0) |
| 3 | `every-rider-has-a-row` (`EVERY_RIDER_HAS_A_ROW`) | some city and rider with a ride in the range has no statement row for the month |

A correct month passes all three without throwing. The checks also call `verify` straight on a table they filled by hand.

## Stage 5: backfill (`StatementJob`, TODO 6)

`backfill(YearMonth from, YearMonth to)` runs `runMonth` for every month from `from` to `to`, **both included, oldest first**, each in its own transaction. It returns the months in the order it ran them.

- `from` after `to` throws `IllegalArgumentException`.
- A month that fails stops the backfill: its exception is thrown, the months before it stay built, and later months are not run.
- Running the same backfill twice leaves the table exactly the same.

## Fares in the checks

Riverton: 100 cents to unlock, then a minute CLASSIC 15, EBIKE 30, CARGO 25. Bramford: 120 to unlock, then CLASSIC 15, EBIKE 35. The job never computes fares: it adds up `fare_cents` as saved.

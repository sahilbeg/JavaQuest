# SQL basics

Theo wants saved queries he can run himself. Fill in the seven constants in `RideQueries.java`. Each one is a single SQL statement, written as a Java string.

Every constant starts as `"SELECT 1 WHERE FALSE"`. That is valid SQL that returns no rows, so the checks fail with a message until you replace it.

## The tables

The checks build a fresh in-memory H2 database for every test, with these three tables. The full sample data is at the top of `test/jq/m08/sql/SpokeDatabase.java`.

```sql
station(id, name, docks)                        -- id is a slug like 'harbor', name is 'Harbor'
bike(id, type, status, station_id)              -- station_id is NULL while the bike is out on a ride
ride(id, bike_id, rider_id, start_station_id, end_station_id, started_at, minutes, fare_cents)
```

`type` is `'CLASSIC'`, `'EBIKE'` or `'CARGO'`. `status` is `'AVAILABLE'`, `'RENTED'` or `'MAINTENANCE'`.

## What to write

The checks compare the columns you select and the order of the rows, so follow each line exactly.

1. **TODO 1, `FREE_BIKES_AT_HARBOR`:** one column, the bike `id`, for bikes with status `'AVAILABLE'` and station_id `'harbor'`. Sort by id.
2. **TODO 2, `EBIKES_COUNT`:** one row with one number, how many bikes have type `'EBIKE'`, whatever their status.
3. **TODO 3, `RIDES_PER_START_STATION`:** two columns, the station `name` and how many rides started there. Most rides first. When two stations tie, sort them by name. Only list stations where at least one ride started (an inner join, like the worked example).
4. **TODO 4, `LONGEST_THREE_RIDES`:** two columns, the ride `id` and its `minutes`, for the three longest rides. Longest first.
5. **TODO 5, `STATIONS_WITH_NO_RIDES`:** one column, the station `name`, for every station where no ride started. Sort by name. Use a `LEFT JOIN`.
6. **TODO 6, `RIDERS_OVER_1000_CENTS`:** two columns, `rider_id` and the total of their `fare_cents`, only for riders whose total is more than 1000. Biggest total first.
7. **TODO 7, `MARK_BIKE_MAINTENANCE`:** an `UPDATE` that sets status to `'MAINTENANCE'` for one bike. Put a `?` where the bike id goes. The checks fill it in with a `PreparedStatement`, which the next topic explains.

## How to check your SQL

When a check fails, it shows what it expected and what your query returned. Each row is shown as its columns joined by a comma, for example `Harbor, 5`. If your SQL has a typo, the message quotes H2's own error.

You can try a query first in the lesson's runnable examples, then paste it into the constant.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

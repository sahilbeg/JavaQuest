# Database design and performance

Two jobs. First, write Spoke's schema so the database itself refuses bad rows. Second, build `SchemaMigrator`, a tiny version of Flyway that applies each schema change exactly once.

## What you get

- `Migration(int version, String description, String sql)`: one versioned change, like Flyway's `V2__rentals.sql`.
- `MigrationException`: an unchecked exception for migrations that cannot run safely.
- `SchemaMigrator.checksum(sql)`: a fingerprint of the SQL text. Finished for you.
- `SchemaMigrator.createHistoryTable`: finished for you. It creates this table if it is missing:

```sql
CREATE TABLE IF NOT EXISTS schema_history (
    version     INT PRIMARY KEY,
    description VARCHAR(200) NOT NULL,
    checksum    BIGINT NOT NULL
)
```

- `SchemaMigrator.migrate`: finished for you. It sorts the list, reads the history, checks that nothing changed, then applies each version that is not recorded yet and counts them. It calls the four methods you write.

## Part 1: the Spoke schema (`SpokeSchema.java`)

Keep every table name, column name, column type and primary key as they are. Add the constraints.

1. **TODO 1, `station`:** `name` is `NOT NULL` and `UNIQUE`. `docks` is `NOT NULL` with `CHECK (docks > 0)`.
2. **TODO 2, `bike`:** `type` is `NOT NULL` and one of `'CLASSIC'`, `'EBIKE'`, `'CARGO'`. `status` is `NOT NULL` and one of `'AVAILABLE'`, `'RENTED'`, `'MAINTENANCE'`. Use `CHECK (type IN (...))`. `station_id` stays nullable, because a rented bike is at no station, and `REFERENCES station(id)`.
3. **TODO 3, `rental`:** `rider_id`, `bike_id` and `started_at` are `NOT NULL`. `bike_id` `REFERENCES bike(id)`. `minutes` and `fare_cents` may be NULL while a ride is still going, but `CHECK` that they are `>= 0`.
4. **TODO 4:** add `new Migration(3, "index for a rider's ride history", ...)` that creates an index named exactly `idx_rental_rider_started` on `rental`. The rider app runs `SELECT id, started_at FROM rental WHERE rider_id = ? ORDER BY started_at`. Choose the column order so the database can jump straight to one rider.

The checks insert good rows, which must still work, and bad rows, which must throw `SQLException`. A station that still docks bikes must not be deletable. The last check runs `EXPLAIN` on the history query and expects the plan to look up `RIDER_ID` through `IDX_RENTAL_RIDER_STARTED`.

## Part 2: the migrator (`SchemaMigrator.java`)

5. **TODO 5, `inVersionOrder`:** if two migrations share a version, throw `IllegalArgumentException` before anything runs. Otherwise return a **new** list sorted by version, lowest first. Do not change the list you were given (it may be immutable).
6. **TODO 6, `appliedChecksums`:** read `version` and `checksum` from `schema_history` into a `Map<Integer, Long>`.
7. **TODO 7, `checkNothingChanged`:** for every version in the history, find the migration with that version. If it is missing from the list, or `checksum(migration.sql())` is not the stored checksum, throw a `MigrationException` whose message starts with `V` and the version, such as `V1 was changed after it was applied`.
8. **TODO 8, `apply`:** in one transaction, run `migration.sql()` with a `Statement`, then insert `(version, description, checksum(sql))` into `schema_history` with a `PreparedStatement` and `?` parameters. Descriptions can hold an apostrophe, such as `index for a rider's ride history`, so never glue them into the SQL with `+`. The lesson card "Running a migration in one transaction" shows every call you need. Turn auto-commit off first and commit at the end. On an `SQLException`, roll back and throw a `MigrationException` whose message starts with `V` and the version, with the `SQLException` as its cause. Always put auto-commit back to what it was.

Rules the checks test:

- An empty database gets every migration, and `migrate` returns how many it applied.
- A second run applies nothing and returns 0.
- Migrations given out of order still run lowest version first.
- A migration added later is the only one that runs.
- An edited, already-applied migration is refused before anything new runs.
- A failing migration is not recorded, the ones after it do not run, and the fixed version runs next time.

## How the checks work

Every check gets its own fresh in-memory H2 database. Nothing touches the network or your disk.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

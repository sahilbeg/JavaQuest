# Concurrent writes

Spoke now runs two servers behind the load balancer. Both can handle a request for the same bike, rider or fare at the same moment. Java locks cannot help, because the two servers are two separate JVMs. Make the database settle every race.

Every method receives a `Connection`. The caller owns the transaction, so `BikeDesk`, `FareEditor` and `RentalLedger` never call `commit`, `rollback` or `setAutoCommit`. Use `PreparedStatement` with `?` parameters, and close every statement with try-with-resources.

## 1. `BikeDesk.rentIfAvailable(connection, bikeId, riderId)`

Table: `bike (id VARCHAR(10) PRIMARY KEY, status VARCHAR(12) NOT NULL, rider_id VARCHAR(20))`.

- **TODO 1:** rent the bike with **one** conditional `UPDATE`: set `status = 'RENTED'` and `rider_id` to the rider, but only `WHERE id = ? AND status = 'AVAILABLE'`. Return `true` when exactly one row changed.
- A bike that is `RENTED`, in `MAINTENANCE`, or does not exist gives `false`, and nothing changes.
- Do not `SELECT` the status first and then update. One check lets the other server rent the bike between your read and your write, and then both servers answer `true`.

## 2. `FareEditor` (optimistic locking)

Table: `fare (bike_type VARCHAR(10) PRIMARY KEY, cents_per_minute BIGINT NOT NULL, version BIGINT NOT NULL)`.

- **TODO 2, `read(connection, bikeType)`:** return `Optional.of(new Fare(bikeType, centsPerMinute, version))`, or `Optional.empty()` for an unknown type.
- **TODO 3, `updateWithVersion(connection, bikeType, newCentsPerMinute, expectedVersion)`:** set the new price and `version = version + 1`, but only `WHERE bike_type = ? AND version = ?`. Return `true` when exactly one row changed. Return `false` when the row's version moved on, because someone saved first, or when the type does not exist.

## 3. `RentalLedger` (a unique constraint as a lock)

`active_rider` holds the rider's id while the ride is on, and `NULL` after it ends. A `UNIQUE` column allows many `NULL`s, but each rider id only once. So each rider has at most one active rental.

- **TODO 4:** in `SCHEMA`, add `CONSTRAINT one_active_rental_per_rider UNIQUE (active_rider)`. Keep the rest of the table as it is.
- **TODO 5, `startRental(connection, rentalId, riderId, bikeId)`:** insert the row with `active_rider = riderId` and return `true`. When the database refuses the row as a duplicate, return `false`: catch `SQLException` and check `"23505".equals(e.getSQLState())`, which works with every driver. Throw any other `SQLException` on. (H2 also throws the subclass `SQLIntegrityConstraintViolationException`, so catching that passes the checks too, but PostgreSQL's driver does not use it.) Do not `SELECT` first to check: the other server can insert between your check and your insert.
- **TODO 6, `endRental(connection, rentalId)`:** set `active_rider = NULL` `WHERE id = ? AND active_rider IS NOT NULL`. Return `true` when one row changed, and `false` for an unknown or already-ended rental.

## 4. `TransactionRetry.inTransaction(connection, maxAttempts, work)`

`SqlWork<T>` is one piece of database work: `T run(Connection connection) throws SQLException`. `isRetryable(e)` is given. It is `true` for SQLState `40001`, a serialization failure, which H2 also uses for deadlocks, and `40P01`, PostgreSQL's deadlock code.

- **TODO 7:** throw `IllegalArgumentException` when `maxAttempts` is below 1. Remember `getAutoCommit()`, then call `setAutoCommit(false)`.
- **TODO 8:** run the work, `commit()`, and return its result.
  - On an `SQLException`, `rollback()`. If `isRetryable(e)` and attempts are left, run the **whole** work again from the start. Otherwise throw `e` itself.
  - On a `RuntimeException`, `rollback()` and throw it.
  - The work runs at most `maxAttempts` times in all.
- **TODO 9:** in a `finally` block, put auto-commit back to what it was, after success and after failure.

## How the checks work

Each check opens one fresh in-memory H2 database with two connections, "server A" and "server B". There are no threads and no timing. The checks decide exactly which server acts when.

Two checks wrap your connection so the other server acts **just before your first `INSERT` or `UPDATE`**. That is exactly the gap a check-then-act bug falls into. If your code waits for a row lock that the other server holds, the wait gives up after 0.2 seconds.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

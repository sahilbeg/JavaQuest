# JDBC

Build `JdbcRideRepository` so the rentals service can save rides and read them back with plain JDBC. The rider search must also be safe: the text `r-1' OR '1'='1` must find nobody.

## What you get

- `Ride`: a saved ride, with the id the database picked.
- `NewRide`: a ride that is not saved yet, so it has no id.
- `RideRepositoryException`: an unchecked exception that keeps the original `SQLException` as its cause.
- `JdbcRideRepository.createSchema`: finished for you. It creates this table, and it shows the try-with-resources and catch shape to copy.

```sql
CREATE TABLE ride (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rider_id   VARCHAR(20) NOT NULL,
    bike_id    VARCHAR(10) NOT NULL,
    minutes    INT NOT NULL,
    fare_cents BIGINT NOT NULL
)
```

## What to write

Three rules hold for every method:

- Get the connection from `dataSource.getConnection()` inside a try-with-resources, so it is always closed.
- Put every value in a `?` parameter and fill it with a setter. Never glue a value into the SQL with `+`.
- Catch `SQLException` and throw `new RideRepositoryException("what you were doing", e)`.

1. **TODO 1:** in `save`, insert the four fields with a `PreparedStatement`, passing `Statement.RETURN_GENERATED_KEYS` to `prepareStatement`.
2. **TODO 2:** read the new id from `getGeneratedKeys()`, and return a `Ride` with that id and the same fields.
3. **TODO 3:** `findById` returns the ride with that id in an `Optional`, or `Optional.empty()` when there is no such row.
4. **TODO 4:** `findByRider` returns every ride of that rider, sorted by id. A rider with no rides gets an empty list.
5. **TODO 5:** `totalFareCents` returns the sum of that rider's `fare_cents`, or 0 when they have no rides.
6. **TODO 6:** `deleteById` deletes the ride and returns `true`, or returns `false` when no row had that id.
7. **TODO 7:** `toRide` turns the current row of a `ResultSet` into a `Ride`. Write it before TODO 3, because TODO 3 and TODO 4 use it.

## How the checks work

Each test gets a fresh in-memory H2 database. The repository receives a `DataSource` that counts the connections you open and close. A forgotten `close` shows up as a failed check.

Other checks save rides and read them back, and try the injection text on `findByRider` and `totalFareCents`. The last check hands the repository a database that is switched off, and expects your `RideRepositoryException`.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.

package jq.m08.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/** Reads and writes rides with plain JDBC. Every method borrows a connection from the DataSource and closes it. */
public class JdbcRideRepository {

    private final DataSource dataSource;

    public JdbcRideRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Creates the ride table. Given to you: copy its try-with-resources and catch shape in your own methods. */
    public static void createSchema(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE ride (
                        id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        rider_id   VARCHAR(20) NOT NULL,
                        bike_id    VARCHAR(10) NOT NULL,
                        minutes    INT NOT NULL,
                        fare_cents BIGINT NOT NULL
                    )""");
        } catch (SQLException e) {
            throw new RideRepositoryException("Could not create the ride table", e);
        }
    }

    // In every method below: use try-with-resources, use ? parameters (never + to glue values into SQL),
    // and catch SQLException and throw new RideRepositoryException("what you were doing", e).

    public Ride save(NewRide ride) {
        // TODO 1: INSERT INTO ride (rider_id, bike_id, minutes, fare_cents) VALUES (?, ?, ?, ?), asking for Statement.RETURN_GENERATED_KEYS
        // TODO 2: read the new id from getGeneratedKeys() and return a Ride with that id and the same fields
        return new Ride(0, ride.riderId(), ride.bikeId(), ride.minutes(), ride.fareCents());
    }

    public Optional<Ride> findById(long id) {
        // TODO 3: SELECT the ride with this id; return it with Optional.of(toRide(rows)), or Optional.empty() when there is no row
        return Optional.empty();
    }

    public List<Ride> findByRider(String riderId) {
        // TODO 4: SELECT every ride whose rider_id is the ? parameter, ORDER BY id, and add toRide(rows) for each row to a list
        return List.of();
    }

    public long totalFareCents(String riderId) {
        // TODO 5: SELECT SUM(fare_cents) for this rider; getLong gives 0 when the sum is NULL because the rider has no rides
        return 0;
    }

    public boolean deleteById(long id) {
        // TODO 6: DELETE the ride with this id and return true when executeUpdate says one row changed
        return false;
    }

    private static Ride toRide(ResultSet rows) throws SQLException {
        // TODO 7: build a Ride from the current row with getLong("id"), getString("rider_id"), getString("bike_id"), getInt("minutes") and getLong("fare_cents")
        return new Ride(0, "", "", 0, 0);
    }
}

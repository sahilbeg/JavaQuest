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
        String sql = "INSERT INTO ride (rider_id, bike_id, minutes, fare_cents) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, ride.riderId());
            statement.setString(2, ride.bikeId());
            statement.setInt(3, ride.minutes());
            statement.setLong(4, ride.fareCents());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return new Ride(keys.getLong(1), ride.riderId(), ride.bikeId(), ride.minutes(), ride.fareCents());
            }
        } catch (SQLException e) {
            throw new RideRepositoryException("Could not save a ride for rider " + ride.riderId(), e);
        }
    }

    public Optional<Ride> findById(long id) {
        String sql = "SELECT id, rider_id, bike_id, minutes, fare_cents FROM ride WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(toRide(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RideRepositoryException("Could not find ride " + id, e);
        }
    }

    public List<Ride> findByRider(String riderId) {
        String sql = "SELECT id, rider_id, bike_id, minutes, fare_cents FROM ride WHERE rider_id = ? ORDER BY id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, riderId);
            try (ResultSet rows = statement.executeQuery()) {
                List<Ride> rides = new ArrayList<>();
                while (rows.next()) {
                    rides.add(toRide(rows));
                }
                return rides;
            }
        } catch (SQLException e) {
            throw new RideRepositoryException("Could not find rides for rider " + riderId, e);
        }
    }

    public long totalFareCents(String riderId) {
        String sql = "SELECT SUM(fare_cents) FROM ride WHERE rider_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, riderId);
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getLong(1);
            }
        } catch (SQLException e) {
            throw new RideRepositoryException("Could not total the fares for rider " + riderId, e);
        }
    }

    public boolean deleteById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM ride WHERE id = ?")) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RideRepositoryException("Could not delete ride " + id, e);
        }
    }

    private static Ride toRide(ResultSet rows) throws SQLException {
        return new Ride(
                rows.getLong("id"),
                rows.getString("rider_id"),
                rows.getString("bike_id"),
                rows.getInt("minutes"),
                rows.getLong("fare_cents"));
    }
}

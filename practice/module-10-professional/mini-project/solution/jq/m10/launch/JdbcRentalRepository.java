package jq.m10.launch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Adapter (driven side): stores rentals in the rentals table with plain JDBC.
 * The schema is created in code, because this project has no resource files. In production it would be
 * a versioned migration (Flyway or Liquibase), run once per release.
 */
public class JdbcRentalRepository implements RentalRepository {

    /**
     * active_bike_id holds bike_id while the ride is on, and NULL once it ends. In H2 a UNIQUE column allows many NULLs,
     * so the database itself refuses a second ACTIVE rental of one bike, even from another server.
     */
    static final List<String> SCHEMA = List.of(
            "CREATE SEQUENCE IF NOT EXISTS rental_ids START WITH 1",
            """
            CREATE TABLE IF NOT EXISTS rentals (
                id               VARCHAR(40) PRIMARY KEY,
                city_id          VARCHAR(40) NOT NULL,
                rider_id         VARCHAR(40) NOT NULL,
                bike_id          VARCHAR(20) NOT NULL,
                bike_type        VARCHAR(10) NOT NULL,
                start_station_id VARCHAR(40) NOT NULL,
                started_at       TIMESTAMP WITH TIME ZONE NOT NULL,
                end_station_id   VARCHAR(40),
                ended_at         TIMESTAMP WITH TIME ZONE,
                fare_cents       BIGINT NOT NULL,
                idempotency_key  VARCHAR(64),
                active_bike_id   VARCHAR(20),
                CONSTRAINT one_active_rental_per_bike UNIQUE (active_bike_id),
                CONSTRAINT one_rental_per_rider_key UNIQUE (rider_id, idempotency_key)
            )""");

    private static final String COLUMNS = "id, city_id, rider_id, bike_id, bike_type, start_station_id, started_at, "
            + "end_station_id, ended_at, fare_cents, idempotency_key, active_bike_id";

    private final JdbcTemplate jdbc;

    public JdbcRentalRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        SCHEMA.forEach(jdbc::execute);
    }

    @Override
    public String nextId() {
        return "rental-" + jdbc.queryForObject("SELECT NEXT VALUE FOR rental_ids", Long.class);
    }

    @Override
    public void save(Rental r) {
        String activeBikeId = r.isActive() ? r.bikeId() : null;
        int updated = jdbc.update(
                "UPDATE rentals SET end_station_id = ?, ended_at = ?, fare_cents = ?, active_bike_id = ? WHERE id = ?",
                r.endStationId(), toTimestamp(r.endedAt()), r.fareCents(), activeBikeId, r.id());
        if (updated == 0) {
            jdbc.update("INSERT INTO rentals (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    r.id(), r.cityId(), r.riderId(), r.bikeId(), r.bikeType().name(), r.startStationId(),
                    toTimestamp(r.startedAt()), r.endStationId(), toTimestamp(r.endedAt()), r.fareCents(),
                    r.idempotencyKey(), activeBikeId);
        }
    }

    @Override
    public Optional<Rental> findById(String rentalId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM rentals WHERE id = ?", JdbcRentalRepository::toRental, rentalId)
                .stream().findFirst();
    }

    @Override
    public Optional<Rental> findActiveByRider(String riderId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM rentals WHERE rider_id = ? AND ended_at IS NULL",
                        JdbcRentalRepository::toRental, riderId)
                .stream().findFirst();
    }

    @Override
    public Optional<Rental> findByIdempotencyKey(String riderId, String idempotencyKey) {
        return jdbc.query("SELECT " + COLUMNS + " FROM rentals WHERE rider_id = ? AND idempotency_key = ?",
                        JdbcRentalRepository::toRental, riderId, idempotencyKey)
                .stream().findFirst();
    }

    /** One row of the rentals table, as a Rental. */
    static Rental toRental(ResultSet rs, int rowNumber) throws SQLException {
        return new Rental(rs.getString("id"), rs.getString("city_id"), rs.getString("rider_id"),
                rs.getString("bike_id"), BikeType.valueOf(rs.getString("bike_type")),
                rs.getString("start_station_id"), toInstant(rs, "started_at"), rs.getString("end_station_id"),
                toInstant(rs, "ended_at"), rs.getLong("fare_cents"), rs.getString("idempotency_key"));
    }

    /** An Instant as a TIMESTAMP WITH TIME ZONE value in UTC, or null. */
    static OffsetDateTime toTimestamp(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    static Instant toInstant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }
}

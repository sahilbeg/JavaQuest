package jq.m08.databasedesign;

import java.util.List;

/** Spoke's database schema, written as an ordered list of migrations. */
public final class SpokeSchema {

    private SpokeSchema() {}

    public static List<Migration> migrations() {
        return List.of(
                new Migration(1, "stations and bikes", """
                        CREATE TABLE station (
                            id    VARCHAR(20) PRIMARY KEY,
                            name  VARCHAR(40) NOT NULL UNIQUE,
                            docks INT NOT NULL CHECK (docks > 0)
                        );
                        CREATE TABLE bike (
                            id         VARCHAR(10) PRIMARY KEY,
                            type       VARCHAR(10) NOT NULL CHECK (type IN ('CLASSIC', 'EBIKE', 'CARGO')),
                            status     VARCHAR(12) NOT NULL CHECK (status IN ('AVAILABLE', 'RENTED', 'MAINTENANCE')),
                            station_id VARCHAR(20) REFERENCES station(id)
                        );
                        """),
                new Migration(2, "rentals", """
                        CREATE TABLE rental (
                            id         VARCHAR(20) PRIMARY KEY,
                            rider_id   VARCHAR(20) NOT NULL,
                            bike_id    VARCHAR(10) NOT NULL REFERENCES bike(id),
                            started_at TIMESTAMP NOT NULL,
                            minutes    INT CHECK (minutes >= 0),
                            fare_cents BIGINT CHECK (fare_cents >= 0)
                        );
                        """),
                new Migration(3, "index for a rider's ride history",
                        "CREATE INDEX idx_rental_rider_started ON rental(rider_id, started_at)"));
    }
}

package jq.m08.databasedesign;

import java.util.List;

/** Spoke's database schema, written as an ordered list of migrations. */
public final class SpokeSchema {

    private SpokeSchema() {}

    public static List<Migration> migrations() {
        return List.of(
                // TODO 1: station: name must be present and unique, and docks must be present and above 0
                // TODO 2: bike: type and status must be present and one of the allowed words, and station_id must point at a real station
                new Migration(1, "stations and bikes", """
                        CREATE TABLE station (
                            id    VARCHAR(20) PRIMARY KEY,
                            name  VARCHAR(40),
                            docks INT
                        );
                        CREATE TABLE bike (
                            id         VARCHAR(10) PRIMARY KEY,
                            type       VARCHAR(10),
                            status     VARCHAR(12),
                            station_id VARCHAR(20)
                        );
                        """),
                // TODO 3: rental: rider_id, bike_id and started_at must be present, bike_id must point at a real bike,
                //         and minutes and fare_cents may be NULL but never negative
                new Migration(2, "rentals", """
                        CREATE TABLE rental (
                            id         VARCHAR(20) PRIMARY KEY,
                            rider_id   VARCHAR(20),
                            bike_id    VARCHAR(10),
                            started_at TIMESTAMP,
                            minutes    INT,
                            fare_cents BIGINT
                        );
                        """)
                // TODO 4: add Migration 3, "index for a rider's ride history", that creates the index idx_rental_rider_started
                //         so that WHERE rider_id = ? ORDER BY started_at can jump straight to one rider's rentals
                );
    }
}

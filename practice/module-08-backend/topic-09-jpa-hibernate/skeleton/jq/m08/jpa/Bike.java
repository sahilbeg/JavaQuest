package jq.m08.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** A bike, stored as one row in the bike table. Many bikes belong to one station. */
@Entity
public class Bike {

    @Id
    private String id;      // "B-1042"

    // TODO 1: store type and status as words, not numbers: put @Enumerated(EnumType.STRING) on both fields
    private BikeType type;

    private BikeStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private Station station;

    protected Bike() {
        // JPA needs a no-argument constructor to build objects from rows
    }

    public Bike(String id, BikeType type, BikeStatus status, Station station) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.station = station;
    }

    public String getId() {
        return id;
    }

    public BikeType getType() {
        return type;
    }

    public BikeStatus getStatus() {
        return status;
    }

    public Station getStation() {
        return station;
    }
}

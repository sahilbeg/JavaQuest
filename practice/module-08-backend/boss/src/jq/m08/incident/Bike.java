package jq.m08.incident;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
public class Bike {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private BikeStatus status;

    /** The station the bike is docked at, or null while someone rides it. */
    private String stationId;

    protected Bike() {
    }

    public Bike(String id, BikeStatus status, String stationId) {
        this.id = id;
        this.status = status;
        this.stationId = stationId;
    }

    public String getId() {
        return id;
    }

    public BikeStatus getStatus() {
        return status;
    }

    public String getStationId() {
        return stationId;
    }

    /** Takes the bike off its dock for a ride. */
    public void markRented() {
        status = BikeStatus.RENTED;
        stationId = null;
    }
}

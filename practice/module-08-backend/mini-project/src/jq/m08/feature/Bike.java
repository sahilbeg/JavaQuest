package jq.m08.feature;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Bike {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private BikeType type;

    @Enumerated(EnumType.STRING)
    private BikeStatus status;

    /** The station the bike is docked at. A RENTED bike is out on the street, so its station is null. */
    @ManyToOne
    private Station station;

    protected Bike() {
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

    /** A rider takes the bike off its dock. */
    public void rent() {
        status = BikeStatus.RENTED;
        station = null;
    }
}

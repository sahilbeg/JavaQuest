package jq.m08.incident;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Rental {

    @Id
    @GeneratedValue
    private Long id;

    private String riderId;

    private String bikeId;

    private String startStation;

    protected Rental() {
    }

    public Rental(String riderId, String bikeId, String startStation) {
        this.riderId = riderId;
        this.bikeId = bikeId;
        this.startStation = startStation;
    }

    public Long getId() {
        return id;
    }

    public String getRiderId() {
        return riderId;
    }

    public String getBikeId() {
        return bikeId;
    }

    public String getStartStation() {
        return startStation;
    }
}

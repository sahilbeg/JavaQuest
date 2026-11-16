package jq.m08.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/** A docking station, stored as one row in the station table. */
@Entity
public class Station {

    @Id
    private String id;      // a short slug such as "harbor"

    private String name;    // "Harbor"

    private int docks;      // 12

    protected Station() {
        // JPA needs a no-argument constructor to build objects from rows
    }

    public Station(String id, String name, int docks) {
        this.id = id;
        this.name = name;
        this.docks = docks;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDocks() {
        return docks;
    }
}

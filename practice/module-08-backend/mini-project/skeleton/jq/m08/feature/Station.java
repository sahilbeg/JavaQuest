package jq.m08.feature;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Station {

    /** A short slug, such as "old-mill". */
    @Id
    private String id;

    private String name;

    /** How many docks the station has in total. */
    private int docks;

    protected Station() {
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

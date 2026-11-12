package jq.m03.customexceptions;

public class Station {

    private final String name;
    private final int docks;
    private int docked;

    public Station(String name, int docks) {
        this.name = name;
        this.docks = docks;
    }

    public String getName() {
        return name;
    }

    public int dock(String bikeId) throws DockFullException {
        if (docked == docks) {
            throw new DockFullException(name);
        }
        docked = docked + 1;
        return docked;
    }
}

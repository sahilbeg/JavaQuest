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
        // TODO 3: if every dock is used, throw new DockFullException(name)
        // TODO 4: otherwise add 1 to docked and return the new dock number
        return 0;
    }
}

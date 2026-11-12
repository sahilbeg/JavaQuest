package jq.m02.composition;

public class Station {
    private final String name;
    private final Dock[] docks;

    public Station(String name) {
        this.name = name;
        this.docks = new Dock[12];
        // TODO 4: put a new Dock() in every slot of docks, so the station HAS 12 real docks
    }

    public String getName() {
        return name;
    }

    public int dock(Bike bike) {
        // TODO 5: park the bike in the first free dock and return its number (index + 1), or -1 when all 12 are taken
        return 0;
    }

    public Bike undock(String bikeId) {
        // TODO 6: find the dock holding bikeId, release that bike and return it, or return null
        return null;
    }

    public int freeDocks() {
        // TODO 7: count the docks where isFree() is true
        return 0;
    }

    public int findDock(String bikeId) {
        // TODO 8: return the number (1 to 12) of the dock holding the bike with this id, or -1
        return 0;
    }
}

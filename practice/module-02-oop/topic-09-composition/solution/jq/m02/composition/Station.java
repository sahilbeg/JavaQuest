package jq.m02.composition;

public class Station {
    private final String name;
    private final Dock[] docks;

    public Station(String name) {
        this.name = name;
        this.docks = new Dock[12];
        for (int i = 0; i < docks.length; i++) {
            docks[i] = new Dock();
        }
    }

    public String getName() {
        return name;
    }

    public int dock(Bike bike) {
        for (int i = 0; i < docks.length; i++) {
            if (docks[i].isFree()) {
                docks[i].park(bike);
                return i + 1;
            }
        }
        return -1;
    }

    public Bike undock(String bikeId) {
        int number = findDock(bikeId);
        if (number == -1) {
            return null;
        }
        return docks[number - 1].release();
    }

    public int freeDocks() {
        int count = 0;
        for (Dock dock : docks) {
            if (dock.isFree()) {
                count++;
            }
        }
        return count;
    }

    public int findDock(String bikeId) {
        for (int i = 0; i < docks.length; i++) {
            Bike parked = docks[i].getBike();
            if (parked != null && parked.getId().equals(bikeId)) {
                return i + 1;
            }
        }
        return -1;
    }
}

package jq.m02.composition;

public class Dock {
    private Bike bike;

    public Bike getBike() {
        return bike;
    }

    public boolean isFree() {
        // TODO 1: return true when no bike is parked here (bike is null)
        return false;
    }

    public void park(Bike bike) {
        // TODO 2: store the bike in the field
    }

    public Bike release() {
        // TODO 3: remember the bike, empty the field, then return the bike you remembered
        return null;
    }
}

package jq.m02.composition;

public class Dock {
    private Bike bike;

    public Bike getBike() {
        return bike;
    }

    public boolean isFree() {
        return bike == null;
    }

    public void park(Bike bike) {
        this.bike = bike;
    }

    public Bike release() {
        Bike leaving = bike;
        bike = null;
        return leaving;
    }
}

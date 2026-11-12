package jq.m05.synchronization;

public class Dock {
    private final String station;
    private int bikes;

    public Dock(String station, int bikes) {
        this.station = station;
        // TODO 3: store the number of bikes
    }

    public String station() {
        return station;
    }

    public boolean tryUnlock() {
        // TODO 4: if a bike is left, take it and return true; otherwise return false.
        //         The check and the take must happen under one lock.
        return false;
    }

    public int bikesLeft() {
        // TODO 5: return the number of bikes left, safely
        return 0;
    }

    public static int unlockMany(Dock dock, int riders) throws InterruptedException {
        // TODO 6: start `riders` threads, each calling dock.tryUnlock() once,
        //         join them all, and return how many unlocks succeeded
        return 0;
    }

    public static void main(String[] args) throws InterruptedException {
        Dock harbor = new Dock("Harbor", 5);
        int unlocked = unlockMany(harbor, 50);
        System.out.println(unlocked + " riders unlocked a bike at " + harbor.station());
        System.out.println("Bikes left: " + harbor.bikesLeft());
    }
}

package jq.m05.synchronization;

public class Dock {
    private final String station;
    private int bikes;

    public Dock(String station, int bikes) {
        this.station = station;
        this.bikes = bikes;
    }

    public String station() {
        return station;
    }

    public synchronized boolean tryUnlock() {
        if (bikes > 0) {
            bikes--;
            return true;
        }
        return false;
    }

    public synchronized int bikesLeft() {
        return bikes;
    }

    public static int unlockMany(Dock dock, int riders) throws InterruptedException {
        UnlockCounter successes = new UnlockCounter();
        Thread[] threads = new Thread[riders];
        for (int i = 0; i < riders; i++) {
            threads[i] = new Thread(() -> {
                if (dock.tryUnlock()) {
                    successes.increment();
                }
            }, "rider-" + (i + 1));
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }
        return successes.get();
    }

    public static void main(String[] args) throws InterruptedException {
        Dock harbor = new Dock("Harbor", 5);
        int unlocked = unlockMany(harbor, 50);
        System.out.println(unlocked + " riders unlocked a bike at " + harbor.station());
        System.out.println("Bikes left: " + harbor.bikesLeft());
    }
}

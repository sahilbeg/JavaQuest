package jq.m03.lists;

import java.util.ArrayList;
import java.util.List;

public class RideQueue {

    private List<Ride> rides = new ArrayList<>();

    public void add(Ride ride) {
        rides.add(ride);
    }

    public int size() {
        return rides.size();
    }

    public Ride get(int index) {
        return rides.get(index);
    }

    public void addFirst(Ride ride) {
        // TODO 1: put ride at position 0 with rides.add(0, ride)
    }

    public int totalMinutes() {
        // TODO 2: add up minutes() for every ride
        return 0;
    }

    public int removeShortRides(int underMinutes) {
        // TODO 3: loop backwards with an index, remove rides under underMinutes, count them
        return 0;
    }

    public List<Ride> ridesFrom(String station) {
        // TODO 4: build a new ArrayList with the rides whose station equals station
        return null;
    }

    public Ride longest() {
        // TODO 5: return the ride with the most minutes, or null when the queue is empty
        return null;
    }

    public static void main(String[] args) {
        RideQueue queue = new RideQueue();
        queue.add(new Ride("B-1042", "Harbor", "ebike", 25));
        queue.add(new Ride("B-2001", "Harbor", "classic", 1));
        queue.add(new Ride("B-3300", "Uptown", "cargo", 40));
        queue.addFirst(new Ride("B-7777", "Harbor", "classic", 12));
        System.out.println("Removed short rides: " + queue.removeShortRides(2));
        System.out.println("Total minutes: " + queue.totalMinutes());
        System.out.println("From Harbor: " + queue.ridesFrom("Harbor"));
        System.out.println("Longest: " + queue.longest());
    }
}

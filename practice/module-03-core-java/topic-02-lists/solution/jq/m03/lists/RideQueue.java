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
        rides.add(0, ride);
    }

    public int totalMinutes() {
        int total = 0;
        for (Ride ride : rides) {
            total = total + ride.minutes();
        }
        return total;
    }

    public int removeShortRides(int underMinutes) {
        int removed = 0;
        for (int i = rides.size() - 1; i >= 0; i--) {
            if (rides.get(i).minutes() < underMinutes) {
                rides.remove(i);
                removed++;
            }
        }
        return removed;
    }

    public List<Ride> ridesFrom(String station) {
        List<Ride> result = new ArrayList<>();
        for (Ride ride : rides) {
            if (ride.station().equals(station)) {
                result.add(ride);
            }
        }
        return result;
    }

    public Ride longest() {
        if (rides.isEmpty()) {
            return null;
        }
        Ride best = rides.get(0);
        for (Ride ride : rides) {
            if (ride.minutes() > best.minutes()) {
                best = ride;
            }
        }
        return best;
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

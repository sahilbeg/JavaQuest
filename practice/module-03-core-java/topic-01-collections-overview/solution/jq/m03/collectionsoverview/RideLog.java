package jq.m03.collectionsoverview;

import java.util.ArrayList;
import java.util.List;

public class RideLog {

    private List<String> rides = new ArrayList<>();

    public void add(String bikeId) {
        rides.add(bikeId);
    }

    public int count() {
        return rides.size();
    }

    public String get(int index) {
        return rides.get(index);
    }

    public static String pickCollection(String job) {
        if (job.equals("keep rides in order")) {
            return "List";
        }
        if (job.equals("unique bike ids")) {
            return "Set";
        }
        if (job.equals("minutes per bike")) {
            return "Map";
        }
        return "";
    }

    public static void main(String[] args) {
        RideLog log = new RideLog();
        for (int i = 1; i <= 101; i++) {
            log.add("B-" + (1000 + i));
        }
        System.out.println("Rides logged: " + log.count());
        System.out.println("Ride 101 was bike " + log.get(100));
    }
}

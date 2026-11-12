package jq.m03.collectionsoverview;

import java.util.ArrayList;
import java.util.List;

public class RideLog {

    // TODO 1: start the log as an empty ArrayList with new ArrayList<>()
    private List<String> rides = null;

    public void add(String bikeId) {
        // TODO 2: put bikeId on the end of the list
    }

    public int count() {
        // TODO 3: return how many rides the list holds
        return 0;
    }

    public String get(int index) {
        // TODO 4: return the bike id at this position
        return "";
    }

    public static String pickCollection(String job) {
        // TODO 5: "keep rides in order" -> "List", "unique bike ids" -> "Set", "minutes per bike" -> "Map", anything else -> ""
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

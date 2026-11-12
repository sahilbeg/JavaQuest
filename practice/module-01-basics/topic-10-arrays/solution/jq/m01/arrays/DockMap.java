package jq.m01.arrays;

public class DockMap {

    public static int countBikes(boolean[] docks) {
        int bikes = 0;
        for (boolean hasBike : docks) {
            if (hasBike) {
                bikes++;
            }
        }
        return bikes;
    }

    public static int firstEmptyDock(boolean[] docks) {
        for (int i = 0; i < docks.length; i++) {
            if (!docks[i]) {
                return i + 1;
            }
        }
        return -1;
    }

    public static int totalMinutes(int[] rides) {
        int total = 0;
        for (int minutes : rides) {
            total = total + minutes;
        }
        return total;
    }

    public static int longestRide(int[] rides) {
        int longest = 0;
        for (int minutes : rides) {
            if (minutes > longest) {
                longest = minutes;
            }
        }
        return longest;
    }

    public static int[] bikesPerStation(boolean[][] stations) {
        int[] counts = new int[stations.length];
        for (int s = 0; s < stations.length; s++) {
            counts[s] = countBikes(stations[s]);
        }
        return counts;
    }

    public static void main(String[] args) {
        boolean[] riverside = {true, true, false, true, false, false, true, true, true, false, true, true};
        System.out.println("Bikes at Riverside: " + countBikes(riverside));
        System.out.println("First empty dock: " + firstEmptyDock(riverside));
    }
}

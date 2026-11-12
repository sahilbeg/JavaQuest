package jq.m01.arrays;

public class DockMap {

    public static int countBikes(boolean[] docks) {
        // TODO 1: count how many docks are true (hold a bike)
        return 0;
    }

    public static int firstEmptyDock(boolean[] docks) {
        // TODO 2: return the dock NUMBER (index + 1) of the first false dock, or -1 if every dock holds a bike
        return 0;
    }

    public static int totalMinutes(int[] rides) {
        // TODO 3: add up every ride's minutes
        return 0;
    }

    public static int longestRide(int[] rides) {
        // TODO 4: return the biggest number in rides, or 0 when rides is empty
        return 0;
    }

    public static int[] bikesPerStation(boolean[][] stations) {
        // TODO 5: return an int[] with one bike count per row (call countBikes for each row)
        return new int[0];
    }

    public static void main(String[] args) {
        boolean[] riverside = {true, true, false, true, false, false, true, true, true, false, true, true};
        System.out.println("Bikes at Riverside: " + countBikes(riverside));
        System.out.println("First empty dock: " + firstEmptyDock(riverside));
    }
}

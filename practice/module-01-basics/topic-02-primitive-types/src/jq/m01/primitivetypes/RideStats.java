package jq.m01.primitivetypes;

public class RideStats {

    public static double distanceKm() {
        // TODO 1: store 2.5 in a double variable called distanceKm, then return it
        return 0.0;
    }

    public static boolean isMember() {
        // TODO 2: store true in a boolean variable called isMember, then return it
        return false;
    }

    public static int roundedDownKm(double km) {
        // TODO 3: cast km to an int and return it, so 2.5 becomes 2
        return 0;
    }

    public static double averageMinutes(int totalMinutes, int rides) {
        // TODO 4: cast totalMinutes to a double before dividing by rides, so 45 and 2 give 22.5
        return 0.0;
    }

    public static void main(String[] args) {
        System.out.println(distanceKm());
        System.out.println(isMember());
        System.out.println(roundedDownKm(2.5));
        System.out.println(averageMinutes(45, 2));
    }
}

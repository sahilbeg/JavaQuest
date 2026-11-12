package jq.m01.primitivetypes;

public class RideStats {

    public static double distanceKm() {
        double distanceKm = 2.5;
        return distanceKm;
    }

    public static boolean isMember() {
        boolean isMember = true;
        return isMember;
    }

    public static int roundedDownKm(double km) {
        return (int) km;
    }

    public static double averageMinutes(int totalMinutes, int rides) {
        return (double) totalMinutes / rides;
    }

    public static void main(String[] args) {
        System.out.println(distanceKm());
        System.out.println(isMember());
        System.out.println(roundedDownKm(2.5));
        System.out.println(averageMinutes(45, 2));
    }
}

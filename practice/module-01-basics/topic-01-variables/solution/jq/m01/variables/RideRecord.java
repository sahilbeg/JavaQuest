package jq.m01.variables;

public class RideRecord {

    public static String bikeId() {
        String bikeId = "B-1042";
        return bikeId;
    }

    public static String startStation() {
        String startStation = "Riverside";
        return startStation;
    }

    public static int rideMinutes() {
        int rideMinutes = 20;
        return rideMinutes;
    }

    public static int minutesAfterExtension() {
        int minutes = 20;
        minutes = 35;
        return minutes;
    }

    public static void main(String[] args) {
        System.out.println(bikeId());
        System.out.println(startStation());
        System.out.println(rideMinutes());
        System.out.println(minutesAfterExtension());
    }
}

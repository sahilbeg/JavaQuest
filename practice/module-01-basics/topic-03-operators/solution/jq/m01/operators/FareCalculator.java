package jq.m01.operators;

public class FareCalculator {

    public static double classicFare(int minutes) {
        return 1.00 + minutes * 0.15;
    }

    public static double eBikeFare(int minutes) {
        return 1.00 + minutes * 0.30;
    }

    public static String hoursAndMinutes(int minutes) {
        return (minutes / 60) + " h " + (minutes % 60) + " min";
    }

    public static String fareLine(int minutes) {
        return "Fare: $" + classicFare(minutes);
    }

    public static void main(String[] args) {
        System.out.println(hoursAndMinutes(75));
        System.out.println(fareLine(10));
    }
}

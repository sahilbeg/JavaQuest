package jq.m01.operators;

public class FareCalculator {

    public static double classicFare(int minutes) {
        // TODO 1: return the unlock fee 1.00 plus minutes times 0.15
        return 0.0;
    }

    public static double eBikeFare(int minutes) {
        // TODO 2: return the unlock fee 1.00 plus minutes times 0.30
        return 0.0;
    }

    public static String hoursAndMinutes(int minutes) {
        // TODO 3: use / and % to return text like "1 h 15 min"
        return "";
    }

    public static String fareLine(int minutes) {
        // TODO 4: return "Fare: $" joined with classicFare(minutes)
        return "";
    }

    public static void main(String[] args) {
        System.out.println(hoursAndMinutes(75));
        System.out.println(fareLine(10));
    }
}

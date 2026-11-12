package jq.m01.switchcases;

public class BikeTypes {

    public static double pricePerMinute(String type) {
        // TODO 1: use a switch to return 0.15 for "classic", 0.30 for "ebike", 0.25 for "cargo", and -1 for anything else
        return 0.0;
    }

    public static double fare(String type, int minutes) {
        // TODO 2: return 1.00 + minutes * pricePerMinute(type), or -1 when the type is unknown
        return 0.0;
    }

    public static String dockColor(int dock) {
        // TODO 3: use a switch expression: docks 1, 2, 3 are "blue", docks 4, 5, 6 are "green", any other dock is "grey"
        return "";
    }

    public static void main(String[] args) {
        System.out.println("Cargo, 20 minutes: $" + fare("cargo", 20));
        System.out.println("E-bike, 10 minutes: $" + fare("ebike", 10));
        System.out.println("Dock 5 is " + dockColor(5));
    }
}

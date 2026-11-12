package jq.m01.switchcases;

public class BikeTypes {

    public static double pricePerMinute(String type) {
        return switch (type) {
            case "classic" -> 0.15;
            case "ebike" -> 0.30;
            case "cargo" -> 0.25;
            default -> -1;
        };
    }

    public static double fare(String type, int minutes) {
        double price = pricePerMinute(type);
        if (price == -1) {
            return -1;
        }
        return 1.00 + minutes * price;
    }

    public static String dockColor(int dock) {
        return switch (dock) {
            case 1, 2, 3 -> "blue";
            case 4, 5, 6 -> "green";
            default -> "grey";
        };
    }

    public static void main(String[] args) {
        System.out.println("Cargo, 20 minutes: $" + fare("cargo", 20));
        System.out.println("E-bike, 10 minutes: $" + fare("ebike", 10));
        System.out.println("Dock 5 is " + dockColor(5));
    }
}

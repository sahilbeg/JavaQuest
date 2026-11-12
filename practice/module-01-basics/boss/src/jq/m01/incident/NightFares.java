package jq.m01.incident;

public class NightFares {

    public static int minutesBetween(int start, int end) {
        // TODO bronze: rides that cross midnight come out negative
        return end - start;
    }

    public static double pricePerMinute(String type) {
        return switch (type) {
            case "classic" -> 0.15;
            case "ebike" -> 0.30;
            case "cargo" -> 0.25;
            default -> -1;
        };
    }

    public static double fare(int start, int end, String type, boolean member) {
        double price = pricePerMinute(type);
        if (price < 0) {
            return -1;
        }
        int minutes = minutesBetween(start, end);
        // TODO silver: members must not pay the unlock fee, and their first 30 minutes are free
        if (member && minutes < 30) {
            return 1.00;
        }
        return 1.00 + minutes * price;
    }

    public static String receipt(int start, int end, String type, boolean member) {
        // TODO gold: return a line like "23:50-00:20 30 min $5.50"
        return "";
    }

    public static void main(String[] args) {
        System.out.println(receipt(1430, 20, "classic", false));
    }
}

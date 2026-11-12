package jq.m01.incident;

public class NightFares {

    public static int minutesBetween(int start, int end) {
        return (end - start + 1440) % 1440;
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
        if (member) {
            int paidMinutes = 0;
            if (minutes > 30) {
                paidMinutes = minutes - 30;
            }
            return paidMinutes * price;
        }
        return 1.00 + minutes * price;
    }

    private static String clock(int minuteOfDay) {
        return String.format("%02d:%02d", minuteOfDay / 60, minuteOfDay % 60);
    }

    public static String receipt(int start, int end, String type, boolean member) {
        return String.format("%s-%s %d min $%.2f", clock(start), clock(end), minutesBetween(start, end),
                fare(start, end, type, member));
    }

    public static void main(String[] args) {
        System.out.println(receipt(1430, 20, "classic", false));
    }
}

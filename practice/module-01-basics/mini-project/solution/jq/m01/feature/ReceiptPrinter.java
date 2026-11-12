package jq.m01.feature;

public class ReceiptPrinter {

    public static double pricePerMinute(String type) {
        return switch (type) {
            case "classic" -> 0.15;
            case "ebike" -> 0.30;
            case "cargo" -> 0.25;
            default -> -1;
        };
    }

    public static double fare(String type, int minutes, boolean member) {
        double price = pricePerMinute(type);
        if (member) {
            int paidMinutes = 0;
            if (minutes > 30) {
                paidMinutes = minutes - 30;
            }
            return paidMinutes * price;
        }
        return 1.00 + minutes * price;
    }

    public static String receipt(String bikeId, String station, String type, int minutes, boolean member) {
        if (pricePerMinute(type) < 0) {
            return "Unknown bike type";
        }
        StringBuilder text = new StringBuilder();
        text.append("SPOKE RIVERTON").append("\n");
        text.append(String.format("%-9s%s", "Bike", bikeId)).append("\n");
        text.append(String.format("%-9s%s", "From", station)).append("\n");
        text.append(String.format("%-9s%s", "Type", type)).append("\n");
        text.append(String.format("%-9s%d min", "Time", minutes)).append("\n");
        text.append(String.format("%-9s$%.2f", "Fare", fare(type, minutes, member))).append("\n");
        text.append("Thank you for riding!");
        return text.toString();
    }

    public static void main(String[] args) {
        System.out.println(receipt("B-1042", "Riverside", "ebike", 25, false));
    }
}

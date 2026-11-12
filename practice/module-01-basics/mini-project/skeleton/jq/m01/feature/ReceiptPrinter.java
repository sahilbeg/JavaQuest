package jq.m01.feature;

public class ReceiptPrinter {

    public static double pricePerMinute(String type) {
        // TODO 1: 0.15 for classic, 0.30 for ebike, 0.25 for cargo, -1 for anything else
        return 0;
    }

    public static double fare(String type, int minutes, boolean member) {
        // TODO 2: non-members pay 1.00 plus every minute; members get 30 free minutes and no unlock fee
        return 0;
    }

    public static String receipt(String bikeId, String station, String type, int minutes, boolean member) {
        // TODO 3: build the seven-line receipt from the KB page, or "Unknown bike type"
        return "";
    }

    public static void main(String[] args) {
        System.out.println(receipt("B-1042", "Riverside", "ebike", 25, false));
    }
}

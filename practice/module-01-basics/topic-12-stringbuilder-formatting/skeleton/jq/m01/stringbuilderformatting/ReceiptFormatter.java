package jq.m01.stringbuilderformatting;

public class ReceiptFormatter {

    public static String money(double amount) {
        // TODO 1: return the amount as "$" plus 2 decimals, using String.format("$%.2f", amount)
        return "";
    }

    public static String line(String label, double amount) {
        // TODO 2: return String.format("%-12s%8s", label, money(amount))
        return "";
    }

    public static String receipt(String[] labels, double[] amounts) {
        // TODO 3: make a StringBuilder and a total that starts at 0
        // TODO 4: for each item, append line(...) and "\n", and add the amount to the total
        // TODO 5: append line("TOTAL", total) and return the builder's toString()
        return "";
    }

    public static void main(String[] args) {
        String[] labels = {"Unlock fee", "Ride 10 min"};
        double[] amounts = {1.0, 1.5};
        System.out.println(receipt(labels, amounts));
    }
}

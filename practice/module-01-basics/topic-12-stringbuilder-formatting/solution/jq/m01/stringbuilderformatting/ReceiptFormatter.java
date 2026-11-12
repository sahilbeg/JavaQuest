package jq.m01.stringbuilderformatting;

public class ReceiptFormatter {

    public static String money(double amount) {
        return String.format("$%.2f", amount);
    }

    public static String line(String label, double amount) {
        return String.format("%-12s%8s", label, money(amount));
    }

    public static String receipt(String[] labels, double[] amounts) {
        StringBuilder sb = new StringBuilder();
        double total = 0;
        for (int i = 0; i < labels.length; i++) {
            sb.append(line(labels[i], amounts[i]));
            sb.append("\n");
            total = total + amounts[i];
        }
        sb.append(line("TOTAL", total));
        return sb.toString();
    }

    public static void main(String[] args) {
        String[] labels = {"Unlock fee", "Ride 10 min"};
        double[] amounts = {1.0, 1.5};
        System.out.println(receipt(labels, amounts));
    }
}

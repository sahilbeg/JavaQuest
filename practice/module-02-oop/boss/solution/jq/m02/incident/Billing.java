package jq.m02.incident;

public class Billing {

    public static String money(double amount) {
        return String.format("$%.2f", amount);
    }

    public static String invoice(Bike[] bikes, int[] minutes) {
        StringBuilder text = new StringBuilder();
        double total = 0;
        for (int i = 0; i < bikes.length; i++) {
            double fare = bikes[i].fare(minutes[i]);
            total += fare;
            text.append(String.format("%-7s%-9s%3d min %6s", bikes[i].getId(), bikes[i].type(), minutes[i], money(fare)));
            text.append("\n");
        }
        text.append(String.format("%-23s%7s", "TOTAL", money(total)));
        return text.toString();
    }

    public static void main(String[] args) {
        Bike[] bikes = { new EBike("B-2001"), new CargoBike("B-3001") };
        System.out.println(invoice(bikes, new int[] { 10, 20 }));
    }
}

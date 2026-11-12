package jq.m02.incident;

public class Billing {

    public static String money(double amount) {
        return String.format("$%.2f", amount);
    }

    public static String invoice(Bike[] bikes, int[] minutes) {
        // TODO gold: one line per ride, then a TOTAL line, joined with "\n"
        return "";
    }

    public static void main(String[] args) {
        Bike[] bikes = { new EBike("B-2001"), new CargoBike("B-3001") };
        System.out.println(invoice(bikes, new int[] { 10, 20 }));
    }
}

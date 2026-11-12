package jq.m02.feature;

public class Rider {

    private final String name;
    private Membership level;
    private final Ride[] rides = new Ride[50];
    private int count;

    public Rider(String name, Membership level) {
        this.name = name;
        this.level = level;
    }

    public void recordRide(Ride ride) {
        if (count < rides.length) {
            rides[count] = ride;
            count++;
        }
    }

    public int rideCount() {
        return count;
    }

    public void upgrade(Membership level) {
        this.level = level;
    }

    public double fareFor(Ride ride) {
        int paidMinutes = ride.minutes() - level.freeMinutes();
        if (paidMinutes < 0) {
            paidMinutes = 0;
        }
        double fare = paidMinutes * ride.type().pricePerMinute();
        if (level.paysUnlockFee()) {
            fare += 1.00;
        }
        return fare;
    }

    public double monthTotal() {
        double total = 0;
        for (int i = 0; i < count; i++) {
            total += fareFor(rides[i]);
        }
        return total;
    }

    public String statement() {
        StringBuilder text = new StringBuilder();
        text.append(String.format("Statement for %s (%s)", name, level)).append("\n");
        for (int i = 0; i < count; i++) {
            Ride ride = rides[i];
            text.append(String.format("%-9s%4d min %7s", ride.type(), ride.minutes(), money(fareFor(ride)))).append("\n");
        }
        text.append(String.format("%-18s%7s", "Total", money(monthTotal())));
        return text.toString();
    }

    private static String money(double amount) {
        return String.format("$%.2f", amount);
    }

    public static void main(String[] args) {
        Rider ada = new Rider("Ada", Membership.PLUS);
        ada.recordRide(new Ride(BikeType.EBIKE, 40));
        ada.recordRide(new Ride(BikeType.CLASSIC, 20));
        ada.recordRide(new Ride(BikeType.CARGO, 90));
        System.out.println(ada.statement());
    }
}

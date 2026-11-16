package jq.m06.incident;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class Billing {

    private static final double UNLOCK_FEE = 1.00;
    private static final Map<String, Double> PRICE_PER_MINUTE = Map.of("classic", 0.15, "ebike", 0.30, "cargo", 0.25);

    private Billing() {
    }

    public static long fareCents(Ride ride, Membership membership) {
        Double price = PRICE_PER_MINUTE.get(ride.type());
        if (price == null) {
            throw new IllegalArgumentException("unknown type: " + ride.type());
        }
        int billable = Math.max(0, ride.minutes() - membership.freeMinutes());
        double dollars = billable * price;
        // TODO bronze: which memberships pay the unlock fee in LegacyBilling?
        if (membership != Membership.PLUS) {
            dollars = dollars + UNLOCK_FEE;
        }
        // TODO silver: a double cannot hold 0.15 exactly, and the cast cuts off the lost cent
        return (long) (dollars * 100);
    }

    public static String invoice(List<Ride> rides, Membership membership) {
        // TODO gold: this sorts the caller's list
        rides.sort(Comparator.comparing(Ride::bikeId));
        StringBuilder text = new StringBuilder();
        long sum = 0;
        for (Ride ride : rides) {
            long cents = fareCents(ride, membership);
            sum += cents;
            text.append(String.format("%s %s %d min: %s%n", ride.bikeId(), ride.type(), ride.minutes(), dollars(cents)));
        }
        return text.append("Total: ").append(dollars(sum)).toString();
    }

    private static String dollars(long cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }

    public static void main(String[] args) {
        List<Ride> rides = new java.util.ArrayList<>(List.of(
                new Ride("B-2001", "classic", 3),
                new Ride("B-1042", "ebike", 70)));
        System.out.println(invoice(rides, Membership.PRO));
        System.out.println("Your list afterwards: " + rides);
    }
}

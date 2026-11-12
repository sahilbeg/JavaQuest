package jq.m06.incident;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class Billing {

    private static final long UNLOCK_FEE_CENTS = 100;
    private static final Map<String, Long> CENTS_PER_MINUTE = Map.of("classic", 15L, "ebike", 30L, "cargo", 25L);

    private Billing() {
    }

    public static long fareCents(Ride ride, Membership membership) {
        Long centsPerMinute = CENTS_PER_MINUTE.get(ride.type());
        if (centsPerMinute == null) {
            throw new IllegalArgumentException("unknown type: " + ride.type());
        }
        int billable = Math.max(0, ride.minutes() - membership.freeMinutes());
        long cents = billable * centsPerMinute;
        if (membership == Membership.BASIC) {
            cents += UNLOCK_FEE_CENTS;
        }
        return cents;
    }

    public static String invoice(List<Ride> rides, Membership membership) {
        List<Ride> sorted = new ArrayList<>(rides);
        sorted.sort(Comparator.comparing(Ride::bikeId));
        StringBuilder text = new StringBuilder();
        long sum = 0;
        for (Ride ride : sorted) {
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

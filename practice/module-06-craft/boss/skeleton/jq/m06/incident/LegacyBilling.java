package jq.m06.incident;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** The old billing code. It is ugly but correct, and it is the reference. Do not change it. */
public final class LegacyBilling {

    private LegacyBilling() {
    }

    public static long fareCents(Ride r, Membership m) {
        int free = 0;
        if (m == Membership.PLUS) {
            free = 30;
        }
        if (m == Membership.PRO) {
            free = 60;
        }
        long total = 0;
        if (m == Membership.BASIC) {
            total = total + 100;
        }
        int billable = r.minutes() - free;
        if (billable < 0) {
            billable = 0;
        }
        if (r.type().equals("classic")) {
            total = total + billable * 15L;
        } else if (r.type().equals("ebike")) {
            total = total + billable * 30L;
        } else if (r.type().equals("cargo")) {
            total = total + billable * 25L;
        } else {
            throw new IllegalArgumentException("unknown type: " + r.type());
        }
        return total;
    }

    public static String invoice(List<Ride> rides, Membership m) {
        List<Ride> sorted = new ArrayList<>(rides);
        sorted.sort(Comparator.comparing(Ride::bikeId));
        StringBuilder text = new StringBuilder();
        long sum = 0;
        for (Ride r : sorted) {
            long cents = fareCents(r, m);
            sum = sum + cents;
            text.append(String.format("%s %s %d min: $%d.%02d%n", r.bikeId(), r.type(), r.minutes(), cents / 100, cents % 100));
        }
        text.append(String.format("Total: $%d.%02d", sum / 100, sum % 100));
        return text.toString();
    }
}

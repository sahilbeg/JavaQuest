package jq.m06.refactoring;

import java.util.ArrayList;
import java.util.List;

public class InvoiceGenerator {
    private static final long UNLOCK_FEE_CENTS = 100;

    public static String generate(List<String[]> rides, String membership) {
        String out = "Spoke invoice\n";
        out = out + "Membership: " + membership + "\n";
        if (rides.isEmpty()) {
            return out + "No rides this month.\n" + "Total: $0.00\n";
        }
        long total = 0;
        for (InvoiceLine line : lines(rides, membership)) {
            out = out + format(line) + "\n";
            total = total + line.cents();
        }
        out = out + "Rides: " + rides.size() + "\n";
        return out + "Total: " + money(total) + "\n";
    }

    public static List<InvoiceLine> lines(List<String[]> rides, String membership) {
        List<InvoiceLine> result = new ArrayList<>();
        for (String[] ride : rides) {
            String bikeId = ride[0];
            String type = ride[1];
            int minutes = Integer.parseInt(ride[2]);
            result.add(new InvoiceLine(bikeId, type, minutes, rideCents(type, minutes, membership)));
        }
        return result;
    }

    static String format(InvoiceLine line) {
        return line.bikeId() + " | " + line.type() + " | " + line.minutes() + " min | " + money(line.cents());
    }

    private static long rideCents(String type, int minutes, String membership) {
        if (membership.equals("BASIC")) {
            return UNLOCK_FEE_CENTS + minutes * centsPerMinute(type);
        }
        int paidMinutes = Math.max(0, minutes - freeMinutes(membership));
        return paidMinutes * centsPerMinute(type);
    }

    private static int freeMinutes(String membership) {
        if (membership.equals("PLUS")) {
            return 30;
        }
        if (membership.equals("PRO")) {
            return 60;
        }
        throw new IllegalArgumentException("Unknown membership: " + membership);
    }

    private static long centsPerMinute(String type) {
        if (type.equals("classic")) {
            return 15;
        }
        if (type.equals("ebike")) {
            return 30;
        }
        if (type.equals("cargo")) {
            return 25;
        }
        throw new IllegalArgumentException("Unknown bike type: " + type);
    }

    private static String money(long cents) {
        String pennies = String.valueOf(cents % 100);
        if (pennies.length() == 1) {
            pennies = "0" + pennies;
        }
        return "$" + (cents / 100) + "." + pennies;
    }

    public static void main(String[] args) {
        List<String[]> rides = List.of(
                new String[] {"B-1042", "classic", "12"},
                new String[] {"B-2001", "ebike", "45"});
        System.out.print(generate(rides, "PLUS"));
    }
}

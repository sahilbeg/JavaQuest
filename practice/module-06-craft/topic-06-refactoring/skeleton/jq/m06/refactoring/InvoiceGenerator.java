package jq.m06.refactoring;

import java.util.ArrayList;
import java.util.List;

public class InvoiceGenerator {

    public static String generate(List<String[]> rides, String membership) {
        String out = "Spoke invoice\n";
        out = out + "Membership: " + membership + "\n";
        if (rides.isEmpty()) {
            out = out + "No rides this month.\n";
            out = out + "Total: $0.00\n";
            return out;
        }
        long t = 0;
        for (String[] r : rides) {
            int m = Integer.parseInt(r[2]);
            long c = 0;
            if (membership.equals("BASIC")) {
                if (r[1].equals("classic")) {
                    c = 100 + m * 15;
                } else if (r[1].equals("ebike")) {
                    c = 100 + m * 30;
                } else if (r[1].equals("cargo")) {
                    c = 100 + m * 25;
                } else {
                    throw new IllegalArgumentException("Unknown bike type: " + r[1]);
                }
            } else if (membership.equals("PLUS")) {
                int paid = m - 30;
                if (paid < 0) {
                    paid = 0;
                }
                if (r[1].equals("classic")) {
                    c = paid * 15;
                } else if (r[1].equals("ebike")) {
                    c = paid * 30;
                } else if (r[1].equals("cargo")) {
                    c = paid * 25;
                } else {
                    throw new IllegalArgumentException("Unknown bike type: " + r[1]);
                }
            } else if (membership.equals("PRO")) {
                int paid = m - 60;
                if (paid < 0) {
                    paid = 0;
                }
                if (r[1].equals("classic")) {
                    c = paid * 15;
                } else if (r[1].equals("ebike")) {
                    c = paid * 30;
                } else if (r[1].equals("cargo")) {
                    c = paid * 25;
                } else {
                    throw new IllegalArgumentException("Unknown bike type: " + r[1]);
                }
            } else {
                throw new IllegalArgumentException("Unknown membership: " + membership);
            }
            String d = String.valueOf(c % 100);
            if (d.length() == 1) {
                d = "0" + d;
            }
            out = out + r[0] + " | " + r[1] + " | " + m + " min | $" + (c / 100) + "." + d + "\n";
            t = t + c;
        }
        String td = String.valueOf(t % 100);
        if (td.length() == 1) {
            td = "0" + td;
        }
        out = out + "Rides: " + rides.size() + "\n";
        out = out + "Total: $" + (t / 100) + "." + td + "\n";
        return out;
    }

    public static List<InvoiceLine> lines(List<String[]> rides, String membership) {
        // TODO 2: build one InvoiceLine per ride, with the same cents that generate works out
        return List.of();
    }

    static String format(InvoiceLine line) {
        // TODO 3: return one ride line exactly as generate prints it, without the newline
        return "";
    }

    // TODO 4: rewrite generate so it uses lines() and format(), and check the golden-master tests still pass

    public static void main(String[] args) {
        List<String[]> rides = List.of(
                new String[] {"B-1042", "classic", "12"},
                new String[] {"B-2001", "ebike", "45"});
        System.out.print(generate(rides, "PLUS"));
    }
}

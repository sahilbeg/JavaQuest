package jq.m09.performance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Builds the ride-history report: one line per ride, then a total line. */
public final class RideHistoryReport {

    private RideHistoryReport() {}

    /**
     * The old code, kept so you can compare. Its output is right, but it is slow on big inputs:
     * it scans the whole riders list for every ride, and += copies the whole report on every line.
     * Do not call it from build.
     */
    public static String slowBuild(List<Ride> rides, List<Rider> riders) {
        String report = "";
        long totalCents = 0;
        for (Ride ride : rides) {
            String name = "unknown rider";
            for (Rider rider : riders) {
                if (rider.id().equals(ride.riderId())) {
                    name = rider.name();
                    break;
                }
            }
            report += ride.rentalId() + " " + name + " " + ride.minutes() + " min " + ride.fareCents() + " cents\n";
            totalCents += ride.fareCents();
        }
        report += "total " + rides.size() + " rides " + totalCents + " cents\n";
        return report;
    }

    /** The same report as slowBuild, character for character, but fast. */
    public static String build(List<Ride> rides, List<Rider> riders) {
        // TODO 8: read the riders list ONCE, putting each rider's name into namesById under their id.
        Map<String, String> namesById = new HashMap<>();

        // TODO 9: for each ride, look the name up in namesById ("unknown rider" when it is missing),
        //         append the same line slowBuild writes to report, and add the fare to totalCents.
        StringBuilder report = new StringBuilder();
        long totalCents = 0;

        // TODO 10: append the total line, exactly as slowBuild does, and return report.toString().
        return "";
    }
}

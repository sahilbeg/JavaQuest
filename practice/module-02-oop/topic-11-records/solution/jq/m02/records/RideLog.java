package jq.m02.records;

public class RideLog {

    public static int totalMinutes(Ride[] rides) {
        int total = 0;
        for (int i = 0; i < rides.length; i++) {
            total = total + rides[i].minutes();
        }
        return total;
    }

    public static double totalFare(Ride[] rides) {
        double total = 0.0;
        for (int i = 0; i < rides.length; i++) {
            total = total + rides[i].fare();
        }
        return total;
    }

    public static Ride longest(Ride[] rides) {
        Ride best = null;
        for (int i = 0; i < rides.length; i++) {
            if (best == null || rides[i].minutes() > best.minutes()) {
                best = rides[i];
            }
        }
        return best;
    }
}

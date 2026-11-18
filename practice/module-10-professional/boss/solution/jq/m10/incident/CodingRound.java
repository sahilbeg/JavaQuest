package jq.m10.incident;

/**
 * Round 1, the coding round. Each element is one day's net takings in cents:
 * fares minus repair costs, so a day can be negative.
 * bestRun returns the largest total of one or more consecutive days.
 */
public class CodingRound {

    /** O(n) time, O(1) space: the best total of a run that ends today, carried from day to day. */
    public static long bestRun(int[] dailyNetCents) {
        if (dailyNetCents.length == 0) {
            throw new IllegalArgumentException("no days: there is no run to find");
        }
        long endingHere = dailyNetCents[0];
        long best = dailyNetCents[0];
        for (int day = 1; day < dailyNetCents.length; day++) {
            // Either today joins the run that ended yesterday, or a new run starts today.
            endingHere = Math.max(dailyNetCents[day], endingHere + dailyNetCents[day]);
            best = Math.max(best, endingHere);
        }
        return best;
    }
}

package jq.m10.incident;

/**
 * Round 1, the coding round. Each element is one day's net takings in cents:
 * fares minus repair costs, so a day can be negative.
 * bestRun returns the largest total of one or more consecutive days.
 *
 * This is the candidate's answer. It works on their one example, and nowhere else.
 */
public class CodingRound {

    // TODO 1 (bronze): Kakashi's notes say this answer has four bugs. Test it by hand, like an
    //                  interviewer: no days at all, one day, days that all lose money, a best run that
    //                  ends on the last day, and two days of 2,000,000,000 cents each. The README gives
    //                  the right answer for each one.
    // TODO 2 (silver): trying every start with every end is O(n^2). Make it one pass, O(n).
    public static long bestRun(int[] dailyNetCents) {
        int best = 0;
        for (int start = 0; start < dailyNetCents.length - 1; start++) {
            if (Thread.currentThread().isInterrupted()) {
                // The mock loop's timer: it stops a slow answer when the round's time is up. Keep it while loops are nested.
                throw new IllegalStateException("time is up: the round's timer stopped this answer");
            }
            int total = 0;
            for (int end = start; end < dailyNetCents.length - 1; end++) {
                total += dailyNetCents[end];
                if (total > best) {
                    best = total;
                }
            }
        }
        return best;
    }
}

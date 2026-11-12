package jq.m06.debugging;

public class RefundCalculator {

    // TODO 3: crash report 3 says partial refunds come out as 0 cents. Find out why and fix it, rounding down to whole cents.
    public long refundCents(long paidCents, int minutesLost, int totalMinutes) {
        return paidCents * (minutesLost / totalMinutes);
    }
}

package jq.m06.debugging;

public class RefundCalculator {

    public long refundCents(long paidCents, int minutesLost, int totalMinutes) {
        return paidCents * minutesLost / totalMinutes;
    }
}

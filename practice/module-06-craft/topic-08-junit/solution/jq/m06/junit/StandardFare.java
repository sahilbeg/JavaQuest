package jq.m06.junit;

/**
 * Spoke's fare rule, in cents.
 * BASIC pays the 100 cent unlock fee plus every minute.
 * PLUS rides the first 30 minutes free and pays no unlock fee.
 * PRO rides the first 60 minutes free and pays no unlock fee.
 * Per minute: classic 15, ebike 30, cargo 25.
 */
public class StandardFare implements FareCalculator {

    private static final long UNLOCK_FEE_CENTS = 100;

    @Override
    public long fareCents(String type, int minutes, String membership) {
        if (minutes < 0) {
            throw new IllegalArgumentException("minutes cannot be negative: " + minutes);
        }
        long perMinute = centsPerMinute(type);
        int freeMinutes = freeMinutes(membership);
        long unlockFee = 0;
        if (membership.equals("BASIC")) {
            unlockFee = UNLOCK_FEE_CENTS;
        }
        int paidMinutes = Math.max(0, minutes - freeMinutes);
        return unlockFee + paidMinutes * perMinute;
    }

    private long centsPerMinute(String type) {
        switch (type) {
            case "classic": return 15;
            case "ebike": return 30;
            case "cargo": return 25;
            default: throw new IllegalArgumentException("unknown bike type: " + type);
        }
    }

    private int freeMinutes(String membership) {
        switch (membership) {
            case "BASIC": return 0;
            case "PLUS": return 30;
            case "PRO": return 60;
            default: throw new IllegalArgumentException("unknown membership: " + membership);
        }
    }
}

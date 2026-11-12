package jq.m06.cleancode;

public class Billing {

    static final long UNLOCK_FEE_CENTS = 100;

    /** Old callers pass number codes. New code should call fareCents. */
    @Deprecated
    static long calc(int t, int m, int k) {
        return fareCents(BikeType.values()[t], m, Membership.values()[k]);
    }

    static long fareCents(BikeType type, int minutes, Membership membership) {
        if (minutes < 0) {
            throw new IllegalArgumentException("minutes must not be negative, got " + minutes);
        }
        return unlockFee(membership) + minuteCharge(type, minutes, membership);
    }

    private static long unlockFee(Membership membership) {
        if (!membership.paysUnlockFee()) {
            return 0;
        }
        return UNLOCK_FEE_CENTS;
    }

    private static long minuteCharge(BikeType type, int minutes, Membership membership) {
        int paidMinutes = Math.max(0, minutes - membership.freeMinutes());
        return paidMinutes * type.centsPerMinute();
    }

    public static void main(String[] args) {
        System.out.println(fareCents(BikeType.EBIKE, 40, Membership.BASIC));
    }
}

package jq.m02.enums;

public class FareCalculator {

    public static double fare(BikeType type, Membership level, int minutes) {
        if (level.paysUnlockFee()) {
            return 1.00 + minutes * type.pricePerMinute();
        }
        int paid = minutes - level.freeMinutes();
        if (paid < 0) {
            paid = 0;
        }
        return paid * type.pricePerMinute();
    }
}

package jq.m02.enums;

public class FareCalculator {

    public static double fare(BikeType type, Membership level, int minutes) {
        // TODO 5: if level pays the unlock fee, return 1.00 + minutes * type.pricePerMinute()
        // TODO 6: otherwise take off level.freeMinutes() first (never below 0) and charge the rest per minute
        return 0.0;
    }
}

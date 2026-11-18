package jq.m04.bitmanipulation;

import java.util.ArrayList;
import java.util.List;

/**
 * A bike's features packed into one int, one bit per feature.
 * A flag may combine several features, such as LIGHTS | GPS.
 */
public final class BikeFeatures {

    public static final int LIGHTS = 1;          // bit 0
    public static final int BASKET = 1 << 1;     // bit 1
    public static final int CHILD_SEAT = 1 << 2; // bit 2
    public static final int GPS = 1 << 3;        // bit 3
    public static final int BELL = 1 << 4;       // bit 4

    private BikeFeatures() {
    }

    /** True when the bike has EVERY feature in flag. */
    public static boolean has(int features, int flag) {
        // TODO 1: keep only the bits of flag with &, then check that all of them survived.
        return false;
    }

    /** The features with every bit of flag turned on. */
    public static int add(int features, int flag) {
        // TODO 2: turn the bits of flag on.
        return features;
    }

    /** The features with every bit of flag turned off. Bits that were already off stay off. */
    public static int remove(int features, int flag) {
        // TODO 3: turn the bits of flag off. Use & with the flipped flag (~flag).
        return features;
    }

    /** The features with every bit of flag flipped. */
    public static int toggle(int features, int flag) {
        // TODO 4: flip the bits of flag.
        return features;
    }

    /**
     * Every combination of add-ons whose total price fits the budget, as masks in increasing order.
     * Bit i of a mask means add-on i is chosen. The empty combination, mask 0, costs 0.
     */
    public static List<Integer> addOnCombos(long[] pricesCents, long budgetCents) {
        // TODO 5: loop mask from 0 while mask < (1 << n). For each mask, add up the prices
        // of the add-ons whose bit is set, and keep the mask when the total is within budget.
        return new ArrayList<>();
    }
}

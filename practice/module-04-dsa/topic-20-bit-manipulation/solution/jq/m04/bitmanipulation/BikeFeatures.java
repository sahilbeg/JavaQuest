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
        return (features & flag) == flag;
    }

    /** The features with every bit of flag turned on. */
    public static int add(int features, int flag) {
        return features | flag;
    }

    /** The features with every bit of flag turned off. Bits that were already off stay off. */
    public static int remove(int features, int flag) {
        return features & ~flag;
    }

    /** The features with every bit of flag flipped. */
    public static int toggle(int features, int flag) {
        return features ^ flag;
    }

    /**
     * Every combination of add-ons whose total price fits the budget, as masks in increasing order.
     * Bit i of a mask means add-on i is chosen. The empty combination, mask 0, costs 0.
     */
    public static List<Integer> addOnCombos(long[] pricesCents, long budgetCents) {
        List<Integer> combos = new ArrayList<>();
        int n = pricesCents.length;
        for (int mask = 0; mask < (1 << n); mask++) {
            long total = 0;
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    total += pricesCents[i];
                }
            }
            if (total <= budgetCents) {
                combos.add(mask);
            }
        }
        return combos;
    }
}

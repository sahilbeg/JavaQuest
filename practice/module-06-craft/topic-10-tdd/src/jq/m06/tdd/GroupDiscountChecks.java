package jq.m06.tdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GroupDiscountChecks {

    public static void checkSmallGroupsPayFullFare(GroupDiscount discount) {
        // TODO 1: assert that 1 rider and 2 riders pay the full fare
    }

    public static void checkThreeOrFourGetTwentyPercentOff(GroupDiscount discount) {
        // TODO 2: assert that 3 riders and 4 riders pay 80 percent of the fare
    }

    public static void checkFiveOrMoreGetThirtyPercentOff(GroupDiscount discount) {
        // TODO 3: assert that 5 riders and a bigger group pay 70 percent of the fare
    }

    public static void checkRoundsDownToWholeCents(GroupDiscount discount) {
        // TODO 4: pick a fare where the discount leaves a fraction of a cent, and assert it rounds down
    }

    public static void checkRejectsGroupSizeBelowOne(GroupDiscount discount) {
        // TODO 5: use assertThrows to check that 0 and a negative group size throw IllegalArgumentException
    }
}

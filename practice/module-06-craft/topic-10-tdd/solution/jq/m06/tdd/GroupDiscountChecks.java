package jq.m06.tdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GroupDiscountChecks {

    public static void checkSmallGroupsPayFullFare(GroupDiscount discount) {
        assertEquals(1000, discount.discountedCents(1000, 1), "a solo rider pays the full fare");
        assertEquals(1000, discount.discountedCents(1000, 2), "two riders pay the full fare");
    }

    public static void checkThreeOrFourGetTwentyPercentOff(GroupDiscount discount) {
        assertEquals(800, discount.discountedCents(1000, 3), "three riders get 20 percent off");
        assertEquals(800, discount.discountedCents(1000, 4), "four riders get 20 percent off");
    }

    public static void checkFiveOrMoreGetThirtyPercentOff(GroupDiscount discount) {
        assertEquals(700, discount.discountedCents(1000, 5), "five riders get 30 percent off");
        assertEquals(700, discount.discountedCents(1000, 12), "twelve riders get 30 percent off");
    }

    public static void checkRoundsDownToWholeCents(GroupDiscount discount) {
        assertEquals(987, discount.discountedCents(1234, 3), "80 percent of 1234 is 987.2, rounded down to 987");
        assertEquals(863, discount.discountedCents(1234, 5), "70 percent of 1234 is 863.8, rounded down to 863");
    }

    public static void checkRejectsGroupSizeBelowOne(GroupDiscount discount) {
        assertThrows(IllegalArgumentException.class, () -> discount.discountedCents(1000, 0),
                "a group of 0 riders makes no sense");
        assertThrows(IllegalArgumentException.class, () -> discount.discountedCents(1000, -3),
                "a negative group size makes no sense");
    }
}

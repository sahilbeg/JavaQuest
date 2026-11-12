package jq.m06.junit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** A safety net for any FareCalculator. Each check throws an AssertionError when the fare is wrong. */
public class FareChecks {

    public static void checkBasicClassic(FareCalculator calc) {
        // Worked example: arrange the inputs, act by calling fareCents, assert the result.
        long fare = calc.fareCents("classic", 10, "BASIC");
        assertEquals(250, fare, "BASIC, classic, 10 minutes: 100 unlock + 10 x 15 = 250 cents");
        assertEquals(100, calc.fareCents("classic", 0, "BASIC"),
                "BASIC, classic, 0 minutes: only the 100 cent unlock fee");
    }

    public static void checkMembershipFreeMinutes(FareCalculator calc) {
        assertAll(
                () -> assertEquals(0, calc.fareCents("classic", 30, "PLUS"),
                        "PLUS, 30 minutes: all free, no unlock fee"),
                () -> assertEquals(0, calc.fareCents("classic", 45, "PRO"),
                        "PRO, 45 minutes: still inside the 60 free minutes"),
                () -> assertEquals(0, calc.fareCents("classic", 60, "PRO"),
                        "PRO, 60 minutes: the last free minute"));
    }

    public static void checkAfterFreeMinutes(FareCalculator calc) {
        assertAll(
                () -> assertEquals(15, calc.fareCents("classic", 31, "PLUS"),
                        "PLUS, 31 minutes: one paid minute at 15 cents"),
                () -> assertEquals(150, calc.fareCents("classic", 40, "PLUS"),
                        "PLUS, 40 minutes: 10 paid minutes at 15 cents"),
                () -> assertEquals(15, calc.fareCents("classic", 61, "PRO"),
                        "PRO, 61 minutes: one paid minute at 15 cents"));
    }

    public static void checkEbikeAndCargo(FareCalculator calc) {
        assertAll(
                () -> assertEquals(400, calc.fareCents("ebike", 10, "BASIC"),
                        "BASIC, e-bike, 10 minutes: 100 + 10 x 30 = 400 cents"),
                () -> assertEquals(350, calc.fareCents("cargo", 10, "BASIC"),
                        "BASIC, cargo, 10 minutes: 100 + 10 x 25 = 350 cents"));
    }

    public static void checkRejectsNegativeMinutes(FareCalculator calc) {
        assertThrows(IllegalArgumentException.class,
                () -> calc.fareCents("classic", -1, "BASIC"),
                "a ride of -1 minutes is impossible, so fareCents should throw IllegalArgumentException");
    }
}

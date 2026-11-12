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
        // TODO 1: also check the edge case of a 0 minute BASIC classic ride (only the unlock fee)
    }

    public static void checkMembershipFreeMinutes(FareCalculator calc) {
        // TODO 2: check that PLUS and PRO riders pay 0 cents while they are inside their free minutes
    }

    public static void checkAfterFreeMinutes(FareCalculator calc) {
        // TODO 3: check the first paid minute just after the free minutes end, for PLUS and for PRO
    }

    public static void checkEbikeAndCargo(FareCalculator calc) {
        // TODO 4: check a BASIC e-bike ride and a BASIC cargo ride
    }

    public static void checkRejectsNegativeMinutes(FareCalculator calc) {
        // TODO 5: check that -1 minutes throws IllegalArgumentException (use assertThrows)
    }
}

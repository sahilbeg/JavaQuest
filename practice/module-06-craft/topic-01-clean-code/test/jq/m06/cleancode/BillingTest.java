package jq.m06.cleancode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BillingTest {

    @Test
    @DisplayName("The enums carry the prices and membership rules")
    void enumFields() {
        assertEquals(15, BikeType.CLASSIC.centsPerMinute(), "BikeType.CLASSIC.centsPerMinute() should be 15");
        assertEquals(30, BikeType.EBIKE.centsPerMinute(), "BikeType.EBIKE.centsPerMinute() should be 30");
        assertEquals(25, BikeType.CARGO.centsPerMinute(), "BikeType.CARGO.centsPerMinute() should be 25");
        assertEquals(0, Membership.BASIC.freeMinutes(), "Membership.BASIC.freeMinutes() should be 0");
        assertEquals(30, Membership.PLUS.freeMinutes(), "Membership.PLUS.freeMinutes() should be 30");
        assertEquals(60, Membership.PRO.freeMinutes(), "Membership.PRO.freeMinutes() should be 60");
        assertTrue(Membership.BASIC.paysUnlockFee(), "BASIC riders pay the unlock fee, so paysUnlockFee() should be true");
        assertTrue(!Membership.PLUS.paysUnlockFee() && !Membership.PRO.paysUnlockFee(),
                "PLUS and PRO riders skip the unlock fee, so paysUnlockFee() should be false for both");
    }

    @Test
    @DisplayName("BASIC riders pay the unlock fee plus every minute")
    void basicFares() {
        assertEquals(250, Billing.fareCents(BikeType.CLASSIC, 10, Membership.BASIC),
                "classic, 10 minutes, BASIC should be 250 cents: unlock 100 plus 10 x 15");
        assertEquals(400, Billing.fareCents(BikeType.EBIKE, 10, Membership.BASIC),
                "e-bike, 10 minutes, BASIC should be 400 cents: unlock 100 plus 10 x 30");
        assertEquals(350, Billing.fareCents(BikeType.CARGO, 10, Membership.BASIC),
                "cargo, 10 minutes, BASIC should be 350 cents: unlock 100 plus 10 x 25");
    }

    @Test
    @DisplayName("PLUS and PRO riders get free minutes and no unlock fee")
    void memberFares() {
        assertEquals(0, Billing.fareCents(BikeType.EBIKE, 30, Membership.PLUS),
                "e-bike, 30 minutes, PLUS should be free: all 30 minutes are free and there is no unlock fee");
        assertEquals(150, Billing.fareCents(BikeType.CLASSIC, 40, Membership.PLUS),
                "classic, 40 minutes, PLUS should be 150 cents: 10 paid minutes x 15");
        assertEquals(0, Billing.fareCents(BikeType.CARGO, 60, Membership.PRO),
                "cargo, 60 minutes, PRO should be free: PRO gets 60 free minutes");
        assertEquals(300, Billing.fareCents(BikeType.EBIKE, 70, Membership.PRO),
                "e-bike, 70 minutes, PRO should be 300 cents: 10 paid minutes x 30");
        assertEquals(250, Billing.fareCents(BikeType.CARGO, 70, Membership.PRO),
                "cargo, 70 minutes, PRO should be 250 cents: 10 paid minutes x 25");
    }

    @Test
    @DisplayName("The old calc gives the same answer as fareCents for every combination")
    void calcAgrees() {
        int[] minuteValues = {0, 10, 30, 45, 61, 90};
        for (int t = 0; t < 3; t++) {
            for (int k = 0; k < 3; k++) {
                for (int m : minuteValues) {
                    BikeType type = BikeType.values()[t];
                    Membership membership = Membership.values()[k];
                    assertEquals(Billing.fareCents(type, m, membership), Billing.calc(t, m, k),
                            "calc(" + t + ", " + m + ", " + k + ") should equal fareCents(" + type + ", " + m + ", "
                                    + membership + "). Make calc a one-line call to fareCents (and finish fareCents first)");
                }
            }
        }
    }

    @Test
    @DisplayName("calc is marked @Deprecated so new code stops using it")
    void calcDeprecated() throws Exception {
        Method calc = Billing.class.getDeclaredMethod("calc", int.class, int.class, int.class);
        assertTrue(calc.isAnnotationPresent(Deprecated.class),
                "put @Deprecated on the line above calc, so callers see it is the old way");
    }

    @Test
    @DisplayName("Negative minutes are rejected with a clear message")
    void negativeMinutes() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Billing.fareCents(BikeType.CLASSIC, -5, Membership.BASIC),
                "fareCents with -5 minutes should throw IllegalArgumentException");
        assertTrue(e.getMessage() != null && e.getMessage().contains("-5"),
                "the exception message should include the bad value -5, so whoever reads it knows what went wrong");
    }
}

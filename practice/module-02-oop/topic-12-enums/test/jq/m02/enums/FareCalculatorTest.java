package jq.m02.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FareCalculatorTest {

    @Test
    @DisplayName("Each bike type knows its price per minute")
    void bikePrices() {
        assertEquals(0.15, BikeType.CLASSIC.pricePerMinute(), 0.001,
                "CLASSIC.pricePerMinute() should be 0.15. Store the price in the constructor and return it");
        assertEquals(0.30, BikeType.EBIKE.pricePerMinute(), 0.001,
                "EBIKE.pricePerMinute() should be 0.30");
        assertEquals(0.25, BikeType.CARGO.pricePerMinute(), 0.001,
                "CARGO.pricePerMinute() should be 0.25");
    }

    @Test
    @DisplayName("Each membership level knows its free minutes and whether it pays the unlock fee")
    void membershipRules() {
        assertEquals(0, Membership.BASIC.freeMinutes(), "BASIC.freeMinutes() should be 0");
        assertEquals(30, Membership.PLUS.freeMinutes(), "PLUS.freeMinutes() should be 30");
        assertEquals(60, Membership.PRO.freeMinutes(), "PRO.freeMinutes() should be 60");
        assertTrue(Membership.BASIC.paysUnlockFee(), "BASIC.paysUnlockFee() should be true");
        assertFalse(Membership.PLUS.paysUnlockFee(), "PLUS.paysUnlockFee() should be false");
        assertFalse(Membership.PRO.paysUnlockFee(), "PRO.paysUnlockFee() should be false");
    }

    @Test
    @DisplayName("BASIC riders pay the unlock fee plus every minute")
    void basicFare() {
        assertEquals(2.5, FareCalculator.fare(BikeType.CLASSIC, Membership.BASIC, 10), 0.001,
                "fare(CLASSIC, BASIC, 10) should be 2.5: unlock fee 1.00 plus 10 minutes at 0.15");
        assertEquals(13.0, FareCalculator.fare(BikeType.EBIKE, Membership.BASIC, 40), 0.001,
                "fare(EBIKE, BASIC, 40) should be 13.0: unlock fee 1.00 plus 40 minutes at 0.30");
    }

    @Test
    @DisplayName("PLUS riders get 30 free minutes and no unlock fee")
    void plusFare() {
        assertEquals(0.0, FareCalculator.fare(BikeType.EBIKE, Membership.PLUS, 25), 0.001,
                "fare(EBIKE, PLUS, 25) should be 0.0: all 25 minutes are inside the 30 free minutes");
        assertEquals(3.0, FareCalculator.fare(BikeType.EBIKE, Membership.PLUS, 40), 0.001,
                "fare(EBIKE, PLUS, 40) should be 3.0: 10 paid minutes at 0.30, no unlock fee");
    }

    @Test
    @DisplayName("PRO riders get 60 free minutes and no unlock fee")
    void proFare() {
        assertEquals(0.0, FareCalculator.fare(BikeType.CARGO, Membership.PRO, 60), 0.001,
                "fare(CARGO, PRO, 60) should be 0.0: exactly 60 free minutes");
        assertEquals(5.0, FareCalculator.fare(BikeType.CARGO, Membership.PRO, 80), 0.001,
                "fare(CARGO, PRO, 80) should be 5.0: 20 paid minutes at 0.25, no unlock fee");
    }
}

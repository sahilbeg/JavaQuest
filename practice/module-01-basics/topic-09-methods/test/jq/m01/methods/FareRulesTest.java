package jq.m01.methods;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FareRulesTest {

    @Test
    @DisplayName("pricePerMinute knows the rate for every bike type")
    void pricePerMinute() {
        assertEquals(0.15, FareRules.pricePerMinute("classic"), 0.001,
                "pricePerMinute(\"classic\") should be 0.15");
        assertEquals(0.30, FareRules.pricePerMinute("ebike"), 0.001,
                "pricePerMinute(\"ebike\") should be 0.30");
        assertEquals(0.25, FareRules.pricePerMinute("cargo"), 0.001,
                "pricePerMinute(\"cargo\") should be 0.25");
        assertEquals(0.0, FareRules.pricePerMinute("scooter"), 0.001,
                "pricePerMinute(\"scooter\") should be 0, because Spoke has no scooters");
    }

    @Test
    @DisplayName("unlockFee charges non-members 1.00 and members nothing")
    void unlockFee() {
        assertEquals(1.00, FareRules.unlockFee(false), 0.001,
                "unlockFee(false) should be 1.00 for a rider without a membership");
        assertEquals(0.0, FareRules.unlockFee(true), 0.001,
                "unlockFee(true) should be 0, because members pay no unlock fee");
    }

    @Test
    @DisplayName("A non-member pays the unlock fee plus every minute")
    void nonMemberFare() {
        assertEquals(7.0, FareRules.fare("ebike", 20, false), 0.001,
                "fare(\"ebike\", 20, false) should be 7.0: unlock fee 1.00 plus 20 minutes at 0.30");
        assertEquals(3.5, FareRules.fare("cargo", 10, false), 0.001,
                "fare(\"cargo\", 10, false) should be 3.5: unlock fee 1.00 plus 10 minutes at 0.25");
    }

    @Test
    @DisplayName("A member rides the first 30 minutes free and then pays per minute")
    void memberFare() {
        assertEquals(0.0, FareRules.fare("ebike", 25, true), 0.001,
                "fare(\"ebike\", 25, true) should be 0: members ride their first 30 minutes free");
        assertEquals(0.0, FareRules.fare("classic", 30, true), 0.001,
                "fare(\"classic\", 30, true) should be 0: exactly 30 minutes is still free");
        assertEquals(2.5, FareRules.fare("cargo", 40, true), 0.001,
                "fare(\"cargo\", 40, true) should be 2.5: 10 minutes after the free 30, at 0.25, and no unlock fee");
    }

    @Test
    @DisplayName("fare(minutes) gives a classic non-member fare")
    void overloadedFare() {
        assertEquals(2.5, FareRules.fare(10), 0.001,
                "fare(10) should be 2.5: unlock fee 1.00 plus 10 minutes at 0.15");
        assertEquals(1.0, FareRules.fare(0), 0.001,
                "fare(0) should be 1.0: the unlock fee alone");
    }
}

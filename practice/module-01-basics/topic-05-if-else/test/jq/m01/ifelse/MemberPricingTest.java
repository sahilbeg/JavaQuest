package jq.m01.ifelse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberPricingTest {

    @Test
    @DisplayName("Only a member riding 30 minutes or less rides free")
    void memberShortRideIsFree() {
        assertTrue(MemberPricing.isFreeRide(true, 12),
                "isFreeRide(true, 12) should be true: members ride free for their first 30 minutes");
        assertTrue(MemberPricing.isFreeRide(true, 30),
                "isFreeRide(true, 30) should be true: exactly 30 minutes is still free, so use <=");
        assertFalse(MemberPricing.isFreeRide(true, 31),
                "isFreeRide(true, 31) should be false: the free time ends after 30 minutes");
        assertFalse(MemberPricing.isFreeRide(false, 5),
                "isFreeRide(false, 5) should be false: only members ride free, so check member && ...");
    }

    @Test
    @DisplayName("A non-member pays the unlock fee plus 0.15 per minute")
    void nonMemberFare() {
        assertEquals(2.80, MemberPricing.fare(false, 12), 0.001,
                "fare(false, 12) should be 2.80: unlock fee 1.00 plus 12 minutes at 0.15");
        assertEquals(1.00, MemberPricing.fare(false, 0), 0.001,
                "fare(false, 0) should be 1.00: non-members always pay the unlock fee");
    }

    @Test
    @DisplayName("A member pays nothing for 30 minutes, then 0.15 per extra minute")
    void memberFare() {
        assertEquals(0.0, MemberPricing.fare(true, 12), 0.001,
                "fare(true, 12) should be 0.00: the first 30 minutes are free for members");
        assertEquals(0.0, MemberPricing.fare(true, 30), 0.001,
                "fare(true, 30) should be 0.00: exactly 30 minutes is still free");
        assertEquals(1.80, MemberPricing.fare(true, 42), 0.001,
                "fare(true, 42) should be 1.80: 12 extra minutes at 0.15, and members pay no unlock fee");
    }

    @Test
    @DisplayName("rideLabel sorts rides into short, normal and long")
    void rideLabels() {
        assertEquals("short", MemberPricing.rideLabel(14),
                "rideLabel(14) should be \"short\": anything under 15 minutes is short");
        assertEquals("normal", MemberPricing.rideLabel(15),
                "rideLabel(15) should be \"normal\": normal starts at exactly 15 minutes");
        assertEquals("normal", MemberPricing.rideLabel(59),
                "rideLabel(59) should be \"normal\": normal goes up to 59 minutes");
        assertEquals("long", MemberPricing.rideLabel(60),
                "rideLabel(60) should be \"long\": long starts at exactly 60 minutes");
    }
}

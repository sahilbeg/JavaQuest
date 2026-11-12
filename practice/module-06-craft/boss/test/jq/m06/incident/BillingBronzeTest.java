package jq.m06.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BillingBronzeTest {

    @Test
    @DisplayName("Bronze: PRO and PLUS riders pay no unlock fee")
    void unlockFee() {
        Ride shortRide = new Ride("B-1042", "ebike", 10);
        assertEquals(0, Billing.fareCents(shortRide, Membership.PRO), "PRO: 10 minutes are free and there is no unlock fee");
        assertEquals(0, Billing.fareCents(shortRide, Membership.PLUS), "PLUS: 10 minutes are free and there is no unlock fee");
        assertEquals(400, Billing.fareCents(shortRide, Membership.BASIC), "BASIC: $1.00 unlock fee plus 10 x 30 cents");
    }
}

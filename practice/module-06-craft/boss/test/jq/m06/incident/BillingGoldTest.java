package jq.m06.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BillingGoldTest {

    @Test
    @DisplayName("Gold: the invoice matches and the caller's list keeps its order")
    void invoiceWithoutSideEffects() {
        List<Ride> rides = new ArrayList<>(List.of(
                new Ride("B-3001", "cargo", 45),
                new Ride("B-1042", "ebike", 70),
                new Ride("B-2001", "classic", 3)));
        List<Ride> before = List.copyOf(rides);
        for (Membership membership : Membership.values()) {
            assertEquals(LegacyBilling.invoice(rides, membership), Billing.invoice(rides, membership),
                    membership + ": the invoice text differs from LegacyBilling");
        }
        assertEquals(before, rides, "invoice must not reorder the list it was given; sort a copy instead");
    }
}

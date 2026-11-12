package jq.m06.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BillingSilverTest {

    @Test
    @DisplayName("Silver: every fare matches the legacy code to the cent")
    void matchesLegacy() {
        for (Membership membership : Membership.values()) {
            for (String type : List.of("classic", "ebike", "cargo")) {
                for (int minutes = 0; minutes <= 90; minutes++) {
                    Ride ride = new Ride("B-1042", type, minutes);
                    assertEquals(LegacyBilling.fareCents(ride, membership), Billing.fareCents(ride, membership),
                            membership + ", " + type + ", " + minutes + " min: the new fare differs from LegacyBilling");
                }
            }
        }
    }
}

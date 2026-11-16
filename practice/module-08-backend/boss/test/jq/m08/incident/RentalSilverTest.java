package jq.m08.incident;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RentalSilverTest extends IncidentTestBase {

    @Test
    @DisplayName("Silver: a declined payment leaves the bike docked and free")
    void declinedPaymentRollsBack() {
        assertThrows(PaymentDeclinedException.class, () -> rentalService.startRental("r-broke", "B-1042"),
                "the decline should still reach the caller, so the app can ask for another card");
        assertStillDocked("B-1042", "harbor", "the bank said no, so the reservation must be undone");
    }

    @Test
    @DisplayName("Silver: after a decline, another rider can take the same bike")
    void bikeCanBeRentedAfterDecline() throws Exception {
        assertThrows(PaymentDeclinedException.class, () -> rentalService.startRental("r-broke", "B-3001"));
        rentalService.startRental("r-17", "B-3001");
        assertRented("B-3001", "r-17");
    }
}

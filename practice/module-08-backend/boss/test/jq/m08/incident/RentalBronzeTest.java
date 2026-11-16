package jq.m08.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RentalBronzeTest extends IncidentTestBase {

    @Test
    @DisplayName("Bronze: a normal unlock rents the bike and records the rental")
    void normalUnlock() throws Exception {
        Rental rental = rentalService.startRental("r-17", "B-1042");
        assertEquals("harbor", rental.getStartStation(), "the rental should remember the station the bike left");
        assertRented("B-1042", "r-17");
    }

    @Test
    @DisplayName("Bronze: when the lock jams, nothing is left half-finished")
    void jammedLockRollsBack() {
        assertThrows(LockJammedException.class, () -> rentalService.startRental("r-17", "B-2002"),
                "the jam should still reach the caller, so the app can tell the rider");
        assertStillDocked("B-2002", "harbor", "the lock never opened, so the rental must not exist");
    }
}

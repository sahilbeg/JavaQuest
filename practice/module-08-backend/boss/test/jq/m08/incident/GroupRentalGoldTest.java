package jq.m08.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GroupRentalGoldTest extends IncidentTestBase {

    @Test
    @DisplayName("Gold: a jammed bike in a family ride is undone, and the other bikes still go")
    void oneJamDoesNotSpoilTheGroup() {
        GroupResult result = groupRentals.unlockAll("r-17", List.of("B-2001", "B-2002", "B-2003"));

        assertEquals(List.of("B-2001", "B-2003"), result.unlocked(), "the two bikes whose locks opened should be unlocked");
        assertEquals(List.of("B-2002"), result.failed(), "only the jammed bike should fail");
        assertRented("B-2001", "r-17");
        assertRented("B-2003", "r-17");
        assertStillDocked("B-2002", "harbor", "its lock jammed, so its own steps must roll back");
    }

    @Test
    @DisplayName("Gold: a declined card undoes every bike of the group")
    void declinedCardUndoesEveryBike() {
        GroupResult result = groupRentals.unlockAll("r-broke", List.of("B-2001", "B-2003"));

        assertEquals(List.of(), result.unlocked(), "no bike should unlock for a declined card");
        assertStillDocked("B-2001", "harbor", "the bank said no");
        assertStillDocked("B-2003", "harbor", "the bank said no");
    }
}

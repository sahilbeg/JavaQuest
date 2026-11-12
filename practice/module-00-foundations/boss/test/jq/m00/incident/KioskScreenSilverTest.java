package jq.m00.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KioskScreenSilverTest {

    @Test
    @DisplayName("Silver: the welcome and the tap instruction are on separate lines")
    void twoLines() {
        var lines = ScreenOutput.linesPrintedByMain();
        assertTrue(lines.size() >= 2, "main should print at least two lines, but printed " + lines.size());
        assertEquals("Welcome to Spoke!", lines.get(0), "line 1 should be the welcome");
        assertEquals("Tap your card to unlock a bike.", lines.get(1), "line 2 should be the tap instruction");
    }
}

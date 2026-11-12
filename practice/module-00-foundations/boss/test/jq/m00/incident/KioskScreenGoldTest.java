package jq.m00.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KioskScreenGoldTest {

    @Test
    @DisplayName("Gold: the screen shows exactly three lines, ending with the station name")
    void fullScreen() {
        assertEquals(List.of("Welcome to Spoke!", "Tap your card to unlock a bike.", "Riverside Station"),
                ScreenOutput.linesPrintedByMain(),
                "the kiosk should print exactly these three lines");
    }
}

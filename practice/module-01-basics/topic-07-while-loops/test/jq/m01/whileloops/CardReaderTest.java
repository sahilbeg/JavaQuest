package jq.m01.whileloops;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CardReaderTest {

    @Test
    @DisplayName("A card that works on the first tap uses 1 tap")
    void firstTapWorks() {
        assertEquals(1, CardReader.tapsNeeded(1, 3),
                "tapsNeeded(1, 3) should be 1: the very first tap worked. Does your counter start at 1?");
    }

    @Test
    @DisplayName("A card that works on the last allowed tap uses all 3 taps")
    void lastTapWorks() {
        assertEquals(3, CardReader.tapsNeeded(3, 3),
                "tapsNeeded(3, 3) should be 3. Use <= maxTaps in the loop condition so tap 3 still gets a try");
    }

    @Test
    @DisplayName("A card that never works within the limit gives -1")
    void neverWorks() {
        assertEquals(-1, CardReader.tapsNeeded(4, 3),
                "tapsNeeded(4, 3) should be -1: the card needs tap 4 but the kiosk stops after 3");
    }

    @Test
    @DisplayName("The station fills after the right number of whole minutes")
    void minutesUntilFull() {
        assertEquals(3, CardReader.minutesUntilFull(12, 5),
                "minutesUntilFull(12, 5) should be 3: 12 free docks go to 7, then 2, then below 0");
        assertEquals(3, CardReader.minutesUntilFull(12, 4),
                "minutesUntilFull(12, 4) should be 3: 12 goes to 8, then 4, then exactly 0");
        assertEquals(0, CardReader.minutesUntilFull(0, 3),
                "minutesUntilFull(0, 3) should be 0: a full station needs no minutes");
    }

    @Test
    @DisplayName("No arriving bikes gives -1 instead of looping forever")
    void noArrivals() {
        assertEquals(-1, CardReader.minutesUntilFull(12, 0),
                "minutesUntilFull(12, 0) should be -1: with no bikes arriving the station never fills");
    }

    @Test
    @DisplayName("countdown builds the numbers then the word go")
    void countdown() {
        assertEquals("3 2 1 go", CardReader.countdown(3),
                "countdown(3) should be \"3 2 1 go\": each number, then a space, then go at the end");
        assertEquals("go", CardReader.countdown(0),
                "countdown(0) should be just \"go\": the loop body never runs");
    }
}

package jq.m01.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NightFaresGoldTest {

    @Test
    @DisplayName("Gold: the receipt line shows times, minutes and fare")
    void midnightReceipt() {
        assertEquals("23:50-00:20 30 min $5.50", NightFares.receipt(1430, 20, "classic", false),
                "format: HH:MM-HH:MM, then minutes, then the fare with two decimals");
    }

    @Test
    @DisplayName("Gold: early times keep their leading zeros")
    void leadingZeros() {
        assertEquals("09:05-09:45 40 min $3.00", NightFares.receipt(545, 585, "ebike", true),
                "9:05 must print as 09:05, and a 40 minute member e-bike ride costs 3.00");
    }
}

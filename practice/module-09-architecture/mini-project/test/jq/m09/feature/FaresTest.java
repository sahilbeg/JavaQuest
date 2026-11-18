package jq.m09.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FaresTest {

    @Test
    @DisplayName("The fare is 100 cents to unlock plus the per-minute price times the minutes")
    void fareRule() {
        assertEquals(400, Fares.expectedCents(BikeType.CLASSIC, 20), "CLASSIC, 20 minutes: 100 + 15 x 20");
        assertEquals(460, Fares.expectedCents(BikeType.EBIKE, 12), "EBIKE, 12 minutes: 100 + 30 x 12");
        assertEquals(350, Fares.expectedCents(BikeType.CARGO, 10), "CARGO, 10 minutes: 100 + 25 x 10");
        assertEquals(100, Fares.expectedCents(BikeType.EBIKE, 0), "a 0-minute ride still costs the 100-cent unlock");
    }
}

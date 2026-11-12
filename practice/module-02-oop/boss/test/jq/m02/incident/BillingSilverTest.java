package jq.m02.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BillingSilverTest {

    @Test
    @DisplayName("Silver: a cargo bike in a Bike variable is billed at 0.25 per minute")
    void cargoThroughBikeVariable() {
        Bike bike = new CargoBike("B-3001");
        assertEquals(6.00, bike.fare(20), 0.001, "20 cargo minutes: 1.00 + 20 x 0.25 = 6.00");
        assertEquals(0.25, bike.pricePerMinute(), 0.001, "pricePerMinute() of a cargo bike should be 0.25");
    }
}

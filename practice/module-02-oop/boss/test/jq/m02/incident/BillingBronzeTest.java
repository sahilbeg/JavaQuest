package jq.m02.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BillingBronzeTest {

    @Test
    @DisplayName("Bronze: an e-bike in a Bike variable is billed at 0.30 per minute")
    void eBikeThroughBikeVariable() {
        Bike bike = new EBike("B-2001");
        assertEquals(4.00, bike.fare(10), 0.001, "10 e-bike minutes: 1.00 + 10 x 0.30 = 4.00");
    }

    @Test
    @DisplayName("Bronze: a classic bike still costs 0.15 per minute")
    void classicUnchanged() {
        assertEquals(2.50, new Bike("B-1001").fare(10), 0.001, "10 classic minutes: 1.00 + 10 x 0.15 = 2.50");
    }
}

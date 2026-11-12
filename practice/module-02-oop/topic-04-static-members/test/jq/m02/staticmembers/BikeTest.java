package jq.m02.staticmembers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BikeTest {

    @BeforeEach
    void startFresh() {
        Bike.resetCounter();
    }

    @Test
    @DisplayName("The price constants hold Spoke's real prices")
    void constants() {
        assertEquals(1.00, Prices.UNLOCK_FEE, 0.001, "Prices.UNLOCK_FEE should be 1.00");
        assertEquals(0.15, Prices.CLASSIC, 0.001, "Prices.CLASSIC should be 0.15 per minute");
        assertEquals(0.30, Prices.EBIKE, 0.001, "Prices.EBIKE should be 0.30 per minute");
        assertEquals(0.25, Prices.CARGO, 0.001, "Prices.CARGO should be 0.25 per minute");
    }

    @Test
    @DisplayName("Prices.fare adds the unlock fee to the minutes")
    void fare() {
        assertEquals(2.5, Prices.fare(0.15, 10), 0.001,
                "fare(0.15, 10) should be 2.5: unlock fee 1.00 plus 10 minutes at 0.15");
        assertEquals(7.0, Prices.fare(0.30, 20), 0.001,
                "fare(0.30, 20) should be 7.0: unlock fee 1.00 plus 20 minutes at 0.30");
        assertEquals(1.0, Prices.fare(0.25, 0), 0.001,
                "fare(0.25, 0) should be 1.0: just the unlock fee");
    }

    @Test
    @DisplayName("Each new bike gets the next id, starting at B-1001")
    void idsCountUp() {
        Bike first = new Bike("classic");
        Bike second = new Bike("ebike");
        Bike third = new Bike("cargo");
        assertEquals("B-1001", first.getId(), "the first bike should get B-1001: nextId adds 1 to counter, then uses 1000 + counter");
        assertEquals("B-1002", second.getId(), "the second bike should get B-1002");
        assertEquals("B-1003", third.getId(), "the third bike should get B-1003");
    }

    @Test
    @DisplayName("Each bike keeps its own type")
    void typeIsStored() {
        Bike a = new Bike("classic");
        Bike b = new Bike("cargo");
        assertEquals("classic", a.getType(), "new Bike(\"classic\").getType() should return \"classic\"");
        assertEquals("cargo", b.getType(), "new Bike(\"cargo\").getType() should return \"cargo\"");
    }

    @Test
    @DisplayName("Bike.created counts every bike built")
    void createdCounts() {
        assertEquals(0, Bike.created(), "before any bike is built, Bike.created() should be 0");
        new Bike("classic");
        new Bike("ebike");
        assertEquals(2, Bike.created(), "after building 2 bikes, Bike.created() should be 2");
    }
}

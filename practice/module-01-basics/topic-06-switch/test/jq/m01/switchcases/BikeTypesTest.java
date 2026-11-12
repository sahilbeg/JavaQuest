package jq.m01.switchcases;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BikeTypesTest {

    @Test
    @DisplayName("Each known bike type has its own price per minute")
    void knownPrices() {
        assertEquals(0.15, BikeTypes.pricePerMinute("classic"), 0.001,
                "pricePerMinute(\"classic\") should be 0.15");
        assertEquals(0.30, BikeTypes.pricePerMinute("ebike"), 0.001,
                "pricePerMinute(\"ebike\") should be 0.30");
        assertEquals(0.25, BikeTypes.pricePerMinute("cargo"), 0.001,
                "pricePerMinute(\"cargo\") should be 0.25");
    }

    @Test
    @DisplayName("An unknown bike type gets the price -1")
    void unknownPrice() {
        assertEquals(-1, BikeTypes.pricePerMinute("scooter"), 0.001,
                "pricePerMinute(\"scooter\") should be -1: use default for any type you do not know");
        assertEquals(-1, BikeTypes.pricePerMinute("Classic"), 0.001,
                "pricePerMinute(\"Classic\") should be -1: a String switch cares about capital letters");
    }

    @Test
    @DisplayName("The fare is the unlock fee plus minutes times the bike's price")
    void fares() {
        assertEquals(4.00, BikeTypes.fare("classic", 20), 0.001,
                "fare(\"classic\", 20) should be 4.00: unlock fee 1.00 plus 20 minutes at 0.15");
        assertEquals(4.00, BikeTypes.fare("ebike", 10), 0.001,
                "fare(\"ebike\", 10) should be 4.00: unlock fee 1.00 plus 10 minutes at 0.30");
        assertEquals(6.00, BikeTypes.fare("cargo", 20), 0.001,
                "fare(\"cargo\", 20) should be 6.00: unlock fee 1.00 plus 20 minutes at 0.25");
    }

    @Test
    @DisplayName("The fare for an unknown bike type is -1")
    void unknownFare() {
        assertEquals(-1, BikeTypes.fare("unicycle", 20), 0.001,
                "fare(\"unicycle\", 20) should be -1: check whether pricePerMinute gave back -1 before adding the unlock fee");
    }

    @Test
    @DisplayName("Docks get the right colour for their row")
    void dockColors() {
        assertEquals("blue", BikeTypes.dockColor(1), "dockColor(1) should be \"blue\": docks 1, 2 and 3 are blue");
        assertEquals("blue", BikeTypes.dockColor(3), "dockColor(3) should be \"blue\": docks 1, 2 and 3 are blue");
        assertEquals("green", BikeTypes.dockColor(4), "dockColor(4) should be \"green\": docks 4, 5 and 6 are green");
        assertEquals("green", BikeTypes.dockColor(6), "dockColor(6) should be \"green\": docks 4, 5 and 6 are green");
        assertEquals("grey", BikeTypes.dockColor(12), "dockColor(12) should be \"grey\": every other dock is grey");
    }
}

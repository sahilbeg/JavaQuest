package jq.m04.bitmanipulation;

import static jq.m04.bitmanipulation.BikeFeatures.BASKET;
import static jq.m04.bitmanipulation.BikeFeatures.BELL;
import static jq.m04.bitmanipulation.BikeFeatures.CHILD_SEAT;
import static jq.m04.bitmanipulation.BikeFeatures.GPS;
import static jq.m04.bitmanipulation.BikeFeatures.LIGHTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BikeFeaturesTest {

    @Test
    @DisplayName("add turns features on, and has sees them")
    void addAndHas() {
        int bike = BikeFeatures.add(0, LIGHTS);
        bike = BikeFeatures.add(bike, GPS);
        assertEquals(LIGHTS | GPS, bike, "add(0, LIGHTS) then add(.., GPS) should give LIGHTS | GPS, which is 9 (binary 1001)");
        assertTrue(BikeFeatures.has(bike, LIGHTS), "has(bike, LIGHTS) should be true after adding LIGHTS");
        assertTrue(BikeFeatures.has(bike, GPS), "has(bike, GPS) should be true after adding GPS");
        assertFalse(BikeFeatures.has(bike, BASKET), "has(bike, BASKET) should be false: BASKET was never added");
        assertEquals(bike, BikeFeatures.add(bike, LIGHTS), "Adding LIGHTS again should change nothing");
    }

    @Test
    @DisplayName("has with a combined flag needs every feature in it, not just one")
    void hasNeedsAll() {
        int bike = LIGHTS | BASKET;
        assertTrue(BikeFeatures.has(bike, LIGHTS | BASKET), "has(LIGHTS | BASKET, LIGHTS | BASKET) should be true: both are there");
        assertFalse(BikeFeatures.has(bike, LIGHTS | GPS),
                "has(LIGHTS | BASKET, LIGHTS | GPS) should be false: the bike has lights but no GPS. "
                        + "Check (features & flag) == flag, not != 0");
        assertFalse(BikeFeatures.has(0, BELL), "A bike with no features has no bell");
    }

    @Test
    @DisplayName("remove turns features off and leaves an absent feature off")
    void removeClears() {
        int bike = LIGHTS | BASKET | GPS;
        assertEquals(LIGHTS | GPS, BikeFeatures.remove(bike, BASKET), "remove(LIGHTS | BASKET | GPS, BASKET) should leave LIGHTS | GPS");
        assertEquals(LIGHTS | BASKET | GPS, BikeFeatures.remove(bike, CHILD_SEAT),
                "Removing CHILD_SEAT, which the bike does not have, should change nothing. "
                        + "Use features & ~flag, not features ^ flag");
        assertEquals(BASKET, BikeFeatures.remove(bike, LIGHTS | GPS), "remove(.., LIGHTS | GPS) should turn off both at once");
    }

    @Test
    @DisplayName("toggle flips features, and toggling twice gives back the start")
    void toggleFlips() {
        assertEquals(LIGHTS | BASKET, BikeFeatures.toggle(LIGHTS, BASKET), "toggle(LIGHTS, BASKET) should turn BASKET on");
        assertEquals(0, BikeFeatures.toggle(LIGHTS, LIGHTS), "toggle(LIGHTS, LIGHTS) should turn LIGHTS off, leaving 0");
        int bike = LIGHTS | GPS;
        assertEquals(bike, BikeFeatures.toggle(BikeFeatures.toggle(bike, BELL | GPS), BELL | GPS),
                "Toggling the same flag twice should give back the bike you started with");
    }

    @Test
    @DisplayName("addOnCombos lists every combination within budget, as masks in increasing order")
    void combosWithinBudget() {
        long[] prices = {300, 500, 200}; // add-on 0 costs 300 cents, add-on 1 costs 500, add-on 2 costs 200
        List<Integer> combos = assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> BikeFeatures.addOnCombos(prices, 600),
                "addOnCombos took too long. Loop mask from 0 up to, but not including, 1 << n");
        assertEquals(List.of(0, 1, 2, 4, 5), combos,
                "With a 600-cent budget the combos are: none (0), add-on 0 (300), add-on 1 (500), add-on 2 (200) "
                        + "and add-ons 0 and 2 (500). Masks 3, 6 and 7 cost too much");
    }

    @Test
    @DisplayName("addOnCombos with no add-ons, or no money, still offers the empty combination")
    void combosEdgeCases() {
        assertEquals(List.of(0), BikeFeatures.addOnCombos(new long[0], 1000),
                "With no add-ons there is exactly one combination: none, mask 0");
        assertEquals(List.of(0), BikeFeatures.addOnCombos(new long[] {100, 250}, 0),
                "With a budget of 0 cents only the empty combination, mask 0, fits");
        assertEquals(1 << 10, assertTimeoutPreemptively(Duration.ofSeconds(2),
                        () -> BikeFeatures.addOnCombos(new long[10], 0).size(),
                        "addOnCombos took too long for 10 free add-ons"),
                "Ten free add-ons give 2 to the power 10 = 1024 combinations, all within any budget");
    }
}

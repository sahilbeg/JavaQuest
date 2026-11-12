package jq.m03.functionalinterfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PromotionsTest {

    private final Ride ebike = new Ride("B-1042", "Harbor", "ebike", 20);
    private final Ride classic = new Ride("B-2001", "Riverside", "classic", 10);
    private final Ride cargo = new Ride("B-3300", "Uptown", "cargo", 45);

    @Test
    @DisplayName("Promotion is marked as a functional interface")
    void promotionIsAnnotated() {
        assertTrue(Promotion.class.isAnnotationPresent(FunctionalInterface.class),
                "Put @FunctionalInterface on the line above 'public interface Promotion'");
    }

    @Test
    @DisplayName("halfPrice cuts any fare in half")
    void halfPriceHalves() {
        assertEquals(3.5, Promotions.halfPrice().apply(classic, 7.0), 0.001,
                "halfPrice() on a 7.00 fare should give 3.50");
    }

    @Test
    @DisplayName("eBikeDiscount only changes e-bike fares")
    void eBikeDiscountOnlyForEbikes() {
        Promotion twentyOff = Promotions.eBikeDiscount(20);
        assertEquals(5.6, twentyOff.apply(ebike, 7.0), 0.001,
                "eBikeDiscount(20) on an ebike fare of 7.00 should give 5.60, which is 20 percent off");
        assertEquals(2.5, twentyOff.apply(classic, 2.5), 0.001,
                "eBikeDiscount(20) on a classic ride should leave the fare at 2.50");
    }

    @Test
    @DisplayName("applyAll runs every promotion in order")
    void applyAllChains() {
        List<Promotion> promotions = List.of(Promotions.eBikeDiscount(20), Promotions.halfPrice());
        assertEquals(2.8, Promotions.applyAll(ebike, 7.0, promotions), 0.001,
                "7.00 with 20 percent off is 5.60, then half price gives 2.80");
        assertEquals(7.0, Promotions.applyAll(ebike, 7.0, List.of()), 0.001,
                "With no promotions, applyAll should return the fare unchanged");
    }

    @Test
    @DisplayName("filter with longRide keeps only the long rides")
    void filterLongRides() {
        List<Ride> rides = List.of(ebike, classic, cargo);
        assertEquals(List.of(ebike, cargo), Promotions.filter(rides, Promotions.longRide(20)),
                "longRide(20) should keep rides of 20 minutes or more: B-1042 and B-3300, in that order");
        Predicate<Ride> shortRide = Promotions.longRide(20).negate();
        assertEquals(List.of(classic), Promotions.filter(rides, shortRide),
                "longRide(20).negate() should keep only the 10 minute ride B-2001");
    }

    @Test
    @DisplayName("label turns a ride into bike id at station")
    void labelText() {
        assertEquals("B-1042 at Harbor", Promotions.label().apply(ebike),
                "label() should give \"B-1042 at Harbor\" for bike B-1042 at Harbor");
    }
}

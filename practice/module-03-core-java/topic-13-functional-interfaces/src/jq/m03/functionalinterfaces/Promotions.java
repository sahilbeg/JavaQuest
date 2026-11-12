package jq.m03.functionalinterfaces;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class Promotions {

    public static Promotion halfPrice() {
        // TODO 2: return a Promotion that gives back half of the fare
        return (ride, fare) -> fare;
    }

    public static Promotion eBikeDiscount(double percent) {
        // TODO 3: take percent off the fare for "ebike" rides, and leave other rides unchanged
        return (ride, fare) -> fare;
    }

    public static double applyAll(Ride ride, double fare, List<Promotion> promotions) {
        // TODO 4: run every promotion in order, feeding each result into the next one
        return 0.0;
    }

    public static Predicate<Ride> longRide(int minutes) {
        // TODO 5: return a Predicate that is true when the ride lasted at least this many minutes
        return ride -> false;
    }

    public static List<Ride> filter(List<Ride> rides, Predicate<Ride> test) {
        // TODO 6: loop over rides and keep the ones where test.test(ride) is true
        return new ArrayList<>();
    }

    public static Function<Ride, String> label() {
        // TODO 7: return a Function that turns a ride into text like "B-1042 at Harbor"
        return ride -> "";
    }
}

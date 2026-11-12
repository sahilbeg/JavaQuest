package jq.m03.functionalinterfaces;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class Promotions {

    public static Promotion halfPrice() {
        return (ride, fare) -> fare / 2;
    }

    public static Promotion eBikeDiscount(double percent) {
        return (ride, fare) -> {
            if (ride.type().equals("ebike")) {
                return fare * (100 - percent) / 100;
            }
            return fare;
        };
    }

    public static double applyAll(Ride ride, double fare, List<Promotion> promotions) {
        double result = fare;
        for (Promotion promotion : promotions) {
            result = promotion.apply(ride, result);
        }
        return result;
    }

    public static Predicate<Ride> longRide(int minutes) {
        return ride -> ride.minutes() >= minutes;
    }

    public static List<Ride> filter(List<Ride> rides, Predicate<Ride> test) {
        List<Ride> kept = new ArrayList<>();
        for (Ride ride : rides) {
            if (test.test(ride)) {
                kept.add(ride);
            }
        }
        return kept;
    }

    public static Function<Ride, String> label() {
        return ride -> ride.bikeId() + " at " + ride.station();
    }
}

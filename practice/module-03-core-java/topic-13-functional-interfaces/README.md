# Pluggable promotions

Marketing changes promotions every week. Make each promotion a small object you can plug in, so a new one needs no change to the fare code.

1. **TODO 1:** put `@FunctionalInterface` above `public interface Promotion` in `Promotion.java`.
2. **TODO 2:** make `halfPrice()` return a `Promotion` that gives back half of the fare.
3. **TODO 3:** make `eBikeDiscount(percent)` take `percent` percent off the fare when the ride type is `"ebike"`. Other rides keep their fare.
4. **TODO 4:** make `applyAll` run every promotion in the list, in order. Each promotion gets the fare the previous one returned.
5. **TODO 5:** make `longRide(minutes)` return a `Predicate<Ride>` that is true when the ride lasted at least `minutes` minutes.
6. **TODO 6:** make `filter` loop over the rides and keep the ones where `test.test(ride)` is true.
7. **TODO 7:** make `label()` return a `Function<Ride, String>` that gives text like `B-1042 at Harbor`.

Press **Submit** in JavaQuest, or just save the files in your IDE. The tests run on their own.

# Optional

Stop the app crashing when a rider looks up a retired bike. Finish `BikeLookup.java` so every lookup returns an `Optional` box.

1. **TODO 1:** `findStation` returns the bike's station in an `Optional`. Use `Optional.ofNullable(docks.get(bikeId))`, so a missing bike gives an empty box.
2. **TODO 2:** `stationOrUnknown` returns the station, or `unknown` when the bike is not in the map. Use `orElse`.
3. **TODO 3:** `longestRide` returns the ride with the most minutes. Use `stream().max(...)`. An empty list gives an empty box.
4. **TODO 4:** `longestRideLabel` returns a label like `B-1042: 80 min`, or `no rides` for an empty list. Use `map` and `orElse`.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

# Ride reports as streams

The report code is full of loops, and each loop has its own off-by-one bug. Rewrite each report as one stream pipeline.

1. **TODO 1:** make `eBikeRides` return only the rides whose type is `"ebike"`, in their original order.
2. **TODO 2:** make `totalMinutes` add up the minutes of every ride. Use `mapToInt` and `sum`.
3. **TODO 3:** make `stationsVisited` return each station once, sorted from A to Z.
4. **TODO 4:** make `topThreeLongest` return the bike ids of the three longest rides, longest first.
5. **TODO 5:** make `averageMinutes` return the average ride length, or `0.0` when there are no rides.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

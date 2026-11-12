# Comparable, Comparator, Iterator

Sort the ride report so support can find rides, and drop the rides that are over a limit.

1. **TODO 1:** in `Ride`, make `compareTo` order rides by minutes with `Integer.compare`.
2. **TODO 2:** in `ByMinutes`, make `compare` order two rides by minutes.
3. **TODO 3:** in `ByStationThenMinutes`, compare the stations first. On a tie, compare the minutes.
4. **TODO 4:** make `sortByMinutes` sort the copy in natural order with `Collections.sort`.
5. **TODO 5:** make `sortByStationThenMinutes` sort the copy with a `ByStationThenMinutes` object.
6. **TODO 6:** make `longestFirst` sort the copy with `new ByMinutes().reversed()`.
7. **TODO 7:** make `removeAbove` walk the list with an `Iterator`. Remove every ride over `maxMinutes` with `it.remove()`, and return how many you removed.

The three sort methods return a new list and leave the original alone.

Press **Submit** in JavaQuest, or just save the files in your IDE. The tests run on their own.

# Streams: grouping, reducing, flatMap

Build the numbers behind the board's monthly dashboard in `Dashboard.java`. Each method is one stream pipeline.

1. **TODO 1:** `ridesPerStation` returns how many rides started at each station. Use `Collectors.groupingBy` with `Collectors.counting()`.
2. **TODO 2:** `minutesPerType` returns the total minutes for each bike type. Use `Collectors.summingInt(Ride::minutes)` as the downstream collector.
3. **TODO 3:** `longOrShort` splits the rides with `Collectors.partitioningBy`. Rides longer than `limit` go under `true`. The rest go under `false`.
4. **TODO 4:** `allBikes` gets one list per station. Flatten them with `flatMap`, keep the bike ids, remove repeats, and sort them.
5. **TODO 5:** `summary` returns each station once, in alphabetical order, joined like `Harbor, Old Mill, Riverside`.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

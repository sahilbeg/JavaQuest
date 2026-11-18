# Weighted shortest paths: Dijkstra

Kakashi says the route finder sends riders the way with the fewest stops, even when it takes longer. Build `RideTimes.java`, a lane map with riding minutes, and find the fastest route with Dijkstra's algorithm.

The class already has two records: `Lane(to, minutes)` for one lane out of a station, and `Visit(station, minutes)` for one entry in the priority queue.

1. **TODO 1:** `addLane(a, b, minutes)` adds a lane that works both ways, with the same minutes each way. Add `new Lane(b, minutes)` to `a`'s list and `new Lane(a, minutes)` to `b`'s list.
   - Negative minutes throw `IllegalArgumentException`. A 0-minute lane is allowed.
   - Two lanes may join the same two stations. Keep both. The faster one wins on its own.
2. **TODO 2:** `search(from, best, parent)` runs Dijkstra from `from`. It fills `best` (station to fewest minutes) and `parent` (station to the station just before it on the fastest route).
   - Use a `PriorityQueue<Visit>` ordered by minutes, then by station name A to Z.
   - When you poll a `Visit` whose minutes are more than the station's `best`, skip it. It is stale.
   - Update a station only when the new total is **strictly smaller** than its best so far, or it has no best yet.
   - A station with no lanes has no list. Use `getOrDefault`.
3. **TODO 3:** `fastestMinutes(from, to)` returns the fewest minutes from `from` to `to`.
   - From a station to itself is always 0 minutes, even for a station that is not on the map. So `fastestMinutes("Airport", "Airport")` is 0.
   - Otherwise, return **-1** when `to` cannot be reached, including when either station is not on the map.
4. **TODO 4:** `fastestRoute(from, to)` returns the stations of the fastest route as a list, with both ends included.
   - Return an empty list when `to` cannot be reached.
   - From a station to itself, the route is just that station, even for a station that is not on the map: `fastestRoute("Airport", "Airport")` is `[Airport]`.

**Tie rule.** When two routes take the same minutes, the queue order and the strict `<` decide. Example: Harbor and Old Mill are both 2 minutes from Riverside, and both are 3 minutes from Uptown. Harbor comes first A to Z, so it is polled first and becomes Uptown's parent. Old Mill's total is also 5, which is not strictly smaller, so the route stays `[Riverside, Harbor, Uptown]`.

**Speed.** One check builds a line of 30,000 docks. Take the next station from the `PriorityQueue`. Scanning every station to find the closest one is too slow.

Build the search yourself. Do not use a graph library.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.

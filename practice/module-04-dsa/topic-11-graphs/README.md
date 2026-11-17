# Graphs: BFS and DFS

Shizune wants the app to tell a rider the route with the fewest stops between two stations. Build `LaneMap.java`, a graph of bike lanes.

1. **TODO 1:** `addLane(a, b)` joins two stations. A lane works both ways, so add `b` to `a`'s list and `a` to `b`'s list. Create a new `ArrayList` the first time you meet a station.
2. **TODO 2:** `neighbours(station)` returns the stations one lane away, in the order the lanes were added. Return an empty list for a station with no lanes.
3. **TODO 3:** `reachable(start)` returns every station you can ride to from `start`, including `start`, sorted A to Z. Use depth-first search (DFS) or breadth-first search (BFS) with a visited set.
4. **TODO 4:** `fewestStops(from, to)` returns the route as a list, from `from` to `to` with both ends included. Use BFS with a queue, a visited set and a parent map.
   - If `from` equals `to`, the route is just that one station.
   - If no lanes lead to `to`, return an empty list.
   - When two routes have the same number of stops, BFS order decides. Neighbours are checked in the order their lanes were added, so the neighbour added first wins.

Build the search yourself. Do not use a graph library.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

# Tries and union-find

Two fixes for the Spoke app. Both live in package `jq.m04.triesunionfind`.

## Part 1: `StationTrie`, the search box

Build station-name autocomplete as a trie. Each `Node` already has a `TreeMap<Character, Node> children` and a `String name`. Keep the names in the nodes. Do not add a `List`, `Set` or `Map` field to `StationTrie` itself.

Matching ignores case. Store each character as `Character.toLowerCase(c)`, and look it up the same way. When a whole name ends at a node, set that node's `name` to the name exactly as it was first inserted.

1. **TODO 1:** `insert(String name)` returns `true` for a new name and adds 1 to `size()`. If the name is already there, ignoring case, it returns `false` and keeps the first spelling.
2. **TODO 2:** `contains(String name)` is `true` only for a whole stored name. A prefix is not enough: with only `Riverside` stored, `contains("River")` is `false`.
3. **TODO 3:** `startsWith(String prefix)` is `true` when at least one stored name starts with `prefix`. A whole name counts as a prefix of itself. The empty prefix `""` is `true` only when the trie holds at least one name.
4. **TODO 4:** `complete(String prefix)` returns every stored name that starts with `prefix`, in A to Z order of the lowercased letters. Names keep their inserted spelling. A space comes before letters, so `River Park` comes before `Riverside`. A name comes before longer names that start with it: `Harbor` before `Harbor East`. No match gives an empty list. `complete("")` lists every name.

Names are never `null` or empty.

## Part 2: `LaneNetwork`, which stations bike lanes join

Stations are numbered `0` to `stations - 1`. The tests use 0 Riverside, 1 Old Mill, 2 Harbor, 3 Uptown, 4 Depot. Build a union-find on the `parent` and `size` arrays. Station numbers passed in are always valid.

5. **TODO 5:** the constructor makes every station its own group: `parent[i] = i`, `size[i] = 1`, and `groups` equals the number of stations.
6. **TODO 6:** `find(station)` returns the root of the station's group. Use path compression: point every station on the path straight at the root.
7. **TODO 7:** `connect(a, b)` joins the two groups and returns `true`. If `a` and `b` are already in one group, including `a == b`, it returns `false` and changes nothing. Link the two roots, never the two stations. Hang the smaller group's root under the larger group's root (union by size), and take 1 off `groups`.
8. **TODO 8:** `connected(a, b)` is `true` when both have the same root. A station is always connected to itself.
9. **TODO 9:** `groupSize(station)` returns how many stations share its group, itself included.
10. **TODO 10:** `static int firstRedundantLane(int stations, int[][] lanes)`. Each lane is `{a, b}`. Connect the lanes in order on a new network. Return the index of the first lane whose stations were already connected. A repeated lane and a lane from a station to itself both count. Return `-1` if no lane is redundant, or if there are no lanes.
11. **TODO 11:** `static long cheapestNetworkCost(int stations, int[][] lanes)` uses Kruskal's algorithm. Each lane is `{a, b, costCents}`. Sort a copy of the lanes by cost, cheapest first. Keep a lane only when it joins two groups, and add its cost. Return the total when every station ends in one group. Return `-1` when the lanes cannot join every station. With 0 or 1 stations the answer is `0`.

A test joins 100,000 stations to station 0, once as `connect(0, i)` and once as `connect(i, 0)`. Each run must finish within 2 seconds. Without at least one of union by size or path compression, one of the two orders builds a long chain and runs too long. Use both.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

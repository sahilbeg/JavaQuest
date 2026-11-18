# Dynamic programming patterns

Tsunade's planning desk has six questions, and every one is a dynamic programming pattern. Fill in the six methods of `PlanningDesk.java`. Each one fills a table from the smallest case upward. Plain recursion that tries every choice is too slow for the big tests, which have a 2-second limit.

1. **TODO 1:** `maxRidersWithoutNeighbours(int[] riders)` returns the most riders a promo team can reach. `riders[i]` is the number of riders at station `i` along one road, and every count is 0 or more. The team may not visit two stations that sit next to each other in the array. No stations means 0. Example: `{5, 1, 1, 5}` gives 10, and `{2, 1, 1, 2}` gives 4.
2. **TODO 2:** `fewestCoins(int[] coins, int amount)` returns the fewest coins that add up to exactly `amount` cents. Every coin value is positive, and each coin can be used as many times as you like. `fewestCoins({1, 3, 4}, 6)` is 2 (3 + 3), not 3 (4 + 1 + 1). An amount of 0 needs 0 coins.
3. **TODO 3:** `fewestCoins` returns -1 when the amount cannot be made at all, for example 3 cents from coins `{5, 10}`.
4. **TODO 4:** `waysToPay(int[] coins, int amount)` returns, as a `long`, the number of different mixes of coins that add up to `amount`. Coin values are positive and different from each other, and each can be used any number of times. Order does not matter: 1 + 2 and 2 + 1 are the same mix. There is exactly 1 way to pay 0 (no coins). An amount that cannot be made has 0 ways.
5. **TODO 5:** `routesThroughGrid(boolean[][] blocked)` returns 0 for an empty grid (no rows or no columns).
6. **TODO 6:** `routesThroughGrid` counts, as a `long`, the routes from the top-left square to the bottom-right square of a rectangular street grid. Each step moves one square right or one square down. `blocked[r][c]` is `true` for a closed square, which no route may enter. A closed start or end square means 0 routes. A 1 by 1 open grid has 1 route. An open 30 by 30 grid has 30067266499541040 routes, so an `int` is too small.
7. **TODO 7:** `bestTruckLoad(int[] weights, int[] values, int capacity)` returns the highest total value of crates that fit on the truck. Crate `i` weighs `weights[i]` kilograms (at least 1) and is worth `values[i]`. Each crate goes on the truck at most once, and the total weight must be at most `capacity`. Capacity 0, no crates, or crates that are all too heavy give 0.
8. **TODO 8:** `editDistance(String a, String b)` returns the fewest single-letter edits that turn `a` into `b`. An edit inserts one letter, deletes one letter, or replaces one letter with another. Letters are case-sensitive. `editDistance("Harbour", "Harbor")` is 1, and from `""` to `"Depot"` is 5.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

# Backtracking: trying every choice

Itachi's riders and drivers want every option listed: every set of promos, every order to visit stations, every way to fill the rebalancing truck. Finish `LoadPlanner.java` with the choose, explore, un-choose template.

The public methods are already wired to private helpers. You fill in the helpers. Every method returns a new list of lists, and never `null`.

**Order rule.** Tests check the exact order, which the template in the lesson gives for free:

- Inside each answer, items keep the order of their position in the input (strings) or go from small to large (numbers).
- The answers are listed in dictionary order: compare the first item, then the second, and so on. Strings compare by their position in the input, not alphabetically, so `Harbor` comes before `Depot` when Harbor is given first. Numbers compare by size. A list that is the start of a longer one comes first, so `[]` comes before `[WEEKEND]`, which comes before `[WEEKEND, STUDENT]`.

1. **TODO 1:** in `subsets`, record a **copy** of `current` in `result` first: `result.add(new ArrayList<>(current))`. Adding `current` itself records the same list object again and again, and every set ends up empty.
2. **TODO 2:** then, for each index `i` from `start` to the end, add `promos.get(i)` to `current`, call `subsets(promos, i + 1, current, result)`, and remove the last item of `current`. `promoSets(["WEEKEND", "STUDENT", "FIRSTRIDE"])` gives `[], [WEEKEND], [WEEKEND, STUDENT], [WEEKEND, STUDENT, FIRSTRIDE], [WEEKEND, FIRSTRIDE], [STUDENT], [STUDENT, FIRSTRIDE], [FIRSTRIDE]`. No promos gives one empty set, `[[]]`.
3. **TODO 3:** in `orders`, when `current` holds every station, record a copy and return.
4. **TODO 4:** otherwise loop `i` from 0 over the stations in their given order. Skip any station whose `used[i]` is true. Mark it used, add it, explore, then remove it and mark it unused again. `visitOrders(["Harbor", "Uptown", "Depot"])` gives `[Harbor, Uptown, Depot], [Harbor, Depot, Uptown], [Uptown, Harbor, Depot], [Uptown, Depot, Harbor], [Depot, Harbor, Uptown], [Depot, Uptown, Harbor]`. No stations gives one empty order, `[[]]`.
5. **TODO 5:** `loadsForCapacity(bikeSlots, capacity)` lists every set of bikes whose slots add up to exactly `capacity`. Each bike is used at most once. Sort a **copy** (`bikeSlots.clone()`), because the caller's array must keep its order. All values are 1 or more, and `capacity` is 1 or more.
6. **TODO 6:** in `loads`, when `remaining` is 0, record a copy of `current` and return.
7. **TODO 7:** loop `i` from `start`. Two bikes can take the same number of slots, but the same load must appear only once: skip `sorted[i]` when `i > start` and `sorted[i] == sorted[i - 1]`. Prune: `break` as soon as `sorted[i] > remaining`, because every later bike is at least as big. Then choose, explore with `i + 1`, un-choose. `{2, 3, 2, 5, 3}` with capacity 7 gives `[[2, 2, 3], [2, 5]]`. When nothing fits, return an empty list. A test with 30 bikes and a 2-second limit fails without pruning.
8. **TODO 8:** `refillPlans(crateSizes, bikesNeeded)` lists every way to make exactly `bikesNeeded` from crate sizes, where **each size can be used again and again**. The sizes are all different and 1 or more. In `plans`, do what `loads` does, but explore with `i`, not `i + 1`, so the same size can be picked again. `{5, 2, 3}` with 8 gives `[[2, 2, 2, 2], [2, 3, 3], [3, 5]]`. When nothing works, return an empty list.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

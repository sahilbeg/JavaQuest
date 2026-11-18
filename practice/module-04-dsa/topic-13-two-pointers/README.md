# Two pointers, sliding windows and prefix sums

Kakashi's ops dashboard tries every start and every end of every stretch, so a day of minute-by-minute data takes ages. Rewrite `Stretches.java` so each answer comes from one pass over the data.

Every method is `static`. Some tests use 200,000 values and a 2-second limit, so two nested loops over the data will time out.

1. **TODO 1:** `pairWithTotal(sortedFares, target)` gets fares in cents, sorted from small to large. Return the indexes `{i, j}` of two different fares that add up to `target`, with `i < j`. If several pairs work, any one is fine. If none works, return an empty array (`new int[0]`). One fare can never pair with itself, so an array with a single fare has no pair. Use two pointers that start at the two ends.
2. **TODO 2:** `bestWindow(ridesPerMinute, k)` returns 0 when `k < 1` or `k` is bigger than the array length. An empty array therefore gives 0.
3. **TODO 3:** otherwise `bestWindow` returns the biggest total of any `k` values in a row. All values are 0 or more. Slide a window of size `k`: add the value coming in and subtract the value going out.
4. **TODO 4:** `longestStretchWithin(minutes, limit)` returns the length of the longest run of values in a row whose total is **at most** `limit` (equal is allowed). All values are 0 or more, and `limit` is 0 or more. If no single value fits, return 0. An empty array gives 0. Use a window that grows on the right and shrinks from the left.
5. **TODO 5:** `prefixSums(values)` returns a `long[]` one cell longer than `values`. `prefix[0]` is 0 and `prefix[i + 1]` is the total of `values[0]` to `values[i]`. For `{4, 0, 7, 2, 9}` it is `{0, 4, 4, 11, 13, 22}`. For an empty array it is `{0}`. Totals can pass the `int` limit, so add them as `long`.
6. **TODO 6:** `rangeTotal(prefix, from, to)` throws `IllegalArgumentException` when `from < 0`, when `from > to`, or when `to` is past the last value (`to >= prefix.length - 1`).
7. **TODO 7:** otherwise `rangeTotal` returns the total of the original values from index `from` to index `to`, **both included**, with one subtraction: `prefix[to + 1] - prefix[from]`.
8. **TODO 8:** `countStretchesSummingTo(values, k)` returns how many runs of values in a row add up to exactly `k`, as a `long`. Values can be negative (bikes arriving minus bikes leaving), so a sliding window does not work. Keep a `Map<Long, Integer>` of running totals seen so far, seeded with `0L -> 1`, so runs that start at index 0 are counted. An empty array gives 0.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

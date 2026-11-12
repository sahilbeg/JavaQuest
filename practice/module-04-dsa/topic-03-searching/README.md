# Linear and binary search

Sam's support tool reads 50,000 retired bike ids one by one. Build the searches in `Search.java` so it can jump to the answer.

Do not call `Arrays.binarySearch` or `Collections.binarySearch`. The point is to write the search yourself, and a test counts your loop steps.

1. **TODO 1:** `linear(values, target)` checks each item from the start. It returns the index of the first match, or `-1`.
2. **TODO 2:** `binary(sorted, target)` resets `steps` to 0, then loops while `low <= high`. Add 1 to `steps` on every pass.
3. **TODO 3:** inside the loop, use `mid = low + (high - low) / 2`. Return `mid` on a match, or move `low` or `high` past `mid`. Return `-1` when the range is empty. Searching 1,024 items must take at most 11 steps.
4. **TODO 4:** `binaryRecursive(sorted, target)` calls the helper `search` with the whole range.
5. **TODO 5:** `search(sorted, target, low, high)` returns `-1` for an empty range. Otherwise it checks `mid` and calls itself on one half.
6. **TODO 6:** `firstAtLeast(sortedMinutes, minutes)` returns the index of the first value that is `>= minutes`. If no value is big enough, return the array length. Use a binary search, so it stays O(log n).
7. **TODO 7:** `firstAtLeast` also counts its loop passes in `steps`, like `binary`. Set `steps` to 0 at the start and add 1 on every pass. Searching 1,024 items must take at most 11 steps.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

# Complexity and Big-O

The unlock check at Harbor scans every ride of the year. First you measure how loops grow by counting their steps. Then you write a slow check and a fast check for "is this bike already out?".

Open `StepCounter.java`. Each method returns how many loop steps it performed.

1. **TODO 1:** `countLinear(n)` loops `n` times and returns the step count. `countLinear(10)` is `10`.
2. **TODO 2:** `countNested(n)` runs a loop of `n` inside a loop of `n`. `countNested(10)` is `100`.
3. **TODO 3:** `countHalving(n)` halves `n` with `n = n / 2` while `n` is greater than 1, counting each halving. `countHalving(8)` is `3`. For `n` of 1 or less it returns `0`.
4. **TODO 4:** `bigO(name)` returns the Big-O label for each name, spelled exactly like this:

   | name | returns |
   |---|---|
   | `"linear"` | `"O(n)"` |
   | `"nested"` | `"O(n^2)"` |
   | `"halving"` | `"O(log n)"` |
   | `"hashset"` | `"O(1)"` |

Open `BikeCheck.java`.

5. **TODO 5:** `containsSlow(ids, id)` loops over the list and returns `true` when an id `equals` the one you want. Do not call `ids.contains`: the point is to write the O(n) loop yourself.
6. **TODO 6:** `containsFast(ids, id)` returns `ids.contains(id)` on the set. Do not loop over the set.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.

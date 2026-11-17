# Recursion

Shizune wants the total free docks along the overflow chain: Riverside, then Old Mill, then Harbor, then Uptown. You will write that, plus four warm-up methods, using recursion.

**Rule: no loops.** Write every method in `Recursion.java` by calling itself. No `for`, no `while`, no streams. The tests only check results, so this rule is on your honour. Each method needs a base case and a recursive case.

1. **TODO 1 and 2:** `factorial(n)` returns `n * (n - 1) * ... * 1` as a `long`. `factorial(0)` and `factorial(1)` are `1`. `factorial(5)` is `120`.
2. **TODO 3 and 4:** `sum(docks)` adds every number in the array. It calls the private helper `sumFrom(docks, 0)`. The helper returns `0` when `index` reaches `docks.length`. Otherwise it returns `docks[index]` plus the sum of the rest. `sum({3, 0, 5, 2})` is `10`.
3. **TODO 5:** `countDown(n)` returns text such as `"3 2 1 go"`. `countDown(0)` is `"go"`.
4. **TODO 6:** `reverse(s)` returns the text back to front. `reverse("B-1042")` is `"2401-B"`. The empty string reverses to itself.
5. **TODO 7:** `power(base, exp)` returns `base` multiplied by itself `exp` times. `power(2, 10)` is `1024` and `power(12, 0)` is `1`.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

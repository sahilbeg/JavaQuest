# Dynamic programming basics

Tsunade's pass pricing tool uses plain recursion, and it freezes on a 30-day month. Rewrite `Passes.java` so every answer comes from a table of small answers you already worked out.

1. **TODO 1:** `fib(n)` returns the n-th Fibonacci number: `fib(0) = 0`, `fib(1) = 1`, and each next one is the sum of the two before. Use a `long[]` table or a memo. `fib(80)` must come back at once.
2. **TODO 2:** `cheapestCover` returns 0 when `rideDays` is empty.
3. **TODO 3:** `cheapestCover(rideDays, dayPrice, weekPrice, monthPrice)` returns the cheapest total price of passes that covers every ride day. `rideDays` holds sorted day numbers from 1 to 365. A day pass covers 1 day, a week pass covers 7 days in a row, and a month pass covers 30 days in a row. Build a table `cost[d]`, the cheapest cover for all ride days up to day `d`.
4. **TODO 4:** `waysToDock(bikes)` counts the ways to dock a row of bikes using slots that hold 1 or 2 bikes. `waysToDock(0)` is 1, `waysToDock(2)` is 2 and `waysToDock(5)` is 8.

Each test has a time limit, so a plain recursive solution fails instead of hanging; use memoization or a table.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

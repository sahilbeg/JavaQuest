# Counting loops: for, break, continue

The kiosk screen needs a list of its docks, and Tsunade says: "I also want a fare total for a batch of rides." Fill in `DockList.java` with `for` loops.

1. **TODO 1:** `dockLine(docks)` returns the dock numbers from 1 to `docks`, separated by single spaces. `dockLine(5)` is `1 2 3 4 5`. There is no space at the end.
2. **TODO 2:** `evenDocks(docks)` returns only the even dock numbers, the same way. `evenDocks(12)` is `2 4 6 8 10 12`.
3. **TODO 3:** `totalClassicFare(rides, minutesEach)` adds one classic fare per ride: `1.00 + minutesEach * 0.15`. `totalClassicFare(3, 10)` is `7.5`.
4. **TODO 4 and 5:** `firstFreeDock(docks, takenUpTo)` returns the first dock number greater than `takenUpTo`. Use `break` to stop the loop when you find it. Return `-1` if every dock is taken.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

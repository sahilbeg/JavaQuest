# Directed graphs: order, cycles and grids

Jiraiya wants two tools for the repair crew. The first puts maintenance jobs in a safe order. The second reads a map of city blocks and works out the lane zones and the distance to the nearest dock.

## Part 1: `MaintenancePlan.java`

A rule `mustComeBefore(first, then)` is an arrow from `first` to `then`. It works in one direction only.

1. **TODO 1:** `addJob(job)` adds a job with no rules yet. Adding the same job twice changes nothing.
2. **TODO 2:** `mustComeBefore(first, then)` adds both jobs if they are new, then records the arrow. Adding the same rule twice counts once.
3. **TODO 3:** `waitingOn(job)` returns the job's in-degree: how many different jobs must come before it. A job that is not in the plan returns 0.
4. **TODO 4:** `order()` returns every job once, each one after all the jobs that must come before it. Use Kahn's algorithm.
   - **Tie rule:** when several jobs are ready at the same time, the smallest name goes next, the way `String.compareTo` orders them. For lowercase names like the ones in the checks, that is A to Z. A `PriorityQueue<String>` does this for you. A plain queue does not.
   - A plan with no jobs gives an empty list.
   - If the rules contain a cycle, no order exists. Throw `new IllegalStateException("cycle")`. You can spot it because Kahn's result has fewer jobs than the plan. A job that must come before itself is a cycle too.
5. **TODO 5:** `hasCycle()` returns `true` when the rules contain a cycle. Calling `order()` and catching the exception is fine.

## Part 2: `CityGrid.java`

The constructor takes the map as rows of text, all the same length. Each character is one city block:

- `#` a block with a bike lane
- `D` a dock, which also sits on a lane
- `.` a block with no lane

Row 0 is the top row and column 0 is the left column. A rider moves one block up, down, left or right, and only along lanes (`#` or `D`). Never diagonally.

6. **TODO 6:** `isLane(row, col)` returns `true` for `#` or `D`. It returns `false` for `.` and for any position outside the grid, such as row -1. It must never crash.
7. **TODO 7:** `laneZones()` counts the separate groups of lane cells. Two lane cells are in the same group when you can ride from one to the other. An empty grid, or a grid with no lanes, has 0 zones.
   - Use BFS or DFS with an `ArrayDeque` and a `boolean[][]` visited array. Do **not** use recursion: one test uses a 600 x 600 grid, and a recursive flood fill runs out of stack.
8. **TODO 8:** `distanceToDock()` returns an `int[rows()][cols()]` array. Each lane cell holds the fewest moves to the nearest dock. A dock holds 0. A `.` cell holds -1, and so does a lane cell with no lane route to any dock. A grid with no rows gives an empty array.
   - Use multi-source BFS: put every dock in the queue at distance 0 before the loop starts.

Build the searches yourself. Do not use a graph library.

Press **Submit** in JavaQuest, or just save the files in your IDE. The tests run on their own.

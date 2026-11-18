## Hook

In the last DP ticket you saved Tsunade's pass pricing tool. Now she has a whole pile of planning questions. Which stations should the promo team visit? How many coins should the kiosk hand back? Which crates go on the truck?

They look like six different puzzles. They are not. Almost every DP problem you will meet is one of a handful of patterns. Learn the patterns, and a scary new question turns into "oh, it's that one again".

## Concept

### The DP recipe

Every DP solution answers the same five questions. Answer them in words before you write any code.

1. **State:** what does one cell of the table mean? Say it as a full sentence.
2. **Transition:** how do you work out one cell from cells you already have?
3. **Base cases:** which cells do you know without any work?
4. **Answer cell:** which cell holds the final answer?
5. **Fill order:** in which order must you fill cells, so every cell you read is ready?

```callout jargon
The **state** is what one table cell means, like "`cost[d]` is the cheapest cover for every ride up to day d". The **transition** is the rule that builds a cell from earlier cells. A **base case** is a cell you fill in directly, with no rule.
```

In SPK-0412, the state was `cost[d]`. The transition picked the cheapest of three passes. The base case was `cost[0] = 0`. The answer cell was the last ride day, and the fill order was day 1 upward.

### Spotting a DP problem

The wording of a problem often gives it away. Look for these phrases:

| The question says | It usually means |
|---|---|
| "the number of ways", "how many routes" | Counting DP: add the ways from each choice |
| "the minimum", "the fewest", "the cheapest" | Min DP: take the smallest choice |
| "the maximum", "the most", "the longest" | Max DP: take the biggest choice |
| "can you reach", "is it possible" | Yes/no DP: a `boolean` table |

There is one more clue. Each choice you make changes what you can choose later. If you could decide each step on its own, you would not need DP at all.

### Pattern 1: take or skip

The promo team drives along one road of stations. `riders[i]` is how many riders wait at station `i`. The team may not visit two neighbouring stations, because they share a crowd. Which stations reach the most riders?

At each station there are two choices. **Skip** it: keep the best answer up to the station before. **Take** it: add its riders to the best answer from two stations back.

- State: `best[i]` is the most riders reachable using stations 0 to i.
- Transition: `best[i] = max(best[i - 1], best[i - 2] + riders[i])`.
- Base cases: before any station the answer is 0.
- Answer cell: the last station. Fill order: left to right.

```callout tip
Interviewers call this the "house robber" problem: a robber may not rob two houses next door. Same rule, different story.
```

### Watch take or skip run

Each cell only needs the two cells before it. So the code keeps just two numbers and shifts them along the road.

```animate
title: The promo team picks stations
kind: code
code: |
  int[] riders = {5, 1, 1, 5};
  int twoBack = 0;
  int oneBack = 0;
  for (int r : riders) {
      int best = Math.max(oneBack, twoBack + r);
      twoBack = oneBack;
      oneBack = best;
  }
  System.out.println(oneBack);
steps:
  - line: 1
    objects: { road: { type: "int[]", fields: { "0": 5, "1": 1, "2": 1, "3": 5 } } }
    vars: { riders: "@road" }
    say: Four stations on one road. The first and last have 5 riders each.
  - line: 3
    vars: { twoBack: 0, oneBack: 0 }
    say: Before any station, the best is 0. These two boxes are the base cases.
  - line: 5
    vars: { r: 5, best: 5 }
    say: "Station 0: skip gives 0, take gives 0 + 5. Take wins: 5."
  - line: 7
    vars: { twoBack: 0, oneBack: 5 }
    say: Shift along the road. oneBack now holds the best answer so far.
  - line: 5
    vars: { r: 1, best: 5 }
    say: "Station 1: skip keeps 5, take gives 0 + 1. Skip wins."
  - line: 7
    vars: { twoBack: 5, oneBack: 5 }
    say: Shift again.
  - line: 5
    vars: { r: 1, best: 6 }
    say: "Station 2: take gives 5 + 1 = 6, which beats 5."
  - line: 7
    vars: { twoBack: 5, oneBack: 6 }
    say: Shift again.
  - line: 5
    vars: { r: 5, best: 10 }
    say: "Station 3: take gives 5 + 5 = 10. That means stations 0 and 3."
  - line: 7
    vars: { twoBack: 6, oneBack: 10 }
    say: The road is done. oneBack holds the answer.
  - line: 9
    vars: { r: ~, best: ~ }
    out: "10"
    say: The most riders is 10. Picking every other station would only reach 6.
```

### Syntax: a rolling array

When each cell only reads the last few cells, you do not need the whole table. Keep only those few cells and roll them forward.

```java nocompile
int twoBack = 0;
int oneBack = 0;
for (int r : riders) {
    int best = Math.max(oneBack, twoBack + r);
    twoBack = oneBack;
    oneBack = best;
}
```

The full table costs O(n) memory. Two variables cost O(1). The time stays O(n), one step per station.

```callout jargon
A **rolling array** keeps only the cells the transition still needs, and overwrites old cells as the loop moves on. It saves memory but forgets the earlier answers.
```

### Pattern 2: unbounded choices

The kiosk gives change in coins of 1, 3 and 4 cents. Each coin value can be used again and again. That is what **unbounded** means here. What is the fewest number of coins for 6 cents?

- State: `fewest[a]` is the fewest coins that make exactly `a` cents.
- Transition: try every coin as the last coin. `fewest[a] = 1 + min(fewest[a - coin])`.
- Base case: `fewest[0] = 0`. Every other cell starts as "impossible".
- Answer cell: `fewest[amount]`. Fill order: 0 cents upward.

### Syntax: fewest coins

```java
static int fewestCoins(int[] coins, int amount) {
    int impossible = amount + 1;
    int[] fewest = new int[amount + 1];
    java.util.Arrays.fill(fewest, impossible);
    fewest[0] = 0;
    for (int a = 1; a <= amount; a++) {
        for (int coin : coins) {
            if (coin <= a && fewest[a - coin] + 1 < fewest[a]) {
                fewest[a] = fewest[a - coin] + 1;
            }
        }
    }
    return fewest[amount] == impossible ? -1 : fewest[amount];
}
```

`amount + 1` works as "impossible", because no real answer can use more coins than cents. At the end, a cell still at `amount + 1` means the amount cannot be made, so return -1. It costs O(amount × k) time for k coin values, and O(amount) memory.

### Why the biggest coin first fails

```callout jargon
A **greedy** method makes the choice that looks best right now, and never goes back to change it.
```

The greedy idea for change is "always hand over the biggest coin that fits". For 6 cents with coins 1, 3 and 4, greedy takes 4, then 1, then 1. That is 3 coins. But 3 + 3 is only 2 coins.

Greedy happens to work for real coin sets like 1, 5, 10 and 25. It fails for 1, 3 and 4. DP tries every last coin, so it never misses 3 + 3. The next ticket, SPK-0418, shows when greedy is safe.

### Counting ways: loop order matters

Now count the mixes of coins that make an amount. The transition adds instead of taking the minimum: `ways[a] += ways[a - coin]`. The base case is `ways[0] = 1`, because there is one way to pay nothing: use no coins.

The order of the two loops changes the meaning. Coins in the outer loop count **mixes**: 1 + 2 and 2 + 1 are one mix. Amounts in the outer loop count **orders**: 1 + 2 and 2 + 1 are two.

```java run
public class Main {
    static long mixes(int[] coins, int amount) {
        long[] ways = new long[amount + 1];
        ways[0] = 1;
        for (int coin : coins) {
            for (int a = coin; a <= amount; a++) {
                ways[a] += ways[a - coin];
            }
        }
        return ways[amount];
    }

    static long orders(int[] coins, int amount) {
        long[] ways = new long[amount + 1];
        ways[0] = 1;
        for (int a = 1; a <= amount; a++) {
            for (int coin : coins) {
                if (coin <= a) {
                    ways[a] += ways[a - coin];
                }
            }
        }
        return ways[amount];
    }

    public static void main(String[] args) {
        int[] coins = {1, 2, 5};
        System.out.println("Mixes of coins for 5: " + mixes(coins, 5));
        System.out.println("Orders of coins for 5: " + orders(coins, 5));
    }
}
```

With coins outside, each coin is added in one fixed order, so a mix can only be built one way. Both versions cost O(amount × k) time and O(amount) memory.

### Pattern 3: paths through a grid

A rider crosses a street grid from the top-left square to the bottom-right square. Each step goes right or down. Some squares are closed for roadworks. How many routes are there?

- State: `routes[r][c]` is the number of routes from the start to square (r, c).
- Transition: you arrive from above or from the left. `routes[r][c] = routes[r - 1][c] + routes[r][c - 1]`.
- Base case: the start square has 1 route. A closed square has 0.
- Answer cell: the bottom-right square. Fill order: row by row, left to right.

Here is a 3 by 3 grid with the middle closed (X):

| | col 0 | col 1 | col 2 |
|---|---|---|---|
| row 0 | 1 | 1 | 1 |
| row 1 | 1 | X (0) | 1 |
| row 2 | 1 | 1 | 2 |

It costs O(rows × cols) time, one step per square. A rolling array keeps only one row, so memory is O(cols). Route counts grow fast: an open 30 by 30 grid has about 3 × 10^16 routes, so use `long`.

### Pattern 4: the 0/1 knapsack

```callout jargon
The **knapsack** problem: you have items with a weight and a value, and a bag with a weight limit. Pick items for the most value. In **0/1 knapsack**, you take each item once or not at all.
```

The rebalancing truck carries crates of spare parts. Each crate has a weight and a value. The truck has a weight limit. Take or skip each crate, and get the most value on board.

- State: `best[i][w]` is the most value using the first i crates with at most w kilograms.
- Transition: skip crate i, or take it if it fits. `best[i][w] = max(best[i - 1][w], best[i - 1][w - weight] + value)`.
- Base cases: no crates, or capacity 0, is worth 0.
- Answer cell: all crates, full capacity. Fill order: one crate at a time.

This is take-or-skip again, with a second number in the state.

### Syntax: one row, filled backwards

```java nocompile
int[] best = new int[capacity + 1];
for (int i = 0; i < weights.length; i++) {
    for (int w = capacity; w >= weights[i]; w--) {
        best[w] = Math.max(best[w], best[w - weights[i]] + values[i]);
    }
}
```

The rolling row must be filled from the **top capacity down**. Then `best[w - weights[i]]` still holds the old value, from before this crate. Going up would read a cell that already used this crate, and add it twice.

It costs O(n × W) time for n crates and capacity W, and O(W) memory. If W were a billion, this table would be far too big.

### Pattern 5: two strings

A rider types "Harbour" into the app, but the station is called "Harbor". The app suggests the closest name. **Edit distance** counts the fewest edits from one text to another. An edit inserts, deletes or replaces one letter.

- State: `dist[i][j]` is the edits that turn the first i letters of `a` into the first j letters of `b`.
- Transition: if the letters match, `dist[i][j] = dist[i - 1][j - 1]`. If not, it is 1 plus the smallest of replace `dist[i - 1][j - 1]`, delete `dist[i - 1][j]` and insert `dist[i][j - 1]`.
- Base cases: `dist[i][0] = i` (delete everything) and `dist[0][j] = j` (insert everything).
- Answer cell: `dist[a.length()][b.length()]`. Fill order: row by row.

Turning "dok" into "dock" takes one edit, an inserted c:

| | "" | d | o | c | k |
|---|---|---|---|---|---|
| "" | 0 | 1 | 2 | 3 | 4 |
| d | 1 | 0 | 1 | 2 | 3 |
| o | 2 | 1 | 0 | 1 | 2 |
| k | 3 | 2 | 1 | 1 | 1 |

It costs O(n × m) time and memory for strings of length n and m. Each cell reads only the row above, so two rows are enough to get the number.

```callout tip
The **longest common subsequence** (LCS) uses the same grid. Matching letters add 1 to the diagonal. Otherwise take the bigger of the cell above and the cell to the left. Tools that show changes between two files are built on this idea.
```

### Pattern 6: longest increasing subsequence

```callout jargon
A **subsequence** keeps some items of a list in their original order, and may skip any of them. From 12, 30, 18, 25 you can take 12, 18, 25.
```

Tsunade wants the longest stretch of days where rides kept going up, skipping any days. That is the **longest increasing subsequence** (LIS).

- State: `len[i]` is the longest rising subsequence that ends on day i.
- Transition: look at every earlier day j with fewer rides. `len[i] = 1 + max(len[j])`, or 1 if there is none.
- Answer: the biggest `len[i]` anywhere, not the last cell.

```java run
public class Main {
    static int longestRise(int[] rides) {
        if (rides.length == 0) {
            return 0;
        }
        int[] len = new int[rides.length];
        int best = 1;
        for (int i = 0; i < rides.length; i++) {
            len[i] = 1;
            for (int j = 0; j < i; j++) {
                if (rides[j] < rides[i] && len[j] + 1 > len[i]) {
                    len[i] = len[j] + 1;
                }
            }
            best = Math.max(best, len[i]);
        }
        return best;
    }

    public static void main(String[] args) {
        int[] ridesPerDay = {12, 30, 18, 25, 9, 40, 33, 50};
        System.out.println("Most days in a rising line: " + longestRise(ridesPerDay));
    }
}
```

Each day looks back at every earlier day, so this is O(n²) time and O(n) memory. A cleverer version keeps a sorted list of best tails and binary searches it. That runs in O(n log n), and interviewers like to ask for it.

### How fast is each?

| Pattern | Time | Memory | Why |
|---|---|---|---|
| Take or skip | O(n) | O(1) | One pass, two rolling variables |
| Fewest coins, ways to pay | O(A × k) | O(A) | A amounts, each tries k coins |
| Grid paths | O(R × C) | O(C) | Each square once, one rolling row |
| 0/1 knapsack | O(n × W) | O(W) | n crates, each tries W capacities |
| Edit distance, LCS | O(n × m) | O(n × m) | One cell per pair of prefixes |
| LIS | O(n²) | O(n) | Each day looks back at all earlier days |

The rule of thumb: time is the number of cells times the work per cell.

### In an interview

Listen for "how many ways", "fewest", "most", "longest" or "is it possible", plus choices that affect later choices. Then say the recipe out loud: state, transition, base cases, answer cell, fill order.

If the transition is hard to see, write a memoized recursion first. Turn it into a table once it works.

Classic problems that use these patterns:

- Robbing houses that are not next door (take or skip)
- Coin change: the fewest coins, and the number of ways (unbounded choices)
- Unique paths through a grid with obstacles (grid paths)
- Splitting a list into two groups with equal sums, or packing a bag (0/1 knapsack)
- Edit distance and longest common subsequence (two strings)
- Longest increasing subsequence, and its O(n log n) version

In production you meet them as "did you mean" search, file diffs, and loading trucks or budgets.

## Worked example

Monday's bike visited five stations. Tuesday's bike visited the same five in a different order. How many stops did both days visit in the same order? This is the longest common subsequence, built from the recipe.

```java run
public class Main {
    static int sharedStops(String[] monday, String[] tuesday) {
        int n = monday.length;
        int m = tuesday.length;
        int[][] same = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (monday[i - 1].equals(tuesday[j - 1])) {
                    same[i][j] = same[i - 1][j - 1] + 1;
                } else {
                    same[i][j] = Math.max(same[i - 1][j], same[i][j - 1]);
                }
            }
        }
        return same[n][m];
    }

    public static void main(String[] args) {
        String[] monday = {"Riverside", "Harbor", "Uptown", "Depot", "Old Mill"};
        String[] tuesday = {"Harbor", "Riverside", "Uptown", "Old Mill", "Depot"};
        System.out.println("Stops in the same order: " + sharedStops(monday, tuesday));
        System.out.println("Against an empty day: " + sharedStops(monday, new String[0]));
    }
}
```

Line by line:

1. **State:** `same[i][j]` is the longest shared order using the first i stops of Monday and the first j stops of Tuesday.
2. **Base cases:** row 0 and column 0 mean "no stops on one day", so they stay 0. Java fills a new `int[][]` with zeros.
3. **Transition, match:** when stop i equals stop j, both days share it. Add 1 to the diagonal cell.
4. **Transition, no match:** drop the last stop of one day or the other, and keep the better result.
5. **Fill order:** row by row, so the cells above, left and diagonal are always ready.
6. **Answer cell:** `same[n][m]`. Here it is 3, for example Harbor, Uptown, Depot. An empty day shares 0 stops.

It runs in O(n × m) time and memory.

## Try it

This compares "biggest coin first" with DP for the kiosk's coins. Run it. Then change the coins to `{1, 5, 10, 25}` and the amount to 63. Both should give 6 coins. Then try `{1, 7, 10}` with 14. Which one wins?

```java run
import java.util.Arrays;

public class Main {
    static int greedyCoins(int[] coins, int amount) {
        int[] sorted = coins.clone();
        Arrays.sort(sorted);
        int count = 0;
        for (int i = sorted.length - 1; i >= 0; i--) {
            while (amount >= sorted[i]) {
                amount -= sorted[i];
                count++;
            }
        }
        return amount == 0 ? count : -1;
    }

    static int fewestCoins(int[] coins, int amount) {
        int impossible = amount + 1;
        int[] fewest = new int[amount + 1];
        Arrays.fill(fewest, impossible);
        fewest[0] = 0;
        for (int a = 1; a <= amount; a++) {
            for (int coin : coins) {
                if (coin <= a && fewest[a - coin] + 1 < fewest[a]) {
                    fewest[a] = fewest[a - coin] + 1;
                }
            }
        }
        return fewest[amount] == impossible ? -1 : fewest[amount];
    }

    public static void main(String[] args) {
        int[] coins = {1, 3, 4};
        int amount = 6;
        System.out.println("Biggest coin first: " + greedyCoins(coins, amount) + " coins");
        System.out.println("Dynamic programming: " + fewestCoins(coins, amount) + " coins");
    }
}
```

## Recap

- Every DP solution answers five questions: state, transition, base cases, answer cell and fill order.
- "Number of ways", "fewest", "most" and "can you reach" are strong hints that a problem needs DP.
- Take or skip, unbounded choices, grid paths, 0/1 knapsack, two strings and LIS cover most DP problems you will meet.
- Greedy "biggest coin first" fails for coins 1, 3 and 4. DP tries every choice and never misses 3 + 3.
- A rolling array keeps only the cells the transition needs. Fill a 0/1 knapsack row from the top capacity down.

## Common mistakes

**1. Filling the knapsack row upward.**

```java
static int bestTruckLoad(int[] weights, int[] values, int capacity) {
    int[] best = new int[capacity + 1];
    for (int i = 0; i < weights.length; i++) {
        for (int w = weights[i]; w <= capacity; w++) {
            best[w] = Math.max(best[w], best[w - weights[i]] + values[i]);
        }
    }
    return best[capacity];
}
```

One crate weighs 2 and is worth 3. With capacity 6, this returns 9, as if the truck held three copies of it. Going up, `best[w - weights[i]]` already includes this crate. Fix: loop `w` from `capacity` down to `weights[i]`.

**2. Using Integer.MAX_VALUE as "impossible" and adding 1.**

```java nocompile
Arrays.fill(fewest, Integer.MAX_VALUE);
fewest[0] = 0;
for (int a = 1; a <= amount; a++) {
    for (int coin : coins) {
        if (coin <= a) {
            fewest[a] = Math.min(fewest[a], fewest[a - coin] + 1);
        }
    }
}
```

`fewestCoins({2}, 3)` returns `-2147483648` instead of -1. `Integer.MAX_VALUE + 1` wraps around to the most negative `int`, and that "wins" every minimum. Fix: use `amount + 1` as "impossible". It is safe to add 1 to.

**3. Putting the amounts in the outer loop when you count mixes.**

```java nocompile
for (int a = 1; a <= amount; a++) {
    for (int coin : coins) {
        if (coin <= a) {
            ways[a] += ways[a - coin];
        }
    }
}
```

For coins 1, 2 and 5, this counts 9 ways to pay 5 cents, not 4. It counts 1 + 2 and 2 + 1 as different. Fix: put the coins in the outer loop and the amounts inside.

**4. Filling the first row of a grid with 1s, ignoring closed squares.**

```java nocompile
for (int c = 0; c < cols; c++) {
    routes[0][c] = 1;
}
```

A one-street grid of 4 squares, with square 2 closed, should have 0 routes. This code says 1, because the last square never checks for the block before it. Fix: use the same transition everywhere. A square in row 0 takes its count from the left, and a closed square is 0.

**5. Forgetting the base row and column of edit distance.**

```java nocompile
int[][] dist = new int[a.length() + 1][b.length() + 1];
for (int i = 1; i <= a.length(); i++) {
    for (int j = 1; j <= b.length(); j++) {
        // the transition, with no base cases set
    }
}
```

`editDistance("", "Depot")` returns 0, but it needs 5 inserts. "Harbour" and "Harbor" still give 1, so the bug hides. Fix: set `dist[i][0] = i` and `dist[0][j] = j` before the loops.

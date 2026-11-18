## Hook

You sit on a train and look out of one window. You can see three houses at a time. When the train moves on, one new house slides in on the right and one old house slides out on the left. You never count all three again. You just update what changed.

Kakashi's ops dashboard does the opposite. To find the busiest hour of the day, it starts at every minute and re-adds the next 60 minutes, again and again. This page shows a family of tricks that walk the data once, like that train window.

## Concept

### Why trying every stretch is slow

Many questions about a day of ride data ask about a **stretch**: values that sit next to each other, like minutes 10 to 70.

```callout jargon
A **subarray** is a run of values that sit next to each other in an array, such as `rides[3]` to `rides[7]`. This page also calls it a stretch or a window.
```

The obvious answer tries every start and every end. That is two nested loops. For n values it checks about n × n / 2 stretches, so it costs **O(n²)**.

```callout jargon
**Brute force** means trying every possible answer, one by one. It is easy to write and always correct, but often far too slow.
```

A day has 1,440 minutes, so brute force checks about a million stretches. A month of minutes is 43,200 values and almost a billion stretches. Every trick on this page does the same job in one pass: **O(n)**.

### Two pointers

```callout jargon
A **pointer** here is just an `int` that holds an index into an array. "Moving a pointer" means adding or subtracting 1 from it.
```

The **two pointers** pattern keeps two indexes and moves them by simple rules. Each pointer only ever moves one way, so together they take at most about 2n steps. That is O(n) time. Two `int` variables are all the extra memory, so the space is O(1).

There are two common shapes:

- **Opposite ends**: one pointer starts at the left, one at the right, and they walk towards each other.
- **Same direction**: a fast pointer reads ahead, and a slow pointer marks where to write.

### Opposite ends: a pair with a target total

A rider has a voucher worth exactly 1,100 cents. It must cover two rides. Which two fares in the sorted list add up to 1,100?

Put `left` on the cheapest fare and `right` on the most expensive one. Look at their sum:

- Equal to the target: you found the pair.
- Too small: the left fare is too cheap even with the biggest fare left, so move `left` up.
- Too big: the right fare is too expensive even with the cheapest fare left, so move `right` down.

Each step rules out one fare for good. That is why the method is correct, and why it is O(n).

```animate
title: Two pointers close in on 1,100 cents
kind: code
code: |
  int[] fares = {150, 250, 400, 700, 900};
  int target = 1100;
  int left = 0;
  int right = fares.length - 1;
  while (left < right) {
      int sum = fares[left] + fares[right];
      if (sum == target) {
          System.out.println(left + " and " + right);
          break;
      } else if (sum < target) {
          left++;
      } else {
          right--;
      }
  }
steps:
  - line: 1
    vars: { fares: "@f" }
    objects: { f: { type: "int[]", fields: { "0": 150, "1": 250, "2": 400, "3": 700, "4": 900 } } }
    say: The fares are sorted, cheapest first. Sorting is what makes this trick work.
  - line: 2
    vars: { target: 1100 }
    say: We want two fares that add up to exactly 1,100 cents.
  - line: 3
    vars: { left: 0 }
    say: left starts on the cheapest fare.
  - line: 4
    vars: { right: 4 }
    say: right starts on the most expensive fare.
  - line: 6
    vars: { sum: 1050 }
    say: 150 + 900 is 1,050. That is too small.
  - line: 11
    vars: { left: 1 }
    say: Even the biggest fare cannot rescue 150, so 150 is out. left moves up.
  - line: 6
    vars: { sum: 1150 }
    say: 250 + 900 is 1,150. Now it is too big.
  - line: 13
    vars: { right: 3 }
    say: Even the cheapest fare left is too much with 900, so 900 is out. right moves down.
  - line: 6
    vars: { sum: 950 }
    say: 250 + 700 is 950. Too small again, so 250 is out.
  - line: 11
    vars: { left: 2 }
    say: left moves up to 400.
  - line: 6
    vars: { sum: 1100 }
    say: 400 + 700 is exactly 1,100.
  - line: 8
    out: "2 and 3"
    say: Found in 4 looks. Brute force could have tried all 10 pairs.
```

```callout tip
On unsorted fares, use the HashMap two-sum from [Build a hash table](/t/module-04-dsa/topic-08-hash-tables). It is O(n) time but O(n) space for the map. On sorted fares, two pointers need only O(1) space.
```

### Syntax: opposite-end pointers

```java nocompile
int left = 0;
int right = sorted.length - 1;
while (left < right) {
    int sum = sorted[left] + sorted[right];
    if (sum == target) {
        return new int[] {left, right};
    } else if (sum < target) {
        left++;
    } else {
        right--;
    }
}
return new int[0];
```

The condition is `left < right`, not `<=`. When the pointers meet, only one fare is left, and a fare cannot pair with itself.

### Same direction: fast and slow pointers

A dock scanner logs bike numbers in sorted order, but it repeats a bike each time it pings. You want each bike once, **in place**.

```callout jargon
**In place** means the method changes the array it was given, instead of building a new one. It needs only O(1) extra memory.
```

In [Linked lists](/t/module-04-dsa/topic-06-linked-lists), fast and slow meant two walkers moving 1 and 2 steps. On arrays, the same name usually means a read pointer and a write pointer.

`fast` reads every value. `slow` marks the last unique value kept so far. When `fast` finds a new value, `slow` steps forward and the value is copied there.

```java run
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int[] bikeIds = {1042, 1042, 1050, 1077, 1077, 1077, 1090};
        int slow = 0;
        for (int fast = 1; fast < bikeIds.length; fast++) {
            if (bikeIds[fast] != bikeIds[slow]) {
                slow++;
                bikeIds[slow] = bikeIds[fast];
            }
        }
        int unique = slow + 1;
        System.out.println(unique + " different bikes");
        System.out.println(Arrays.toString(Arrays.copyOf(bikeIds, unique)));
    }
}
```

It prints `4 different bikes` and `[1042, 1050, 1077, 1090]`. Time O(n), because `fast` visits each value once. Space O(1). An empty array needs a guard first, because `unique` would wrongly be 1.

### Fixed-size sliding window

```callout jargon
A **sliding window** is a stretch of the array that moves along it one step at a time. The window keeps a running total, so each move costs O(1).
```

To find the busiest k minutes in a row, add up the first k values once. Then slide: add the value that comes in on the right, and subtract the one that leaves on the left.

```java run
public class Main {
    public static void main(String[] args) {
        int[] rides = {2, 5, 1, 8, 3, 0, 4};
        int k = 3;
        int window = 0;
        for (int i = 0; i < k; i++) {
            window += rides[i];
        }
        int best = window;
        System.out.println("minutes 0 to " + (k - 1) + ": " + window);
        for (int i = k; i < rides.length; i++) {
            window += rides[i] - rides[i - k];
            System.out.println("minutes " + (i - k + 1) + " to " + i + ": " + window);
            best = Math.max(best, window);
        }
        System.out.println("best: " + best);
    }
}
```

The windows hold 8, 14, 12, 11 and 7 rides, so the best is 14. Brute force re-adds k values for every window, which is O(n × k). The slide is O(n) time and O(1) space.

### Syntax: fixed window

```java nocompile
int window = 0;
for (int i = 0; i < k; i++) {
    window += values[i];
}
int best = window;
for (int i = k; i < values.length; i++) {
    window += values[i] - values[i - k];
    best = Math.max(best, window);
}
```

The value leaving is always `values[i - k]`, exactly k places behind the one coming in.

### Variable-size window

Sometimes the window size is the answer. A truck driver has a 30-minute slot. Each station visit takes some minutes. What is the longest run of back-to-back visits that fits?

The window grows on the right, one visit at a time. When the total goes over the limit, it shrinks from the left until it fits again. After that, the window always fits, so you keep the longest one seen.

```animate
title: The window grows, then shrinks to fit 30 minutes
kind: code
code: |
  int[] mins = {5, 10, 20, 5, 5};
  int limit = 30;
  int left = 0, total = 0, best = 0;
  for (int right = 0; right < mins.length; right++) {
      total += mins[right];
      while (total > limit) {
          total -= mins[left];
          left++;
      }
      best = Math.max(best, right - left + 1);
  }
  System.out.println(best);
steps:
  - line: 1
    vars: { mins: "@m" }
    objects: { m: { type: "int[]", fields: { "0": 5, "1": 10, "2": 20, "3": 5, "4": 5 } } }
    say: Five station visits, in minutes. The driver's slot is 30 minutes.
  - line: 3
    vars: { limit: 30, left: 0, total: 0, best: 0 }
    say: The window starts empty at index 0.
  - line: 5
    vars: { right: 0, total: 5 }
    say: The 5-minute visit joins the window.
  - line: 10
    vars: { best: 1 }
    say: The window fits, so the best run so far is 1 visit.
  - line: 5
    vars: { right: 1, total: 15 }
    say: The 10-minute visit joins. 15 minutes still fits.
  - line: 10
    vars: { best: 2 }
    say: Now the best run is 2 visits.
  - line: 5
    vars: { right: 2, total: 35 }
    say: The 20-minute visit joins. 35 is over the limit.
  - line: 7
    vars: { total: 30 }
    say: Shrink from the left. The first visit leaves, so the total drops to 30.
  - line: 8
    vars: { left: 1 }
    say: left moves up. The window is 10 and 20, and it fits again.
  - line: 5
    vars: { right: 3, total: 35 }
    say: A 5-minute visit joins. 35 is too much again.
  - line: 7
    vars: { total: 25 }
    say: The 10-minute visit leaves. 25 fits.
  - line: 8
    vars: { left: 2 }
    say: The window is now 20 and 5.
  - line: 5
    vars: { right: 4, total: 30 }
    say: The last 5-minute visit joins. 30 fits exactly, because the limit allows equal.
  - line: 10
    vars: { best: 3 }
    say: The window 20, 5, 5 holds 3 visits. That is the new best.
  - line: 12
    out: "3"
    say: The longest run that fits in 30 minutes has 3 visits.
```

The inner `while` looks like a nested loop, but it is not O(n²). `left` only moves forward, so it moves at most n times in the whole run. Both pointers together take at most 2n steps: O(n) time, O(1) space.

```callout warning
Shrinking only works when every value is 0 or more. Then adding a value can only raise the total, and removing one can only lower it. With negative values, a total over the limit might drop again later, so "shrink now" is no longer safe.
```

### Syntax: variable window

```java nocompile
int left = 0;
long total = 0;
int best = 0;
for (int right = 0; right < values.length; right++) {
    total += values[right];
    while (total > limit) {
        total -= values[left];
        left++;
    }
    best = Math.max(best, right - left + 1);
}
```

Grow on the right every step. Shrink on the left only while the window breaks the rule.

For the shortest stretch whose total is at least a target, flip it. Shrink while `total >= target`, and record the length inside the `while`, before each shrink, because that is when the window is valid.

### Prefix sums

Kakashi asks many questions about one day: rides in hours 1 to 3? Hours 6 to 20? Adding each range is O(n) per question. With q questions, that is O(n × q).

```callout jargon
A **prefix sum** is the running total of every value before a point. `prefix[i]` is the total of the first i values, so `prefix[0]` is always 0.
```

Build the prefix array once, in O(n). Then the total from index `from` to index `to` is one subtraction: everything up to `to`, minus everything before `from`.

```java run
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int[] ridesPerHour = {4, 0, 7, 2, 9};
        long[] prefix = new long[ridesPerHour.length + 1];
        for (int i = 0; i < ridesPerHour.length; i++) {
            prefix[i + 1] = prefix[i] + ridesPerHour[i];
        }
        System.out.println(Arrays.toString(prefix));
        System.out.println("hours 1 to 3: " + (prefix[3 + 1] - prefix[1]));
        System.out.println("hours 0 to 4: " + (prefix[4 + 1] - prefix[0]));
    }
}
```

It prints `[0, 4, 4, 11, 13, 22]`, then 9 and 22. Building costs O(n) time and O(n) space. Each question then costs O(1).

### Syntax: prefix sums

```java nocompile
long[] prefix = new long[values.length + 1];
for (int i = 0; i < values.length; i++) {
    prefix[i + 1] = prefix[i] + values[i];
}
long total = prefix[to + 1] - prefix[from];   // from and to both included
```

The array is one cell longer than the data. That extra 0 at the front means a range starting at index 0 needs no special case. Use `long`, because totals grow fast.

### Prefix sums and a HashMap

Now the hard one. At Harbor, each hour has a net bike count: bikes arriving minus bikes leaving. It can be negative. How many stretches of hours gained exactly k bikes?

The window trick fails here, because negatives break shrinking. Prefix sums give a new idea. A stretch from i to j adds up to k when:

```text
running total at j  -  running total before i  =  k
so: running total before i  =  running total at j  -  k
```

So walk once, keeping the running total. At each step, ask a map: "How many earlier running totals equal `running - k`?" Each one is the start of a stretch that ends here.

- **Seed the map with `0 -> 1`.** It stands for "nothing added yet", before index 0. Without it, stretches that start at index 0 are never counted.
- **Look up before you store.** Otherwise a running total could match itself when k is 0.

Time is O(n), because each step does one lookup and one update. Space is O(n), for the map.

### Syntax: counting with Map.merge

```java nocompile
seen.merge(running, 1, Integer::sum);
```

`merge(key, 1, Integer::sum)` stores 1 when the key is new. Otherwise it adds 1 to the count already there. It replaces a three-line "get, add one, put back".

### Frequency counting

Counting how often each value appears is the same idea, without the running total. For lowercase letters, an `int[26]` is enough: `counts[c - 'a']++` turns `'a'` into slot 0 and `'z'` into slot 25. For anything else, use a map and `merge`.

```java run
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        String printed = "rbahor";
        String real = "harbor";
        int[] counts = new int[26];
        for (char c : real.toCharArray()) {
            counts[c - 'a']++;
        }
        for (char c : printed.toCharArray()) {
            counts[c - 'a']--;
        }
        boolean sameLetters = Arrays.stream(counts).allMatch(n -> n == 0);
        System.out.println("Same letters: " + sameLetters);

        String[] docked = {"Harbor", "Uptown", "Harbor", "Depot", "Harbor", "Uptown"};
        Map<String, Integer> perStation = new TreeMap<>();
        for (String station : docked) {
            perStation.merge(station, 1, Integer::sum);
        }
        System.out.println(perStation);
    }
}
```

A broken label printer scrambled "harbor" into "rbahor". Every slot ends at 0, so both words use the same letters: `Same letters: true`. The map prints `{Depot=1, Harbor=3, Uptown=2}`. Both are O(n) time. The `int[26]` is O(1) space, because it never grows. The map is O(k) space for k different keys.

### Which pattern fits?

| The question says | Pattern | Time | Space |
|---|---|---|---|
| Sorted data, find a pair | Opposite-end pointers | O(n) | O(1) |
| Find a triple that adds up to a target | Sort, then one opposite-end pass for each first value | O(n²) | O(1) |
| Change the array in place, keep or drop items | Fast and slow pointers | O(n) | O(1) |
| Every block of exactly k in a row | Fixed sliding window | O(n) | O(1) |
| Longest stretch with total at most a limit, or shortest with total at least a target, no negatives | Variable sliding window | O(n) | O(1) |
| Many range totals on data that does not change | Prefix sums | O(n) build, O(1) each | O(n) |
| Count stretches that add up to k, negatives allowed | Prefix sums and a HashMap | O(n) | O(n) |
| Same letters, most common item, counts | Frequency counting | O(n) | O(1) or O(k) |

### In an interview

Listen for these words in the problem: **sorted**, **pair**, **in place**, **contiguous** (next to each other), **subarray**, **substring**, **window**, **at most k**, **longest** or **shortest**. They point to two pointers or a window. **Range sum** and **sum equals k** point to prefix sums.

Classic problems that use these patterns:

- Two numbers in a sorted array that add up to a target, and its bigger cousin, three numbers that add up to zero. For three, sort, fix each first value in turn, and run one opposite-end pass on the rest: O(n²). Skip a first value equal to the one before it, so no triple repeats.
- The container that holds the most water: two walls chosen from the two ends.
- The longest piece of a string with no repeated letter: a variable window plus a frequency count.
- The shortest stretch whose total reaches a target.
- Count the stretches whose total equals k: prefix sums and a HashMap.

Always say the brute force first, with its O(n²) cost. Then name the pattern and why it is safe, for example "values are never negative, so the window can shrink".

## Worked example

Here is the prefix-sum-and-map method on Harbor's net bikes, `{2, -1, 1, 2}`, counting stretches that gain exactly 2 bikes.

```java run
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        int[] netBikes = {2, -1, 1, 2};
        int k = 2;
        Map<Long, Integer> seen = new HashMap<>();
        seen.put(0L, 1);
        long running = 0;
        long count = 0;
        for (int i = 0; i < netBikes.length; i++) {
            running += netBikes[i];
            int matches = seen.getOrDefault(running - k, 0);
            count += matches;
            seen.merge(running, 1, Integer::sum);
            System.out.println("i=" + i + " running=" + running + " need=" + (running - k)
                    + " matches=" + matches + " count=" + count);
        }
        System.out.println("Stretches that net " + k + " bikes: " + count);
    }
}
```

| i | running | need | Totals seen before | matches | Stretches found |
|---|---|---|---|---|---|
| 0 | 2 | 0 | {0: 1} | 1 | {2} |
| 1 | 1 | -1 | {0: 1, 2: 1} | 0 | none |
| 2 | 2 | 0 | {0: 1, 1: 1, 2: 1} | 1 | {2, -1, 1} |
| 3 | 4 | 2 | {0: 1, 1: 1, 2: 2} | 2 | {-1, 1, 2} and {2} |

Line by line:

1. `seen.put(0L, 1)` records the empty start. It is what finds `{2}` at i = 0.
2. Each step adds the hour to `running`.
3. `running - k` is the running total a stretch must start after.
4. `getOrDefault` counts how many earlier points had that total. Each is one stretch ending here.
5. Only then does `merge` record the new running total, for later steps.
6. At i = 3, the total 2 was seen twice, so two stretches end here. The final count is 4.

## Try it

Change `k` to 2, then 5, then 7. Watch the best window move. Then put a 9 at the end of `rides` and run it again.

```java run
public class Main {
    public static void main(String[] args) {
        int[] rides = {2, 5, 1, 8, 3, 0, 4};
        int k = 3;
        int window = 0;
        for (int i = 0; i < k; i++) {
            window += rides[i];
        }
        int best = window;
        int bestStart = 0;
        for (int i = k; i < rides.length; i++) {
            window += rides[i] - rides[i - k];
            if (window > best) {
                best = window;
                bestStart = i - k + 1;
            }
        }
        System.out.println("Busiest " + k + " minutes start at minute " + bestStart + " with " + best + " rides");
    }
}
```

## Recap

- Brute force tries every stretch in O(n²). These patterns walk the data once, in O(n).
- Two pointers move one way only: from both ends on sorted data, or fast and slow to work in place. O(1) space.
- A sliding window keeps a running total: add the value coming in, subtract the one going out. A variable window shrinks from the left, but only works when no value is negative.
- Prefix sums cost O(n) once, then answer any range total in O(1) as `prefix[to + 1] - prefix[from]`.
- To count stretches that add up to k, even with negatives, pair prefix sums with a HashMap seeded with `0 -> 1`. Count frequencies with `int[26]` or `Map.merge`.

## Common mistakes

**1. Letting the pointers meet.** With `left <= right`, one fare gets paired with itself.

```java
public class Main {
    public static void main(String[] args) {
        int[] fares = {250, 500, 900};
        int target = 1000;
        int left = 0;
        int right = fares.length - 1;
        while (left <= right) {
            int sum = fares[left] + fares[right];
            if (sum == target) {
                System.out.println("Pair: " + left + " and " + right);
                return;
            } else if (sum < target) {
                left++;
            } else {
                right--;
            }
        }
        System.out.println("No pair");
    }
}
```

It prints `Pair: 1 and 1`, which counts the 500 fare twice. Fix: loop `while (left < right)`, so it prints `No pair`.

**2. Forgetting the 0 -> 1 seed.** Stretches that start at index 0 are never counted.

```java
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        int[] netBikes = {2, -1, 1, 2};
        int k = 2;
        Map<Long, Integer> seen = new HashMap<>();
        long running = 0;
        long count = 0;
        for (int value : netBikes) {
            running += value;
            count += seen.getOrDefault(running - k, 0);
            seen.merge(running, 1, Integer::sum);
        }
        System.out.println(count);
    }
}
```

It prints `2`, but the right answer is 4. It misses `{2}` and `{2, -1, 1}`, which both start at index 0. Fix: add `seen.put(0L, 1);` before the loop.

**3. Using a sliding window when values can be negative.** Shrinking throws away starts that a later negative value would have saved.

```java
public class Main {
    public static void main(String[] args) {
        int[] netBikes = {2, -1, 1, 2};
        int k = 2;
        int left = 0;
        int total = 0;
        int count = 0;
        for (int right = 0; right < netBikes.length; right++) {
            total += netBikes[right];
            while (total > k && left <= right) {
                total -= netBikes[left];
                left++;
            }
            if (total == k) {
                count++;
            }
        }
        System.out.println(count);
    }
}
```

It prints `3`, not 4. Fix: when values can be negative, use prefix sums and a HashMap. Keep windows for data that is never negative.

**4. Building the prefix array without the leading 0.** A range that starts at index 0 then reads index -1.

```java run crash
public class Main {
    public static void main(String[] args) {
        int[] rides = {4, 0, 7, 2, 9};
        long[] prefix = new long[rides.length];
        prefix[0] = rides[0];
        for (int i = 1; i < rides.length; i++) {
            prefix[i] = prefix[i - 1] + rides[i];
        }
        int from = 0;
        int to = 2;
        System.out.println(prefix[to] - prefix[from - 1]);
    }
}
```

The program stops with:

```text
Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index -1 out of bounds for length 5
	at Main.main(Main.java:11)
```

Fix: make the array `values.length + 1` long with `prefix[0] = 0`. Then every range is `prefix[to + 1] - prefix[from]`.

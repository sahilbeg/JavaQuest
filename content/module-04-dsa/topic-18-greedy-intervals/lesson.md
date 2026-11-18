## Hook

Harbor has one booking screen for its docks. Riders reserve time windows. The crew books maintenance. Jiraiya wants the screen to answer three questions fast. When is the station busy? How many bookings fit on one dock? How many docks do we need at rush hour?

You could try every mix of bookings, but that grows like 2^n. There is a faster trick. Make the best choice right now, and never look back. That trick has a name: **greedy**. It is fast and simple, and sometimes it is plain wrong. This ticket teaches you to tell the difference.

## Concept

### What greedy means

```callout jargon
A **greedy algorithm** builds its answer one step at a time. At each step it makes the choice that looks best right now, and it never goes back to change it.
```

DP from SPK-0417 tries every choice and keeps the best. Greedy commits to one choice per step. So greedy is usually faster and uses less memory.

The catch: a choice that looks best now can ruin later steps. Greedy is only correct when you can argue that the local best choice is always safe.

### When greedy fails

You already saw one failure. With coins of 1, 3 and 4 cents, "biggest coin first" pays 6 cents as 4 + 1 + 1. That is 3 coins, but 3 + 3 is only 2.

Greedy failed because taking the 4 left an awkward 2 behind. The local best choice hurt a later step. When you cannot prove that never happens, use DP instead.

```callout tip
Before you trust a greedy rule, hunt for a small counterexample: three or four items, chosen to trick the rule. If you find one, the rule is wrong.
```

### Arguing greedy is right: the exchange argument

```callout jargon
An **exchange argument** proves a greedy choice is safe. Take any best answer. Swap one of its choices for the greedy choice. Show the answer is still valid and no worse.
```

Here is the idea in plain words, for booking one dock. The greedy rule takes the booking that **ends first**.

1. Picture the best possible plan. Its first booking ends at some time T.
2. The greedy booking ends at or before T, because it ends first of all.
3. Swap the plan's first booking for the greedy one. Nothing later overlaps it, because it ends no later.
4. The plan has the same number of bookings and is still valid. So taking the greedy booking first never loses.

Repeat the argument for the next booking, and the whole greedy plan is a best plan.

### Half-open windows

```callout jargon
An **interval** is a stretch of time with a start and an end. A **half-open** interval includes its start but not its end. A **closed** interval includes both.
```

Does a booking ending at 10:00 overlap one starting at 10:00 at the same dock? With closed intervals, yes: both claim minute 10:00. That would mean a second dock, which is silly. The first booking is over when the second begins.

So Spoke uses half-open windows, written in minutes after midnight. A booking from 600 to 660 uses minutes 600 to 659. A booking from 660 to 720 starts the moment the first one ends. They only **touch**, and one dock serves both.

Two half-open windows overlap when each one starts before the other one ends.

### Syntax: a window record

```java
record Interval(int start, int end) {
    boolean overlaps(Interval other) {
        return start < other.end && other.start < end;
    }
}
```

Both checks use `<`, not `<=`. With `<=`, touching windows would count as overlapping. Always say which kind of interval you use, in code comments and in interviews.

### Syntax: sort a copy

Almost every interval problem starts with a sort. Sort a copy, so you never change a list someone else owns.

```java nocompile
List<Interval> sorted = new ArrayList<>(bookings);
sorted.sort(Comparator.comparingInt(Interval::start));
```

Use `Interval::end` instead to sort by end time. A list made with `List.of(...)` cannot be sorted in place, so the copy also keeps you safe.

### Merging busy periods

When is Harbor busy? Sort the bookings by start. Then walk through them, keeping a list of merged windows.

- If a booking overlaps the last merged window, stretch that window to the **later** end.
- Otherwise, the booking starts a new window.

```java run
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
    static List<Interval> merge(List<Interval> bookings) {
        List<Interval> sorted = new ArrayList<>(bookings);
        sorted.sort(Comparator.comparingInt(Interval::start));
        List<Interval> merged = new ArrayList<>();
        for (Interval booking : sorted) {
            if (!merged.isEmpty() && booking.start() < merged.get(merged.size() - 1).end()) {
                Interval last = merged.remove(merged.size() - 1);
                merged.add(new Interval(last.start(), Math.max(last.end(), booking.end())));
            } else {
                merged.add(booking);
            }
        }
        return merged;
    }

    public static void main(String[] args) {
        List<Interval> bookings = List.of(
                new Interval(900, 960),
                new Interval(600, 660),
                new Interval(630, 700),
                new Interval(700, 730));
        System.out.println(merge(bookings));
    }
}

record Interval(int start, int end) {
}
```

600-660 and 630-700 merge into 600-700. Then 700-730 only touches it, so it stays separate. After sorting, a booking can only overlap the last merged window, which is why one pass is enough.

The sort costs O(n log n) and the pass costs O(n), so the total is O(n log n) time. The copy and the answer take O(n) memory.

### The most bookings on one dock

```callout jargon
**Interval scheduling** means picking the most intervals with no two overlapping.
```

Riders ask to book one dock for different windows. Which bookings should you accept, to say yes to the most riders?

Sort by **end time**. Take each request that starts at or after the end of the last one you took. Skip the rest. The exchange argument above proves this is a best answer.

Two tempting rules fail:

- **Earliest start first:** one long booking from 600 to 900 starts first and blocks everyone else.
- **Shortest first:** a short booking from 690 to 720 can sit across two longer ones and block both.

It costs O(n log n) time for the sort, plus one O(n) pass.

### How many docks at once

How many docks must Harbor set aside so every booking gets one? That is the most bookings active at the same moment.

The **sweep** idea: turn each booking into two events, +1 at its start and −1 at its end. Walk through all events in time order and keep a running total. The biggest total is the answer.

In code, sort all starts in one array and all ends in another. Before each start, free a dock for every end at or before it. Ends go first, so a booking starting at 660 reuses the dock of a booking ending at 660.

### Watch the docks sweep

```animate
title: Counting docks at Harbor
kind: code
code: |
  int[] starts = {600, 630, 650, 710};
  int[] ends = {680, 700, 720, 800};
  int inUse = 0;
  int most = 0;
  int e = 0;
  for (int s = 0; s < starts.length; s++) {
      while (ends[e] <= starts[s]) {
          inUse--;
          e++;
      }
      inUse++;
      most = Math.max(most, inUse);
  }
  System.out.println(most);
steps:
  - line: 1
    objects: { st: { type: "int[]", fields: { "0": 600, "1": 630, "2": 650, "3": 710 } } }
    vars: { starts: "@st" }
    say: Four bookings, with every start time sorted into one array.
  - line: 2
    objects: { en: { type: "int[]", fields: { "0": 680, "1": 700, "2": 720, "3": 800 } } }
    vars: { ends: "@en" }
    say: Every end time, sorted into another array. The two arrays no longer line up by booking, and that is fine.
  - line: 5
    vars: { inUse: 0, most: 0, e: 0 }
    say: No docks in use yet. e points at the next booking to end.
  - line: 12
    vars: { s: 0, inUse: 1, most: 1 }
    say: "Minute 600: nothing has ended, since the first end is 680. One dock in use."
  - line: 12
    vars: { s: 1, inUse: 2, most: 2 }
    say: "Minute 630: still nothing ended. Two docks."
  - line: 12
    vars: { s: 2, inUse: 3, most: 3 }
    say: "Minute 650: three bookings are active at once. That is the busiest moment."
  - line: 9
    vars: { s: 3, inUse: 2, e: 1 }
    say: "Minute 710: the booking ending at 680 is over, so its dock is free."
  - line: 9
    vars: { inUse: 1, e: 2 }
    say: The booking ending at 700 is over too. The next end, 720, is after 710, so the while loop stops.
  - line: 12
    vars: { inUse: 2 }
    say: The booking at 710 takes a dock. Two in use, and most stays 3.
  - line: 14
    vars: { s: ~ }
    out: "3"
    say: Harbor needs 3 docks for these bookings.
```

Two sorts cost O(n log n). The sweep moves `s` and `e` forward only, so it is O(n). Memory is O(n) for the two arrays.

### Inserting a maintenance window

The booked windows are already sorted and do not overlap. The crew adds one maintenance window. Walk the list in three phases:

1. Copy every window that ends at or before the new one starts.
2. Merge every window that overlaps the new one, stretching it to the earliest start and the latest end.
3. Add the merged window, then copy the rest.

The list is already sorted, so there is no sort at all. This is O(n) time.

### Jumping ahead

Greedy also solves "can you reach the end?" questions. A battery bike sits at station 0 of a long road. From station i it can ride at most `hop[i]` stations ahead. Can it reach the last station?

Keep `farthest`, the furthest station reachable so far. Walk the road. If you ever stand past `farthest`, you are stuck.

```java run
public class Main {
    static boolean canReachDepot(int[] hop) {
        int farthest = 0;
        for (int i = 0; i < hop.length; i++) {
            if (i > farthest) {
                return false;
            }
            farthest = Math.max(farthest, i + hop[i]);
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(canReachDepot(new int[] {2, 3, 1, 1, 4}));
        System.out.println(canReachDepot(new int[] {3, 2, 1, 0, 4}));
    }
}
```

In the second road, every route lands on station 3, where `hop` is 0. One pass is O(n) time and O(1) memory. A DP table would also work, but greedy needs no table.

### How fast is each?

| Problem | Time | Memory | Why |
|---|---|---|---|
| Merge busy periods | O(n log n) | O(n) | Sort by start, then one pass |
| Most bookings | O(n log n) | O(n) | Sort by end, then one pass |
| Docks needed | O(n log n) | O(n) | Two sorts, then a sweep |
| Insert a window | O(n) | O(n) | Already sorted, one pass |
| Can reach the end | O(n) | O(1) | One pass, one variable |

The sort is the expensive part. Without it, every interval method here is a single loop.

### In an interview

Listen for "intervals", "meetings", "bookings", "schedules", "time ranges" or "overlap". Your first move is almost always "sort by start" or "sort by end". Then ask: are the ends included? Say "half-open" out loud.

For a greedy answer, give a one-line exchange argument. Interviewers often ask "why does that work?". If you cannot argue it, try a small counterexample, then fall back to DP.

Classic problems that use these ideas:

- Merge overlapping intervals, and insert an interval into a sorted list
- The most meetings in one room, or the fewest to remove so none overlap (sort by end)
- The fewest meeting rooms needed (a heap of end times, or a +1/−1 sweep)
- Jump game: can you reach the last index?
- Gas station: where can a round trip start?

In production you meet them in calendars, booking systems, and in shift and server capacity planning.

## Worked example

The sweep is one way to count docks. Here is the other one, with the min-heap from SPK-0410. Sort the bookings by start. Keep a heap of the end times of bookings still active.

```java run
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Main {
    static int docksNeeded(List<Interval> bookings) {
        List<Interval> byStart = new ArrayList<>(bookings);
        byStart.sort(Comparator.comparingInt(Interval::start));
        PriorityQueue<Integer> endTimes = new PriorityQueue<>();
        int most = 0;
        for (Interval booking : byStart) {
            while (!endTimes.isEmpty() && endTimes.peek() <= booking.start()) {
                endTimes.poll();
            }
            endTimes.add(booking.end());
            most = Math.max(most, endTimes.size());
        }
        return most;
    }

    public static void main(String[] args) {
        List<Interval> bookings = List.of(
                new Interval(600, 700),
                new Interval(630, 720),
                new Interval(650, 680),
                new Interval(710, 800),
                new Interval(800, 830));
        System.out.println("Docks needed at Harbor: " + docksNeeded(bookings));
        System.out.println("Back-to-back bookings: " + docksNeeded(List.of(
                new Interval(600, 660), new Interval(660, 720))));
    }
}

record Interval(int start, int end) {
}
```

Line by line:

1. `byStart` is a sorted copy, so the caller's list never changes.
2. The heap holds the end time of every booking still active. Its smallest value is the next dock to come free.
3. Before each booking starts, `poll` every end time at or before its start. Those docks are free again. `<=` matches half-open windows.
4. `add` the new booking's end time. The heap's size is the number of docks in use right now.
5. The answers are 3 and 1. The back-to-back bookings share one dock, because 660 is not before 660.

Each booking goes into the heap once and out at most once, at O(log n) each. With the sort, the total is O(n log n) time and O(n) memory.

## Try it

This tries three greedy rules for booking one dock. Run it, and only "earliest end" gets 3 bookings. Then add `new Interval(560, 600)` to the requests. Every rule takes one more booking, and earliest end still wins with 4. Then try to build a list where "shortest first" wins outright. You can't: earliest end is always at least as good.

```java run
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
    static List<Interval> pick(List<Interval> requests, Comparator<Interval> order) {
        List<Interval> sorted = new ArrayList<>(requests);
        sorted.sort(order);
        List<Interval> taken = new ArrayList<>();
        for (Interval r : sorted) {
            boolean fits = true;
            for (Interval t : taken) {
                if (r.start() < t.end() && t.start() < r.end()) {
                    fits = false;
                }
            }
            if (fits) {
                taken.add(r);
            }
        }
        return taken;
    }

    public static void main(String[] args) {
        List<Interval> requests = List.of(
                new Interval(600, 900),
                new Interval(610, 700),
                new Interval(690, 720),
                new Interval(710, 800),
                new Interval(810, 840));
        System.out.println("Earliest start: " + pick(requests, Comparator.comparingInt(Interval::start)).size());
        System.out.println("Shortest first: " + pick(requests, Comparator.comparingInt(Interval::length)).size());
        System.out.println("Earliest end:   " + pick(requests, Comparator.comparingInt(Interval::end)).size());
    }
}

record Interval(int start, int end) {
    int length() {
        return end - start;
    }
}
```

## Recap

- A greedy algorithm makes the best-looking choice at each step and never goes back. It is fast, but only correct when you can argue it.
- An exchange argument shows a greedy choice is safe: swap it into any best answer, and the answer stays valid and no worse.
- Spoke's windows are half-open, so a booking ending at 660 and one starting at 660 do not overlap.
- Merge by sorting on start. Fit the most bookings by sorting on end. Count docks with a sweep or a min-heap of end times.
- These interval methods cost O(n log n), and the sort is the expensive part.

## Common mistakes

**1. Sorting the list you were given.**

```java
import java.util.Comparator;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Interval> bookings = List.of(new Interval(700, 760), new Interval(600, 660));
        bookings.sort(Comparator.comparingInt(Interval::start));
        System.out.println(bookings);
    }
}

record Interval(int start, int end) {
}
```

It crashes with `java.lang.UnsupportedOperationException`, because a `List.of` list cannot change. Even when the sort works, the caller's list is now in a different order. Fix: sort a copy, `new ArrayList<>(bookings)`.

**2. Taking the new booking's end when merging.**

```java nocompile
Interval last = merged.remove(merged.size() - 1);
merged.add(new Interval(last.start(), booking.end()));
```

Merging 600-720 with 630-650 gives `[Interval[start=600, end=650]]`. The short booking sits inside the long one, and the busy period got cut short. Fix: use `Math.max(last.end(), booking.end())`.

**3. Treating touching windows as overlapping.**

```java nocompile
while (ends[e] < starts[s]) {
    inUse--;
    e++;
}
```

Bookings 600-660 and 660-720 should share one dock, but this prints 2. The booking ending at 660 is not freed before the booking starting at 660. Fix: free docks while `ends[e] <= starts[s]`.

**4. Booking by earliest start.**

```java nocompile
sorted.sort(Comparator.comparingInt(Interval::start));
```

For requests 600-900, 610-700, 690-720, 710-800 and 810-840, this accepts only 1 booking. The long 600-900 request comes first and blocks every other one. Fix: sort by end time, which accepts 3.

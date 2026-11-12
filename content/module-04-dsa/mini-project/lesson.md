## The design

Each station has a number of bikes and a target number. The difference tells you what to do:

| Station | Bikes | Target | Surplus |
|---|---|---|---|
| Riverside | 12 | 6 | 6 to give |
| Harbor | 2 | 9 | 7 needed |
| Uptown | 10 | 6 | 4 to give |
| Old Mill | 5 | 8 | 3 needed |

The truck holds 5 bikes. The plan uses a greedy rule: every trip goes from the station with the most bikes to give to the station that needs the most. Ties go to the name that comes first alphabetically.

```text
Riverside -> Harbor: 5 bikes
Uptown -> Old Mill: 3 bikes
Riverside -> Harbor: 1 bike
Uptown -> Harbor: 1 bike
Moved 10 bikes in 4 trips
```

```callout tip
"Always take the largest" is the job a priority queue does best. You built one by hand in topic 10.
```

## What you build

All of these are in package `jq.m04.feature`.

1. `record Station(String name, int bikes, int target)` with `surplus()`: bikes minus target.
2. `record Trip(String from, String to, int bikes)`.
3. `TruckPlanner.plan(stations, capacity)` returns the trips in order.
   - Use two `PriorityQueue`s: givers and takers. Order each by amount, largest first, then by name.
   - Each trip moves the smallest of: the giver's amount, the taker's amount and `capacity`.
   - A station that still has an amount left goes back into its queue.
   - Stop when either queue is empty. Stations with a surplus of 0 are in neither queue.
4. `TruckPlanner.report(trips)` returns one line per trip, then `Moved N bikes in T trips`, joined with `\n`. A trip of one bike says `1 bike`. The total line always says `bikes` and `trips`.

### Syntax: a queue of amounts

```java nocompile
record Need(String name, int amount) {}

PriorityQueue<Need> givers = new PriorityQueue<>(
        Comparator.comparingInt(Need::amount).reversed().thenComparing(Need::name));
```

A record is fixed, so put back a new `Need` with the smaller amount.

## Recap

- A greedy rule makes the best-looking choice at every step.
- A priority queue hands you the largest item in O(log n).
- A clear tie rule keeps the plan the same on every run.

## Common mistakes

**Ignoring the truck's capacity.** A trip can never carry more bikes than the truck holds.

**Forgetting to put a station back.** Riverside gives 5 of its 6 bikes on the first trip. It still has 1 to give, so it goes back into the queue.

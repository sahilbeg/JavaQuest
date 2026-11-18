## What happened

Kakashi runs your mock loop today, and he plays the interviewer. He hands you the answers a previous candidate gave in three rounds. Each answer passed the candidate's own example. Each one still failed the round.

1. **The coding round.** "Each day's net takings in cents can be negative. Return the largest total of one or more days in a row." The answer is right on one example, and has four bugs that show on edge cases.
2. **The follow-up.** "Can you do better?" The answer tries every start with every end. On a year of city data it never finishes.
3. **The design round.** "Riders hold a dock before they return a bike. Rules must plug in without editing the class, and two riders must never hold the same dock." The design breaks both requirements.

Your job: fix each answer the way an interviewer expects to see it fixed.

```callout jargon
A **mock loop** is a practice interview loop, run by a colleague who plays the interviewers. A **follow-up** is the extra question an interviewer asks after your first answer works, often "can you make it faster?".
```

### Why the edge cases sink the round

The candidate never tested by hand. Their one example, `{500, -200, 700, -1000, 300}`, has a best run in the middle, a positive answer and small numbers. So it could not show any of the four bugs.

| Input | Right answer | The candidate's answer |
|---|---|---|
| `{-100, 200, 300}` | 500 | 200: both loops stop one day early |
| `{-300, -50, -200}` | -50 | 0: `best` starts at 0, an empty run nobody chose |
| `{}` | throw `IllegalArgumentException` | 0 |
| `{2_000_000_000, 2_000_000_000, 1}` | 4,000,000,001 | 2,000,000,000: the `int` total wraps round |

In a real loop, the interviewer would type each of these. Finding them yourself is the testing signal.

### Two riders, one dock

The candidate's `reserve` first checks "is the dock free?", then later puts the rider in. Two riders tapping at the same moment can both pass the check before either one puts.

```animate
title: Two riders tap for harbor-3 at the same moment
kind: scene
nodes:
  - { id: ana, label: Rider Ana, emoji: "📱", row: 0, col: 0 }
  - { id: ben, label: Rider Ben, emoji: "📱", row: 2, col: 0 }
  - { id: desk, label: DockReservations, emoji: "🗂️", value: "harbor-3: free", row: 1, col: 2 }
  - { id: dock, label: Dock harbor-3, emoji: "🚲", value: "empty", row: 1, col: 4 }
steps:
  - say: Ana and Ben both want harbor-3. Their taps arrive a millisecond apart.
    focus: [ana, ben]
  - say: Ana's request checks first. harbor-3 is free, so she moves on to the rules.
    send: { from: ana, to: desk, label: "free? yes" }
  - say: Ben's request checks before Ana has claimed it. The dock still looks free to him too.
    send: { from: ben, to: desk, label: "free? yes" }
  - say: Ana's request puts her in. Ben's request puts him in a moment later, over Ana's hold.
    set: { desk: "harbor-3: Ben (Ana lost it)" }
  - say: Both apps said "reserved". Two riders now ride towards one dock.
    send: [{ from: desk, to: ana, label: "reserved", back: true }, { from: desk, to: ben, label: "reserved", back: true }]
    set: { dock: "2 riders, 1 dock" }
  - say: The fix is one atomic step. putIfAbsent checks and claims together, so only Ana gets null back. Ben is refused.
    set: { desk: "harbor-3: Ana" , dock: "Ana's" }
```

```callout jargon
**Check-then-act** is code that checks a condition, then acts on it in a separate step. Between the two steps another thread can change the answer. An **atomic** step cannot be split: no other thread sees it half done.
```

## What you need

You learned each idea in an earlier ticket. Here they meet in one incident.

- The coding-round method, testing by hand and edge cases: [Interview prep](/t/module-10-professional/topic-05-interview-prep).
- Big-O, and why O(n²) dies on a million items: [Complexity and Big-O](/t/module-04-dsa/topic-01-big-o).
- One pass that carries a running total: [Prefix sums](/t/module-04-dsa/topic-13-two-pointers) and the "best answer ending here" idea in [DP patterns](/t/module-04-dsa/topic-17-dp-patterns).
- Rules behind an interface, and two riders at once: [Object-oriented design](/t/module-06-craft/topic-13-object-oriented-design).
- `ConcurrentHashMap.putIfAbsent`: [Concurrent collections](/t/module-05-advanced-java/topic-06-concurrent-collections).

### The classes

| Class | What it is |
|---|---|
| `CodingRound.bestRun(int[] dailyNetCents)` | round 1 and its follow-up; returns `long` |
| `BikeType` | `CLASSIC`, `EBIKE`, `CARGO` |
| `ReservationRequest(riderId, dockId, bikeType)` | one tap; `stationId()` gives `"old-mill"` for dock `"old-mill-4"` |
| `ReservationRule` | one rule: `Optional<String> check(request)`, a reason to refuse or empty |
| `ReservationResult(reserved, reason)` | `ok()` has reason `"reserved"`; `refused(reason)` |
| `CargoOnlyAtDepot` | the cargo rule as a plug-in, still a stub |
| `DockReservations(List<ReservationRule> rules)` | round 3; `reserve`, `holder`, `release`, `heldCount` |

### The follow-up: one pass

Every best run ends on some day. So walk the days once, and carry one number: the best total of a run that **ends today**. Each day either joins yesterday's run or starts a new one, whichever is bigger. The best of those totals is the answer. That is O(n) time and O(1) space.

### How the checks race two riders

The race checks start their threads at the same moment with a latch. One check also hands in a rule that waits until both riders are inside `reserve`. So both have passed the "is it free?" check before either can claim the dock. A check-then-put design fails that check every time, not just on unlucky days.

## The tiers

1. **Bronze** resolves the incident. `bestRun` passes every edge case in the table. The last day counts, and one day is a run. All-losing days give the smallest loss. No days throws `IllegalArgumentException`, and totals add up in `long`.
2. **Silver**: the follow-up. A million days finish within 3 seconds, in one pass. Every bronze rule still holds.
3. **Gold**: the design round. Rules plug in through the constructor and run in order, the first refusal winning. The cargo rule lives in `CargoOnlyAtDepot`, not in `reserve`. A held dock is refused with `dock-taken` before any rule runs. Two riders can never hold the same dock.

The exact rules are in the README.

## Recap

- Test by hand before you say "done": empty, one item, all negative, the last item, and the biggest values. A happy-path example hides all four bugs.
- Sums of many cents belong in `long`. An `int` overflows silently past 2,147,483,647.
- "Can you do better?" means finding the repeated work. Carrying the best run that ends today turns O(n²) into one O(n) pass.
- Extensible means new rules are new classes handed in, not new `if` lines in the service.
- Two users at once need an atomic claim, such as `putIfAbsent`. Check-then-act lets both through.

## Common mistakes

**1. Starting `best` at 0.**

```java nocompile
long best = 0;          // invents an empty run worth 0
```

Symptom: `{-300, -50, -200}` gives 0 instead of -50. The bronze check says "0 would be an empty run, which the README does not allow". Fix: start `best` at the first day, after the empty check.

**2. A running total in `int`.**

```java nocompile
int total = 0;                    // the total of the current run
total += dailyNetCents[end];      // 2,000,000,000 + 2,000,000,000 wraps round
```

Symptom: on `{2_000_000_000, 2_000_000_000, 1}` the total after day two is -294,967,296, not 4,000,000,000, so the overflow check fails. Returning `long` does not help: the total wrapped before it became a `long`. Fix: make every running total a `long`.

**3. Fixing the edge cases but keeping both loops.** Bronze passes, and every silver check fails with "a million days must take one pass". Two nested loops are O(n²) whatever they add up. Fix: one loop that carries the best run ending today.

**4. Running the rules but keeping check-then-put.**

```java nocompile
if (holds.containsKey(dockId)) { return ReservationResult.refused("dock-taken"); }
// ... rules ...
holds.put(dockId, riderId);   // a second rider can get here too
```

Symptom: the gold race check says "Exactly one of rider-7 and rider-8 may hold harbor-3, but 2 did". A `ConcurrentHashMap` alone does not help, because the check and the put are still two steps. Fix: `putIfAbsent`, and refuse with `dock-taken` when it returns someone else's id.

**5. Running the rules before the "is it held?" check.** Symptom: the gold check "a held dock is refused with dock-taken before any rule runs" fails. A rule ran for a dock that was already gone. Fix: check the hold first, then the rules, then claim.

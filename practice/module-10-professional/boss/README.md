# INC-10: The mock interview loop

Kakashi runs your mock loop. He hands you the answers a previous candidate gave in three rounds. Each one is broken. Fix them the way an interviewer expects. Package `jq.m10.incident`.

`BikeType`, `ReservationRequest`, `ReservationRule` and `ReservationResult` are ready. Change `CodingRound`, `CargoOnlyAtDepot` and `DockReservations`.

## Bronze: the coding round (`CodingRound`, TODO 1)

`static long bestRun(int[] dailyNetCents)`. Each element is one day's net takings in cents: fares minus repair costs, so a day can be negative. Return the **largest total of one or more consecutive days**.

The candidate's answer works on their one example and fails on five inputs, from four bugs:

| Input | Answer | Why |
|---|---|---|
| `{500, -200, 700, -1000, 300}` | `1000` | 500 − 200 + 700 |
| `{-100, 200, 300}` | `500` | the best run can end on the last day |
| `{700}` | `700` | one day is a run |
| `{-300, -50, -200}` | `-50` | a run has at least one day, so when every day loses money the best is the smallest loss, not 0 |
| `{}` | throws `IllegalArgumentException` | no days, no run |
| `{2_000_000_000, 2_000_000_000, 1}` | `4_000_000_001` | add up in `long`; an `int` total overflows |

## Silver: the follow-up, "can you do better?" (`CodingRound`, TODO 2)

The same method must handle 1,000,000 days within 3 seconds. The candidate tries every start with every end, which is O(n²): about 500 billion steps. Make it one pass, O(n) time and O(1) extra space. Every bronze rule still holds.

Keep the timer check (`Thread.currentThread().isInterrupted()`) in any nested loop you keep while you work. It lets the checks stop a slow answer.

## Gold: the design round (`CargoOnlyAtDepot`, `DockReservations`, TODOs 3–5)

A rider holds a dock before returning a bike. `DockReservations(List<ReservationRule> rules)` takes its business rules from outside. `reserve(ReservationRequest)` returns a `ReservationResult`: `ReservationResult.ok()` (reason `"reserved"`) or `ReservationResult.refused(reason)`.

The interviewer's two requirements, which the candidate's design breaks:

1. **Extensible without editing.** A new rule plugs in as a `ReservationRule` handed to the constructor. `reserve` must not hard-code any rule.
2. **Safe under concurrent use.** Many riders tap at the same moment. Two riders must never hold the same dock.

`reserve` works in this order:

1. If the dock is already held, refuse with `"dock-taken"`. **No rule runs** for a held dock.
2. Run every rule, **in the order given**. The first rule that returns a reason refuses the request with that reason, and later rules do not run.
3. Claim the dock for `request.riderId()`. The claim and the "is it free?" check must be **one atomic step**: if another rider claimed it meanwhile, refuse with `"dock-taken"`. A `ConcurrentHashMap` with `putIfAbsent` does this. A `synchronized` method works too.

A refused request never holds a dock. `holder(dockId)` returns the rider holding a dock, or `Optional.empty()`. `heldCount()` counts held docks.

`CargoOnlyAtDepot` is the cargo rule as a plug-in: a `CARGO` bike at any station other than `depot` is refused with `"cargo-needs-depot"`; everything else is allowed. `request.stationId()` gives the station part of a dock id (`"old-mill-4"` gives `"old-mill"`). With no rules, a cargo bike may hold any dock.

The concurrency checks start two or four threads at the same moment. One check uses a rule that waits until both riders are inside `reserve`, so a check-then-put design fails it every time.

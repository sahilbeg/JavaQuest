# Greedy choices and intervals

Jiraiya wants the dock booking screen to plan its day. Every rider booking or maintenance job is an `Interval(start, end)`, in minutes after midnight. Minute 600 is 10:00.

Every window is **half-open**: it includes `start` and stops just before `end`. So 600-660 and 660-720 only touch. They do **not** overlap, and one dock can serve both. `new Interval(start, end)` already throws `IllegalArgumentException` when `end <= start`.

1. **TODO 1:** in `Interval`, `overlaps(other)` returns `true` when the two windows share at least one minute: each one starts before the other one ends. Touching windows return `false`.

The methods in `DockScheduler` must never change the list they are given. It may be unmodifiable, like `List.of(...)`, so sort a copy (`new ArrayList<>(list)`). An empty list gives an empty list or 0. Sort with `Comparator.comparingInt(Interval::start)` or `Interval::end`. Each method should run in O(n log n) time: the big test has 100000 bookings and a 2-second limit.

2. **TODO 2:** `mergeBusyPeriods(bookings)` copies the bookings and sorts the copy by start.
3. **TODO 3:** it then merges every group of overlapping bookings into one window, from the earliest start to the **latest** end in the group. Touching windows stay separate. It returns a new list sorted by start. Example: 900-960, 600-660, 630-700 gives 600-700, 900-960.
4. **TODO 4:** `mostBookings(requests)` returns the largest number of requests that can all be booked on one dock, with no two overlapping. A booking may start the minute the previous one ends. Sorting by end time and taking every request that fits is the greedy choice that always works.
5. **TODO 5:** `docksNeeded(bookings)` returns the most bookings happening at the same moment, which is the number of docks needed. A booking that starts at the minute another ends can reuse its dock. Sort all starts and all ends, then sweep through them.
6. **TODO 6:** `insertMaintenanceWindow(booked, window)` gets `booked` already sorted by start with no overlaps. It returns a new list with the window added, sorted by start. Every booking that overlaps the window merges with it into one window, from the earliest start to the latest end. Windows that only touch the new one stay separate.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.

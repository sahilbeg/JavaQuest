# Mock coding round: three problems

Tsunade booked you a practice round before the real loop. It has three problems, from easier to harder, each in its own class in package `jq.m10.interview`. Treat each one like an interview. Read the contract and write down the edge cases. Think of the brute force and its cost, then write the faster version.

Every check has a time limit. A large-input check fails with a timeout when the code is too slow, so the complexity targets below matter.

## 1. `RideTimeline`: where was a bike at a given second?

The live map stores each sighting of a bike: its id, the second it was seen, and the station. Seconds count from when the city opened, as a `long`. Support questions like "where was B-1042 at second 250?".

- **TODO 1–3, `void put(String bikeId, long second, String station)`:** remember the sighting.
  - For one bike, times must go **strictly up**. A `second` equal to or smaller than that bike's last sighting throws `IllegalArgumentException`, and nothing is stored.
  - The rule is per bike: B-7 may be seen at second 300 after B-1042 was seen at second 900.
- **TODO 4–5, `String get(String bikeId, long second)`:** return the station of the **latest sighting at or before** `second`.
  - A sighting at exactly `second` counts.
  - After the last sighting, the answer is the last station.
  - Before the bike's first sighting, return `null`. A bike never seen also gives `null`, not an exception.
- Target: `put` is O(1) amortised, `get` is O(log n) for a bike with n sightings. Because each bike's times go up, its list is already sorted, so `get` can binary search for the last sighting whose second is `<= second`. Space: O(total sightings).
- The large check makes 200,000 sightings of one bike, then 200,000 lookups, within 2 seconds.

## 2. `RentalCounter`: rentals in the last 300 seconds

The ops dashboard shows how many rentals started in the last five minutes. Seconds are `long` values from 0 up.

- **TODO 6–8, `void record(long second)`:** count one rental that started at `second`. Many rentals can share a second.
- **TODO 9, `int countRecent(long second)`:** how many recorded rentals started from `second - 299` to `second`, both included. So a rental at second 0 still counts at second 299, and is gone at second 300.
- **TODO 10, time only goes forward:** both methods throw `IllegalArgumentException` for a negative `second`. They also throw for a `second` smaller than the largest second either method has seen. The same second again is fine.
- Target: O(1) work per call and O(1) memory, using `WINDOW_SECONDS` (300) buckets reused in a circle. Bucket `second % 300` holds one second and its count. When a new second lands in a bucket that holds an older second, reset that bucket first. A bucket counts only while `second - bucketSecond < 300`. `countRecent` looks at 300 buckets, which is a constant, so it is O(1).
- The large check records 1,000,000 rentals (two a second) and calls `countRecent` 100,000 times, within 2 seconds.

## 3. `GroupBooking.mostBikeHours(int[] freeBikes)`: the biggest group booking

`freeBikes[i]` is how many bikes a station has free in hour `i`. A school wants the same number of bikes for every hour of a trip. Pick a run of consecutive hours and a number of bikes that is free in **every** hour of the run. The booking is worth bikes × hours. Return the largest value possible, as a `long`.

- `{2, 1, 5, 6, 2, 3}` gives 10: 5 bikes for hours 2 and 3.
- No hours (an empty array) gives 0. An hour with 0 free bikes splits the day.
- A negative count throws `IllegalArgumentException`.
- Multiply as `long`: two hours of 2,000,000,000 bikes give 4,000,000,000, more than an `int` holds.
- **TODO 11–12.** Target: O(n) time with a **monotonic stack** of hour indexes whose free bikes go up from bottom to top. When hour `i` has no more bikes than the top hour, pop the top. The popped hour is the lowest hour of a block. The block starts just after the new top, or at hour 0 when the stack is empty, and ends just before `i`. Process one extra step at `i = freeBikes.length` with 0 bikes, so every hour gets popped. Space: O(n) for the stack.
- The large check runs 200,000 rising hours and 200,000 flat hours within 2 seconds. Growing a block outward from every hour is O(n²) and times out.

## Before you submit

Say each method's time and space complexity out loud, as you would in the interview.

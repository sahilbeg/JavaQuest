# Performance: measure, then make the report fast

Kakashi's ride-history report looks fine on average, but some riders wait seconds. Three jobs: measure latency properly with percentiles, size the server with Little's law, then fix the two hotspots in the report.

## 1. `LatencyStats`: percentiles by nearest rank

Samples are response times in whole milliseconds, in a `long[]`, in any order.

- **TODO 1-3, `percentile(millis, percent)`:** `percent` is a whole number from 1 to 100. Sort a **copy** of the samples, smallest first. The rank is `percent × n / 100`, rounded **up**: use `Math.ceilDiv((long) percent * n, 100)`. Return the sample at that rank, counting from 1, so `sorted[rank - 1]`.
  - Example: 10 samples, p50 → rank 5, the 5th smallest. p21 → rank ceil(2.1) = 3. p99 → rank ceil(9.9) = 10, the slowest one.
  - p100 is always the slowest sample (the max).
  - Throw `IllegalArgumentException` when `percent` is below 1 or above 100.
  - Throw `IllegalArgumentException` when the array is empty: a percentile of no samples is undefined.
  - Never change the caller's array. `Arrays.sort(millis)` would reorder their data. Use `Arrays.copyOf` first.
- **TODO 4, `summarize(millis)`:** return `new LatencySummary(count, p50, p95, p99, max)`. For an **empty** array, return `new LatencySummary(0, 0, 0, 0, 0)`, so a dashboard with no traffic still shows something.

## 2. `LittlesLaw`: how much capacity you need

Little's law: requests in flight = arrival rate × time each one spends in the system. Latency is given in milliseconds, so divide by 1000.

- **TODO 5, `inFlight(requestsPerSecond, latencyMillis)`:** `requestsPerSecond × latencyMillis / 1000`, rounded **up** (`Math.ceilDiv`). 200 requests a second at 250 ms is 50. 30 at 120 ms is 3.6, so 4.
- **TODO 6, `threadsNeeded(requestsPerSecond, latencyMillis, targetUtilisationPercent)`:** enough threads that each is busy at most the target percent of the time. That is the exact in-flight value divided by `target / 100`, rounded **up**: `Math.ceilDiv(requestsPerSecond * latencyMillis * 100, 1000L * target)`. 200 at 250 ms with a 70% target is 50 / 0.7 = 71.4, so 72. Divide the exact value, not the already-rounded `inFlight`.
- **TODO 7, `maxRequestsPerSecond(threads, latencyMillis)`:** `threads × 1000 / latencyMillis`, rounded **down** (plain `long` division). 20 threads at 250 ms is 80. 3 threads at 400 ms is 7.5, so 7.
- The rounding rule: round toward the safe side. Things you must provide (threads, connections) round up. A rate you promise rounds down.
- Throw `IllegalArgumentException` for a negative rate, a negative latency, a negative thread count, a latency of 0 or less in `maxRequestsPerSecond`, or a target outside 1 to 100.

## 3. `RideHistoryReport.build`: make the report fast

`slowBuild` is the old code. Its output is right, so keep it as the reference, and do not call it from `build`. It has two hotspots:

- For every ride, it scans the whole riders list to find a name. That is O(rides × riders).
- `report += ...` copies the whole report so far on every line. That is O(n²) characters copied.

**TODO 8-10, `build(rides, riders)`:** return exactly the same text as `slowBuild`.

- Read the riders list **once**, into a `HashMap` from rider id to name. Each lookup is then O(1). Rider ids in the list are unique.
- One line per ride, in the rides' order: `rentalId name minutes min fareCents cents`, then a new line (`\n`). Example: `rental-17 Ada 12 min 460 cents`.
- A rider id that is not in the list shows the name `unknown rider`.
- Last line: `total <number of rides> rides <sum of fareCents> cents`, then `\n`. With no rides, the report is just `total 0 rides 0 cents\n`.
- Build the text in one `StringBuilder`.

The checks build a report of 150,000 rides and 50,000 riders, and allow 3 seconds. The old code needs far longer. They also count how many times your code reads the riders list: at most twice per rider.

## How the checks work

Plain Java, no network, no clock. If your code runs past a time limit, the check stops it.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

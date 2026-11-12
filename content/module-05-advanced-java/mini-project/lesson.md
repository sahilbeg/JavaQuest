## The design

Each telemetry line has three parts, separated by `|`:

```text
B-1042|BATTERY|80
B-1042|GPS|51.51,-0.12
B-2001|FAULT|E7
B-3001|BATTERY|lots
```

The last line is broken: `lots` is not a number. The ingester counts it as a bad line and carries on.

A sealed interface lists the three kinds of reading:

```java nocompile
public sealed interface Reading permits Battery, Gps, Fault {}
public record Battery(String bikeId, int percent) implements Reading {}
public record Gps(String bikeId, double lat, double lon) implements Reading {}
public record Fault(String bikeId, String code) implements Reading {}
```

```callout tip
The report must come out the same however the threads happen to run. So it keeps the lowest battery level and counts faults. Neither result depends on which line a thread handles first.
```

## What you build

All of these are in package `jq.m05.feature`. The records and `IngestReport` are already written.

1. `TelemetryIngester.parse(line)` returns a `Battery`, `Gps` or `Fault`. It throws `IllegalArgumentException` for a line without 3 parts, an unknown kind or a bad number. A GPS value must be exactly two numbers, `lat,lon`. Anything else is a bad line.
2. `TelemetryIngester.ingest(lines, workers)` returns an `IngestReport`:
   - `lowestBattery`: the lowest battery level seen per bike, sorted by bike id.
   - `faults`: the number of faults per bike, sorted by bike id.
   - `gpsFixes`: how many GPS lines there were.
   - `badLines`: how many lines failed to parse.
   - Split the lines into `workers` chunks and run each chunk on a fixed thread pool of `workers` threads.
3. `TelemetryIngester.needsCharge(report, below)` returns the bike ids whose lowest battery is under `below`, sorted.

### Syntax: one switch for every kind of reading

```java nocompile
switch (reading) {
    case Battery(var bike, var percent) -> lowest.merge(bike, percent, Math::min);
    case Fault(var bike, var code) -> faults.merge(bike, 1, Integer::sum);
    case Gps g -> gpsFixes.incrementAndGet();
}
```

The interface is sealed, so the compiler checks that the switch covers every kind.

## Recap

- A sealed interface plus a switch keeps a closed set of message types safe to extend.
- Parallel work needs results that do not depend on timing: counts, sums and minimums.
- Wait for every task before you read the shared maps.

## Common mistakes

**Using a plain HashMap for shared results.** Several threads update it at once, so counts go missing or the map breaks.

**Reading the results before the pool finishes.** Call `get()` on every `Future`, or close the executor, before you build the report.

## The design

A station's log is a CSV file. The first line is a header:

```text
bikeId,station,type,minutes
B-1042,Harbor,ebike,40
B-2001,Riverside,classic,30
B-3001,Harbor,cargo,twenty
B-4001,Old Mill,tandem,10

B-5001,Harbor,ebike,25
```

Line 4 has bad minutes and line 5 has an unknown bike type. Line 6 is blank, so it is skipped silently. The report for this file is:

```text
Rides: 3 (2 bad lines)
Revenue: $27.00
Busiest station: Harbor
classic: 30 min
ebike: 65 min
line 4: bad minutes: twenty
line 5: unknown type: tandem
```

```callout tip
This feature uses most of module 3: Files, records, exceptions, maps, streams and Optional.
```

## What you build

All of these are in package `jq.m03.feature`.

1. `record Ride(String bikeId, String station, String type, int minutes)` with `fare()`: 1.00 plus minutes times 0.15, 0.30 or 0.25.
2. `record ImportResult(List<Ride> rides, List<String> errors)`.
3. `RideLogReader.read(Path file)` reads the file and returns an `ImportResult`. It throws `IOException` only when the file cannot be read.
   - Line numbers count from 1, and the header is line 1.
   - A line that does not have 4 parts gives `line N: expected 4 values`.
   - Minutes that are not a number give `line N: bad minutes: X`.
   - A type other than classic, ebike or cargo gives `line N: unknown type: X`.
4. `RideAnalytics` with these static methods:
   - `revenue(rides)` adds up every ride's fare.
   - `busiestStation(rides)` returns an `Optional<String>`: the station with the most minutes. It is empty for no rides.
   - `minutesPerType(rides)` returns a `TreeMap`, sorted by type.
   - `report(result)` returns the text above.

### Syntax: the report lines

```java nocompile
String first = String.format("Rides: %d (%d bad lines)", rides.size(), errors.size());
String money = String.format("Revenue: $%.2f", revenue);
String busiest = "Busiest station: " + busiestStation(rides).orElse("none");
```

Then one line per type, like `ebike: 65 min`, then one line per error. Join everything with `\n`, with no newline at the end.

## Recap

- Read the whole file, then handle each line on its own.
- One broken line becomes an error message. It never stops the import.
- `Optional` makes "no busiest station" an honest answer.

## Common mistakes

**Line numbers that start at 0.** Editors and people count lines from 1. The header is line 1.

**Crashing on the first bad line.** Catch the problem for that line, record it, and move on to the next.

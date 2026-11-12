# Three crash reports from last night

Sam's support inbox has three reports from the nightly ride import. The developer who wrote this code is on holiday. Each test in `CrashReportsTest` is a failing reproduction of one report. Read the report, find the line, fix the bug, and watch that test turn green.

## Crash report 1: the Harbor ride with no note

The import crashed on this line from the Harbor kiosk:

```
B-1042,Harbor,12,
```

```
Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index 3 out of bounds for length 3
	at jq.m06.debugging.RideParser.parse(RideParser.java:11)
	at jq.m06.debugging.NightlyImport.main(NightlyImport.java:5)
```

The rider left the note empty, so the line ends with a comma.

## Crash report 2: the new Depot station

Depot opened yesterday and is not in the station index yet. The dock screen asked for its free docks:

```
Exception in thread "main" java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "java.util.Map.get(Object)" is null
	at jq.m06.debugging.StationIndex.docksAt(StationIndex.java:18)
	at jq.m06.debugging.NightlyImport.main(NightlyImport.java:6)
```

## Crash report 3: the refund that paid nothing

This one did not crash. A rider paid 280 cents for a 12 minute ride, and the bike broke after 6 minutes. Support expected a 140 cent refund. The program printed:

```
refund: 0 cents
```

## Your tasks

1. **TODO 1:** fix `RideParser.parse` so a line with an empty last field parses, with an empty note. A line without exactly 4 fields should throw `IllegalArgumentException` with a clear message.
2. **TODO 2:** fix `StationIndex.docksAt` so a station missing from the index returns 0.
3. **TODO 3:** fix `RefundCalculator.refundCents` so a partial refund is the paid amount times the lost share, rounded down to whole cents.

Money in this topic is in whole cents, stored as `long`.

Press **Submit** in JavaQuest, or just save the files in your IDE. The tests run on their own.

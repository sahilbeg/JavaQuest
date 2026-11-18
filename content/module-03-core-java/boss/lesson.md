## What happened

The monthly report has three jobs:

1. Drop the rides that the workshop made while testing bikes. Their ids start with `TEST-`.
2. Work out the average ride length per station.
3. List the busiest stations, by total minutes.

Each job has a bug. None of them is a typo, and two of them don't even crash.

```callout analogy
A report is like a bike that passed a quick look. The chain only slips when someone rides it uphill.
```

## What you need

Everything here comes from module 3.

### Syntax: removing safely

```java nocompile
rides.removeIf(ride -> ride.bikeId().startsWith("TEST-"));
```

`removeIf` walks the list for you and removes matching items safely. An `Iterator` with `remove()` also works.

### Syntax: a real average

```java nocompile
int total = 45;
int count = 2;
double average = (double) total / count;   // 22.5
```

Cast before you divide. `(double) (total / count)` is too late: the decimals are already gone.

### Syntax: sorting map entries

```java nocompile
List<Map.Entry<String, Integer>> entries = new ArrayList<>(minutes.entrySet());
entries.sort(Map.Entry.<String, Integer>comparingByValue().reversed()
        .thenComparing(Map.Entry.comparingByKey()));
```

`<String, Integer>` after `Map.Entry.` is a **type witness**: you tell Java the key and value types by hand. Java cannot work them out when you chain `.reversed()` straight onto the call. Leave it out and the compiler says `incompatible types: Comparator<Entry<Object,V>> cannot be converted to Comparator<? super Entry<String,Integer>>`.

```callout jargon
A **type witness** is a type argument written by hand in front of a generic method call, like `Map.Entry.<String, Integer>comparingByValue()`.
```

A simpler way to get largest first is `Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())`. Alone, as `entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()))`, it needs no witness. You still need `thenComparing` for the alphabetical tie-break.

You may also use a stream with `sorted(...)` and `limit(n)`.

## The tiers

1. **Bronze** resolves the incident: `removeTestRides(rides)` removes every ride whose bike id starts with `TEST-`, without crashing, and returns how many it removed.
2. **Silver**: `averageMinutesPerStation(rides)` returns a `Map<String, Double>` with the exact average per station, such as `22.5`.
3. **Gold**: `topStations(rides, n)` returns the `n` stations with the most total minutes, largest first. Ties go in alphabetical order.

## Recap

- Never remove from a list inside its own for-each loop.
- `int / int` is whole-number division. Cast first.
- `HashMap` order means nothing. Sort before you report.

## Common mistakes

**Catching the exception instead of fixing the loop.** A `try`/`catch` around the loop stops the crash, but it also stops the removing halfway through.

**Sorting by minutes only.** Two stations with the same total must come out in alphabetical order, or the report changes from run to run.

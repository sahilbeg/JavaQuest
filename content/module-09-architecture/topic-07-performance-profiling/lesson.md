## Hook

A school bus takes 20 minutes on most days. Once a week it takes two hours, because it waits at a broken bridge. The "average trip" is about 35 minutes, which sounds fine. Yet one day a week, every kid on that bus misses first period.

Spoke's ride-history report has the same shape. The dashboard shows a healthy average, while 1 rider in 100 waits five seconds. In this ticket you learn to measure speed the way production engineers do, find where the time really goes, and only then fix it.

## Concept

### Measure before you optimise

**Optimising** means changing code so it uses less time or memory. The classic mistake is to optimise the part you *think* is slow.

Programs spend most of their time in a tiny part of the code. That part is the **hotspot**. Your guess about where it is will usually be wrong, even after years of practice.

```callout jargon
A **hotspot** is the small piece of code where most of the time goes. A **bottleneck** is the one resource that limits the whole system, such as the CPU, the database or one lock.
```

So performance work is a loop:

1. **Measure** the real symptom with numbers, such as "p99 is 5 seconds".
2. **Profile** to find where the time goes.
3. **Fix** that one hotspot.
4. **Measure again**, the same way, to prove it helped.

Trap: a clever rewrite of fast code makes it harder to read and saves nothing. Spoke once rewrote the fare maths with bit tricks. The report stayed exactly as slow, because the time was in a lookup.

### Latency, throughput and utilisation

Three words describe how fast a system is. They measure different things.

```callout jargon
**Latency** is how long one request takes, start to finish, such as 180 ms. **Throughput** is how many requests finish per second. **Utilisation** is the share of time a resource is busy, such as "the CPU is 70% busy".
```

A Spoke example: the ride-history endpoint answers in 180 ms on average (latency). It serves 40 requests a second (throughput). Its 8 worker threads are busy 90% of the time (utilisation).

They pull against each other. Batching 100 GPS pings into one database write raises throughput, but each ping waits longer, so latency rises. Riders feel latency. Your server bill follows throughput and utilisation.

### Percentiles: p50, p95, p99, p99.9

A **percentile** answers "how slow were the slowest few?". Sort every response time, smallest first. p95 is the value that 95% of requests were at or below.

```callout jargon
**p50** (the median) is the middle request. **p95**, **p99** and **p99.9** show the slow end. The slowest 1% of requests is called the **tail**, and its latency is the **tail latency**.
```

The simplest exact method is **nearest rank**:

1. Sort the n samples, smallest first.
2. rank = p × n / 100, rounded **up**.
3. The answer is the sample at that rank, counting from 1.

By hand, with ten response times in ms: 12, 15, 11, 14, 13, 19, 18, 17, 16, 240. Sorted: 11 12 13 14 15 16 17 18 19 240.

- p50: rank = 50 × 10 / 100 = 5, so the 5th value, **15 ms**.
- p90: rank = 9, so **19 ms**.
- p99: rank = 9.9, rounded up to 10, so **240 ms**.

With only ten samples, p99 is just the maximum. You need at least 100 samples for a meaningful p99, and 1,000 for p99.9.

```animate
title: Nearest-rank p95 of five requests
kind: code
code: |
  long[] millis = {40, 10, 30, 20, 900};
  long[] sorted = millis.clone();
  java.util.Arrays.sort(sorted);
  long rank = Math.ceilDiv(95L * sorted.length, 100L);
  System.out.println("p95 = " + sorted[(int) rank - 1] + " ms");
steps:
  - line: 1
    vars: { millis: "@a1" }
    objects: { a1: { type: "long[]", fields: { 0: 40, 1: 10, 2: 30, 3: 20, 4: 900 } } }
    say: Five response times, in the order the requests arrived. One rider waited 900 ms.
  - line: 2
    vars: { sorted: "@a2" }
    objects: { a2: { type: "long[]", fields: { 0: 40, 1: 10, 2: 30, 3: 20, 4: 900 } } }
    say: Copy the array first. The caller's data must keep its order.
  - line: 3
    objects: { a2: { type: "long[]", fields: { 0: 10, 1: 20, 2: 30, 3: 40, 4: 900 } } }
    say: Sort only the copy, smallest first.
  - line: 4
    vars: { rank: 5 }
    say: 95 x 5 / 100 = 4.75. Nearest rank rounds up, so the rank is 5.
  - line: 5
    out: "p95 = 900 ms"
    say: Rank 5 is index 4, the slowest request. With five samples, p95 is the max.
```

### Syntax: nearest-rank percentile

```java
static long percentile(long[] millis, int percent) {
    long[] sorted = java.util.Arrays.copyOf(millis, millis.length);
    java.util.Arrays.sort(sorted);
    long rank = Math.ceilDiv((long) percent * sorted.length, 100L);
    return sorted[(int) rank - 1];
}
```

`Math.ceilDiv(a, b)` divides and rounds up, with whole numbers only, so no `double` rounding errors creep in. Sorting costs O(n log n) time and the copy O(n) space.

Real monitoring tools cannot keep every sample forever. They count samples in **buckets** (0-10 ms, 10-25 ms, and so on) and estimate percentiles from the counts. A later ticket on observability shows this.

### Why averages lie

An average adds everything up and divides. A few huge values get blended into many small ones, and both disappear.

```java run
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        long[] millis = new long[100];
        Arrays.fill(millis, 100);     // 98 quick requests...
        millis[13] = 5000;            // ...and 2 slow ones
        millis[77] = 5000;

        long sum = 0;
        for (long m : millis) {
            sum += m;
        }
        System.out.println("average: " + sum / millis.length + " ms");

        long[] sorted = Arrays.copyOf(millis, millis.length);
        Arrays.sort(sorted);
        for (int p : new int[] {50, 95, 99}) {
            long rank = Math.ceilDiv((long) p * sorted.length, 100L);
            System.out.println("p" + p + ": " + sorted[(int) rank - 1] + " ms");
        }
    }
}
```

```text
average: 198 ms
p50: 100 ms
p95: 100 ms
p99: 5000 ms
```

No rider ever waited 198 ms. Most waited 100 ms, and two waited 5 seconds. The average describes nobody. p50 tells you the typical rider, and p99 tells you the unlucky one.

Why care about 1%? Spoke serves 100,000 riders. If each opens the app ten times a day, 1% is 10,000 slow screens a day. Heavy users open the app most, so they hit the tail most.

### Tail latency grows with fan-out

**Fan-out** means one request calls many backends and waits for all of them. The rider's home screen might call rides, payments, stations and promos at once.

The screen is only as fast as its *slowest* call. Say each call is slow 1% of the time. The chance that at least one of n calls is slow is 1 − 0.99ⁿ.

```java run
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        double fast = 0.99;   // each backend call is fast 99% of the time
        for (int calls : new int[] {1, 10, 50, 100}) {
            double slow = 1 - Math.pow(fast, calls);
            System.out.printf(Locale.ROOT, "%3d calls: %.1f%% of requests wait for a slow one%n", calls, slow * 100);
        }
    }
}
```

```text
  1 calls: 1.0% of requests wait for a slow one
 10 calls: 9.6% of requests wait for a slow one
 50 calls: 39.5% of requests wait for a slow one
100 calls: 63.4% of requests wait for a slow one
```

With 100 backend calls, one backend's rare p99 becomes the *common* case. This is **tail latency amplification**. It is why big companies set targets on p99 and p99.9 of every backend, not on the average.

```animate
title: One slow backend slows the whole screen
kind: scene
nodes:
  - { id: app, label: Rider app, emoji: "📱", value: waiting }
  - { id: api, label: Home screen API, emoji: "🖥️", row: 0, col: 2 }
  - { id: rides, label: Rides, emoji: "🚲", row: 1, col: 1 }
  - { id: pay, label: Payments, emoji: "💳", row: 1, col: 2 }
  - { id: promo, label: Promos, emoji: "🎁", row: 1, col: 3 }
steps:
  - say: The rider opens the app. One request reaches the home screen API.
    send: { from: app, to: api, label: "GET /home" }
  - say: The API fans out. It calls three backends at the same time.
    send: [{ from: api, to: rides, label: "rides?" }, { from: api, to: pay, label: "cards?" }, { from: api, to: promo, label: "promos?" }]
  - say: Rides and Payments answer in about 20 ms each.
    set: { rides: "20 ms", pay: "22 ms" }
    send: [{ from: rides, to: api, label: "ok", back: true }, { from: pay, to: api, label: "ok", back: true }]
  - say: Promos hits its rare slow path. The API cannot answer until every part is back.
    set: { promo: "1900 ms" }
    focus: [promo]
  - say: The rider waits 1.9 seconds, though two of the three calls were fast. The slowest call sets the pace.
    send: { from: promo, to: api, label: "ok", back: true }
    set: { app: "1.9 s" }
```

Fixes include timeouts with a fallback, sending a second "hedge" request when the first is slow, and fewer calls per screen.

### Little's law: how many are in flight

You used **Little's law** in [system design](/t/module-09-architecture/topic-06-system-design) to count bikes on the road. It links three numbers in any stable system, from a shop queue to a web server:

```callout jargon
**Little's law**: L = λ × W. **L** is how many items are inside the system at once. **λ** (lambda) is the arrival rate. **W** is how long each item stays.
```

For bikes: in the morning rush, 5,000 rides start in one hour. That is λ = 5,000 / 60 ≈ 83.3 rides a minute. A ride lasts 20 minutes on average, so W = 20.

L = 83.3 × 20 ≈ 1,667, so **about 1,700 bikes on the road at once**. Spoke has 2,000 bikes, so over 80% are out. Each riding bike sends a GPS ping every 10 seconds: about 1,700 / 10 = **170 pings a second**.

The same law sizes a server. The ride-history endpoint gets 40 requests a second, and each takes 0.18 s on average. L = 40 × 0.18 = **7.2 requests in flight**. Its pool has 8 threads, so they are 7.2 / 8 = 90% busy. Any burst queues, and the queue is where p99 comes from.

Turn it around for throughput: λ = L / W. Those 8 threads at 0.18 s each can finish at most 8 / 0.18 ≈ 44 requests a second. At 40, there is almost no room left.

Rounding rule: things you must provide, such as threads, round **up**. A rate you promise rounds **down**.

### Why latency explodes near 100% busy

Requests do not arrive evenly. Sometimes three land in the same millisecond. A server that is busy most of the time has no slack to soak up those bursts, so a queue builds.

Queueing theory gives a rule of thumb for random arrivals at one server. If serving takes S, the average time in the system is S / (1 − utilisation). With S = 50 ms:

| Busy | Time in system |
|---|---|
| 50% | 50 / 0.5 = 100 ms |
| 80% | 50 / 0.2 = 250 ms |
| 90% | 50 / 0.1 = 500 ms |
| 99% | 50 / 0.01 = 5,000 ms |

Going from 90% to 99% busy makes requests ten times slower. That curve is called the **hockey stick**. So teams plan for 60-70% utilisation at peak, and keep the rest as headroom. Spoke's ride-history pool, at 90%, sits on the steep part.

Trap: "the CPU is only 70% busy, so we have 30% spare" is wrong at the tail. The p99 is already climbing long before 100%.

### Amdahl's law: the part you cannot split

Adding cores or threads only speeds up the part of the work that can run in parallel. **Amdahl's law** says how much that helps:

```callout jargon
**Amdahl's law**: speedup = 1 / ((1 − p) + p / n), where **p** is the share of the work that can run in parallel and **n** is the number of cores.
```

Spoke's nightly report spends 10% of its time reading one big file in order. The other 90% can be split across cores, so p = 0.9.

- 8 cores: 1 / (0.1 + 0.9 / 8) = 1 / 0.2125 ≈ **4.7× faster**, not 8×.
- 64 cores: 1 / (0.1 + 0.9 / 64) ≈ **8.8×**.
- Infinite cores: 1 / 0.1 = **10×**, never more.

The serial 10% caps you. So before buying a bigger machine, shrink the part that cannot be split. Locks count as serial parts too: threads queue at a lock one at a time.

### CPU-bound, I/O-bound or waiting on a lock

Before you pick a fix, find out what the slow code is *doing* while it is slow. There are three main answers.

| Kind | What you see | Spoke example | Typical fix |
|---|---|---|---|
| **CPU-bound** | CPU near 100%, threads `RUNNABLE` | scanning a list for every ride | a better algorithm or data structure |
| **I/O-bound** | CPU low, threads waiting on the network or disk | one database query per ride | fewer round trips, batching, caching |
| **Lock-bound** | CPU low, many threads `BLOCKED` on one monitor | every request updating one `synchronized` counter | smaller locks, less shared state |

```callout jargon
**I/O** (input/output) is any talk with the outside: disk, network, database. Code that waits on it is **I/O-bound**. Code limited by the processor is **CPU-bound**.
```

The quick test is a few **thread dumps**, taken seconds apart with `jcmd <pid> Thread.print`. You met them in [debugging](/t/module-06-craft/topic-11-debugging). If the same stack shows up again and again, that is your hotspot.

Trap: more threads help I/O-bound work, because waiting threads cost little. They make CPU-bound work slower, because threads fight over the same cores.

### Profilers: sampling vs instrumenting

A **profiler** is a tool that shows where a running program spends its time or memory. There are two ways to build one.

```callout jargon
A **sampling profiler** looks at every thread's stack many times a second and counts what it sees. An **instrumenting profiler** adds timing code around every method call.
```

| | Sampling | Instrumenting |
|---|---|---|
| Overhead | about 1-2%, safe in production | often 2-10× slower |
| Accuracy | statistical: needs enough samples | exact call counts |
| Distortion | small | tiny methods look slow, because the timer costs more than they do |

Use sampling in production and for "where does the time go?". Many old Java profilers only sampled at **safepoints**, moments when the JVM may pause a thread. That hides hot loops. JFR and async-profiler avoid this bias.

### Syntax: Java Flight Recorder

**Java Flight Recorder** (JFR) is a sampling profiler built into the JDK. It records CPU samples, allocations, GC pauses, locks and I/O into one `.jfr` file, at about 1% overhead.

```bash
# start recording when the app starts
java -XX:StartFlightRecording=duration=60s,filename=report.jfr -jar spoke-api.jar

# or attach to a running JVM (find the pid with: jcmd)
jcmd <pid> JFR.start name=report settings=profile duration=60s filename=report.jfr

# list the hottest methods (JDK 21)
jfr view hot-methods report.jfr
```

- `duration=60s` records for one minute, then writes the file.
- `settings=profile` samples more often than the default settings.
- **JDK Mission Control** (JMC) is a free desktop app that opens `.jfr` files, with flame graphs and charts.
- **async-profiler** is a popular open-source sampling profiler for Linux and macOS: `asprof -d 30 -f flame.html <pid>` writes a flame graph after 30 seconds. It can also profile allocations (`-e alloc`) and locks (`-e lock`).

### Reading a flame graph

A **flame graph** draws thousands of stack samples as one picture. Here is a sketch of the old ride-history report:

```text
[ArrayList$Itr.next][ String.equals ...................... ]
[ findStation .................................................... ][ Integer.toString ]
[ oldReport ............................................................................ ]
[ handleRideHistory .................................................................... ]
[ Thread.run ........................................................................... ]
```

- Each box is a method. The box under it is its caller. The root is at the bottom.
- **Width** is the share of samples, so the share of time. Wide means expensive.
- Boxes on the same level are sorted alphabetically, left to right. It is **not** time order.
- Colours are random. They mean nothing.

Look for a **wide plateau**: a wide box with a flat top, where nothing sits above it. That method burns the time itself. Here the plateau is `String.equals`, and the box under it is `findStation`. The report spends its life comparing ids while it searches a list.

### Microbenchmarks lie

A **microbenchmark** times one small piece of code, such as "is `HashMap.get` faster than a list scan?". In Java, a naive one gives wrong numbers for four reasons.

1. **JIT warm-up.** The JVM first interprets bytecode, then the **JIT compiler** turns hot methods into fast machine code. The first thousand calls can be many times slower than later ones.
2. **Dead-code elimination.** If you never use a result, the JIT may delete the computation. Your loop "takes" 0 ns.
3. **Constant folding.** If the input is a constant, the JIT may compute the answer once while compiling.
4. **GC noise.** A garbage collection can land inside one measurement and make it look ten times slower.

```callout jargon
A **JIT** (just-in-time) compiler turns bytecode into machine code while the program runs. It watches which methods are hot and optimises those, sometimes several times.
```

So `long start = System.nanoTime(); work(); long end = System.nanoTime();` around one call measures almost nothing useful.

### Syntax: a JMH benchmark

**JMH** (Java Microbenchmark Harness) is the OpenJDK tool that handles all four traps for you.

```java nocompile
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(2)
public class StationLookupBenchmark {
    List<String> list;
    Map<String, String> map;
    String id;

    @Setup
    public void setUp() {
        list = new ArrayList<>();
        map = new HashMap<>();
        for (int i = 0; i < 300; i++) {
            list.add("station-" + i);
            map.put("station-" + i, "Station " + i);
        }
        id = "station-299";   // a field, not a constant, so it cannot be folded
    }

    @Benchmark
    public boolean listContains() {
        return list.contains(id);   // returning the result stops dead-code elimination
    }

    @Benchmark
    public void mapGet(Blackhole hole) {
        hole.consume(map.get(id));  // or hand it to a Blackhole
    }
}
```

- `@Warmup` runs the code first, so the JIT finishes before timing starts.
- `@Fork(2)` repeats everything in two fresh JVMs, so one lucky run does not decide.
- Returning a value, or `Blackhole.consume`, keeps the JIT from deleting the work.

Even a perfect benchmark only answers a small question. Always confirm with a profile of the real program.

### Allocation pressure and the GC

Every `new` object costs a little memory. Allocating in Java is cheap, but **allocation pressure**, millions of short-lived objects a second, makes the garbage collector run often. Each young collection pauses threads briefly, and those pauses land in your p99.

You met the collector in [garbage collection](/t/module-05-advanced-java/topic-08-garbage-collection). To see whether GC hurts latency, turn on GC logging and compare pause times with your slow requests:

```bash
java -Xlog:gc -jar spoke-api.jar
```

JFR's allocation view (`jfr view allocation-by-class report.jfr`) shows which classes are created most. If memory only ever grows, it is not pressure but a leak. Then you get the errors from the [OutOfMemoryError card](/t/module-05-advanced-java/topic-07-jvm-memory) and need a heap dump.

### Hotspot: a search inside a loop

The most common hotspot in business code is an O(n) search hiding inside a loop. `list.contains(x)`, `list.indexOf(x)` and "find the station with this id" each read the list from the start.

```java run
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    static long compares = 0;

    record BikeId(String id) {
        @Override
        public boolean equals(Object other) {
            compares++;
            return other instanceof BikeId b && b.id.equals(id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    public static void main(String[] args) {
        List<BikeId> docked = new ArrayList<>();
        List<BikeId> wanted = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            docked.add(new BikeId("B-" + (1000 + i)));   // B-1000 to B-2999
            wanted.add(new BikeId("B-" + (2000 + i)));   // B-2000 to B-3999: half are docked
        }

        int found = 0;
        for (BikeId bike : wanted) {
            if (docked.contains(bike)) {
                found++;
            }
        }
        System.out.println("list: found " + found + " with " + compares + " comparisons");

        Set<BikeId> dockedSet = new HashSet<>(docked);
        compares = 0;
        found = 0;
        for (BikeId bike : wanted) {
            if (dockedSet.contains(bike)) {
                found++;
            }
        }
        System.out.println("set:  found " + found + " with " + compares + " comparisons");
    }
}
```

```text
list: found 1000 with 3500500 comparisons
set:  found 1000 with 1000 comparisons
```

Same answer, 3,500 times fewer comparisons. The list loop is O(n × m). The `HashSet` costs O(m) to build and O(1) per lookup, so O(n + m) overall, for O(m) extra memory.

The database version of this mistake is the **N+1 query**: one query for the list, then one more per row. You met it in [database design](/t/module-08-backend/topic-14-database-design). The fix is the same idea: fetch everything you need in one go, then look it up in memory.

### Hotspot: repeated work inside a loop

Work that gives the same answer every time should leave the loop. Four common cases:

**String `+=` in a loop.** A `String` never changes, so `report += line` copies the whole report so far into a new string. For n lines that is O(n²) characters copied. One `StringBuilder` grows in place, O(n) in total.

```java nocompile
String report = "";                                   // before: copies everything, every line
for (Ride ride : rides) report += ride.rentalId() + "\n";

StringBuilder out = new StringBuilder();              // after: one buffer
for (Ride ride : rides) out.append(ride.rentalId()).append('\n');
```

**Boxing in a hot loop.** `Long total = 0L; total += fare;` unboxes, adds and creates a new `Long` object on most passes. Use the primitive `long total = 0;`.

**`Pattern.compile` per call.** `rentalId.matches("rental-\\d+")` compiles the regular expression every time. Compile it once into a `static final Pattern RENTAL_ID = Pattern.compile("rental-\\d+")`, then call `RENTAL_ID.matcher(id).matches()`.

**Too much logging.** One INFO line per GPS ping is 170 lines a second at peak, all formatted, written and shipped. Log per ride, not per ping, and use `{}` placeholders so hidden levels cost almost nothing.

### Hotspot: waiting instead of working

Some slow code is not busy at all. It is waiting.

**Lock contention** happens when many threads need the same lock. Only one gets in. The rest are `BLOCKED`, and you saw in [synchronization](/t/module-05-advanced-java/topic-02-synchronization) how that makes the lock a serial part. Fixes: hold the lock for less time, or split it, such as one `LongAdder` instead of a `synchronized` counter, or a `ConcurrentHashMap`.

**Unbuffered I/O** pays a system call for every tiny write. Writing a CSV export one character at a time with a raw `FileOutputStream` calls the operating system millions of times.

```java nocompile
try (Writer out = new BufferedWriter(new FileWriter(file))) {   // collects writes into 8 KB chunks
    for (Ride ride : rides) {
        out.write(ride.rentalId());
        out.write('\n');
    }
}
```

`BufferedWriter` and `BufferedReader` gather small writes into large chunks, so the operating system is called thousands of times fewer.

### Load testing without fooling yourself

A **load test** sends realistic traffic at a test copy of your system, to see how latency changes as the rate grows. Popular tools are **JMeter**, **Gatling** and **k6**.

- Use production-like data. A report over 10 test rides proves nothing.
- **Warm up** first, so the JIT and connection pools are ready, and throw that part away.
- Ramp the rate up in steps and watch p99, not the average. The step where p99 bends upward is your real capacity.

One trap fools many teams: **coordinated omission**. A simple load tool sends a request, waits for the answer, then sends the next. When the server stalls for 2 seconds, the tool also stops sending. So it records *one* slow request, while real riders kept arriving and *hundreds* would have waited.

```callout jargon
**Coordinated omission** is when a load tester waits for slow answers before sending more, so it quietly skips the requests that would have been slow. The fix is a fixed arrival rate, which Gatling's open model and k6's `constant-arrival-rate` give you.
```

### Performance budgets

A **performance budget** is a limit agreed in advance, such as "the ride-history endpoint: p99 under 300 ms at 50 requests a second".

- It turns "is this fast enough?" from an opinion into a test.
- Split it across the parts: 300 ms might be 50 ms of app code, 150 ms of database and 100 ms of spare.
- Check it automatically. A nightly load test in CI can fail the build when p99 goes over budget, just like a failing unit test.

Without a budget, every release adds 5 ms and nobody notices until it is 2 seconds.

### In an interview

Performance comes up in system design rounds and in "this service got slow, what do you do?" questions. Spot it when the question says slow, latency, p99, capacity, "how many servers?" or "it got slower after we grew".

- **"The endpoint is slow. What do you do?"** Measure percentiles first, then profile, then fix the hotspot, then measure again. Never start with a guess.
- **"Why not use the average?"** Show a tail hidden by the mean, and explain fan-out with 1 − 0.99ⁿ.
- **"How many servers or threads do we need?"** Little's law, L = λ × W, plus headroom to stay around 60-70% busy.
- **"Will 8× the cores make it 8× faster?"** Amdahl's law: the serial part sets the limit.
- **"Find the bottleneck in this code"**: a lookup in a loop, N+1 queries, `String +=`, or a hot lock. State the before and after complexity.

## Worked example

Kakashi's ticket: the ride-history report averages a healthy 180 ms, but some riders wait seconds.

**Step 1: measure the right thing.** From a day of access logs, compute percentiles instead of the average. Say they show p50 120 ms, p95 400 ms and p99 5.2 s. Now the symptom has a number.

**Step 2: check capacity with Little's law.** At 40 requests a second and 0.18 s each, 7.2 requests are in flight. The 8 threads are 90% busy, so every burst queues. That explains part of the tail, but why are some requests slow on their own?

**Step 3: profile.** Record 60 seconds with JFR on a test server under realistic load. `-XX:+DebugNonSafepoints` makes JFR place samples on the exact method. On one laptop, the hot-methods view of the old report looked like this:

```bash
java -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints \
     -XX:StartFlightRecording=filename=report.jfr,settings=profile -jar spoke-api.jar
jfr view hot-methods report.jfr
```

```text
Method                                                          Samples Percent
--------------------------------------------------------------- ------- -------
java.lang.String.equals(Object)                                     115  73.25%
RideReport.findStation(List, String)                                 36  22.93%
RideReport$Station.id()                                               3   1.91%
java.util.ArrayList$Itr.next()                                        3   1.91%
```

Over 96% of the samples sit in the station search. Riders with long histories trigger thousands of searches each, which is why they land in the tail.

**Step 4: fix the hotspot.** Read the station list once into a `HashMap`, then look up each ride in O(1). The program below counts how many station records each version reads, for 5,000 rides over 300 stations.

```java run
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    record Station(String id, String name) {}
    record Ride(String rentalId, String stationId) {}

    public static void main(String[] args) {
        List<Station> stations = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            stations.add(new Station("station-" + i, "Station " + i));
        }
        List<Ride> rides = new ArrayList<>();
        for (int i = 0; i < 5000; i++) {
            rides.add(new Ride("rental-" + i, "station-" + (i * 7 % 300)));
        }

        long reads = 0;
        StringBuilder before = new StringBuilder();
        for (Ride ride : rides) {                 // before: scan the list for every ride
            for (Station s : stations) {
                reads++;
                if (s.id().equals(ride.stationId())) {
                    before.append(ride.rentalId()).append(" from ").append(s.name()).append('\n');
                    break;
                }
            }
        }
        System.out.println("before: " + reads + " station reads");

        reads = 0;
        Map<String, String> names = new HashMap<>();
        for (Station s : stations) {              // after: read the list once, into a map
            reads++;
            names.put(s.id(), s.name());
        }
        StringBuilder after = new StringBuilder();
        for (Ride ride : rides) {
            after.append(ride.rentalId()).append(" from ").append(names.get(ride.stationId())).append('\n');
        }
        System.out.println("after:  " + reads + " station reads");
        System.out.println("same report: " + before.toString().equals(after.toString()));
    }
}
```

```text
before: 750900 station reads
after:  300 station reads
same report: true
```

1. The "before" loop reads, on average, half the station list for every ride: O(rides × stations).
2. The "after" version reads each station once to fill the map, O(stations), then does one O(1) lookup per ride.
3. The last line checks that the output did not change. A speed fix that changes the answer is a bug.

**Step 5: measure again, and set a budget.** Run the same load test and compute the same percentiles. Then write the target down: "p99 under 300 ms at 50 requests a second". Add it to the nightly load test, so it cannot quietly slip back.

## Try it

Ten response times from the ride-history endpoint, with one slow one. Run it, then try the changes below.

```java run
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        long[] millis = {120, 95, 130, 110, 4800, 105, 125, 98, 140, 115};

        long sum = Arrays.stream(millis).sum();
        System.out.println("average: " + sum / millis.length + " ms");

        long[] sorted = Arrays.copyOf(millis, millis.length);
        Arrays.sort(sorted);
        for (int p : new int[] {50, 90, 99}) {
            long rank = Math.ceilDiv((long) p * sorted.length, 100L);
            System.out.println("p" + p + " (rank " + rank + "): " + sorted[(int) rank - 1] + " ms");
        }
    }
}
```

1. Change 4800 to 140. How much do the average and p99 move? Which one tells you more?
2. Add ten more fast samples. Does p99 still equal the maximum?
3. Print `millis` after the loop with `Arrays.toString`. Did its order change? Why not?
4. Use Little's law: at 40 requests a second and the average latency printed, how many requests are in flight?

## Recap

- Measure, profile, fix, measure again. Never optimise a guess.
- Report latency as percentiles (p50, p95, p99, p99.9), not averages. Fan-out turns a rare slow call into a common slow page: 1 − 0.99ⁿ.
- Little's law, L = λ × W, sizes threads and pools. Latency explodes near 100% busy, and Amdahl's law caps what parallelism can give.
- Profile with a sampling profiler such as JFR or async-profiler, and read the flame graph for wide plateaus. Benchmark with JMH, not `nanoTime` around one call.
- Classic hotspots: a search in a loop, N+1 queries, `String +=`, boxing, regex and logging in hot loops, lock contention, unbuffered I/O.

## Common mistakes

**1. Averaging percentiles from several servers.**

```java
import java.util.Arrays;

public class Main {
    static long p99(long[] millis) {
        long[] sorted = Arrays.copyOf(millis, millis.length);
        Arrays.sort(sorted);
        return sorted[(int) Math.ceilDiv(99L * sorted.length, 100L) - 1];
    }

    public static void main(String[] args) {
        long[] busy = new long[900];          // server A: 900 requests, 45 of them slow
        Arrays.fill(busy, 100);
        Arrays.fill(busy, 0, 45, 2000);
        long[] quiet = new long[100];         // server B: 100 requests, all fast
        Arrays.fill(quiet, 100);

        System.out.println("average of the two p99s: " + (p99(busy) + p99(quiet)) / 2 + " ms");

        long[] all = new long[1000];
        System.arraycopy(busy, 0, all, 0, 900);
        System.arraycopy(quiet, 0, all, 900, 100);
        System.out.println("real p99 of all requests: " + p99(all) + " ms");
    }
}
```

```text
average of the two p99s: 1050 ms
real p99 of all requests: 2000 ms
```

A percentile cannot be averaged: the quiet server counts as much as the busy one. Fix: compute the percentile from all the samples together, or merge bucket counts (histograms) and compute from those.

**2. Sorting the caller's array.**

```java
import java.util.Arrays;

public class Main {
    static long median(long[] millis) {
        Arrays.sort(millis);                  // sorts the caller's array!
        return millis[(int) Math.ceilDiv(50L * millis.length, 100L) - 1];
    }

    public static void main(String[] args) {
        long[] lastFour = {300, 100, 200, 50};   // in the order the requests arrived
        System.out.println("median: " + median(lastFour));
        System.out.println("newest request: " + lastFour[lastFour.length - 1] + " ms");
    }
}
```

```text
median: 100
newest request: 300 ms
```

The newest request took 50 ms, but the method reordered the caller's data. Fix: sort a copy, `long[] sorted = Arrays.copyOf(millis, millis.length);`.

**3. Rounding Little's law down.**

```java
public class Main {
    public static void main(String[] args) {
        int requestsPerSecond = 30;
        int latencyMillis = 120;
        int threads = requestsPerSecond * latencyMillis / 1000;
        System.out.println("threads: " + threads);
    }
}
```

```text
threads: 3
```

The real answer is 3.6, and integer division threw away the 0.6. With 3 threads, requests queue all day. Fix: round up with `Math.ceilDiv(requestsPerSecond * latencyMillis, 1000)`, which gives 4, then add headroom.

**4. Timing one cold call with nanoTime.**

```java nocompile
long start = System.nanoTime();
String report = RideReport.build(rides, stations);
System.out.println((System.nanoTime() - start) / 1_000_000 + " ms");
```

Symptom: the first run is much slower than the second, the numbers jump between runs, and an unused result may be optimised away entirely. Fix: use JMH, with warm-up iterations, several forks and the result returned or sent to a `Blackhole`.

**5. Optimising without a profile.**

```java nocompile
long fare = 100 + (minutes << 4) - minutes;   // "faster" than 100 + 15 * minutes
```

Symptom: the code is harder to read, and p99 does not move at all, because the time was spent in a station lookup. Fix: profile first with JFR or async-profiler, and change only the wide plateau in the flame graph.

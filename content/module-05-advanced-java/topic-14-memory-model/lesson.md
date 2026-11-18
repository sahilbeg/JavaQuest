## Hook

Picture a team whiteboard that says "Harbor sensor: ON". Every worker copies the board onto a sticky note at their desk. From then on, they only look at their sticky note, because walking to the board takes time.

Now the boss rubs out "ON" and writes "OFF". Nobody walks back to look. Each worker keeps reading their own note, which still says "ON".

That is Kakashi's ticket. One thread set the sensor's stop flag. The sensor thread kept reading its own old copy and never stopped.

## Concept

### Each core keeps its own copy

Your laptop's processor has several **cores**. Each core can run one thread at a time. Reading from main memory is slow, so each core works on fast private copies of the values it uses.

```callout jargon
A **register** is a tiny, very fast slot inside a core, where it keeps the few values it is working on right now. A **write buffer** is a short queue where a core parks its new values before they go out to memory.
```

A thread may keep a field's value in a register and never read the field again. A new value may wait in its core's write buffer for a moment. Either way, another thread can read an older value. It sees a **stale** value: one that is out of date.

Processors also have **caches**, small fast memories next to each core. The hardware keeps caches in agreement, so they are not the problem here. You do not need to know which trick caused a stale read. The memory model below hides these details behind a few simple rules.

### The compiler may move your code

The second cause is speed tricks. While your program runs, the JVM watches which code runs often. Then it rewrites that code into faster machine code.

```callout jargon
The **JIT compiler** ("just in time") is the part of the JVM that turns busy Java code into fast machine code while the program runs. **Reordering** means running your lines in a different order, or skipping a repeat read, when one thread alone could never tell the difference.
```

Inside one thread, these tricks never change the result. Between threads, they can. A loop that keeps reading the same field may be rewritten to read it only once.

### The loop that never ends

Here is the sensor bug in miniature. Do not run it: it can spin forever.

```java nocompile
public class Main {
    static boolean stop = false;

    public static void main(String[] args) throws InterruptedException {
        Thread sensor = new Thread(() -> {
            long readings = 0;
            while (!stop) {
                readings++;
            }
            System.out.println("Sensor stopped after " + readings + " readings");
        });
        sensor.start();
        Thread.sleep(1000);
        stop = true;
        System.out.println("main set stop = true");
        sensor.join();
        System.out.println("Done");
    }
}
```

On a real run on a laptop, this printed `main set stop = true` and then hung. It was still spinning 6 seconds later.

Why? The loop ran millions of times, so the JIT made it fast. It saw that nothing in the loop changes `stop`. So it read `stop` once and turned the loop into `while (true)`. Nothing in the code told Java that another thread might change `stop`.

```animate
title: Two cores, one stop flag
kind: scene
nodes:
  - { id: core1, label: "Core 1: main", emoji: "⚙️", value: "stop = false", row: 0, col: 0 }
  - { id: memory, label: Main memory, emoji: "🗄️", value: "stop = false", row: 1, col: 1 }
  - { id: core2, label: "Core 2: sensor", emoji: "⚙️", value: "stop = false", row: 0, col: 2 }
steps:
  - say: The sensor thread runs on core 2. It reads stop and keeps it in a register.
    send: { from: memory, to: core2, label: "read stop" }
  - say: The loop runs millions of times. The JIT decides to stop re-reading the flag and trusts its copy.
    focus: [core2]
    set: { core2: "stop = false (kept)" }
  - say: Main, on core 1, sets stop to true. The new value waits in core 1's write buffer first.
    set: { core1: "stop = true" }
  - say: Later the value reaches main memory. Core 2 is not looking any more.
    send: { from: core1, to: memory, label: "write stop" }
    set: { memory: "stop = true" }
  - say: The sensor still sees false and spins forever. This is a stale read.
    focus: [core2]
  - say: With volatile, every read of stop must fetch the latest value. Core 2 sees true and the loop ends.
    send: { from: memory, to: core2, label: "volatile read" }
    set: { core2: "stop = true" }
```

### Syntax: volatile

```java nocompile
private volatile boolean running = true;
```

`volatile` is a keyword you put on a field. It makes two promises:

- **Visibility.** A write to a volatile field is seen by every later read of it, from any thread. No thread keeps a private stale copy.
- **Ordering.** Writes made *before* a volatile write cannot be moved after it. So a thread that reads the new volatile value also sees those earlier writes.

Add `volatile` to `stop` in the program above, and it prints `Sensor stopped after ... readings` and `Done`.

### volatile is not atomic

`volatile` fixes *seeing* a value. It does not fix *changing* it from many threads. `count++` is still three steps: read, add 1, write back. Two threads can still read the same old number.

```java
public class Main {
    static volatile int hits = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread[] pings = new Thread[4];
        for (int i = 0; i < pings.length; i++) {
            pings[i] = new Thread(() -> {
                for (int n = 0; n < 10_000; n++) {
                    hits++;
                }
            });
            pings[i].start();
        }
        for (Thread t : pings) {
            t.join();
        }
        System.out.println("hits = " + hits);
    }
}
```

You expect 40000. Three real runs printed `hits = 16991`, `hits = 21861` and `hits = 20827`.

```callout warning
For a counter, use an atomic, from the concurrent collections topic. Use `volatile` for a value that one thread sets and others only read, like an on/off flag.
```

### Syntax: LongAdder and accumulateAndGet

```java nocompile
LongAdder totalMinutes = new LongAdder();
totalMinutes.add(25);                              // safe from any thread
long total = totalMinutes.sum();                   // read the total

AtomicInteger longestRide = new AtomicInteger();
longestRide.accumulateAndGet(25, Math::max);       // keep the bigger of old and 25
```

A **max** is another trap. `if (ride > longest) longest = ride;` is a check and then a write. Two threads can both pass the check, and the smaller number can be written last. `accumulateAndGet(value, Math::max)` does the check and the write as one atomic step. `LongAdder` is a counter built for many threads adding at once.

```java run
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        LongAdder totalMinutes = new LongAdder();
        AtomicInteger longestRide = new AtomicInteger();
        Thread[] kiosks = new Thread[4];
        for (int k = 0; k < kiosks.length; k++) {
            int kiosk = k;
            kiosks[k] = new Thread(() -> {
                for (int minutes = 1; minutes <= 30; minutes++) {
                    int ride = minutes + kiosk * 10;
                    totalMinutes.add(ride);
                    longestRide.accumulateAndGet(ride, Math::max);
                }
            });
            kiosks[k].start();
        }
        for (Thread t : kiosks) {
            t.join();
        }
        System.out.println("Total minutes: " + totalMinutes.sum());
        System.out.println("Longest ride: " + longestRide.get() + " minutes");
    }
}
```

It prints `Total minutes: 3660` and `Longest ride: 60 minutes` on every run.

### volatile on arrays, references, long and double

`volatile` covers the field itself, and nothing behind it.

- `volatile int[] docks` makes only the *reference* volatile. Writing `docks[3] = 7` is a plain write. Use an `AtomicIntegerArray`, or replace the whole array with a new one.
- `volatile StationSnapshot latest` makes the swap of the whole object visible. Changes made *inside* the object later are not covered. That is one more reason to share immutable objects.
- A plain `long` or `double` is 64 bits. Java allows a plain write of one to happen as two 32-bit halves, so another thread could see half old and half new. A `volatile long` or `volatile double` is always written whole.

### Happens-before

Java's memory model is a set of rules. They say when one thread is guaranteed to see what another thread wrote.

```callout jargon
**Happens-before** is a promise between two actions. If action A happens-before action B, then B sees everything A did, and everything before A. Without a happens-before link, B may see old values.
```

The rules, in plain words:

1. **One thread:** each line happens-before the next line in the same thread.
2. **Locks:** leaving a `synchronized` block (or `unlock()`) happens-before the next thread taking the same lock.
3. **volatile:** a write to a volatile field happens-before every later read that sees it.
4. **Starting:** everything before `t.start()` happens-before the new thread's first line.
5. **Joining:** everything a thread did happens-before `t.join()` returns in another thread.
6. **Executors:** `submit(task)` happens-before the task runs. The task's work happens-before `future.get()` returns.
7. **Concurrent collections:** putting an item into a `ConcurrentHashMap` or `BlockingQueue` happens-before another thread gets it out.

The rules chain together. If A happens-before B, and B happens-before C, then A happens-before C.

### Syntax: a volatile flag that hands over data

```java nocompile
// writer thread
fare = 400;          // plain write
ready = true;        // volatile write

// reader thread
if (ready) {         // volatile read sees true
    use(fare);       // guaranteed to see 400
}
```

Only `ready` is volatile. The plain write to `fare` comes before the volatile write, so rule 1 and rule 3 chain together. A reader that sees `ready == true` also sees `fare == 400`.

### Safe publication

```callout jargon
To **publish** an object means to make it reachable by other threads, for example by storing it in a shared field. **Safe publication** means other threads see the object fully built, never half-built.
```

Here is the danger. Building an object is several writes: set field 1, set field 2, then store the reference. Without a happens-before link, another thread may see the new reference *before* it sees the fields. It sees a Harbor snapshot with `null` for its station name.

Safe ways to publish:

- Store the reference in a `volatile` field.
- Store and read it while holding the same lock.
- Put it into a concurrent collection or a `BlockingQueue`.
- Build it in a `static` field's initialiser (more on this below).
- Build it before calling `t.start()`, or hand it back through `t.join()` or `future.get()`.
- Make the object immutable, with all fields `final`.

### Immutable objects are thread-safe

```callout jargon
An **immutable** object can never change after its constructor finishes. To change something, you build a new object.
```

Java gives `final` fields a special promise. Once the constructor finishes, every thread sees the values the constructor put in them. This holds even if the object was published without any lock.

The recipe for an immutable class:

- Make the class `final`, so no subclass can add fields that change.
- Make every field `private final`.
- Write no setters.
- Copy any list you are given, for example with `List.copyOf(list)`. Return that copy, which nobody can change.
- To "change" a value, return a new object, as in `withFreeDocks(9)`.
- Do not hand out `this` from inside the constructor, for example by registering a listener there.

An immutable object needs no locks at all. Any number of threads can read it at once. That is why `String`, `Integer` and records with unchangeable parts are so easy to share.

### Syntax: holder-class idiom

Sometimes you want one shared object, built only when first needed. For example, one fare table for the whole app.

```java nocompile
final class FareTable {
    private FareTable() { }

    private static final class Holder {
        static final FareTable INSTANCE = new FareTable();
    }

    static FareTable get() {
        return Holder.INSTANCE;
    }
}
```

```callout jargon
A **singleton** is a class that has exactly one object in the whole program. **Lazy** means it is built the first time someone asks, not when the program starts.
```

Java sets up a class the first time code uses it, and it does this exactly once. While it does, other threads that need the class wait. So `Holder.INSTANCE` is built once, on the first `get()`, and safely published to every thread. No `volatile`, no `synchronized` in your code.

```animate
title: The holder builds one table
kind: code
code: |
  public class Main {
      public static void main(String[] args) {
          System.out.println("App started");
          PromoTable first = PromoTable.get();
          PromoTable second = PromoTable.get();
          System.out.println(first == second);
      }
  }

  final class PromoTable {
      private PromoTable() {
          System.out.println("Building the promo table");
      }

      private static final class Holder {
          static final PromoTable INSTANCE = new PromoTable();
      }

      static PromoTable get() {
          return Holder.INSTANCE;
      }
  }
steps:
  - line: 3
    out: App started
    say: The program starts. No PromoTable exists yet, because nobody has asked for one.
  - line: 4
    say: main calls PromoTable.get() for the first time.
  - line: 20
    say: get() touches the Holder class for the first time. Java must set up Holder now, and only once.
  - line: 16
    say: Java runs Holder's static line while holding a lock. Any other thread that asks now has to wait.
  - line: 12
    objects: { p1: { type: PromoTable } }
    out: Building the promo table
    say: The constructor runs and prints. This is the only time it ever runs.
  - line: 4
    vars: { first: "@p1" }
    say: get() returns the one table, and first points to it.
  - line: 5
    vars: { second: "@p1" }
    say: The second call finds Holder already set up. No building, no waiting, just the same reference.
  - line: 6
    out: "true"
    say: Both variables point to one object, so == prints true.
```

### Double-checked locking, broken and fixed

You will meet an older pattern in real code and interviews. It checks for `null` twice, to skip the lock once the object exists.

```java
class FareTable {
    private static FareTable instance;

    static FareTable get() {
        if (instance == null) {
            synchronized (FareTable.class) {
                if (instance == null) {
                    instance = new FareTable();
                }
            }
        }
        return instance;
    }
}
```

This is broken. The first `if` reads `instance` without the lock. Java may store the reference *before* the constructor's writes are visible. A second thread then sees a non-null table whose fields are still empty.

The fix is one word: make the field `volatile`.

```java
class FareTable {
    private static volatile FareTable instance;

    static FareTable get() {
        FareTable local = instance;
        if (local == null) {
            synchronized (FareTable.class) {
                local = instance;
                if (local == null) {
                    local = new FareTable();
                    instance = local;
                }
            }
        }
        return local;
    }
}
```

Now the volatile write of `instance` happens-before any read that sees it, so readers see a fully built table. The `local` variable reads the volatile field once in the common case, which is a little faster. In new code, prefer the holder idiom. It is shorter and hard to get wrong.

### What it costs

- A `volatile` read or write costs a little more than a plain one, because the value cannot stay in a register. It is still far cheaper than taking a lock.
- `AtomicInteger.incrementAndGet()` is O(1): on common processors it is one atomic add. Under heavy traffic, threads still slow each other down, because the shared value keeps moving between cores. A `LongAdder` spreads the work over several cells and adds them up in `sum()`.
- A defensive copy with `List.copyOf` costs O(n) time and O(n) memory for n items, once per object. After that, every read is O(1) and needs no lock.
- The holder idiom costs nothing after the first call: `get()` is one plain field read.

### In an interview

Listen for **"this loop never stops"**, **"a flag set by another thread"**, **"is volatile enough?"**, **"thread-safe singleton"** or **"make this class immutable"**. Those are memory model questions.

Classic questions:

- **Stop a worker thread cleanly:** a `volatile boolean` flag, or `interrupt()`.
- **volatile vs synchronized vs atomic:** volatile gives visibility, a lock gives visibility plus one-at-a-time, an atomic gives a safe single-step update.
- **Why does `count++` on a volatile lose updates?** Because it is still read, add, write.
- **Write a thread-safe lazy singleton:** the holder idiom, double-checked locking with `volatile`, or a one-value `enum`.
- **Make a class immutable and explain why it is thread-safe:** final class, final fields, defensive copies, final-field promise.

Say the rule out loud: "Without a happens-before link, the other thread may see stale values."

## Worked example

A route planner thread builds a route card and publishes it. A screen thread waits for it and shows it.

```java run
import java.util.ArrayList;
import java.util.List;

public class Main {
    static volatile RouteCard published;

    public static void main(String[] args) throws InterruptedException {
        Thread screen = new Thread(() -> {
            RouteCard card;
            while ((card = published) == null) {
                Thread.onSpinWait();
            }
            System.out.println("Screen shows " + card.rider() + ": " + card.stops());
        });
        screen.start();

        List<String> stops = new ArrayList<>(List.of("Riverside", "Old Mill"));
        published = new RouteCard("rider-7", stops);
        stops.add("Depot");

        screen.join();
        System.out.println("Planner's own list: " + stops);
    }
}

final class RouteCard {
    private final String rider;
    private final List<String> stops;

    RouteCard(String rider, List<String> stops) {
        this.rider = rider;
        this.stops = List.copyOf(stops);
    }

    String rider() {
        return rider;
    }

    List<String> stops() {
        return stops;
    }
}
```

Line by line:

1. `published` is `volatile`. The screen thread re-reads it on every turn of its loop, so it will notice the card.
2. `Thread.onSpinWait()` tells the processor "I am waiting in a loop". It saves a little power. It is not what makes the loop see the change; `volatile` is.
3. The planner builds the card, then stores it in `published`. That volatile write publishes the card safely.
4. `RouteCard` is immutable: a final class, private final fields, and a `List.copyOf` copy of the stops.
5. The planner adds Depot to *its own* list after publishing. The card kept a copy, so the screen still shows two stops.
6. `join` waits for the screen thread, so the output order never changes.

It prints:

```
Screen shows rider-7: [Riverside, Old Mill]
Planner's own list: [Riverside, Old Mill, Depot]
```

## Try it

This counter is safe. Four threads add 10,000 views each, and it prints `views = 40000` every time. Now change the field to `static volatile int views = 0;` and the loop body to `views++;`. Fix the print line to match, then run it a few times and watch the total drop.

```java run
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    static final AtomicInteger views = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException {
        Thread[] screens = new Thread[4];
        for (int i = 0; i < screens.length; i++) {
            screens[i] = new Thread(() -> {
                for (int n = 0; n < 10_000; n++) {
                    views.incrementAndGet();
                }
            });
            screens[i].start();
        }
        for (Thread t : screens) {
            t.join();
        }
        System.out.println("views = " + views.get());
    }
}
```

## Recap

- Threads can see stale values, because cores keep copies and the JIT reorders and skips reads.
- `volatile` makes a field's writes visible to every thread and keeps earlier writes in order. It does not make `count++` safe: use an atomic.
- Happens-before is the rule for "will the other thread see it?": locks, volatile, `start`, `join`, `Future.get` and concurrent collections all create it.
- Publish shared objects safely, and prefer immutable objects: final class, `private final` fields, defensive copies.
- For a lazy singleton, use the holder-class idiom, or double-checked locking with a `volatile` field.

## Common mistakes

**1. Using volatile for a counter.** The total comes out low.

```java
public class Main {
    static volatile int rides = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread a = new Thread(() -> { for (int i = 0; i < 10_000; i++) rides++; });
        Thread b = new Thread(() -> { for (int i = 0; i < 10_000; i++) rides++; });
        a.start();
        b.start();
        a.join();
        b.join();
        System.out.println("rides = " + rides);
    }
}
```

There is no error. The total is below 20000 on most runs, and it changes from run to run. `volatile` only makes the value visible. Use `AtomicInteger` with `incrementAndGet()`.

**2. A stop flag without volatile.** The thread never stops.

```java nocompile
private boolean running = true;

public void run() {
    while (running) {
        readings++;
    }
}
```

There is no error and no exception. The program prints the line after setting the flag, then hangs while the loop spins at full speed. It may even work on some runs, before the JIT kicks in. Fix: `private volatile boolean running = true;`.

**3. A final field that holds someone else's list.** The "immutable" object changes.

```java
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> bikes = new ArrayList<>(List.of("B-1042"));
        DockList dock = new DockList(bikes);
        bikes.add("B-2210");
        System.out.println(dock.bikes());
    }
}

final class DockList {
    private final List<String> bikes;

    DockList(List<String> bikes) {
        this.bikes = bikes;
    }

    List<String> bikes() {
        return bikes;
    }
}
```

This prints `[B-1042, B-2210]`. `final` only stops the field from pointing at a different list. The list itself can still change. Fix: `this.bikes = List.copyOf(bikes);`, and the program prints `[B-1042]`.

**4. Double-checked locking without volatile.** It looks right and passes every test.

```java nocompile
private static FareTable instance;   // missing volatile
```

You will not see an error message. On rare runs, under load, a thread can get a table whose fields are still empty. Bugs like this are almost impossible to reproduce, so follow the rule: the field must be `volatile`. Better still, use the holder idiom.

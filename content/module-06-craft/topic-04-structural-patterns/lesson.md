## Hook

You fly to another country and your phone charger will not fit the wall socket. You do not rewire the hotel, and you do not cut the plug off your charger. You buy a small travel adapter. It fits the wall on one side and your plug on the other.

Itachi's ticket has the same problem. Spoke bought a second-hand fleet for Depot. Their locks speak a different language: `engage` and `disengage`, with number codes. Nobody is allowed to change the lock classes. Support also wants every unlock logged and retried once. In this topic you learn three patterns that solve this by wrapping objects: adapter, decorator and facade.

## Concept

### Composition: the common thread

```callout jargon
**Composition** means one object keeps another object in a field and uses it. **Delegation** means passing a job on to that inner object by calling its method. A **structural pattern** is a pattern about how objects fit together.
```

In module 2 you met inheritance with `extends`. Inheritance glues a child to one parent forever, at compile time. Composition is looser. You can pick the inner object when the program runs, and you can swap it.

```java
interface Charger {
    boolean start(String bikeId);
}

class LoudCharger implements Charger {
    private final Charger inner;

    LoudCharger(Charger inner) {
        this.inner = inner;
    }

    public boolean start(String bikeId) {
        System.out.println("starting " + bikeId);
        return inner.start(bikeId);
    }
}
```

`LoudCharger` holds a `Charger` in a field (composition). Its `start` calls `inner.start` (delegation). All three patterns below are built from these two moves.

### Syntax: an adapter

An **adapter** wraps an object with the wrong methods and makes it look like your interface.

Imagine Spoke's chargers use the `Charger` interface above. The old Depot chargers come from another company:

```java
class OldCharger {
    int powerOn(String id) {
        return 0;   // 0 means ok, any other number is an error code
    }
}
```

You cannot change `OldCharger`. So you write an adapter:

```java
interface Charger {
    boolean start(String bikeId);
}

class OldCharger {
    int powerOn(String id) { return 0; }
}

class OldChargerAdapter implements Charger {
    private final OldCharger old;

    OldChargerAdapter(OldCharger old) {
        this.old = old;
    }

    @Override
    public boolean start(String bikeId) {
        return old.powerOn(bikeId) == 0;
    }
}
```

- The adapter **implements your interface**, `Charger`.
- It **holds the foreign object**, `OldCharger`, in a field.
- Each method **translates**: it calls the old method and turns the code `0` into `true`.

Now the rest of the app sees one more `Charger` and never learns about number codes.

```java nocompile
Charger c = new OldChargerAdapter(new OldCharger());
System.out.println(c.start("B-9001"));   // true
```

```callout analogy
The travel plug adapter does not change the wall or your charger. It sits between them and translates the shape. A code adapter does the same between two method shapes.
```

### Syntax: a decorator

A **decorator** wraps an object of the same interface and adds a little extra behaviour around each call. It implements the interface and also holds an object of that interface.

```java nocompile
class CountingCharger implements Charger {
    private final Charger inner;
    private int calls = 0;

    CountingCharger(Charger inner) {
        this.inner = inner;
    }

    @Override
    public boolean start(String bikeId) {
        calls++;
        return inner.start(bikeId);
    }

    int calls() {
        return calls;
    }
}
```

- The outside is a `Charger`, so callers cannot tell a decorator from a plain charger.
- The inside is any `Charger`, so it can wrap a real charger, an adapter, or another decorator.
- The extra work goes before or after the delegated call.

A retry is a decorator too:

```java nocompile
class RetryingCharger implements Charger {
    private final Charger inner;

    RetryingCharger(Charger inner) {
        this.inner = inner;
    }

    @Override
    public boolean start(String bikeId) {
        if (inner.start(bikeId)) {
            return true;
        }
        return inner.start(bikeId);
    }
}
```

### Stacking decorators

Because each decorator is a `Charger` and holds a `Charger`, you can nest them like Russian dolls.

```mermaid
flowchart LR
    App --> Counting[CountingCharger] --> Retrying[RetryingCharger] --> Adapter[OldChargerAdapter] --> Old[OldCharger]
```

The order changes what you see. Put the counter outside the retry, and it counts one call per request. Put it inside, and it counts every attempt, retries included.

```java run
public class Main {
    public static void main(String[] args) {
        CountingCharger outside = new CountingCharger(new RetryingCharger(new FlakyCharger()));
        outside.start("B-9001");
        System.out.println("counter outside retry: " + outside.calls());

        CountingCharger inside = new CountingCharger(new FlakyCharger());
        new RetryingCharger(inside).start("B-9001");
        System.out.println("counter inside retry: " + inside.calls());
    }
}

interface Charger {
    boolean start(String bikeId);
}

class FlakyCharger implements Charger {
    private boolean failedOnce = false;

    public boolean start(String bikeId) {
        if (!failedOnce) {
            failedOnce = true;
            return false;
        }
        return true;
    }
}

class RetryingCharger implements Charger {
    private final Charger inner;

    RetryingCharger(Charger inner) {
        this.inner = inner;
    }

    public boolean start(String bikeId) {
        if (inner.start(bikeId)) {
            return true;
        }
        return inner.start(bikeId);
    }
}

class CountingCharger implements Charger {
    private final Charger inner;
    private int calls = 0;

    CountingCharger(Charger inner) {
        this.inner = inner;
    }

    public boolean start(String bikeId) {
        calls++;
        return inner.start(bikeId);
    }

    int calls() {
        return calls;
    }
}
```

`FlakyCharger` fails the first time and works after that. Both stacks end with a charged bike, but the counters disagree: 1 and 2.

```callout tip
Why not a subclass like `LoggingRetryingOldCharger extends OldCharger`? With three extras you would need a subclass for every mix: seven of them. With decorators you write three small classes and combine them in one line.
```

### Syntax: a facade

A **facade** is one simple class in front of several parts. The caller makes one call, and the facade talks to each part in the right order.

```java nocompile
class ChargeFacade {
    private final Set<String> members;
    private final Charger charger;

    ChargeFacade(Set<String> members, Charger charger) {
        this.members = members;
        this.charger = charger;
    }

    String charge(String riderId, String bikeId) {
        if (!members.contains(riderId)) {
            return "rider " + riderId + " not allowed";
        }
        if (!charger.start(bikeId)) {
            return "charger failed for " + bikeId;
        }
        return "charging " + bikeId;
    }
}
```

- The kiosk code calls `charge(...)` once. It does not need to know about members or chargers.
- The facade adds no new power. It only hides the steps behind one door.
- A **facade** is the front of a building: one entrance in front of many rooms.

### Telling the three apart

| Pattern | Outside looks like | Inside holds | Purpose |
|---|---|---|---|
| Adapter | your interface | a foreign object | translate methods |
| Decorator | your interface | the same interface | add behaviour |
| Facade | a new simple class | several parts | hide steps |

### Proxy: a stand-in that controls access

```callout jargon
A **proxy** is a stand-in object. It implements the same interface as the real object and decides when, or whether, the real one gets the call.
```

A proxy is built like a decorator: same interface outside, real object inside. The difference is the purpose. A decorator adds behaviour. A proxy controls access: it loads the real object late, caches answers, checks permissions, or wraps a call in a transaction (a group of database changes that all succeed or all fail together).

Here the fare table is slow to load, so a proxy waits until someone first asks.

```java run
public class Main {
    public static void main(String[] args) {
        FareTable fares = new LazyFareTable();
        System.out.println("kiosk started");
        System.out.println("EBIKE: " + fares.perMinuteCents("EBIKE"));
        System.out.println("CARGO: " + fares.perMinuteCents("CARGO"));
    }
}

interface FareTable {
    long perMinuteCents(String bikeType);
}

class RealFareTable implements FareTable {
    RealFareTable() {
        System.out.println("loading fares from the database...");
    }

    public long perMinuteCents(String bikeType) {
        return switch (bikeType) {
            case "EBIKE" -> 30;
            case "CARGO" -> 25;
            default -> 15;
        };
    }
}

class LazyFareTable implements FareTable {
    private FareTable real;

    public long perMinuteCents(String bikeType) {
        if (real == null) {
            real = new RealFareTable();
        }
        return real.perMinuteCents(bikeType);
    }
}
```

The kiosk starts before any loading happens, and the table loads only once. You will meet proxies again in module 8: Spring wraps a `@Transactional` bean in a proxy that starts and ends the [transaction](/t/module-08-backend/topic-10-transactions) around each call.

## Worked example

Here is the full Depot story for chargers: adapter, then decorator, then facade.

```java run
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Charger adapted = new OldChargerAdapter(new OldCharger());
        LoggingCharger logged = new LoggingCharger(adapted);
        ChargeFacade kiosk = new ChargeFacade(Set.of("r1"), logged);

        System.out.println(kiosk.charge("r1", "B-9001"));
        System.out.println(kiosk.charge("r1", "B-9013"));
        System.out.println(kiosk.charge("r7", "B-9001"));
        System.out.println(logged.log());
    }
}

interface Charger {
    boolean start(String bikeId);
}

class OldCharger {
    int powerOn(String id) {
        if (id.endsWith("13")) {
            return 5;
        }
        return 0;
    }
}

class OldChargerAdapter implements Charger {
    private final OldCharger old;

    OldChargerAdapter(OldCharger old) {
        this.old = old;
    }

    public boolean start(String bikeId) {
        return old.powerOn(bikeId) == 0;
    }
}

class LoggingCharger implements Charger {
    private final Charger inner;
    private final List<String> log = new ArrayList<>();

    LoggingCharger(Charger inner) {
        this.inner = inner;
    }

    public boolean start(String bikeId) {
        boolean ok = inner.start(bikeId);
        log.add("start " + bikeId + ": " + ok);
        return ok;
    }

    List<String> log() {
        return log;
    }
}

class ChargeFacade {
    private final Set<String> members;
    private final Charger charger;

    ChargeFacade(Set<String> members, Charger charger) {
        this.members = members;
        this.charger = charger;
    }

    String charge(String riderId, String bikeId) {
        if (!members.contains(riderId)) {
            return "rider " + riderId + " not allowed";
        }
        if (!charger.start(bikeId)) {
            return "charger failed for " + bikeId;
        }
        return "charging " + bikeId;
    }
}
```

Step by step:

1. `new OldChargerAdapter(new OldCharger())` makes the old charger look like a `Charger`.
2. `new LoggingCharger(adapted)` wraps the adapter. Every call now leaves a line in the log.
3. `new ChargeFacade(..., logged)` gives the kiosk one method to call.
4. B-9001 returns code 0, so the adapter says `true`. B-9013 returns code 5, so it says `false`.
5. Rider r7 is not a member. The facade stops early, so the charger is never called and the log has only two lines.

The output is:

```
charging B-9001
charger failed for B-9013
rider r7 not allowed
[start B-9001: true, start B-9013: false]
```

## Try it

Run the program. Then wrap the flaky charger in a `RetryingCharger` (copy it from the Concept section) before you pass it to `LoggingCharger`. Run again. The log still shows one line, but now it says `true`. Then swap the order and watch the log grow to two lines.

```java run
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        LoggingCharger logged = new LoggingCharger(new FlakyCharger());
        logged.start("B-1042");
        System.out.println(logged.log());
    }
}

interface Charger {
    boolean start(String bikeId);
}

class FlakyCharger implements Charger {
    private boolean failedOnce = false;

    public boolean start(String bikeId) {
        if (!failedOnce) {
            failedOnce = true;
            return false;
        }
        return true;
    }
}

class LoggingCharger implements Charger {
    private final Charger inner;
    private final List<String> log = new ArrayList<>();

    LoggingCharger(Charger inner) {
        this.inner = inner;
    }

    public boolean start(String bikeId) {
        boolean ok = inner.start(bikeId);
        log.add("start " + bikeId + ": " + ok);
        return ok;
    }

    List<String> log() {
        return log;
    }
}
```

## Recap

- Composition keeps another object in a field. Delegation passes the job to it. All three patterns use both.
- An adapter implements your interface and translates calls to a foreign object you cannot change.
- A decorator implements an interface and wraps another object of the same interface to add behaviour.
- Decorators stack, and the order of the stack changes what each one sees.
- A facade gives callers one simple method in front of several parts.

## Common mistakes

**1. Trying to use the foreign class as your interface.**

```java nocompile
public class Main {
    public static void main(String[] args) {
        Charger c = new OldCharger();
    }
}

interface Charger { boolean start(String bikeId); }
class OldCharger { int powerOn(String id) { return 0; } }
```

```
Main.java:3: error: incompatible types: OldCharger cannot be converted to Charger
        Charger c = new OldCharger();
                    ^
```

Fix: wrap it, `Charger c = new OldChargerAdapter(new OldCharger());`. The adapter is the piece that says `implements Charger`.

**2. Forgetting one method of the interface in an adapter.**

```java nocompile
interface Lock {
    boolean open(String bikeId);
    boolean close(String bikeId);
}

class OldLock { int disengage(String id) { return 0; } }

class OldLockAdapter implements Lock {
    private final OldLock old = new OldLock();
    public boolean open(String bikeId) { return old.disengage(bikeId) == 0; }
}
```

```
Main.java:8: error: OldLockAdapter is not abstract and does not override abstract method close(String) in Lock
class OldLockAdapter implements Lock {
^
```

Fix: an adapter must translate every method of the interface. Add `close` too.

**3. Calling yourself instead of the inner object.**

```java
public class Main {
    public static void main(String[] args) {
        Charger c = new LoudCharger(new PlainCharger());
        System.out.println(c.start("B-1042"));
    }
}

interface Charger { boolean start(String bikeId); }
class PlainCharger implements Charger { public boolean start(String bikeId) { return true; } }

class LoudCharger implements Charger {
    private final Charger inner;
    LoudCharger(Charger inner) { this.inner = inner; }
    public boolean start(String bikeId) {
        return start(bikeId);
    }
}
```

```
Exception in thread "main" java.lang.StackOverflowError
	at LoudCharger.start(Main.java:15)
	at LoudCharger.start(Main.java:15)
	at LoudCharger.start(Main.java:15)
```

The decorator calls its own `start` again and again until the stack runs out. Fix: write `return inner.start(bikeId);`. A decorator always passes the call to `inner`.

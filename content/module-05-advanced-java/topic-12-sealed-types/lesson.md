## Hook

Think about a vending machine with three buttons: water, juice and soda. The person who stocks it knows every possible order. Nobody can glue a fourth button on the side.

A Harbor dock sensor is the same. It sends three kinds of messages: a bike docked, a bike left, or a fault. Tsunade's ticket says someone glued on a fourth message from another package. The handler had no branch for it, so it did nothing, and nobody noticed. In this topic you learn how to close a family of types so Java itself refuses a surprise fourth member.

## Concept

### The problem with an open interface

In module 2 you met interfaces. Any class, anywhere, can write `implements DockEvent`. That openness is great for plug-ins. It is bad when the list of kinds is fixed by the real world.

```java
interface DockEvent { }
record BikeDocked(String station, String bikeId) implements DockEvent { }
record BikeUndocked(String station, String bikeId) implements DockEvent { }
record Fault(String station, String code) implements DockEvent { }
record Rebooted(String station) implements DockEvent { }   // surprise!
```

The last line compiles happily. Your handler only checks for three types, so a `Rebooted` message slips through every `if`.

### Syntax: a sealed interface

```java
sealed interface DockEvent permits BikeDocked, BikeUndocked, Fault { }
record BikeDocked(String station, String bikeId) implements DockEvent { }
record BikeUndocked(String station, String bikeId) implements DockEvent { }
record Fault(String station, String code) implements DockEvent { }
```

```callout jargon
A **sealed** type is a class or interface that lists every type allowed to extend or implement it. The list comes after the word **permits**. The listed types are called the **permitted subclasses**.
```

- `sealed` closes the interface.
- `permits` names the only three types that may implement it.
- Any other class that tries `implements DockEvent` gets a compile error.

### Syntax: a sealed class

Classes can be sealed too. The rule is the same: `sealed`, then `permits`, then the list.

```java
sealed class Bike permits ClassicBike, EBike { }
final class ClassicBike extends Bike { }
final class EBike extends Bike { }
```

An abstract sealed class works as well. Use it when the parent has shared fields or code.

```java
abstract sealed class Bike permits ClassicBike, EBike {
    abstract double perMinute();
}
final class ClassicBike extends Bike {
    double perMinute() { return 0.15; }
}
final class EBike extends Bike {
    double perMinute() { return 0.30; }
}
```

### Syntax: final, sealed or non-sealed children

Every permitted class must say what happens below it. It picks exactly one of three words.

```java
sealed interface Vehicle permits Bike, Truck, Scooter { }
final class Bike implements Vehicle { }                         // no children at all
sealed class Truck implements Vehicle permits CargoTruck { }    // closed again, one level down
final class CargoTruck extends Truck { }
non-sealed class Scooter implements Vehicle { }                 // open again: anyone may extend it
```

- `final` means "this branch stops here".
- `sealed` means "this branch has its own fixed list".
- `non-sealed` means "this branch is open to everyone".

```callout tip
Records are automatically `final`. That makes them the natural fit for permitted types. You never write `final` on a record.
```

### Records as permitted types

Most sealed families in real code are a sealed interface plus a few records. Each record carries the data for one kind of message. Records cannot extend a class, but they can implement an interface. So the parent is usually an interface.

```java run
public class Main {
    public static void main(String[] args) {
        DockEvent e = new BikeDocked("Harbor", "B-1042");
        System.out.println(e);
        System.out.println(DockEvent.class.isSealed());
        for (Class<?> c : DockEvent.class.getPermittedSubclasses()) {
            System.out.println("permitted: " + c.getSimpleName());
        }
    }
}

sealed interface DockEvent permits BikeDocked, BikeUndocked, Fault { }
record BikeDocked(String station, String bikeId) implements DockEvent { }
record BikeUndocked(String station, String bikeId) implements DockEvent { }
record Fault(String station, String code) implements DockEvent { }
```

The `Class` object from the reflection topic can tell you about a sealed type. `isSealed()` answers true or false. `getPermittedSubclasses()` gives back the permitted list.

### Why a closed set helps

With a sealed type, the compiler knows every possible case. Think of a teacher with a class list. If the list says 30 names, the teacher knows exactly who should answer the roll call.

```callout analogy
An open interface is a party with an open door: anyone can walk in. A sealed interface is a party with a guest list at the door. You know every face before the music starts.
```

This knowledge pays off in two places:

- A reader sees the whole family in one line, at the top of the parent.
- In the next topic, [a switch over a sealed type](/t/module-05-advanced-java/topic-13-pattern-matching) becomes exhaustive. **Exhaustive** means the compiler checks that every permitted type has a case. Forget one and the code does not compile.

In this topic you still use `instanceof` chains. The chain itself does not warn you about a missing type, but the sealed parent stops strangers from joining the family.

### Sealed types and enums compared

An **enum** (from module 2) is also a closed set. The difference is what each member can hold.

| | enum | sealed interface + records |
|---|---|---|
| Members | fixed objects, created once | fixed types, many objects each |
| Data | the same fields for every constant | each record has its own fields |
| Example | `BikeType.CLASSIC`, `BikeType.EBIKE` | `BikeDocked(station, bikeId)`, `Fault(station, code)` |

Pick an enum when every member looks the same, like bike types. Pick a sealed type when each kind carries different data, like dock messages.

## Worked example

Here is a log that turns each dock message into text and counts free docks.

```java run
import java.util.List;

public class Main {
    static String describe(DockEvent e) {
        if (e instanceof BikeDocked d) {
            return d.bikeId() + " docked at " + d.station();
        } else if (e instanceof BikeUndocked u) {
            return u.bikeId() + " left " + u.station();
        } else if (e instanceof Fault f) {
            return "Fault " + f.code() + " at " + f.station();
        }
        throw new IllegalStateException("Unknown dock event: " + e);
    }

    public static void main(String[] args) {
        List<DockEvent> events = List.of(
                new BikeDocked("Harbor", "B-1042"),
                new Fault("Harbor", "E7"),
                new BikeUndocked("Harbor", "B-1042"));
        for (DockEvent e : events) {
            System.out.println(describe(e));
        }
    }
}

sealed interface DockEvent permits BikeDocked, BikeUndocked, Fault { }
record BikeDocked(String station, String bikeId) implements DockEvent { }
record BikeUndocked(String station, String bikeId) implements DockEvent { }
record Fault(String station, String code) implements DockEvent { }
```

Line by line:

1. `sealed interface DockEvent permits ...` fixes the family at three records.
2. `e instanceof BikeDocked d` checks the type and, if it matches, names the record `d`. That name is the **pattern variable** you met in module 2.
3. `d.bikeId()` and `d.station()` are the record's accessor methods.
4. The final `throw` runs only if a type has no branch, or if e is null. With a sealed parent, that can happen only if someone edits the permits list and forgets this method.

```callout warning
The `throw` at the end is still needed here. An `if` chain cannot prove it covers every type. The switch in the next topic can, and then you can delete that line.
```

## Try it

Run the program below. Then add `Rebooted` to the `permits` list and make it implement `DockEvent`. Add `System.out.println(describe(new Rebooted("Uptown")));` at the end of `main`. Run it again and see the fallback line run for the new message.

```java run
public class Main {
    static String describe(DockEvent e) {
        if (e instanceof BikeDocked d) {
            return d.bikeId() + " docked at " + d.station();
        } else if (e instanceof Fault f) {
            return "Fault " + f.code() + " at " + f.station();
        }
        return "no branch for " + e.getClass().getSimpleName();
    }

    public static void main(String[] args) {
        System.out.println(describe(new BikeDocked("Uptown", "B-2001")));
        System.out.println(describe(new Fault("Uptown", "W3")));
    }
}

sealed interface DockEvent permits BikeDocked, Fault { }
record BikeDocked(String station, String bikeId) implements DockEvent { }
record Fault(String station, String code) implements DockEvent { }
record Rebooted(String station) { }
```

## Recap

- `sealed` plus `permits` lists every type allowed to extend a class or implement an interface.
- Each permitted class is `final`, `sealed` or `non-sealed`. Records are already `final`.
- A sealed interface with records is the usual shape for a fixed family of messages.
- A closed set means the compiler knows every case. The next topic uses that for exhaustive switches.
- Use an enum when every member holds the same fields. Use a sealed type when each kind holds different data.

## Common mistakes

**1. Implementing a sealed interface without being on the list.**

```java nocompile
sealed interface DockEvent permits BikeDocked, BikeUndocked { }
record BikeDocked(String station, String bikeId) implements DockEvent { }
record BikeUndocked(String station, String bikeId) implements DockEvent { }
record Fault(String station, String code) implements DockEvent { }
```

```
Main.java:4: error: class is not allowed to extend sealed class: DockEvent (as it is not listed in its 'permits' clause)
record Fault(String station, String code) implements DockEvent { }
^
```

Fix: add `Fault` to the `permits` list. This error is the whole point of sealing, so read it as "the family is closed".

**2. Forgetting final, sealed or non-sealed on a permitted class.**

```java nocompile
sealed interface DockEvent permits Fault { }
class Fault implements DockEvent { }
```

```
Main.java:2: error: sealed, non-sealed or final modifiers expected
class Fault implements DockEvent { }
^
```

Fix: write `final class Fault`, or turn it into a record, which is final already.

**3. Listing a type in permits that does not implement the parent.**

```java nocompile
sealed interface DockEvent permits BikeDocked, Fault { }
record BikeDocked(String station, String bikeId) implements DockEvent { }
record Fault(String station, String code) { }
```

```
Main.java:1: error: invalid permits clause
sealed interface DockEvent permits BikeDocked, Fault { }
                                               ^
  (subclass Fault must extend sealed class)
```

Fix: add `implements DockEvent` to `Fault`. The list and the `implements` must agree in both directions.

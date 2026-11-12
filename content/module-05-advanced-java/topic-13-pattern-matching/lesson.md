## Hook

Picture a mail sorter at a post office. Each envelope gets a quick look: letter, parcel or postcard? Then it goes in the right bin. If a new kind of mail appears, a good sorter shouts "I have no bin for this!" before anything gets lost.

Theo wants the dock event handler to work like that sorter. Right now it is a long chain of `instanceof` checks, and a new event type would fall through silently. In this topic you replace the chain with a `switch` that unpacks each event. Best of all, the compiler shouts when a type has no case.

## Concept

### Recap: pattern variables

In module 2 and in the sealed types topic you wrote this:

```java nocompile
if (e instanceof Fault f) {
    System.out.println(f.code());
}
```

`Fault f` is a **type pattern**. It tests the type and, on a match, gives you a variable `f` of that type. Java 21 lets you use the same patterns in more places.

### Syntax: type patterns in a switch

```java nocompile
String text = switch (e) {
    case BikeDocked d -> d.bikeId() + " docked";
    case BikeUndocked u -> u.bikeId() + " left";
    case Fault f -> "fault " + f.code();
};
```

- `switch (e)` looks at the object `e`.
- Each `case` holds a type pattern. The first one that matches wins.
- The arrow `->` gives the value for that case.
- The whole `switch` is a **switch expression**: it produces a value, so it ends with `;`.

### Exhaustive switches over a sealed type

`DockEvent` is sealed, so the compiler knows its full family. A switch with a case for every permitted type is **exhaustive**. That means it covers every possible value. You need no `default`.

Now the important part. Add a fourth record to the `permits` list, and every switch that lacks a case for it stops compiling:

```
Main.java:3: error: the switch expression does not cover all possible input values
        return switch (e) {
               ^
```

That error is the sorter shouting "no bin for this". It points at each switch you need to update.

```callout warning
Adding `default -> ...` to a switch over a sealed type switches this safety off. The default quietly catches the new type, and the compiler has nothing to complain about. Leave the default out when you want the check.
```

```java run
public class Main {
    static String describe(DockEvent e) {
        return switch (e) {
            case BikeDocked d -> d.bikeId() + " docked at " + d.station();
            case BikeUndocked u -> u.bikeId() + " left " + u.station();
            case Fault f -> "Fault " + f.code() + " at " + f.station();
        };
    }

    public static void main(String[] args) {
        System.out.println(describe(new BikeDocked("Harbor", "B-1042")));
        System.out.println(describe(new Fault("Old Mill", "E7")));
    }
}

sealed interface DockEvent permits BikeDocked, BikeUndocked, Fault { }
record BikeDocked(String station, String bikeId) implements DockEvent { }
record BikeUndocked(String station, String bikeId) implements DockEvent { }
record Fault(String station, String code) implements DockEvent { }
```

### Syntax: record patterns

A **record pattern** matches a record and unpacks its fields in one step. You list a variable for each record component, in order.

```java nocompile
case BikeDocked(String station, String bikeId) -> bikeId + " docked at " + station;
```

You can write `var` and let Java work out each type:

```java nocompile
case BikeDocked(var station, var bikeId) -> bikeId + " docked at " + station;
```

```callout jargon
**Deconstruction** means taking a record apart into its fields. A record pattern deconstructs the record, so you skip calls like `d.station()`.
```

The names are yours to pick. Only the order and the types must match the record. Record patterns work with `instanceof` too:

```java nocompile
if (e instanceof Fault(var station, var code)) {
    System.out.println(code + " at " + station);
}
```

### Syntax: nested record patterns

A record field can itself be a record. You can unpack both levels in one pattern.

```java run
public class Main {
    public static void main(String[] args) {
        Object o = new Trip(new Stop("Riverside", 4), new Stop("Harbor", 9));
        if (o instanceof Trip(Stop(var from, var a), Stop(var to, var b))) {
            System.out.println(from + " -> " + to);
            System.out.println("free docks: " + a + " and " + b);
        }
    }
}

record Stop(String station, int freeDocks) { }
record Trip(Stop start, Stop end) { }
```

`Trip(Stop(var from, var a), Stop(var to, var b))` matches a `Trip`, then matches each `Stop` inside it.

### Syntax: guards with when

A **guard** is an extra true or false test on a case. You write it after the pattern with the word `when`.

```java nocompile
case Fault f when f.code().startsWith("E") -> 1;
case Fault f -> 2;
```

The first case only matches faults whose code starts with `"E"`. Every other fault falls to the second case. Order matters: Java tries cases from top to bottom.

### Syntax: case null

A plain switch throws a `NullPointerException` when its value is `null`. Add `case null` to handle it yourself.

```java nocompile
String text = switch (o) {
    case null -> "no event";
    case DockEvent e -> "an event";
    default -> "something else";
};
```

When the switch looks at an `Object`, the family is open. Anything could arrive. So here you do need a `default`.

```java run crash
public class Main {
    static String kind(Object o) {
        return switch (o) {
            case String s -> "text";
            default -> "other";
        };
    }

    public static void main(String[] args) {
        System.out.println(kind("Harbor"));
        System.out.println(kind(null));
    }
}
```

The first call prints `text`. The second call crashes, because this switch has no `case null`.

## Worked example

Here is the event handler from Theo's ticket, with the new `Rebalanced` event for the truck.

```java run
public class Main {
    static String describe(DockEvent e) {
        return switch (e) {
            case BikeDocked(var station, var bikeId) -> bikeId + " docked at " + station;
            case BikeUndocked(var station, var bikeId) -> bikeId + " left " + station;
            case Fault(var station, var code) -> "Fault " + code + " at " + station;
            case Rebalanced(var from, var to, var bikes) ->
                    "Truck moved " + bikes + " bikes from " + from + " to " + to;
        };
    }

    static int priority(DockEvent e) {
        return switch (e) {
            case Fault f when f.code().startsWith("E") -> 1;
            case Fault f -> 2;
            case Rebalanced r when r.bikes() > 10 -> 3;
            default -> 4;
        };
    }

    public static void main(String[] args) {
        DockEvent[] events = {
            new Rebalanced("Riverside", "Harbor", 5),
            new Fault("Harbor", "E7"),
            new Fault("Uptown", "W2")
        };
        for (DockEvent e : events) {
            System.out.println(priority(e) + " " + describe(e));
        }
    }
}

sealed interface DockEvent permits BikeDocked, BikeUndocked, Fault, Rebalanced { }
record BikeDocked(String station, String bikeId) implements DockEvent { }
record BikeUndocked(String station, String bikeId) implements DockEvent { }
record Fault(String station, String code) implements DockEvent { }
record Rebalanced(String from, String to, int bikes) implements DockEvent { }
```

Line by line:

1. `describe` has one record pattern per permitted type and no `default`. Remove the `Rebalanced` case and it stops compiling.
2. `Rebalanced(var from, var to, var bikes)` unpacks all three fields. `bikes` is an `int`, because `var` takes the record's type.
3. In `priority`, the guarded `Fault` case comes first. The plain `Fault` case catches the rest.
4. `priority` does use `default`. Four different types all get the same answer, so one fallback is clearer than four cases.

```callout tip
Choose on purpose. Leave out `default` when each type needs its own handling, so the compiler warns you about new types. Use `default` when "everything else" really means everything else.
```

## Try it

In the worked example, add `record Rebooted(String station) implements DockEvent { }` and add `Rebooted` to the `permits` list. Run it: `describe` reports "the switch expression does not cover all possible input values". Then add a case for it in `describe` and run again.

Or play with guards here. Change the `when` test, or the order of the cases, and predict the output before you run it.

```java run
public class Main {
    static String label(Fault f) {
        return switch (f) {
            case Fault(var station, var code) when code.startsWith("E") -> "urgent at " + station;
            case Fault(var station, var code) when station.equals("Depot") -> "check later";
            case Fault(var station, var code) -> "normal at " + station;
        };
    }

    public static void main(String[] args) {
        System.out.println(label(new Fault("Harbor", "E7")));
        System.out.println(label(new Fault("Depot", "W2")));
        System.out.println(label(new Fault("Uptown", "W2")));
    }
}

record Fault(String station, String code) { }
```

## Recap

- A switch can hold type patterns: `case Fault f -> ...`.
- A switch over a sealed type with a case per permitted type is exhaustive. It needs no `default`, and a missing case is a compile error.
- A record pattern unpacks fields: `case BikeDocked(var station, var bikeId) -> ...`. Patterns can nest.
- A guard adds a test with `when`. Put the guarded case before the plain one.
- `case null` handles null. Without it, a switch throws `NullPointerException`.

## Common mistakes

**1. Missing a case in a switch over a sealed type.**

```java nocompile
public class Main {
    static String describe(DockEvent e) {
        return switch (e) {
            case BikeDocked d -> "docked";
            case BikeUndocked u -> "left";
        };
    }
    public static void main(String[] args) { }
}
sealed interface DockEvent permits BikeDocked, BikeUndocked, Fault { }
record BikeDocked(String station, String bikeId) implements DockEvent { }
record BikeUndocked(String station, String bikeId) implements DockEvent { }
record Fault(String station, String code) implements DockEvent { }
```

```
Main.java:3: error: the switch expression does not cover all possible input values
        return switch (e) {
               ^
```

Fix: add `case Fault f -> "fault";`. Resist adding `default`, because it hides the next new type too.

**2. Putting the plain case above the guarded one.**

```java nocompile
public class Main {
    static int priority(DockEvent e) {
        return switch (e) {
            case Fault f -> 2;
            case Fault(var station, var code) when code.startsWith("E") -> 1;
            default -> 4;
        };
    }
    public static void main(String[] args) { }
}
sealed interface DockEvent permits BikeDocked, Fault { }
record BikeDocked(String station, String bikeId) implements DockEvent { }
record Fault(String station, String code) implements DockEvent { }
```

```
Main.java:5: error: this case label is dominated by a preceding case label
            case Fault(var station, var code) when code.startsWith("E") -> 1;
                 ^
```

Fix: move the guarded case above `case Fault f`. **Dominated** means an earlier case already catches everything this one could.

**3. Forgetting null.**

```java nocompile
String text = switch (o) {
    case DockEvent e -> "an event";
    default -> "something else";
};
```

When `o` is `null`, this throws `java.lang.NullPointerException`. The `default` case does not catch `null`. Fix: add `case null -> "no event";` as the first case.

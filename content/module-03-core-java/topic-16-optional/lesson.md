## Hook

A rider at Harbor typed in B-0007. That bike was retired last month. The lookup method found nothing and returned `null`. The next line called a method on that `null`, and the app crashed.

Picture a parcel locker. You open the door, and the box inside may hold a parcel or may be empty. You always get a box. You just check inside before you use what is there.

Java has a box like that. It is called `Optional`. A method that returns an `Optional` tells you, right in its signature, that the answer might be missing.

## Concept

A **NullPointerException** is the crash you get when you call a method on `null`. `null` means "no object here". The trouble is that a `String` return type does not warn you that `null` might come back.

`Optional<T>` is a box that holds one value of type `T`, or nothing. A box with nothing inside is called **empty**. You import it from `java.util`:

```java nocompile
import java.util.Optional;
```

```callout analogy
Optional is a parcel locker box. Every lookup hands you a box. Some boxes hold a parcel and some are empty, and you open it carefully.
```

### Syntax: creating an Optional

```java nocompile
Optional<String> full = Optional.of("Harbor");          // must not be null
Optional<String> maybe = Optional.ofNullable(station);  // empty if station is null
Optional<String> none = Optional.empty();               // always empty
```

Use `ofNullable` when the value comes from a place that may return `null`, such as `Map.get`. `Optional.of(null)` throws a `NullPointerException` straight away.

```java nocompile
Map<String, String> docks = Map.of("B-1042", "Riverside");
Optional<String> a = Optional.ofNullable(docks.get("B-1042")); // holds "Riverside"
Optional<String> b = Optional.ofNullable(docks.get("B-0007")); // empty
```

### Syntax: checking the box

```java nocompile
boolean has = maybe.isPresent();  // true if something is inside
boolean none = maybe.isEmpty();   // true if nothing is inside
```

### Syntax: getting the value out

Each of these returns the value when the box is full. They differ in what happens when it is empty.

```java nocompile
String s1 = maybe.orElse("unknown");             // use this default
String s2 = maybe.orElseGet(() -> lookUpAgain()); // run this lambda for a default
String s3 = maybe.orElseThrow();                  // throw NoSuchElementException
```

- `orElse(value)` always builds the default, even when the box is full.
- `orElseGet(supplier)` runs the lambda only when the box is empty. Use it when the default is slow to make.
- `orElseThrow()` says "this must be here". It throws `NoSuchElementException` when the box is empty.

```callout warning
Optional also has get(). It throws when the box is empty, just like orElseThrow(). Prefer orElseThrow(), because its name says what can happen.
```

### Syntax: orElseThrow with your own exception

```java nocompile
Bike bike = findBike(bikeId).orElseThrow(() -> new BikeNotFoundException(bikeId));
```

Plain `orElseThrow()` throws a vague `NoSuchElementException: No value present`. Give it a lambda that makes your own exception instead. The lambda runs only when the box is empty. Real services use this line all the time, for example to turn "no such bike" into a "not found" reply to the app.

```java run
import java.util.Map;
import java.util.Optional;

public class Main {
    static final Map<String, String> DOCKS = Map.of("B-1042", "Harbor", "B-2001", "Uptown");

    static Optional<String> findStation(String bikeId) {
        return Optional.ofNullable(DOCKS.get(bikeId));
    }

    public static void main(String[] args) {
        String station = findStation("B-1042")
                .orElseThrow(() -> new BikeNotFoundException("B-1042"));
        System.out.println(station);

        try {
            findStation("B-9999").orElseThrow(() -> new BikeNotFoundException("B-9999"));
        } catch (BikeNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
}

class BikeNotFoundException extends RuntimeException {
    BikeNotFoundException(String bikeId) {
        super("No bike with id " + bikeId);
    }
}
```

It prints `Harbor`, then `No bike with id B-9999`.

### Syntax: map and ifPresent

`map` changes the value inside the box and gives you a new box. An empty box stays empty, and the lambda never runs.

```java nocompile
Optional<Integer> length = maybe.map(s -> s.length());
String shout = maybe.map(String::toUpperCase).orElse("NONE");
```

`ifPresent` runs a lambda only when the box is full.

```java nocompile
maybe.ifPresent(s -> System.out.println("Docked at " + s));
```

### Syntax: filter, flatMap and ifPresentOrElse

```java nocompile
box.filter(b -> b.type().equals("ebike"))          // keep the value only if the test passes
box.flatMap(id -> findBike(id))                    // the lambda itself returns an Optional
box.ifPresentOrElse(b -> show(b), () -> showNone()) // one lambda for full, one for empty
```

- `filter` empties the box when the test fails.
- `flatMap` is for a lambda that already returns an `Optional`. With `map`, you would get a box inside a box, `Optional<Optional<Bike>>`. `flatMap` keeps one box.
- `ifPresentOrElse` is `ifPresent` with a second lambda for the empty case.

```java run
import java.util.Map;
import java.util.Optional;

public class Main {
    record Bike(String id, String type) { }

    static final Map<String, Bike> BIKES = Map.of("B-1042", new Bike("B-1042", "ebike"));
    static final Map<String, String> RENTAL_BIKE = Map.of("rental-17", "B-1042", "rental-18", "B-7777");

    static Optional<String> bikeIdFor(String rentalId) {
        return Optional.ofNullable(RENTAL_BIKE.get(rentalId));
    }

    static Optional<Bike> findBike(String bikeId) {
        return Optional.ofNullable(BIKES.get(bikeId));
    }

    public static void main(String[] args) {
        for (String rental : new String[] {"rental-17", "rental-18", "rental-99"}) {
            bikeIdFor(rental)
                    .flatMap(Main::findBike)
                    .filter(b -> b.type().equals("ebike"))
                    .ifPresentOrElse(
                            b -> System.out.println(rental + ": e-bike " + b.id()),
                            () -> System.out.println(rental + ": no e-bike"));
        }
    }
}
```

It prints `rental-17: e-bike B-1042`, then `rental-18: no e-bike` (bike B-7777 is unknown), then `rental-99: no e-bike` (no such rental). Not one `if` and not one `null`.

### Syntax: Optional from streams

Some stream methods may find nothing, so they return an `Optional`. `findFirst()` gives the first item that is left. `max(comparator)` gives the largest item, judged by your comparator. Now you can see what they return.

```java nocompile
Optional<Ride> first = rides.stream().filter(r -> r.minutes() > 30).findFirst();
Optional<Ride> longest = rides.stream()
        .max((a, b) -> Integer.compare(a.minutes(), b.minutes()));
```

An empty list gives an empty box, so nothing crashes.

### When not to use Optional

`Optional` is made for return values. Do not use it for fields or method parameters.

```java nocompile
class Bike {
    private Optional<String> station;      // avoid: store a plain String
}
void dock(Optional<String> station) { }   // avoid: callers must wrap every value
```

A field or parameter of type `Optional` adds a box around every value for no gain. Never return `null` from a method whose return type is `Optional`. That brings back the very crash you were avoiding.

For lists, skip `Optional` too. A method that finds rides should return an empty list, not `Optional<List<Ride>>`. An empty list already means "nothing found", and callers can loop over it with no check.

## Worked example

This program looks up two bikes. One is docked, and one is retired.

```java run
import java.util.Map;
import java.util.Optional;

public class Main {
    static Map<String, String> docks = Map.of("B-1042", "Riverside", "B-2001", "Harbor");

    static Optional<String> findStation(String bikeId) {
        return Optional.ofNullable(docks.get(bikeId));
    }

    public static void main(String[] args) {
        Optional<String> a = findStation("B-1042");
        Optional<String> b = findStation("B-0007");

        System.out.println(a.isPresent());
        System.out.println(b.isEmpty());
        System.out.println(a.orElse("unknown"));
        System.out.println(b.orElse("unknown"));
        System.out.println(a.map(String::toUpperCase).orElse("NONE"));
        b.ifPresent(s -> System.out.println("never printed"));
    }
}
```

Line by line:

- `docks.get(bikeId)` returns `null` for B-0007. `ofNullable` turns that `null` into an empty box.
- `a.isPresent()` prints `true`, and `b.isEmpty()` prints `true`.
- `a.orElse("unknown")` prints `Riverside`. `b.orElse("unknown")` prints `unknown`.
- `a.map(String::toUpperCase)` gives a box holding `RIVERSIDE`, so `orElse` prints `RIVERSIDE`.
- `b` is empty, so `ifPresent` never runs its lambda.

## Try it

This program finds the longest ride and builds a label for it.

```java run
import java.util.List;
import java.util.Optional;

public class Main {
    static String label(List<Ride> rides) {
        Optional<Ride> longest = rides.stream()
                .max((a, b) -> Integer.compare(a.minutes(), b.minutes()));
        return longest.map(r -> r.bikeId() + ": " + r.minutes() + " min").orElse("no rides");
    }

    public static void main(String[] args) {
        List<Ride> rides = List.of(
                new Ride("B-1042", "Riverside", "ebike", 80),
                new Ride("B-2001", "Harbor", "classic", 10));
        System.out.println(label(rides));
        System.out.println(label(List.of()));
    }
}

record Ride(String bikeId, String station, String type, int minutes) {}
```

Change the 80 to 5 and run it again. Then swap `orElse("no rides")` for `orElseThrow()` and watch the empty list crash.

## Recap

- `Optional<T>` is a box that holds one value or nothing. It warns callers that the answer may be missing.
- Make one with `Optional.of`, `Optional.ofNullable` or `Optional.empty()`.
- Get the value with `orElse`, `orElseGet` or `orElseThrow(() -> new YourException(...))`. Avoid `isPresent()` followed by `get()`.
- `map`, `filter` and `flatMap` work on the value inside. `ifPresent` and `ifPresentOrElse` run code for a full or an empty box.
- Stream methods such as `findFirst` and `max` return an `Optional`. Use `Optional` for return values only, and return an empty list instead of `Optional<List>`.

## Common mistakes

**1. Using Optional.of on a value that may be null.**

```java
import java.util.Map;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        Map<String, String> docks = Map.of("B-1042", "Riverside");
        Optional<String> s = Optional.of(docks.get("B-0007"));
        System.out.println(s.orElse("unknown"));
    }
}
```

The program crashes with `java.lang.NullPointerException`. `Optional.of` refuses `null`. Use `Optional.ofNullable(docks.get("B-0007"))` instead.

**2. Treating the Optional as the value itself.**

```java nocompile
Optional<String> station = findStation("B-1042");
String name = station;
```

The compiler says `incompatible types: Optional<String> cannot be converted to String`. Open the box first, for example with `station.orElse("unknown")`.

**3. Calling orElseThrow on a box that might be empty.**

```java
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        List<Integer> minutes = List.of();
        Optional<Integer> longest = minutes.stream().max(Integer::compare);
        System.out.println(longest.orElseThrow());
    }
}
```

The program crashes with `java.util.NoSuchElementException: No value present`. Use `orElseThrow` only when a missing value is a real bug. Otherwise give a default with `orElse(0)`.

**4. Checking isPresent, then calling get.**

```java nocompile
Optional<String> station = findStation("B-1042");
if (station.isPresent()) {
    System.out.println("Docked at " + station.get());
} else {
    System.out.println("Not docked");
}
```

This works, but it is the old `null` check wearing a costume. Nothing stops someone from deleting the `if` and keeping the `get()`, which then crashes. Write it in one step: `station.ifPresentOrElse(s -> System.out.println("Docked at " + s), () -> System.out.println("Not docked"));`, or `System.out.println(station.map(s -> "Docked at " + s).orElse("Not docked"));`.

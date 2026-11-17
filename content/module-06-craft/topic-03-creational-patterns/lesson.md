## Hook

Imagine ordering a sandwich by shouting nine words in a row: "white, ham, cheese, no, yes, toasted, large, mustard, Tuesday". The cook has to guess which word means what. Swap two words and you get a cold sandwich with no ham.

Spoke's `Ride` constructor works like that shout. It takes nine values in a fixed order. Last week someone swapped the start and end station, and rider r1 got a receipt that said Uptown to Harbor. The compiler said nothing, because both values were Strings. On top of that, the app builds a brand new `PriceTable` for every request, even though the prices never change.

In this topic you learn three ways to make objects safer to create: factories, builders and singletons.

## Concept

### What a design pattern is

```callout jargon
A **design pattern** is a named, well-known answer to a problem that keeps coming back in code. A **creational pattern** is a pattern about creating objects.
```

A pattern is a shape for your own code. You write it yourself, so there is nothing to download. Knowing the name helps you talk to teammates: "use a builder here" says a lot in three words.

### The problem: a long constructor

```java nocompile
Ride ride = new Ride("B-1042", "Uptown", "Harbor", "classic", 25, "r1", false, true, 0);
```

Read that line out loud. Which String is the start station? What do `false`, `true` and `0` mean? You cannot tell without opening the `Ride` class. Each pattern below fixes a different part of this.

### Syntax: a static factory method

```java
class Ride {
    private final String bikeId;
    private final String type;

    private Ride(String bikeId, String type) {
        this.bikeId = bikeId;
        this.type = type;
    }

    static Ride classic(String bikeId) {
        return new Ride(bikeId, "classic");
    }

    static Ride ebike(String bikeId) {
        return new Ride(bikeId, "ebike");
    }
}
```

- A **static factory method** is a `static` method that creates and returns an object of its own class.
- The constructor is `private`, so outside code must go through the factory methods.
- Each method has a name that says what you get. People call these **named constructors**.

You have used static factories for a long time already:

```java
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> stations = List.of("Harbor", "Uptown");
        Integer docks = Integer.valueOf(12);
        String text = String.valueOf(25);
        System.out.println(stations + " " + docks + " " + text);
    }
}
```

`List.of`, `Integer.valueOf` and `String.valueOf` all create an object without the word `new` at the call site.

```java run
public class Main {
    public static void main(String[] args) {
        Ride a = Ride.classic("B-1042");
        Ride b = Ride.ebike("B-2001");
        System.out.println(a.describe());
        System.out.println(b.describe());
    }
}

class Ride {
    private final String bikeId;
    private final String type;

    private Ride(String bikeId, String type) {
        this.bikeId = bikeId;
        this.type = type;
    }

    static Ride classic(String bikeId) {
        return new Ride(bikeId, "classic");
    }

    static Ride ebike(String bikeId) {
        return new Ride(bikeId, "ebike");
    }

    String describe() {
        return type + " ride on " + bikeId;
    }
}
```

`Ride.ebike("B-2001")` reads like a sentence. Nobody can pass the type in the wrong slot, because there is no type slot.

### Syntax: a simple factory that picks a subclass

Sometimes the caller knows a word, like `"cargo"`, and needs the right class behind it. A **simple factory** is one method that reads the word and picks which class to create.

```java
interface Bike { int centsPerMinute(); }
class ClassicBike implements Bike { public int centsPerMinute() { return 15; } }
class EBike implements Bike { public int centsPerMinute() { return 30; } }

class BikeFactory {
    static Bike create(String type) {
        return switch (type) {
            case "classic" -> new ClassicBike();
            case "ebike" -> new EBike();
            default -> throw new IllegalArgumentException("unknown bike type: " + type);
        };
    }
}
```

- The return type is the interface `Bike`. The caller never names `EBike`.
- The `switch` is the only place in the app that knows which word maps to which class.
- An unknown word fails loudly with an exception. A silent default bike would hide a typo.

```java nocompile
Bike bike = BikeFactory.create("ebike");
System.out.println(bike.centsPerMinute());   // 30
```

```callout tip
The bigger cousin of this idea is called the **factory method pattern**. There, a parent class declares an abstract `createBike()` method, and each subclass decides which bike it returns. The goal is the same: the code that uses a bike does not pick its class.
```

### Syntax: a builder

A **builder** is a helper object that collects values one at a time, with a named method for each. At the end you call `build()`, and it creates the real object.

```java
record Receipt(String rideId, String riderId, String note) {

    static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private String rideId;
        private String riderId;
        private String note = "";

        Builder ride(String rideId) {
            this.rideId = rideId;
            return this;
        }

        Builder rider(String riderId) {
            this.riderId = riderId;
            return this;
        }

        Builder note(String note) {
            this.note = note;
            return this;
        }

        Receipt build() {
            if (rideId == null) {
                throw new IllegalStateException("missing required field: ride");
            }
            if (riderId == null) {
                throw new IllegalStateException("missing required field: rider");
            }
            return new Receipt(rideId, riderId, note);
        }
    }
}
```

- `static class Builder` is a **nested class**: a class declared inside another one. `static` means you do not need a `Receipt` to make a `Builder`.
- Each setter stores one value and ends with `return this;`. That lets you chain calls with dots.
- **Required fields** start as `null`. `build()` checks them and throws if one is missing.
- **Optional fields** get a default value, like `note = ""`.

```java nocompile
Receipt r = Receipt.builder().ride("R-7").rider("r1").build();
Receipt s = Receipt.builder().rider("r1").note("helmet returned").ride("R-8").build();
```

The order of the calls no longer matters. Every value sits next to its name, so you cannot swap `from` and `to` by accident.

```callout analogy
A builder is a form with labelled boxes. A long constructor is a blank line where you write nine answers in a row. The form checks for empty boxes when you press submit, and that is `build()`.
```

### Syntax: a singleton with an enum

A **singleton** is a class that has exactly one object in the whole program. In Java, the safest way to write one is an enum with a single constant.

```java
enum PriceTable {
    INSTANCE;

    int centsPerMinute(String type) {
        return switch (type) {
            case "ebike" -> 30;
            case "cargo" -> 25;
            default -> 15;
        };
    }
}
```

```java nocompile
int cents = PriceTable.INSTANCE.centsPerMinute("ebike");   // 30
```

- Java creates `INSTANCE` once, when the enum is first used.
- Nobody can write `new PriceTable()`. The compiler forbids it for enums.
- No more fresh price table on every request.

### Why a singleton is often a smell

```callout warning
A **code smell** is a sign that something may be wrong in the design. A singleton is global: any class can reach it from anywhere. That hides who depends on it, and a test cannot swap in different prices.
```

Look at this class:

```java nocompile
class FareService {
    int fare(String type, int minutes) {
        return 100 + minutes * PriceTable.INSTANCE.centsPerMinute(type);
    }
}
```

The constructor of `FareService` takes nothing, so it looks like it needs nothing. In fact it secretly needs `PriceTable`.

### Syntax: dependency injection

**Dependency injection** means you hand an object the things it needs, usually through its constructor. It no longer reaches out to grab them.

```java nocompile
class FareService {
    private final PriceTable prices;

    FareService(PriceTable prices) {
        this.prices = prices;
    }

    int fare(String type, int minutes) {
        return 100 + minutes * prices.centsPerMinute(type);
    }
}
```

The program still creates one `PriceTable` and passes it everywhere. The difference is that the need is now visible in the constructor. Turn `PriceTable` into an interface later, and a test can pass in a fake one with its own prices.

## Worked example

Here is Shizune's ride, built three ways in one program.

```java run
public class Main {
    public static void main(String[] args) {
        Trip a = Trip.builder().from("Harbor").to("Uptown").minutes(25).build();
        Trip b = Trip.builder().minutes(10).to("Old Mill").from("Riverside").build();
        Trip c = Trip.shortHop("Depot", "Harbor");
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        try {
            Trip.builder().from("Harbor").minutes(5).build();
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

record Trip(String from, String to, int minutes) {

    static Trip shortHop(String from, String to) {
        return builder().from(from).to(to).minutes(5).build();
    }

    static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private String from;
        private String to;
        private int minutes;

        Builder from(String from) {
            this.from = from;
            return this;
        }

        Builder to(String to) {
            this.to = to;
            return this;
        }

        Builder minutes(int minutes) {
            this.minutes = minutes;
            return this;
        }

        Trip build() {
            if (from == null) {
                throw new IllegalStateException("missing required field: from");
            }
            if (to == null) {
                throw new IllegalStateException("missing required field: to");
            }
            if (minutes < 0) {
                throw new IllegalStateException("minutes must be 0 or more");
            }
            return new Trip(from, to, minutes);
        }
    }
}
```

Line by line:

1. `Trip.builder()` returns a fresh, empty `Builder`.
2. `.from("Harbor").to("Uptown")` stores each station next to its label. Trip `b` sets them in a different order and still comes out right.
3. `.build()` runs the checks, then calls the record constructor once, with every value in the right slot.
4. `Trip.shortHop(...)` is a static factory. It gives a common case a name and reuses the builder inside.
5. The last builder never calls `.to(...)`, so `build()` throws with a message that names the missing field.

The output is:

```
Trip[from=Harbor, to=Uptown, minutes=25]
Trip[from=Riverside, to=Old Mill, minutes=10]
Trip[from=Depot, to=Harbor, minutes=5]
Error: missing required field: to
```

## Try it

Run the program. Then change `FareService` so it receives the `PriceTable` through its constructor. Pass `PriceTable.INSTANCE` in from `main`. The output stays the same, but the dependency is now visible.

```java run
public class Main {
    public static void main(String[] args) {
        FareService service = new FareService();
        System.out.println(service.fare("classic", 10));
        System.out.println(service.fare("ebike", 10));
        System.out.println(PriceTable.INSTANCE == PriceTable.INSTANCE);
    }
}

enum PriceTable {
    INSTANCE;

    int centsPerMinute(String type) {
        return switch (type) {
            case "ebike" -> 30;
            case "cargo" -> 25;
            default -> 15;
        };
    }
}

class FareService {
    int fare(String type, int minutes) {
        return 100 + minutes * PriceTable.INSTANCE.centsPerMinute(type);
    }
}
```

The fares are in whole cents: 250 cents is $2.50.

## Recap

- A static factory method is a named constructor, like `Ride.classic(...)`. Make the real constructor private.
- A simple factory reads a word and returns the right class behind an interface. Unknown words throw.
- A builder sets each value by name, gives optional fields defaults, and checks required fields in `build()`.
- An enum with one constant is the safest singleton in Java.
- A singleton hides dependencies. Dependency injection passes the object in through the constructor instead.

## Common mistakes

**1. Forgetting `return this` in a builder setter.**

```java nocompile
public class Main {
    public static void main(String[] args) {
        Trip t = new Trip.Builder().from("Harbor").to("Uptown").build();
    }
}

record Trip(String from, String to) {
    static class Builder {
        private String from;
        private String to;
        void from(String from) { this.from = from; }
        Builder to(String to) { this.to = to; return this; }
        Trip build() { return new Trip(from, to); }
    }
}
```

```
Main.java:3: error: void cannot be dereferenced
        Trip t = new Trip.Builder().from("Harbor").to("Uptown").build();
                                                  ^
```

Fix: make every setter return `Builder` and end it with `return this;`.

**2. Forgetting to call `build()`.**

```java nocompile
public class Main {
    public static void main(String[] args) {
        Trip t = new Trip.Builder().from("Harbor").to("Uptown");
    }
}

record Trip(String from, String to) {
    static class Builder {
        private String from;
        private String to;
        Builder from(String from) { this.from = from; return this; }
        Builder to(String to) { this.to = to; return this; }
        Trip build() { return new Trip(from, to); }
    }
}
```

```
Main.java:3: error: incompatible types: Builder cannot be converted to Trip
        Trip t = new Trip.Builder().from("Harbor").to("Uptown");
                                                     ^
```

Fix: end the chain with `.build()`. The builder is the form. `build()` hands you the finished object.

**3. Trying to create a second enum singleton.**

```java nocompile
public class Main {
    public static void main(String[] args) {
        PriceTable fresh = new PriceTable();
    }
}

enum PriceTable { INSTANCE }
```

```
Main.java:3: error: enum classes may not be instantiated
        PriceTable fresh = new PriceTable();
                           ^
```

Fix: use `PriceTable.INSTANCE`, or better, receive it through a constructor. The error is the enum protecting its "only one" promise.

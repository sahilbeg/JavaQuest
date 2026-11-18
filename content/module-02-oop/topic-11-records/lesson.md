## Hook

Think of a till receipt. It lists what you bought and the price. Nobody edits a receipt after it prints. It is just a record of what happened.

A ride at Spoke works the same way. It has a bike id, a bike type and the number of minutes. The old `Ride` class needed 60 lines to hold those three values. Two of those lines had bugs. This page shows you how to write the same thing in one line.

## Concept

In the last topic you wrote `equals`, `hashCode` and `toString` by hand. That code is long, and it is easy to get one field wrong. For classes that only carry data, Java can write all of it for you.

```callout jargon
A **record** is a special kind of class that only carries data. You list its values once, and Java writes the fields, the constructor, the getters, `equals`, `hashCode` and `toString`.
```

The values you list are called **components**.

### Syntax: declaring a record

```java
record Ride(String bikeId, String type, int minutes) { }
```

- `record` replaces the word `class`.
- The round brackets ( ) hold the components, written like method parameters.
- The braces can stay empty. Java fills in the rest.

That one line gives you everything below:

| You get | What it does |
|---|---|
| a private final field per component | stores `bikeId`, `type` and `minutes` |
| a constructor | `new Ride("B-1042", "classic", 12)` |
| an accessor per component | `ride.minutes()` returns the minutes |
| `equals` | true when every component matches |
| `hashCode` | matches `equals`, so records work as map keys later |
| `toString` | `Ride[bikeId=B-1042, type=classic, minutes=12]` |

### Syntax: accessors

```java nocompile
Ride ride = new Ride("B-1042", "classic", 12);
int m = ride.minutes();
String id = ride.bikeId();
```

An **accessor** is a method that reads one component. Its name is the component name, with no `get` in front. You write `ride.minutes()`. There is no `getMinutes()`.

```java run
public class Main {
    record Ride(String bikeId, String type, int minutes) { }

    public static void main(String[] args) {
        Ride ride = new Ride("B-1042", "classic", 12);
        System.out.println(ride.bikeId());
        System.out.println(ride.minutes());
        System.out.println(ride);
    }
}
```

The last line prints the generated `toString`: `Ride[bikeId=B-1042, type=classic, minutes=12]`.

### Generated equals

Two records are equal when all their components are equal.

```java nocompile
Ride a = new Ride("B-1042", "classic", 12);
Ride b = new Ride("B-1042", "classic", 12);
System.out.println(a.equals(b));   // true
System.out.println(a == b);        // false, two separate objects
```

`==` still checks whether two variables point at the same object. `equals` checks the values. Watch everything Java wrote for you at work:

```animate
title: A one-line record at work
kind: code
code: |
  public class Main {
      record Ride(String bikeId, String type, int minutes) { }

      public static void main(String[] args) {
          Ride a = new Ride("B-1042", "classic", 12);
          Ride b = new Ride("B-1042", "classic", 12);
          System.out.println(a);
          System.out.println(a == b);
          System.out.println(a.equals(b));
          Ride longer = new Ride(a.bikeId(), a.type(), a.minutes() + 5);
          System.out.println(longer.minutes() + " vs " + a.minutes());
      }
  }
steps:
  - line: 5
    vars: { a: "@r1" }
    objects: { r1: { type: Ride, fields: { bikeId: '"B-1042"', type: '"classic"', minutes: 12 } } }
    say: Java wrote the constructor for you. It stores the three components in final fields.
  - line: 6
    vars: { b: "@r2" }
    objects: { r2: { type: Ride, fields: { bikeId: '"B-1042"', type: '"classic"', minutes: 12 } } }
    say: A second new builds a second Ride with the same values.
  - line: 7
    out: Ride[bikeId=B-1042, type=classic, minutes=12]
    say: println calls the generated toString. It prints the record name and every component as name=value.
  - line: 8
    out: "false"
    say: == asks if a and b point at one object. They point at two, so false.
  - line: 9
    out: "true"
    say: The generated equals compares every component. All three match, so true.
  - line: 10
    vars: { longer: "@r3" }
    objects: { r3: { type: Ride, fields: { bikeId: '"B-1042"', type: '"classic"', minutes: 17 } } }
    say: You cannot change a's minutes, because the fields are final. So you build a new Ride from a's accessors, with 5 more minutes.
  - line: 11
    out: 17 vs 12
    say: The new ride has 17 minutes. The old ride still has 12, untouched, like a printed receipt.
```

### Records cannot change

Every field in a record is `final`. That means it gets a value once and keeps it forever. A record has no setters. (A field that points at an array is a special case. You will see it in a moment.)

```callout analogy
A record is like a printed receipt. To fix a mistake, you print a new receipt. You never scribble on the old one.
```

To "change" a ride, you build a new one:

```java nocompile
Ride longer = new Ride(ride.bikeId(), ride.type(), ride.minutes() + 5);
```

The record itself is also `final`. No other class can `extend` it.

### Syntax: compact constructor

Sometimes the incoming values need cleaning up. A **compact constructor** runs before Java stores the components.

```java
record Ride(String bikeId, String type, int minutes) {
    Ride {
        if (minutes < 0) {
            minutes = 0;
        }
    }
}
```

- It has the record name and no round brackets ( ): `Ride { ... }`.
- Inside, the component names are the incoming parameters.
- You may change a parameter. Java stores the new value after the block ends.
- You never write `this.minutes = minutes;`. Java does that for you.

```java run
public class Main {
    record Ride(String bikeId, String type, int minutes) {
        Ride {
            if (minutes < 0) {
                minutes = 0;
            }
        }
    }

    public static void main(String[] args) {
        Ride broken = new Ride("B-2001", "ebike", -4);
        System.out.println(broken.minutes());
    }
}
```

This prints `0`. A faulty dock that reports minus 4 minutes is billed as a 0 minute ride, not a negative fare.

### Clamp or refuse?

Turning -4 into 0 is called **clamping**. It hides the bad value. Most of the time, a bad value means a bug somewhere else. Hiding it lets the bug keep running.

```callout jargon
To **clamp** a value is to push it back inside allowed limits. -4 minutes becomes 0, the lowest allowed value.
```

So the usual choice in real code is to refuse, loudly. Throw an `IllegalArgumentException`, like the constructors in topic 2:

```java run crash
public class Main {
    record Ride(String bikeId, String type, int minutes) {
        Ride {
            if (minutes < 0) {
                throw new IllegalArgumentException("minutes cannot be negative: " + minutes);
            }
        }
    }

    public static void main(String[] args) {
        Ride ok = new Ride("B-1042", "ebike", 12);
        System.out.println(ok.minutes());
        Ride broken = new Ride("B-2001", "ebike", -4);
        System.out.println(broken.minutes());
    }
}
```

It prints `12`, then stops with `java.lang.IllegalArgumentException: minutes cannot be negative: -4`. No broken ride ever exists.

Clamp only when you know where the bad value comes from, and 0 is the honest answer. Spoke's old docks sometimes report a few minutes below zero when their clock drifts. Kakashi decided those rides count as 0 minutes. Your exercise follows that rule, so it clamps.

```callout tip
Default to refusing. Clamp only when the team has agreed that the bad value is known noise, and say so in a comment.
```

### final freezes the arrow, not the object

A record's fields are `final`. For a number like `minutes`, that is the end of the story. But a field that holds an object only holds an arrow to it. `final` stops the arrow from moving. It does not stop anyone changing the object at the end of the arrow.

An array is an object. Watch a "fixed" trip change under your feet:

```animate
title: final froze the arrow, not the array
kind: code
code: |
  public class Main {
      record Trip(String rentalId, String[] stops) { }

      public static void main(String[] args) {
          String[] stops = {"Riverside", "Harbor"};
          Trip trip = new Trip("rental-17", stops);
          stops[1] = "Depot";
          System.out.println(trip.stops()[1]);
      }
  }
steps:
  - line: 5
    vars: { stops: "@arr" }
    objects: { arr: { type: "String[]", fields: { "0": '"Riverside"', "1": '"Harbor"' } } }
    say: main builds an array of two stops. The variable stops holds an arrow to it.
  - line: 6
    vars: { trip: "@t" }
    objects: { t: { type: Trip, fields: { rentalId: '"rental-17"', stops: "@arr" } } }
    say: The record stores the arrow it was given, not a copy. Now two arrows point at one array.
  - line: 7
    objects: { arr: { type: "String[]", fields: { "0": '"Riverside"', "1": '"Harbor"' } } }
    say: main changes slot 1 through its own arrow. The record's final field cannot stop this. It never moved.
  - line: 7
    objects: { arr: { type: "String[]", fields: { "0": '"Riverside"', "1": '"Depot"' } } }
    say: The array now says Depot. There is only one array, so the trip sees it too.
  - line: 8
    out: Depot
    say: The trip was meant to be a fixed receipt. It now lists a stop the rider never made.
```

There is a second surprise. A record's `equals` compares an array component with `==`. Two trips with the same stops in two different arrays are not equal.

```java run
public class Main {
    record Trip(String rentalId, String[] stops) { }

    public static void main(String[] args) {
        Trip a = new Trip("rental-17", new String[] {"Riverside", "Depot"});
        Trip b = new Trip("rental-17", new String[] {"Riverside", "Depot"});
        System.out.println(a.equals(b));
    }
}
```

It prints `false`. Avoid array components in records. Module 3 brings lists, which compare their items properly.

### Immutable classes and defensive copies

```callout jargon
An **immutable** object can never change after it is built, like a String. A **defensive copy** is a copy you make so outside code cannot reach your object's insides.
```

To make an object truly immutable, follow this recipe:

1. Make the class `final`, so no child class can add changing parts. A record already is.
2. Make every field `private final`. A record already does.
3. Write no setters.
4. **Copy in:** copy any array or list you are given, in the constructor.
5. **Copy out:** return a copy from any getter, never your own array.
6. A "change" method returns a new object, like `withMinutes(20)`.

```java run
public class Main {
    record Trip(String rentalId, String[] stops) {
        Trip {
            stops = stops.clone();
        }

        public String[] stops() {
            return stops.clone();
        }
    }

    public static void main(String[] args) {
        String[] stops = {"Riverside", "Harbor"};
        Trip trip = new Trip("rental-17", stops);
        stops[1] = "Depot";
        trip.stops()[0] = "Uptown";
        System.out.println(trip.stops()[0] + " -> " + trip.stops()[1]);
    }
}
```

It prints `Riverside -> Harbor`. `clone()` on an array builds a new array with the same items. The compact constructor copies in, and the accessor copies out. Neither change reached the trip.

With lists from module 3, the copy is one call: `stops = List.copyOf(stops);`. It makes a list nobody can change.

### Syntax: methods in a record

A record can hold your own methods too. Inside the record, you can use the component names directly.

```java
record Ride(String bikeId, String type, int minutes) {
    boolean isShort() {
        return minutes < 10;
    }
}
```

```callout tip
Use a record when a thing is just data that never changes, like a ride or a receipt. Use a normal class when the object needs to change over time, like a bike's battery.
```

## Worked example

Here is a ride that works out its own fare. Classic bikes cost $0.15 per minute, e-bikes $0.30 and cargo bikes $0.25. Every ride adds the $1.00 unlock fee.

```java run
public class Main {
    record Ride(String bikeId, String type, int minutes) {
        Ride {
            if (minutes < 0) {
                minutes = 0;
            }
        }

        double fare() {
            double rate = 0.15;
            if (type.equals("ebike")) {
                rate = 0.30;
            } else if (type.equals("cargo")) {
                rate = 0.25;
            }
            return 1.00 + minutes * rate;
        }
    }

    public static void main(String[] args) {
        Ride ride = new Ride("B-1042", "ebike", 10);
        System.out.println(ride.fare());
        System.out.println(ride.equals(new Ride("B-1042", "ebike", 10)));
    }
}
```

Line by line:

1. `record Ride(...)` declares three components.
2. The compact constructor turns negative minutes into 0.
3. `fare()` picks a rate from `type`, then adds the unlock fee.
4. `new Ride("B-1042", "ebike", 10)` builds a ride with 10 minutes.
5. `ride.fare()` returns `1.00 + 10 * 0.30`, which prints `4.0`.
6. The second ride has the same components, so `equals` prints `true`.

```callout warning
The exercise keeps `fare()` as a `double` of dollars, to stay small. Real billing never does that, because `double` cannot hold 0.1 exactly. It stores whole cents in a `long`: 100 cents to unlock, plus 30 cents a minute for an e-bike. The [static members](/t/module-02-oop/topic-04-static-members) ticket explains why.
```

## Try it

Change the worked example above. Make the ride a `"cargo"` ride with `-7` minutes. Predict the fare first, then press Run. You should see `1.0`, because the minutes become 0.

## Recap

- A record is a short class for plain data: `record Ride(String bikeId, String type, int minutes) { }`.
- Accessors use the component name: `ride.minutes()`.
- Java writes `equals`, `hashCode` and `toString` from the components.
- Record fields are final, but `final` freezes the arrow, not the object. Copy arrays in and out.
- A compact constructor `Ride { ... }` can clean up values before they are stored.

## Common mistakes

**1. Calling a getter that does not exist**

```java nocompile
Ride ride = new Ride("B-1042", "classic", 12);
int m = ride.getMinutes();
```

The compiler says:

```text
error: cannot find symbol
  symbol:   method getMinutes()
```

Fix: records name accessors after the component. Write `ride.minutes()`.

**2. Trying to change a component**

```java nocompile
record Ride(String bikeId, String type, int minutes) {
    void addMinutes(int extra) {
        minutes = minutes + extra;
    }
}
```

The compiler says:

```text
error: cannot assign a value to final variable minutes
```

Fix: return a new record instead: `return new Ride(bikeId, type, minutes + extra);` with return type `Ride`.

**3. Assigning the field in a compact constructor**

```java nocompile
record Ride(String bikeId, String type, int minutes) {
    Ride {
        if (minutes < 0) {
            this.minutes = 0;
        }
    }
}
```

The compiler says:

```text
error: cannot assign a value to final variable minutes
```

Fix: change the parameter, `minutes = 0;`, without `this.`. Java copies it into the field after the block.

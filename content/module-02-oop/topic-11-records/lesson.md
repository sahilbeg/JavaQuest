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

`==` still checks whether two variables point at the same object. `equals` checks the values.

### Records cannot change

Every field in a record is `final`. That means it gets a value once and keeps it forever. A record has no setters.

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

This prints `0`. A faulty dock that reports minus 4 minutes no longer charges a strange fare.

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

## Try it

Change the worked example above. Make the ride a `"cargo"` ride with `-7` minutes. Predict the fare first, then press Run. You should see `1.0`, because the minutes become 0.

## Recap

- A record is a short class for plain data: `record Ride(String bikeId, String type, int minutes) { }`.
- Accessors use the component name: `ride.minutes()`.
- Java writes `equals`, `hashCode` and `toString` from the components.
- Record fields are final, and the record cannot be extended.
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

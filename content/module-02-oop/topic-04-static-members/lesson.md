## Hook

Picture a ticket machine at a bakery. Every customer pulls a ticket with the next number: 41, 42, 43. The machine keeps one counter for the whole shop. If every customer had a private counter, everyone would get ticket number 1.

Spoke has the second problem. Kakashi's crew types bike ids by hand, so two bikes at Uptown both ended up as B-1042. Your ticket is to build a ticket machine for bike ids: one counter, shared by the whole `Bike` class.

## Concept

Every field you wrote so far was an **instance field**. Each object gets its own copy. Two `EBike` objects have two separate `battery` values.

```callout jargon
An **instance** is one object made from a class. `new Bike("classic")` creates one instance of `Bike`.
```

Sometimes you want one value for the whole class. That is what `static` is for.

### Syntax: a static field

```java
class Bike {
    private static int counter = 0;
    private String type;
}
```

- `static` means the field belongs to the **class**, not to any one object.
- There is exactly one `counter`, however many bikes you create.
- Every `Bike` object sees and changes that same one value.
- `type` has no `static`, so each bike still has its own type.

```callout analogy
An instance field is a name tag: every bike wears its own. A static field is the scoreboard on the wall: there is one, and everyone looks at the same board.
```

Watch one shared counter and two bikes:

```animate
title: One counter for the whole class
kind: code
code: |
  public class Main {
      public static void main(String[] args) {
          Bike a = new Bike("classic");
          Bike b = new Bike("ebike");
          System.out.println("Bikes built: " + Bike.created());
      }
  }
  class Bike {
      private static int counter = 0;
      private String type;
      public Bike(String type) {
          this.type = type;
          counter++;
      }
      public static int created() { return counter; }
  }
steps:
  - line: 9
    objects: { cls: { type: "Bike (class)", fields: { counter: 0 } } }
    say: Before any bike exists, Java sets up the Bike class. Its one static counter starts at 0.
  - line: 3
    objects: { b1: { type: Bike, fields: { type: null } } }
    say: new builds the first bike. It gets its own type field, but no counter of its own.
  - line: 12
    objects: { b1: { type: Bike, fields: { type: '"classic"' } } }
    vars: { this: "@b1" }
    say: The constructor runs on this new bike and fills in its own type.
  - line: 13
    objects: { cls: { type: "Bike (class)", fields: { counter: 1 } } }
    say: counter++ changes the class's counter, not a field in the bike. It is now 1.
  - line: 3
    vars: { this: ~, a: "@b1" }
    say: Back in main, a points to the first bike.
  - line: 4
    objects: { b2: { type: Bike, fields: { type: null } } }
    say: The second new builds another bike, with its own type field.
  - line: 12
    objects: { b2: { type: Bike, fields: { type: '"ebike"' } } }
    vars: { this: "@b2" }
    say: The constructor runs on the second bike and sets its type to ebike.
  - line: 13
    objects: { cls: { type: "Bike (class)", fields: { counter: 2 } } }
    say: Then it bumps the same counter. It shows 2, not 1, because both bikes share it.
  - line: 4
    vars: { this: ~, b: "@b2" }
    say: b points to the second bike. Two bikes, one counter.
  - line: 5
    out: "Bikes built: 2"
    say: Bike.created() asks the class, not a bike. It reads the shared counter.
```

### The counter in action

Here is the counter as a program you can run. The constructor adds 1 each time a bike is built.

```java run
public class Main {
    public static void main(String[] args) {
        new Bike("classic");
        new Bike("ebike");
        new Bike("cargo");
        System.out.println("Bikes built: " + Bike.created());
    }
}

class Bike {
    private static int counter = 0;
    private String type;

    public Bike(String type) {
        this.type = type;
        counter++;
    }

    public static int created() {
        return counter;
    }
}
```

It prints `Bikes built: 3`. If `counter` were an instance field, each bike would count only itself, and every bike would hold 1.

### Syntax: a static final constant

```java
class Prices {
    public static final double UNLOCK_FEE = 1.00;
    public static final double CLASSIC = 0.15;
}
```

- `static` gives one shared copy.
- `final` means the value can never change after it is set.
- Together, `static final` makes a **constant**: a fixed value the whole program shares.
- Constant names use CAPITAL_LETTERS with underscores between the words.

A constant that holds a number or a `String` can be `public` safely. Nobody can change it, so there is nothing to protect. That is different from the battery in the last topic. One kind of constant is not safe, though: an array. The next card shows why.

```callout warning
Gojo says: these prices are `double` to keep the first examples short, and the exercise uses them too. Real billing never stores money in a `double`. A `double` holds most decimals only nearly, so `Prices.fare(Prices.CLASSIC, 9)` prints `2.3499999999999996`, not `2.35`. Real fare code counts whole cents in a `long`: `UNLOCK_FEE = 100`, `CLASSIC = 15`. Then 9 minutes is exactly `235` cents. [Module 1's last ticket](/t/module-01-basics/topic-12-stringbuilder-formatting) shows the cents way.
```

### A final array can still change

`final` freezes the variable, not the object it points to. For an `int` or a `String`, that is the same thing. For an array, it is not.

```java run
public class Main {
    public static void main(String[] args) {
        Prices.PER_MINUTE[1] = 0;     // allowed: the array itself is not final
        System.out.println(Prices.PER_MINUTE[1]);
    }
}

class Prices {
    // per-minute prices in cents: CLASSIC, EBIKE, CARGO
    public static final long[] PER_MINUTE = {15, 30, 25};
}
```

It prints `0`. Any code anywhere just made e-bikes free. `final` only stops `Prices.PER_MINUTE = new long[] {0, 0, 0};`. That line fails with `cannot assign a value to static final variable PER_MINUTE`.

```callout analogy
`final` glues the arrow to one box. You cannot point the arrow at a new box. But anyone can still open the box and swap what is inside.
```

The fix: make the array `private`, or share a list nobody can change, such as `List.of(15L, 30L, 25L)` (lists come in module 3). Better still, use one constant per price, as `Prices` does.

### Syntax: a static method

```java
class Prices {
    public static final double UNLOCK_FEE = 1.00;

    public static double fare(double perMinute, int minutes) {
        return UNLOCK_FEE + perMinute * minutes;
    }
}
```

You already know this kind of method. Every method in module 1 was `public static`. A static method belongs to the class, so it needs no object to run. `fare` only uses its parameters and a constant, so it does not need a bike.

### Syntax: calling with the class name

```java nocompile
double price = Prices.fare(Prices.CLASSIC, 10);   // 2.5
int count = Bike.created();
```

- Outside the class, write the class name, a dot, then the static field or method.
- You never write `new Prices()`. There is no object involved.
- Inside the same class, you can drop the class name: `fare(CLASSIC, 10)` works there.

```callout tip
Reading code, a capital letter before the dot is a clue. `Prices.fare(...)` calls a static method on a class. `bike.getId()` calls an instance method on an object.
```

### Static methods and instance methods

An **instance method** has no `static`. It runs on one object, so it can use that object's fields and `this`.

| | Static method | Instance method |
|---|---|---|
| Belongs to | the class | one object |
| Called like | `Bike.created()` | `bike.getId()` |
| Can use `this` | no | yes |
| Can use instance fields | not directly (no `this`) | yes |
| Can use static fields | yes | yes |

### Why a static method cannot use `this`

`this` means "the object this method is running on". A static method runs on no object at all. So there is no `this` to point at.

```java nocompile
class Bike {
    private String type;

    public static String describe() {
        return this.type;   // which bike? there is none
    }
}
```

Java says: `non-static variable this cannot be referenced from a static context`. If a method needs one bike's data, make it an instance method.

The other direction is fine. An instance method can use static fields and call static methods, because the class is always there.

### Syntax: a utility class

```java
final class Fares {
    private Fares() { }   // nobody can make a Fares object

    static final long UNLOCK_CENTS = 100;

    static long cents(long perMinute, int minutes) {
        return UNLOCK_CENTS + perMinute * minutes;
    }
}
```

A class that holds only static methods and constants is a **utility class**. Java's own `Math` is one. Nobody needs a `Fares` object, so the constructor is `private`. Then `new Fares()` fails with `Fares() has private access in Fares`. `final` on the class stops anyone from extending it.

`Fares.cents(30, 10)` returns `400`: 100 cents to unlock plus 10 minutes at 30 cents.

### Syntax: a static factory method

```java nocompile
static Bike ebike(String id) {
    return new Bike(id, "EBIKE");
}
```

A **static factory method** is a static method that builds and returns an object. It is an extra way in, beside the constructor.

```java run
public class Main {
    public static void main(String[] args) {
        Bike b = Bike.ebike("B-1042");
        System.out.println(b.describe());
    }
}

class Bike {
    private final String id;
    private final String type;

    private Bike(String id, String type) {
        this.id = id;
        this.type = type;
    }

    static Bike classic(String id) {
        return new Bike(id, "CLASSIC");
    }

    static Bike ebike(String id) {
        return new Bike(id, "EBIKE");
    }

    String describe() {
        return id + " " + type;
    }
}
```

It prints `B-1042 EBIKE`. The name says what you get, which a constructor cannot do. Java uses this style a lot: `List.of(...)`, `Integer.valueOf(...)`.

### When not to use static

A static field that can change is **global state**. Any code in the program can read it and change it, and you cannot tell who did.

```callout jargon
**Global state** is data that the whole program shares and can change. Bugs in it are hard to find, because any line could be the culprit.
```

Our bike `counter` is global state, and it already shows the cost:

- **Tests leak into each other.** The counter survives from one test to the next. That is why the exercise needs `resetCounter()`. A method that exists only to clean up for tests is a warning sign.
- **Two jobs at once break it.** A big server runs many tasks at the same time. Two of them can run `counter++` together and both hand out `B-1043`. Module 5 shows why and how to fix it.
- **One per program.** If Spoke opens a second city, both cities share one counter.

Real code keeps a counter like this in an object, such as a `BikeRegistry`, and passes that object to whoever needs it. Static is great for constants, utility methods and factory methods. Be wary of static fields that change.

### Classes inside classes

You can write a class inside another class. It is called a **nested class**. There are two kinds, and the difference is `static`.

```java run
public class Main {
    public static void main(String[] args) {
        Station uptown = new Station("Uptown");
        Station.Dock d1 = uptown.new Dock(1);
        Station.Sign sign = new Station.Sign("Welcome");
        System.out.println(d1.label());
        System.out.println(sign.text());
    }
}

class Station {
    private final String name;

    Station(String name) {
        this.name = name;
    }

    class Dock {                  // inner class: needs a Station
        private final int number;
        Dock(int number) { this.number = number; }
        String label() { return name + " dock " + number; }
    }

    static class Sign {           // static nested class: stands alone
        private final String text;
        Sign(String text) { this.text = text; }
        String text() { return text; }
    }
}
```

It prints `Uptown dock 1`, then `Welcome`.

```callout jargon
An **inner class** is a nested class without `static`. Each object keeps a hidden link to one outer object, so `Dock` can read `name`. A **static nested class** has no such link. It is a normal class that lives inside another one's name.
```

- A `Dock` needs a `Station` to exist: `uptown.new Dock(1)`.
- A `Sign` stands alone. If it tried to read `name`, Java would say `non-static variable name cannot be referenced from a static context`.
- The hidden link costs memory and can keep a big outer object alive. Prefer `static` nested classes unless you really need the outer object.

Later lessons put `record Ride` and `enum BikeType` inside `Main`. Records and enums nested in a class are always static, so they need no `Main` object.

## Worked example

This version hands out ids like a ticket machine.

```java run
public class Main {
    public static void main(String[] args) {
        Bike a = new Bike("classic");
        Bike b = new Bike("ebike");
        System.out.println(a.getId() + " " + a.getType());
        System.out.println(b.getId() + " " + b.getType());
        System.out.println("Created: " + Bike.created());
    }
}

class Bike {
    private static int counter = 0;
    private final String id;
    private final String type;

    public Bike(String type) {
        this.type = type;
        this.id = nextId();
    }

    private static String nextId() {
        counter++;
        return "B-" + (1000 + counter);
    }

    public static int created() {
        return counter;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
```

It prints:

```
B-1001 classic
B-1002 ebike
Created: 2
```

1. `new Bike("classic")` stores the type, then calls the static method `nextId()`.
2. `nextId()` raises the shared `counter` from 0 to 1 and returns `"B-1001"`.
3. The second bike raises the same counter to 2, so it gets `"B-1002"`.
4. `Bike.created()` is called with the class name. It returns the shared count, 2.

`nextId()` is `private`, so only the `Bike` class can hand out ids. Nobody can type one by hand any more.

## Try it

First, go back to the worked example. Add a third bike, a cargo bike, and press **Run**. It should get `B-1003`.

Next, run this fare calculator.

```java run
public class Main {
    public static void main(String[] args) {
        System.out.println(Prices.fare(Prices.CLASSIC, 10));
        System.out.println(Prices.fare(Prices.EBIKE, 10));
    }
}

class Prices {
    public static final double UNLOCK_FEE = 1.00;
    public static final double CLASSIC = 0.15;
    public static final double EBIKE = 0.30;

    public static double fare(double perMinute, int minutes) {
        return UNLOCK_FEE + perMinute * minutes;
    }
}
```

Now add a constant `CARGO = 0.25` to `Prices` above and print the fare for a 10 minute cargo ride. You should see `3.5`.

```callout warning
A static counter keeps its value for as long as the program runs. In the exercise, every test builds new bikes, so `resetCounter()` sets the counter back to 0 before each one.
```

## Recap

- A `static` field belongs to the class. All objects share one copy.
- `static final` makes a constant, named in CAPITAL_LETTERS.
- A static method runs without an object. Call it with the class name: `Prices.fare(...)`.
- An instance method runs on one object, so it can use fields and `this`.
- A static method cannot use `this` or instance fields, because there is no object.

## Common mistakes

**Using an instance field inside a static method.**

```java nocompile
class Bike {
    private String type;

    public static String label() {
        return "Bike: " + type;
    }
}
```

Java says: `non-static variable type cannot be referenced from a static context`. Remove `static` from `label`, so it runs on one bike and can read its type.

**Calling an instance method with the class name.**

```java nocompile
String id = Bike.getId();
```

Java says: `non-static method getId() cannot be referenced from a static context`. `getId()` needs one bike. Call it on an object: `bike.getId()`.

**Trying to change a constant.**

```java nocompile
Prices.UNLOCK_FEE = 0.50;
```

Java says: `cannot assign a value to static final variable UNLOCK_FEE`. A constant is fixed for good. If the price really changes, edit the line where the constant is declared.

## Hook

Picture a ticket machine at a bakery. Every customer pulls a ticket with the next number: 41, 42, 43. The machine keeps one counter for the whole shop. If every customer had a private counter, everyone would get ticket number 1.

Spoke has the second problem. Theo's crew types bike ids by hand, so two bikes at Uptown both ended up as B-1042. Your ticket is to build a ticket machine for bike ids: one counter, shared by the whole `Bike` class.

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

A constant can be `public` safely. Nobody can change it, so there is nothing to protect. That is different from the battery in the last topic.

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

Java says: `cannot assign a value to final variable UNLOCK_FEE`. A constant is fixed for good. If the price really changes, edit the line where the constant is declared.

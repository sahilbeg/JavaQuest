## Hook

Think about a school fire drill. The teacher does not shout one order to the tall kids and another to the short kids. She says "everyone walk outside". Each student walks in their own way, but one order covers them all.

Shizune wants the same thing for the fleet. Right now the report has one loop for classic bikes, one for e-bikes and one for cargo bikes. She wants one loop that says "every bike, tell me your fare", and each bike answers with its own price.

## Concept

In the last topic you built `EBike` and `CargoBike` on top of `Bike`. That gives you something new: a `Bike` variable can hold any kind of bike.

```callout jargon
**Polymorphism** means "many shapes". One variable type, such as `Bike`, can hold objects of many shapes: a classic bike, an e-bike or a cargo bike.
```

Every example on this page uses these three classes.

```java
class Bike {
    private String id;

    public Bike(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public double pricePerMinute() {
        return 0.15;
    }

    public double fare(int minutes) {
        return 1.00 + minutes * pricePerMinute();
    }
}

class EBike extends Bike {
    private int battery;

    public EBike(String id, int battery) {
        super(id);
        this.battery = battery;
    }

    public int getBattery() {
        return battery;
    }

    @Override
    public double pricePerMinute() {
        return 0.30;
    }
}

class CargoBike extends Bike {
    public CargoBike(String id) {
        super(id);
    }

    @Override
    public double pricePerMinute() {
        return 0.25;
    }
}
```

### Syntax: a superclass variable holding a subclass object

```java nocompile
Bike b = new EBike("B-2001", 80);
```

The left side says the variable type is `Bike`. The right side makes an `EBike` object. This is allowed because every e-bike is a bike.

The reverse is not allowed. `EBike e = new Bike("B-1042");` does not compile, because a plain bike is not an e-bike.

### Dynamic dispatch: the object's own method runs

```java nocompile
Bike b = new EBike("B-2001", 80);
System.out.println(b.pricePerMinute());   // 0.3
```

The variable says `Bike`, yet Java prints 0.3. When you call a method, Java looks at the real object at run time and runs that object's override. Watch one variable point at two kinds of bike:

```animate
title: The object picks the method
kind: code
code: |
  public class Main {
      public static void main(String[] args) {
          Bike b = new EBike();
          System.out.println(b.pricePerMinute());
          b = new Bike();
          System.out.println(b.pricePerMinute());
      }
  }
  class Bike {
      public double pricePerMinute() { return 0.15; }
  }
  class EBike extends Bike {
      @Override
      public double pricePerMinute() { return 0.30; }
  }
steps:
  - line: 3
    objects: { e1: { type: EBike } }
    vars: { b: "@e1" }
    say: The variable's type is Bike, but it points to an EBike object. That is allowed, because every e-bike is a bike.
  - line: 4
    say: To call pricePerMinute(), Java does not look at the variable's type. It follows the arrow and finds an EBike.
  - line: 14
    say: So EBike's override runs, and it returns 0.30.
  - line: 4
    out: "0.3"
    say: println prints 0.3. The label said Bike, but the object decided.
  - line: 5
    objects: { e1: ~, c1: { type: Bike } }
    vars: { b: "@c1" }
    say: Now b points to a plain Bike. Nothing points at the old e-bike any more, so Java can clean it up later.
  - line: 10
    say: This time the object is a Bike, so Bike's own version runs and returns 0.15.
  - line: 6
    out: "0.15"
    say: The same call gives a different answer. That is dynamic dispatch.
```

```callout jargon
**Dynamic dispatch** means Java picks which version of an overridden method to run while the program runs, based on the real object.
```

```callout analogy
The variable type is the label on a parcel. The object is what is inside. When you press a button on the parcel, the thing inside decides what happens.
```

### The variable type decides what you can call

The variable type still matters for one thing. It decides which methods you are allowed to call.

```java nocompile
Bike b = new EBike("B-2001", 80);
b.getBattery();   // does not compile: Bike has no getBattery()
```

### Syntax: an array of the superclass type

```java nocompile
Bike[] fleet = {
    new Bike("B-1042"),
    new EBike("B-2001", 80),
    new CargoBike("B-3005")
};

for (Bike b : fleet) {
    System.out.println(b.getId() + " " + b.fare(10));
}
```

One array holds all three kinds. One loop asks each bike for its fare. Each bike answers with its own price: 2.5, 4.0 and 3.5.

```animate
title: One loop, three prices
kind: code
code: |
  public class Main {
      public static void main(String[] args) {
          Bike[] fleet = { new Bike(), new EBike(), new CargoBike() };
          for (Bike b : fleet) {
              System.out.println(b.fare(10));
          }
      }
  }
  class Bike {
      public double pricePerMinute() { return 0.15; }
      public double fare(int minutes) { return 1.00 + minutes * pricePerMinute(); }
  }
  class EBike extends Bike { @Override public double pricePerMinute() { return 0.30; } }
  class CargoBike extends Bike { @Override public double pricePerMinute() { return 0.25; } }
steps:
  - line: 3
    objects:
      c1: { type: Bike }
      e1: { type: EBike }
      k1: { type: CargoBike }
      fleet-arr: { type: "Bike[]", fields: { "0": "@c1", "1": "@e1", "2": "@k1" } }
    vars: { fleet: "@fleet-arr" }
    say: One Bike[] array holds three kinds of bike. Each slot points to a different object.
  - line: 4
    vars: { b: "@c1" }
    say: First lap. b points to the classic bike.
  - line: 5
    out: "2.5"
    say: fare(10) asks this object for its price. A plain Bike says 0.15, so 1.00 + 1.50 = 2.5.
  - line: 4
    vars: { b: "@e1" }
    say: Second lap. The same variable b now points to the e-bike.
  - line: 5
    out: "4.0"
    say: Same line, but the object is an EBike. Its override says 0.30, so the fare is 4.0.
  - line: 4
    vars: { b: "@k1" }
    say: Third lap. b points to the cargo bike.
  - line: 5
    out: "3.5"
    say: The cargo bike answers 0.25 a minute, so 3.5. One loop, three prices, and no if to check the kind.
```

### Syntax: instanceof

Sometimes you do need to know the real kind. `instanceof` asks "is this object of that class?" and gives a `boolean`.

```java nocompile
Bike b = new EBike("B-2001", 80);
System.out.println(b instanceof EBike);      // true
System.out.println(b instanceof Bike);       // true, every EBike is a Bike
System.out.println(b instanceof CargoBike);  // false
```

### Syntax: the instanceof pattern

To call `getBattery()`, you need a variable of type `EBike`. The pattern form tests the type and makes that variable in one step.

```java nocompile
if (b instanceof EBike e) {
    System.out.println(e.getBattery());
}
```

Read it as "if b is an EBike, call it e". Inside the braces, `e` is an `EBike` that points to the same object as `b`. Outside the braces, `e` does not exist.

### Syntax: casting

The older way is a **cast**: you write the type in round brackets to tell Java "treat this as an EBike".

```java nocompile
if (b instanceof EBike) {
    EBike e = (EBike) b;
    System.out.println(e.getBattery());
}
```

```callout warning
A cast does not change the object. If `b` really holds a plain `Bike`, the cast crashes the program with a `ClassCastException`. Check with `instanceof` first, or use the pattern form, which does both at once.
```

## Worked example

This program builds a small fleet and reports on it with one loop.

```java run
public class Main {
    public static void main(String[] args) {
        Bike[] fleet = {
            new Bike("B-1042"),
            new EBike("B-2001", 15),
            new CargoBike("B-3005"),
            new EBike("B-2002", 90)
        };

        double total = 0.0;
        int eBikes = 0;
        for (Bike b : fleet) {
            total += b.fare(10);
            if (b instanceof EBike e) {
                eBikes++;
                if (e.getBattery() < 20) {
                    System.out.println(e.getId() + " needs a charge");
                }
            }
        }
        System.out.println("Total: " + total);
        System.out.println("E-bikes: " + eBikes);
    }
}

class Bike {
    private String id;

    public Bike(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public double pricePerMinute() {
        return 0.15;
    }

    public double fare(int minutes) {
        return 1.00 + minutes * pricePerMinute();
    }
}

class EBike extends Bike {
    private int battery;

    public EBike(String id, int battery) {
        super(id);
        this.battery = battery;
    }

    public int getBattery() {
        return battery;
    }

    @Override
    public double pricePerMinute() {
        return 0.30;
    }
}

class CargoBike extends Bike {
    public CargoBike(String id) {
        super(id);
    }

    @Override
    public double pricePerMinute() {
        return 0.25;
    }
}
```

Line by line:

- `Bike[] fleet = { ... }` makes one array that holds four bikes of three kinds.
- `for (Bike b : fleet)` visits each bike. The variable `b` has type `Bike` every time.
- `b.fare(10)` runs `Bike.fare`, which calls `pricePerMinute()`. Dynamic dispatch picks each bike's own price. The total is 2.5 + 4.0 + 3.5 + 4.0 = 14.0.
- `b instanceof EBike e` is true for B-2001 and B-2002 only. Inside the `if`, `e` lets you call `getBattery()`.
- B-2001 has 15 percent, which is below 20, so it prints a warning.

## Try it

Run the program. Then add `new CargoBike("B-3006")` to the array and predict the new line it prints. You should see `B-3006 pays 0.25 per minute`.

```java run
public class Main {
    public static void main(String[] args) {
        Bike[] fleet = { new Bike("B-1042"), new EBike("B-2001"), new CargoBike("B-3005") };
        for (Bike b : fleet) {
            System.out.println(b.getId() + " pays " + b.pricePerMinute() + " per minute");
        }
    }
}

class Bike {
    private String id;

    public Bike(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public double pricePerMinute() {
        return 0.15;
    }
}

class EBike extends Bike {
    public EBike(String id) {
        super(id);
    }

    @Override
    public double pricePerMinute() {
        return 0.30;
    }
}

class CargoBike extends Bike {
    public CargoBike(String id) {
        super(id);
    }

    @Override
    public double pricePerMinute() {
        return 0.25;
    }
}
```

```callout tip
Byte says: reach for `instanceof` only when you need something that one kind of bike has and the others lack, such as a battery. For anything every bike can do, just call the method and let dispatch choose.
```

## Recap

- A `Bike` variable or a `Bike[]` array can hold any subclass object, such as an `EBike` or a `CargoBike`.
- When you call an overridden method, the real object's version runs. This is dynamic dispatch.
- The variable type decides which methods you may call. `Bike` has no `getBattery()`.
- `b instanceof EBike` tests the real kind. `if (b instanceof EBike e)` tests it and gives you an `EBike` variable.
- A cast like `(EBike) b` only works when the object really is an `EBike`.

## Common mistakes

**Calling a subclass-only method through a superclass variable.**

```java nocompile
Bike b = new EBike("B-2001", 80);
System.out.println(b.getBattery());
```

Java says: `cannot find symbol` with `symbol: method getBattery()` and `location: variable b of type Bike`. The compiler only knows `b` is a `Bike`. Use `if (b instanceof EBike e)` and call `e.getBattery()`.

**Storing a parent object in a child variable.**

```java nocompile
EBike e = new Bike("B-1042");
```

Java says: `incompatible types: Bike cannot be converted to EBike`. A plain bike has no battery, so it cannot be an e-bike. Flip it: a `Bike` variable can hold an `EBike`, but never the other way.

**Casting without checking first.**

```java nocompile
Bike b = new CargoBike("B-3005");
EBike e = (EBike) b;
System.out.println(e.getBattery());
```

This compiles, then crashes when it runs. The message starts like this, followed by some details about modules:

```text
Exception in thread "main" java.lang.ClassCastException: class CargoBike cannot be cast to class EBike ...
```

Guard the cast with `if (b instanceof EBike e)`, so the code inside only runs for real e-bikes.

## Hook

Think of the gears on a bike. You can pick gear 1, 2 or 3. You cannot pick gear "tree". The bike only offers the gears that exist.

The kiosk stores bike types as plain text. Text accepts anything, including the typo `"ebkie"`. That typo matched no price, so a rider paid $0. This page shows you how to give Java a fixed list of choices, so a typo stops the build.

## Concept

```callout jargon
An **enum** (short for enumeration) is a type with a fixed list of named values. Each value is called a **constant**. Nothing outside the list can exist.
```

### Syntax: declaring an enum

```java
enum BikeType { CLASSIC, EBIKE, CARGO }
```

- `enum` replaces the word `class`.
- The constants are separated by commas.
- Constants are written in CAPITALS, because they never change.

### Syntax: using a constant

```java nocompile
BikeType type = BikeType.EBIKE;
System.out.println(type);
```

You reach a constant with the enum name, a dot and the constant name. Printing it shows its name: `EBIKE`.

If you misspell it, as in `BikeType.EBKIE`, the program does not compile. The typo from the ticket can no longer reach a rider.

```callout analogy
A String is a blank form where anyone can write anything. An enum is a tick box. You can only tick a box that is printed on the form.
```

### Syntax: values() and valueOf

```java nocompile
BikeType[] all = BikeType.values();
BikeType t = BikeType.valueOf("CARGO");
```

- `values()` returns an array of every constant, in the order you declared them.
- `valueOf("CARGO")` turns text into the matching constant. The text must match exactly, capitals included.

```java run
public class Main {
    enum BikeType { CLASSIC, EBIKE, CARGO }

    public static void main(String[] args) {
        BikeType[] all = BikeType.values();
        for (int i = 0; i < all.length; i++) {
            System.out.println(all[i]);
        }
        System.out.println(BikeType.valueOf("CARGO"));
    }
}
```

### Comparing with ==

Each constant exists exactly once in the whole program. So you can compare enums with `==`.

```java nocompile
if (type == BikeType.EBIKE) {
    System.out.println("Check the battery");
}
```

```callout tip
Strings need `.equals`. Enums can use `==`, because there is only ever one `EBIKE` object. `==` is also safe when the variable is `null`.
```

### Syntax: switch on an enum

```java nocompile
switch (type) {
    case CLASSIC -> System.out.println("Pedal power");
    case EBIKE -> System.out.println("Motor help");
    case CARGO -> System.out.println("Big basket");
}
```

Inside the `case` labels you write only the constant name, without `BikeType.` in front. Watch a loop visit every constant while the switch picks a case:

```animate
title: Three constants, one switch
kind: code
code: |
  public class Main {
      enum BikeType { CLASSIC, EBIKE, CARGO }

      public static void main(String[] args) {
          for (BikeType t : BikeType.values()) {
              switch (t) {
                  case CLASSIC -> System.out.println("Pedal power");
                  case EBIKE -> System.out.println("Motor help");
                  case CARGO -> System.out.println("Big basket");
              }
          }
      }
  }
steps:
  - line: 2
    objects:
      classic: { type: BikeType, fields: { name: '"CLASSIC"' } }
      ebike: { type: BikeType, fields: { name: '"EBIKE"' } }
      cargo: { type: BikeType, fields: { name: '"CARGO"' } }
    say: Java builds exactly three BikeType objects, one per constant. No code can ever make a fourth.
  - line: 5
    vars: { t: "@classic" }
    objects: { all: { type: "BikeType[]", fields: { "0": "@classic", "1": "@ebike", "2": "@cargo" } } }
    say: values() hands back an array of every constant, in declared order. The loop keeps this array out of sight and starts with t pointing at CLASSIC.
  - line: 7
    out: Pedal power
    say: The switch looks at t and jumps straight to case CLASSIC. The other cases are skipped.
  - line: 5
    vars: { t: "@ebike" }
    say: Next round. t now points at the EBIKE object. There is only one EBIKE in the whole program, which is why == works on enums.
  - line: 8
    out: Motor help
    say: This time the switch picks case EBIKE.
  - line: 5
    vars: { t: "@cargo" }
    say: Last round. t points at CARGO.
  - line: 9
    out: Big basket
    say: case CARGO prints. The loop has visited every constant, so it ends.
```

### Syntax: a switch that gives back a value

A switch can also produce a value that you return. An arrow case holds a value or a { } block. It cannot hold a bare return.

```java nocompile
enum Membership {
    BASIC, PLUS, PRO;

    int freeMinutes() {
        return switch (this) {
            case BASIC -> 0;
            case PLUS -> 30;
            case PRO -> 60;
        };
    }
}
```

### A new constant breaks the build, on purpose

A switch expression must give back a value for every possible input. So the compiler checks that you covered every constant. Say someone adds `TANDEM` to `BikeType`, but forgets the price:

```java nocompile
enum BikeType { CLASSIC, EBIKE, CARGO, TANDEM }

static long centsPerMinute(BikeType type) {
    return switch (type) {
        case CLASSIC -> 15;
        case EBIKE -> 30;
        case CARGO -> 25;
    };
}
```

The compiler says:

```text
error: the switch expression does not cover all possible input values
```

That error is a gift. Every switch that forgot tandems now fails before any rider sees a wrong price. Do not add a `default` case just to silence it. A `default` would quietly price tandems like something else.

### Syntax: fields, constructor and methods

An enum can carry data, just like a class. Each constant passes values to a constructor.

```java
enum BikeType {
    CLASSIC(0.15), EBIKE(0.30), CARGO(0.25);

    private final double price;

    BikeType(double price) {
        this.price = price;
    }

    double pricePerMinute() {
        return price;
    }
}
```

- `CLASSIC(0.15)` calls the constructor with `0.15`.
- A semicolon ends the list of constants when more code follows.
- The constructor has no `public`. Java calls it once per constant, and you never write `new BikeType(...)`.
- `pricePerMinute()` is a normal method. Call it on a constant: `BikeType.EBIKE.pricePerMinute()`.

```callout warning
Your exercise stores dollars in a `double` to stay small. Real billing stores whole cents in a `long`, because `double` cannot hold 0.15 exactly: `CLASSIC(15), EBIKE(30), CARGO(25)`. The new examples below use cents.
```

### A different method body per constant

A field works when only a number differs between constants. Sometimes the steps differ too. Then each constant can carry its own body for a method.

```java run
public class Main {
    enum Discount {
        NONE {
            long apply(long cents) { return cents; }
        },
        HALF_PRICE {
            long apply(long cents) { return cents / 2; }
        },
        CAPPED {
            long apply(long cents) { return Math.min(cents, 500); }
        };

        abstract long apply(long cents);
    }

    public static void main(String[] args) {
        long fare = 700;   // a 20 minute e-bike ride: 100 + 20 * 30
        for (Discount d : Discount.values()) {
            System.out.println(d + ": " + d.apply(fare) + " cents");
        }
    }
}
```

It prints `NONE: 700 cents`, `HALF_PRICE: 350 cents` and `CAPPED: 500 cents`.

- The enum declares `abstract long apply(long cents);`, like an abstract class.
- Each constant fills it in with its own braces after its name.
- If you add a constant and forget its body, the code does not compile.

### Enums can implement an interface

An enum cannot `extend` another class. Java already gives every enum a parent. But an enum can `implement` any interface, just like a class or a record.

```java run
public class Main {
    interface Describable {
        String describe();
    }

    enum Membership implements Describable {
        BASIC, PLUS, PRO;

        public String describe() {
            return name() + " member";
        }
    }

    record Station(String name) implements Describable {
        public String describe() {
            return name + " station";
        }
    }

    public static void main(String[] args) {
        Describable[] things = { Membership.PRO, new Station("Harbor") };
        for (Describable d : things) {
            System.out.println(d.describe());
        }
    }
}
```

It prints `PRO member`, then `Harbor station`. Code that takes a `Describable` does not care whether it got an enum constant or a record.

### EnumMap and EnumSet

Often you want one value per constant, such as how many bikes of each type sit at Riverside. Java has two tools built for enums.

```callout jargon
A **map** stores pairs: a key and its value, like a word and its meaning. A **set** stores items with no repeats. Module 3 covers both in depth.
```

```java run
import java.util.EnumMap;
import java.util.EnumSet;

public class Main {
    enum BikeType { CLASSIC, EBIKE, CARGO }

    public static void main(String[] args) {
        EnumMap<BikeType, Integer> docked = new EnumMap<>(BikeType.class);
        docked.put(BikeType.CARGO, 2);
        docked.put(BikeType.CLASSIC, 9);
        System.out.println(docked);
        System.out.println(docked.get(BikeType.CLASSIC));

        EnumSet<BikeType> needCharging = EnumSet.of(BikeType.EBIKE);
        System.out.println(needCharging.contains(BikeType.EBIKE));
        System.out.println(EnumSet.complementOf(needCharging));
    }
}
```

It prints:

```text
{CLASSIC=9, CARGO=2}
9
true
[CLASSIC, CARGO]
```

- `EnumMap<BikeType, Integer>` maps each bike type to a whole number. The angle brackets name the key type and the value type.
- It keeps keys in declared order, so `CLASSIC` prints first even though `CARGO` went in first.
- `EnumSet.of(...)` makes a set of constants. `complementOf` gives every constant not in it.
- Both are very fast, because inside they just use each constant's position.

### Store the name, never the ordinal

Every constant has a position, called its **ordinal**, counted from 0. `CARGO.ordinal()` is 2. It is tempting to save that number in a file or database. Don't.

```java run
public class Main {
    enum BikeType { CLASSIC, EBIKE, CARGO }

    public static void main(String[] args) {
        BikeType t = BikeType.CARGO;
        System.out.println(t.ordinal());
        System.out.println(t.name());
        System.out.println(BikeType.valueOf(t.name()));
    }
}
```

It prints `2`, `CARGO`, then `CARGO`. Now imagine someone adds `TANDEM` between `EBIKE` and `CARGO`. Cargo moves to position 3. Every old ride saved as `2` now loads as a tandem.

The name does not move when the list changes. Save `t.name()`, and read it back with `BikeType.valueOf(...)`.

```callout tip
Never rename a constant that is already saved somewhere, either. A saved `"CARGO"` cannot find a constant called `CARGO_BIKE`.
```

## Worked example

Membership levels make a good enum too. Each level knows its free minutes.

```java run
public class Main {
    enum Membership {
        BASIC(0), PLUS(30), PRO(60);

        private final int free;

        Membership(int free) {
            this.free = free;
        }

        int freeMinutes() {
            return free;
        }

        boolean paysUnlockFee() {
            return this == BASIC;
        }
    }

    public static void main(String[] args) {
        Membership level = Membership.PLUS;
        System.out.println(level.freeMinutes());
        System.out.println(level.paysUnlockFee());
        System.out.println(Membership.BASIC.paysUnlockFee());
    }
}
```

Line by line:

1. `BASIC(0), PLUS(30), PRO(60)` makes three constants, each with its own free minutes.
2. The constructor stores the number in the `free` field.
3. `freeMinutes()` returns that field.
4. `paysUnlockFee()` compares `this` with `BASIC`. `this` means the constant the method was called on.
5. `PLUS` has 30 free minutes and no unlock fee, so it prints `30` then `false`.
6. `BASIC` pays the unlock fee, so the last line prints `true`.

## Try it

In the worked example, change `level` to `Membership.PRO`. Predict the first two lines, then press Run. You should see `60` and `false`.

## Recap

- An enum is a fixed list of constants, like `BikeType.EBIKE`. A misspelled constant does not compile.
- `values()` lists every constant. `valueOf("CARGO")` turns exact text into a constant.
- Compare enums with `==`, and switch on them using bare constant names.
- Enums can have fields, methods, a body per constant, and can implement interfaces.
- Save `name()`, never `ordinal()`. A switch expression must cover every constant.

## Common mistakes

**1. Comparing with text**

```java nocompile
BikeType type = BikeType.EBIKE;
if (type == "EBIKE") {
    System.out.println("e-bike");
}
```

The compiler says:

```text
error: incomparable types: BikeType and String
```

Fix: compare with the constant: `type == BikeType.EBIKE`.

**2. Calling new on an enum**

```java nocompile
BikeType type = new BikeType(0.15);
```

The compiler says:

```text
error: enum classes may not be instantiated
```

Fix: use an existing constant, such as `BikeType.CLASSIC`.

**3. Wrong capitals in valueOf**

```java nocompile
BikeType type = BikeType.valueOf("ebike");
```

This compiles, but the program crashes when it runs:

```text
java.lang.IllegalArgumentException: No enum constant BikeType.ebike
```

Fix: the text must match the constant exactly: `BikeType.valueOf("EBIKE")`.

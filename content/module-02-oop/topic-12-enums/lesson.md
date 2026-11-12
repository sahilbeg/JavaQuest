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

Inside the `case` labels you write only the constant name, without `BikeType.` in front.

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

- An enum is a type with a fixed list of constants, like `BikeType.EBIKE`.
- A misspelled constant is a compile error, so typos never reach a rider.
- `values()` lists every constant. `valueOf("CARGO")` turns exact text into a constant.
- Compare enums with `==`, and switch on them using bare constant names.
- Enums can have fields, a constructor and methods, such as `pricePerMinute()`.

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

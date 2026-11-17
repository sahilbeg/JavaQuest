## Hook

Think about a wall socket. The socket does not care what you plug in. A lamp, a kettle and a phone charger all fit, because they share one plug shape.

Shizune's ticket asks for the same thing for promotions. Marketing invents a new deal every week. Today, each deal means an engineer edits the fare code. You want one "plug shape" for a promotion, so the fare code can accept any deal that fits it.

## Concept

### What a functional interface is

In the lambdas topic, you passed lambdas to methods. A lambda always needs a target type. That target type is a **functional interface**.

```callout jargon
A **functional interface** is an interface with exactly one abstract method. An **abstract method** is a method with no body, only a name, inputs and a return type.
```

Because there is only one abstract method, Java knows which method a lambda fills in. The lambda becomes the body of that one method.

### Syntax: writing your own functional interface

```java
interface Promotion {
    double apply(String type, double fare);
}
```

- This is a plain interface, like the ones from module 2.
- It has one abstract method, `apply`, so it is a functional interface.
- Any lambda that takes two inputs and returns a `double` fits it.

```java nocompile
Promotion halfPrice = (type, fare) -> fare / 2;
double price = halfPrice.apply("classic", 4.0);   // 2.0
```

You call the lambda through the interface method name. Here that name is `apply`.

### Syntax: @FunctionalInterface

```java
@FunctionalInterface
interface Promotion {
    double apply(String type, double fare);
}
```

`@FunctionalInterface` is an **annotation**. An annotation is a note that starts with `@` and tells the compiler something about the code below it.

This note says "this interface must have exactly one abstract method". If someone adds a second abstract method later, the compiler stops with an error. The interface works without the note, but the note guards it.

```callout tip
A functional interface may still have `default` and `static` methods. Only the abstract methods count, and there must be exactly one.
```

### The built-in functional interfaces

Java ships a set of ready functional interfaces in the package `java.util.function`. You import them like any other class. These six cover most jobs:

| Interface | Method | Takes | Gives back | Example job |
|---|---|---|---|---|
| `Predicate<T>` | `test` | one T | `boolean` | Is this ride long? |
| `Function<T, R>` | `apply` | one T | one R | Ride to label text |
| `Consumer<T>` | `accept` | one T | nothing | Print a ride |
| `Supplier<T>` | `get` | nothing | one T | Make a default station name |
| `UnaryOperator<T>` | `apply` | one T | the same type T | Round a fare |
| `BiFunction<T, U, R>` | `apply` | one T and one U | one R | Minutes and price to fare |

The letters `T`, `U` and `R` are type parameters from the generics topic. `R` usually stands for "result".

### Syntax: Predicate

```java nocompile
Predicate<Integer> isLong = minutes -> minutes >= 30;
boolean answer = isLong.test(45);   // true
```

### Syntax: Function

```java nocompile
Function<String, Integer> nameLength = name -> name.length();
int size = nameLength.apply("Harbor");   // 6
```

### Syntax: Consumer

```java nocompile
Consumer<String> shout = station -> System.out.println(station.toUpperCase());
shout.accept("Uptown");   // prints UPTOWN
```

### Syntax: Supplier

```java nocompile
Supplier<String> homeStation = () -> "Riverside";
String start = homeStation.get();   // "Riverside"
```

### Syntax: UnaryOperator

```java nocompile
UnaryOperator<Double> addUnlockFee = fare -> fare + 1.00;
double total = addUnlockFee.apply(3.0);   // 4.0
```

### Syntax: BiFunction

```java nocompile
BiFunction<Integer, Double, Double> fare = (minutes, price) -> 1.00 + minutes * price;
double cost = fare.apply(10, 0.15);   // 2.5
```

Here is a run snippet that uses four of them together:

```java run
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Main {
    public static void main(String[] args) {
        Predicate<Integer> isLong = minutes -> minutes >= 30;
        Function<Integer, Double> classicFare = minutes -> 1.00 + minutes * 0.15;
        Consumer<String> show = text -> System.out.println("Kiosk: " + text);
        Supplier<String> station = () -> "Old Mill";

        show.accept(station.get());
        System.out.println(isLong.test(45));
        System.out.println(classicFare.apply(10));
    }
}
```

### Syntax: combining with and, or, negate

`Predicate` has `default` methods that build a new predicate out of two old ones.

```java nocompile
Predicate<Integer> isLong = minutes -> minutes >= 30;
Predicate<Integer> isHuge = minutes -> minutes >= 120;

Predicate<Integer> between = isLong.and(isHuge.negate());   // 30 to 119
Predicate<Integer> extreme = isHuge.or(minutes -> minutes < 2);
```

- `a.and(b)` is true when both are true.
- `a.or(b)` is true when at least one is true.
- `a.negate()` flips true and false.

### Syntax: chaining with andThen

`Function` has `andThen`. It feeds the result of the first function into the second one.

```java nocompile
Function<Integer, Double> fare = minutes -> 1.00 + minutes * 0.15;
Function<Double, String> money = amount -> String.format("$%.2f", amount);

Function<Integer, String> receipt = fare.andThen(money);
receipt.apply(10);   // "$2.50"
```

```callout analogy
`andThen` is a relay race. The first runner finishes and hands the baton to the second runner.
```

```java run
import java.util.function.Function;
import java.util.function.Predicate;

public class Main {
    public static void main(String[] args) {
        Predicate<Integer> isLong = minutes -> minutes >= 30;
        Predicate<Integer> isHuge = minutes -> minutes >= 120;
        Predicate<Integer> normalLong = isLong.and(isHuge.negate());

        System.out.println(normalLong.test(45));
        System.out.println(normalLong.test(150));
        System.out.println(isLong.negate().test(10));

        Function<Integer, Double> fare = minutes -> 1.00 + minutes * 0.15;
        Function<Double, String> money = amount -> String.format("$%.2f", amount);
        System.out.println(fare.andThen(money).apply(10));
    }
}
```

## Worked example

This program builds a pluggable promotion, close to what the ticket needs.

```java run
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Promotion halfPrice = (type, fare) -> fare / 2;
        Promotion eBikeDeal = (type, fare) -> {
            if (type.equals("ebike")) {
                return fare * 80 / 100;
            }
            return fare;
        };

        List<Promotion> thisWeek = List.of(eBikeDeal, halfPrice);
        double fare = 7.00;
        for (Promotion promotion : thisWeek) {
            fare = promotion.apply("ebike", fare);
        }
        System.out.println(fare);
    }
}

@FunctionalInterface
interface Promotion {
    double apply(String type, double fare);
}
```

1. At the bottom, `@FunctionalInterface` marks `Promotion` as a one-method interface.
2. `halfPrice` is a lambda that fills in `apply`. It returns half the fare.
3. `eBikeDeal` uses a block lambda with braces, because it needs an `if`.
4. `thisWeek` is a list of promotions. Marketing could swap it every week.
5. The loop feeds each result into the next promotion. 7.00 becomes 5.60, then 2.80.
6. The program prints `2.8`.

The fare loop never changes. A new deal is one more lambda in the list.

## Try it

Add a third promotion called `flatOff` that takes 0.50 off any fare. Put it at the end of `thisWeek` in the worked example, then press **Run**. You should see `2.3`.

Then try a `Predicate<Integer>` of your own. Make it true for rides under 5 minutes, and print `.negate().test(3)`.

## Recap

- A functional interface has exactly one abstract method. A lambda fills in that method.
- `@FunctionalInterface` makes the compiler check the "exactly one" rule.
- `java.util.function` gives you `Predicate`, `Function`, `Consumer`, `Supplier`, `UnaryOperator` and `BiFunction`.
- `and`, `or` and `negate` combine predicates. `andThen` chains functions.
- You can write your own functional interface when the built-in ones do not fit your words.

## Common mistakes

**Two abstract methods under @FunctionalInterface.**

```java nocompile
@FunctionalInterface
interface Promotion {
    double apply(String type, double fare);
    String name();
}
```

Java says: `Unexpected @FunctionalInterface annotation ... Promotion is not a functional interface ... multiple non-overriding abstract methods found`. Keep one abstract method. Turn the other into a `default` method or move it elsewhere.

**Calling the wrong method name.**

```java nocompile
Predicate<Integer> isLong = minutes -> minutes >= 30;
boolean answer = isLong.apply(45);
```

Java says: `cannot find symbol ... method apply(int)`. Each interface names its own method. A `Predicate` uses `test`, a `Consumer` uses `accept` and a `Supplier` uses `get`.

**A lambda with the wrong number of inputs.**

```java nocompile
BiFunction<Integer, Double, Double> fare = minutes -> 1.00 + minutes * 0.15;
```

Java says: `incompatible types: incompatible parameter types in lambda expression`. A `BiFunction` takes two inputs, so the lambda needs two names: `(minutes, price) -> 1.00 + minutes * price`.

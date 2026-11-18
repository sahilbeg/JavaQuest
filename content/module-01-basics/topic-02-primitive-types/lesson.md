## Hook

Think of a parcel locker. A small slot fits a letter. A big slot fits a box. If you push a box into the letter slot, something gets squashed.

That is what happened to Itachi's rider. Her ride was 2.5 km long. The kiosk stored it in a box made for whole numbers, so the .5 got squashed off. The receipt said 2 km. This page shows you the right box for each kind of number.

## Concept

In the last topic you met `int` and `String`. Java has a small set of built in types for simple values. They are called **primitive types**.

```callout jargon
A **primitive type** is one of Java's basic building blocks. Its name is all lowercase, like `int` or `double`. `String` starts with a capital letter because it is a class, so it is not a primitive.
```

Java has exactly eight of them. Each one is a box of a fixed size, measured in **bits**, the 0s and 1s from [How computers work](/t/module-00-foundations/topic-01-how-computers-work). More bits means a bigger range.

| Type | Holds | Size | Range | Example |
|---|---|---|---|---|
| `byte` | a tiny whole number | 8 bits | -128 to 127 | `byte level = 3;` |
| `short` | a small whole number | 16 bits | -32,768 to 32,767 | `short year = 2026;` |
| `int` | a whole number | 32 bits | about -2.1 to 2.1 billion | `int docks = 12;` |
| `long` | a very big whole number | 64 bits | about -9.2 to 9.2 billion billion | `long totalRides = 3000000000L;` |
| `float` | a decimal, less exact | 32 bits | about 7 correct digits | `float speed = 18.5f;` |
| `double` | a decimal | 64 bits | about 15 correct digits | `double km = 2.5;` |
| `boolean` | `true` or `false` | not fixed | `true`, `false` | `boolean isMember = true;` |
| `char` | a single character | 16 bits | 65,536 different characters | `char size = 'M';` |

You will use five of them every day: `int`, `long`, `double`, `boolean` and `char`. `byte`, `short` and `float` save memory in special cases, such as raw file data or huge tables of numbers.

### Syntax: double

```java
double distanceKm = 2.5;
```

A `double` holds numbers with a decimal point. Use it for distances, speeds and averages.

```java
double fare = 4.0;
double perMinute = 0.15;
double eBikeRate = 0.30;
```

Java prints a whole-number double with `.0`, so `double fare = 4;` prints as `4.0`.

```callout warning
A double stores most decimals as a very close guess, so `0.1 + 0.2` gives `0.30000000000000004`. That is fine for a distance. Real billing never uses double for money. It counts whole cents in a `long`, so 15 cents is exactly `15`. These first tickets use double prices to keep things simple. Topic 12 shows the cents way. Real Spoke billing uses cents. A few later exercises keep doubles to stay small, and their lessons point it out.
```

### Syntax: boolean

```java
boolean isMember = true;
```

A `boolean` holds only `true` or `false`. There are no quotes around them. Use it for yes or no questions.

```java
boolean isMember = false;
boolean dockIsFree = true;
```

### Syntax: char

```java
char bikeType = 'E';
```

A `char` holds exactly one character. It uses single quotes. A String uses double quotes and can hold many characters.

```java
char first = 'B';
String bikeId = "B-1042";
```

### Syntax: long

```java
long totalRides = 3000000000L;
```

An `int` stops at about 2.1 billion. A `long` holds much bigger whole numbers. Put an `L` at the end of a big number so Java reads it as a long.

### Syntax: literals have types

```java
long big = 3000000000L;
float speed = 18.5f;
```

```callout jargon
A **literal** is a value typed straight into the code, like `12`, `2.5`, `'E'` or `"Harbor"`.
```

Every literal already has a type, before it goes into any box:

- A whole number like `12` is an `int`. Add `L` to make it a long: `12L`.
- A number with a point like `3.0` is a `double`. Add `f` to make it a float: `3.0f`.
- `'E'` is a `char`, and `"E"` is a String.

That is why `long big = 3000000000;` fails with `integer number too large`. Java reads `3000000000` as an int first, and it does not fit. In the same way, `float speed = 18.5;` fails with `possible lossy conversion from double to float`.

```callout tip
Write a capital `L`, never a small `l`. In many fonts `10l` looks like the number 101.
```

### Syntax: var

```java
var minutes = 20;
```

With `var`, Java works out the type from the value on the right. Here `20` is an int literal, so `minutes` is an int box, forever. It is still a typed box. You just let Java write the type for you.

```java
var station = "Harbor";   // a String
var km = 2.5;             // a double
var total = 20L;          // a long
```

- `var` works only for variables inside a method, and only with a starting value. `var total;` fails with `cannot infer type for local variable total`.
- The value decides the type. `var price = 4;` makes an int, not a double.

This course mostly writes the type out, so you can always see it.

### Past the top: overflow

An int box has 32 bits, so its biggest value is 2,147,483,647. Java names it `Integer.MAX_VALUE`. What happens if you add one more?

```java run
public class Main {
    public static void main(String[] args) {
        int top = Integer.MAX_VALUE;
        System.out.println(top);
        System.out.println(top + 1);
    }
}
```

It prints `2147483647`, then `-2147483648`. There is no error and no warning. The number wraps around to the most negative int.

```callout jargon
**Overflow** happens when a result is too big for its box. Java's whole-number types wrap around silently, like a car's distance counter rolling from 999999 back to 000000.
```

Silent is the dangerous part. An int counting cents tops out near $21 million. A busy company passes that quickly, and the total suddenly turns negative. That is why money totals use `long`.

### Syntax: Math.addExact

```java nocompile
int sum = Math.addExact(a, b);
```

`Math.addExact` adds like `+`, but it refuses to wrap. If the answer does not fit, the program stops with an error instead.

```java run crash
public class Main {
    public static void main(String[] args) {
        int top = Integer.MAX_VALUE;
        int next = Math.addExact(top, 1);
        System.out.println(next);
    }
}
```

The program stops with `java.lang.ArithmeticException: integer overflow`. A loud crash sounds bad, but it beats a fare that is silently wrong. `Math.subtractExact` and `Math.multiplyExact` do the same for `-` and `*`.

### Why 2.5 does not fit in an int

An int box only fits whole numbers. Java refuses to squash 2.5 in by itself:

```java nocompile
int distance = 2.5;
```

Java says: `incompatible types: possible lossy conversion from double to int`. The word **lossy** means some of the value would be lost.

### Syntax: widening

```java
double km = 7;
```

Going the other way is safe. Every int fits in a double box exactly. Java converts it for you, and `km` holds `7.0`. This automatic move to a bigger type is called **widening**.

```java
int minutes = 20;
double exact = minutes;   // exact holds 20.0
long big = minutes;       // an int fits in a long too
```

One small catch: a double keeps only about 15 to 16 digits. A huge long, past 9 million billion, can lose its last digit when it widens into a double.

### Too late to widen

Widening happens only when the value moves into the box. The sum on the right is worked out first, using the types it has. If every number there is an int, the sum is done in int, and it can overflow before it ever reaches the long box.

```animate
title: The sum overflows before it reaches the long
kind: code
code: |
  int days = 30;
  long ms = days * 24 * 60 * 60 * 1000;
  long fixed = days * 24 * 60 * 60 * 1000L;
  System.out.println(ms);
  System.out.println(fixed);
steps:
  - line: 1
    vars: { days: 30 }
    say: A monthly bike pass lasts 30 days. We want that time in milliseconds, thousandths of a second.
  - line: 2
    say: Every number on the right is an int, so Java multiplies in int. The true answer is 2,592,000,000, past the int limit.
  - line: 2
    vars: { ms: -1702967296 }
    say: The int sum wraps around to a negative number. Only then is it widened into the long box. Too late.
  - line: 3
    say: Here the last number is 1000L, a long. Multiplying by a long makes the answer a long.
  - line: 3
    vars: { fixed: 2592000000 }
    say: A long has room for 2,592,000,000, so the answer is right.
  - line: 4
    out: "-1702967296"
    say: The broken version prints a negative time. No error, just a wrong number.
  - line: 5
    out: "2592000000"
    say: The fixed version is correct. Once a long joins in, every step after it is done in long. Put the L first to be safe.
```

```callout tip
When a sum could get big, put the `L` on the first number: `30L * 24 * 60 * 60 * 1000`. Then every step is done in long.
```

### Syntax: casting

```java
int whole = (int) 2.5;
```

Sometimes you really do want the whole number part. A **cast** tells Java "convert this value to this type, I know I may lose something". You write the type in brackets before the value.

```java
int a = (int) 2.5;   // 2
int b = (int) 2.9;   // 2, the .9 is cut off
double c = (double) 5;  // 5.0
```

```callout warning
A cast to int cuts off the decimal part. It never rounds up, so `(int) 2.9` gives `2`. It always cuts toward zero, so `(int) -2.9` gives `-2`.
```

Watch a ride distance move between a double box and an int box:

```animate
title: Squashing 2.9 km into an int
kind: code
code: |
  double km = 2.9;
  int wholeKm = (int) km;
  double back = wholeKm;
  System.out.println(km);
  System.out.println(wholeKm);
  System.out.println(back);
steps:
  - line: 1
    vars: { km: 2.9 }
    say: A double box holds the ride distance, 2.9 km. A double keeps the decimal part.
  - line: 2
    vars: { wholeKm: 2 }
    say: The cast (int) cuts off the .9 and puts 2 in an int box. It never rounds up to 3.
  - line: 3
    vars: { back: '2.0' }
    say: Widening needs no cast. 2 goes into a double box as 2.0, but the lost .9 does not come back.
  - line: 4
    out: "2.9"
    say: The km box was never changed. A cast makes a new value and leaves the old box alone.
  - line: 5
    out: "2"
    say: The int box prints a whole number, with no decimal point.
  - line: 6
    out: "2.0"
    say: The double box prints 2.0. Java always shows a double with a decimal point.
```

### Rounding instead of cutting

A cast cuts. When you want proper rounding, use Java's `Math` tools:

```java
long a = Math.round(2.5);     // 3, rounds to the nearest whole number
long b = Math.round(-2.5);    // -2, a half always rounds up, toward +
double c = Math.floor(2.7);   // 2.0, always down
double d = Math.ceil(2.1);    // 3.0, always up
double e = Math.floor(-2.9);  // -3.0, down means toward the more negative side
int f = (int) -2.9;           // -2, a cast cuts toward zero
```

Two traps hide here:

- `Math.round` of a double gives back a **long**, not an int. So `int r = Math.round(2.7);` fails with `possible lossy conversion from long to int`. Write `int r = (int) Math.round(2.7);`.
- `Math.floor` and `Math.ceil` give back a double, such as `2.0`. Cast it if you need an int.

### Syntax: Math.max, min and abs

```java
int bigger = Math.max(3, 7);    // 7
int smaller = Math.min(3, 7);   // 3
int size = Math.abs(-5);        // 5, the distance from zero
```

`Math.max` and `Math.min` pick the larger or smaller of two values. `Math.abs` drops the minus sign.

```callout warning
`Math.abs(Integer.MIN_VALUE)` is still `-2147483648`. The int range goes one further below zero than above it, so +2147483648 does not fit and wraps back around.
```

### The integer division surprise

The four maths signs `+ - * /` work on numbers. Topic 03 covers them in detail. One rule matters right now. When you divide an int by an int, Java gives back an int and throws away the remainder.

```java
int a = 5 / 2;        // 2
double b = 5 / 2;     // 2.0, the .5 was already lost
double c = 5.0 / 2;   // 2.5
```

Watch Java share 7 minutes between 2 rides, three different ways:

```animate
title: Three ways to divide 7 by 2
kind: code
code: |
  int minutes = 7;
  int rides = 2;
  int a = minutes / rides;
  double b = minutes / rides;
  double c = (double) minutes / rides;
  System.out.println(a + " " + b + " " + c);
steps:
  - line: 1
    vars: { minutes: 7 }
    say: An int box holds 7 minutes.
  - line: 2
    vars: { rides: 2 }
    say: Another int box holds 2 rides. We want the average minutes per ride.
  - line: 3
    vars: { a: 3 }
    say: An int divided by an int gives an int. 7 / 2 is really 3.5, but Java drops the .5, so a holds 3.
  - line: 4
    say: Here Java divides first, still with two ints. The answer is 3 again, even though the box is a double.
  - line: 4
    vars: { b: '3.0' }
    say: Only then does Java widen 3 into the double box, as 3.0. The .5 was already lost.
  - line: 5
    vars: { c: 3.5 }
    say: The cast turns 7 into 7.0 before dividing. A double divided by an int gives a double, so c holds 3.5.
  - line: 6
    out: 3 3.0 3.5
    say: Same numbers, three answers. Cast one side to double before you divide to keep the decimals.
```

### Fixing it with a cast

Make one side a double before dividing:

```java
int total = 45;
int rides = 2;
double average = (double) total / rides;   // 22.5
```

The cast turns `total` into `45.0` first. A double divided by an int gives a double, so the .5 survives.

### Syntax: a method with inputs

```java nocompile
public static double averageMinutes(int totalMinutes, int rides) {
    return (double) totalMinutes / rides;
}
```

Your exercise methods take inputs, like `averageMinutes(int totalMinutes, int rides)`. The values inside the round brackets are called **parameters**. Treat each one as a variable that already holds a value. The word `return` hands the answer back to whoever called the method.

You can call a method and pass it a value: `averageMinutes(45, 2)` runs averageMinutes with 45 and 2 as its inputs. One method can also call another, so `main` can call `averageMinutes(45, 2)` and print what comes back.

## Worked example

This program stores the rider's real ride and prints it:

```java run
public class Main {
    public static void main(String[] args) {
        double distanceKm = 2.5;
        boolean isMember = true;
        char bikeType = 'E';
        int roundedKm = (int) distanceKm;
        System.out.println(distanceKm);
        System.out.println(isMember);
        System.out.println(bikeType);
        System.out.println(roundedKm);
    }
}
```

1. Line 3 stores 2.5 in a double, so nothing is lost.
2. Line 4 stores `true` in a boolean.
3. Line 5 stores the single character E in a char.
4. Line 6 casts the distance to an int, which cuts it down to 2.
5. The program prints `2.5`, `true`, `E` and `2`, each on its own line.

Now see integer division side by side with the fix:

```java run
public class Main {
    public static void main(String[] args) {
        int totalMinutes = 45;
        int rides = 2;
        double wrong = totalMinutes / rides;
        double right = (double) totalMinutes / rides;
        System.out.println(wrong);
        System.out.println(right);
    }
}
```

The first line prints `22.0`. The second prints `22.5`.

## Try it

Change `totalMinutes` to `61` and `rides` to `4`. Predict both answers before you press **Run**.

```java run
public class Main {
    public static void main(String[] args) {
        int totalMinutes = 7;
        int rides = 2;
        System.out.println(totalMinutes / rides);
        System.out.println((double) totalMinutes / rides);
    }
}
```

## Recap

- Use `int` for whole numbers, `double` for decimals, `boolean` for true or false, `char` for one character, and `long` for very big whole numbers.
- Java widens a smaller type into a bigger one for you, like an int into a double.
- A cast such as `(int) 2.9` cuts toward zero. `Math.round` rounds properly.
- An int divided by an int gives an int: `5 / 2` is `2`. Cast first to keep the decimals: `(double) 5 / 2` is `2.5`.
- An int past about 2.1 billion overflows and wraps around silently. Use `long`, or `Math.addExact` to fail loudly.

## Common mistakes

**Storing a decimal in an int.**

```java nocompile
int distanceKm = 2.5;
```

Java says: `incompatible types: possible lossy conversion from double to int`. Use a double box: `double distanceKm = 2.5;`

**Casting after the division.**

```java
int total = 45;
int rides = 2;
double average = (double) (total / rides);
System.out.println(average);
```

This compiles, but it prints `22.0`. The brackets make the int division happen first. Cast the first number on its own: `(double) total / rides` prints `22.5`.

**Double quotes around a char.**

```java nocompile
char bikeType = "E";
```

Java says: `incompatible types: String cannot be converted to char`. A char uses single quotes: `char bikeType = 'E';`

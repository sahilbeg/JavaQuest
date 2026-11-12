## Hook

Think about a calculator on a phone. You type numbers and symbols like `+` and `*`, and it works out the answer. Java has the same symbols built in.

Priya's ticket is about money. A rider at Riverside wants to see the price before unlocking a bike. A classic ride costs $1.00 to unlock plus $0.15 for every minute. The kiosk has to do that sum on its own. In this lesson you teach it how.

## Concept

An **operator** is a symbol that does something to values. `+` is an operator. An **expression** is any piece of code that works out to a value. `3 + 4` is an expression, and its value is `7`.

```callout jargon
An **operator** is a symbol such as `+` or `*`. An **expression** is code that produces one value.
```

### Syntax: arithmetic operators

```java
int sum = 7 + 3;         // 10
int difference = 7 - 3;  // 4
int product = 7 * 3;     // 21
int quotient = 7 / 3;    // 2
int remainder = 7 % 3;   // 1
```

- `+` adds and `-` subtracts.
- `*` multiplies. Java uses a star because a keyboard has no × key.
- `/` divides.
- `%` gives the **remainder** after dividing. People call it "modulo" or "mod".

Here are a few more short examples:

```java
int docks = 12;
int bikesOut = 5;
int freeDocks = docks - bikesOut;   // 7
double perMinute = 0.15;
double tenMinutes = 10 * perMinute; // 1.5
```

### Syntax: dividing whole numbers

When both sides of `/` are `int`, Java gives back an `int`. It throws away anything after the decimal point. This is called **integer division**.

```java
int a = 75 / 60;       // 1, the .25 is dropped
double b = 75 / 60.0;  // 1.25, because 60.0 is a double
```

```callout warning
`7 / 2` is `3` in Java. If you want `3.5`, make one side a double: `7 / 2.0`.
```

### Syntax: remainder with %

`%` answers the question "what is left over?". It pairs nicely with integer division.

```java
int minutes = 75;
int hours = minutes / 60;  // 1 whole hour
int extra = minutes % 60;  // 15 minutes left over
```

```callout analogy
Picture 75 bikes and racks that hold 60 each. `75 / 60` is how many racks you fill. `75 % 60` is how many bikes are left on the pavement.
```

### Syntax: precedence and brackets

**Precedence** means which operator Java does first. `*`, `/` and `%` happen before `+` and `-`. Operators of the same level run from left to right.

```java
int x = 2 + 3 * 4;     // 14, because 3 * 4 happens first
int y = (2 + 3) * 4;   // 20, brackets happen first
int z = 10 - 4 - 3;    // 3, left to right
```

Brackets `( )` always win. Use them whenever an expression could confuse a reader.

### Syntax: compound assignment

A **compound assignment** changes a variable using its own value. It is a shortcut.

```java
double total = 1.00;
total += 0.15;   // same as total = total + 0.15;
total -= 0.15;   // same as total = total - 0.15;
total *= 2;      // same as total = total * 2;
```

### Syntax: ++ and --

`++` adds one to a variable. `--` takes one away. Programmers use these for counting.

```java
int ridesToday = 0;
ridesToday++;   // now 1
ridesToday++;   // now 2
ridesToday--;   // now 1
```

### Syntax: joining text with +

When one side of `+` is a String, Java joins the two values into one String. This is called **concatenation**.

```java
String station = "Riverside";
String label = "Station: " + station;  // "Station: Riverside"
String fare = "Fare: " + 2.5;          // "Fare: 2.5"
```

Java reads `+` from left to right. Once it has made a String, every later `+` joins text.

```java
System.out.println("Total: " + 1 + 2);    // Total: 12
System.out.println("Total: " + (1 + 2));  // Total: 3
```

## Worked example

This program works out a classic fare for a 10 minute ride:

```java run
public class Main {
    public static void main(String[] args) {
        int minutes = 10;
        double fare = 1.00 + minutes * 0.15;
        System.out.println("Ride: " + minutes + " min");
        System.out.println("Fare: $" + fare);
    }
}
```

1. `minutes` holds `10`.
2. Java does `minutes * 0.15` first, because `*` comes before `+`. That gives `1.5`.
3. Then Java adds the unlock fee: `1.00 + 1.5` is `2.5`.
4. The first `println` joins three pieces into `Ride: 10 min`.
5. The second `println` prints `Fare: $2.5`.

Here is the hours and minutes trick in action:

```java run
public class Main {
    public static void main(String[] args) {
        int minutes = 75;
        int hours = minutes / 60;
        int left = minutes % 60;
        System.out.println(hours + " h " + left + " min");
    }
}
```

It prints `1 h 15 min`. Notice that `hours + " h "` starts with an int. That still works, because the right side is a String.

## Try it

Change `minutes` to `30`, then to `90`, and press **Run** each time. Then swap `0.15` for `0.30` to price an e-bike.

```java run
public class Main {
    public static void main(String[] args) {
        int minutes = 20;
        double fare = 1.00;
        fare += minutes * 0.15;
        int rides = 0;
        rides++;
        System.out.println("Rides so far: " + rides);
        System.out.println("Fare: $" + fare);
    }
}
```

```callout tip
If a number looks wrong, add brackets to show the order you mean. The answer often fixes itself.
```

## Recap

- `+`, `-`, `*` and `/` do maths. `%` gives the remainder.
- Dividing two ints drops the decimal part.
- `*`, `/` and `%` run before `+` and `-`. Brackets run first of all.
- `+=` and `-=` update a variable. `++` adds one and `--` takes one away.
- `+` with a String joins text together.

## Common mistakes

**Expecting decimals from int division.**

```java
int minutes = 90;
double hours = minutes / 60;
System.out.println(hours);
```

You see `1.0` instead of `1.5`. Both sides of `/` are ints, so Java drops the `.5` before storing it. Fix it with a double on one side: `minutes / 60.0`.

**Forgetting precedence.**

```java
int minutes = 10;
double fare = (1.00 + minutes) * 0.15;
System.out.println(fare);
```

You see `1.65` instead of `2.5`. The brackets make Java add first. Remove them: `1.00 + minutes * 0.15`.

**Adding numbers inside a String by accident.**

```java
System.out.println("Docks: " + 10 + 2);
```

You see `Docks: 102` instead of `Docks: 12`. After the first `+`, Java is joining text. Put the sum in brackets: `"Docks: " + (10 + 2)`.

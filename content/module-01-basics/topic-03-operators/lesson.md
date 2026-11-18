## Hook

Think about a calculator on a phone. You type numbers and symbols like `+` and `*`, and it works out the answer. Java has the same symbols built in.

Jiraiya's ticket is about money. A rider at Riverside wants to see the price before unlocking a bike. A classic ride costs $1.00 to unlock plus $0.15 for every minute. The kiosk has to do that sum on its own. In this lesson you teach it how.

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
- `%` gives the **remainder** after dividing. People often call it "mod", but it is not quite maths' modulo. The two differ for negative numbers, as you will see below.

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

Watch `/` and `%` split a long ride into hours and minutes:

```animate
title: Splitting 130 minutes
kind: code
code: |
  int minutes = 130;
  int hours = minutes / 60;
  int left = minutes % 60;
  System.out.println(hours + " h " + left + " min");
steps:
  - line: 1
    vars: { minutes: 130 }
    say: A rider kept bike B-1042 out for 130 minutes. The receipt should show hours and minutes.
  - line: 2
    vars: { hours: 2 }
    say: 60 fits into 130 two whole times. Integer division keeps the 2 and drops the rest.
  - line: 3
    vars: { left: 10 }
    say: "% asks what is left over. Two hours use 120 minutes, so 10 minutes remain."
  - line: 4
    out: 2 h 10 min
    say: The receipt joins both boxes into one line of text.
```

### Edge cases: dividing by zero

What is the average ride length when there were zero rides? With ints, Java refuses to answer:

```java run crash
public class Main {
    public static void main(String[] args) {
        int totalMinutes = 120;
        int rides = 0;
        System.out.println(totalMinutes / rides);
    }
}
```

The program stops with `java.lang.ArithmeticException: / by zero`. The same happens with `%` and an int 0.

Doubles behave differently. They never crash. They give back special values instead:

```java run
public class Main {
    public static void main(String[] args) {
        System.out.println(7.0 / 0);
        System.out.println(-7.0 / 0);
        double nothing = 0.0 / 0;
        System.out.println(nothing);
        System.out.println(nothing == nothing);
        System.out.println(Double.isNaN(nothing));
    }
}
```

It prints `Infinity`, `-Infinity`, `NaN`, `false` and `true`. NaN is never equal to anything, not even itself. So test for it with `Double.isNaN(x)`, never with `==`.

```callout jargon
**NaN** stands for "Not a Number". It is the double you get from a sum with no sensible answer, such as 0.0 / 0.
```

In production, the crash is the friendly case: you see it at once. A NaN slips through quietly, and a rider's receipt shows "Average: NaN min". Check for zero before you divide. Topic 05 shows how.

### Edge cases: negatives and %

In Java, the answer of `%` takes the sign of the number on the left:

```java
System.out.println(7 % 3);                  // 1
System.out.println(-7 % 3);                 // -1
System.out.println(Math.floorMod(-7, 3));   // 2
```

That surprises people who test for odd numbers with `n % 2 == 1`. For `n = -3`, `n % 2` is `-1`, so the test says "not odd". Write `n % 2 != 0` instead. It works for every int.

`Math.floorMod` gives the maths-class answer, which is never negative when the right side is positive. It is handy for things that wrap around, like a clock. Thirty minutes before midnight is `Math.floorMod(-30, 1440)`, which is `1410`, or 23:30. Plain `-30 % 1440` would give `-30`.

### Syntax: precedence and brackets

**Precedence** means which operator Java does first. `*`, `/` and `%` happen before `+` and `-`. Operators of the same level run from left to right.

```java
int x = 2 + 3 * 4;     // 14, because 3 * 4 happens first
int y = (2 + 3) * 4;   // 20, brackets happen first
int z = 10 - 4 - 3;    // 3, left to right
```

Brackets `( )` always win. Use them whenever an expression could confuse a reader.

Watch Java price a 10 minute ride, then get it wrong with brackets in the wrong place:

```animate
title: Which sum happens first?
kind: code
code: |
  int minutes = 10;
  double fare = 1.00 + minutes * 0.15;
  System.out.println("Fare: $" + fare);
  double wrong = (1.00 + minutes) * 0.15;
  System.out.println("Oops: $" + wrong);
steps:
  - line: 1
    vars: { minutes: 10 }
    say: The ride lasted 10 minutes.
  - line: 2
    say: "* comes before +, so Java works out minutes * 0.15 first. That is 1.5, the cost of the minutes."
  - line: 2
    vars: { fare: 2.5 }
    say: Then Java adds the 1.00 unlock fee. fare holds 2.5.
  - line: 3
    out: "Fare: $2.5"
    say: The kiosk shows the right fare.
  - line: 4
    say: Brackets always win, so this time Java adds first. 1.00 + 10 makes 11.0.
  - line: 4
    vars: { wrong: 1.65 }
    say: Then 11.0 * 0.15 gives 1.65. Every number is right, but the order is wrong.
  - line: 5
    out: "Oops: $1.65"
    say: The rider would pay too little. Check where your brackets go.
```

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

Watch `++` and `+=` work together as the fare meter ticks, in cents:

```animate
title: The fare meter ticks
kind: code
code: |
  int fareCents = 100;
  int minutes = 0;
  minutes++;
  fareCents += 15;
  minutes++;
  fareCents += 15;
  System.out.println(minutes + " min: " + fareCents + " cents");
steps:
  - line: 1
    vars: { fareCents: 100 }
    say: Unlocking a classic bike costs 100 cents, so the meter starts at 100.
  - line: 2
    vars: { minutes: 0 }
    say: No minutes have passed yet.
  - line: 3
    vars: { minutes: 1 }
    say: One minute passes. minutes++ adds one to the box, so 0 becomes 1.
  - line: 4
    vars: { fareCents: 115 }
    say: fareCents += 15 means fareCents = fareCents + 15. The box now holds 115.
  - line: 5
    vars: { minutes: 2 }
    say: Another minute. ++ adds one again, so minutes is 2.
  - line: 6
    vars: { fareCents: 130 }
    say: Another 15 cents. Each += reads the box, adds, and stores the answer back in the same box.
  - line: 7
    out: "2 min: 130 cents"
    say: After two minutes the ride costs 130 cents.
```

### Prefix and postfix ++

`++` can go after the name (`count++`, **postfix**) or before it (`++count`, **prefix**). On a line of its own, both add one. The difference shows when you use the value in the same line:

- `count++` hands out the old value, then adds one.
- `++count` adds one first, then hands out the new value.

```animate
title: count++ versus ++count
kind: code
code: |
  int count = 5;
  int a = count++;
  int b = ++count;
  System.out.println(a + " " + b + " " + count);
steps:
  - line: 1
    vars: { count: 5 }
    say: Five bikes are docked at Harbor.
  - line: 2
    vars: { a: 5 }
    say: count++ hands out the old value first. a gets 5.
  - line: 2
    vars: { count: 6 }
    say: Then the ++ happens, and count becomes 6.
  - line: 3
    vars: { count: 7 }
    say: ++count adds one first. count becomes 7.
  - line: 3
    vars: { b: 7 }
    say: Then it hands out the new value. b gets 7.
  - line: 4
    out: "5 7 7"
    say: Both lines added one to count. They handed out different values.
```

Later, with arrays, you may see `rides[count++] = minutes;`. It stores a value at slot `count`, then moves `count` on to the next slot. Anywhere else, keep `++` on its own line, so nobody has to think about the order.

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

### Bitwise operators

Every int is stored as 32 bits, the 0s and 1s from [How computers work](/t/module-00-foundations/topic-01-how-computers-work). **Bitwise operators** work on those bits one column at a time. Here are 5 and 3, showing only their last four bits:

```
5      = 0101
3      = 0011
5 & 3  = 0001  = 1    1 where both have a 1
5 | 3  = 0111  = 7    1 where either has a 1
5 ^ 3  = 0110  = 6    1 where they differ
5 << 1 = 1010  = 10   every bit moves one place left
```

- `~` flips every bit, so `~5` is `-6`.
- `<<` shifts left, which doubles the number for each place. `>>` shifts right, which halves it: `20 >> 2` is `5`.
- `>>>` also shifts right, but it fills the left side with 0s, even for negative numbers. `-8 >> 1` is `-4`, but `-8 >>> 28` is `15`.
- `Integer.toBinaryString(5)` shows the bits as text: `101`.

You will rarely write these in everyday code. You will read them inside libraries, for example where Java's HashMap mixes the bits of a hash code with `^` and `>>>`. They also pack many yes or no flags into one int.

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

Java does `minutes * 0.15` first, because `*` comes before `+`, then adds the unlock fee: `1.00 + 1.5` is `2.5`. The two lines print `Ride: 10 min` and `Fare: $2.5`.

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

- `+`, `-`, `*` and `/` do maths. `%` gives the remainder, which takes the sign of the left number.
- Dividing two ints drops the decimal part. Dividing an int by 0 crashes, and a double gives `Infinity` or `NaN`.
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

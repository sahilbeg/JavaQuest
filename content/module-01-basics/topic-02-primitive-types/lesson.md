## Hook

Think of a parcel locker. A small slot fits a letter. A big slot fits a box. If you push a box into the letter slot, something gets squashed.

That is what happened to Sam's rider. Her ride was 2.5 km long. The kiosk stored it in a box made for whole numbers, so the .5 got squashed off. The receipt said 2 km. This page shows you the right box for each kind of number.

## Concept

In the last topic you met `int` and `String`. Java has a small set of built in types for simple values. They are called **primitive types**.

```callout jargon
A **primitive type** is one of Java's basic building blocks. Its name is all lowercase, like `int` or `double`. `String` starts with a capital letter because it is a class, so it is not a primitive.
```

Here are the five you need now:

| Type | Holds | Example |
|---|---|---|
| `int` | a whole number | `int docks = 12;` |
| `long` | a very big whole number | `long totalRides = 3000000000L;` |
| `double` | a number with a decimal point | `double km = 2.5;` |
| `boolean` | `true` or `false` | `boolean isMember = true;` |
| `char` | a single character | `char size = 'M';` |

### Syntax: double

```java
double distanceKm = 2.5;
```

A `double` holds numbers with a decimal point. Use it for distances, prices and averages.

```java
double fare = 4.0;
double perMinute = 0.15;
double eBikeRate = 0.30;
```

Java prints a whole-number double with `.0`, so `double fare = 4;` prints as `4.0`.

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

Going the other way is safe. Every whole number fits in a double box. Java converts it for you, and `km` holds `7.0`. This automatic move to a bigger type is called **widening**.

```java
int minutes = 20;
double exact = minutes;   // exact holds 20.0
long big = minutes;       // an int fits in a long too
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
A cast to int cuts off the decimal part. It never rounds up, so `(int) 2.9` gives `2`.
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
- A cast such as `(int) 2.9` forces a conversion and cuts off the decimal part.
- An int divided by an int gives an int: `5 / 2` is `2`.
- Cast before you divide to keep the decimals: `(double) 5 / 2` is `2.5`.

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

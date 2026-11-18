## Hook

Think about a recipe book. One page explains how to make pizza dough. The pizza recipe just says "make the dough (see page 12)". It does not repeat every step.

The fare code at Spoke has no pages like that. Everything sits in one giant `main`. Your ticket is to cut it into small, named pieces. Each piece does one job, and you can test each one on its own.

## Concept

You already wrote one small method in module 0: `greeting()`, which gave back some text. This page shows the full picture.

```callout jargon
A **method** is a named block of code that does one job. You **call** a method when you use its name to run it.
```

### Syntax: defining a method

```java
public static double unlockFee() {
    return 1.00;
}
```

- `public static` stays the same for now. Later modules explain both words.
- `double` is the **return type**: the kind of value the method gives back.
- `unlockFee` is the method name, written in camelCase.
- The round brackets `()` hold the inputs. This method has none.
- `return 1.00;` hands the value back and ends the method.

### Syntax: calling a method

```java nocompile
double fee = unlockFee();
System.out.println(fee);
```

The call `unlockFee()` runs the method. The value it returns lands where the call was written. Here it goes into `fee`. Watch Java jump into the method and come back:

```animate
title: main asks for the unlock fee
kind: code
code: |
  public static double unlockFee() {
      return 1.00;
  }

  public static void main(String[] args) {
      double fee = unlockFee();
      System.out.println(fee);
  }
steps:
  - line: 5
    say: Every program starts in main. The method unlockFee sits above it, waiting to be called.
  - line: 6
    say: main calls unlockFee(). Java pauses main at this line and jumps into the method.
  - line: 2
    say: return 1.00 hands the value back and ends the method.
  - line: 6
    vars: { fee: '1.0' }
    say: Back in main, 1.0 lands where the call was written, so it goes into fee.
  - line: 7
    out: "1.0"
    say: main carries on with the next line and prints fee.
```

### Syntax: parameters

```java
public static double ridePrice(int minutes) {
    return minutes * 0.15;
}
```

```callout jargon
A **parameter** is a variable in the method's brackets. It receives a value each time someone calls the method. The value you pass in is called an **argument**.
```

```java nocompile
double a = ridePrice(10);   // minutes is 10, a is 1.5
double b = ridePrice(20);   // minutes is 20, b is 3.0
```

### Syntax: several parameters

A method can take several parameters. Separate them with commas, and give each one its own type.

```java
public static double ridePrice(int minutes, double rate) {
    return minutes * rate;
}
```

```java nocompile
double c = ridePrice(10, 0.30);   // 3.0
```

The arguments match the parameters by position. The first argument goes into the first parameter.

### Syntax: return types

The return type can be any type you know: `int`, `double`, `boolean`, `String`.

```java
public static boolean isLongRide(int minutes) {
    return minutes > 60;
}

public static String bikeName() {
    return "B-1042";
}
```

### Syntax: void

Some methods do a job and give nothing back. Their return type is `void`, which means "nothing".

```java
public static void printReceipt(double amount) {
    System.out.println("Total: $" + amount);
}
```

A `void` method needs no `return` line. You call it as a statement on its own line.

```java nocompile
printReceipt(2.5);
```

### Syntax: calling methods from methods

A method can call other methods. This is how you build big jobs out of small ones.

```java nocompile
public static double fare(int minutes) {
    return unlockFee() + ridePrice(minutes, 0.15);
}
```

`fare` asks `unlockFee` and `ridePrice` for their answers, then combines them.

```mermaid
flowchart LR
  M["main"] --> F["fare(10)"]
  F --> U["unlockFee()"]
  F --> R["ridePrice(10, 0.15)"]
  U -->|"1.0"| F
  R -->|"1.5"| F
  F -->|"2.5"| M
```

### Syntax: local variable scope

```java
public static double fare(int minutes) {
    double rate = 0.15;      // rate lives only inside fare
    return 1.00 + minutes * rate;
}
```

```callout jargon
A **local variable** is a variable created inside a method. Its **scope** is the part of the code where it exists: from its line to the closing brace of its block.
```

Parameters are local variables too. Watch the boxes of `fare` appear when it is called, and disappear when it returns:

```animate
title: fare's boxes come and go
kind: code
code: |
  public static double fare(int minutes) {
      double rate = 0.15;
      return 1.00 + minutes * rate;
  }

  public static void main(String[] args) {
      double total = fare(10);
      System.out.println("Total: $" + total);
  }
steps:
  - line: 7
    say: main calls fare(10). Java jumps into fare and main waits.
  - line: 1
    vars: { minutes: 10 }
    say: The parameter minutes is a new box inside fare. It receives the argument 10.
  - line: 2
    vars: { rate: 0.15 }
    say: fare makes a local box, rate. Only fare can see it.
  - line: 3
    vars: { minutes: null, rate: null }
    say: 1.00 + 10 * 0.15 is 2.5. return hands 2.5 back. fare has ended, so minutes and rate disappear.
  - line: 7
    vars: { total: 2.5 }
    say: Back in main, 2.5 lands in total. main never saw minutes or rate.
  - line: 8
    out: "Total: $2.5"
    say: main prints the fare.
```

When the method ends, its local variables disappear. Another method cannot see them.

```callout analogy
Each method is a room with its own whiteboard. What you write on one whiteboard stays in that room. To share a value, pass it in as an argument or give it back with return.
```

### Scope inside loops and ifs

A variable made inside a loop or an `if` block lives only inside that block.

```java
for (int i = 0; i < 3; i++) {
    int doubled = i * 2;      // lives only inside the loop
}
// i and doubled do not exist here
```

### Java passes a copy

When you call a method, Java copies each argument into the parameter box. The method works on its own copy. So changing a parameter never changes the caller's variable. This rule is called **pass-by-value**.

```animate
title: The method changes only its copy
kind: code
code: |
  public static void addTip(int fareCents) {
      fareCents = fareCents + 50;
      System.out.println("Inside: " + fareCents);
  }

  public static void main(String[] args) {
      int fare = 250;
      addTip(fare);
      System.out.println("Outside: " + fare);
  }
steps:
  - line: 7
    vars: { fare: 250 }
    say: main stores a fare of 250 cents.
  - line: 8
    say: main calls addTip(fare). Java reads the 250 out of fare and passes that number along.
  - line: 1
    vars: { fareCents: 250 }
    say: addTip gets a brand new box, fareCents, holding a copy of 250. It has no link back to fare.
  - line: 2
    vars: { fareCents: 300 }
    say: addTip adds a 50 cent tip to its own box. fare in main is not touched.
  - line: 3
    out: "Inside: 300"
    say: Inside the method, the copy holds 300.
  - line: 9
    vars: { fareCents: null }
    out: "Outside: 250"
    say: addTip has ended and its copy is gone. main's fare still holds 250.
```

To change the caller's value, give the new value back with `return`, and let the caller store it:

```java nocompile
public static int withTip(int fareCents) {
    return fareCents + 50;
}

fare = withTip(fare);   // fare now holds 300
```

Arrays and other objects are passed the same way, but there is a twist. The copy is a copy of the arrow, not of the object. Topic 10 shows what that means in "A method gets a copy of the arrow".

### Syntax: method overloading

Two methods may share a name if their parameter lists differ. This is called **overloading**.

```java
public static double fare(int minutes) {
    return 1.00 + minutes * 0.15;
}

public static double fare(int minutes, double rate) {
    return 1.00 + minutes * rate;
}
```

Java picks the version whose parameters match the arguments. `fare(10)` runs the first one. `fare(10, 0.30)` runs the second.

## Worked example

This program splits a classic bike fare into small methods.

```java run
public class Main {
    public static double unlockFee() {
        return 1.00;
    }

    public static double fare(int minutes) {
        double rate = 0.15;
        return unlockFee() + minutes * rate;
    }

    public static void printReceipt(int minutes) {
        System.out.println(minutes + " min: $" + fare(minutes));
    }

    public static void main(String[] args) {
        printReceipt(10);
        printReceipt(20);
    }
}
```

1. Java starts in `main` and calls `printReceipt(10)`. Inside it, `minutes` is 10.
2. `printReceipt` calls `fare(10)`.
3. `fare` creates the local variable `rate` and calls `unlockFee()`, which returns `1.0`.
4. `fare` returns `1.0 + 10 * 0.15`, which is `2.5`.
5. `printReceipt` prints `10 min: $2.5`. The second call does the same with 20 and prints `20 min: $4.0`.

## Try it

This program has a member rule in its own method. Change the minutes in `main` to 25, then to 45, and press **Run** each time.

```java run
public class Main {
    public static double memberFare(int minutes) {
        if (minutes > 30) {
            return (minutes - 30) * 0.15;
        }
        return 0;
    }

    public static void main(String[] args) {
        System.out.println(memberFare(40));
    }
}
```

Now add an overloaded `memberFare(int minutes, double rate)` that uses the rate you pass in. Call it with `0.30` for an e-bike.

```callout tip
Give each method one job and a name that says what it gives back, like `unlockFee` or `pricePerMinute`. Small methods are easy to test.
```

## Recap

- A method is a named block of code. You define it once and call it many times.
- Parameters receive the arguments you pass in, matched by position.
- The return type says what comes back. `void` means nothing comes back.
- Local variables exist only inside the block where you create them. A parameter is a copy, so changing it never changes the caller's variable.
- Overloaded methods share a name but take different parameters.

## Common mistakes

**Forgetting to return a value.**

```java nocompile
public static double unlockFee() {
    double fee = 1.00;
}
```

Java says: `missing return statement`. A method with a `double` return type must end with `return`. Add `return fee;` before the closing brace.

**Using a local variable outside its method.**

```java nocompile
public static double fare(int minutes) {
    double rate = 0.15;
    return minutes * rate;
}

public static void main(String[] args) {
    System.out.println(rate);
}
```

Java says: `cannot find symbol` with `symbol: variable rate`. `rate` lives only inside `fare`. Call the method instead: `System.out.println(fare(10));`

**Passing arguments in the wrong order or of the wrong type.**

```java nocompile
public static double ridePrice(int minutes, double rate) {
    return minutes * rate;
}

public static void main(String[] args) {
    System.out.println(ridePrice(0.15, 10));
}
```

Java says: `incompatible types: possible lossy conversion from double to int`. The first parameter is the `int` minutes. Swap the arguments: `ridePrice(10, 0.15)`.

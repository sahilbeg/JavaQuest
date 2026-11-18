## Hook

Think about a ticket gate at a train station. You tap your card. The gate checks one thing: is the card valid? If yes, the gate opens. If no, it beeps and stays shut.

The Spoke kiosk needs to make choices like that gate. Right now it charges every rider the same way. Jiraiya's ticket says members ride free for their first 30 minutes. So the kiosk must ask a question first: is this rider a member? Then it picks a price based on the answer.

## Concept

A program that makes choices needs two things. It needs a question with a yes or no answer. It also needs a way to run different code for each answer.

### Syntax: boolean values

```java
boolean member = true;
boolean late = false;
```

A `boolean` holds one of two values: `true` or `false`. There is nothing in between.

```callout jargon
A **boolean expression** is any piece of code that works out to `true` or `false`. The name comes from George Boole, a mathematician who studied yes or no logic.
```

### Syntax: comparison operators

```java
int minutes = 40;
boolean longRide = minutes > 30;   // true
```

A comparison operator compares two values and gives back a boolean.

| Operator | Meaning | Example | Result |
|---|---|---|---|
| `==` | equal to | `minutes == 40` | `true` |
| `!=` | not equal to | `minutes != 40` | `false` |
| `>` | greater than | `minutes > 30` | `true` |
| `<` | less than | `minutes < 30` | `false` |
| `>=` | greater than or equal to | `minutes >= 40` | `true` |
| `<=` | less than or equal to | `minutes <= 30` | `false` |

```callout warning
`=` and `==` are different. One `=` puts a value into a variable. Two `==` asks "are these equal?"
```

### Syntax: if

```java
boolean condition = true;
if (condition) {
    // runs only when condition is true
}
```

The condition goes inside round brackets. The code inside the curly braces runs only when the condition is `true`.

```java
int minutes = 45;
if (minutes > 30) {
    System.out.println("Long ride!");
}
```

### Syntax: if and else

```java
boolean condition = true;
if (condition) {
    // runs when condition is true
} else {
    // runs when condition is false
}
```

`else` gives the program a second path. Exactly one of the two blocks runs.

```java
boolean member = false;
if (member) {
    System.out.println("Welcome back, member!");
} else {
    System.out.println("Unlock fee: $1.00");
}
```

You can write `if (member)` directly. `member` is already a boolean, so `== true` is not needed.

Watch Java pick one path. Line 3 never lights up, because its branch is skipped:

```animate
title: One path, never both
kind: code
code: |
  boolean member = false;
  if (member) {
      System.out.println("Welcome back, member!");
  } else {
      System.out.println("Unlock fee: $1.00");
  }
  System.out.println("Enjoy the ride!");
steps:
  - line: 1
    vars: { member: false }
    say: The kiosk stores false, because this rider is not a member.
  - line: 2
    say: Java checks the condition. member is false, so the whole if block is skipped.
  - line: 4
    say: A false condition sends Java to the else block instead.
  - line: 5
    out: "Unlock fee: $1.00"
    say: Only this branch runs. Exactly one of the two blocks runs, never both and never neither.
  - line: 7
    out: Enjoy the ride!
    say: After the if-else, Java carries on with the next line, whichever branch ran.
```

### Syntax: else if

```java
boolean firstCheck = false;
boolean secondCheck = true;
if (firstCheck) {
    // runs when firstCheck is true
} else if (secondCheck) {
    // runs when firstCheck is false and secondCheck is true
} else {
    // runs when no condition above was true
}
```

Java checks each condition from top to bottom. It runs the first block whose condition is `true`, then skips the rest.

```java
int minutes = 20;
if (minutes < 15) {
    System.out.println("short");
} else if (minutes < 60) {
    System.out.println("normal");
} else {
    System.out.println("long");
}
```

This prints `normal`. The first check fails, the second one passes, and Java never looks at the `else`.

### The first true condition wins

In this chain, two conditions are true for a 45 minute ride. Watch which one Java picks:

```animate
title: The first match wins
kind: code
code: |
  int minutes = 45;
  if (minutes < 15) {
      System.out.println("short");
  } else if (minutes < 60) {
      System.out.println("normal");
  } else if (minutes < 120) {
      System.out.println("long");
  } else {
      System.out.println("all day");
  }
  System.out.println("Ride logged");
steps:
  - line: 1
    vars: { minutes: 45 }
    say: Bike B-1042 comes back after 45 minutes.
  - line: 2
    say: Is 45 less than 15? No, that is false, so Java skips the short block.
  - line: 4
    say: Is 45 less than 60? Yes, the first true condition in the chain.
  - line: 5
    out: normal
    say: Java runs this block, and only this block.
  - line: 11
    out: Ride logged
    say: Java jumps past the rest of the chain. 45 < 120 is true too, but it is never checked.
```

### Syntax: logical operators

```java
boolean a = true;
boolean b = false;
boolean both = a && b;     // AND: true only when both are true
boolean either = a || b;   // OR: true when at least one is true
boolean flipped = !a;      // NOT: flips true to false and false to true
```

Logical operators join booleans together.

```java
boolean member = true;
int minutes = 25;

boolean free = member && minutes <= 30;     // true
boolean payFee = !member;                    // false
boolean busy = minutes > 60 || minutes < 2;  // false
```

```callout analogy
`&&` is a door with two locks. You need both keys. `||` is a house with two doors. Either one lets you in.
```

### && and || stop early

Java reads `&&` and `||` from left to right, and it stops as soon as it knows the answer. This is called **short-circuit** evaluation.

- `a && b`: if `a` is false, the whole thing is false. Java never looks at `b`.
- `a || b`: if `a` is true, the whole thing is true. Java never looks at `b`.

That lets the left side act as a **guard** that protects the right side. Say a rider typed nothing, so the code is `null`.

```callout jargon
**null** means "no object here". A String variable can hold null instead of an arrow to some text. Calling a method on null crashes with a `NullPointerException`.
```

```animate
title: The guard on the left
kind: code
code: |
  String code = null;
  if (code != null && code.startsWith("B")) {
      System.out.println("Bike code");
  } else {
      System.out.println("No code typed");
  }
steps:
  - line: 1
    vars: { code: "null" }
    say: The rider pressed Enter without typing a code. code holds null, not text.
  - line: 2
    say: Java checks the left side first. code != null is false.
  - line: 2
    say: false && anything is false, so Java skips startsWith completely. Calling it on null would have crashed.
  - line: 4
    say: The condition was false, so the else block runs.
  - line: 5
    out: No code typed
    say: The kiosk answers politely instead of crashing. Always put the safety check on the left.
```

A single `&` also means AND, but it always checks both sides. Swap `&&` for `&` and the guard stops working:

```java run crash
public class Main {
    public static void main(String[] args) {
        String code = null;
        System.out.println(code != null & code.startsWith("B"));
    }
}
```

The program crashes with a `java.lang.NullPointerException`. Use `&&` and `||` for conditions. The same trick protects other risky steps. `rides != 0 && total / rides > 30` never divides by zero, because the division runs only when `rides` is not 0.

### Syntax: the ternary operator

```java nocompile
type name = condition ? valueIfTrue : valueIfFalse;
```

The **ternary operator** is a tiny if-else that gives back a value. It reads as "is this true? then this one, otherwise that one".

```java
int minutes = 45;
String size = minutes > 30 ? "long ride" : "short ride";   // "long ride"
boolean member = true;
System.out.println("Unlock fee: " + (member ? 0 : 100) + " cents");
```

Use it for a short choice between two values. When either side needs more than one step, write a normal if-else. It is easier to read.

### Syntax: comparing Strings

```java
String station = "Harbor";
if (station.equals("Harbor")) {
    System.out.println("Ferry nearby!");
}
```

To check whether two Strings hold the same text, use `.equals(...)`. Do not use `==` for Strings. With text, `==` can say `false` even when the letters match. `.equals` always compares the letters.

### Methods with inputs

Methods like `fare(boolean member, int minutes)` take parameters, which topic 02 explains in "Syntax: a method with inputs".

## Worked example

This program works out a fare for a member and for a non-member.

```java run
public class Main {
    public static void main(String[] args) {
        boolean member = true;
        int minutes = 42;

        if (!member) {
            System.out.println(1.00 + minutes * 0.15);
        } else if (minutes <= 30) {
            System.out.println(0.0);
        } else {
            System.out.println((minutes - 30) * 0.15);
        }
    }
}
```

1. `member` is `true`, so `!member` is `false`. Java skips the first block.
2. `minutes <= 30` asks "is 42 at most 30?" The answer is `false`, so Java skips the second block.
3. No condition matched, so the `else` block runs.
4. The rider pays for 12 extra minutes: `12 * 0.15`, which prints `1.7999999999999998`. That is 1.80 with a tiny rounding error.

A `double` stores most decimals as a very close guess, so tiny rounding digits like these appear.

```callout warning
That is fine for a distance, but a rider must never be charged 1.7999999999999998. Real billing never uses double for money. It counts whole cents in a `long`, so 12 minutes at 15 cents is exactly `180`. Topic 12 shows how. Real Spoke billing uses cents. A few later exercises keep doubles to stay small, and their lessons point it out.
```

## Try it

Change `member` to `false` and `minutes` to `20`, then press **Run**. Which block runs now?

```java run
public class Main {
    public static void main(String[] args) {
        boolean member = true;
        int minutes = 20;

        if (member && minutes <= 30) {
            System.out.println("Free ride!");
        } else {
            System.out.println("This ride costs money.");
        }

        String station = "Uptown";
        if (station.equals("Uptown") || station.equals("Harbor")) {
            System.out.println("Busy station");
        }
    }
}
```

Next, change `station` to `"Old Mill"` and run it again.

## Recap

- A `boolean` is `true` or `false`, and comparisons like `minutes > 30` produce one.
- `if` runs code only when its condition is `true`, and `else` runs when it is `false`.
- `else if` checks more conditions in order, and the first match wins.
- `&&` means both, `||` means either, and `!` flips a boolean. `&&` and `||` stop as soon as they know the answer, so put guards like `code != null` on the left.
- Compare Strings with `.equals(...)`, never with `==`.

## Common mistakes

**Using `=` instead of `==`.**

```java nocompile
int minutes = 30;
if (minutes = 30) {
    System.out.println("Half an hour");
}
```

Java says: `incompatible types: int cannot be converted to boolean`. One `=` stores a value. Write `minutes == 30` to compare.

**A semicolon right after the condition.**

```java
int minutes = 5;
if (minutes > 30); {
    System.out.println("Long ride!");
}
```

This compiles, but it prints `Long ride!` even for 5 minutes. The `;` ends the `if` with an empty step, so the braces always run. Remove the semicolon after `)`.

**Chaining two comparisons like in maths.**

```java nocompile
int minutes = 20;
if (15 <= minutes < 60) {
    System.out.println("normal");
}
```

Java says: `bad operand types for binary operator '<'`. Java works out `15 <= minutes` first, which gives a boolean, then tries `boolean < 60`. Join two comparisons with `&&`: `minutes >= 15 && minutes < 60`.

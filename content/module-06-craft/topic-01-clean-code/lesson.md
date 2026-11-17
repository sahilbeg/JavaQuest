## Hook

Imagine a recipe that says: "Take t of the x, add 3 of the y, then do k." The food might taste fine. But nobody can change the recipe without breaking it.

Spoke's billing code is that recipe. Tsunade's ticket says a new hire spent two days reading `calc(int t, int m, int k)`. They changed one price and still got it wrong. The code worked. It just could not be read.

This module is called "Paying off debt". **Technical debt** is the extra cost you pay later for code that was quick to write but hard to change. In this topic you learn the first tools for paying it off: good names, small methods and honest comments.

## Concept

```callout jargon
**Clean code** is code that another person can read, understand and change safely. The computer does not care about clean code. Your teammates, and you in six months, care a lot.
```

### Names that say what they mean

An **intention-revealing name** tells the reader what a thing holds or does, without a comment. Compare these two lines.

```java nocompile
long r = m * 15;
long fareCents = minutes * CLASSIC_CENTS_PER_MINUTE;
```

Both compute the same number. Only the second one tells you it is a fare, in cents, for minutes on a classic bike.

Some quick rules for names:

- Variables and fields are nouns: `minutes`, `freeMinutes`, `riderName`.
- Methods are verbs or questions: `chargeRider()`, `isDocked()`, `fareCents()`.
- Put the unit in the name when it matters: `priceCents`, `timeoutSeconds`.
- Skip single letters, except a loop counter like `i`.

```java
int d = 12;
int docksPerKiosk = 12;
```

The second line needs no comment. The name is the comment.

### Magic numbers

A **magic number** is a number in the code with no name. Nobody knows why `25` is there, or which other `25`s must change with it.

### Syntax: a named constant

```java
static final long UNLOCK_FEE_CENTS = 100;
```

- `static` means one copy for the whole class.
- `final` means the value never changes.
- The name is in UPPER_SNAKE_CASE. That is the Java habit for constants.

```java run
public class Main {
    static final long UNLOCK_FEE_CENTS = 100;
    static final long CLASSIC_CENTS_PER_MINUTE = 15;

    public static void main(String[] args) {
        int minutes = 10;
        long fareCents = UNLOCK_FEE_CENTS + minutes * CLASSIC_CENTS_PER_MINUTE;
        System.out.println("Fare: " + fareCents + " cents");
    }
}
```

### Syntax: an enum with a field

When a number belongs to one of a few kinds, put it on an enum. You met enums with fields in module 2. Each constant passes its value to the enum constructor.

```java
enum BikeType {
    CLASSIC(15), EBIKE(30), CARGO(25);

    private final long centsPerMinute;

    BikeType(long centsPerMinute) {
        this.centsPerMinute = centsPerMinute;
    }

    long centsPerMinute() {
        return centsPerMinute;
    }
}
```

Now the price lives next to the bike type. Changing the e-bike price means changing one number, in one obvious place.

```java run
public class Main {
    public static void main(String[] args) {
        for (BikeType type : BikeType.values()) {
            System.out.println(type + " costs " + type.centsPerMinute() + " cents a minute");
        }
    }
}

enum BikeType {
    CLASSIC(15), EBIKE(30), CARGO(25);

    private final long centsPerMinute;

    BikeType(long centsPerMinute) {
        this.centsPerMinute = centsPerMinute;
    }

    long centsPerMinute() {
        return centsPerMinute;
    }
}
```

### Small functions that do one thing

A method should do one thing, and its name should say what that thing is. If you need "and" to describe it, it probably does two things.

A related rule is **one level of abstraction per function**. An **abstraction level** is how close a line is to the big idea or to the tiny details. A high-level method reads like a summary. The details live in helpers.

```java nocompile
static long fareCents(BikeType type, int minutes, Membership membership) {
    return unlockFee(membership) + minuteCharge(type, minutes, membership);
}
```

You can read that line out loud: "the fare is the unlock fee plus the minute charge." The math hides one level down.

### Command-query separation

**Command-query separation** says a method should either change something or answer a question. It should never do both.

- A **command** changes state and returns nothing: `void recordRide(int minutes)`.
- A **query** returns an answer and changes nothing: `long totalCents()`.

A query that secretly changes state surprises people. Calling it twice gives two different answers.

### Comments that say why

A good comment explains **why** the code is the way it is. The code itself already says **what** it does.

```java
// add 100 to the total
long total = 0;
total = total + 100;
```

That comment repeats the code. Delete it. Compare this one:

```java
int rideMinutes = 200;
// The city contract caps a cargo ride at 3 hours, so longer rides are billed as 180 minutes.
int billedMinutes = Math.min(rideMinutes, 180);
```

The second comment holds something the code cannot show: a reason from outside the program.

```callout tip
Before you write a comment, try a better name first. If the name says it all, skip the comment.
```

### Syntax: a guard clause

Deep nesting means `if` inside `if` inside `if`. Each level pushes the real work further right. A **guard clause** checks a bad or special case at the top and leaves the method early.

```java nocompile
if (minutes < 0) {
    throw new IllegalArgumentException("minutes must not be negative, got " + minutes);
}
```

After the guard, the rest of the method can assume the input is fine.

```callout analogy
A guard clause is the bouncer at the door. Bad guests get turned away at the entrance. Everyone inside the club is already allowed to be there, so nobody checks again.
```

## Worked example

Here is a tiny piece of old Spoke code, before and after.

```java nocompile
static long f(int k, int m) {
    long r = 0;
    if (m >= 0) {
        if (k == 0) {
            r = 100 + m * 15;
        } else {
            if (m > 30) {
                r = (m - 30) * 15;
            }
        }
    }
    return r;
}
```

Here it is cleaned up, as a full program.

```java run
public class Main {
    static final long UNLOCK_FEE_CENTS = 100;
    static final long CLASSIC_CENTS_PER_MINUTE = 15;
    static final int MEMBER_FREE_MINUTES = 30;

    static long classicFareCents(int minutes, boolean isMember) {
        if (minutes < 0) {
            throw new IllegalArgumentException("minutes must not be negative, got " + minutes);
        }
        if (isMember) {
            return paidMinutes(minutes) * CLASSIC_CENTS_PER_MINUTE;
        }
        return UNLOCK_FEE_CENTS + minutes * CLASSIC_CENTS_PER_MINUTE;
    }

    static int paidMinutes(int minutes) {
        return Math.max(0, minutes - MEMBER_FREE_MINUTES);
    }

    public static void main(String[] args) {
        System.out.println(classicFareCents(10, false));
        System.out.println(classicFareCents(10, true));
        System.out.println(classicFareCents(40, true));
    }
}
```

Line by line:

1. Three constants replace the magic numbers 100, 15 and 30.
2. The name `classicFareCents` says what comes back, and in which unit.
3. `int k` became `boolean isMember`. Nobody has to guess what code 0 means.
4. The guard clause throws for bad input. The old code returned 0 silently, which hid the bug.
5. Members get a single early `return`. There is no `else` and no nesting.
6. `paidMinutes` is a small query with one job. `Math.max` stops the result from going below zero.

The output is `250`, then `0`, then `150`.

### Keeping old callers working

Other code may still call the old `f`. You can keep it as a one-line delegate. A **delegate** is a method that passes its work straight to another method. Mark it with `@Deprecated`, the annotation you met in module 5. It tells everyone "this still works, but use the new one".

```java nocompile
@Deprecated
static long f(int k, int m) {
    return classicFareCents(m, k != 0);
}
```

## Try it

Run this program. Then clean it up:

1. Rename `c` and `x` to names that say what they mean.
2. Replace `12` with a named constant `DOCKS_PER_KIOSK`.
3. Delete the comment that repeats the code.

```java run
public class Main {
    static int c(int x) {
        // subtract x from 12
        return 12 - x;
    }

    public static void main(String[] args) {
        System.out.println("Free docks at Harbor: " + c(5));
    }
}
```

The output must stay `Free docks at Harbor: 7` after your changes. A clean-up that changes the behaviour is a bug.

## Recap

- Names should say what a thing holds or does, including the unit when it matters.
- Replace magic numbers with named constants, or with fields on an enum.
- Keep each method small, doing one thing at one level of abstraction.
- A method either changes something or answers a question, never both.
- Comments explain why. Guard clauses handle bad cases at the top, so the main path stays flat.

## Common mistakes

**1. A query that also changes state.**

```java
public class Main {
    static int ridesToday = 0;

    static int nextRideNumber() {
        ridesToday = ridesToday + 1;
        return ridesToday;
    }

    public static void main(String[] args) {
        System.out.println("Ride " + nextRideNumber());
        System.out.println("Logged ride " + nextRideNumber());
    }
}
```

You expect both lines to show ride 1. The program prints:

```
Ride 1
Logged ride 2
```

The method sounds like a question but changes a field. Fix it by splitting it: a command `void startRide()` that adds one, and a query `int currentRide()` that only returns the value.

**2. Writing `public` on an enum constructor.**

```java nocompile
enum BikeType {
    CLASSIC(15);

    private final long centsPerMinute;

    public BikeType(long centsPerMinute) {
        this.centsPerMinute = centsPerMinute;
    }
}
```

```
Main.java:6: error: modifier public not allowed here
    public BikeType(long centsPerMinute) {
           ^
```

Only the enum itself creates its constants, so its constructor can never be public. Fix: remove `public`, or write `private`.

**3. A guard clause that does not leave the method.**

```java
public class Main {
    static long fareCents(int minutes) {
        if (minutes < 0) {
            System.out.println("bad minutes: " + minutes);
        }
        return 100 + minutes * 15;
    }

    public static void main(String[] args) {
        System.out.println(fareCents(-10));
    }
}
```

```
bad minutes: -10
-50
```

The guard printed a warning but kept going, so a rider got a fare of minus 50 cents. A guard must end the method. Fix: replace the `println` with `throw new IllegalArgumentException("minutes must not be negative, got " + minutes);`.

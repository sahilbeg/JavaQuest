## Hook

Imagine a smoke alarm that only blinks a tiny light when it finds smoke. If you are in the next room, you miss it. A real alarm screams, and you cannot carry on until you deal with it.

The Harbor kiosk works like the blinking light. When the station is full, `dock()` returns `-1`. Some code forgets to check, and a rider gets charged for a ride that never ended. In this lesson you turn the blinking light into a screaming alarm.

## Concept

### Checked and unchecked exceptions

Java has two families of exceptions.

- An **unchecked exception** extends `RuntimeException`. The compiler does not force anyone to handle it. `NumberFormatException` and `IllegalArgumentException` are unchecked. They usually mean a bug or a bad value.
- A **checked exception** extends `Exception` but not `RuntimeException`. The compiler checks that every caller handles it. It means "this can go wrong even when your code is correct", such as a full station or a missing file.
- A third branch, `Error`, sits beside `Exception`. Errors such as `OutOfMemoryError` mean the JVM itself is in trouble. They are unchecked, and your code should not catch them.

```mermaid
flowchart TD
  T["Throwable"] --> X["Error: unchecked, the JVM is in trouble"]
  T --> E["Exception: checked"]
  E --> R["RuntimeException: unchecked"]
  R --> I["IllegalArgumentException"]
  I --> N["NumberFormatException"]
  E --> D["DockFullException: your own, checked"]
  R --> U["UnknownBikeTypeException: your own, unchecked"]
```

```callout jargon
**Checked** means the compiler checks it. If a method can throw a checked exception, every caller must catch it or pass it on. Otherwise the code does not compile.
```

### Syntax: your own exception class

```java
class DockFullException extends Exception {
    DockFullException(String station) {
        super("Station full: " + station);
    }
}
```

- An exception is a normal class. It extends `Exception` (checked) or `RuntimeException` (unchecked).
- `super("...")` passes the message to the parent class. Later, `getMessage()` gives that text back.
- The class name says what went wrong. That makes a stack trace easy to read.

An unchecked one looks the same, with a different parent:

```java
class UnknownBikeTypeException extends RuntimeException {
    UnknownBikeTypeException(String type) {
        super("Unknown bike type: " + type);
    }
}
```

### Syntax: throws in a method signature

```java nocompile
int dock(String bikeId) throws DockFullException {
    // ...
}
```

`throws DockFullException` is a warning label on the method. It tells every caller: "this method can throw a `DockFullException`". For a checked exception the label is required.

```java nocompile
int dock(String bikeId) throws DockFullException {
    if (docked == docks) {
        throw new DockFullException(name);
    }
    docked = docked + 1;
    return docked;
}
```

Note the two words. `throw` (no s) sends one exception now. `throws` (with s) goes in the signature and lists what the method might throw.

### The caller must decide

When you call `dock`, the compiler gives you two choices.

Choice 1: catch it.

```java nocompile
try {
    int number = harbor.dock("B-1042");
    System.out.println("Docked at " + number);
} catch (DockFullException e) {
    System.out.println(e.getMessage());
}
```

Choice 2: pass it on, by adding `throws` to your own method.

```java nocompile
static void endRide(Station s, String bikeId) throws DockFullException {
    s.dock(bikeId);
}
```

If you do neither, the code does not compile. The full-station case cannot be forgotten.

```callout tip
Use a checked exception when a careful caller can still hit the problem and must plan for it. Use an unchecked one for a bug, such as a bike type that should never exist.
```

### Syntax: rethrowing with a cause

```java nocompile
} catch (NumberFormatException e) {
    throw new BadRideLineException("Bad minutes in: " + line, e);
}
```

Sometimes you catch a low-level exception and throw a clearer one. Pass the original as the **cause**. The cause is the exception that started the problem. It keeps the full story in the stack trace.

```java
class BadRideLineException extends Exception {
    BadRideLineException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

`Throwable` is the top parent of every exception. Read the cause back with `e.getCause()`.

### Syntax: try-with-resources

```java nocompile
try (Session session = new Session()) {
    // use session
}
```

A **resource** is something you must close when you finish, like a kiosk screen session or a file. A class is a resource when it implements `AutoCloseable`. That interface has one method: `close()`.

Put the resource in round brackets after `try`. Java calls `close()` for you when the block ends. It does this when the block finishes normally, and also when it throws.

```java
class Session implements AutoCloseable {
    public void close() {
        System.out.println("Session closed");
    }
}
```

```callout analogy
try-with-resources is like a door that shuts itself. However you leave the room, calmly or running, the door closes behind you.
```

### Several resources and suppressed exceptions

One `try` can open several resources, separated by `;`. Java closes them in reverse order: the last one opened closes first. What if `close()` throws too, while another exception is already flying?

```java run
public class Main {
    public static void main(String[] args) {
        try (Light light = new Light(); Lock lock = new Lock()) {
            System.out.println("Unlocking B-1042");
            throw new IllegalStateException("Lock jammed");
        } catch (IllegalStateException e) {
            System.out.println("Caught: " + e.getMessage());
            for (Throwable s : e.getSuppressed()) {
                System.out.println("Suppressed: " + s.getMessage());
            }
        }
    }
}

class Light implements AutoCloseable {
    public void close() {
        System.out.println("Light closed");
    }
}

class Lock implements AutoCloseable {
    public void close() {
        System.out.println("Lock closed");
        throw new IllegalStateException("Lock would not close");
    }
}
```

The output:

```
Unlocking B-1042
Lock closed
Light closed
Caught: Lock jammed
Suppressed: Lock would not close
```

The first problem, `Lock jammed`, is the one you catch. The close problem is not lost. Java attaches it as a **suppressed** exception, and `getSuppressed()` returns it. Stack traces print it too, under `Suppressed:`.

```callout jargon
A **suppressed exception** is a second problem that happened while Java was already handling a first one. It rides along inside the first exception.
```

### Syntax: multi-catch

```java nocompile
catch (NumberFormatException | ArrayIndexOutOfBoundsException e) { ... }
```

When two exception types need the same handling, list them in one `catch` with `|` between them. You write the handling once.

```java run
public class Main {
    static int parseMinutes(String[] parts) {
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Skipped a bad line: " + e.getClass().getSimpleName());
            return 0;
        }
    }

    public static void main(String[] args) {
        System.out.println(parseMinutes("B-1042,25".split(",")));
        System.out.println(parseMinutes("B-2001,oops".split(",")));
        System.out.println(parseMinutes("B-3300".split(",")));
    }
}
```

It prints `25`, then a skipped `NumberFormatException` and `0`, then a skipped `ArrayIndexOutOfBoundsException` and `0`. The two types in one multi-catch cannot be parent and child. That would be a compile error, since the parent already covers the child.

### Rules for real services

A **service** is a program that runs all day and answers requests, like the Spoke API. Its exception handling follows a few rules.

- **Catch only what you can handle.** `catch (Exception e)` also swallows every bug, such as a `NullPointerException`. Never catch `Throwable` or `Error`: you cannot fix a JVM out of memory.
- **Log or rethrow, never both.** If every layer logs and rethrows, one problem fills the log three times. Handle it in one place, usually at the top.
- **Pick the right built-in type.** `IllegalArgumentException` means "this value is wrong", like `-5` minutes. `IllegalStateException` means "the value is fine, but not now", like ending a rental that already ended.
- **Fail fast on null.** Check inputs at the door, in the constructor, so the error points at the real culprit.

```java nocompile
record Rental(String id, String bikeId) {
    Rental {
        Objects.requireNonNull(bikeId, "bikeId must not be null");   // import java.util.Objects;
    }
}
```

`new Rental("rental-18", null)` now fails at once with `java.lang.NullPointerException: bikeId must not be null`. Without the check, it would fail much later, somewhere far away.

```callout jargon
To **log** is to write a line about what happened into the service's log, a diary that engineers read when something breaks.
```

### Money with BigDecimal

Module 01 showed why money is counted in whole cents in a `long`: a `double` cannot hold 0.10 exactly. Sometimes you need parts of a cent, such as a 7.25% tax. Then use `BigDecimal` from `java.math`. It stores decimals exactly.

```java run
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Main {
    public static void main(String[] args) {
        System.out.println(new BigDecimal(0.1));
        System.out.println(new BigDecimal("0.10"));

        BigDecimal fare = new BigDecimal("8.50");
        BigDecimal tax = fare.multiply(new BigDecimal("0.0725"));
        System.out.println(tax);
        System.out.println(tax.setScale(2, RoundingMode.HALF_EVEN));
        System.out.println(new BigDecimal("0.125").setScale(2, RoundingMode.HALF_EVEN));

        BigDecimal a = new BigDecimal("2.0");
        BigDecimal b = new BigDecimal("2.00");
        System.out.println(a.equals(b));
        System.out.println(a.compareTo(b) == 0);
    }
}
```

The output:

```
0.1000000000000000055511151231257827021181583404541015625
0.10
0.616250
0.62
0.12
false
true
```

- Build a `BigDecimal` from a String, `new BigDecimal("0.10")`. `new BigDecimal(0.1)` copies the double's error exactly.
- `setScale(2, RoundingMode.HALF_EVEN)` rounds to 2 decimal places. `HALF_EVEN` sends an exact half to the even digit, so 0.125 becomes 0.12. Banks use it because it does not lean up or down over many sums.
- `equals` also compares the **scale**, the number of decimal places. So `2.0` and `2.00` are not equal. Compare amounts with `compareTo(...) == 0`.

```callout tip
Gojo says: this ticket's practice still returns prices like 0.15 as a `double`, as the old kiosk did. New Spoke code uses `long` cents, or `BigDecimal` when it needs parts of a cent.
```

## Worked example

This program docks bikes at a tiny station with 2 docks. A session opens for each bike and closes on its own.

```java run
public class Main {
    public static void main(String[] args) {
        Station harbor = new Station("Harbor", 2);
        String[] bikes = {"B-1042", "B-2001", "B-3300"};
        for (String bike : bikes) {
            try (Session session = new Session()) {
                System.out.println(bike + " docked at " + harbor.dock(bike));
            } catch (DockFullException e) {
                System.out.println(bike + ": " + e.getMessage());
            }
        }
    }
}

class DockFullException extends Exception {
    DockFullException(String station) {
        super("Station full: " + station);
    }
}

class Station {
    private final String name;
    private final int docks;
    private int docked;

    Station(String name, int docks) {
        this.name = name;
        this.docks = docks;
    }

    int dock(String bikeId) throws DockFullException {
        if (docked == docks) {
            throw new DockFullException(name);
        }
        docked = docked + 1;
        return docked;
    }
}

class Session implements AutoCloseable {
    public void close() {
        System.out.println("  session closed");
    }
}
```

1. `DockFullException` extends `Exception`, so it is checked. Its message is `Station full: Harbor`.
2. `dock` says `throws DockFullException`. When all docks are used, it throws one.
3. In `main`, the call sits inside `try`, and a `catch` handles the full case. Remove the `catch` and the program does not compile.
4. `Session` implements `AutoCloseable`. Java calls `close()` at the end of each `try`, before any `catch` runs.
5. The third bike prints `  session closed` first and then `B-3300: Station full: Harbor`.

## Try it

Run this program. Then change `"ebike"` to `"tandem"` and run it again. Read the message.

```java run
public class Main {
    static long centsPerMinute(String type) {
        if (type.equals("classic")) {
            return 15;
        }
        if (type.equals("ebike")) {
            return 30;
        }
        if (type.equals("cargo")) {
            return 25;
        }
        throw new UnknownBikeTypeException(type);
    }

    public static void main(String[] args) {
        try {
            System.out.println(centsPerMinute("ebike"));
        } catch (UnknownBikeTypeException e) {
            System.out.println("Problem: " + e.getMessage());
        }
    }
}

class UnknownBikeTypeException extends RuntimeException {
    UnknownBikeTypeException(String type) {
        super("Unknown bike type: " + type);
    }
}
```

`centsPerMinute` has no `throws` label. `UnknownBikeTypeException` is unchecked, so the compiler does not ask for one.

```callout warning
Do not turn a checked exception into silence with an empty catch. The compiler made you look at the problem. Return a clear message, or pass it on.
```

## Recap

- A **checked** exception extends `Exception`. Every caller must catch it or declare it with `throws`.
- An **unchecked** exception extends `RuntimeException`. The compiler does not force anyone to handle it.
- Write your own exception as a class, and pass the message up with `super(message)`. Read it with `getMessage()`.
- When you catch one exception and throw a clearer one, pass the original as the cause. Catch only what you can handle, and log or rethrow, never both.
- try-with-resources closes any `AutoCloseable` object for you, last opened first, even when the block throws. Count money in `long` cents, or `BigDecimal` made from a String.

## Common mistakes

**Calling a method that throws a checked exception without handling it.**

```java nocompile
public static void main(String[] args) {
    Station harbor = new Station("Harbor", 12);
    harbor.dock("B-1042");
}
```

Java says: `unreported exception DockFullException; must be caught or declared to be thrown`. Wrap the call in `try` with a `catch (DockFullException e)`, or add `throws DockFullException` to the method.

**Forgetting to pass the message to super.**

```java nocompile
class DockFullException extends Exception {
    DockFullException(String station) {
    }
}
```

This compiles, but `e.getMessage()` gives back `null`. The rider sees `null` on the screen. Add `super("Station full: " + station);` as the first line of the constructor.

**Using a class that is not AutoCloseable in try-with-resources.**

```java nocompile
class Session {
    public void close() {
        System.out.println("closed");
    }
}

try (Session s = new Session()) {
}
```

Java says: `incompatible types: try-with-resources not applicable to variable type`. Having a `close()` method is not enough. Write `class Session implements AutoCloseable`.

## Hook

Itachi forwards three crash reports at 9 a.m. Each one ends in a wall of text that starts with `Exception in thread "main"`. The developer who wrote the code is on holiday, so nobody can ask what they meant.

That wall of text is a gift. It tells you what went wrong, where, and how the program got there. In this topic you learn to read it. Then you watch the code run one line at a time, until the bug has nowhere to hide.

## Concept

```callout jargon
A **bug** is code that does something its author did not intend. **Debugging** is finding and fixing it. A **stack trace** is the report Java prints when an exception escapes: the exception, then the chain of method calls that led to it.
```

### Syntax: reading a stack trace

Here is a trace in its smallest form, with each part named.

```
Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index 3 out of bounds for length 3
	at jq.m06.debugging.RideParser.parse(RideParser.java:11)
	at jq.m06.debugging.NightlyImport.main(NightlyImport.java:5)
```

- **Exception type**: `ArrayIndexOutOfBoundsException`. This tells you the kind of problem.
- **Message**: `Index 3 out of bounds for length 3`. This gives the details. The code asked for index 3 of an array with only 3 slots.
- **Frames**: each `at` line is one method call that was still running. The top frame is where the exception happened. Each line below it is the caller of the line above.
- **File and line**: `RideParser.java:11` means line 11 of that file.

Read top to bottom. Stop at the **first line in your own code**. Lines that start with `java.base/` are the JDK's code, and the bug is almost never there.

### Syntax: Caused by

Sometimes code catches one exception and throws a new one with the old one inside. You did this in module 3 with `throw new X("message", e)`. The trace then has a second part that starts with `Caused by:`.

```java run crash
public class Main {
    public static void main(String[] args) {
        ImportJob job = new ImportJob();
        job.importLine("B-1042,Harbor,twelve");
    }
}

class ImportJob {
    void importLine(String line) {
        try {
            int minutes = readMinutes(line);
            System.out.println("minutes: " + minutes);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("could not import line: " + line, e);
        }
    }

    int readMinutes(String line) {
        String[] parts = line.split(",");
        return Integer.parseInt(parts[2]);
    }
}
```

Run it on JDK 21 and you see this:

```
Exception in thread "main" java.lang.IllegalStateException: could not import line: B-1042,Harbor,twelve
	at ImportJob.importLine(Main.java:14)
	at Main.main(Main.java:4)
Caused by: java.lang.NumberFormatException: For input string: "twelve"
	at java.base/java.lang.NumberFormatException.forInputString(NumberFormatException.java:67)
	at java.base/java.lang.Integer.parseInt(Integer.java:662)
	at java.base/java.lang.Integer.parseInt(Integer.java:778)
	at ImportJob.readMinutes(Main.java:20)
	at ImportJob.importLine(Main.java:11)
	at Main.main(Main.java:4)
	...
```

The top part says the import failed. The real reason is in the **last** `Caused by`. Skip the three `java.base/` lines, and the first line of your own code is `readMinutes(Main.java:20)`. The text "twelve" is not a number. (The lines after `Main.main` come from the launcher that runs `java Main.java`. Ignore them.)

```callout tip
The fastest way through any trace: find the last `Caused by`, then its first frame from your own package. Read that line of code before anything else.
```

### Syntax: helpful NullPointerException messages

A **NullPointerException** (NPE) happens when you call a method on `null`. Since Java 14, the message names exactly which value was null.

```java run crash
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<String, Rider> riders = Map.of("ada", new Rider("Ada", null));
        Rider ada = riders.get("ada");
        System.out.println(ada.card().length());
    }
}

record Rider(String name, String card) { }
```

```
Exception in thread "main" java.lang.NullPointerException: Cannot invoke "String.length()" because the return value of "Rider.card()" is null
	at Main.main(Main.java:7)
```

Line 7 has two calls. The message says `card()` returned null, so `ada` itself was fine. Read the "because" part every time.

One NPE looks strange at first: `Cannot invoke "java.lang.Integer.intValue()"`. You never wrote `intValue()`. Java wrote it for you. When a method returns `int` but the map holds `Integer`, Java **unboxes** the value by calling `intValue()`. If the map had no entry, the value was null, and unboxing null crashes.

### Breakpoints

Printing values works, but it means editing code and running again for every question. A **debugger** pauses the running program so you can look inside it.

A **breakpoint** is a mark on a line that says "pause here". In IntelliJ IDEA or VS Code, click in the narrow gutter left of a line number. A red dot appears. Then start the program with the **Debug** button (a bug icon) instead of Run. When the program reaches that line, it stops before running it.

While paused, the **Variables** panel shows every local variable and field with its current value. You can open an object to see its fields, or open an array to see each slot.

A **conditional breakpoint** only pauses when a condition is true. Right-click the red dot and type a Java expression such as `line.endsWith(",")`. This helps when a loop runs 10,000 times and only one line is bad.

```callout analogy
A breakpoint is the pause button on a cooking video. The video stops at the moment the cake collapses, and you can look at every bowl on the counter.
```

### Stepping

Once paused, you move forward with step buttons.

| Button | What it does |
|---|---|
| **Step over** | Run this line, then pause on the next line of the same method. |
| **Step into** | If this line calls one of your methods, go inside it and pause on its first line. |
| **Step out** | Finish the current method and pause back in the caller. |
| **Resume** | Run on until the next breakpoint. |

A **watch** is an expression you pin in the debugger. Type `parts.length` or `minutesLost / totalMinutes` into the Watches panel. The debugger shows its value after every step.

### Finding the bug without a debugger

- **Rubber-duck debugging**: explain the code, line by line, out loud to a rubber duck or a patient colleague. Saying "and then this divides 6 by 12, which is..." often reveals the bug before you finish the sentence.
- **Binary search**: if a long method gives the wrong answer, check the value halfway through. If it is already wrong, the bug is in the first half. If not, it is in the second half. Each check halves the search. You can also comment out half the code and see if the crash goes away.
- **git bisect** does the same across history. You tell git one commit that works and one that is broken. It checks out the commit in the middle, you test it and type `git bisect good` or `git bisect bad`, and it halves again. Within about 10 steps it names the commit that brought in the bug, even across 1,000 commits.

### Reproduce first, with a failing test

Before you change any code, write a test that fails the same way the crash report does. You learned this rhythm in [TDD](/t/module-06-craft/topic-10-tdd). The test proves you found the real bug. When it turns green, you know the fix worked. It also stays in the suite, so the bug cannot quietly come back.

```java nocompile
@Test
void parsesLineWithEmptyNote() {
    ParsedRide ride = new RideParser().parse("B-1042,Harbor,12,");
    assertEquals("", ride.note());
}
```

## Worked example

Crash report 3 says a rider paid 280 cents, lost 6 of 12 minutes, and got 0 cents back. There is no stack trace this time, because nothing crashed. The answer is just wrong.

The code is one line: `return paidCents * (minutesLost / totalMinutes);`. Put a breakpoint on it, debug the failing test, and add a watch for `minutesLost / totalMinutes`. The watch shows `0`. The program below does the same thing with printing.

```java run
public class Main {
    public static void main(String[] args) {
        int lost = 6;
        int total = 12;
        long paid = 280;
        System.out.println("lost / total = " + (lost / total));
        System.out.println("paid * (lost / total) = " + paid * (lost / total));
        System.out.println("paid * lost / total = " + paid * lost / total);
    }
}
```

```
lost / total = 0
paid * (lost / total) = 0
paid * lost / total = 140
```

1. `6 / 12` with two ints is **integer division**. It throws away the fraction, so 0.5 becomes 0.
2. Anything times 0 is 0. That is the 0 cent refund.
3. Multiply first and the numbers stay whole: `280 * 6` is 1680, and `1680 / 12` is 140.
4. Because `paid` is a `long`, `paid * lost` is computed as a `long`, so large amounts do not overflow an `int`.

The fix is to move the brackets. The investigation took one breakpoint and one watch.

## Try it

Change the line `job.importLine("B-1042,Harbor,twelve");` in the Caused-by program above to `job.importLine("B-1042,Harbor");`. Before you press Run, predict the exception type and the line number. Then run it and check your guess against the trace.

## Recap

- Read a stack trace top to bottom: type, message, then the first frame in your own code. The real cause is in the last `Caused by`.
- A helpful NPE message names the null value. `intValue()` in the message means a null `Integer` was unboxed.
- A breakpoint pauses the program. Step over, into and out move through it, and watches show values as you go.
- Rubber ducks, binary search and `git bisect` narrow a bug down when there is no trace.
- Reproduce the bug with a failing test before you fix it.

## Common mistakes

**1. Printing only the message.**

```java
public class Main {
    public static void main(String[] args) {
        try {
            int minutes = Integer.parseInt("twelve");
            System.out.println(minutes);
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
        }
    }
}
```

What you see:

```
For input string: "twelve"
```

There is no exception type and no line number, so nobody knows where it came from. Fix: rethrow with the cause, or let the exception escape. Later you will log it with the full trace.

**2. Wrapping an exception without its cause.**

```java
public class Main {
    public static void main(String[] args) {
        try {
            int minutes = Integer.parseInt("twelve");
            System.out.println(minutes);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("could not import ride");
        }
    }
}
```

What you see:

```
Exception in thread "main" java.lang.IllegalStateException: could not import ride
	at Main.main(Main.java:7)
```

The `Caused by` part is gone, so the real reason is lost. Fix: pass the caught exception along, `throw new IllegalStateException("could not import ride", e);`.

**3. Trusting split to keep empty fields at the end.**

```java
public class Main {
    public static void main(String[] args) {
        String[] parts = "B-1042,Harbor,12,".split(",");
        System.out.println(parts.length);
    }
}
```

What you see:

```
3
```

You expected 4 parts. `split` drops empty strings at the end. Fix: `split(",", -1)` keeps them, and then check `parts.length` before you read a slot.

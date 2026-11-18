## Hook

Picture a ship's captain who writes every thought in the logbook. "Sky is blue." "Had tea." "Engine on fire." "Had more tea." When the storm hits, nobody finds the fire among a thousand cups of tea.

That is the Spoke kiosk today. Every event goes through `System.out.println`. Last night Itachi's night shift searched ten thousand lines for one failed payment at Old Mill. Worse, one line printed a rider's full card number. In this topic you replace those prints with real **logging**.

## Concept

### Why println is not logging

```callout jargon
**Logging** means writing short records of what a program did, each with a time, a level of importance and the place it came from. One record is a **log line**.
```

`System.out.println` has four problems for a live kiosk:

- Every line looks the same. An error looks exactly like "had tea".
- No line says when it happened or which class wrote it.
- You cannot turn the chatty lines off without editing code.
- Everything goes to one screen. You cannot send errors to a file or an alert.

A logging library fixes all four. You pick how important each line is, and a config file decides what gets shown and where.

### SLF4J and Logback

Java has several logging libraries. Spoke uses two that work as a pair.

```callout jargon
A **facade** is a simple front door that hides the machinery behind it. **SLF4J** (Simple Logging Facade for Java) is the facade your code calls. **Logback** is the machinery behind it that formats each line and writes it out.
```

Your code only ever imports `org.slf4j`. You could swap Logback for another library later, and your classes would not change. That is the same idea as coding to an interface, which you met with the SOLID principles.

### Syntax: getting a logger

```java nocompile
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Checkout {
    private static final Logger log = LoggerFactory.getLogger(Checkout.class);
}
```

- `LoggerFactory.getLogger(Checkout.class)` makes a logger named after the class.
- `private static final` means one logger per class, shared and never replaced.
- Every log line then shows `jq.m06.logging.Checkout`, so you know where it came from.

### Syntax: the five levels

```java nocompile
log.trace("checking dock sensor 7");
log.debug("fare rule picked: ebike");
log.info("ride started for R-17");
log.warn("Harbor has only 1 free dock");
log.error("payment failed for R-17");
```

| Level | Use it for | Example |
|---|---|---|
| TRACE | Tiny steps, only while hunting a bug | each loop pass |
| DEBUG | Details a developer wants | which fare rule ran |
| INFO | Normal events worth a record | a ride started, a payment went through, the bank declined a card |
| WARN | Something odd that the program handled | the payment provider timed out and the retry worked, a station nearly full |
| ERROR | Something failed and needs a human | a payment crashed |

The levels are ordered from TRACE (lowest) to ERROR (highest). When you set a level, you see that level and everything above it. At INFO, the night shift sees INFO, WARN and ERROR. TRACE and DEBUG stay hidden.

```callout tip
Gojo says: ask "would the night shift want to be woken up for this?" If yes, it is ERROR. If it is odd but handled, it is WARN.
```

A declined card is a normal business outcome. Banks decline cards many times a day, so most teams log it at INFO. Spoke's kiosk has one house rule on top: a payment over its $500 limit is logged at WARN. A fare that large should never happen, so it may mean a bug or fraud. The practice task follows this house rule. At work, follow your team's.

### Syntax: parameterized messages

```java nocompile
log.info("payment ok for {} card {} amount {}", riderId, masked, cents);
```

- Each `{}` is a **placeholder**: a hole that SLF4J fills with the next value.
- The values come after the message, in order.
- With `riderId` = `"R-17"`, `masked` = `"**** **** **** 4242"` and `cents` = `1250`, the line reads `payment ok for R-17 card **** **** **** 4242 amount 1250`.

Why not join strings with `+`? Two reasons.

1. `"ride " + ride` builds the text every time, even when that level is switched off. With `{}`, SLF4J skips the formatting for a hidden line, including the call to `ride.toString()`.
2. With `{}`, the message template stays the same for every rider. Log search tools can then group all "payment ok" lines together.

### Expensive arguments still run

`{}` saves the formatting, not the arguments. Java works out every argument **before** it calls `log.debug`. So `log.debug("report {}", buildReport())` runs `buildReport()` even when DEBUG is off.

When an argument is slow to build, guard it. Either check the level first, or hand SLF4J 2 a **supplier**: a lambda it calls only when the line will really be written.

```java run
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.trace("report {}", buildReport());

        if (log.isTraceEnabled()) {
            log.trace("report {}", buildReport());
        }

        log.atTrace().addArgument(() -> buildReport()).log("report {}");

        System.out.println("done");
    }

    static String buildReport() {
        System.out.println("building the report (slow!)");
        return "Harbor: 12 docks";
    }
}
```

```text
building the report (slow!)
done
```

TRACE is hidden by default, so no log line appears. Yet the report was built once, for the first line, and thrown away. The `isTraceEnabled()` guard and the `atTrace()` supplier both skipped it. For a plain variable like `riderId`, none of this matters: just use `{}`.

### Syntax: logging an exception

```java nocompile
log.error("payment failed for {}", riderId, exception);
```

Put the exception **last**, after the values for the placeholders. SLF4J sees that the last value is an exception. It prints the message, then the full stack trace under it. The stack trace is the map you learned to read in [Debugging](/t/module-06-craft/topic-11-debugging).

### Never log secrets

A log file gets copied, emailed and stored for months. A card number in a log is a card number in many places. Payment rules also forbid it.

```callout warning
Never log a full card number, a password or a security code. Mask the card first, so only the last 4 digits remain: `**** **** **** 4242`.
```

### MDC in one paragraph

**MDC** (Mapped Diagnostic Context) lets you attach a label, such as a request id or a ride id, to every log line on the current thread. You call `MDC.put("ride", "rental-17")` at the start of a request and `MDC.remove("ride")` at the end, in a `finally` block. Logback can then print that label on every line in between. You do not need it for this ticket.

### Syntax: clearing the MDC

```java nocompile
try (MDC.MDCCloseable ride = MDC.putCloseable("ride", "rental-17")) {
    log.info("ride started");
}   // the label is removed here, even if an exception was thrown
```

Servers reuse a small **pool** of threads for many requests. If you forget to remove the label, the next request on that thread inherits it. Its log lines then name `rental-17`, a ride it has nothing to do with. `MDC.putCloseable` in a try-with-resources removes the label for you, like `MDC.remove` in a `finally`.

```java run
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (MDC.MDCCloseable ride = MDC.putCloseable("ride", "rental-17")) {
            log.info("ride {} started", MDC.get("ride"));
        }
        System.out.println("after: " + MDC.get("ride"));
    }
}
```

`after: null` shows the label is gone once the block ends.

### Syntax: a minimal logback.xml

Logback reads its settings from a file called `logback.xml` in `src/main/resources`, the folder for files that ship with your code. Module 7's [Maven](/t/module-07-tooling/topic-01-maven) topic explains this folder layout. This file prints INFO and above to the screen:

```xml
<configuration>
  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss} %-5level %logger{20} - %msg%n</pattern>
    </encoder>
  </appender>
  <root level="INFO">
    <appender-ref ref="CONSOLE" />
  </root>
</configuration>
```

- An **appender** is a place log lines go, here the console.
- The **pattern** sets the layout: time, level, logger name, message, new line.
- `root level="INFO"` hides TRACE and DEBUG.

With no `logback.xml` at all, Logback shows DEBUG and above in a default layout.

### Syntax: levels per package

```xml
<logger name="jq.m06" level="DEBUG" />
```

A `<logger>` line sets the level for one package and everything inside it. It goes inside `<configuration>`, next to `<root>`. Logger names follow class names, so `jq.m06` covers `jq.m06.logging.Checkout` too.

Production usually runs with `root` at INFO, so the logs stay small and cheap. When you hunt a bug in one area, turn on DEBUG for just that package. The rest of the app stays quiet.

```xml
<configuration>
  <!-- appender as above -->
  <logger name="jq.m06.logging" level="DEBUG" />
  <logger name="org.springframework" level="WARN" />
  <root level="INFO">
    <appender-ref ref="CONSOLE" />
  </root>
</configuration>
```

## Worked example

Here is the checkout from the ticket, written with a logger.

```java run
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        pay("R-17", "4242 4242 4242 4242", 1250);
        pay("R-17", "4242 4242 4242 4242", 60_000);
        pay("R-17", null, 1250);
    }

    static boolean pay(String riderId, String cardNumber, long cents) {
        if (cardNumber == null) {
            log.error("payment failed for {}", riderId, new IllegalArgumentException("card number is missing"));
            return false;
        }
        String masked = mask(cardNumber);
        if (cents > 50_000) {
            log.warn("payment declined for {} card {}", riderId, masked);
            return false;
        }
        log.info("payment ok for {} card {} amount {}", riderId, masked, cents);
        return true;
    }

    static String mask(String cardNumber) {
        String digits = cardNumber.replaceAll("[^0-9]", "");
        if (digits.length() < 4) {
            return "****";
        }
        return "**** **** **** " + digits.substring(digits.length() - 4);
    }
}
```

The output looks like this. Your times will differ.

```text
21:18:44.795 [main] INFO Main -- payment ok for R-17 card **** **** **** 4242 amount 1250
21:18:44.797 [main] WARN Main -- payment declined for R-17 card **** **** **** 4242
21:18:44.797 [main] ERROR Main -- payment failed for R-17
java.lang.IllegalArgumentException: card number is missing
	at Main.pay(Main.java:15)
```

1. The `log` field is made once for the class.
2. A null card is checked first. The ERROR line attaches a new `IllegalArgumentException`, so the stack trace shows where it happened.
3. `mask` keeps only the digits, then the last 4. `replaceAll("[^0-9]", "")` removes everything that is not a digit.
4. A payment over 50,000 cents ($500.00) is declined. A fare that big should never happen, so Spoke's house rule logs it at WARN.
5. A normal payment is INFO. All three lines use `{}` placeholders.

## Try it

Change the level of each line below and run it. Then add a TRACE line. Notice that TRACE never appears, because the default level is DEBUG.

```java run
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String station = "Harbor";
        int freeDocks = 1;
        log.debug("counting docks at {}", station);
        log.info("{} has {} free docks out of 12", station, freeDocks);
        if (freeDocks < 2) {
            log.warn("{} is nearly full", station);
        }
    }
}
```

## Recap

- `System.out.println` has no levels, no time and no source. Use a logger instead.
- SLF4J is the facade you call. Logback is the library that writes the lines. Get a logger with `LoggerFactory.getLogger(MyClass.class)`.
- Pick the level by importance: TRACE, DEBUG, INFO, WARN, ERROR.
- Use `{}` placeholders, and pass an exception as the last argument to get its stack trace. Guard slow arguments with `isDebugEnabled()` or a supplier.
- Never log secrets. Mask card numbers to the last 4 digits. Log an exception once, and always clear the MDC.

## Common mistakes

### 1. Logging only the exception's message

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Exception e = new IllegalStateException("dock 7 jammed");
        log.error("unlock failed: " + e.getMessage());
    }
}
```

Output:

```text
21:19:09.081 [main] ERROR Main -- unlock failed: dock 7 jammed
```

The stack trace is gone, so nobody can tell where the dock jammed. Pass the exception itself as the last argument: `log.error("unlock failed at {}", "Harbor", e);`.

### 2. Fewer values than placeholders

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("payment ok for {} card {}", "R-17");
    }
}
```

Output:

```text
21:19:09.081 [main] INFO Main -- payment ok for R-17 card {}
```

The second `{}` has no value, so it prints as is. Count the placeholders and pass one value for each.

### 3. Logging the raw card number

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String cardNumber = "4242 4242 4242 4242";
        log.info("payment ok for {} card {}", "R-17", cardNumber);
    }
}
```

Output:

```text
21:19:09.081 [main] INFO Main -- payment ok for R-17 card 4242 4242 4242 4242
```

The full card number is now in every copy of the log. Mask it before logging, so the line reads `card **** **** **** 4242`.

### 4. Logging the same exception twice

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            unlock("B-1042");
        } catch (IllegalStateException e) {
            log.error("unlock request failed", e);
        }
    }

    static void unlock(String bikeId) {
        try {
            throw new IllegalStateException("dock 7 jammed");
        } catch (IllegalStateException e) {
            log.error("unlock failed for {}", bikeId, e);
            throw e;
        }
    }
}
```

Output:

```text
10:35:04.158 [main] ERROR Main -- unlock failed for B-1042
java.lang.IllegalStateException: dock 7 jammed
	at Main.unlock(Main.java:17)
	at Main.main(Main.java:9)
10:35:04.160 [main] ERROR Main -- unlock request failed
java.lang.IllegalStateException: dock 7 jammed
	at Main.unlock(Main.java:17)
	at Main.main(Main.java:9)
```

One failure, two ERROR lines and two stack traces. In a real server each method on the way up may do this, so one jam looks like five problems. This is called **log and rethrow**. Fix: either handle the exception and log it, or rethrow it (with context) and do not log. Log it once, at the **boundary**: the top of the request, where it is finally handled.

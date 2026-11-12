## Hook

Picture a ride that starts at Harbor at 23:50 on Friday. The rider docks at Uptown at 00:20 on Saturday. That ride lasted 30 minutes.

The old kiosk stored only "minutes since midnight". Friday 23:50 became 1430. Saturday 00:20 became 20. The kiosk did 20 minus 1430 and printed a ride of minus 1410 minutes.

The problem is simple. A number of minutes forgets which day it belongs to. Java has a whole toolbox for real dates and times, called `java.time`. This lesson shows you how to use it.

## Concept

`java.time` is a package that ships with Java. A package is a folder of ready-made classes. You bring a class in with an `import` line at the top of your file.

```callout jargon
An **immutable** object never changes after you create it. Every `java.time` object is immutable. A method like `plusDays` gives you a brand-new object and leaves the old one alone.
```

### The three main classes

- `LocalDate` holds a date only: year, month, day. Example: 2026-11-14.
- `LocalTime` holds a time only: hours, minutes, seconds. Example: 23:50.
- `LocalDateTime` holds both together. Example: 2026-11-14T23:50.

"Local" means there is no time zone inside. That is fine for a kiosk where every station is in Riverton.

### Syntax: creating with of

```java nocompile
LocalDate day = LocalDate.of(year, month, dayOfMonth);
LocalTime time = LocalTime.of(hour, minute);
LocalDateTime when = LocalDateTime.of(year, month, dayOfMonth, hour, minute);
```

The `of` method is a static factory. A factory is a method that builds an object for you, in place of `new`. Months run from 1 (January) to 12 (December).

```java nocompile
LocalDate payday = LocalDate.of(2026, 11, 30);
LocalTime opening = LocalTime.of(6, 0);
LocalDateTime start = LocalDateTime.of(2026, 11, 13, 23, 50);
```

### Syntax: creating with parse

```java nocompile
LocalDate day = LocalDate.parse("2026-11-14");
LocalTime time = LocalTime.parse("23:50");
LocalDateTime when = LocalDateTime.parse("2026-11-14T23:50");
```

To parse means to read text and turn it into an object. The text must use the standard ISO layout: year-month-day, then a capital `T`, then hours:minutes. This is handy when you read a ride log line.

### Syntax: moving forward and back

```java nocompile
LocalDate later = day.plusDays(3);
LocalDate earlier = day.minusDays(1);
LocalDateTime end = start.plusMinutes(30);
LocalDate expiry = day.plusMonths(1);
```

Each `plus` or `minus` method returns a new object. Java rolls over for you. Add 30 minutes to 23:50 and you get 00:20 on the next day.

### Syntax: comparing

```java nocompile
boolean early = a.isBefore(b);
boolean late = a.isAfter(b);
boolean same = a.equals(b);
```

Use these in place of `<` and `>`. Those operators only work on numbers.

### Syntax: Duration between two moments

```java nocompile
Duration d = Duration.between(start, end);
long minutes = d.toMinutes();
```

A `Duration` is an amount of time measured in hours, minutes and seconds. `toMinutes()` gives the whole minutes as a `long`. A `long` is a whole number type that holds much bigger values than `int`.

### Syntax: Period for days and months

```java nocompile
Period p = Period.between(firstDate, secondDate);
int months = p.getMonths();
int days = p.getDays();
```

A `Period` is an amount of time in years, months and days. Use it for calendar amounts, such as "1 month and 3 days". Use `Duration` for clock amounts, such as "45 minutes".

```callout tip
Byte says: Months have different lengths. `LocalDate.of(2027, 1, 31).plusMonths(1)` gives 2027-02-28. Java picks the last valid day so you never land on 31 February.
```

### Syntax: DayOfWeek

```java nocompile
DayOfWeek dow = day.getDayOfWeek();
boolean saturday = dow == DayOfWeek.SATURDAY;
```

`DayOfWeek` is an enum, just like the enums from module 2. Its values run from `MONDAY` to `SUNDAY`. You compare enum values with `==`.

### Syntax: formatting for display

```java nocompile
DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE d MMM HH:mm", Locale.ENGLISH);
String text = when.format(fmt);
```

A formatter turns a date into text in the shape you choose. Each letter in the pattern stands for a part:

| Letter | Means | Example |
|---|---|---|
| `EEE` | short day name | Fri |
| `d` | day of month, no leading zero | 7 |
| `dd` | day of month, two digits | 07 |
| `MMM` | short month name | Nov |
| `MM` | month number, two digits | 11 |
| `yyyy` | year | 2026 |
| `HH` | hour, 00 to 23 | 23 |
| `mm` | minute | 50 |

`Locale.ENGLISH` tells Java to use English names. Without it, a laptop set to French would print "ven." for Friday.

```callout warning
Byte says: `MM` is month and `mm` is minute. Mix them up and your receipts will show the minute where the month should be.
```

## Worked example

Here is the midnight ride from the hook, done the new way.

```java run
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        LocalDateTime start = LocalDateTime.of(2026, 11, 13, 23, 50);
        LocalDateTime end = start.plusMinutes(30);
        long minutes = Duration.between(start, end).toMinutes();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE d MMM HH:mm", Locale.ENGLISH);
        System.out.println("Start: " + start.format(fmt));
        System.out.println("End:   " + end.format(fmt));
        System.out.println("Ride:  " + minutes + " min");
        System.out.println("End after start? " + end.isAfter(start));
    }
}
```

Line by line:

1. The four `import` lines bring in the classes you need.
2. `LocalDateTime.of(2026, 11, 13, 23, 50)` builds Friday 13 November at 23:50.
3. `start.plusMinutes(30)` builds a new moment 30 minutes later. It rolls into Saturday by itself.
4. `Duration.between(start, end)` measures the gap, and `toMinutes()` turns it into 30.
5. The formatter prints `Fri 13 Nov 23:50` and `Sat 14 Nov 00:20`.
6. `isAfter` confirms the end comes after the start.

Now a monthly pass. A rider buys a pass on 31 January.

```java run
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;

public class Main {
    public static void main(String[] args) {
        LocalDate bought = LocalDate.parse("2027-01-31");
        LocalDate expiry = bought.plusMonths(1);
        System.out.println("Expires: " + expiry);
        System.out.println("Weekday: " + expiry.getDayOfWeek());
        System.out.println("Sunday? " + (expiry.getDayOfWeek() == DayOfWeek.SUNDAY));
        Period length = Period.between(bought, expiry);
        System.out.println("Days covered: " + length.getDays());
    }
}
```

`plusMonths(1)` lands on 2027-02-28 because February 2027 has 28 days. Printing a `LocalDate` gives the ISO text `2027-02-28`. `Period.between` reports 0 months and 28 days.

## Try it

Change the ride below. Make it start on Sunday 15 November at 22:10 and last 2 days and 15 minutes. Use `plusDays` and `plusMinutes`. Then predict the minutes before you press Run.

```java run
import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        LocalDateTime start = LocalDateTime.parse("2026-11-14T08:00");
        LocalDateTime end = start.plusDays(1).plusMinutes(45);
        System.out.println(start + " -> " + end);
        System.out.println(Duration.between(start, end).toMinutes() + " minutes");
    }
}
```

## Recap

- `LocalDate`, `LocalTime` and `LocalDateTime` hold a date, a time, or both. Build them with `of` or `parse`.
- They are immutable. `plusDays`, `plusMonths` and `plusMinutes` return new objects and roll over for you.
- Compare with `isBefore`, `isAfter` and `equals`.
- `Duration` measures clock time. `Period` measures calendar time.
- `DateTimeFormatter.ofPattern` with `Locale.ENGLISH` shapes text for display.

## Common mistakes

**1. Ignoring the result of plus.**

```java
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        LocalDate day = LocalDate.of(2026, 11, 14);
        day.plusDays(1);
        System.out.println(day);
    }
}
```

This prints `2026-11-14`. The date did not move, because `java.time` objects never change. Fix: keep the new object with `day = day.plusDays(1);`.

**2. Using < to compare dates.**

```java nocompile
LocalDate a = LocalDate.of(2026, 11, 14);
LocalDate b = LocalDate.of(2026, 11, 15);
if (a < b) {
    System.out.println("a is first");
}
```

The compiler says `error: bad operand types for binary operator '<'`. Fix: write `if (a.isBefore(b))`.

**3. Mixing up MM and mm.**

```java
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        LocalDateTime t = LocalDateTime.of(2026, 11, 14, 9, 5);
        System.out.println(t.format(DateTimeFormatter.ofPattern("dd/mm/yyyy")));
    }
}
```

This prints `14/05/2026`. The `05` is the minute of the time. Fix: use `MM` for month, so the pattern is `"dd/MM/yyyy"` and the output is `14/11/2026`.

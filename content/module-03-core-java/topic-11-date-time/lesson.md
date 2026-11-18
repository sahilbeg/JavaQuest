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

"Local" means there is no time zone inside. A `LocalDateTime` is what a wall clock shows, not an exact moment on Earth. That is fine for showing a time on the kiosk screen. For storing and measuring real moments, you need the zone classes below.

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

`getDays()` is only the days part of a `Period`, not the total. From 15 January to 20 March 2027 is `P2M5D`: 2 months and 5 days. So `getDays()` gives `5`. For the total number of days, use `ChronoUnit.DAYS.between(from, to)`, which gives `64`. `ChronoUnit` lives in `java.time.temporal`.

```callout tip
Gojo says: Months have different lengths. `LocalDate.of(2027, 1, 31).plusMonths(1)` gives 2027-02-28. Java picks the last valid day so you never land on 31 February.
```

### Instant, ZoneId and ZonedDateTime

```java nocompile
Instant moment = Instant.parse("2026-11-14T04:50:00Z");             // one exact moment, in UTC
ZonedDateTime local = moment.atZone(ZoneId.of("America/New_York")); // the same moment on a local clock
```

- An `Instant` is one exact moment on Earth, counted in **UTC**. It looks the same in every country.
- A `ZoneId` names a time zone, such as `"America/New_York"` or `"Europe/London"`. Say Riverton uses New York time.
- A `ZonedDateTime` is a date and time plus its zone. It knows the exact moment, and it knows what the local clock shows.

```callout jargon
**UTC** is the world's reference clock. Every time zone is "UTC plus or minus some hours". The `Z` at the end of `04:50:00Z` means UTC.
```

```java run
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Main {
    public static void main(String[] args) {
        Instant unlocked = Instant.parse("2026-11-14T04:50:00Z");
        System.out.println(unlocked);

        ZonedDateTime inRiverton = unlocked.atZone(ZoneId.of("America/New_York"));
        ZonedDateTime inLondon = unlocked.atZone(ZoneId.of("Europe/London"));
        System.out.println(inRiverton);
        System.out.println(inLondon);
        System.out.println(inRiverton.toOffsetDateTime());
        System.out.println(inRiverton.toInstant().equals(inLondon.toInstant()));
    }
}
```

It prints:

```
2026-11-14T04:50:00Z
2026-11-13T23:50-05:00[America/New_York]
2026-11-14T04:50Z[Europe/London]
2026-11-13T23:50-05:00
true
```

The same unlock is 23:50 in Riverton and 04:50 the next day in London. Underneath, it is one moment, so the last line is `true`.

The rule real services follow: **store moments as `Instant`**, and convert to a zone only to show them to a person. When an API sends a time as text, use the ISO-8601 form with an offset, like `2026-11-13T23:50-05:00`. The `-05:00` says how far that clock is from UTC, so nobody has to guess.

```callout jargon
**ISO-8601** is the world standard way to write dates and times as text, such as `2026-11-14T04:50:00Z`. An **offset** like `-05:00` is the gap between a local clock and UTC.
```

### Daylight saving time

In many places the clocks jump forward one hour in spring and back in autumn. This is **daylight saving time**. In New York on 8 March 2026, the clock goes from 01:59 straight to 03:00.

```callout jargon
**Daylight saving time** (DST) is the custom of moving clocks one hour forward in spring and one hour back in autumn. Some days then have 23 hours, and some have 25.
```

A `LocalDateTime` knows nothing about this. Watch a ride that starts at 01:50 and ends at 03:10 that night.

```java run
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Main {
    public static void main(String[] args) {
        ZoneId riverton = ZoneId.of("America/New_York");
        LocalDateTime start = LocalDateTime.of(2026, 3, 8, 1, 50);
        LocalDateTime end = LocalDateTime.of(2026, 3, 8, 3, 10);

        System.out.println("Local:  " + Duration.between(start, end).toMinutes() + " min");

        ZonedDateTime zStart = start.atZone(riverton);
        ZonedDateTime zEnd = end.atZone(riverton);
        System.out.println("Zoned:  " + Duration.between(zStart, zEnd).toMinutes() + " min");
        System.out.println(zStart);
        System.out.println(zEnd);
    }
}
```

It prints:

```
Local:  80 min
Zoned:  20 min
2026-03-08T01:50-05:00[America/New_York]
2026-03-08T03:10-04:00[America/New_York]
```

The rider really rode for 20 minutes. The `LocalDateTime` version charges them for 80, because it cannot see the missing hour. Look at the offsets: `-05:00` before the jump, `-04:00` after.

```callout warning
Never measure a real ride with `LocalDateTime` across a night when the clocks change. Measure between `Instant` or `ZonedDateTime` values instead. Bugs like this show up only twice a year, which makes them hard to catch.
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
Gojo says: `MM` is month and `mm` is minute. Mix them up and your receipts will show the minute where the month should be.
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
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {
        LocalDate bought = LocalDate.parse("2027-01-31");
        LocalDate expiry = bought.plusMonths(1);
        System.out.println("Expires: " + expiry);
        System.out.println("Weekday: " + expiry.getDayOfWeek());
        System.out.println("Sunday? " + (expiry.getDayOfWeek() == DayOfWeek.SUNDAY));
        System.out.println("Days covered: " + ChronoUnit.DAYS.between(bought, expiry));
    }
}
```

`plusMonths(1)` lands on 2027-02-28 because February 2027 has 28 days. Printing a `LocalDate` gives the ISO text `2027-02-28`. `ChronoUnit.DAYS.between` counts the total days: 28.

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

- `LocalDate`, `LocalTime` and `LocalDateTime` hold a date, a time, or both, with no zone. Build them with `of` or `parse`.
- They are immutable. `plusDays`, `plusMonths` and `plusMinutes` return new objects. Compare with `isBefore` and `isAfter`.
- Store real moments as `Instant` in UTC. Use `ZonedDateTime` for a local clock, and to measure safely across daylight saving changes.
- `Duration` measures clock time. `Period` measures calendar time. `ChronoUnit.DAYS.between` counts total days.
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

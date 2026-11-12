## What happened

The kiosk stores times as **minutes since midnight**. 00:00 is 0, 09:30 is 570, and 23:50 is 1430. That makes subtraction easy, until a ride crosses midnight.

A ride from 23:50 to 00:20 should last 30 minutes. The kiosk computes `20 - 1430` and gets `-1410`. Riders then see a negative fare.

A second bug hides in the pricing. Members pay the unlock fee, and they should not.

```callout analogy
Think of a clock face. A ride from 23:50 to 00:20 goes round past midnight. It does not go backwards 23 and a half hours.
```

## What you need

Everything here comes from module 1.

- `%` gives the remainder. `1450 % 1440` is `10`.
- `if` and `else` pick the member rule.
- `switch` picks the price per minute.
- `String.format` builds the receipt line.

### Syntax: wrapping round midnight

```java
int minutesInDay = 1440;
int start = 1430;
int end = 20;
int minutes = (end - start + minutesInDay) % minutesInDay;
```

Adding 1440 makes the number positive. The `%` then removes any extra full day.

### Syntax: a time with two digits

```java
int minuteOfDay = 5;
String time = String.format("%02d:%02d", minuteOfDay / 60, minuteOfDay % 60);
```

`%02d` prints a whole number with at least two digits, filling with a zero. This gives `00:05`.

## The tiers

1. **Bronze** resolves the incident: `minutesBetween(start, end)` is right for normal rides and for rides across midnight.
2. **Silver**: `fare(start, end, type, member)` follows every pricing rule. Non-members pay the $1.00 unlock fee plus the price per minute. Members ride free for 30 minutes, then pay per minute, with no unlock fee. Unknown bike types cost `-1`.
3. **Gold**: `receipt(start, end, type, member)` returns one line like `23:50-00:20 30 min $5.50`.

## Recap

- Times as minutes since midnight wrap round at 1440.
- `(end - start + 1440) % 1440` gives the minutes between two times.
- Pricing rules belong in one place, so one fix reaches every receipt.

## Common mistakes

**Only fixing the midnight case.** `if (end < start) end = end + 1440;` works too. Check that a normal ride, like 600 to 615, still gives 15.

**Rounding the fare inside the maths.** Keep the fare a `double`. Format it with `%.2f` only when you build the text.

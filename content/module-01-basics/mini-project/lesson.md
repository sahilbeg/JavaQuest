## The design

When a rider docks a bike, the kiosk prints this receipt:

```text
SPOKE RIVERTON
Bike     B-1042
From     Riverside
Type     ebike
Time     25 min
Fare     $8.50
Thank you for riding!
```

Each middle line has a label padded to 9 characters, then the value. The fare always has two decimals.

```callout tip
This feature uses almost every tool from module 1: variables, operators, if, switch, methods, StringBuilder and String.format.
```

## What you build

`ReceiptPrinter` has three methods.

1. `pricePerMinute(type)` returns 0.15, 0.30 or 0.25 for `classic`, `ebike` and `cargo`, and -1 for anything else.
2. `fare(type, minutes, member)` uses the fare rules. Non-members pay the $1.00 unlock fee plus every minute. Members ride 30 minutes free, then pay per minute, with no unlock fee.
3. `receipt(bikeId, station, type, minutes, member)` returns the seven lines above, joined with `\n`. For an unknown type it returns exactly `Unknown bike type`.

### Syntax: one padded line

```java
String line = String.format("%-9s%s", "Bike", "B-1042");
```

`%-9s` adds spaces after the label until it is 9 characters long. The value then starts at column 10.

### Syntax: joining lines

```java
StringBuilder text = new StringBuilder();
text.append("SPOKE RIVERTON").append("\n");
text.append("Thank you for riding!");
String receipt = text.toString();
```

## Recap

- Split the work into small methods that each do one job.
- Pad labels with `%-9s` so the values line up.
- Money uses `%.2f` and a `$` in front.

## Common mistakes

**A newline after the last line.** The receipt ends with `Thank you for riding!` and no `\n` after it.

**Charging members the unlock fee.** Members never pay the $1.00 fee. A 40 minute member ride on an e-bike costs 10 x 0.30 = $3.00.

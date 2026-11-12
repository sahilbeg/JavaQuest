## The design

Spoke sells three memberships.

| Level | Unlock fee | Free minutes per ride |
|---|---|---|
| BASIC | $1.00 | 0 |
| PLUS | none | 30 |
| PRO | none | 60 |

Prices per minute stay the same: classic 0.15, e-bike 0.30, cargo 0.25. Free minutes come first. Every minute after them costs the bike's price.

At the end of the month a rider gets this statement:

```text
Statement for Ada (PLUS)
EBIKE      40 min   $3.00
CLASSIC    20 min   $0.00
CARGO      90 min  $15.00
Total              $18.00
```

```callout tip
This feature joins most of module 2: enums with fields, a record, a class with private fields, and an array of objects.
```

## What you build

All of these are in package `jq.m02.feature`.

1. `enum BikeType` with `CLASSIC`, `EBIKE` and `CARGO`, and `pricePerMinute()`.
2. `enum Membership` with `BASIC`, `PLUS` and `PRO`, plus `freeMinutes()` and `paysUnlockFee()`.
3. `record Ride(BikeType type, int minutes)`.
4. `class Rider` with a name, a level, and room for 50 rides:
   - `recordRide(Ride ride)` stores a ride, and ignores it when the rider already has 50.
   - `rideCount()` returns how many rides are stored.
   - `upgrade(Membership level)` changes the level. The statement prices every stored ride at the current level.
   - `fareFor(Ride ride)` applies the rules in the table.
   - `monthTotal()` adds up every stored ride. Loop `i` from 0 up to `count`: the other slots of the array are still `null`.
   - `statement()` returns the text above.

### Syntax: the statement lines

```java nocompile
String header = String.format("Statement for %s (%s)", name, level);
String line = String.format("%-9s%4d min %7s", ride.type(), ride.minutes(), money);
String total = String.format("%-18s%7s", "Total", money);
```

`money` is text like `$3.00`, made with `String.format("$%.2f", amount)`. Join the lines with `\n`, with no newline after the total.

## Recap

- Enums hold the fixed lists and their numbers in one place.
- A record is the right shape for a ride: plain data that never changes.
- `Rider` keeps its rides private and offers methods to add and add up.

## Common mistakes

**Charging free minutes.** A PLUS rider's 20 minute ride is free, so the fare is `$0.00`. The paid minutes are never negative.

**Charging PLUS and PRO riders the unlock fee.** Only BASIC pays the $1.00 fee.

**Looping over the whole array.** `for (Ride r : rides)` visits all 50 slots, and the empty ones are `null`. Calling `r.minutes()` on `null` crashes with a `NullPointerException`. Loop up to `count` instead.

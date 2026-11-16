## What happened

Every `EBike` ride is billed at 0.15 per minute, the classic price. Every `CargoBike` ride is too. The subclasses seem to set the right prices, so nobody spotted it in review.

Both bugs are classic inheritance traps. Each one compiles, runs and gives no error. That is what makes them dangerous.

```callout analogy
Picture a price sticker stuck on the wrong side of a bike. The bike has a sticker, but the kiosk reads the side with the old one.
```

## What you need

### Syntax: overriding versus overloading

```java nocompile
class Bike {
    double pricePerMinute() { return 0.15; }
}

class EBike extends Bike {
    // Overloading: a NEW method with different parameters. Bike's version still runs.
    double pricePerMinute(int minutes) { return 0.30; }

    // Overriding: the SAME method signature. This one replaces Bike's version.
    @Override
    double pricePerMinute() { return 0.30; }
}
```

A method's **signature** is its name plus its parameter types. `@Override` asks the compiler to check that you really replace a parent method. If the signature does not match, it refuses to compile.

### Syntax: a hidden field

```java nocompile
class Bike {
    protected double price = 0.15;
    double pricePerMinute() { return price; }
}

class CargoBike extends Bike {
    protected double price = 0.25;   // a second, separate field that hides Bike's
}
```

A `CargoBike` now holds two fields called `price`. Watch which one the fare reads:

```animate
title: Two price fields in one bike
kind: code
code: |
  public class Main {
      public static void main(String[] args) {
          Bike b = new CargoBike();
          System.out.println(b.pricePerMinute());
      }
  }
  class Bike {
      protected double price = 0.15;
      double pricePerMinute() { return price; }
  }
  class CargoBike extends Bike {
      protected double price = 0.25;
  }
steps:
  - line: 3
    vars: { b: "@c1" }
    objects: { c1: { type: CargoBike, fields: { "Bike's price": 0.15, "CargoBike's price": 0.25 } } }
    say: new CargoBike() builds one object. Inside it sit two separate fields called price, one from Bike and one from CargoBike.
  - line: 4
    say: pricePerMinute() is written only in Bike, so Bike's method runs.
  - line: 9
    say: Inside Bike's method, price means Bike's own field. It cannot see CargoBike's field, so it returns 0.15.
  - line: 4
    out: "0.15"
    say: The cargo ride is billed at the classic price. That is the incident. Fields are not overridden; only methods are.
```

Delete the `price` field from `CargoBike`. Then, in its constructor after `super(id);`, write `price = 0.25;`. That sets the field `CargoBike` inherited from `Bike`.

```callout tip
Fields are not overridden. Only methods are. When a subclass needs a different value, set the parent's field or override a method.
```

## The tiers

1. **Bronze** resolves the incident: an `EBike` fare uses 0.30 per minute, even through a `Bike` variable.
2. **Silver**: a `CargoBike` fare uses 0.25 per minute, even through a `Bike` variable.
3. **Gold**: `Billing.invoice(bikes, minutes)` returns one line per ride and a total line, like this:

```text
B-2001 ebike     10 min  $4.00
B-3001 cargo     20 min  $6.00
TOTAL                   $10.00
```

Each ride line is `String.format("%-7s%-9s%3d min %6s", id, type, minutes, money)`, where money is like `$4.00`. The total line is `String.format("%-23s%7s", "TOTAL", money)`. Lines are joined with `\n`, with no newline at the end.

## Recap

- Overloading adds a method with different parameters. Overriding replaces a method with the same signature.
- `@Override` turns a silent overloading mistake into a compile error.
- Fields are not polymorphic. A subclass field with the same name hides the parent's field.

## Common mistakes

**Fixing only `EBike`.** Run the silver checks too. The cargo bug is a different trap.

**Calling the subclass method directly in the test of your fix.** `new EBike("B-1").pricePerMinute(10)` returns 0.30 even with the bug. The invoice calls `fare` on `Bike` variables, so test through a `Bike` variable.

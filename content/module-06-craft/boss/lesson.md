## What happened

`LegacyBilling` is old, long and ugly, but its numbers have been right for years. Last sprint someone rewrote it as `Billing`: shorter, with named constants. The few existing tests still pass, so the change went out.

It changed three behaviours. Nobody noticed, because no test pinned them down.

```callout analogy
Refactoring is like rebuilding a bike's wheel. It may look better afterwards, but it still has to roll exactly as straight as before.
```

## What you need

This builds on topic 06 (refactoring safely) and topic 08 (JUnit).

### Syntax: a characterization test

```java nocompile
for (Membership membership : Membership.values()) {
    for (int minutes = 0; minutes <= 90; minutes++) {
        Ride ride = new Ride("B-1042", "classic", minutes);
        assertEquals(LegacyBilling.fareCents(ride, membership), Billing.fareCents(ride, membership),
                membership + ", " + minutes + " min");
    }
}
```

A characterization test pins down what the old code does today, right or wrong. The new code must match it exactly.

### Syntax: money in whole cents

```java nocompile
long cents = minutes * centsPerMinute;   // 3 * 15 = 45, exactly
```

A `double` cannot hold 0.15 exactly. `(long) (3 * 0.15 * 100)` gives 44.

## The tiers

1. **Bronze** resolves the incident: `Billing.fareCents` charges the unlock fee only to BASIC riders, as the legacy code does.
2. **Silver**: every fare in cents matches `LegacyBilling` for every bike type, membership and ride length from 0 to 90 minutes.
3. **Gold**: `Billing.invoice(rides, membership)` gives the same text as the legacy code and leaves the caller's list in its original order.

Do not change `LegacyBilling`. It is the reference.

## Recap

- Pin the current behaviour with tests before you refactor.
- Keep money in whole cents, as `long`.
- A method that changes its arguments surprises every caller.

## Common mistakes

**Fixing the numbers with Math.round.** Rounding hides the symptom for these cases. Whole cents remove the cause.

**Updating the expected values.** The legacy output is the specification here. Change the new code until it matches.

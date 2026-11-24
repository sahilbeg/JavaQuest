## What happened

The old WhatsApp calculator was written in a hurry, long before you joined. It keeps every amount in rupees, as decimals: a tote bag is `899`, and a discount might be `229.8`.

Mrs. Kapoor's Diwali order was two tote bags (₹1,798), baby booties (₹350) and a coaster set (₹150). That's ₹2,298. The Diwali offer takes 10% off orders above ₹1,500, so ₹229.80 comes off, and she owes ₹2,068.20. In paise, that's `206820`.

The old code does the offer in rupees, then turns the answer into paise for UPI. Run its maths yourself:

```js run
const subtotal = 2298;
const total = subtotal - subtotal * 0.1;
console.log(total);
console.log(total * 100);
console.log(Math.floor(total * 100));
```

The first line looks fine: `2068.2`. But times 100, the stored guess comes out a hair below the real 206820, at `206819.99999999997`. `Math.floor` then chops it down to `206819`. One paisa lost, on every order like this one.

```callout analogy
It's like a ruler with slightly worn markings. One measurement looks right. But measure a hundred orders, and the total drifts away from the truth.
```

The receipt has a second problem: it shows amounts like `₹2068.19`, with no comma after the thousands.

## What you need

Everything here comes from this project:

- Money is stored in whole paise, never as rupee decimals ([Numbers and paise](/t/web-03-javascript-basics/topic-02-numbers-and-paise)).
- `Math.round` goes to the nearest whole number, and `Math.floor` rounds down.
- "Above ₹1,500" means `>`, not `>=` ([Booleans and comparison](/t/web-03-javascript-basics/topic-04-booleans-and-comparison)).
- `Intl.NumberFormat` shows rupees the Indian way ([Showing rupees](/t/web-03-javascript-basics/topic-11-formatting-money)).

### Syntax: 10% off, rounded down

```js
const discount = Math.floor(subtotalPaise / 10);
```

10% is one tenth, so divide the paise by 10. When the answer isn't whole, `Math.floor` rounds it down, so Dhaaga never gives away a part of a paisa. Watch it on an order of ₹1,500.19:

```animate
title: The Diwali offer, in whole paise
kind: code
language: js
code: |
  const subtotal = 150019;
  const exact = subtotal / 10;
  const discount = Math.floor(exact);
  const total = subtotal - discount;
  console.log(discount);
  console.log(total);
steps:
  - line: 1
    vars: { subtotal: 150019 }
    say: An order of ₹1,500.19, stored as whole paise. It's above ₹1,500, so the offer applies.
  - line: 2
    vars: { exact: '15001.9' }
    say: One tenth of it is 15001.9 paise. Nobody can pay nine tenths of a paisa.
  - line: 3
    vars: { discount: 15001 }
    say: Math.floor rounds down to 15001 paise. The offer never gives away more than 10%.
  - line: 4
    vars: { total: 135018 }
    say: Whole paise minus whole paise. The answer is exact, with no tiny guesses left over.
  - line: 5
    out: "15001"
    say: The discount, in paise.
  - line: 6
    out: "135018"
    say: The total, in paise. Only the receipt turns it into rupees.
```

### Syntax: the nearest paisa

```js
const paise = Math.round(rupees * 100);
```

Some amounts in the old sheet are still typed in rupees. Multiplying by 100 can land just below or just above the right number, so round to the nearest. Never round down here.

## The tiers

1. **Bronze** resolves the incident. `PRICES` holds every price in whole paise, like `CST: 15000`. `toPaise(rupees)` turns an amount from the old sheet into the nearest whole paisa: `2068.2` gives `206820`. `cartTotal(lines)` adds up whole paise and takes off the Diwali offer, so Mrs. Kapoor's order comes to exactly `206820`.
2. **Silver**: `diwaliDiscount(subtotalPaise)` gives 10% off only **above** `150000` paise, rounded **down**: `150000` gives `0`, and `150019` gives `15001`. `cartTotal` uses it.
3. **Gold**: `formatRupees(paise)` shows paise as rupees with `Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" })`: `206820` gives `"₹2,068.20"`. `totalLine(lines)` then reads `"Total: ₹2,068.20"`.

## Recap

- Rupee decimals are stored as close guesses, and the errors grow as you do maths with them.
- Keep every amount in whole paise, and do every sum with whole paise.
- When rupees must become paise, round to the nearest with `Math.round`.
- A discount rounds down with `Math.floor`, so the shop never gives away part of a paisa.
- Format rupees only for the screen, with the `"en-IN"` formatter.

## Common mistakes

**1. Rounding at the end instead of storing paise.** Changing `Math.floor` to `Math.round` in `toPaise` hides this one order's error. But `PRICES` would still hold rupees, and the next sum could drift again. Bronze checks that every price is whole paise.

**2. Giving the offer at exactly ₹1,500.** The old code uses `>=`. "Above ₹1,500" means `subtotalPaise > 150000`, so an order of exactly `150000` paise pays full price.

**3. Rounding the discount to the nearest.** `Math.round(150005 / 10)` is `15001`, half a paisa more than 10%. Use `Math.floor`, which gives `15000`.

**4. Formatting paise as rupees.** `inr.format(206820)` shows `₹2,06,820.00`, a hundred times too much. Divide by 100 first.

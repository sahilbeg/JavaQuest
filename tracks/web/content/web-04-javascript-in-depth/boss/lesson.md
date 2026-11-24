## What happened

The Diwali offer is 10% off orders above ₹1,500. The discount is rounded down to whole paise. On the first sale morning, three things went wrong:

1. A cart with two lines got 10% off, then 10% off again. Three lines got it three times.
2. The offer function wrote its answer into the cart object itself. The cart is shared by the cart page, the checkout and the WhatsApp summary, so they all saw a changed cart.
3. Each time Mrs. Rao opened her cart, the page ran the offer again on the already-discounted total, and the price dropped further.

Here is the first bug, step by step, on Mrs. Rao's cart: two tote bags and a coaster set, ₹1,948.00.

```animate
title: One offer, applied once per line
kind: code
language: js
code: |
  const cart = { totalPaise: 194800, lines: ["BAG x2", "CST x1"] };
  for (const line of cart.lines) {
    if (cart.totalPaise > 150000) {
      cart.totalPaise = cart.totalPaise - Math.floor(cart.totalPaise / 10);
    }
  }
  console.log(cart.totalPaise);
steps:
  - line: 1
    objects: { o1: { type: Object, fields: { totalPaise: 194800, lines: '["BAG x2", "CST x1"]' } } }
    vars: { cart: "@o1" }
    say: Mrs. Rao's cart has two lines and comes to 194800 paise, which is ₹1,948.00.
  - line: 2
    vars: { line: '"BAG x2"' }
    say: The loop starts with the first line, the two tote bags.
  - line: 4
    objects: { o1: { type: Object, fields: { totalPaise: 175320, lines: '["BAG x2", "CST x1"]' } } }
    say: The total is above ₹1,500, so 10% comes off. ₹1,753.20 is the right answer, but the loop isn't finished.
  - line: 2
    vars: { line: '"CST x1"' }
    say: The loop moves on to the coaster set.
  - line: 4
    objects: { o1: { type: Object, fields: { totalPaise: 157788, lines: '["BAG x2", "CST x1"]' } } }
    say: ₹1,753.20 is still above ₹1,500, so another 10% comes off. This is the bug.
  - line: 7
    vars: { line: ~ }
    out: "157788"
    say: She would pay ₹1,577.88 instead of ₹1,753.20. And the cart object itself now holds the wrong total.
```

## What you need

Everything here comes from this project.

### Syntax: the total before any discount

```js
const before = cart.lines.reduce((sum, line) => sum + line.paise * line.qty, 0);
```

The lines never change when an offer is applied, so they always give the price before any discount. `totalPaise` might already be discounted.

### Syntax: a new cart instead of a changed one

```js
const offered = { ...cart, discountPaise: 19480, totalPaise: 175320 };
```

Spread copies every field of the cart into a new object, then the new values replace two of them. The cart that was passed in stays exactly as it was. The new cart may share the same `lines` array, as long as nobody changes it.

### Doing it twice must equal doing it once

A page may run the offer every time it draws the cart. So applying it to a cart that already has the offer must give the same answer as applying it once.

```callout jargon
An operation is **idempotent** when doing it twice gives the same result as doing it once. Pressing a lift button twice doesn't call two lifts.
```

### What the cart looks like

```js
const cart = {
  id: "order-17",
  lines: [
    { code: "BAG", paise: 89900, qty: 2 },
    { code: "CST", paise: 15000, qty: 1 },
  ],
  totalPaise: 194800,
};
```

After the offer, the cart also has `discountPaise`, and `totalPaise` is what the customer pays. A cart that doesn't qualify gets `discountPaise: 0` and its full total.

## The tiers

The fix goes in `src/diwali.js`, in `applyDiwaliOffer(cart)`.

1. **Bronze** resolves the incident: the offer is applied once per order, however many lines the cart has. Mrs. Rao's ₹1,948.00 cart gets `discountPaise` 19480 and `totalPaise` 175320. A cart of ₹1,500.00 or less gets `discountPaise` 0 and keeps its total.
2. **Silver**: the cart passed in is never changed. `applyDiwaliOffer` returns a new cart object, with the same `id` and `lines`.
3. **Gold**: calling `applyDiwaliOffer` again on its own result changes nothing. That includes a ₹1,589.00 cart, which drops below ₹1,500 after its discount but must keep it.

## Recap

- An offer is decided once per order, outside any loop over the lines.
- Never write the answer into an object other code is holding. Return a new object made with spread.
- Work from data that never changes, like the lines, not from a total that may already be discounted. Then repeat calls are safe.

## Common mistakes

**Fixing the loop and stopping there.** Bronze passes, but the cart object still changes. The next screen that runs the offer discounts it again.

**Copying the cart but reading the discounted total.** `{ ...cart, totalPaise: cart.totalPaise - discount }` doesn't change the input, but a second call takes another 10% off. Add up the lines instead.

**Checking ₹1,500 against the discounted price.** A ₹1,589.00 cart becomes ₹1,430.10. If a second call checks that against ₹1,500, the customer loses the offer.

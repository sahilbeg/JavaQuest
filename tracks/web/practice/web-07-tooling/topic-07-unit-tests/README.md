# WEB-0707: Free delivery broke twice, and customers found it first

Write the delivery fee again, and a table of test cases strong enough to catch the mistakes that broke it.

## The rule

- An empty cart (subtotal `0`) pays no delivery.
- Up to and including ₹999 (99900 paise), delivery inside Pune is ₹60: `6000` paise.
- Above ₹999, delivery is free: `0`.
- A subtotal that is negative, or not a whole number of paise (like `450.5`), throws a `RangeError`.

## What to do

1. In `src/delivery.js`, write `deliveryFee(subtotalPaise)` (TODO 1 and TODO 2). Use the constants `DELIVERY_PAISE` and `FREE_ABOVE_PAISE`.
2. In `src/deliveryCases.js`, fill the `cases` array (TODO 3). Each case is an object:

   ```js
   { name: "exactly ₹999 still pays ₹60", subtotal: 99900, expected: 6000 }
   ```

   `name` is a sentence, `subtotal` is whole paise (0 or more), and `expected` is the fee in paise.

## What the checks do

- `test/deliveryFee.test.js` tests your `deliveryFee` directly.
- `test/yourCases.test.js` runs your cases. It wants at least four. Every case must pass on the correct fee and on your own `deliveryFee`.
- It then runs your cases against five broken versions, in `test/brokenVersions.js`. Each broken version must fail at least one of your cases:
  - free from exactly ₹999 (`>=` instead of `>`);
  - ₹60 written as `60` instead of `6000`;
  - never free;
  - ₹60 charged for an empty cart;
  - free above ₹99.90 (a zero missing from 99900).

Read the two test files: they are examples of how Vitest tests are written.

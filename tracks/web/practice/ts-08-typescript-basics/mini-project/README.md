# FEAT-08: The typed price calculator

The price calculator, rebuilt in TypeScript across four modules:

- `src/prices.ts`: item codes, prices and fees, written once with `as const`.
- `src/types.ts`: `CartLine`, `Order` and `Quote`, plus `PaymentMode` and `Delivery`.
- `src/money.ts`: `formatRupees`, already done.
- `src/calculator.ts`: the seven functions you fill in.

Every amount is whole paise.

1. **TODO 1:** `parseCode(text)`: trimmed and upper-cased, the matching code, or `null`.
2. **TODO 2:** `itemsTotal(lines)`: price times quantity for every line.
3. **TODO 3:** `extrasTotal(lines)`: per item, ₹30 gift wrap and ₹50 for a name, times the quantity.
4. **TODO 4:** `diwaliDiscount(subtotal)`: 10% rounded down, only above `150000`.
5. **TODO 5:** `deliveryFee(subtotal, delivery)`: `0` for `"pickup"`. For `"pune"`, `0` above `99900`, and `6000` otherwise.
6. **TODO 6:** `quote(order)`: `{ items, extras, subtotal, discount, delivery, total }`, with `subtotal = items + extras` and `total = subtotal - discount + delivery`.
7. **TODO 7:** `receipt(order)`: the quote text, rows joined with `"\n"`:

```text
Dhaaga & Co. price quote
BAG x 2: ₹1,798.00
BTY x 1: ₹350.00
Extras: ₹30.00
Subtotal: ₹2,178.00
Diwali offer: -₹217.80
Delivery: FREE
Total: ₹1,960.20
Payment: UPI
```

- One row per cart line, with that line's item cost, without extras.
- `Extras:` and `Subtotal:` always appear. `Diwali offer:` appears only when there's a discount.
- `Delivery:` says `Pickup at the stall` for a pickup, `FREE` for free delivery, or the fee, like `₹60.00`.
- The last row is `Payment:` and the mode, with no newline after it.

Press **Submit** to run the checks.

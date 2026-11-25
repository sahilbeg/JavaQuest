# Union and literal types

`src/payments.ts` adds up the day's payments at the counter. A payment's `mode` can only be `"CASH"`, `"UPI"` or `"CARD"`. Fill in the five functions. Every amount is whole paise.

1. **TODO 1:** `modeLabel(mode)` gives `"Cash"`, `"UPI"` or `"Card"`.
2. **TODO 2:** `orderLabel(id)` takes a number or text: `17` and `"17"` both give `"order-17"`, and `"order-17"` stays as it is. `String(id)` works for both.
3. **TODO 3:** `totalFor(payments, mode)` adds up the paise of the payments made with that mode.
4. **TODO 4:** `dayTotals(payments)` returns `{ CASH, UPI, CARD }` with the total paise for each mode, and `0` for a mode nobody used.
5. **TODO 5:** `readMode(typed)` trims and upper-cases what Raju typed. It returns `"CASH"`, `"UPI"` or `"CARD"` when it matches one, and `"OTHER"` when it doesn't.

Press **Submit** to run the checks.

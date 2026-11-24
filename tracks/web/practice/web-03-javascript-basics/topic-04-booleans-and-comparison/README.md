# Booleans and comparison: === and its friends

Make the checkout's checks in `src/checks.js` tell the truth. Each one returns `true` or `false`.

1. **TODO 1:** `isFreeDelivery(subtotalPaise)` is `true` above ₹999 (`99900` paise). Exactly ₹999 still pays for delivery.
2. **TODO 2:** `isWholePaise(value)` is `true` only for a whole number, like `15000`. Text like `"15000"` and `150.5` give `false`.
3. **TODO 3:** `sameQuantity(typed, qty)` turns the typed text, like `"2"`, into a number and compares it with `qty` using `===`.
4. **TODO 4:** `isBlankNote(note)` is `true` when the gift note is empty or only spaces.

Press **Submit** to run the checks.

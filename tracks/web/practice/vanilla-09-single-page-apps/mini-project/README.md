# FEAT-09: Checkout as a single-page app

Turn the checkout into four screens, each with its own address: `/checkout/cart`, `/checkout/details`, `/checkout/pay` and `/checkout/done`. Everything goes in `src/checkout.js`. `src/steps.js` (the four screens) and `src/store.js` are finished.

The state looks like `{ lines: [{ id, qty }], details: { name, phone, pin }, paid }`, and prices are in `PRICES`, in paise.

**Money**

1. **TODO 1:** `subtotal(lines)` adds up `PRICES[id] × qty`.
2. **TODO 2:** `deliveryFee(subtotal)` is `6000` (₹60) up to and including `99900` (₹999), `0` above it, and `0` for an empty cart.
3. **TODO 3:** `orderTotal(lines)` is the subtotal plus its delivery fee.

**Which steps are open**

4. **TODO 4:** `detailsErrors(details)` gives a message for each field that fails, or `{}`. Trim each value first. An empty field fails too, with the same message:
   - `name` is empty: `"Please tell us your name."`
   - `phone` doesn't match `/^\d{5} ?\d{5}$/`: `"A phone number has 10 digits, like 98765 43210."`
   - `pin` doesn't match `/^[1-9]\d{5}$/`: `"A PIN code has 6 digits, like 411004."`
5. **TODO 5:** `furthestStep(state)`: `"done"` once paid, `"cart"` while the cart is empty, `"details"` while the details have errors, otherwise `"pay"`.
6. **TODO 6:** `canVisit(state, step)`: never for a name not in `STEPS`; only `"done"` once paid; otherwise any step up to the furthest one.

**The router**

7. **TODO 7:** in `show()`, a step that `canVisit` refuses becomes the furthest step, and the address is swapped with `history.replaceState`.
8. **TODO 8:** draw `STEP_VIEWS[step](context)` in the outlet, and set `document.title` to `Checkout: <step title> | Dhaaga & Co.` (titles are in `STEP_TITLES`).
9. **TODO 9:** `go(step)` pushes `/checkout/<step>`, unless that is already the address, then calls `show()`.
10. **TODO 10:** `start()` shows the step once and listens for `popstate`.
11. **TODO 11:** `stop()` removes that listener.

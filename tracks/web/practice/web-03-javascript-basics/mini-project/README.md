# FEAT-03: The price and delivery calculator

Write the calculator in `src/calculator.js`. A cart is an array of lines like `{ code: "BTY", qty: 1, giftWrap: true, name: "Riya" }`, where `giftWrap` and `name` are optional. Every amount is whole paise.

1. **TODO 1:** `formatRupees(paise)` shows paise as rupees with `inr`, like `"₹1,299.00"`.
2. **TODO 2:** `itemsTotal(lines)` adds up price times `qty` for every line.
3. **TODO 3:** `extrasTotal(lines)` adds `GIFT_WRAP` (3000) for a wrapped item and `NAME_STITCHED` (5000) for a named item, for every item in the line's `qty`.
4. **TODO 4:** `diwaliDiscount(subtotal)` is 10% rounded down, only above `150000`.
5. **TODO 5:** `deliveryFee(subtotal)` is `0` above `99900`, and `DELIVERY` (6000) otherwise.
6. **TODO 6:** `quote(lines)` returns `{ items, extras, subtotal, discount, delivery, total }`, where `subtotal` is items plus extras and `total` is subtotal minus discount plus delivery.
7. **TODO 7:** `receipt(lines)` returns the quote text from the KB page, rows joined with `"\n"`. The `Diwali offer:` row appears only when there's a discount, and `Delivery:` says `FREE` or `₹60.00`.

Press **Run** to see what `main.js` prints. Press **Submit** to run the checks.

# Modules: import and export

The price code is split into three modules. Finish wiring them together. `main.js` uses them, so press **Run** to see the receipt.

**`src/prices.js`**

1. **TODO 1:** Add a named export `DELIVERY_PAISE`, set to `6000` (₹60 delivery inside Pune).
2. **TODO 2:** `priceOf(code)` returns the price of `code` from `PRICES`, or `0` when the code isn't on the list.

**`src/format.js`**

3. **TODO 3:** `formatter` is a helper for this file only. Stop exporting it, so the module's only export is its default, `rupees`.
4. **TODO 4:** `rupees(paise)` returns the amount in rupees, like `"₹150.00"` or `"₹1,798.00"`. Use `formatter.format(paise / 100)`.

**`src/receipt.js`**

5. **TODO 5:** `receiptLine(code, qty)` returns a line like `"2 x CST = ₹300.00"`, using `prices.priceOf` and `rupees`.
6. **TODO 6:** `deliveryLine(subtotalPaise)` returns `"Delivery: ₹60.00"`, using `prices.DELIVERY_PAISE`. When the subtotal is above ₹999 (99900 paise), it returns `"Delivery: free"`.

Press **Submit** in Cadence, or just save the files in your editor. The checks run on their own.

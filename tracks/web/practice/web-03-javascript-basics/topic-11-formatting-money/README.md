# Showing rupees: toFixed and Intl.NumberFormat

Make prices read the way Indian customers expect, in `src/money.js`. Every amount comes in as whole paise.

1. **TODO 1:** `plainRupees(paise)` uses `toFixed` to give rupees with two decimals: `15050` gives `"150.50"`.
2. **TODO 2:** `formatINR(paise)` uses `Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" })`: `129900` gives `"₹1,299.00"`, and `12345678` gives `"₹1,23,456.78"`.
3. **TODO 3:** `priceTag(name, paise)` returns a tag like `"Tote bag: ₹899.00"`.
4. **TODO 4:** `receiptRow(label, paise)` pads the label with spaces at the end to 12 characters, then adds `formatINR(paise)` padded at the start to 10 characters. Every row is 22 characters long.

Press **Run** to see what `main.js` prints. Press **Submit** to run the checks.

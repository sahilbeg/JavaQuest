# Fixed choices: as const and literal unions

`src/catalogue.ts` writes Dhaaga's item codes, prices and payment modes once, with `as const`, and gets the types `ItemCode` and `PaymentMode` from them. Fill in the five functions. Every amount is whole paise.

1. **TODO 1:** `parseCode(text)` trims and upper-cases the text, and returns the matching code from `ITEM_CODES`, or `null`.
2. **TODO 2:** `priceOf(code)` returns the code's price from `PRICES`.
3. **TODO 3:** `parseMode(text)` does the same as `parseCode`, for `PAYMENT_MODES`.
4. **TODO 4:** `priceBoard()` returns one line per code, in `ITEM_CODES` order: `"CST ₹150.00"`, `"KEY ₹120.00"`, and so on.
5. **TODO 5:** `codesUpTo(maxPaise)` returns the codes whose price is at most `maxPaise`, in `ITEM_CODES` order.

`ITEM_CODES.find(...)` gives back an `ItemCode` or `undefined`, so `?? null` makes it the return type. Press **Submit** to run the checks.

# Typed functions

Fill in the five typed helpers in `src/pricing.ts`. Their parameter and return types are already written. Every amount is whole paise.

1. **TODO 1:** `lineTotal(paise, qty)` is the price of one item times the quantity.
2. **TODO 2:** `withExtras(paise, giftWrap?, name?)` adds `GIFT_WRAP` (₹30) when `giftWrap` is `true`, and `NAME_STITCHED` (₹50) when `name` is given and not empty. Both are optional.
3. **TODO 3:** `splitBill(totalPaise, people = 2)` is each share, rounded down to whole paise. Without `people`, it splits between 2.
4. **TODO 4:** `sumAll(...amounts)` adds up every amount passed in, and gives `0` for none.
5. **TODO 5:** `applyOffer(paise, offer)` calls `offer(paise)` to learn how many paise come off, and returns what's left, never below `0`.

Keep the types as they are: your code is type-checked before the checks run. Press **Submit** to run the checks.

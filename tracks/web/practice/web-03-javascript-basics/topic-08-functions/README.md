# Functions: parameters and return values

Give the calculator one small function per job in `src/extras.js`. Gift wrap is `3000` paise and a stitched name is `5000` paise, for each item.

1. **TODO 1:** give `qty` in `lineTotal(paise, qty)` a default of `1`, then return `paise` times `qty`.
2. **TODO 2:** `extrasFor(giftWrap, name)` returns the extras for one item: `3000` for gift wrap, plus `5000` when `name` has text. A name of `""` or a missing name means no stitching.
3. **TODO 3:** `itemCost(paise, qty, giftWrap, name)` calls `lineTotal` and `extrasFor`. Extras are for each item, so multiply them by `qty` too.
4. **TODO 4:** make the arrow function `deliveryFor(subtotalPaise)` return `0` above `99900` paise, and `6000` otherwise.

Press **Run** to see what `main.js` prints. Press **Submit** to run the checks.

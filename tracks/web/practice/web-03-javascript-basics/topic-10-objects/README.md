# Objects

Keep the price list in one object in `src/catalogue.js`. Each code points to an object with the item's `name` and its price in `paise`.

1. **TODO 1:** add `BTY` to `CATALOGUE`: `"Baby booties"` at `35000` paise.
2. **TODO 2:** `priceOf(code)` looks up the code with square brackets and returns its paise, or `null` when the code isn't there.
3. **TODO 3:** `makeLine(code, qty)` returns a cart line like `{ code: "AMI", name: "Amigurumi", paise: 45000, qty: 2 }`, or `null` for an unknown code.
4. **TODO 4:** `cartTotal(lines)` adds up `paise * qty` for every line object.

Press **Run** to see what `main.js` prints. Press **Submit** to run the checks.

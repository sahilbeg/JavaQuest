# Arrays and tuples

`src/cart.ts` keeps the price list as tuples, `[code, paise]`, and a cart as tuples, `[code, qty]`. Fill in the five functions. Every amount is whole paise.

1. **TODO 1:** `priceOf(code)` finds the entry in `PRICE_LIST` with that code and returns its price, or `0` when there isn't one.
2. **TODO 2:** `cartTotal(lines)` adds up price times quantity for every `[code, qty]` line. Use `priceOf`.
3. **TODO 3:** `codesInCart(lines)` lists each code once, in the order it first appears: `[["KEY", 1], ["CST", 2], ["KEY", 3]]` gives `["KEY", "CST"]`.
4. **TODO 4:** `priceRange(prices)` returns the pair `[lowest, highest]`, or `[0, 0]` for no prices.
5. **TODO 5:** `cheapestFirst(list)` returns a **new** list sorted from the lowest price to the highest. The list it was given must stay as it was.

Destructuring works on tuples: `for (const [code, qty] of lines)`. Press **Submit** to run the checks.

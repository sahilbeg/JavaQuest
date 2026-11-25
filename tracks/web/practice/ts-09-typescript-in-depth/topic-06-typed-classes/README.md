# Classes in TypeScript

Finish the `StockBook` class in `src/stockBook.ts`. It implements the `StockStore` interface and keeps its counts in a `private` Map, so the only way to change stock is through its methods.

1. **TODO 1:** `stockOf(id)` returns the units in stock, or `0` for an item that was never received.
2. **TODO 2:** `receive(id, qty)` adds `qty` units. If `qty` isn't a whole number above 0, throw an `Error` with the message `Quantity must be a whole number above 0`, and change nothing.
3. **TODO 3:** `sell(id, qty)` takes `qty` units away and returns `true`. If there aren't enough, it changes nothing and returns `false`.
4. **TODO 4:** the getter `totalUnits` adds up every unit in the shop.

`shop` is `readonly` and `counts` is `private`; don't change those. `src/` is type-checked in strict mode before the checks run.

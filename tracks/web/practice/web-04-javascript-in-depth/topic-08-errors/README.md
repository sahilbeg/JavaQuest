# Errors: throw, try and catch

The stock code sold three Golus when only one was left. Make it refuse, and make the order page answer kindly. Change `src/stock.js`.

Items look like `{ id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 3 }`.

1. **TODO 1:** In `OutOfStockError`, the message is already set by `super`. Also set `this.name` to `"OutOfStockError"` and `this.itemId` to the item's id.
2. **TODO 2:** `reserve(item, qty)` first checks the quantity. If `qty` is not a whole number of at least 1, throw a `RangeError` with the message `"qty must be a whole number above 0"`.
3. **TODO 3:** Then, if `qty` is more than `item.stock`, throw `new OutOfStockError(item, qty)`.
4. **TODO 4:** Otherwise, return a new item with `stock` lowered by `qty`. The item you were given must not change.
5. **TODO 5:** `tryReserve(item, qty)` calls `reserve` inside `try`:
   - On success, it returns `{ ok: true, item: <the new item> }`.
   - If `reserve` throws an `OutOfStockError`, it returns `{ ok: false, message: <the error's message> }`.
   - Any other error, like the `RangeError`, is a bug in the page: throw it again.

Press **Submit** in Cadence, or just save the file in your editor. The checks run on their own.

# Scope and closures

The order counter is a loose variable that any script can reset. Lock it, and the stock count, inside closures. Change `src/counters.js`.

1. **TODO 1:** `makeOrderNumbers(start)` returns a function. Each call of that function returns the next order id: `"order-17"`, then `"order-18"`, and so on. Two counters made by two calls never share a number.
2. **TODO 2:** `makeStockCounter(initial)` returns an object with three functions that share one private count:
   - `sell()` takes one item off and returns `true`. When the count is already 0, it changes nothing and returns `false`.
   - `restock(qty)` adds `qty` to the count.
   - `left()` returns the count.
   The object must not have a `count` property: the count lives only in the closure.
3. **TODO 3:** `makeNumberedLabels(names)` returns one function per name. Function number 0 returns `"1. Golu the elephant"` for the first name, and so on. Right now every function returns the wrong thing. Find out why, and fix it.

Press **Submit** in Cadence, or just save the file in your editor. The checks run on their own.

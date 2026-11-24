# Destructuring, spread and rest

Plain items show "(undefined)" on their cards. Fix the label and write the card helpers in `src/items.js`.

1. **TODO 1:** `cardLabel(item)` returns `"Golu the elephant (Indigo): ₹450.00"`: the name, the colour in brackets, then the price in rupees with two decimals. An item with no `colour` shows `Off-white`. Destructure `name`, `paise` and `colour` in the parameter list, with a default for `colour`.
2. **TODO 2:** `withStock(item, stock)` returns a new item with every field copied and `stock` set to the new number. The item you were given must not change. Use spread.
3. **TODO 3:** `mergeCatalogues(stall, shop)` returns one new array: the stall's items, then the shop's. Use spread.
4. **TODO 4:** `splitFeatured(items)` returns `{ featured, others }`: the first item, and a new array of all the rest. With an empty list, `featured` is `undefined` and `others` is `[]`. Use array destructuring with rest.
5. **TODO 5:** `totalPaise(...prices)` takes any number of prices and returns their sum. With no prices, it returns 0.

Press **Submit** in Cadence, or just save the file in your editor. The checks run on their own.

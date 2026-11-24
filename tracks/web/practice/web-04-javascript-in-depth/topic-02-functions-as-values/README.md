# Functions as values

Replace the copied counting loops with functions that take, and return, other functions. Change `src/offers.js`.

Each catalogue item looks like `{ id: "DC-1042", code: "AMI", name: "Golu the elephant", colour: "Indigo", paise: 45000, stock: 3 }`.

1. **TODO 1:** `countWhere(items, test)` returns how many items make `test(item)` return `true`.
2. **TODO 2:** `applyToAll(items, change)` returns a new array holding `change(item)` for every item, in order. It must not change `items`.
3. **TODO 3:** `priceAtMost(maxPaise)` returns a function. That function takes an item and returns `true` when its `paise` is `maxPaise` or less.
4. **TODO 4:** `withGiftWrap(priceOf)` returns a function. That function takes an item and returns `priceOf(item)` plus ₹30 (3000 paise) for gift wrap.

Use arrow functions for the functions you return. Don't use `map` or `filter` yet: the next ticket is about them.

Press **Submit** in Cadence, or just save the file in your editor. The checks run on their own.

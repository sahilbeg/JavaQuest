# FEAT-09: The typed catalogue engine

Build the engine behind Sana's filter buttons in `src/catalogue.ts`. The types, `rupees` and `assertNever` are already there. Items look like `{ id: "DC-1042", code: "AMI", name: "Golu the elephant", colour: "Indigo", paise: 45000, stock: 3 }`.

1. **TODO 1:** `matches(item, filter)` says whether one item passes one filter:
   - `colour`: the item's colour is exactly that colour.
   - `price`: `minPaise <= paise <= maxPaise`, ends included. Either limit may be missing, and `0` is a real limit.
   - `inStock`: the stock is above 0.
   - `search`: the name contains the text, ignoring capital letters and spaces around the text. `""` matches everything.
   - anything else: throw, through `assertNever`.
2. **TODO 2:** `runQuery(items, query)` returns a new array of the items that pass every filter, in catalogue order. With `query.sort`, sort by `name`, `paise` or `stock`, `"asc"` or `"desc"`; ties keep catalogue order. Never change `items`.
3. **TODO 3:** `countBy(items, key)` returns a `Map` from each value of `key` to how many items have it, in the order values first appear.
4. **TODO 4:** `toCard(item)` returns `{ id, name, price, soldOut }`: `price` is `rupees(item.paise)`, and `soldOut` is `true` when the stock is 0.

`src/` is type-checked in strict mode before the checks run.

# Array methods

Answer the catalogue page's five questions with array methods. Change `src/catalogue.js`.

Each item looks like `{ id: "DC-1042", code: "AMI", name: "Golu the elephant", colour: "Indigo", paise: 45000, stock: 3 }`.

1. **TODO 1:** `names(items)` returns every item's name, in order. Use `map`.
2. **TODO 2:** `inStock(items)` returns the items whose `stock` is above 0. Use `filter`.
3. **TODO 3:** `stockValue(items)` returns the worth of all stock in paise: each item's `paise` times its `stock`, added up. Use `reduce`. An empty list is worth 0.
4. **TODO 4:** `findById(items, id)` returns the item with that id, or `undefined` when there is none. Use `find`.
5. **TODO 5:** `cheapestFirst(items)` returns a new array sorted by `paise`, cheapest first. Items with the same price keep their old order. The list you were given must keep its own order.

None of these functions may change the list they are given.

Press **Submit** in Cadence, or just save the file in your editor. The checks run on their own.

# FEAT-04: The catalogue filter engine

Build the engine behind Sana's catalogue controls, in `src/filters.js`. Items look like `{ id: "DC-1042", code: "AMI", name: "Golu the elephant", colour: "Indigo", paise: 45000, stock: 3 }`.

**`filterCatalogue(items, options)`** returns a new array and never changes `items`. Every option may be left out, and a missing option filters nothing.

1. **TODO 1, `colour`:** keep items of this colour, ignoring capital letters. `""` means any colour.
2. **TODO 2, `minPaise` and `maxPaise`:** keep items priced within the range, ends included. `0` is a real limit.
3. **TODO 3, `inStockOnly`:** when `true`, keep only items with `stock` above 0.
4. **TODO 4, `search`:** keep items whose `name` contains the text, ignoring capital letters and spaces around the text. `""` or only spaces means no search.
5. **TODO 5, `sort`:** `"price-asc"` is cheapest first, `"price-desc"` is dearest first. Missing means catalogue order. Items with the same price keep their catalogue order.

**Helpers for the controls**

6. **TODO 6:** `colourOptions(items)` returns each colour once, sorted A to Z.
7. **TODO 7:** `priceRange(items)` returns `{ minPaise, maxPaise }`: the lowest and highest prices. For an empty list, it returns `null`.

Press **Submit** in Cadence, or just save the file in your editor. The checks run on their own.

# State and render: keeping the page in sync

Rebuild Golu's item page around one state object, in `src/item-page.js`. The state is `{ qty, giftWrap, stitch }`. An item looks like `{ id: "DC-1042", name: "Golu the elephant", paise: 45000 }`.

1. **TODO 1:** `itemTotal(item, state)` returns the total in paise. Each piece costs `item.paise`, plus `STITCH_PAISE` (₹50) when `stitch` is not blank. Multiply by `qty`, then add `GIFT_WRAP_PAISE` (₹30) once when `giftWrap` is true.

`createItemPage(root, item, max)` wires the page and returns `{ getState }`:

2. **TODO 2:** `render()` draws everything from the state: the quantity in `.qty`, the − button disabled at 1, the + button disabled at `max`, the gift wrap box ticked or not, the summary like `2 × Golu the elephant`, and the total in rupees, like `₹930.00`.
3. **TODO 3:** `update(changes)` makes a **new** state object with the changes on top, then calls `render()`.
4. **TODO 4:** one click listener on `root`: `data-action="inc"` adds 1 (never above `max`), `data-action="dec"` takes 1 away (never below 1).
5. **TODO 5:** ticking or unticking the gift wrap box updates `giftWrap`, and typing in the stitch box updates `stitch`.

Handlers only call `update`. Only `render` changes the page.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.

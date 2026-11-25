# Updating lists without redrawing everything

Write `reconcile(list, items, { key, create, update })` in `src/keyed.js`. It brings `list`'s children in line with `items`, keeping every row it can. The packing screen (`src/orders.js` and `src/main.js`) already uses it.

- `key(item)` gives an item's key, like `"order-17"`.
- `create(item)` makes a new, empty row element.
- `update(row, item)` fills a row from its item.

1. **TODO 1:** first, remember the rows already in the list, by their `data-key`.
2. **TODO 2:** for each item, use the row that has its key, the very same element. Only when there is none, make one with `create(item)` and set its `data-key` to the item's key.
3. **TODO 3:** call `update(row, item)` on every row, kept or new, and put the row in its place, so the list ends up in the items' order. Move a row only when it isn't already in its place.
4. **TODO 4:** remove the rows whose keys are no longer in `items`.

So a ticked checkbox and a half-typed note survive an update, as long as their row's key is still there. Focus survives too, in a row that doesn't have to move.

Open the preview: tick some orders, then press "A new WhatsApp order".

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.

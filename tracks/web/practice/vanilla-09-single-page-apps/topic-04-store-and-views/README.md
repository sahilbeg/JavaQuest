# A store and views that follow it

The whole app keeps its state in one store: `{ lines: [{ id, qty }], route }`. Write the cart actions and a badge that follows the store, in `src/cart.js`. `src/store.js` is finished: `getState()`, `setState(update)` (where `update` gets the old state and returns a new one) and `subscribe(listener)`, which returns an unsubscribe function.

1. **TODO 1:** `addItem(store, id)` adds a line `{ id, qty: 1 }` at the end, or raises the qty of the item's existing line by 1.
2. **TODO 2:** `removeItem(store, id)` takes the item's line out.
3. **TODO 3:** `itemCount(state)` adds up every line's `qty`.
4. **TODO 4:** `watch(store, selector, onChange)` calls `onChange(selector(state))` at once, then again only when the selected value changes (compare with `!==`). It returns a function that stops watching.
5. **TODO 5:** `mountCartBadge(el, store)` uses `watch` with `itemCount` to keep `el.textContent` equal to the count, as text. It returns the unmount function.

**Never change the old state.** Build new objects and arrays with `{ ...state }`, `[...lines, line]`, `map` and `filter`. No `push`, no `splice`, and no `line.qty++`.

Open the preview and add and remove items: the badge in the header should follow.

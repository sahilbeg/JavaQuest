# Custom hooks: useCart

Pull the cart logic into one hook that any component can call.

Change `src/useCart.js`. An item looks like `{ code: "KEY", name: "Keychain", paise: 12000 }`, and a cart line is the item plus `qty`.

1. **TODO 1:** keep the lines in state. Start from `loadLines(storageKey)`, which is already written, and call it only on the first render.
2. **TODO 2:** save the lines to `localStorage` under `storageKey` (as JSON) whenever they change.
3. **TODO 3:** `add(item)` puts the item at the end with `qty: 1`. If a line with the same `code` is already there, it raises that line's `qty` by 1 instead. Two calls to `add` in the same click must both count.
4. **TODO 4:** `remove(code)` drops the line with that code.
5. **TODO 5:** `count` is how many things are in the cart (all the `qty` values added up). `total` is the price of everything in paise.

The hook returns `{ lines, add, remove, count, total }`. `src/main.jsx` shows a small shop that uses it in the preview.

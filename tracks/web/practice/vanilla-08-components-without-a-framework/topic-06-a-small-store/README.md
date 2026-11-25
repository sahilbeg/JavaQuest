# A small store with subscribers

Keep the cart in one store, in `src/store.js`, and make `<cart-count>` follow it, in `src/cart-count.js`.

`createStore(initialState)` returns `{ getState, setState, subscribe }`:

1. **TODO 1:** keep the subscribers in a `Set`.
2. **TODO 2:** `setState(change)` accepts an object of changes, or a function that gets the current state and returns the changes. Either way, it makes a **new** state object with the changes on top of the old fields. The old state object is never changed.
3. **TODO 3:** after the state has changed, every subscriber is called with the new state.
4. **TODO 4:** `subscribe(listener)` adds the listener and returns a function that removes it again.

In `src/cart-count.js`, a cart line looks like `{ id: "DC-1042", qty: 2 }`, and the store's state looks like `{ lines: [...] }`:

5. **TODO 5:** `countItems(lines)` returns the number of pieces: the sum of every `qty` (0 for no lines).
6. **TODO 6:** `<cart-count>` gets its store as a property (`element.store = cart`) before it joins the page. When it joins, it subscribes and shows `Cart (3)` straight away, then follows every change. When it leaves the page, it unsubscribes. If it comes back, it subscribes again.

Open the preview: add items, and remove and bring back the header badge.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.

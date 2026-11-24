# useReducer for the cart

Move every change to the cart into one reducer.

The state is `{ lines: [...] }`, and each line is `{ code, name, paise, qty }`.

In `src/cartReducer.js`, handle these actions. Always return a new state; never change the one you were given.

1. **TODO 1:** `{ type: "added", item }` adds the item with `qty: 1` at the end, or raises the qty of the line with the same code by 1.
2. **TODO 2:** `{ type: "removed", code }` drops the line with that code.
3. **TODO 3:** `{ type: "quantityChanged", code, qty }` sets that line's qty. A qty of 0 or less removes the line.
4. **TODO 4:** `{ type: "cleared" }` empties the cart.
5. **TODO 5:** any other type throws an `Error` with the message `Unknown action: <type>`, for example `Unknown action: couponApplied`.

In `src/CartPanel.jsx`:

6. **TODO 6:** keep the cart with `useReducer(cartReducer, emptyCart)`. The buttons already dispatch the right actions.

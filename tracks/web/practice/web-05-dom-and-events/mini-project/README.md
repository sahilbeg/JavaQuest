# FEAT-05: The cart drawer

Build the cart drawer in `src/drawer.js`. A cart line is `{ id: "DC-1042", qty: 2 }`; a product is `{ id, name, paise }`.

**The cart maths**

1. **TODO 1:** `countItems(lines)` returns the number of pieces, the sum of every `qty`.
2. **TODO 2:** `cartTotal(lines, catalogue)` returns the total in paise: each line's product price times its `qty`, added up.
3. **TODO 3:** `changeQty(lines, id, delta)` returns a **new** array with that line's `qty` changed by `delta`. A new id with a `delta` of 1 is added at the end with `qty` 1. A line that reaches 0 is removed. An unknown id with −1 changes nothing.

**The drawer:** `createDrawer(root, catalogue, startLines)` keeps `{ open, lines }` as its state (closed, with `startLines`, at the start) and returns `{ getLines }`.

4. **TODO 4:** `render()` draws everything from the state:
   - `#cart-toggle` says `Cart (3)` (the piece count) and has `aria-expanded` `"true"` or `"false"`;
   - `#cart-drawer` is hidden when closed;
   - `#drawer-lines` holds one line per cart line, like the HTML in the KB page: `li.line[data-id]` with `.name`, a `data-action="dec"` button labelled `One less <name>`, `.qty`, a `data-action="inc"` button labelled `One more <name>`, and `.amount` in ₹;
   - `#drawer-empty` shows only when the cart is empty, and `#drawer-total` shows the total in ₹.
5. **TODO 5:** the cart button opens and closes the drawer. Opening moves focus to `#cart-close`.
6. **TODO 6:** `#cart-close`, and Escape pressed inside the drawer, close it and move focus back to `#cart-toggle`.
7. **TODO 7:** one listener on `#products`: an `.add` click adds one of that card's item. The drawer stays as it is.
8. **TODO 8:** one listener on `#drawer-lines`: `inc` adds one, `dec` takes one away (removing the line at 0).

Open the preview and try it with the mouse, then with only the keyboard.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.

# Classes: a Cart with methods

Write one `Cart` class for the whole website, in `src/Cart.js`. The lines live in the private field `#lines`. Each line looks like `{ id: "DC-1042", name: "Golu the elephant", paise: 45000, qty: 2 }`.

1. **TODO 1:** The constructor stores the customer's name as `this.customer`: `new Cart("Asha").customer` is `"Asha"`.
2. **TODO 2:** `add(item, qty)` adds a catalogue item. `qty` is 1 when left out. If the cart already has a line with the same `id`, raise that line's `qty` instead of adding a second line. `add` returns the cart, so calls can be chained.
3. **TODO 3:** `remove(id)` takes out the line with that id. Removing an id that isn't in the cart changes nothing.
4. **TODO 4:** `lines()` returns copies of the lines, in the order they were first added. Changing what it returns must not change the cart.
5. **TODO 5:** The getter `count` is the number of items: every line's `qty`, added up.
6. **TODO 6:** The getter `total` is the price of everything in paise: `paise * qty` for every line, added up.

Press **Submit** in Cadence, or just save the file in your editor. The checks run on their own.

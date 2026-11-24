# Event delegation: one listener for many buttons

Replace the listener-per-button loop with delegation, in `src/delegate.js`. Each function adds **exactly one** click listener, on the element it is given.

1. **TODO 1:** `listenForAdds(list, onAdd)`: a click anywhere inside an `.add` button, including on the icon inside it, calls `onAdd(id)` with the `data-id` of that button's `.product` card.
2. **TODO 2:** clicks that are not inside an `.add` button, like on a card's heading, do nothing. Cards added to the list later must work too.
3. **TODO 3:** `listenForLineActions(lines, actions)`: a click inside a `button[data-action]` calls `actions[action](id)`, where `action` is the button's `data-action` (`"inc"`, `"dec"` or `"remove"`) and `id` is the `data-id` of the line around it.
4. **TODO 4:** a button whose action isn't in `actions`, or a click outside any button, does nothing and throws nothing.

Open the preview: add items, use + and −, then press "Show the Diwali special" and try its Add button.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.

# Components talking: CustomEvent

Make `<qty-picker item-id="DC-1042">` announce what the customer wants, in `src/qty-picker.js`. Its buttons are already in its shadow root: `−` (`data-action="dec"`), the quantity in `.qty`, `+` (`data-action="inc"`), and `Add to cart` (`.add`).

1. **TODO 1:** `+` and `−` change the quantity by one and show it in `.qty`. It never goes below 1 or above the `max` attribute (10 when there is no `max`).
2. **TODO 2:** `Add to cart` dispatches a `CustomEvent` called `add-to-cart` from the picker itself, with `detail` `{ id, qty }`: the `item-id` attribute and the quantity as a number.
3. **TODO 3:** the event has `bubbles: true` and `composed: true`, so the page hears it even when the picker sits inside another component's shadow root.
4. **TODO 4:** after the event is sent, the quantity goes back to 1.

On the page's side:

5. **TODO 5:** `listenForAddToCart(root, onAdd)` adds **one** listener to `root` that calls `onAdd(id, qty)` for every `add-to-cart` event from inside it.

Open the preview: choose a quantity, press Add, and watch the cart line.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.

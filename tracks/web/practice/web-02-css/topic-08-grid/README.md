# Grid: the product grid

Swap the wrapping flex row for a real grid, so every card lines up with the cards above it, and make Golu's card two columns wide.

1. **TODO 1:** in `.products`, change `display: flex` to `display: grid`, and delete `flex-wrap: wrap`.
2. **TODO 2:** `.products` gets `grid-template-columns: repeat(3, 1fr)`.
3. **TODO 3:** change the gap on `.products` to `1.5rem`.
4. **TODO 4:** add `.featured` with `grid-column: span 2`. Golu's card is `<li class="product featured">`.

You may also delete `flex: 1 1 14rem` from `.product`: grid items take their width from the columns. The cards' own `display: flex` stays, because it lines up the parts inside each card.

File to change: `style.css`.

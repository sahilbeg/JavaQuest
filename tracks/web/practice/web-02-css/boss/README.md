# INC-02: The catalogue breaks on small Android phones

The live stylesheet was built on a laptop. On a 360px phone the page runs off the screen, and a quick fix hides the Order buttons. Fix `style.css` so the catalogue fits every phone.

1. **Bronze:** in `main`, delete `width: 1200px` and add `max-width: 1200px`. Keep `margin: 0 auto`. No rule outside a media query may set a `width` in px wider than 320px.
2. **Silver:** make the product grid phone first.
   - Delete `grid-template-columns` from the base `.products` rule (or set it to `1fr`). Keep `display: grid`.
   - Add the columns back inside a `min-width` media query, for example `@media (min-width: 40rem) { .products { grid-template-columns: repeat(2, 1fr); } }`. You may add a wider one for four columns.
   - Use no `max-width` media queries.
3. **Gold:**
   - `.product img` gets `max-width: 100%` and `height: auto`.
   - Delete `overflow-x: hidden` from `body`.

Make the preview narrow to check it at phone width.

File to change: `style.css`.

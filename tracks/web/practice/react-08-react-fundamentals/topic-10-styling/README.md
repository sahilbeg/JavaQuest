# Styling components

Nothing shows which colour chip is picked. Fix `ColourChip.jsx` and `style.css`. The swatch colours are in `colours.js` (`HEX.Marigold` is `"#F2A81D"`).

1. **TODO 1:** `ColourChip`'s `className` is `"chip"` normally and `"chip chip--selected"` when `selected` is true. It also has `aria-pressed={selected}`.
2. **TODO 2:** The `<span className="swatch">` gets an inline style with `backgroundColor: HEX[colour]`.
3. **TODO 3:** `PriceTag`'s `className` is `"price"` normally and `"price price--sale"` when `onSale` is true.
4. **TODO 4:** In `style.css`, add a `.chip--selected` rule with `border-color: #2E3A8C;` (Indigo).
5. **TODO 5:** In `style.css`, add a `.price--sale` rule with `background-color: #F2A81D;` (Marigold).

The CSS checks read the exact property names `border-color` and `background-color`, so don't use the `border` or `background` shorthand for these two.

The preview shows the five chips and two prices. Tap a chip to pick it. Press **Submit** to run the checks.

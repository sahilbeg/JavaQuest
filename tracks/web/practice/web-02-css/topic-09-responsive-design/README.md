# Responsive design: phone first

Make the header and the footer fit a phone first, then spread them out on wider screens.

1. **TODO 1** (`index.html`): add `<meta name="viewport" content="width=device-width, initial-scale=1">` inside `<head>`.
2. **TODO 2:** in `.site-header`, replace `justify-content: space-between` and `align-items: center` with `flex-direction: column`.
3. **TODO 3:** change the base `h1` size to `1.75rem`.
4. **TODO 4:** add `.places` with `display: grid` and `gap: 1rem`. Don't give it any columns here: phones get one.
5. **TODO 5:** inside the block `@media (min-width: 48rem) { ... }`, add:
   - `.site-header` with `flex-direction: row`, `justify-content: space-between` and `align-items: center`
   - `h1` with `font-size: 2.5rem`
   - `.places` with `grid-template-columns: 1fr 1fr`

Write the media query exactly as `@media (min-width: 48rem)`, and use no `max-width` queries. Make the preview narrow and wide to watch it change.

Files to change: `index.html` and `style.css`.

# Styling in Next.js

Bring back the brand colours everywhere, and give the item cards styles that can't clash. The root layout, `src/app/layout.jsx`, already has `import "./globals.css";`.

1. **TODO 1** in `src/app/globals.css`: on `:root`, set `--marigold: #f2a81d`, `--indigo: #2e3a8c` and `--off-white: #faf7f0`.
2. **TODO 2** in `src/app/globals.css`: `body` gets `background: var(--off-white)` and `color: var(--indigo)`. Links, `a`, get `color: var(--indigo)`.
3. **TODO 3** in `src/app/items/ItemCard.module.css`:
   - `.card`: `border: 2px solid var(--marigold)`, `border-radius: 8px`, `padding: 1rem`,
   - `.price`: `color: var(--indigo)`, `font-weight: 700`,
   - `.soldOut`: `opacity: 0.5`.
4. **TODO 4** in `src/app/items/cardClass.js`: `cardClass(styles, item)` returns `styles.card`, or `styles.card` and `styles.soldOut` joined with one space when `item.stock` is 0.
5. **TODO 5** in `src/app/items/ItemCard.jsx`: use the module instead of plain text class names: `className={cardClass(styles, item)}` on the `<article>`, and `className={styles.price}` on the price.

The checks read your CSS files rule by rule, so write each value as shown (lower-case colours are fine either way).

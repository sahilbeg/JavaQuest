# Styling components: scoping and naming

Give the product card names nothing else uses, keep the brand colours in one place, and let pages recolour the sale badge. Three files change.

In `src/style.css` (the card's stylesheet):

1. **TODO 1:** on `:root`, define the tokens `--dhaaga-marigold: #F2A81D`, `--dhaaga-indigo: #2E3A8C` and `--dhaaga-off-white: #FAF7F0`.
2. **TODO 2:** rename every rule to the card's BEM names, so every selector except `:root` starts with `.product-card`: `.product-card`, `.product-card__name`, `.product-card__price`, `.product-card__add` and `.product-card--sold-out`. Keep what each rule does.
3. **TODO 3:** outside `:root`, no colour is typed as a hex code. Use the tokens: the card's background is `var(--dhaaga-off-white)`, the name's colour `var(--dhaaga-indigo)`, the button's background `var(--dhaaga-marigold)`.

In `src/card.js`:

4. **TODO 4:** `productCard(product)` gives the same card with BEM classes: the `<article>` is `product-card` (plus `product-card--sold-out` when `stock` is 0), the `<h3>` is `product-card__name`, the price is `product-card__price`, and the button is `product-card__add`.

In `src/sale-badge.js` (a shadow DOM component):

5. **TODO 5:** the `:host` rule's background is `var(--sale-badge-bg, #F2A81D)` and its colour `var(--sale-badge-text, #2E3A8C)`, so a page can recolour the badge from outside. Write them exactly like that, with one space after the comma.
6. **TODO 6:** add `:host([hidden]) { display: none; }`, and mark the `<span>` with `part="label"`.

Open the preview: the page's own `h3` and `.price` rules no longer touch the cards, and the gift guide's badge is Mint.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.

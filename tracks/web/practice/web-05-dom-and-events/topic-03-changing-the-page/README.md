# Changing text, attributes and classes

Make each product card show its name, price and stock, in `src/card.js`. Sana's CSS in `src/style.css` is already done: it greys out `.sold-out` cards and highlights `.low` ones.

1. **TODO 1:** `setName(card, name)` writes the name into the card's `<h3>` as plain text. Tags in a name must show as text.
2. **TODO 2:** it also gives the `.add` button an `aria-label` of `Add <name> to cart`, like `Add Golu the elephant to cart`.
3. **TODO 3:** `setPrice(card, paise)` writes the price into `.price` with `rupees(paise)` (from `money.js`), like `₹450.00`, and stores the paise in `data-paise`.
4. **TODO 4:** `showStock(card, stock)` writes into `.stock`: `Sold out` for 0, `Only 2 left` for 1 to 3, and `In stock` for more.
5. **TODO 5:** it also puts the class `sold-out` on the card at 0 and `low` at 1 to 3 (and removes them otherwise), disables the `.add` button only at 0, and stores the stock in `data-stock`. The card keeps its `product` class.

Open the preview to see three cards: one low, one sold out, one in stock.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.

# Showing things conditionally

A sold-out Golu still offered Add to cart. Make the card react to the stock in `ItemCard.jsx`.

1. **TODO 1:** `StockBadge` shows:
   - stock 0: `<span className="badge sold-out">Sold out</span>`
   - stock 1 to 3: `<span className="badge low">Only 2 left</span>`, with the real number
   - more than 3: nothing, so `return null`
2. **TODO 2:** `ItemCard` shows `<StockBadge />` after the price, passing it the `stock`.
3. **TODO 3:** `ItemCard` shows `<p className="gift-wrap">Gift wrap available</p>` only when `giftWrap` is true.
4. **TODO 4:** While `stock` is above 0, the card shows the Add to cart button. At 0 it shows a `Notify me` button instead, which calls `onNotify`.

A sold-out card must never show a stray `0`. Watch what is on the left of every `&&`.

The preview shows three cards: plenty in stock, one left, and sold out. Press **Submit** to run the checks.

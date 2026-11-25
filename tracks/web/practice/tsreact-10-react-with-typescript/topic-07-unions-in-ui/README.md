# Showing states with unions

Make the stock badge tell the truth. The `Stock` union in `StockBadge.tsx` has three kinds.

**`StockBadge.tsx`** returns one `<p>` with the classes `badge` plus one more:

| Stock | Text | Classes |
|---|---|---|
| `in-stock`, count more than 3 | `In stock` | `badge badge--ok` |
| `in-stock`, count 3 or less | `Only 2 left` | `badge badge--low` |
| `sold-out`, `backOn` is null | `Sold out` | `badge badge--out` |
| `sold-out`, `backOn` is `"14 Dec"` | `Sold out, back on 14 Dec` | `badge badge--out` |
| `made-to-order`, 5 days | `Made to order: ready in 5 days` | `badge badge--made` |

1. **TODO 1:** Show `Only N left` with `badge--low` when 3 or fewer are in stock.
2. **TODO 2:** Add the `sold-out` case.
3. **TODO 3:** Add the `made-to-order` case.
4. **TODO 4:** Add a `default` branch with the exhaustive check: `const unhandled: never = stock; return unhandled;`.

**`BuyBox.tsx`**

5. **TODO 5:** Disable the **Add to cart** button when the item is sold out.
6. **TODO 6:** For made-to-order items only, show `<p className="note">Crocheted for you after you order.</p>` after the button.

Press **Submit** to run the checks.

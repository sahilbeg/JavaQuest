# FEAT-10: The typed React catalogue

Build the catalogue page from the KB page's design. `products.ts` and `money.ts` are finished.

**`ProductCard.tsx`**

1. **TODO 1:** `stockText(stock)`: `In stock` for more than 3, `Only 2 left` for 3 or fewer, `Sold out` when sold out.
2. **TODO 2:** The card: `<article className="product-card">` with the name in `<h3>`, `p.price`, `p.colour`, `p.stock`, and an `Add` button labelled `Add <name> to cart` that calls `onAdd(product.id)`.
3. **TODO 3:** Disable Add when the product is sold out, or when `inCart` has reached its stock count.

**`CatalogueApp.tsx`**

4. **TODO 4:** Typed state: the search text, the colour (`Colour | "All"`, starting `"All"`) and the cart (`Record<string, number>`, starting `{}`).
5. **TODO 5:** The header with `<h1>Dhaaga & Co.</h1>` and `p.cart-count` showing `Cart (2)`.
6. **TODO 6:** The search box (`<label htmlFor="search">Search</label>` and `<input id="search" type="search">`), and the colour buttons in `div.colour-filter`: `All`, then each colour, with `aria-pressed`.
7. **TODO 7:** `p.showing` (`Showing 1 of 6 items`), then `section.grid` with one `ProductCard` per visible product keyed by id, or `p.empty` (`No items match your search.`) when nothing matches.

Search matches any part of a product's name, ignores capitals and spaces at the ends, and works together with the colour. The chosen colour button has `aria-pressed` true and the others false. `Cart (N)` counts every Add across all products, and changing the search or colour never resets it. Each card gets its own `inCart` count. Press **Submit** to run the checks.

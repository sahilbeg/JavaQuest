# FEAT-08: The React catalogue

Build the catalogue page of dhaaga.in. The products and colours are in `products.js`, and `rupees` is in `money.js`. Both are finished. The full design is on the KB page.

1. **TODO 1 (`ProductCard.jsx`):** an `<article className="product-card">` with the name in an `<h3>`, `rupees(paise)` in `p.price`, the colour in `p.colour`, and an `Add` button labelled `Add <name> to cart` that calls `onAdd(product.id)`.
2. **TODO 2 (`CatalogueApp.jsx`):** state for the search text (`""`), the chosen colour (`"All"`) and the cart count (`0`).
3. **TODO 3:** the visible products. A name matches when it contains the search text, ignoring capitals and spaces at the ends. A colour matches when it is the chosen colour, or the choice is `All`.
4. **TODO 4:** `p.cart-count` shows the real count, like `Cart (2)`.
5. **TODO 5:** the search box is controlled by the search text.
6. **TODO 6:** the colour buttons: `All`, then each colour in `COLOURS`, in that order. The chosen one has `aria-pressed` true and `className="chip chip--selected"`; the others have `aria-pressed` false and `className="chip"`.
7. **TODO 7:** one `ProductCard` per visible product, keyed by id, whose Add adds one to the cart count. With nothing visible, show `<p className="empty">No items match your search.</p>`.

`Showing 2 of 6 items` already works once `visible` is right. Searching and choosing colours must never reset the cart count.

Press **Submit** to run the checks.

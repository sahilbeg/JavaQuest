# Handling events

Tapping Add to cart on a tile opens the product page, and Enter does nothing in the search box. Fix the three components in `ProductTile.jsx`.

1. **TODO 1:** `CartButton`'s click handler calls `onAdd` with the product's `id`.
2. **TODO 2:** The same handler stops the click from bubbling up to the tile.
3. **TODO 3:** Clicking anywhere on a `ProductTile` calls `onOpen(id)`. Put an `onClick` on the `<article>`.
4. **TODO 4:** The tile shows a `<CartButton />` after the price, with the tile's `id` and `onAdd` passed in.
5. **TODO 5:** In `SearchBar`, pressing Enter calls `onSearch` with the text in the box. Other keys do nothing.

Pass functions to event props without calling them: `onClick={handleClick}`, or `onClick={() => onOpen(id)}` when you need the id.

The preview shows a tile and a search box, with a line that says which handler ran. Press **Submit** to run the checks.

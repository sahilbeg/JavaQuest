# Server and client components

Build the home page: a list drawn on the server, with a small client button in each row.

1. **TODO 1** in `src/app/page.jsx`: make `HomePage` an `async` server component that awaits `getItems()` from `src/lib/catalogue.js`.
2. **TODO 2** in `src/app/page.jsx`: under the `<h1>`, draw a `<ul>` with one `<li>` per item, in catalogue order. Each `<li>` holds:
   - `<h2>` with the item's name,
   - `<p>` with its price from `rupees(item.paise)`, like `₹450.00`,
   - `<AddToCartButton itemId={item.id} name={item.name} />`.
3. **TODO 3** in `src/app/AddToCartButton.jsx`: make it a client component with `"use client";` on the first line, above the imports.
4. **TODO 4** in `src/app/AddToCartButton.jsx`: keep a count in state, starting at 0. The `Add to cart` button adds 1. When the count is above 0, show a `<p>` after the button that says, for example, `2 in your cart`.

`page.jsx` stays a server component: no `"use client"` there. The catalogue and `rupees` in `src/lib/` are finished.

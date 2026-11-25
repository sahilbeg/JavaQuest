# Performance in Next.js

On slow phone data, Golu's page takes almost two seconds before anything shows. Make it fast. The file to change is `src/app/items/[id]/page.jsx`.

The data calls in `src/lib/shop.js` each take about 600 ms in production: `listItems()`, `getItem(id)`, `getReviews(id)` and `getStock(id)`. Don't change that file, or `src/components/AddToCartButton.jsx`.

1. **TODO 1:** `generateStaticParams()` returns `[]`, so no item page is built ahead of time. Return one `{ id }` per item from `listItems()`, in catalogue order: `[{ id: "DC-1042" }, { id: "DC-1101" }, ...]`.
2. **TODO 2:** `ItemPage` awaits the item, then the reviews, then the stock, one after another. Start all three at once with `Promise.all`, so the page waits about 600 ms instead of 1,800.
3. **TODO 3:** `AddToCartButton` is a client component. Every prop you give it is packed into the page and sent to the phone. Give it only `id` and `stock`, not the whole item or the reviews.

The page must still show the item's name in the `<h1>`, the price, the description, and the reviews under a heading like **Reviews (2)**.

## How it's checked

The checks swap the data layer for a pretend one whose calls wait until the check answers them. Right after the page starts, all three calls must already be running. The checks also swap the button for a pretend one that records its props: they must be exactly `{ id: "DC-1042", stock: 7 }`.

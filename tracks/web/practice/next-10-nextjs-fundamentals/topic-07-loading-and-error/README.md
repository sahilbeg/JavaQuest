# Loading and error pages

Give the shop at `/items` kind words for three moments. The shop page itself, `src/app/items/page.jsx`, is finished.

1. **TODO 1** in `src/app/items/loading.jsx`: return `<p role="status">Loading the shop...</p>`.
2. **TODO 2** in `src/app/items/error.jsx`: make it a client component, with `"use client";` as the very first line.
3. **TODO 3** in `src/app/items/error.jsx`: `ShopError({ error, retry })` returns a `<div role="alert">` holding:
   - `<h2>We couldn't load the shop.</h2>`,
   - `<p>Please try again in a moment.</p>`,
   - a `<button>` that says `Try again` and calls `retry()` when pressed.

   Never show `error` or `error.message` on the page.
4. **TODO 4** in `src/app/items/not-found.jsx`: return an `<h1>` that says `We couldn't find that piece.` and a `Link` (from `next/link`) that says `Back to the shop` and goes to `/items`.

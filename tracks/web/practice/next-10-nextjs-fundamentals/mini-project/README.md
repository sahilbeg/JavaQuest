# FEAT-10: The catalogue on Next.js

Build the shop page at `/items`. Every choice lives in the address, like `/items?colour=indigo&sort=price-asc`. The catalogue, colours and `rupees` in `src/lib/` are finished. Items name their colour (`"Rani pink"`); addresses use slugs (`rani-pink`).

1. **TODO 1** `queryCatalogue(items, { colour, q, sort })` in `src/lib/query.js` returns a new array and never changes `items`:
   - `colour` (a slug): only that colour's items; an unknown slug gives `[]`; `""` keeps all.
   - `q`: names containing it, ignoring case and spaces around it; `""` keeps all.
   - `sort`: `"price-asc"` cheapest first, `"price-desc"` dearest first, anything else catalogue order. Equal prices keep catalogue order.
2. **TODO 2** `shopHref({ colour, q, sort })` in `src/lib/query.js`: `"/items"`, plus `?` and the non-empty choices in the order colour, q, sort, built with `URLSearchParams`. So `{ colour: "indigo", q: "", sort: "price-asc" }` gives `"/items?colour=indigo&sort=price-asc"`, and `{ q: "golu elephant" }` gives `"/items?q=golu+elephant"`.
3. **TODO 3** in `src/app/items/page.jsx`: `export const metadata = { title: "Shop", description: "Every handmade piece from Dhaaga & Co., Pune." }`.
4. **TODO 4 and 5** `ShopPage({ searchParams })`: await `searchParams` (`colour`, `q`, `sort`, each `""` when missing), then draw:
   - `<h1>Shop</h1>` and `<Filters colour={colour} q={q} sort={sort} />`,
   - `<p>Showing 2 of 6 pieces</p>` (matches, out of the whole catalogue),
   - a `<ul>` with one `<li>` per match: a `Link` to `/items/<id>` with the name, a space, the price from `rupees`, and `<span> Sold out</span>` when stock is 0,
   - or, when nothing matches, `<p>Nothing matches.</p>` and a `Link` `See everything` to `/items`, with no list.
5. **TODO 6 and 7** in `src/app/items/Filters.jsx` (the menus and the search box are drawn for you):
   - `go(change)` calls `router.push(shopHref(...))` with the current `colour`, `q` and `sort`, and the change on top.
   - Sending the search form calls `go` with `q` set to the trimmed text.

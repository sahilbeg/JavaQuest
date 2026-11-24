# Pages and routing: one app, many pages

Give every page of the store its own address, using the part after `#`.

| Address | Page | Its `<h1>` |
|---|---|---|
| `#/` or no hash | home | `Handmade in Pune` |
| `#/cart` | cart | `Your cart` |
| `#/item/DC-1042` | item | the item's name, `Golu the elephant` |
| anything else, including `#/item/` | not found | `Page not found` |

1. **TODO 1 to 3** in `src/router.js`: `matchRoute(path)` returns `{ page: "home" }`, `{ page: "cart" }`, `{ page: "item", id: "DC-1042" }` or `{ page: "not-found" }`. `currentPath()` is done: it turns `#/cart` into `/cart`.
2. **TODO 4** in `src/useHashRoute.js`: keep the path in state, starting from `currentPath()`.
3. **TODO 5** in `src/useHashRoute.js`: listen for `hashchange` on `window` and update the path. Remove the listener when the app goes away.
4. **TODO 6** in `src/Link.jsx`: render an `<a>` whose `href` is `#` plus `to`, so `to="/cart"` gives `href="#/cart"`.
5. **TODO 7** in `src/App.jsx`: show the page that matches the route. The pages are written; `ItemPage` needs the route's `id`.

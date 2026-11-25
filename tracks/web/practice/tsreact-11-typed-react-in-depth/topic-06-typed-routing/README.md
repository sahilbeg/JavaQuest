# Typed routing

Give every page of the store its own address after the `#`, with routes as a typed union.

| Address | Route | Its `<h1>` |
|---|---|---|
| `#/` or no hash | `{ page: "shop" }` | `All items` |
| `#/item/DC-1042` | `{ page: "item", id: "DC-1042" }` | the item's name, `Golu the elephant` |
| `#/order/order-17` | `{ page: "order", orderId: "order-17" }` | `Order order-17` |
| anything else, like `#/item/` or `#/about` | `{ page: "notFound", path: "/about" }` | `Page not found` |

In `src/routes.ts` (`pathFromHash` is done: it turns `#/item/DC-1042` into `/item/DC-1042`, and an empty hash into `/`):

1. **TODO 1:** `parseRoute(path)` returns the route from the table. An item or order address has exactly three parts when split on `/`, and the last one isn't empty.
2. **TODO 2:** `toHash(route)` goes the other way: `#/`, `#/item/<id>`, `#/order/<orderId>`, or `#` plus the path for notFound.

In `src/useRoute.ts`:

3. **TODO 3:** keep the path in state, starting from `pathFromHash(window.location.hash)`, and return `parseRoute(path)`.
4. **TODO 4:** listen for `hashchange` on `window` and update the path. Remove the listener in the cleanup.

In `src/App.tsx`:

5. **TODO 5:** `Page` shows the page that matches the route, passing `route.id`, `route.orderId` or `route.path`. End the switch with `default: return assertNever(route);`.

`Link` in `src/Link.tsx` is done: it takes a `Route` and uses your `toHash` for its `href`.

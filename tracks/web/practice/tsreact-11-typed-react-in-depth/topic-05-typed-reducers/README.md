# Typed reducers with action unions

Put Raju's rule card for orders into one typed reducer, so no button can break the rules.

In `src/orders.ts`, `ordersReducer(orders, action)` returns the next list of orders. It never changes the old array or the old order objects: return new ones.

1. **TODO 1:** `added` puts `{ id, customer, status: "new" }` at the end of the list. If an order with that id is already there, return `orders` itself, unchanged.
2. **TODO 2:** `packed` turns a `new` order into `packed`.
3. **TODO 3:** `shipped` turns a `packed` order into `shipped`.
4. **TODO 4:** `cancelled` turns a `new` or `packed` order into `cancelled`. A shipped order can't be cancelled.
   - In TODOs 2 to 4, an order in any other status, and every other order, stays as it is.
5. **TODO 5:** the `default` branch returns `assertNever(action)`. TypeScript then complains if a new action type has no case, and an unknown action from plain JavaScript throws `Unknown action: <type>`.

In `src/OrderBoard.tsx`:

6. **TODO 6:** replace the placeholder `orders` and `dispatch` with `useReducer(ordersReducer, start)`. The rows and their **Pack**, **Ship** and **Cancel** buttons are done.

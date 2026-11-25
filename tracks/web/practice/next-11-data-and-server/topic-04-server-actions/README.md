# Server actions

Give Raju's orders page a **Mark packed** button and a **Cancel** button, backed by server actions.

`src/lib/orders.js` is done. It keeps the orders in memory: `getOrder(id)` gives a copy of an order (or `undefined`), `listOrders()` gives them all, and `setStatus(id, status)` changes one. The seed orders are order-17 (NEW), order-18 (PACKED) and order-19 (NEW).

Change `src/app/orders/actions.js` (it starts with `"use server"`):

1. **TODO 1:** `markPacked(formData)` reads `orderId` from the form. If there is no such order, return `{ error: "No order order-99." }`. If the order isn't `NEW`, return `{ error: "order-18 is already PACKED." }` (with its real id and status) and change nothing.
2. **TODO 2:** otherwise set it to `PACKED`, call `revalidatePath("/orders")`, and return `{ ok: true }`.
3. **TODO 3:** `cancelOrder(orderId, formData)` gets the id first, because the page binds it. Unknown order: `{ error: "No order order-99." }`. Not `NEW`: `{ error: "order-18 can't be cancelled once it's PACKED." }`. Otherwise set it to `CANCELLED`, call `revalidatePath("/orders")` and return `{ ok: true }`.

Change `src/app/orders/page.jsx`:

4. **TODO 4:** the Mark packed form says which order it's about with a hidden input: `<input type="hidden" name="orderId" value={order.id} />`.

The checks call your actions with a `FormData` they build, and use a pretend `next/cache`.

# FEAT-11: Orders with server actions

Take orders on dhaaga.in, let Raju move them along, and let the WhatsApp bot ask for an order's status.

Done for you: `src/lib/catalogue.js` (`findItem`), `src/lib/money.js` (`rupees`), and in `src/lib/orders.js` the functions `resetOrders`, `getOrder`, `listOrders` (newest first), `setStatus` and the table `NEXT_STATUS` (`NEW` → `PACKED` → `SHIPPED` → `DELIVERED`).

1. **TODO 1:** `createOrder({ name, phone, lines })` in `src/lib/orders.js`. Price each line `{ itemId, qty }` from the catalogue. Throw `new Error("Unknown item DC-9999.")` for an unknown item, before saving. Delivery is 6000 paise, free when the items come to more than 99900. Save `{ id, name, phone, lines, totalPaise, status: "NEW" }` with ids `order-20`, `order-21`, ..., keeping only `itemId` and `qty` in each line. Return a copy.
2. **TODO 2:** `placeOrder(prevState, formData)` in `src/app/order/actions.js`. Check `name`, `phone`, `itemId` and `qty` with the messages `Please enter your name.`, `Phone needs 10 digits, like 98765 43210.`, `Please choose an item.` and `Choose between 1 and 5.` Any mistake: return `{ errors, values }` (values as typed). Otherwise `createOrder` with the trimmed name, the phone without spaces and one line `{ itemId, qty }` (qty as a number), call `revalidatePath("/orders")`, then `redirect("/order/<id>")`.
3. **TODO 3:** `advanceOrder(formData)` in `src/app/orders/actions.js`. Read `orderId`. Unknown: `{ error: "No order order-99." }`. No next status: `{ error: "order-20 is already DELIVERED." }`. Otherwise `setStatus` to the next status, call `revalidatePath("/orders")` and return `{ ok: true, status: <the new status> }`.
4. **TODO 4:** `GET` in `src/app/api/orders/[id]/route.js` answers `{ id, status, total }`, with `total` like `₹510.00`, and nothing else. Unknown: 404 `{ "error": "No order order-99." }`.
5. **TODO 5:** `OrdersPage` in `src/app/orders/page.jsx`: under `<h1>Orders</h1>`, one `<li>` per order with a `<p>` like `order-20 · Asha Rao · ₹510.00 · NEW`. Each order with a next status gets `<form action={advanceOrder}>` with a hidden input `orderId` and a button `Move to PACKED` (the next status). No orders: `<p>No orders yet.</p>`.

The checks use a pretend `next/navigation` (`redirect` throws `NEXT_REDIRECT /order/order-20`) and a pretend `next/cache`.

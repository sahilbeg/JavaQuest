## The design

An order on dhaaga.in has one life, in four steps, and every part of the app sees it through the same data layer.

```animate
title: The life of order-20
kind: scene
nodes:
  - { id: customer, label: Customer, emoji: "📱" }
  - { id: action, label: Server actions, emoji: "⚡" }
  - { id: dal, label: "lib/orders.js", emoji: "📒", value: "no order-20" }
  - { id: shop, label: Raju's orders page, emoji: "🏪" }
  - { id: bot, label: WhatsApp bot, emoji: "🤖" }
steps:
  - say: Asha fills in the order form. placeOrder checks every field on the server.
    send: { from: customer, to: action, label: "placeOrder(form)" }
  - say: The data layer prices Golu from the catalogue, adds delivery, and saves order-20 as NEW.
    send: { from: action, to: dal, label: "createOrder" }
    set: { dal: "order-20 NEW ₹510.00" }
  - say: The action refreshes the orders page and sends Asha to her order's page.
    send: { from: action, to: customer, label: "redirect /order/order-20", back: true }
  - say: In the shop, Raju presses Move to PACKED. advanceOrder moves the order one step, and the page refreshes.
    send: { from: shop, to: action, label: "advanceOrder(order-20)" }
    set: { dal: "order-20 PACKED ₹510.00" }
  - say: Asha messages the bot. The bot asks the website's API, which answers the status and total, but no name or phone.
    send: [{ from: bot, to: dal, label: "GET /api/orders/order-20" }, { from: dal, to: bot, label: "PACKED, ₹510.00", back: true }]
```

The statuses only ever move forward, one step at a time:

| Now | Next |
|---|---|
| `NEW` | `PACKED` |
| `PACKED` | `SHIPPED` |
| `SHIPPED` | `DELIVERED` |
| `DELIVERED` | none: the order is finished |

`NEXT_STATUS` in `src/lib/orders.js` holds this table. Prices come only from the catalogue. Delivery inside Pune is ₹60 (6000 paise), free when the items come to more than ₹999 (99900 paise).

## What you build

`src/lib/catalogue.js` (`findItem`) and `src/lib/money.js` (`rupees`) are done. In `src/lib/orders.js`, `resetOrders`, `getOrder`, `listOrders` (newest first), `setStatus` and `NEXT_STATUS` are done too.

1. **`createOrder({ name, phone, lines })`** in `src/lib/orders.js`. Price every line `{ itemId, qty }` with `findItem`, and throw `new Error("Unknown item DC-9999.")` for an unknown item, before saving. Add delivery. Save `{ id, name, phone, lines, totalPaise, status: "NEW" }`, where `id` is `order-20`, then `order-21` and so on, and each line keeps only `itemId` and `qty`. Return a copy.
2. **`placeOrder(prevState, formData)`** in `src/app/order/actions.js`, for `useActionState`. Read `name`, `phone`, `itemId` and `qty`. Check them with the rules and messages from topic 5: `Please enter your name.`, `Phone needs 10 digits, like 98765 43210.`, `Please choose an item.` and `Choose between 1 and 5.` With any mistake, return `{ errors, values }` and save nothing. Otherwise create the order with the trimmed name, the phone's digits and one line, call `revalidatePath("/orders")`, and `redirect("/order/order-20")`.
3. **`advanceOrder(formData)`** in `src/app/orders/actions.js`. Read `orderId`. Unknown: `{ error: "No order order-99." }`. Finished: `{ error: "order-20 is already DELIVERED." }`. Otherwise move it to `NEXT_STATUS[status]`, call `revalidatePath("/orders")`, and return `{ ok: true, status: "PACKED" }` (with the new status).
4. **`GET`** in `src/app/api/orders/[id]/route.js`, for the WhatsApp bot. Answer `{ "id": "order-20", "status": "NEW", "total": "₹510.00" }`, and nothing else about the order. An unknown id answers 404 `{ "error": "No order order-99." }`.
5. **`OrdersPage`** in `src/app/orders/page.jsx`, Raju's page. Under `<h1>Orders</h1>`, one `<li>` per order, newest first, with a `<p>` like `order-20 · Asha Rao · ₹510.00 · NEW`. Every order that has a next status gets a form whose action is `advanceOrder`, with a hidden `orderId` input and a button `Move to PACKED` (or `SHIPPED`, or `DELIVERED`). With no orders, show `<p>No orders yet.</p>`.

### Syntax: a status table instead of if chains

```js
const NEXT_STATUS = { NEW: "PACKED", PACKED: "SHIPPED", SHIPPED: "DELIVERED" };

const next = NEXT_STATUS["PACKED"];
```

Looking the next status up in an object keeps every rule in one place. A finished order has no entry, so `NEXT_STATUS["DELIVERED"]` is `undefined`, and the action can refuse it.

## Recap

- The data layer is the only code that saves orders. It prices them from the catalogue and gives each one the next number.
- Server actions check everything on the server: the order form's fields, and whether a status may move.
- After changing data, an action calls `revalidatePath("/orders")`, and `redirect` comes last, outside any `try`.
- The bot's route handler answers only what a stranger may know: id, status and total.
- Raju's page is a server component that reads the data layer directly and gives each unfinished order one button.

## Common mistakes

**1. Answering the bot with the whole order.**

```js
export async function GET(request, { params }) {
  const { id } = await params;
  return Response.json(getOrder(id));
}
```

Anyone who guesses an order number gets the customer's name and phone number.

Fix: answer `{ id, status, total }` only, and 404 when there's no such order.

**2. Redirecting before revalidating.**

```js
redirect(`/order/${order.id}`);
revalidatePath("/orders");
```

`redirect` throws, so the line after it never runs. Raju's page doesn't show the new order until something else refreshes it.

Fix: `revalidatePath` first, `redirect` last.

**3. Letting a status jump.**

```js
setStatus(orderId, "DELIVERED");
```

One wrong tap marks a NEW order as delivered, and the customer gets a "delivered" message for a box still on the shelf.

Fix: always move to `NEXT_STATUS[order.status]`, and refuse when there is none.

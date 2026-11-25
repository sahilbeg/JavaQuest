## Hook

When Dhaaga was one table, Meera Aunty wrote orders on any paper nearby: a receipt, a napkin, the back of a yarn label. Finding an order meant searching every pocket. Worse, anyone at the table could read a customer's number off a napkin.

Then she bought one order book, and only she and Raju write in it. When someone asks about an order, they ask Raju, and he tells them just what they need. Your app needs the same order book.

## Concept

### Data code scattered everywhere

After five topics, order code lives in many places. The orders page reads rows. A route handler reads rows. A server action writes rows. Each one decides by itself what to show and what to check.

That causes three kinds of bug:

- One place forgets a rule the others follow, like a status check.
- One place shows a field it shouldn't, like a full phone number.
- One place trusts data it shouldn't, like a price sent by the browser.

### One module that owns the data

```callout jargon
A **data access layer** (or **data layer**) is one module that is the only code allowed to touch the stored data. Pages, actions and route handlers ask it for what they need, and never read the database themselves.
```

In Next.js, it's usually a file like `src/lib/orders.js` that exports a few functions:

### Syntax: a data layer module

```js
import { dbAll, dbFind } from "./db.js";

export function getOrders() {
  return dbAll("orders").map(toOrderView);
}

export function getOrderView(id) {
  const row = dbFind("orders", id);
  return row ? toOrderView(row) : null;
}
```

Pages call `getOrders()`. Only this file knows the table is called `"orders"`, or how a row looks. If you later move to a real database, only this file changes.

### Everyone goes through the data layer

```animate
title: Every caller asks the data layer
kind: scene
nodes:
  - { id: page, label: Orders page, emoji: "📄", row: 0, col: 0 }
  - { id: action, label: Server action, emoji: "⚡", row: 1, col: 0 }
  - { id: route, label: Route handler, emoji: "🔌", row: 2, col: 0 }
  - { id: dal, label: "lib/orders.js", emoji: "📒", row: 1, col: 2 }
  - { id: db, label: Database, emoji: "🗄️", row: 1, col: 4, value: "full rows" }
steps:
  - say: The orders page needs a list. It doesn't open the database. It calls getOrders().
    send: { from: page, to: dal, label: "getOrders()" }
  - say: The data layer reads the full rows, with phone numbers and payment references.
    send: { from: dal, to: db, label: "all orders" }
  - say: The database answers with everything it has.
    send: { from: db, to: dal, label: "full rows", back: true }
  - say: The data layer keeps only what a page may show, and sends that back.
    send: { from: dal, to: page, label: "id, customer, total, status", back: true }
  - say: The server action and the route handler go the same way. The rules live in one place, for all three.
    send: [{ from: action, to: dal, label: "createOrder()" }, { from: route, to: dal, label: "getOrderView()" }]
```

### Hand out only what's needed

A database row often holds more than any page should see: the full phone number, a UPI payment reference, private notes. If you pass the whole row to a component, all of it can end up on screen, or inside the page's data sent to the browser.

```callout jargon
A **data transfer object** (**DTO**) is a small, plain object made for one purpose, holding only the fields the caller needs. Here, an order *view* is the DTO.
```

So the data layer builds a new object with just the safe fields:

```js run
function toOrderView(row) {
  return {
    id: row.id,
    customer: row.name,
    phoneEnding: row.phone.slice(-4),
    status: row.status,
  };
}

const row = { id: "order-17", name: "Asha Rao", phone: "9876543210", upiRef: "UPI-55120", status: "NEW" };
console.log(toOrderView(row));
```

The view has no `phone` and no `upiRef`. Staff can still confirm "the number ending 3210" with the customer.

```callout warning
Never write `{ ...row }` to make a view. It copies every field, including the ones added to the table next year that nobody thought about.
```

### Never trust a price from the browser

When the browser sends an order, it sends item ids and quantities. Some old code also sent prices, and anyone can change those before they're sent. The data layer looks up every price itself:

```animate
title: The server works out the total
kind: code
language: js
code: |
  const PRICES = { "DC-1042": 45000, "DC-4410": 35000 };
  const lines = [{ itemId: "DC-4410", qty: 2 }, { itemId: "DC-1042", qty: 1, paise: 1 }];
  let items = 0;
  for (const line of lines) items += PRICES[line.itemId] * line.qty;
  const delivery = items > 99900 ? 0 : 6000;
  console.log(items + delivery);
steps:
  - line: 2
    say: Two lines arrive from the browser. Someone has edited the second one to claim Golu costs 1 paisa.
  - line: 3
    vars: { items: 0 }
    say: The total starts at zero.
  - line: 4
    vars: { items: 70000 }
    say: Two pairs of booties at the real price, 35000 paise each.
  - line: 4
    vars: { items: 115000 }
    say: Golu at the real price, 45000. The paise field from the browser is never read.
  - line: 5
    vars: { delivery: 0 }
    say: The items come to more than ₹999, so delivery is free.
  - line: 6
    out: "115000"
    say: The total is ₹1,150.00, worked out entirely on the server.
```

### Syntax: keeping the data layer on the server

```js
import "server-only";
```

Put this line at the top of the data layer. If a client component ever imports the file, even by accident, the build fails with an error. The order rows can never be bundled into browser code.

```callout jargon
**server-only** is a tiny package. Importing it does nothing on the server, and stops the build if the file would end up in the browser.
```

### Reading the same order twice in one request

A layout and a page may both need order-17 during the same render. For `fetch`, Next.js shares the answer, as you saw in topic 2. For a database call, wrap the function in React's `cache`:

```js
import { cache } from "react";

export const getOrderView = cache(async (id) => {
  const row = await db.find("orders", id);
  return row ? toOrderView(row) : null;
});
```

Now the database is asked once per request for each id. Like memoization, it is forgotten when the request ends.

### Check permissions in the data layer too

Because every read and write passes through it, the data layer is the best place for "who may see this?" checks. You'll add a logged-in customer in topic 7, and then `getOrderView` can refuse orders that belong to someone else.

## Worked example

The heart of Kavya's data layer. `dbAll`, `dbFind` and `dbInsert` come from a pretend database, and `rupees` formats paise.

```js
import { dbAll, dbFind, dbInsert, nextOrderNumber } from "./db.js";
import { findItem } from "./catalogue.js";
import { rupees } from "./money.js";

export function toOrderView(row) {
  return {
    id: row.id,
    customer: row.name,
    phoneEnding: row.phone.slice(-4),
    total: rupees(row.totalPaise),
    status: row.status,
  };
}

export function createOrder({ name, phone, lines }) {
  let items = 0;
  for (const line of lines) {
    const item = findItem(line.itemId);
    if (!item) throw new Error(`Unknown item ${line.itemId}.`);
    items += item.paise * line.qty;
  }
  const delivery = items > 99900 ? 0 : 6000;
  const row = {
    id: `order-${nextOrderNumber()}`,
    name,
    phone,
    lines: lines.map(({ itemId, qty }) => ({ itemId, qty })),
    totalPaise: items + delivery,
    status: "NEW",
    upiRef: null,
  };
  dbInsert("orders", row);
  return toOrderView(row);
}
```

- `toOrderView` builds a fresh object with five fields. No `phone`, no `upiRef`, no `lines`.
- `createOrder` prices every line from the catalogue. A wrong item id throws, because it means the caller has a bug.
- Only `itemId` and `qty` are copied from each line, so a stray `paise` field never reaches the database.
- It returns a view, never the row.

## Try it

When the checks pass, add a new field to one seed row in `db.js`, like `notes: "asked for gift wrap"`. Run the checks: they still pass, because `toOrderView` lists its fields one by one. Now change `toOrderView` to start with `...row`, and run them again. Remove the field and the spread afterwards.

## Recap

- A data layer is one module that alone touches the stored data. Pages, actions and route handlers call its functions.
- It returns views (DTOs): new objects with only the fields a page needs, never whole rows.
- It works out prices and totals itself, and ignores prices sent by the browser.
- `import "server-only"` makes the build fail if the data layer reaches the browser.
- React's `cache` shares a database read within one request, and the data layer is the right place for permission checks.

## Common mistakes

**1. Spreading the row into the view.**

```js
export function toOrderView(row) {
  return { ...row, total: rupees(row.totalPaise) };
}
```

The page looks fine. But the full phone number and the UPI reference are now in the page's data, where anyone can read them in DevTools.

Fix: list each field the view needs, by name.

**2. Trusting the line's price.**

```js
for (const line of lines) items += line.paise * line.qty;
```

Someone edits the request and orders ten tote bags at 1 paisa each. The data layer saves a ₹60.10 order.

Fix: `findItem(line.itemId).paise`, and throw when the item doesn't exist.

**3. Reading the database from a page.**

```jsx
export default async function OrdersPage() {
  const rows = dbAll("orders");
  return <OrderTable rows={rows} />;
}
```

It works today. Next month someone renames a column, and this page breaks silently while the others were updated.

Fix: `const orders = getOrders();` from the data layer.

**4. Importing the data layer into a client component.**

```jsx
"use client";
import { getOrders } from "../lib/orders.js";
```

Without `server-only`, the bundler may pull the data code into the browser bundle. With `import "server-only"` at the top of the data layer, the build stops with an error that names the file, which is what you want.

Fix: fetch the data in a server component and pass the views down as props.

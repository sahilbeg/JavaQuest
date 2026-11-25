# A data layer for orders

Put all the order code in one data layer, `src/lib/orders.js`. Pages, actions and route handlers will only ever call its functions.

These files are done:

- `src/lib/db.js` is a pretend database. `dbAll("orders")` gives copies of every row, `dbFind("orders", id)` gives one row or `undefined`, `dbInsert("orders", row)` saves a row, and `nextOrderNumber()` gives 20, then 21, and so on. A row looks like `{ id: "order-17", name: "Asha Rao", phone: "9876543210", lines: [...], totalPaise: 41000, status: "NEW", upiRef: "UPI-55120" }`.
- `src/lib/catalogue.js` has `findItem(id)`, and `src/lib/money.js` has `rupees(paise)`.

Change `src/lib/orders.js`:

1. **TODO 1:** `toOrderView(row)` returns a new object with exactly five fields: `id`, `customer` (the row's name), `phoneEnding` (the last 4 digits of the phone), `total` (like `₹410.00`) and `status`. Nothing else from the row.
2. **TODO 2:** `getOrders({ status })` returns the views of all orders, newest first (order-19 before order-17). When a `status` is given, only orders with that status.
3. **TODO 3:** `getOrderView(id)` returns the view of one order, or `null` when there is none.
4. **TODO 4:** `createOrder({ name, phone, lines })` prices each line `{ itemId, qty }` from the catalogue, ignoring any price the line carries. An unknown item throws `new Error("Unknown item DC-9999.")` and saves nothing. Delivery is 6000 paise, free when the items come to more than 99900 paise. Save a row with the next id (`order-20`), the lines (only `itemId` and `qty`), `totalPaise`, status `NEW` and `upiRef: null`. Return its view.

In a real project, the data layer's first line would be `import "server-only";`, so it can never reach the browser.

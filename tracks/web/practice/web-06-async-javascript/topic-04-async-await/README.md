# async and await

`placeOrder` doesn't wait for anything, so receipts say "UPI ref undefined" and declined payments keep the item held. Fix it in `src/order.js`, and write `totalStock`.

**`placeOrder(api, order)`**, where `order` looks like `{ id: "order-17", itemId: "DC-1042", paise: 45000 }`:

1. **TODO 1:** `await api.reserve(order.itemId)`. If it throws, return `Sorry, DC-1042 just sold out.` (with the real item id) and don't call `pay`.
2. **TODO 2:** `await api.pay(order.paise)` and return `Order order-17 confirmed, UPI ref UPI-5521` (with the real order id and `receipt.ref`).
3. **TODO 3:** if paying throws, `await api.release(order.itemId)`, then return `Payment failed for order-17. Please try again.`

`placeOrder` must never throw: every outcome is one of the three messages.

**`totalStock(getStock, ids)`**

4. **TODO 4:** loop over `ids` with `for...of`, `await getStock(id)` for each one, and return the total. Ask for one id at a time, in order. An empty list gives `0`.

Press **Submit** in Cadence, or save the file in your editor. The checks use a pretend shop API, so nothing real is charged.

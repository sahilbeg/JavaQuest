# INC-11: Two customers get the same order number

At the flash sale, two customers who ordered at the same moment both got order-20, and one order was lost. Fix `createOrder` in `src/lib/orders.js` and the `placeOrder` action in `src/app/checkout/actions.js`.

`src/lib/db.js` is a pretend database where every call waits a moment, like a real one. `nextOrderNumber()` works like a database sequence: each call gets a new number (20, 21, ...), even when calls arrive at the same time.

1. **Bronze:** two customers who order at the same moment get different order numbers, and both orders are saved.
2. **Silver:** a new order never takes the number of an order that exists, even after another order was deleted. Numbers only go up.
3. **Gold:** the checkout form sends a hidden `requestId`. Sending the same form twice, at the same moment or one after the other, makes only one order, and both sends redirect to that order's page (`/orders/order-20`). Different `requestId`s still make separate orders.

Keep what each order holds and how its total is worked out as they are.

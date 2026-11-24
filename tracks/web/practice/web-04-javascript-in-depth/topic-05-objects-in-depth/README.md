# Objects in depth

Fix the basket's methods and make order copies real copies. Change `src/orders.js`.

1. **TODO 1:** In `createBasket(owner)`, the `add(code, paise, qty)` method pushes `{ code, paise, qty }` onto **this** basket's `lines`, then returns the basket, so calls can be chained: `basket.add(...).add(...)`. `qty` is 1 when it is left out.
2. **TODO 2:** The `total()` method returns the paise for every line in this basket: `paise * qty`, added up. An empty basket's total is 0.
3. **TODO 3:** `copyOrder(order)` returns a deep copy of an order like this one:

   ```js
   { id: "order-17", customer: { name: "Mr. Fernandes", phone: "98765 43210" }, lines: [{ code: "AMI", qty: 2 }] }
   ```

   Changing anything in the copy, at any level, must leave the original alone. Right now it returns the same object.

Two baskets must never share their lines.

Press **Submit** in Cadence, or just save the file in your editor. The checks run on their own.

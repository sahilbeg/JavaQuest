# Lifting state up

Every card counts its own taps, so the cart summary always says 0. Move the cart into `Shop` in `Shop.jsx`. `CartSummary` is finished.

1. **TODO 1:** `ProductCard` keeps no state. It takes `product`, `inCart` and `onAdd` as props, shows `In cart: ` and the `inCart` number, and calls `onAdd(product.id)` when Add is pressed.
2. **TODO 2:** `Shop` keeps the cart in state: an object from product id to quantity, starting as `{}`. For example `{ "DC-1042": 2, "DC-1043": 1 }`.
3. **TODO 3:** `addToCart(id)` adds one of that product by setting a **new** object. Never change the old one.
4. **TODO 4:** Work out `count` (all quantities added up) and `totalPaise` (each product's `paise` times its quantity) from the cart, on every render.
5. **TODO 5:** Clear cart empties the cart. Each card gets its own `inCart` (0 when the product is not in the cart) and `onAdd={addToCart}`.

Two elephants and one coaster set should show `Items in cart: 3` and `Total: ₹1,050.00`.

The preview shows the shop. Add a few things, then press Clear cart. Press **Submit** to run the checks.

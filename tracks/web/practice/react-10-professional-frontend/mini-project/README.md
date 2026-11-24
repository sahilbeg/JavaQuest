# CAPSTONE: Launch the dhaaga.in online store

Build the store that launches on Friday: a catalogue with search and filters, a cart with quantities and the delivery and Diwali offer rules, a checkout with a UPI step, and an accessible header and footer. Five stages, one check file each. Do them in order, and run the checks after each stage.

`catalogue.js` (the six products), `money.js` (`formatRupees`), `Footer.jsx`, `main.jsx` and `style.css` are ready. Money is always whole paise.

## Stage 1: prices (`pricing.js`, TODO 1) — `Stage1Pricing.test.js`

`cartTotals(lines)` takes lines `{ id, name, paise, qty }` and returns `{ subtotal, discount, delivery, total }` in paise.

- `subtotal`: `paise × qty` for every line, added up.
- `discount`: the Diwali offer, 10% off when the subtotal is **above** ₹1,500 (150000 paise), rounded **down** to whole paise. Otherwise 0.
- `delivery`: ₹60 (6000 paise). Free when the total after the offer is **above** ₹999 (99900 paise). An empty cart pays no delivery.
- `total`: `subtotal - discount + delivery`.

## Stage 2: the catalogue (`filters.js`, `Catalogue.jsx`, TODOs 2–3) — `Stage2Catalogue.test.jsx`

- `filterProducts(products, { search, category })` keeps products whose name contains the search words (trimmed, any capitals) and whose `category` matches. `"All"` matches every category.
- `Catalogue` keeps `search` and `category` in state, wired to the **Search** input and the **Category** select, and shows only the filtered products.
- The `role="status"` line says how many show: `"6 products"`, `"2 products"`, `"1 product"`.
- Each **Add to cart** button has `aria-label="Add <name> to cart"` and calls `onAdd(product)`.

## Stage 3: the cart (`cart.js`, `Cart.jsx`, TODOs 4–5) — `Stage3Cart.test.jsx`

`cartReducer(lines, action)` never changes the lines it's given:

- `{ type: "add", product }`: a new product becomes `{ id, name, paise, qty: 1 }`, with no other fields. A product already in the cart gets 1 more, never above `MAX_QTY` (5).
- `{ type: "setQty", id, qty }` sets that line's quantity. `{ type: "remove", id }` drops that line. `{ type: "clear" }` empties the cart.

`Cart({ lines, onSetQty, onRemove, onCheckout })`:

- Each line's quantity select is labelled **Quantity for &lt;name&gt;**, shows `line.qty`, and calls `onSetQty(id, number)`.
- Each line's **Remove** button has `aria-label="Remove <name>"` and calls `onRemove(id)`.
- The totals read exactly `Subtotal: ₹450.00`, `Diwali offer: -₹179.80` (only when there is a discount), `Delivery: ₹60.00` or `Delivery: Free`, and `Total: ₹510.00`.
- An empty cart says **Your cart is empty.**, and its **Checkout** button is disabled.

## Stage 4: checkout (`Checkout.jsx`, TODOs 6–8) — `Stage4Checkout.test.jsx`

Three steps, `Address`, `Payment` and `Done`, with the current one marked `aria-current="step"`.

- **TODO 6:** `addressProblem({ name, phone, pin })` returns the first problem, checked in this order: `"Enter your name."`, `"Enter a 10-digit phone number."` (spaces don't count, so `98765 43210` is fine), `"Enter a 6-digit PIN code."`. A good address gives `""`.
- **TODO 7:** `isValidUpiId(text)` accepts IDs like `meera@okaxis`: at least two letters, digits, dots, dashes or underscores, an `@`, then at least two letters. Paying with a good ID calls `onPlaced({ orderId, totalPaise, payment: "UPI" })` once.
- **TODO 8:** when checkout opens, and whenever the step changes, focus moves to the step's heading (`Delivery address`, `Pay by UPI`, `Thank you!`).

Errors show in the `role="alert"` paragraph. The pay button reads `Pay ₹510.00`, with the real total.

## Stage 5: the whole store (`Header.jsx`, `App.jsx`, TODOs 9–10) — `Stage5Store.test.jsx`

- **TODO 9:** the header starts with a **Skip to content** link to `#main`, the first Tab stop on the page. The menu is a `<nav aria-label="Main">`. The cart count reads `Cart: 0 items`, `Cart: 1 item`, `Cart: 3 items`, counting every item's quantity.
- **TODO 10:** `App` keeps the cart with `useReducer(cartReducer, [])` and the view with `useState`. **Checkout** swaps the shop for `Checkout`, **Back to the shop** swaps back, and a placed order clears the cart.

The last check is a whole customer journey: search, filter, add, change a quantity, check out and pay by UPI.

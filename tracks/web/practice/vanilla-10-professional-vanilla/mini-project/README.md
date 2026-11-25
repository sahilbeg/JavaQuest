# CAPSTONE: Launch the dhaaga.in store with plain JavaScript

Build the store that launches on Friday, with no framework: a catalogue with search and filters, a cart with the delivery and Diwali offer rules, a checkout with a UPI step, and an accessible header and footer. Five stages, one check file each. Do them in order, and run the checks after each stage.

Ready for you: `catalogue.js` (the six products), `money.js` (`rupees`), `dom.js` (`el`, which builds elements safely), `store.js` (`createStore`), `layout.js` (the footer), `main.js`, `index.html` and `style.css`. Money is always whole paise.

## Stage 1: prices (`pricing.js`, TODO 1) — `Stage1Pricing.test.js`

`cartTotals(lines)` takes lines `{ id, name, paise, qty }` and returns `{ subtotal, discount, delivery, total }` in paise.

- `subtotal`: `paise × qty` for every line, added up.
- `discount`: the Diwali offer, 10% off when the subtotal is **above** ₹1,500 (150000 paise), rounded **down** to whole paise. Otherwise 0.
- `delivery`: ₹60 (6000 paise). Free when the total after the offer is **above** ₹999 (99900 paise). An empty cart pays no delivery.
- `total`: `subtotal - discount + delivery`.

## Stage 2: the catalogue (`filters.js`, `catalogueView.js`, TODOs 2–4) — `Stage2Catalogue.test.js`

- **TODO 2:** `filterProducts(products, { search, category })` keeps products whose name contains the search (trimmed, any capitals) and whose `category` matches. `"All"` matches every category.
- **TODO 3:** each **Add to cart** button gets `aria-label="Add <name> to cart"`. The `role="status"` line says how many show: `"6 products"`, `"1 product"`, `"0 products"`.
- **TODO 4:** redraw on the search box's `input` and the select's `change`. One click listener on the list calls `onAdd(product)` for the clicked card.

## Stage 3: the cart (`cart.js`, `cartView.js`, TODOs 5–8) — `Stage3Cart.test.js`

- **TODO 5:** `cartReducer(lines, action)` never changes the lines it's given:
  - `{ type: "add", product }`: a new product becomes `{ id, name, paise, qty: 1 }` at the end, with no other fields. A product already in the cart gets 1 more, never above `MAX_QTY` (5).
  - `{ type: "setQty", id, qty }` sets that line's quantity; `{ type: "remove", id }` drops it; `{ type: "clear" }` empties the cart.
- **TODO 6:** each line's label is tied to its select (`<label for="qty-DC-1042">Quantity for Golu the elephant</label>`), and each **Remove** button has `aria-label="Remove <name>"`.
- **TODO 7:** in `render(lines)`:
  - `#cart-empty` (**Your cart is empty.**) shows only when the cart is empty;
  - `#cart-totals` holds one `<p>` per row: `Subtotal: ₹450.00`, then `Diwali offer: -₹180.00` only when there is a discount, then `Delivery: ₹60.00` or `Delivery: Free`, then `Total: ₹510.00`. Hide it for an empty cart;
  - **Checkout** is disabled when the cart is empty;
  - keep focus: if an element inside the cart had focus, focus the element with the same `id` again after redrawing.
- **TODO 8:** a quantity `change` dispatches `{ type: "setQty", id, qty }` with `qty` as a **number**. **Remove** dispatches `{ type: "remove", id }` and moves focus to the `Your cart` heading. **Checkout** calls `onCheckout()`.

## Stage 4: checkout (`checkout.js`, TODOs 9–11) — `Stage4Checkout.test.js`

Three steps, `Address`, `Payment` and `Done`. The forms and the pay button are ready.

- **TODO 9:** `addressProblem({ name, phone, pin })` returns the first problem, checked in this order: `"Enter your name."`, `"Enter a 10-digit phone number."` (spaces don't count, so `98765 43210` is fine), `"Enter a 6-digit PIN code."`. A good address gives `""`.
- **TODO 10:** `isValidUpiId(text)` accepts IDs like `meera@okaxis` and `raju.pawar-1@ybl`: at least two characters that are each a letter, digit, dot, dash or underscore, then one `@`, then at least two letters (no digits), and nothing else. So `meera`, `@okaxis`, `m@okaxis` and `meera@ok1` are refused. A refused ID shows the alert `Enter a UPI ID like name@bank.` (already written).
- **TODO 11:** whenever the step changes (and when checkout opens), the step's `<li>` gets `aria-current="step"`, the others lose it, and focus moves to the step's heading (`Delivery address`, `Pay by UPI`, `Thank you!`).

## Stage 5: the whole store (`header.js`, `app.js`, TODOs 12–13) — `Stage5Store.test.js`

- **TODO 12:** the header starts with `<a class="skip-link" href="#main">Skip to content</a>`, the first link on the page. The menu is a `<nav aria-label="Main">`. The `#cart-count` line follows the store: `Cart: 0 items`, `Cart: 1 item`, `Cart: 3 items`, counting every line's quantity.
- **TODO 13:** `showCheckout()` stops the cart view following the store, swaps the shop for a `<section id="checkout">`, and mounts the checkout with the cart's total, `order-17` for the first order, an `onPlaced` that clears the cart, and **Back to the shop** going back to the shop.

The last check is a whole customer journey: search, filter, add, change a quantity, check out and pay by UPI, using only labels, button names and focus.

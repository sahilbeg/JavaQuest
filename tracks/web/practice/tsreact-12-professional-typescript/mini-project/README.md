# CAPSTONE: Launch the dhaaga.in store in TypeScript

Build the typed store that launches on Friday: a catalogue with search and filters, a cart with the delivery and Diwali offer rules, a checkout with a UPI step, and an accessible header and footer. Five stages, one check file each. Do them in order, and run the checks after each stage. Every file must type-check in strict mode, with no `any`.

Ready for you: `types.ts` (every shared type: `Product`, `CartLine`, `CartAction`, `Totals`, `Address`, `Step`, `PlacedOrder`), `catalogue.ts` (the six products, `CATEGORY_FILTERS` and the guard `isCategoryFilter`), `money.ts` (`formatRupees`), `Footer.tsx`, `main.tsx`, `index.html` and `style.css`. Money is always whole paise.

## Stage 1: prices (`pricing.ts`, TODO 1) — `Stage1Pricing.test.ts`

`cartTotals(lines)` returns `{ subtotal, discount, delivery, total }` in paise.

- `subtotal`: `paise × qty` for every line, added up.
- `discount`: the Diwali offer, 10% off when the subtotal is **above** ₹1,500 (150000 paise), rounded **down** to whole paise. Otherwise 0.
- `delivery`: ₹60 (6000 paise). Free when `subtotal - discount` is **above** ₹999 (99900 paise). An empty cart pays no delivery.
- `total`: `subtotal - discount + delivery`.

## Stage 2: the catalogue (`filters.ts`, `Catalogue.tsx`, TODOs 2–3) — `Stage2Catalogue.test.tsx`

- `filterProducts(products, { search, category })` keeps products whose name contains the search words (trimmed, any capitals) and whose `category` matches. `"All"` matches every category.
- `Catalogue` keeps `search` (a `string`) and `category` (a `CategoryFilter`) in state, wired to the **Search** input and the **Category** select. The select's value is a plain `string`, so check it with `isCategoryFilter` before setting state.
- The `role="status"` line says how many show: `"6 products"`, `"2 products"`, `"1 product"`.
- Each **Add to cart** button has `aria-label="Add <name> to cart"` and calls `onAdd(product)`.

## Stage 3: the cart (`cart.ts`, `Cart.tsx`, TODOs 4–5) — `Stage3Cart.test.tsx`

`cartReducer(lines, action)` handles every `CartAction` and never changes the lines it's given:

- `{ type: "add", product }`: a new product becomes `{ id, name, paise, qty: 1 }`, with no other fields. A product already in the cart gets 1 more, never above `MAX_QTY` (5).
- `{ type: "setQty", id, qty }` sets that line's quantity. `{ type: "remove", id }` drops that line. `{ type: "clear" }` empties the cart.
- Finish the `switch` with an exhaustive `never` check in `default`.

`Cart({ lines, onSetQty, onRemove, onCheckout })`:

- Each line's quantity select is labelled **Quantity for &lt;name&gt;**, shows `line.qty`, and calls `onSetQty(id, number)`.
- Each line's **Remove** button has `aria-label="Remove <name>"` and calls `onRemove(id)`.
- The totals read exactly `Subtotal: ₹450.00`, `Diwali offer: -₹179.80` (only when there is a discount), `Delivery: ₹60.00` or `Delivery: Free`, and `Total: ₹510.00`.
- An empty cart says **Your cart is empty.**, and its **Checkout** button is disabled.

## Stage 4: checkout (`Checkout.tsx`, TODOs 6–8) — `Stage4Checkout.test.tsx`

Three steps, `Address`, `Payment` and `Done`, with the current one marked `aria-current="step"`. The form and the steps are ready; finish the rules and the focus.

- **TODO 6:** `addressProblem({ name, phone, pin })` returns the first problem, checked in this order: `"Enter your name."`, `"Enter a 10-digit phone number."` (spaces don't count, so `98765 43210` is fine), `"Enter a 6-digit PIN code."`. A good address gives `""`.
- **TODO 7:** `isValidUpiId(text)` accepts IDs like `meera@okaxis`: at least two letters, digits, dots, dashes or underscores, an `@`, then at least two letters. Capitals and spaces around it are fine.
- **TODO 8:** when checkout opens, and whenever the step changes, focus moves to the step's heading (`Delivery address`, `Pay by UPI`, `Thank you!`).

## Stage 5: the whole store (`Header.tsx`, `App.tsx`, TODOs 9–10) — `Stage5Store.test.tsx`

- **TODO 9:** the header starts with a **Skip to content** link to `#main`, the first Tab stop on the page. The menu is a `<nav aria-label="Main">`. The cart count reads `Cart: 0 items`, `Cart: 1 item`, `Cart: 3 items`, counting every item's quantity.
- **TODO 10:** `App` keeps the cart with `useReducer(cartReducer, EMPTY_CART)` and the view (`"shop"` or `"checkout"`) with `useState`. **Checkout** swaps the shop for `Checkout`, **Back to the shop** swaps back, and a placed order clears the cart.

The last check is a whole customer journey: search, filter, add, change a quantity, check out and pay by UPI.

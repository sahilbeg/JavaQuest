# CAPSTONE: Launch dhaaga.in on Next.js

Build the Next.js store Meera Aunty launches on Friday: a catalogue, item pages, a stock API, a cart and checkout that places real orders in the data layer. Five stages, one check file each. Do them in order, and run the checks after each stage.

Already done, don't change: `src/lib/money.js` (`formatRupees`), most of `src/lib/store.js`, `CART_COOKIE` and `addToCartValue` in `src/lib/cart.js`, `src/components/AddToCartButton.jsx`, `src/app/cart/page.jsx` and `src/app/orders/[id]/page.jsx`.

The store starts with these items, in this order:

| id | name | paise | stock |
|---|---|---|---|
| DC-1042 | Golu the elephant | 45000 | 7 |
| DC-1101 | Marigold coaster set | 15000 | 12 |
| DC-1205 | Indigo keychain | 12000 | 0 |
| DC-1301 | Tote bag | 89900 | 2 |
| DC-1402 | Baby booties | 35000 | 4 |

## Stage 1: pricing (`src/lib/pricing.js`, TODO 1)

`orderTotals(lines)` takes lines like `[{ paise: 45000, qty: 2 }]` and returns `{ subtotal, offer, delivery, total }`, all in paise:

- `subtotal`: every line's `paise × qty`, added up.
- `offer`: 10% of the subtotal, rounded **down**, only when the subtotal is **above** 150000. Otherwise 0.
- `delivery`: 6000, but 0 when the subtotal minus the offer is **above** 99900, and 0 for an empty cart.
- `total`: subtotal − offer + delivery.

## Stage 2: layout and catalogue (TODOs 2–3)

- **TODO 2**, `src/app/layout.jsx`: `<html lang="en-IN">`, and `metadata` with `metadataBase: new URL("https://dhaaga.in")`, `title: { default: "Dhaaga & Co.", template: "%s | Dhaaga & Co." }` and any description.
- **TODO 3**, `src/app/page.jsx`: under the `<h1>`, a `<ul>` with one `<li>` per item, in order. Each `<li>` starts with a `Link` to `/items/<id>` whose text is the item's name, then the price (like `₹450.00`), then the words `Sold out` when the stock is 0.

## Stage 3: item page and stock API (TODOs 4–5)

- **TODO 4**, `src/app/items/[id]/page.jsx`:
  - `ItemPage` awaits `params` and calls `notFound()` for an unknown id. It shows the name in an `<h1>`, the price, the description, `7 in stock` (or `Out of stock` at 0), and `<AddToCartButton id={item.id} stock={item.stock} />`.
  - `generateMetadata` returns `{ title: item.name, description: item.description, alternates: { canonical: "/items/DC-1042" } }`, or `{ title: "Item not found", robots: { index: false } }`.
- **TODO 5**, `src/app/api/stock/route.js`: `GET /api/stock?id=DC-1042` answers `{ id, stock }` from the store. No id: **400** `{ error: "Missing id" }`. Unknown id: **404** `{ error: "No item DC-9999" }`.

## Stage 4: orders (TODOs 6–7)

- **TODO 6**, `createOrder(order)` in `src/lib/store.js` takes `{ name, phone, lines: [{ id, qty, paise }], totals }`.
  - Add up the quantity asked for each id: the same id can appear twice.
  - If any item doesn't exist or has less stock than that, return `null` and change **nothing**.
  - Otherwise take the stock away, save `{ id: "order-1", name, phone, lines, totals }` (then `order-2`, and so on), and return a copy.
- **TODO 7**, `placeOrder(prevState, formData)` in `src/app/actions.js`. The form sends one `line` field per cart line (`"DC-1042:2"`; read them with `formData.getAll("line")`), plus `name` and `phone`. Check in this order, returning `{ error }` for the first problem:
  1. no `line` fields: `"Your cart is empty"`;
  2. a name that's empty once trimmed: `"Please enter your name"`;
  3. a phone that isn't exactly 10 digits once spaces are removed: `"Please enter a 10-digit phone number"`;
  4. a line whose item doesn't exist, or whose quantity isn't a whole number from 1 to 5: `"Something in your cart can't be sold. Please check it."`;
  5. `createOrder` returning `null`: `"Some items just sold out. Please check your cart."`

  Build lines `{ id, qty, paise }` with the **store's** price, work out `totals` with `orderTotals`, save the trimmed name and the 10-digit phone (`"9876543210"`), then `redirect("/orders/order-1")`.

## Stage 5: the cart (TODOs 8–9)

- **TODO 8**, `readCart(cookieStore)` in `src/lib/cart.js`: `cookieStore.get("cart")` gives `{ name, value }` or `undefined`. The value looks like `"DC-1042:2,DC-1101:1"`. Return `[{ id, name, paise, qty }]` in cookie order, with name and price from the store. Skip unknown items and quantities that aren't whole numbers from 1 to 5. No cookie: `[]`.
- **TODO 9**, `CartView({ lines })` in `src/components/CartView.jsx`:
  - No lines: the text `Your cart is empty.` and a link **Back to the shop** to `/`, and no form.
  - Otherwise a `<li>` per line reading exactly `Golu the elephant × 2: ₹900.00`, then paragraphs `Subtotal: ₹1,799.00`, `Diwali offer: ₹179.90 off` (only when there is an offer), `Delivery: ₹60.00` or `Delivery: Free`, and `Total: ₹1,619.10`.
  - A `<form action={formAction}>` with one `<input type="hidden" name="line" value="DC-1042:2">` per line, an input labelled **Your name** (`name="name"`), one labelled **Phone number** (`name="phone"`), `state.error` in a `role="alert"` paragraph when there is one, and a **Place order** submit button, disabled while `pending`.

The checks mock `next/navigation`: `notFound()` throws `NEXT_NOT_FOUND` and `redirect(to)` throws `NEXT_REDIRECT <to>`. Each check starts with `resetStore()`.

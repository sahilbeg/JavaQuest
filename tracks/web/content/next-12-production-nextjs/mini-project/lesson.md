## The design

This is the store Meera Aunty launches on Friday. You've built every piece before, one ticket at a time: pages from folders, server components, route handlers, server actions, a data layer, metadata, security and tests. Now they come together into one small, complete Next.js app.

### The store on one page

- **Goal:** a customer anywhere in India finds Golu on Google, sees the right price with the Diwali offer and free delivery, and places an order, with a mouse, a finger, a keyboard or a screen reader.
- **Non-goals:** no login and no real UPI payment yet. An order is saved with a name and a phone number, and Dhaaga confirms it on WhatsApp.
- **Pages:** the catalogue at `/`, an item page at `/items/[id]`, the cart at `/cart`, and a thank-you page at `/orders/[id]`.
- **API:** `GET /api/stock?id=...` for the delivery partner's app.
- **Where things run:** every page is a server component. Only the Add to cart button and the cart's checkout form run in the browser.
- **Money:** always whole paise, worked out on the server from the store's prices, shown with `formatRupees`.

### How the parts fit

```animate
title: One order through the Next.js store
kind: scene
nodes:
  - { id: phone, label: Customer's phone, emoji: "📱", value: "cart: empty", row: 1, col: 0 }
  - { id: pages, label: "Pages (server)", emoji: "📄", row: 0, col: 2 }
  - { id: action, label: placeOrder, emoji: "⚙️", row: 2, col: 2 }
  - { id: store, label: Data layer, emoji: "📒", value: "Golu: 7", row: 1, col: 4 }
steps:
  - say: The customer opens Golu's page. The server component reads the store and sends finished HTML.
    send: [{ from: phone, to: pages, label: "GET /items/DC-1042" }, { from: pages, to: store, label: "getItem" }, { from: pages, to: phone, label: "HTML", back: true }]
  - say: Add to cart is the one client island. It writes the cart cookie in the browser and opens the cart.
    set: { phone: "cart: DC-1042:2" }
  - say: The cart page reads the cookie on the server with readCart, using the store's names and prices.
    send: [{ from: phone, to: pages, label: "GET /cart + cookie" }, { from: pages, to: phone, label: "cart and form", back: true }]
  - say: Place order calls the server action with the cart lines, a name and a phone. It checks every field.
    send: { from: phone, to: action, label: "line=DC-1042:2, name, phone" }
  - say: The action prices the lines from the store, applies the rules, and createOrder takes the stock.
    send: { from: action, to: store, label: "createOrder" }
    set: { store: "Golu: 5, order-1" }
    focus: [store]
  - say: Then it redirects to the thank-you page. The browser follows, and the order is placed.
    send: { from: action, to: phone, label: "redirect /orders/order-1", back: true }
```

### The price rules

These come from Dhaaga's rule book, the same everywhere in the course:

| Rule | In paise |
|---|---|
| Subtotal | every line's `paise × qty`, added up |
| Diwali offer | 10% off a subtotal **above** 150000, rounded **down** |
| Delivery | 6000, free when the total after the offer is **above** 99900; nothing for an empty cart |
| Total | subtotal − offer + delivery |

"Above" means strictly more. Exactly ₹999 still pays delivery, and exactly ₹1,500 gets no offer. The checks test both edges.

### Trust nothing from the browser

The cart cookie and the checkout form both come from the customer's phone, so either can be edited. That's why:

- `readCart` takes only ids and quantities from the cookie, and looks up names and prices in the store. It skips anything it doesn't recognise.
- `placeOrder` checks every field again on the server, even though the form looks right. A server action is a public door, as you learned in [Security: secrets, headers and input](/t/next-12-production-nextjs/topic-04-security).
- `createOrder` checks stock for the whole order before it changes anything. Either every line is taken out of stock, or none is.

### Why the cart page isn't tested directly

`app/cart/page.jsx` calls `cookies()` from `next/headers`, which only works inside a running Next.js server. So the logic sits in two small pieces a test can call: `readCart(cookieStore)` takes any object with a `get` method, and `CartView` takes plain lines. The page itself is three lines that wire them together. Keeping framework calls at the edge, and logic in plain functions, is what makes code easy to test.

## What you build

Five stages, one check file each. Do them in order, and run the checks after each stage. The README has every exact text the checks look for.

| Stage | Checks | TODOs | What you write |
|---|---|---|---|
| 1 | `Stage1Pricing` | 1 | `orderTotals`: subtotal, offer, delivery, total |
| 2 | `Stage2Catalogue` | 2–3 | the root layout's language and metadata, and the catalogue page |
| 3 | `Stage3Item` | 4–5 | the item page with `generateMetadata` and `notFound`, and `GET /api/stock` |
| 4 | `Stage4Orders` | 6–7 | `createOrder` in the data layer, and the `placeOrder` server action |
| 5 | `Stage5Cart` | 8–9 | `readCart` from the cookie, and `CartView` with its checkout form |

The checks call your server components, route handler and server action directly, as in [Testing a Next.js app](/t/next-12-production-nextjs/topic-01-testing-next-apps): real `Request` and `Response` objects, `FormData` you'd get from the form, `params` as a Promise, and `next/navigation` mocked.

### The rubric: green checks are the floor

All five stages green means the store works. That's the floor, not the finish line. A reviewer at a good company also asks: would I be happy to put my name on this launch?

Once your checks pass, review your own code against this rubric. Mark each line **Meets** or **Not yet**.

| Area | Meets the bar when… |
|---|---|
| Correctness | Every price is whole paise from the store, worked out in one place, and both "above" edges are right. |
| Security | No price or name comes from the browser unchecked, every action field is validated, and nothing secret or personal reaches a page that doesn't need it. |
| Performance | Pages are server components, the only client code is the button and the form, and independent data calls run together. |
| Accessibility | One `<h1>` per page, `lang="en-IN"`, a skip link, labels on every field, named buttons, and errors in an alert. |
| SEO | A title per page through the template, a description, canonical addresses, and `noindex` on the cart and not-found pages. |
| Readability | Small functions, honest names, no leftover TODO lines or dead code. |
| Tests | You added one check of your own, such as placing an order that sells out the last tote bag. |

```callout tip
"Ready to launch" means every stage green and every rubric line at Meets. The solution opens once your checks pass. Compare yours with it line by line, the way a reviewer would.
```

## Recap

- The store is server components around one in-memory data layer, with two small client islands: the Add to cart button and the checkout form.
- Prices follow the rule book: subtotal, 10% off above ₹1,500 (rounded down), ₹60 delivery unless the total after the offer is above ₹999, all in paise and all on the server.
- The server action checks the cart, name, phone and every line, prices them from the store, saves through `createOrder`, and redirects to the thank-you page.
- `createOrder` checks stock for the whole order before changing anything, and `readCart` trusts only ids and quantities from the cookie.
- Framework calls like `cookies()` stay at the edge, so the logic is plain functions that tests call directly.

## Common mistakes

**1. Delivery on the wrong number, or with the wrong sign.**

```js
const delivery = subtotal >= 99900 ? 0 : 6000;
```

Symptom: the stage 1 check says exactly ₹999 should still pay ₹60 delivery. Fix: use `>`, compare the total **after** the offer, and charge nothing for an empty cart.

**2. Changing stock before checking every line.**

```js
for (const line of order.lines) {
  const item = items.find((candidate) => candidate.id === line.id);
  if (!item || item.stock < line.qty) break;
  item.stock -= line.qty;
}
```

Symptom: the stage 4 check says Golu's stock must not change when the order is refused, but it went from 7 to 6. Fix: check all lines first, and only then take the stock away.

**3. Taking the price from the form or the cookie.**

```js
lines.push({ id, qty, paise: Number(formData.get("paise")) });
```

Symptom: a customer edits the form and pays 1 paisa for Golu. Fix: look the item up with `getItem(id)` and use `item.paise`.

**4. Forgetting that one form field can repeat.**

```js
const entries = [formData.get("line")];
```

Symptom: only the first cart line is ordered, and the saved order is missing the tote bag. Fix: `formData.getAll("line")` returns every value.

**5. Reading params without await.**

```jsx
const item = await getItem(params.id);
```

Symptom: the stage 3 check says the h1 should be Golu's name, but the page calls `notFound()` for every item. Fix: `const { id } = await params;` first.

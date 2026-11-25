## The design

This is the store Meera Aunty launches on Friday, in plain JavaScript. You've built every part before, one ticket at a time: prices in paise, render functions, a store with subscribers, forms, focus and tests. Now they come together into one small, complete app, with no framework at all.

### The store on one page

- **Goal:** a customer anywhere in India finds a product, sees the right price, and pays by UPI, with a mouse, a finger, a keyboard or a screen reader.
- **Non-goals:** no login, no real payment gateway, no photos yet. The UPI step checks the ID and places the order; the server work comes later.
- **Views:** the shop (catalogue and cart side by side) and checkout (three steps). `startStore` swaps one for the other inside `<main>`.
- **State:** the cart lines live in one store, made with `createStore(cartReducer, [])`. The search and category live in the catalogue's own inputs. The checkout's step lives in the checkout. Each piece of state has exactly one home.
- **Money:** always whole paise, shown with `rupees`.
- **Safety:** every element is built with `el`, which only ever puts text in as text.
- **Accessibility:** a skip link, landmarks, labels on every field, named buttons, a live cart count, an alert for errors, and focus moved on purpose.

### How the parts fit

The store is the single source of truth. Views subscribe to it and redraw themselves; they never talk to each other directly.

```animate
title: One order through the plain-JavaScript store
kind: scene
nodes:
  - { id: catalogue, label: Catalogue view, emoji: "🧸", row: 1, col: 0 }
  - { id: store, label: Cart store, emoji: "🧠", value: "0 lines", row: 1, col: 2 }
  - { id: header, label: Header count, emoji: "🧭", value: "Cart: 0 items", row: 0, col: 4 }
  - { id: cart, label: Cart view, emoji: "🛒", value: "empty", row: 2, col: 4 }
  - { id: checkout, label: Checkout view, emoji: "💳", value: closed, row: 3, col: 2 }
steps:
  - say: The customer searches "golu" and presses Add. The catalogue only dispatches an action. It doesn't own the cart.
    send: { from: catalogue, to: store, label: "dispatch add" }
    set: { store: "1 line" }
  - say: The store runs the reducer and calls every subscriber with the new lines.
    send: [{ from: store, to: header, label: "lines" }, { from: store, to: cart, label: "lines" }]
    set: { header: "Cart: 1 item", cart: "₹510.00" }
  - say: Each view works out what it needs from the lines, with cartTotals. Nothing is stored twice.
    focus: [cart]
  - say: Checkout replaces the shop. The cart view unsubscribes first, so no listener is left behind.
    send: { from: cart, to: checkout, label: "onCheckout" }
    set: { checkout: "Address", cart: "unsubscribed" }
    show: [checkout]
  - say: The address and the UPI ID pass their checks. Checkout reports the order is placed.
    send: { from: checkout, to: store, label: "dispatch clear" }
    set: { checkout: "Done: order-17", store: "0 lines" }
  - say: The header is still subscribed, so its live count announces the empty cart.
    send: { from: store, to: header, label: "lines" }
    set: { header: "Cart: 0 items" }
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

### Redraws that keep the customer's place

A plain-JavaScript view redraws by replacing its elements. That's simple and safe, but it has one trap you met in [INC-08](/t/vanilla-08-components-without-a-framework/boss): replacing the element that has focus throws the customer out of it.

So the cart remembers the `id` of the focused element before it redraws, and focuses the new element with that `id` afterwards. A keyboard user who changes Golu's quantity stays on Golu's quantity. When a line is removed, its Remove button is gone for good, so focus moves to the cart's heading instead, as in [Accessibility in depth](/t/vanilla-10-professional-vanilla/topic-02-accessibility-in-depth).

### Listeners once, subscriptions undone

Each view adds its listeners **once**, when it is mounted, using delegation on its list. Its `render` function only draws. That avoids the "two Golus per click" bug from [Code reviews](/t/vanilla-10-professional-vanilla/topic-07-code-reviews).

`mountCart` returns the unsubscribe function from the store. When checkout replaces the shop, the app calls it, so the old cart view stops redrawing a part of the page that no longer exists.

## What you build

Five stages, one check file each. Do them in order, and run the checks after each stage. The README has every exact text the checks look for.

| Stage | Checks | TODOs | What you write |
|---|---|---|---|
| 1 | `Stage1Pricing` | 1 | `cartTotals`: subtotal, offer, delivery, total |
| 2 | `Stage2Catalogue` | 2–4 | `filterProducts`, named Add buttons, the count, and the catalogue's listeners |
| 3 | `Stage3Cart` | 5–8 | `cartReducer`, labelled quantities, named Remove buttons, totals, kept focus, and the cart's listeners |
| 4 | `Stage4Checkout` | 9–11 | the address and UPI checks, `aria-current` on the step, and focus on each step's heading |
| 5 | `Stage5Store` | 12–13 | the skip link, menu and live count in the header, and the switch to checkout |

The last check is a whole customer journey, written the way [Testing a single-page app](/t/vanilla-09-single-page-apps/topic-10-testing-an-spa) taught: it finds fields by their labels and buttons by their names, types, clicks, and checks where focus is.

### The rubric: green checks are the floor

All five stages green means the store works. That's the floor, not the finish line. A reviewer at a good company also asks: would I put my name on this launch?

Once your checks pass, review your own code against this rubric. Mark each line **Meets** or **Not yet**.

| Area | Meets the bar when… |
|---|---|
| Correctness | Every price is whole paise, worked out from the lines in one place, and both "above" edges are right. |
| State | The cart lives only in the store. Counts and totals are computed from the lines, never stored. |
| Accessibility | Every field has a label, every button a name. Tab reaches everything in order, and focus never falls to the top of the page. |
| Security | No customer text reaches `innerHTML`, and nothing secret is in the frontend. |
| Performance | Each view redraws its list in one change, and no listener is added more than once. |
| Readability | Small functions, honest names, no leftover TODO lines or `console.log`. |
| Tests | You added one check of your own, such as removing the last item, or a sixth Golu staying at 5. |

```callout tip
"Ready to launch" means every stage green and every rubric line at Meets. The solution opens once your checks pass. Compare yours with it line by line, the way a reviewer would.
```

## Recap

- The store is a few small views around one cart store. The catalogue and checkout keep their own form state; the cart lines live only in the store.
- Prices follow the rule book: subtotal, 10% off above ₹1,500 (rounded down), ₹60 delivery unless the total after the offer is above ₹999, all in paise.
- Views subscribe to the store and draw from its lines with safe render functions. Listeners are added once, with delegation, and subscriptions are undone when a view goes away.
- Accessibility is built in: a skip link, landmarks, labels, named buttons, a live count, an alert, `aria-current` on the step, and focus kept or moved on purpose.
- The checks end with a whole customer journey, written the way a customer uses the store: labels, button names, typing and clicks.

## Common mistakes

**1. Storing the cart count separately.**

```js
let count = 0;
function onAdd() {
  count += 1;
}
```

Symptom: after changing Golu's quantity to 2, the header still says "Cart: 2 items" for three items. The journey check expects "Cart: 3 items". Fix: work the count out from the lines in the header's subscriber: `lines.reduce((sum, line) => sum + line.qty, 0)`.

**2. Adding listeners inside render.**

```js
function render(list, lines) {
  list.addEventListener("click", () => console.log("remove", lines.length));
}
```

Symptom: after a few changes, one Remove click removes several times, or the console fills up. Fix: add listeners once in the mount function, and let `render` only draw.

**3. A string quantity.**

```js
const qtySelect = document.querySelector("select.qty");
const action = { type: "setQty", id: "DC-1042", qty: qtySelect.value };
```

Symptom: the stage 3 check expects `qty: 3` but gets `"3"`. Later, `"3" + 1` makes `"31"` Golus. Fix: `Number(select.value)`.

**4. Losing focus on every redraw.**

```js
function render(list, lines) {
  list.replaceChildren(...lines.map((line) => document.createElement("li")));
}
```

Symptom: the check says focus should be back on Golu's quantity select. A keyboard user changing a quantity is thrown to the top of the page each time. Fix: remember the focused element's `id` before redrawing, and focus the new element with that `id` afterwards.

**5. Forgetting to unsubscribe.**

```js
function showCheckoutLeaky(main) {
  main.replaceChildren(document.createElement("section"));
}
```

Symptom: the old cart view keeps redrawing a part of the page nobody can see, and a later "Back to the shop" adds a second subscriber. Fix: keep the function `mountCart` returns, and call it before replacing the shop.

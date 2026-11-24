## The design

This is the store Meera Aunty launches on Friday. You've built every part of it before, one ticket at a time: prices in paise, filters, a reducer, forms, focus and tests. Now they come together into one small, complete React app.

### The store on one page

- **Goal:** a customer anywhere in India finds a product, sees the right price, and pays by UPI, with a mouse, a finger, a keyboard or a screen reader.
- **Non-goals:** no login, no real payment gateway, no photos yet. The UPI step checks the ID and places the order; the server work comes later.
- **Pages:** the shop (catalogue and cart side by side) and checkout (three steps). `App` switches between them.
- **State:** the cart lines live in one `useReducer` in `App`. Search and category live in `Catalogue`. Checkout's form and step live in `Checkout`. Each piece of state has exactly one home.
- **Money:** always whole paise, shown with `formatRupees`.
- **Accessibility:** landmarks, a skip link, labels on every field, named buttons, a live cart count, alerts for errors, and focus moved to each new checkout step.

### How the parts fit

```animate
title: One order through the store
kind: scene
nodes:
  - { id: header, label: Header, emoji: "🧭", value: "Cart: 0 items", row: 0, col: 2 }
  - { id: catalogue, label: Catalogue, emoji: "🧸", row: 1, col: 0 }
  - { id: app, label: "App (useReducer)", emoji: "🧠", value: "0 lines", row: 1, col: 2 }
  - { id: cart, label: Cart, emoji: "🛒", value: "₹0.00", row: 1, col: 4 }
  - { id: checkout, label: Checkout, emoji: "💳", value: closed, row: 2, col: 2 }
steps:
  - say: The customer searches "golu" and presses Add. Catalogue only calls onAdd. It doesn't own the cart.
    send: { from: catalogue, to: app, label: "dispatch add" }
    set: { app: "1 line" }
  - say: App's reducer returns new lines. React re-renders everything that reads them.
    send: [{ from: app, to: header, label: "count" }, { from: app, to: cart, label: "lines" }]
    set: { header: "Cart: 1 item", cart: "₹510.00" }
  - say: The cart works out every total from the lines with cartTotals. Nothing is stored twice.
    focus: [cart]
  - say: Checkout replaces the shop. Its heading takes focus, so a screen reader starts in the right place.
    send: { from: cart, to: app, label: "onCheckout" }
    set: { checkout: "Address" }
    show: [checkout]
  - say: The address and the UPI ID pass their checks. Checkout tells App the order is placed.
    send: { from: checkout, to: app, label: "onPlaced" }
    set: { checkout: "Done: order-17" }
  - say: 'App clears the cart, and the header''s live count announces "Cart: 0 items".'
    send: { from: app, to: header, label: "count" }
    set: { app: "0 lines", header: "Cart: 0 items" }
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

### One state, worked out views

The header's count, the cart's totals and the pay button's amount are all **worked out** from the cart lines on every render. None of them is stored in its own `useState`. If they were, one would sooner or later forget to update, and the header would say 3 items while the cart shows 2.

This is the same idea as in [useReducer for the cart](/t/react-09-react-in-depth/topic-05-reducers): keep the least state you can, and compute the rest.

### Checkout and focus

Each checkout step changes most of the screen. A sighted customer sees the new heading. A screen reader user would hear nothing, and their focus would sit on a button that no longer exists.

So whenever the step changes, focus moves to the new step's heading, which has `tabIndex={-1}` so code can focus it. The step list marks the current step with `aria-current="step"`, and errors appear in a `role="alert"` paragraph, which is read out at once. You learned each of these in [Accessibility in depth](/t/react-10-professional-frontend/topic-02-accessibility-in-depth).

## What you build

Five stages, one check file each. Do them in order, and run the checks after each stage. The README has every exact text the checks look for.

| Stage | Checks | TODOs | What you write |
|---|---|---|---|
| 1 | `Stage1Pricing` | 1 | `cartTotals`: subtotal, offer, delivery, total |
| 2 | `Stage2Catalogue` | 2–3 | `filterProducts`, and the search box, category filter, count and named Add buttons |
| 3 | `Stage3Cart` | 4–5 | `cartReducer`, and the cart's labelled quantities, named Remove buttons and totals |
| 4 | `Stage4Checkout` | 6–8 | address checks, the UPI check and `onPlaced`, and focus on each step's heading |
| 5 | `Stage5Store` | 9–10 | the skip link, menu and live count in the header, and `App`'s state and views |

The last check is a whole customer journey, the way [Testing components like a user](/t/react-10-professional-frontend/topic-01-testing-components) taught: it searches, filters, adds, changes a quantity, checks out and pays, using only labels, roles and the keyboard and mouse.

### The rubric: green checks are the floor

All five stages green means the store works. That's the floor, not the finish line. A reviewer at a good company also asks: would I be happy to put my name on this launch?

Once your checks pass, review your own code against this rubric. Mark each line **Meets** or **Not yet**.

| Area | Meets the bar when… |
|---|---|
| Correctness | Every price is whole paise, worked out from the lines in one place, and both "above" edges are right. |
| State | Each piece of state has one home. Counts and totals are computed, never stored twice. |
| Accessibility | Every field has a label, every button a name. Tab reaches everything in order, and focus moves on each checkout step. |
| Security | No customer text becomes HTML, and nothing secret is in the frontend. |
| Performance | No work repeated needlessly on every render, and nothing heavy loaded before it's needed. |
| Readability | Small components, honest names, no leftover TODO lines or dead code. |
| Tests | You added one check of your own, such as removing the last item, or going back from checkout to the shop. |

```callout tip
"Ready to launch" means every stage green and every rubric line at Meets. The solution opens once your checks pass. Compare yours with it line by line, the way a reviewer would.
```

## Recap

- The store is a few small components around one cart reducer in `App`. Search and filters live in the catalogue, the form and step in checkout.
- Prices follow the rule book: subtotal, 10% off above ₹1,500 (rounded down), ₹60 delivery unless the total after the offer is above ₹999, all in paise.
- Counts and totals are computed from the cart lines on every render, never stored separately.
- Accessibility is built in: landmarks, a skip link, labels, named buttons, a live count, alerts, and focus on each new checkout step.
- The checks finish with an end-to-end journey written like a customer: roles, labels and real clicks and typing.

## Common mistakes

**1. Storing the cart count in its own state.**

```jsx
const [count, setCount] = useState(0);
// ...setCount(count + 1) on every Add, and forgotten on Remove and setQty
```

Symptom: after changing Golu's quantity to 2, the header still says "Cart: 2 items" for three items. The end-to-end check expects "Cart: 3 items". Fix: `const count = lines.reduce((sum, line) => sum + line.qty, 0);`, worked out on every render.

**2. A free-delivery check on the wrong number, or with the wrong sign.**

```js
const delivery = subtotal >= 99900 ? 0 : 6000;
```

Symptom: the stage 1 check says ₹999 is not above ₹999, so delivery is still ₹60. Fix: use `>` and compare the total **after** the offer, and make an empty cart pay nothing.

**3. A label that isn't tied to its field.**

```jsx
<label className="visually-hidden">Quantity for {line.name}</label>
<select value={line.qty} onChange={...}>
```

Symptom: the check can't find a select labelled "Quantity for Golu the elephant", and neither can a screen reader. Fix: give the select an `id` and the label a matching `htmlFor`.

**4. Keeping a string quantity.**

```jsx
onChange={(event) => onSetQty(line.id, event.target.value)}
```

Symptom: the check expects `["DC-1042", 3]` but gets `["DC-1042", "3"]`, and later `"3" * 45000` works by luck while `"3" + 1` gives `"31"`. Fix: `Number(event.target.value)`.

**5. Changing the checkout step without moving focus.**

```jsx
if (problem === "") setStep("Payment");
```

Symptom: the check says focus should move to the new step's heading. A screen reader user hears nothing after pressing Continue. Fix: `useEffect(() => { headingRef.current.focus(); }, [step]);`.

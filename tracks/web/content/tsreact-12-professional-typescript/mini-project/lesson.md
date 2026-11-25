## The design

This is the last ticket of the course. You build the whole dhaaga.in store, typed from top to bottom, and test it end to end, the way a real launch is checked.

Everything in it is something you've already built once. The new part is putting it together so the types hold the pieces in place:

- **One file of shared types.** `types.ts` holds `Product`, `CartLine`, `CartAction`, `Totals`, `Address`, `Step` and `PlacedOrder`. Every other file imports its shapes from there, so a change to a shape shows up everywhere at once.
- **Pure logic, then components.** Prices, filters and the cart reducer are plain functions with typed inputs and outputs. The components only show their results and send actions.
- **Unions for every choice.** A category is `Category | "All"`, a cart change is a `CartAction`, a checkout step is a `Step`. A misspelt choice is a type error, and an exhaustive `switch` proves every case is handled.
- **Accessible by default.** Labels on every field, buttons named after their item, a skip link, landmarks, and focus that follows the checkout steps.

```animate
title: One Golu, from a click to the header
kind: scene
nodes:
  - { id: catalogue, label: Catalogue, emoji: "🧸", row: 0, col: 0 }
  - { id: app, label: "App (useReducer)", emoji: "🏠", value: "0 lines", row: 1, col: 2 }
  - { id: reducer, label: cartReducer, emoji: "🧮", row: 2, col: 2 }
  - { id: cart, label: Cart, emoji: "🛒", value: "empty", row: 1, col: 4 }
  - { id: header, label: Header, emoji: "🔝", value: "Cart: 0 items", row: 0, col: 4 }
steps:
  - say: The customer presses Add Golu the elephant to cart. Catalogue calls onAdd with Golu's Product.
    send: { from: catalogue, to: app, label: "onAdd(product)" }
  - say: App dispatches a typed action. A CartAction can only be add, setQty, remove or clear, so a typo can't compile.
    send: { from: app, to: reducer, label: '{ type: "add", product }' }
  - say: The reducer returns a new list of CartLines, with Golu at qty 1. The old list is left alone.
    send: { from: reducer, to: app, label: "CartLine[]", back: true }
    set: { app: "1 line" }
  - say: The Cart gets the new lines. cartTotals works out ₹450.00, plus ₹60 delivery.
    send: { from: app, to: cart, label: "lines" }
    set: { cart: "Total: ₹510.00" }
  - say: The Header gets the count, the sum of every qty, and says it in words a screen reader reads well.
    send: { from: app, to: header, label: "cartCount={1}" }
    set: { header: "Cart: 1 item" }
```

### The rules every stage agrees on

These are Dhaaga's rules, the same as in every earlier ticket:

| Rule | In paise |
|---|---|
| Delivery inside Pune | 6000, free when the order after the offer is above 99900 |
| The Diwali offer | 10% off, rounded down, when the subtotal is above 150000 |
| Most of one item | 5 |

## What you build

Five stages, in order. Each has its own check file, so run the checks after each one. The exact rules are in the README.

1. **Prices** (`pricing.ts`): `cartTotals` gives the subtotal, the offer, delivery and the total.
2. **The catalogue** (`filters.ts`, `Catalogue.tsx`): search and a category filter, with a count in a `role="status"` line and Add buttons named after their item.
3. **The cart** (`cart.ts`, `Cart.tsx`): a reducer for the `CartAction` union with an exhaustive check, labelled quantity selects, named Remove buttons and the totals.
4. **Checkout** (`Checkout.tsx`): address rules, a UPI ID check, and focus that moves to each step's heading.
5. **The whole store** (`Header.tsx`, `App.tsx`): a skip link, a named menu, the item count, and the shop and checkout views joined by one reducer.

### Syntax: narrowing a select's value

```tsx
function chooseCategory(event: ChangeEvent<HTMLSelectElement>) {
  const choice = event.target.value;
  if (isCategoryFilter(choice)) setCategory(choice);
}
```

A `<select>` always gives a `string`, but the state is a `CategoryFilter`. The type guard checks the text and narrows it, so `setCategory` accepts it without an `as`.

```callout tip
Run the type check as often as the tests. In the editor, red lines are free; in a check run, a type error stops everything before a single check runs.
```

### Testing it end to end

```callout jargon
An **end-to-end test** uses the whole app the way a customer does, from the first click to the last screen, instead of testing one piece alone.
```

The last check renders the whole `App` and plays a customer's visit: search for Golu, add him, filter to Accessories, add a tote bag, change a quantity, check out and pay by UPI. It finds everything by role and label, as in [Testing typed components](/t/tsreact-12-professional-typescript/topic-01-testing-typed-components), so it also proves Mrs. Bhosale can find everything with her screen reader.

## Recap

- Keep every shared shape in one types file, so a change to a shape reaches every part of the store at once.
- Write the rules as pure, typed functions first: prices, filters and the cart reducer. Components only show results and send actions.
- Model every choice as a union (`CategoryFilter`, `CartAction`, `Step`), narrow strings from the page with type guards, and end switches with a `never` check.
- Build accessibility in: labels, named buttons, a skip link, landmarks and focus that follows the steps.
- Test each piece alone, then the whole store end to end, finding things by role and label like a customer.

## Common mistakes

**1. Casting the select's value.**

```tsx
<select value={category} onChange={(event) => setCategory(event.target.value as CategoryFilter)}>
```

Symptom: it compiles, and a bad value from the page would slip straight into state. Fix: check it with `isCategoryFilter` first.

**2. Changing the cart inside the reducer.**

```ts
const line = lines.find((l) => l.id === product.id);
if (line) line.qty += 1;
```

Symptom: the stage 3 check says the old lines changed, and the header count stops updating, because React sees the same array. Fix: return `lines.map(...)` with a new line object.

**3. Giving delivery to an empty cart.**

```ts
const delivery = subtotal - discount > 99900 ? 0 : 6000;
```

Symptom: an empty cart shows "Total: ₹60.00". Fix: delivery is also 0 when there are no lines.

**4. Forgetting the singular.**

```tsx
<p role="status">{shown.length} products</p>
```

Symptom: the check says "1 products" should be "1 product". A screen reader reads it aloud, so it should sound right. Fix: `shown.length === 1 ? "product" : "products"`.

**5. Moving to the next step without moving focus.**

```tsx
setStep("Payment");
```

Symptom: the stage 4 check says focus should move to "Pay by UPI". Mrs. Bhosale's screen reader stays on a button that no longer exists. Fix: `useEffect(() => { headingRef.current?.focus(); }, [step]);` with `tabIndex={-1}` on the heading.

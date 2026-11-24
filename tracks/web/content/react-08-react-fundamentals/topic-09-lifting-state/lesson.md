## Hook

On a busy Sunday, two people at the stall each kept their own tally of coaster sets sold. At closing time, one said 14 and the other said 11. Which one was right? Nobody knew.

The fix was one notebook on the counter that both of them wrote in. In React, that notebook is state kept in one parent component, which every child reads from.

## Concept

### Two components, one piece of data

In the last tickets, each component kept its own state. That works while only one component cares about the data. But the cart is needed in two places: each product card shows how many of it are in the cart, and the summary at the top shows the total.

If each card keeps its own count, the summary has no way to see them. State only flows down, never sideways or up.

### Lifting state up

```callout jargon
**Lifting state up** means moving state out of a child and into the closest parent that all the interested components share. The parent then passes the value down as props.
```

For the store, the cards and the summary both sit inside `Shop`. So the cart moves into `Shop`:

```jsx
function Shop({ products }) {
  const [cart, setCart] = useState({});
  // ...
  return (
    <main>
      <CartSummary count={count} totalPaise={totalPaise} />
      {products.map((p) => (
        <ProductCard key={p.id} product={p} inCart={cart[p.id] ?? 0} onAdd={addToCart} />
      ))}
    </main>
  );
}
```

`cart[p.id] ?? 0` reads this product's quantity, or 0 if it is not in the cart yet. `??` gives the right side when the left side is `null` or `undefined`.

### Values down, events up

The card no longer owns the count, so it cannot change it. Instead, it calls a function the parent gave it:

```jsx
function ProductCard({ product, inCart, onAdd }) {
  return (
    <article className="product-card">
      <h3>{product.name}</h3>
      <p className="in-cart">In cart: {inCart}</p>
      <button type="button" onClick={() => onAdd(product.id)}>Add to cart</button>
    </article>
  );
}
```

Watch a tap travel up to the parent and the new values travel back down:

```animate
title: One cart, read by everyone
kind: scene
nodes:
  - { id: shop, label: "Shop (cart state)", emoji: "🏪", value: "{}", row: 0, col: 1 }
  - { id: summary, label: "CartSummary", emoji: "🧾", value: "0 items", row: 1, col: 0 }
  - { id: card, label: "ProductCard: Golu", emoji: "🐘", value: "In cart: 0", row: 1, col: 2 }
steps:
  - say: The cart lives in Shop, the parent of both the summary and the cards. Nobody else keeps a copy.
    focus: [shop]
  - say: The customer taps Add on Golu's card. The card changes nothing itself. It calls the onAdd function it was given.
    send: { from: card, to: shop, label: "onAdd(\"DC-1042\")" }
  - say: Shop's handler calls setCart with a new object. React re-renders Shop and everything inside it.
    set: { shop: "{ DC-1042: 1 }" }
  - say: Shop works out the count and passes it down. The summary shows 1 item.
    send: { from: shop, to: summary, label: "count={1}" }
    set: { summary: "1 item" }
  - say: The same render passes inCart down to Golu's card. Both read from one cart, so they can never disagree.
    send: { from: shop, to: card, label: "inCart={1}" }
    set: { card: "In cart: 1" }
```

```callout jargon
A **single source of truth** means each piece of data is stored in exactly one place. Everything else reads it from there, or works it out from it.
```

### Syntax: updating an object in state

```jsx
function addToCart(id) {
  setCart((old) => ({ ...old, [id]: (old[id] ?? 0) + 1 }));
}
```

This makes a brand new object. `...old` copies every product already in the cart. `[id]: ...` then sets this product's quantity, using the value of `id` as the key name.

Watch the old object stay as it was:

```animate
title: A new cart, not a changed one
kind: code
language: js
code: |
  const before = { "DC-1042": 1 };
  const id = "DC-1043";
  const after = { ...before, [id]: (before[id] ?? 0) + 1 };
  console.log(JSON.stringify(before));
  console.log(JSON.stringify(after));
  console.log(before === after);
steps:
  - line: 1
    objects: { c1: { type: Object, fields: { "DC-1042": 1 } } }
    vars: { before: "@c1" }
    say: The cart holds one Golu.
  - line: 2
    vars: { id: '"DC-1043"' }
    say: The customer adds a Marigold coaster set.
  - line: 3
    objects: { c2: { type: Object, fields: { "DC-1042": 1, "DC-1043": 1 } } }
    vars: { after: "@c2" }
    say: The spread copies Golu into a new object. Then [id] adds the coaster set. before[id] was undefined, so ?? gives 0, plus 1.
  - line: 4
    out: '{"DC-1042":1}'
    say: The old cart is untouched.
  - line: 5
    out: '{"DC-1042":1,"DC-1043":1}'
    say: The new cart has both items.
  - line: 6
    out: "false"
    say: They are two different objects. That difference is exactly what tells React the state changed.
```

```callout jargon
An **immutable update** makes a new object or array with the change, instead of changing the old one. React compares the old state with the new state, and only a new object counts as a change.
```

### Work things out in the parent

```jsx
const count = Object.values(cart).reduce((sum, qty) => sum + qty, 0);
```

The count and the total are not stored. `Shop` works them out from the cart on every render, then passes them down. Storing them separately would give you three tallies to keep in step, which is the Sunday problem again.

### How high to lift

Lift state only as high as the closest parent that needs it. If only the cards and the summary use the cart, `Shop` is right. Lifting it to the very top of the app would make far more components re-render on every tap, for no reason.

## Worked example

At checkout, a customer picks where to collect the order. The choice buttons and the note below them both need the choice, so it lives in their parent.

```jsx
import { useState } from "react";

const PLACES = [
  { id: "stall", name: "FC Road stall", hours: "Sat and Sun, 10:00 to 20:00" },
  { id: "shop", name: "Aundh shop", hours: "Tue to Sun, 11:00 to 21:00" },
];

function PickupButtons({ chosen, onChoose }) {
  return (
    <div className="pickup">
      {PLACES.map((place) => (
        <button key={place.id} type="button" aria-pressed={chosen === place.id} onClick={() => onChoose(place.id)}>
          {place.name}
        </button>
      ))}
    </div>
  );
}

function PickupNote({ chosen }) {
  const place = PLACES.find((p) => p.id === chosen);
  return <p>Collect from the {place.name}: {place.hours}</p>;
}

export function Pickup() {
  const [chosen, setChosen] = useState("shop");
  return (
    <section>
      <PickupButtons chosen={chosen} onChoose={setChosen} />
      <PickupNote chosen={chosen} />
    </section>
  );
}
```

Line by line:

- `Pickup` owns the one piece of state, `chosen`. It is the closest parent of the buttons and the note.
- `PickupButtons` gets the current choice to show which button is pressed, and `onChoose` to report a tap.
- `onChoose={setChosen}` passes the setter itself. Calling `onChoose("stall")` sets the state in the parent.
- `PickupNote` only reads `chosen`. When the customer taps a button, both children re-render with the same new value.

## Try it

React decides "did the state change?" with `Object.is(old, new)`. Run this to see why changing the old object fools it.

```js run
const cart = { "DC-1042": 1 };

const changed = cart;
changed["DC-1042"] = 2;
console.log(Object.is(cart, changed));

const copied = { ...cart, "DC-1042": 3 };
console.log(Object.is(cart, copied));
console.log(cart["DC-1042"], copied["DC-1042"]);
```

The first line prints `true`: it is the same object, so React would think nothing changed and skip the re-render. The copy prints `false`, so React re-renders. The last line prints `2 3`.

## Recap

- When two components need the same state, move it into their closest common parent. This is lifting state up.
- The parent passes values down as props, and passes functions down so children can report events up.
- Keep each piece of data in one place, and work out counts and totals from it.
- Update objects in state by making a new one: `setCart((old) => ({ ...old, [id]: qty }))`. Never change the old object.

## Common mistakes

**1. Copying a prop into the child's own state.**

```jsx
function ProductCard({ product, inCart, onAdd }) {
  const [count, setCount] = useState(inCart);
  return <p className="in-cart">In cart: {count}</p>;
}
```

The card shows the right number at first. After Clear cart, it still shows the old number, because `useState` uses `inCart` only on the first render. Show the prop directly: `In cart: {inCart}`.

**2. Changing the state object, then setting it.**

```jsx
function addToCart(id) {
  cart[id] = (cart[id] ?? 0) + 1;
  setCart(cart);
}
```

Nothing on the page changes. `setCart` got the same object back, so React thinks nothing changed and skips the re-render. Make a new object: `setCart({ ...cart, [id]: (cart[id] ?? 0) + 1 })`.

**3. Storing a total next to the cart.**

```jsx
const [cart, setCart] = useState({});
const [count, setCount] = useState(0);
```

Sooner or later one handler updates the cart and forgets the count, and the summary drifts. Work the count out from the cart instead.

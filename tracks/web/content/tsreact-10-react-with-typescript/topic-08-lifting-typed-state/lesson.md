## Hook

At the Aundh shop, Raju keeps the day's sales in one notebook on the counter. If he and Meera Aunty each kept their own notebook, by evening they would disagree about how many Golus were sold.

Components have the same problem. When two of them need the same information, it must live in one place they both can see. In React, that place is their shared parent.

## Concept

### Two components, one fact

The shop page has three parts: a header showing `Cart (3)`, a list of items with Add buttons, and a cart box with the lines and the total.

If the item list keeps the cart in its own state, only the item list knows about it. The header and the cart box are its siblings, and siblings cannot read each other's state. Data in React flows one way: from parents down to children, through props.

```callout jargon
**Siblings** are components with the same parent. They cannot see each other's state, only what the parent passes them.
```

### Syntax: lifting state up

```tsx
function ShopPage() {
  const [cart, setCart] = useState<CartLine[]>([]);

  return (
    <>
      <CartBadge count={cart.length} />
      <ItemList onAdd={(item) => setCart([...cart, { item, qty: 1 }])} />
      <CartSummary lines={cart} />
    </>
  );
}
```

Move the state up to the closest parent that holds every component needing it. Then pass it down:

- the data, as props, to the components that show it
- callbacks, as props, to the components that change it

```callout jargon
**Lifting state up** means moving state from a child to a parent, so that several children can share it through props.
```

```callout jargon
A **single source of truth** is the one place a piece of data lives. Every other part of the page reads it from there, so they can never disagree.
```

### How the pieces talk

```animate
title: Add travels up, the cart flows down
kind: scene
nodes:
  - { id: list, label: "ItemList", emoji: "🧶", row: 1, col: 0 }
  - { id: shop, label: "ShopPage", emoji: "🧠", value: "cart: []", row: 0, col: 1 }
  - { id: badge, label: "Header badge", emoji: "🛒", value: "Cart (0)", row: 1, col: 1 }
  - { id: summary, label: "CartSummary", emoji: "🧾", value: "empty", row: 1, col: 2 }
steps:
  - say: ShopPage owns the cart. Its three children only get props.
    focus: [shop]
  - say: The customer presses Add on Golu. ItemList can't change the cart itself, so it calls the onAdd prop.
    send: { from: list, to: shop, label: "onAdd(golu)" }
  - say: ShopPage's add function calls setCart with a new array. React re-renders ShopPage.
    set: { shop: "cart: [Golu × 1]" }
  - say: The new cart flows down as props. The header gets the new count.
    send: { from: shop, to: badge, label: "count={1}" }
    set: { badge: "Cart (1)" }
  - say: CartSummary gets the same lines. Both read one source of truth, so they always agree.
    send: { from: shop, to: summary, label: "lines={cart}" }
    set: { summary: "Golu × 1" }
```

### Syntax: typed callback props

```tsx
interface ItemListProps {
  items: readonly Item[];
  onAdd: (item: Item) => void;
}
```

The child describes what it will report; the parent decides what to do about it. Because the callback is typed, `tsc` checks both ends:

- the child must call `onAdd` with an `Item`
- the parent must pass a function that accepts an `Item`

Name callbacks for what happened (`onAdd`, `onRemove`), not for what the parent does with them. The child shouldn't need to know.

### Syntax: updating one object in an array

```tsx
setCart(
  cart.map((line) => (line.item.id === item.id ? { ...line, qty: line.qty + 1 } : line)),
);
```

State must be replaced, never changed in place. To change one line:

- `map` makes a new array.
- The matching line becomes a new object, `{ ...line, qty: line.qty + 1 }`.
- Every other line is kept as it is.

To remove a line, `filter` keeps every line except one: `cart.filter((line) => line.item.id !== id)`.

### Syntax: passing a setter down

```tsx
import type { Dispatch, SetStateAction } from "react";

interface CartBoxProps {
  setCart: Dispatch<SetStateAction<CartLine[]>>;
}
```

Sometimes you see the setter itself passed down. Its type is `Dispatch<SetStateAction<CartLine[]>>`: a function that takes a new cart, or a function from the old cart to a new one.

It works, but it lets the child do anything to the cart. Small, named callbacks like `onRemove(id)` keep the rules in one place: the parent.

## Worked example

A wishlist that two parts of the page share: heart buttons on the items, and a count in the header.

```tsx
import { useState } from "react";

interface Item {
  id: string;
  name: string;
}

function HeartButton({ item, saved, onToggle }: { item: Item; saved: boolean; onToggle: (id: string) => void }) {
  return (
    <button type="button" aria-pressed={saved} onClick={() => onToggle(item.id)}>
      {saved ? "♥" : "♡"} {item.name}
    </button>
  );
}

export function WishlistPage({ items }: { items: readonly Item[] }) {
  const [savedIds, setSavedIds] = useState<string[]>([]);

  function toggle(id: string) {
    setSavedIds(savedIds.includes(id) ? savedIds.filter((s) => s !== id) : [...savedIds, id]);
  }

  return (
    <>
      <p className="wishlist-count">Wishlist ({savedIds.length})</p>
      {items.map((item) => (
        <HeartButton key={item.id} item={item} saved={savedIds.includes(item.id)} onToggle={toggle} />
      ))}
    </>
  );
}
```

Line by line:

- The saved ids live in `WishlistPage`, the parent of both the count and the buttons.
- `HeartButton` has no state. It shows `saved` and reports taps through `onToggle`.
- `toggle` replaces the array: a filtered copy to un-save, or a longer copy to save.
- The count and every button read `savedIds`, so they can't disagree.

## Try it

Here is the cart's add logic without React. Run it, then write a `remove` that uses `filter`.

```ts run
interface Item {
  id: string;
  name: string;
}

interface CartLine {
  item: Item;
  qty: number;
}

function add(cart: CartLine[], item: Item): CartLine[] {
  if (cart.some((line) => line.item.id === item.id)) {
    return cart.map((line) => (line.item.id === item.id ? { ...line, qty: line.qty + 1 } : line));
  }
  return [...cart, { item, qty: 1 }];
}

const golu: Item = { id: "DC-1042", name: "Golu the elephant" };
const coasters: Item = { id: "DC-1043", name: "Marigold coaster set" };

let cart: CartLine[] = [];
cart = add(cart, golu);
cart = add(cart, golu);
cart = add(cart, coasters);
console.log(cart.map((line) => `${line.item.name} × ${line.qty}`).join("; "));
```

It prints `Golu the elephant × 2; Marigold coaster set × 1`. The second Golu raised the quantity instead of adding a new line.

## Recap

- Siblings can't see each other's state. When they need the same data, lift the state to their closest shared parent.
- The parent passes data down as props, and callbacks like `onAdd: (item: Item) => void` let children report changes up.
- One source of truth means the header, the list and the cart box always agree.
- Change state by replacing it: `map` with `{ ...line, qty: line.qty + 1 }` to update, `filter` to remove.
- A setter's type is `Dispatch<SetStateAction<T>>`, but named callbacks keep the rules in the parent.

## Common mistakes

**1. Keeping shared data in one child.**

```tsx
function ItemList({ items }: ItemListProps) {
  const [cart, setCart] = useState<CartLine[]>([]);
  ...
}
```

The list knows about the cart, but the header keeps saying `Cart (0)`. Move the state to the parent and pass `onAdd` down.

**2. Copying props into state.**

```tsx
function CartSummary({ lines }: CartSummaryProps) {
  const [shown, setShown] = useState(lines);
  ...
}
```

The starting value is used once, so the box never sees later additions. Use the `lines` prop directly.

**3. Changing a line in place.**

```tsx
const line = cart.find((l) => l.item.id === item.id);
if (line) line.qty += 1;
setCart(cart);
```

It is the same array, so React may skip the re-render, and the quantity looks stuck. Build a new array with `map` and a new line object.

**4. A callback type that doesn't match.**

```tsx
interface ItemListProps {
  onAdd: (id: string) => void;
}

<ItemList items={ITEMS} onAdd={add} />
```

When `add` takes an `Item`, `tsc` reports that `(item: Item) => void` is not assignable to `(id: string) => void`. Agree on one shape for the callback, and use it on both sides.

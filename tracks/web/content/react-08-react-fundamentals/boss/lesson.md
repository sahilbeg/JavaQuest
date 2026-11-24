## What happened

On the first day of the Diwali sale, customers started removing things from their carts. Then the quantities went strange.

- Mrs. Rao had one Golu, two pairs of baby booties and three Mint keychains. She removed the booties. Her keychains now said 2.
- A customer in Bengaluru had two coaster sets, one Marigold and one Off-white, both called "Coaster set". She removed one, and both disappeared.

Nothing crashes, and the console is quiet. The cart code is in `Cart.jsx`. Each `CartRow` keeps its own quantity in state, so the customer can change it with the dropdown.

```callout analogy
Picture three lockers numbered 1, 2 and 3, each holding a customer's bag. Locker 2's owner leaves, so everyone shuffles their name card up one. The bags stay put. Now the name on locker 2 belongs to someone else, but the bag inside does not.
```

## What you need

### How React matches rows after a removal

On every render, React matches the new list of rows with the old one by key. A row whose key is still there keeps its state, even if its props change.

Watch what happens when the rows are keyed by their position:

```animate
title: The booties leave, their quantity stays
kind: scene
nodes:
  - { id: k0, label: "Row key 0", emoji: "🐘", value: "Golu, qty 1", row: 0, col: 0 }
  - { id: k1, label: "Row key 1", emoji: "🧦", value: "Baby booties, qty 2", row: 0, col: 1 }
  - { id: k2, label: "Row key 2", emoji: "🔑", value: "Mint keychain, qty 3", row: 0, col: 2 }
  - { id: cart, label: "Cart", emoji: "🛒", value: "3 items", row: 1, col: 1 }
steps:
  - say: Three rows, keyed by position. Each row keeps its own qty in state.
    focus: [k0, k1, k2]
  - say: Mrs. Rao removes the baby booties. The cart's list now holds two items, Golu and the keychain.
    send: { from: k1, to: cart, label: "Remove" }
    set: { cart: "2 items" }
  - say: The new list has keys 0 and 1. React keeps the rows with keys 0 and 1, and throws away the row with key 2.
    hide: [k2]
  - say: Row key 1 still holds the booties' state, qty 2. Now it is given the keychain's name and price.
    set: { k1: "Mint keychain, qty 2" }
    focus: [k1]
  - say: The keychain's own quantity, 3, left with the row that was thrown away. To the customer, the wrong item was removed.
    focus: [k1]
```

### Syntax: a key from the data

```jsx
{items.map((item) => (
  <CartRow key={item.id} item={item} onRemove={() => removeItem(item)} />
))}
```

A key must belong to the item, not to its place in the list. Then the row and its state move with the item, whatever happens above it. Keys must also be unique among the rows and stay the same on every render.

### Syntax: removing by id

```jsx
setItems(items.filter((item) => item.id !== target.id));
```

`filter` keeps every item for which the test is true. Compare something that is unique to each item. Two items can share a name, a price or a colour. Only the id is guaranteed to differ.

## The tiers

1. **Bronze** resolves the incident: after removing a row, every row below it still shows its own quantity and line total.
2. **Silver**: a quantity the customer picked in the dropdown survives when a row above it is removed.
3. **Gold**: with two items of the same name but different ids, Remove takes away only the one that was clicked, and React logs no key warnings.

## Recap

- React matches list rows by key. A row with the same key keeps its state, even when it gets new props.
- Position keys break when a row above is removed or added, and random keys throw away every row's state on each render.
- Key each row with the item's id. Remove items by comparing ids, never names.

## Common mistakes

**1. A random key.** `key={Math.random()}` passes bronze, because each row is rebuilt from its item. But every removal rebuilds every row, so the quantities customers picked are lost. Silver catches it.

**2. The name as the key.** `key={item.name}` works until two items share a name. Then two rows have the same key, React warns about it in the console, and gold fails.

**3. Fixing only the key.** The key decides which row keeps which state. Which item gets removed is decided in `removeItem`. Gold needs both.

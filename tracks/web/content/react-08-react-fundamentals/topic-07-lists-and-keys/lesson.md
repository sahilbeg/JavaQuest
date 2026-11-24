## Hook

Dhaaga keeps every product in a register: one line each, with a code, a name and a price. Nobody writes the stall's price board out by hand. You run down the register and make one row per line.

A React list works the same way. You keep the products in an array, and React makes one element per item.

## Concept

### Syntax: map to make elements

```jsx
<ul>
  {products.map((p) => (
    <li key={p.id}>{p.name}</li>
  ))}
</ul>
```

`map` calls the arrow function once per product and collects what it returns into a new array. Here that is an array of `<li>` elements. React draws an array of elements one after another.

Watch `map` turn data into rows. Here the rows are plain text, so it can run as normal JavaScript:

```animate
title: One row per product
kind: code
language: js
code: |
  const products = [
    { id: "DC-1043", name: "Coaster set" },
    { id: "DC-1044", name: "Keychain" },
  ];
  const items = products.map((p) => `<li key=${p.id}>${p.name}</li>`);
  console.log(items.length);
  console.log(items[0]);
  console.log(items[1]);
steps:
  - line: 1
    vars: { products: "[2 products]" }
    say: The data is an array of two product objects.
  - line: 5
    say: map visits each product and calls the arrow function with it.
  - line: 5
    vars: { p: '{ id: "DC-1043", ... }' }
    say: First call. p is the coaster set, and the function returns its row.
  - line: 5
    vars: { p: '{ id: "DC-1044", ... }', items: "[2 rows]" }
    say: Second call, for the keychain. map collects both rows into a new array.
  - line: 6
    out: "2"
    say: Two products in, two rows out.
  - line: 7
    out: "<li key=DC-1043>Coaster set</li>"
    say: Each row carries its product's id as its key.
  - line: 8
    out: "<li key=DC-1044>Keychain</li>"
    say: In React, the rows are JSX elements instead of text, but map works exactly the same.
```

### Keys: a name tag for each row

```callout jargon
A **key** is a special prop that gives each item in a list a name React can recognise. It must be unique among its siblings and stay the same between renders.
```

When the list changes, React compares the new rows with the old ones. Without names, it can only match them by position: first with first, second with second. With keys, it matches each row to the same item, wherever that item moved.

That matters when rows hold something of their own, like typed text or state. Here each delivery row has a note the packer typed, and a new order arrives at the top:

```animate
title: Keys keep each row with its item
kind: scene
nodes:
  - { id: data, label: "Orders", emoji: "📦", value: "2 orders", row: 0, col: 0 }
  - { id: react, label: "React", emoji: "⚛️", row: 0, col: 2 }
  - { id: r0, label: "order-19 (new)", emoji: "🆕", value: "note: (empty)", row: 1, col: 1 }
  - { id: r1, label: "order-17", emoji: "📝", value: "note: Ring the bell", row: 2, col: 1 }
  - { id: r2, label: "order-18", emoji: "📝", value: "note: Leave at gate", row: 3, col: 1 }
steps:
  - say: Two rows, each with a note the packer typed. Their keys are their order ids, order-17 and order-18.
    focus: [r1, r2]
  - say: A new order arrives and goes to the top of the list.
    send: { from: data, to: react, label: "order-19 at the top" }
    set: { data: "3 orders" }
  - say: React looks up each key. order-17 and order-18 still exist, so their rows and typed notes stay exactly as they were.
    focus: [r1, r2]
  - say: order-19 is a key React has not seen, so it makes one fresh row, with an empty note.
    show: [r0]
  - say: With position numbers as keys, the new order would get key 0, the old first row's key. It would take over that row and its note.
    focus: [r0]
```

### Syntax: choosing a key

```jsx
<li key={p.id}>        // good: unique, stable, from the data
<li key={index}>       // risky: changes when items move
<li key={Math.random()}> // wrong: new every render
```

- **Use an id from the data**, like `DC-1042` or `order-17`. It belongs to the item, so it moves with it.
- **The position (index)** is safe only for a list that never changes order and never gains or loses items in the middle. Even then, an id is better.
- **A random key** changes on every render. React thinks every row is brand new, throws the old ones away, and loses whatever they held.

Keys only need to be unique among siblings in one list. Two different lists can use the same ids.

### Syntax: the key goes on the outer element

```jsx
{products.map((p) => (
  <ProductCard key={p.id} name={p.name} paise={p.paise} />
))}
```

Put `key` on whatever the arrow function returns, here the `ProductCard`, not on something inside the card. React uses the key itself; the card never receives it as a prop.

### Syntax: filter, then map

```jsx
{products
  .filter((p) => p.paise < 50000)
  .map((p) => <li key={p.id}>{p.name}</li>)}
```

`filter` keeps only the items you want and returns a new array. Then `map` turns what is left into rows.

### Syntax: sorting a copy

```jsx
const cheapFirst = [...products].sort((a, b) => a.paise - b.paise);
```

`sort` changes the array it is called on. If `products` is a prop, that would change the parent's data behind its back. Spread it into a new array first, with `[...products]`, and sort the copy. (`filter` already makes a new array, so `products.filter(...).sort(...)` is safe too.)

## Worked example

The packing table shows the paid orders, each as its own row component.

```jsx
function OrderRow({ order }) {
  return (
    <li>
      {order.id}: {order.customer}, {rupees(order.paise)}
    </li>
  );
}

export function PaidOrders({ orders }) {
  const paid = orders.filter((order) => order.paid);

  if (paid.length === 0) {
    return <p>No paid orders yet.</p>;
  }

  return (
    <ul className="orders">
      {paid.map((order) => (
        <OrderRow key={order.id} order={order} />
      ))}
    </ul>
  );
}
```

Line by line:

- `filter` keeps only the paid orders. `orders` itself is untouched.
- The early return handles an empty list with a friendly message.
- `map` makes one `OrderRow` per paid order. The key is the order id, like `order-17`, on the `OrderRow` itself.
- Inside `OrderRow`, there is no key to read. The row only gets `order`.

## Try it

Run this. Then delete the `...` and the square brackets around `products` in the first `const` line, and run it again. Both lines now come out sorted, because `sort` changed the original array.

```js run
const products = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-1045", name: "Indigo tote bag", paise: 89900 },
  { id: "DC-1044", name: "Mint keychain", paise: 12000 },
];

const cheapFirst = [...products].sort((a, b) => a.paise - b.paise);
console.log(cheapFirst.map((p) => p.name).join(", "));
console.log(products.map((p) => p.name).join(", "));
```

## Recap

- `array.map((item) => <li key={item.id}>...</li>)` turns data into a list of elements.
- Every item in a list needs a `key` that is unique among its siblings and stays the same between renders.
- Use an id from the data as the key. Positions break when items move, and random keys break every render.
- Put the key on the outer element the map returns. The child never sees it as a prop.
- `filter` before `map` to show some items. Sort a copy, `[...products].sort(...)`, never the prop itself.

## Common mistakes

**1. Leaving out the key.**

```jsx
{products.map((p) => <li>{p.name}</li>)}
```

The list shows, but the console warns that each child in a list should have a unique key prop. Add `key={p.id}` to the `<li>`.

**2. Putting the key on an inner element.**

```jsx
{products.map((p) => (
  <li>
    <span key={p.id}>{p.name}</span>
  </li>
))}
```

The same warning appears. React needs the key on the element the map returns, here the `<li>`.

**3. Using the position when the list changes.**

```jsx
{orders.map((order, index) => <DeliveryRow key={index} order={order} />)}
```

Add an order at the top, and every typed note slides down to the wrong order. The positions changed, so the keys now point at different items. Use `key={order.id}`.

**4. Sorting the prop itself.**

```jsx
const cheapFirst = products.sort((a, b) => a.paise - b.paise);
```

The parent's array is now sorted too, so other parts of the page change order for no clear reason. Sort a copy: `[...products].sort(...)`.

**5. Curly braces with no return in map.**

```jsx
{products.map((p) => {
  <li key={p.id}>{p.name}</li>;
})}
```

The list is empty, with no error. With curly braces, an arrow function needs `return`. Either write `return <li ...>`, or use round brackets: `(p) => ( <li ...> )`.

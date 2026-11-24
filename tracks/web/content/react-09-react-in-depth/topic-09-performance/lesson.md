## Hook

Imagine the shop repainting every price tag whenever one customer buys a keychain. The prices didn't change. Only the till count did. On a quiet day nobody would notice the wasted paint. On Diwali, with a slow painter, the queue would reach the road.

React can do the same: redraw parts of the page whose data never changed. Usually that's fine. On an old phone with a long list, it isn't. This ticket is about spotting that waste and skipping it.

## Concept

### Why a component renders again

A component renders again when:

- its own state changes, or
- its parent renders again.

The second rule surprises people. When `Catalogue` adds 1 to its cart count, React renders `Catalogue`, and then every child inside it, even rows whose props didn't change.

```animate
title: One tap renders every row
kind: scene
nodes:
  - { id: cat, label: Catalogue, emoji: "📋", value: "Cart (0)" }
  - { id: row1, label: "Keychain row", emoji: "🔑", row: 1, col: 0 }
  - { id: row2, label: "Coaster row", emoji: "🟠", row: 1, col: 1 }
  - { id: row3, label: "Tote bag row", emoji: "👜", row: 1, col: 2 }
links:
  - { from: cat, to: row1 }
  - { from: cat, to: row2 }
  - { from: cat, to: row3 }
steps:
  - say: Mr. Pathak taps Add on the keychain. Catalogue's count state changes.
    set: { cat: "Cart (1)" }
    focus: [cat]
  - say: Catalogue renders again. So does every child, by default.
    focus: [row1, row2, row3]
  - say: None of the rows changed. Their item and price are the same as before. That work was wasted.
    set: { row1: rendered, row2: rendered, row3: rendered }
  - say: With memo, each row first compares its new props with the old ones.
    set: { row1: "same props", row2: "same props", row3: "same props" }
  - say: All props are the same, so React reuses each row's last result. Only Catalogue really rendered.
    set: { row1: skipped, row2: skipped, row3: skipped }
    focus: [cat]
```

### Measure before you fix

Rendering is usually fast. Most apps never need the tools in this ticket. Speed fixes also add code that can hide bugs, so find the real cause first.

```callout jargon
A **profiler** is a tool that records what your code did and how long each part took. The React DevTools browser extension has a Profiler tab: press record, use the page, and it shows every component that rendered and why.
```

Kavya's profile showed two costs on each tap: every `ProductRow` rendered again, and `sortByPrice` sorted the whole catalogue again. Those are the two things to fix.

### Syntax: memo

```jsx
const ProductRow = memo(function ProductRow({ item, onAdd }) {
  return <li>{item.name}</li>;
});
```

```callout jargon
**`memo`** wraps a component so React can skip rendering it when its props are the same as last time. The name comes from **memoization**: remembering a result so you don't have to work it out again.
```

`memo` doesn't change what the component shows. It only lets React reuse the last result when nothing it depends on has changed.

### "The same props" means the same values

`memo` compares each prop, one by one, with `Object.is`, like effect dependencies. Numbers and strings compare by value. Objects, arrays and functions compare by **identity**: are they the very same object?

```js run
function makeHandler() {
  return () => console.log("added");
}

const first = makeHandler();
const second = makeHandler();
console.log(first === second);
console.log(Object.is(first, first));
```

Every render of `Catalogue` runs its body again. So `function handleAdd() { ... }`, or an arrow written inline in the JSX, makes a brand-new function each time. To `memo`, a new function is a changed prop, and the row renders anyway.

### Syntax: useCallback

```jsx
const handleAdd = useCallback(() => {
  setCount((c) => c + 1);
}, []);
```

```callout jargon
**`useCallback(fn, deps)`** gives back the same function on every render, until something in `deps` changes. Then it gives the new one.
```

The array works like an effect's. This handler uses no props or state directly: the updater form `(c) => c + 1` always gets the newest count. So its array can be empty, and the function never changes.

If the handler read `count` directly, `count` would have to be in the array, and the function would change on every tap. The updater form keeps it stable.

### Syntax: useMemo

```jsx
const sorted = useMemo(() => sortByPrice(items, order), [items, order]);
```

```callout jargon
**`useMemo(calculate, deps)`** runs `calculate` on the first render and keeps the result. On later renders it hands back the kept result, unless something in `deps` changed. Then it runs `calculate` again.
```

Adding to the cart changes neither `items` nor `order`, so the catalogue isn't sorted again. Choosing "High to low" changes `order`, so it is.

`useMemo` has a second benefit: `sorted` stays the very same array, with the very same item objects. That's what lets each memo'd row see an unchanged `item` prop.

### Don't memo everything

Each `memo`, `useMemo` and `useCallback` costs a little: React must store old values and compare them on every render. For a cheap component or a quick sum, the check can cost more than the work it saves.

Use them when a profile shows a real cost: long lists, slow calculations, or props passed into a memo'd child.

```callout tip
The **React Compiler**, a build tool from the React team, can add this memoising for you automatically. Teams that use it write much less `useMemo` and `useCallback` by hand. Knowing what it does helps you read the code it replaces.
```

## Worked example

Here is the catalogue after the fix.

```jsx
import { memo, useCallback, useMemo, useState } from "react";

export const ProductRow = memo(function ProductRow({ item, onAdd }) {
  return (
    <li>
      {item.name}, {rupees(item.paise)} <button onClick={() => onAdd(item)}>Add {item.name}</button>
    </li>
  );
});

export function Catalogue({ items }) {
  const [count, setCount] = useState(0);
  const [order, setOrder] = useState("low-first");

  const sorted = useMemo(() => sortByPrice(items, order), [items, order]);
  const handleAdd = useCallback(() => setCount((c) => c + 1), []);

  return (
    <section>
      <p>Cart ({count})</p>
      <select value={order} onChange={(e) => setOrder(e.target.value)}>
        <option value="low-first">Low to high</option>
        <option value="high-first">High to low</option>
      </select>
      <ul>
        {sorted.map((item) => (
          <ProductRow key={item.code} item={item} onAdd={handleAdd} />
        ))}
      </ul>
    </section>
  );
}
```

- A tap on Add changes `count`, so `Catalogue` renders.
- `sorted` comes back from `useMemo` unchanged, because `items` and `order` didn't change. No sorting.
- `handleAdd` is the same function as before, thanks to `useCallback`.
- Each row's `item` and `onAdd` are the same, so `memo` skips every row. Only the cart count is drawn again.
- The arrow `() => onAdd(item)` inside the row is new each time the row renders. That's fine: it goes to a plain `<button>`, not to a memo'd component.

## Try it

Open the preview and press Add a few times. The panel under the list counts how many rows rendered and how many sorts ran since the page opened.

Then remove `useCallback` and write `const handleAdd = () => setCount((c) => c + 1);`. Every tap renders every row again, even with `memo`. Put it back.

## Recap

- A component renders again when its state changes or its parent renders. Its children render too, by default.
- Measure with the React DevTools Profiler before adding speed fixes.
- `memo(Component)` skips a render when every prop is the same as last time, compared with `Object.is`.
- `useCallback(fn, deps)` keeps the same function between renders, so a memo'd child doesn't see a "new" prop.
- `useMemo(calculate, deps)` keeps a slow result, like a sorted list, until its inputs change.

## Common mistakes

**1. Passing a new function to a memo'd row.**

```jsx
<ProductRow item={item} onAdd={() => setCount((c) => c + 1)} />
```

The profiler still shows every row rendering on each tap. The arrow is new every render, so `memo` sees a changed prop.

Fix: make the handler once with `useCallback` and pass that.

**2. Leaving something out of the dependency array.**

```jsx
const sorted = useMemo(() => sortByPrice(items, order), [items]);
```

Choosing "High to low" does nothing. The list stays cheapest first, because `order` isn't in the array, so the kept result is reused.

Fix: list everything the calculation reads, `[items, order]`.

**3. Sorting the prop in place.**

```jsx
const sorted = useMemo(() => items.sort((a, b) => a.paise - b.paise), [items]);
```

`sort` changes the array it's called on, so this reorders the parent's `items` behind its back. Other parts of the page that use `items` suddenly see a different order.

Fix: sort a copy, `[...items].sort(...)`.

**4. Memoising things that are already fast.**

```jsx
const label = useMemo(() => `Cart (${count})`, [count]);
```

Nothing breaks, but this does more work than plain code: React stores and compares `count` every render to save one tiny string.

Fix: plain code is all you need.

```jsx
const label = `Cart (${count})`;
```

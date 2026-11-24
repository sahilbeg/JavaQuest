## Hook

Meera Aunty has one pattern for Golu the elephant, written on a card. When two helpers each copied it by hand, one copy lost a row of stitches. Their elephants came out different.

Code copied into two components drifts apart the same way. React lets you put stateful logic in one function that any component can call. That function is a custom hook.

## Concept

### Two components, the same logic

The product grid and the cart drawer both need a cart: a list of lines, a way to add an item, and the count and total. Today each has its own `useState` and its own `add` function. One of them got a bug, the other didn't.

You already pull repeated *calculations* into plain functions. But this logic uses `useState` and `useEffect`, and a plain function can't call those. A custom hook can.

### Syntax: a custom hook

```jsx
function useSomething(input) {
  const [value, setValue] = useState(input);
  // ...more hooks and helper functions
  return { value, setValue };
}
```

```callout jargon
A **hook** is a function from React whose name starts with `use`, like `useState` and `useEffect`. A **custom hook** is your own function that starts with `use` and calls other hooks inside.
```

A custom hook returns whatever the component needs. It can be one value, an array like `useState`, or an object with names. For a cart, an object reads best:

```jsx
export function useCart() {
  const [lines, setLines] = useState([]);
  const count = lines.reduce((sum, line) => sum + line.qty, 0);
  return { lines, count };
}

function CartBadge() {
  const { count } = useCart();
  return <span>Cart ({count})</span>;
}
```

### The rules of hooks

React keeps track of hooks by the order you call them in. The first `useState` in a component is always slot one, the second is slot two, and so on. That only works if the order never changes. So:

- Call hooks only at the top level of a component or a custom hook. Never inside an `if`, a loop or a nested function.
- Call hooks only from React components or custom hooks, not from ordinary functions.
- Start every custom hook's name with `use`, so you and the linter can see it follows these rules.

```callout tip
ESLint's React hooks plugin checks these rules as you type, and most React projects switch it on.
```

### Each call gets its own state

A custom hook shares **logic**, not **state**. When two components call `useCart()`, each one gets its own `useState` inside, so each has its own cart.

```animate
title: Two components call the same hook
kind: scene
nodes:
  - { id: grid, label: ProductGrid, emoji: "🧺" }
  - { id: drawer, label: CartDrawer, emoji: "🛒" }
  - { id: hook1, label: "useCart() for the grid", emoji: "🪝", value: "0 lines", row: 1, col: 0 }
  - { id: hook2, label: "useCart() for the drawer", emoji: "🪝", value: "0 lines", row: 1, col: 1 }
steps:
  - say: Both components call useCart(). The code is the same, written once.
    focus: [grid, drawer]
  - say: But each call runs its own useState. The grid gets one cart, the drawer gets another.
    focus: [hook1, hook2]
  - say: A customer adds a keychain from the grid.
    send: { from: grid, to: hook1, label: "add(KEY)" }
    set: { hook1: "1 line" }
  - say: The drawer's cart has not changed. Its state is separate.
    focus: [hook2]
  - say: To share one cart between them, you need context, which is the next ticket.
    focus: [grid, drawer]
```

### Syntax: updating from the newest state

```jsx
setLines((current) => [...current, newLine]);
```

You met the updater form with timers. It matters for the cart too. A "Buy 2" button might call `add` twice in one click. React waits until the click handler finishes, then applies both updates. If each one used `lines` from this render, the second would overwrite the first.

Here is that in plain JavaScript: a list of updates applied in order, like React does.

```js run
function applyAll(start, updates) {
  let state = start;
  for (const update of updates) {
    state = typeof update === "function" ? update(state) : update;
  }
  return state;
}

const lines = [];
console.log(applyAll(lines, [[...lines, "CST"], [...lines, "KEY"]]).join(", "));
console.log(applyAll(lines, [(l) => [...l, "CST"], (l) => [...l, "KEY"]]).join(", "));
```

The first line prints only `KEY`: both new arrays were built from the same empty `lines`. The updater functions each start from the result of the one before.

### Adding without duplicating lines

When the item is already in the cart, `add` should raise its `qty`, not add a second line. Use `map` to make a new array where only that line changes. Never change the old line in place: React compares the old and new arrays to decide what to redraw.

```jsx
setLines((current) => {
  const found = current.some((line) => line.code === item.code);
  if (found) {
    return current.map((line) => (line.code === item.code ? { ...line, qty: line.qty + 1 } : line));
  }
  return [...current, { ...item, qty: 1 }];
});
```

### Syntax: lazy initial state

```jsx
const [lines, setLines] = useState(() => loadLines(storageKey));
```

To keep the cart after a refresh, the hook starts from what's saved in `localStorage`. If you wrote `useState(loadLines(storageKey))`, the function would run on every render, and React would throw away the result every time after the first.

```callout jargon
**Lazy initial state** means passing `useState` a function instead of a value. React calls it once, on the first render, and never again.
```

### Saving with an effect

Writing to `localStorage` is a side effect, so the hook saves in an effect that runs whenever `lines` changes:

```jsx
useEffect(() => {
  localStorage.setItem(storageKey, JSON.stringify(lines));
}, [storageKey, lines]);
```

Every component that calls `useCart()` now saves its cart, and nobody has to remember to.

### Syntax: testing a hook with renderHook

```jsx
import { renderHook, act } from "@testing-library/react";

const { result } = renderHook(() => useCart());
act(() => result.current.add({ code: "KEY", name: "Keychain", paise: 12000 }));
console.log(result.current.count); // 1
```

`renderHook` renders a tiny test component that calls your hook. `result.current` is what the hook returned on the latest render. Wrap anything that changes state in `act`, so React finishes updating before you check.

## Worked example

Here is the whole hook.

```jsx
import { useEffect, useState } from "react";

function loadLines(key) {
  try {
    const saved = localStorage.getItem(key);
    return saved ? JSON.parse(saved) : [];
  } catch {
    return [];
  }
}

export function useCart(storageKey = "dhaaga-cart") {
  const [lines, setLines] = useState(() => loadLines(storageKey));

  useEffect(() => {
    localStorage.setItem(storageKey, JSON.stringify(lines));
  }, [storageKey, lines]);

  function add(item) {
    setLines((current) => {
      const found = current.some((line) => line.code === item.code);
      if (found) {
        return current.map((line) => (line.code === item.code ? { ...line, qty: line.qty + 1 } : line));
      }
      return [...current, { ...item, qty: 1 }];
    });
  }

  function remove(code) {
    setLines((current) => current.filter((line) => line.code !== code));
  }

  const count = lines.reduce((sum, line) => sum + line.qty, 0);
  const total = lines.reduce((sum, line) => sum + line.paise * line.qty, 0);
  return { lines, add, remove, count, total };
}
```

- `loadLines` is a plain function. It doesn't call hooks, so it doesn't need the `use` name. The `try` covers saved text that isn't valid JSON.
- The lazy initial state reads `localStorage` only on the first render.
- `add` and `remove` use the updater form, so two calls in one click both count.
- `count` and `total` are worked out while rendering, not kept in state.
- Components call it like any hook: `const { lines, add, total } = useCart();`.

## Try it

Open the preview. The shop calls `useCart()` once and passes the cart down to its grid and summary. Press "Buy 2" on the keychain, then refresh the preview. The cart should still show 2 keychains.

Then change `add` to `setLines([...lines, { ...item, qty: 1 }])` and press "Buy 2" again. Only one keychain arrives. Put the updater form back.

## Recap

- A custom hook is a function whose name starts with `use` and which calls other hooks. It lets components share stateful logic.
- Call hooks only at the top level of components and custom hooks, never inside conditions or loops.
- Each component that calls a hook gets its own state. Hooks share logic, not data.
- Use the updater form, `setLines((current) => ...)`, so several updates in one event all count.
- `useState(() => load())` runs the loader only once, on the first render.

## Common mistakes

**1. Calling a hook inside a condition.**

```jsx
function CartDrawer({ open }) {
  if (!open) return null;
  const { lines } = useCart();
  return <ul>{/* ... */}</ul>;
}
```

When `open` changes, React stops with an error like `Rendered more hooks than during the previous render.` The hook order changed between renders.

Fix: call every hook first, then decide what to return.

```jsx
function CartDrawer({ open }) {
  const { lines } = useCart();
  if (!open) return null;
  return <ul>{/* ... */}</ul>;
}
```

**2. Building the new array from `lines` instead of the newest state.**

```jsx
function add(item) {
  setLines([...lines, { ...item, qty: 1 }]);
}
```

A "Buy 2" button that calls `add` twice adds only one item. Both calls copied the same old `lines`.

Fix: `setLines((current) => [...current, { ...item, qty: 1 }]);`

**3. Changing a line in place.**

```jsx
const line = current.find((l) => l.code === item.code);
line.qty += 1;
return current;
```

The count on the page doesn't change. You returned the same array, so React thinks nothing changed and skips the render.

Fix: return a new array from `map`, with a new object for the changed line: `{ ...line, qty: line.qty + 1 }`.

**4. Expecting two components to share a hook's state.**

```jsx
function Header() {
  const { count } = useCart();
  return <p>Cart ({count})</p>;
}
```

The header stays at `Cart (0)` while the product grid adds items. The header's `useCart()` has its own, separate state.

Fix: share one cart through context, which the next ticket covers.

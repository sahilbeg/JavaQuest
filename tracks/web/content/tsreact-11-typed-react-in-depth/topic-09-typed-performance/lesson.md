## Hook

Imagine Raju had to rewrite the whole order book by hand every time one customer picked up a parcel. He'd only need to tick one line. React, by default, is a bit like that: when state changes, it calls the component and all its children again.

Usually that's fast enough. But with hundreds of rows on an old tablet, it isn't. This ticket shows how to skip work that doesn't need doing, and how TypeScript keeps the types right while you do.

## Concept

### Rendering again is normal

```callout jargon
To **re-render** is for React to call a component again to get fresh JSX. When a component's state changes, React re-renders it and, by default, every component inside it.
```

Re-rendering is not a bug. React compares the new JSX with the old, and only touches the page where something really changed. The cost is the work *inside* your components: building JSX for 500 rows, or searching a long list, again and again.

### Measure before you speed up

Don't guess. The **React DevTools** browser extension has a **Profiler** tab. Record a tap, and it shows which components rendered and how long each took.

```callout tip
Only optimise what the Profiler shows is slow. Every `memo`, `useMemo` and `useCallback` adds code to read and keep right. On a fast screen, it's just noise.
```

Arjun's profile showed two costs on every tap of Open: every `OrderRow` rendered again, and `filterOrders` searched the whole list again. Neither had any reason to.

### Syntax: memo

```tsx
const OrderRow = memo(function OrderRow({ order, onOpen }: OrderRowProps) {
  return <li>...</li>;
});
```

`memo` wraps a component. Before re-rendering it, React compares each new prop with the old one using `Object.is`. If every prop is the same, React skips that component and reuses its last JSX.

```callout jargon
To **memoize** means to remember a result and reuse it while the inputs are the same. `memo` memoizes a component's JSX; `useMemo` memoizes a value; `useCallback` memoizes a function.
```

With TypeScript, `memo` keeps the props type of the function you give it. `<OrderRow order={...} onOpen={...} />` is checked exactly as before. Name the inner function too, so DevTools shows `OrderRow`, not `Anonymous`.

### Why memo needs stable props

`memo` compares with `Object.is`. Numbers and strings that look the same are the same. But a function made inside a component is a **new** function on every render:

```animate
title: A new function every render
kind: code
language: ts
code: |
  function render(): (id: string) => void {
    const handleOpen = (id: string) => console.log("open " + id);
    return handleOpen;
  }
  const first = render();
  const second = render();
  console.log(Object.is(first, second));
  const saved = first;
  console.log(Object.is(first, saved));
steps:
  - line: 5
    vars: { first: "handleOpen #1" }
    say: The first render makes a handleOpen function.
  - line: 6
    vars: { second: "handleOpen #2" }
    say: The second render runs the same code, and makes a brand-new function.
  - line: 7
    out: "false"
    say: They do the same job, but they're different objects. To memo, the onOpen prop has changed.
  - line: 8
    vars: { saved: "handleOpen #1" }
    say: Keeping the very same function is what useCallback does for you.
  - line: 9
    out: "true"
    say: The same function passes Object.is, so memo can skip the row.
```

So `memo` on the row does nothing until `onOpen` stays the same between renders.

### Syntax: useCallback

```tsx
const handleOpen = useCallback((id: string) => setOpened(id), []);
```

`useCallback` returns the same function on every render, until something in its dependency array changes. Type the parameters as usual. TypeScript infers the whole type, `(id: string) => void`, from the function you pass.

The dependency array works like an effect's. This handler reads nothing from the render except `setOpened`, and React promises setters never change, so the array can be empty.

### Syntax: useMemo

```tsx
const shown = useMemo(() => filterOrders(orders, query), [orders, query]);
```

`useMemo` calls the function, remembers what it returned, and gives back the same value until a dependency changes. Tapping Open changes `opened`, not `orders` or `query`, so the search is skipped.

TypeScript infers the type from the function's return: `shown` is `PastOrder[]`. You can write it out, `useMemo<PastOrder[]>(...)`, but you rarely need to.

```animate
title: One tap, before and after
kind: scene
nodes:
  - { id: history, label: OrderHistory, emoji: "📚", value: "opened: none" }
  - { id: filter, label: filterOrders, emoji: "🔍", value: "runs: 1" }
  - { id: rows, label: 500 OrderRows, emoji: "🧾", value: "renders: 500" }
steps:
  - say: Raju taps Open on order-17. opened changes, so OrderHistory re-renders.
    set: { history: "opened: order-17" }
  - say: "Without useMemo, the search runs again, though orders and query didn't change."
    send: { from: history, to: filter, label: "same inputs" }
    set: { filter: "runs: 2" }
  - say: Without memo and useCallback, all 500 rows render again, because onOpen is a new function.
    send: { from: history, to: rows, label: "new onOpen" }
    set: { rows: "renders: 1000" }
  - say: With the fix, useMemo sees the same orders and query, and hands back the remembered list.
    set: { filter: "runs: 1" }
  - say: useCallback keeps onOpen the same, so memo skips every row. Only the line that says Opened order-17 changes.
    set: { rows: "renders: 500" }
```

### Types that help

- Let inference work: `useMemo` and `useCallback` take their types from the function you pass. Annotate the parameters, not the hook.
- To reuse a component's props type, `ComponentProps<typeof OrderRow>` gives it to you, even for a `memo` component.
- Keep memoized values out of objects made fresh each render. `memo` comparing `{ order }` (a new object) is useless; pass `order` itself.

### React Compiler

Newer React projects can turn on the **React Compiler**, a build tool that adds this memoizing for you. Many teams still write `memo`, `useMemo` and `useCallback` by hand, so you need to read and write them. The rule doesn't change: measure first.

## Worked example

Here's `useMemo` for a different screen: Sana's Diwali price board, which works out sale prices for the whole catalogue.

```tsx
import { useMemo, useState } from "react";
import type { ChangeEvent } from "react";

interface Item { code: string; name: string; paise: number }

function salePrices(items: Item[], percentOff: number): { code: string; paise: number }[] {
  return items.map((item) => ({ code: item.code, paise: Math.floor((item.paise * (100 - percentOff)) / 100) }));
}

export function PriceBoard({ items }: { items: Item[] }) {
  const [percentOff, setPercentOff] = useState<number>(10);
  const [highlight, setHighlight] = useState<string>("AMI");

  const prices = useMemo(() => salePrices(items, percentOff), [items, percentOff]);

  return (
    <section>
      <input
        type="number"
        value={percentOff}
        onChange={(e: ChangeEvent<HTMLInputElement>) => setPercentOff(Number(e.currentTarget.value))}
      />
      <ul>
        {prices.map((p) => (
          <li key={p.code} onClick={() => setHighlight(p.code)}>
            {p.code === highlight ? <strong>{p.code}</strong> : p.code}: {p.paise} paise
          </li>
        ))}
      </ul>
    </section>
  );
}
```

- `prices` is inferred as `{ code: string; paise: number }[]` from `salePrices`.
- Tapping a row changes `highlight`, which isn't a dependency, so the prices aren't worked out again.
- Changing the discount does change a dependency, so the prices are worked out once more.
- `Math.floor` keeps the paise whole, as the Diwali offer rule says.

## Try it

Open the preview. The page counts row renders and searches. Tap **Open** on a few orders, then press **Show the counters**: both have climbed.

After your fix, tapping Open should leave both counters still. Typing in the search box should still search once per letter. Then remove `useCallback` but keep `memo`, and see the row count climb again.

## Recap

- Re-rendering is normal. Speed up only what the React DevTools Profiler shows is slow.
- `memo(function Row(props: RowProps) {...})` skips a re-render when every prop passes `Object.is`, and keeps the props type.
- Functions made during render are new each time. `useCallback` keeps one, so `memo` can work.
- `useMemo(() => work(a, b), [a, b])` remembers a value until `a` or `b` changes. Its type comes from the function.
- Let inference type the hooks; annotate parameters, not the hook itself.

## Common mistakes

**1. memo with a new function every render.**

```tsx
<OrderRow order={order} onOpen={(id) => setOpened(id)} />
```

Every row still renders on every tap. The arrow is new each time, so `memo` sees a changed prop.

Fix: `const handleOpen = useCallback((id: string) => setOpened(id), []);` and pass `handleOpen`.

**2. Forgetting a dependency.**

```tsx
const shown = useMemo(() => filterOrders(orders, query), [orders]);
```

Typing in the search box changes nothing on screen. `query` isn't in the array, so the old result is reused.

Fix: list everything the function reads, `[orders, query]`.

**3. Losing the export name.**

```tsx
export function OrderRow(props: OrderRowProps) { /* ... */ }
export default memo(OrderRow);
```

Code that imports `{ OrderRow }` still gets the plain, un-memoized component, so nothing is skipped.

Fix: `export const OrderRow = memo(function OrderRow(...) { ... });`.

**4. useMemo for everything.**

```tsx
const label = useMemo(() => `Opened ${opened}`, [opened]);
```

Joining two strings is cheaper than `useMemo`'s own bookkeeping. The code gets longer and no faster.

Fix: keep `useMemo` for work the Profiler shows is slow, like searching hundreds of orders.

## Hook

At a wedding lunch, nobody waits for every dish to be ready before the plates go out. The banana leaf arrives first, then salt and pickle, then rice, then each sabzi as it comes off the stove. You start eating straight away, and the slow dal turns up when it's ready.

A page can be served the same way. The heading and the layout go out at once. Each slow part follows when its data arrives. That's called streaming.

## Concept

### One slow request holds up the whole page

In topic 1, a server component awaited its data before returning JSX. That's right when the data is fast. But if one request takes eight seconds, the whole page waits eight seconds. With no `loading.jsx`, the browser shows nothing at all.

```jsx
export default async function DashboardPage() {
  const sales = await getTodaySales(); // 8 seconds
  const lowStock = await getLowStock(); // 2 seconds
  return <Dashboard sales={sales} lowStock={lowStock} />;
}
```

The heading doesn't need the sales. Neither does the low-stock card. They're stuck behind it anyway.

### Streaming: the page in pieces

```callout jargon
**Streaming** means sending a page in parts over one answer. The server sends what's ready first, keeps the connection open, and sends each slow part when it's done. The browser shows each part as it arrives.
```

```animate
title: The dashboard streams in
kind: scene
nodes:
  - { id: browser, label: Meera's tablet, emoji: "📱", value: blank }
  - { id: next, label: Next.js server, emoji: "🖥️" }
  - { id: sales, label: Sales report, emoji: "📈", value: working }
  - { id: stock, label: Stock report, emoji: "📦", value: working }
steps:
  - say: Meera opens the dashboard. The server starts both reports at once, without waiting for them.
    send: [{ from: next, to: sales, label: start }, { from: next, to: stock, label: start }]
  - say: Straight away, it sends the heading, with a loading message where each card will go.
    send: { from: next, to: browser, label: "heading + 2 fallbacks", back: true }
    set: { browser: "heading, Loading..., Loading..." }
  - say: The stock report finishes first. The server sends just that card, down the same open answer.
    send: [{ from: stock, to: next, label: "3 items low", back: true }, { from: next, to: browser, label: "stock card", back: true }]
    set: { stock: done, browser: "heading, Loading..., stock card" }
  - say: The sales report finishes later. Its card replaces the last loading message.
    send: [{ from: sales, to: next, label: "12 orders", back: true }, { from: next, to: browser, label: "sales card", back: true }]
    set: { sales: done, browser: "heading, sales card, stock card" }
  - say: Meera read the heading and the low stock while the sales were still coming.
    focus: [browser]
```

### Syntax: Suspense

```jsx
<Suspense fallback={<p>Loading today's sales...</p>}>
  <SalesCard salesPromise={salesPromise} />
</Suspense>
```

`Suspense` comes from `react`. It marks a part of the page that may have to wait.

```callout jargon
A **Suspense boundary** is a `<Suspense>` element. While anything inside it is waiting for data, React shows its `fallback` instead. When the data is ready, the real content takes the fallback's place.
```

On the server, Next.js sends the fallback in the first part of the stream, and the content later.

### loading.jsx is a boundary around the whole page

In next-10 you made `loading.jsx` files. Next.js wraps the page in a `Suspense` boundary and uses `loading.jsx` as its fallback. That's one boundary for everything.

Your own `<Suspense>` elements go further: one boundary per slow part, so the fast parts don't wait for the slow ones.

### Start every request early

The page shouldn't wait for slow data, but it should *start* it straight away. Calling an async function starts its work at once and gives you a promise. Don't `await` it in the page: pass the promise down.

```js run
function slow(name, ms) {
  console.log(`start ${name}`);
  return new Promise((resolve) => setTimeout(() => resolve(name), ms));
}

const sales = slow("sales", 20);
const stock = slow("stock", 10);
console.log("page shell is ready");

stock.then((value) => console.log(`${value} arrived`));
sales.then((value) => console.log(`${value} arrived`));
```

Both reports were already running when the shell was ready. Each one arrives when it's done.

### Syntax: reading a promise with use

```jsx
import { use } from "react";

export function SalesCard({ salesPromise }) {
  const sales = use(salesPromise);
  return <p>{sales.orders} orders</p>;
}
```

- `use(promise)` gives the promise's value when it's ready.
- If it isn't ready yet, React pauses this component and shows the nearest Suspense fallback.
- When the promise settles, React renders the component again, and this time `use` returns the value.
- If the promise rejects, the nearest error boundary (`error.jsx`) shows instead.

```callout tip
`use` can be called inside `if` and loops, unlike other hooks. But the promise should be made outside the component, like in the page, and passed in. A promise made inside the component would be a new one on every render.
```

### The page itself must not wait

```jsx
export default function DashboardPage() {
  const salesPromise = getTodaySales();
  return (
    <Suspense fallback={<p>Loading today's sales...</p>}>
      <SalesCard salesPromise={salesPromise} />
    </Suspense>
  );
}
```

The page starts the request, wraps the card, and returns at once. If the page had `await getTodaySales()`, the boundary would never get a chance: the page itself would be the slow part.

An `async` server component that awaits its own data works inside a boundary too: `<Suspense><SalesCard /></Suspense>`, where `SalesCard` does `await getTodaySales()`. Both ways stream. Passing a promise lets the page start every request first.

### One boundary per slow part

Put each slow card in its own boundary. Then whichever finishes first shows first. If both cards share one boundary, both wait for the slower one.

Make the fallback the same size and shape as the real card, like a grey box with the card's heading. Then nothing jumps around when the content arrives.

## Worked example

Meera Aunty's dashboard. `getTodaySales()` answers `{ orders: 12, totalPaise: 845000 }`, and `getLowStock()` answers a list like `[{ id: "DC-1042", name: "Golu the elephant", left: 2 }]`.

```jsx
import { Suspense, use } from "react";
import { getTodaySales, getLowStock } from "../../lib/reports.js";
import { rupees } from "../../lib/money.js";

function SalesCard({ salesPromise }) {
  const sales = use(salesPromise);
  return <p>{`${sales.orders} orders, ${rupees(sales.totalPaise)}`}</p>;
}

function LowStockCard({ stockPromise }) {
  const items = use(stockPromise);
  return (
    <ul>
      {items.map((item) => (
        <li key={item.id}>{`${item.name}: ${item.left} left`}</li>
      ))}
    </ul>
  );
}

export default function DashboardPage() {
  const salesPromise = getTodaySales();
  const stockPromise = getLowStock();

  return (
    <main>
      <h1>Today at Dhaaga</h1>
      <Suspense fallback={<p>Loading today's sales...</p>}>
        <SalesCard salesPromise={salesPromise} />
      </Suspense>
      <Suspense fallback={<p>Loading low stock...</p>}>
        <LowStockCard stockPromise={stockPromise} />
      </Suspense>
    </main>
  );
}
```

- Both requests start on the first lines of the page, before any JSX.
- The heading needs no data, so it goes out in the first part of the stream.
- Each card has its own boundary and fallback, so each fills in on its own.

## Try it

When the checks pass, put both cards inside one `<Suspense>` and run the checks again. The check that shows sales "while stock is still loading" fails: the sales card now waits for the stock. Split them up again.

## Recap

- Awaiting slow data in the page holds back the whole page, even parts that don't need it.
- Streaming sends the page in parts: what's ready first, each slow part when it's done.
- `<Suspense fallback={...}>` marks a part that may wait. `loading.jsx` is one boundary around the whole page.
- Start requests in the page without `await`, pass the promises down, and read them with `use(promise)`.
- Give each slow part its own boundary, with a fallback the same shape as the content.

## Common mistakes

**1. Awaiting in the page.**

```jsx
export default async function DashboardPage() {
  const sales = await getTodaySales();
  return (
    <Suspense fallback={<p>Loading...</p>}>
      <SalesCard sales={sales} />
    </Suspense>
  );
}
```

The fallback never shows, and the page is white until the sales arrive. The waiting happens before the boundary exists.

Fix: `const salesPromise = getTodaySales();` without `await`, and `use(salesPromise)` in the card.

**2. One boundary around everything.**

```jsx
<Suspense fallback={<p>Loading...</p>}>
  <SalesCard salesPromise={salesPromise} />
  <LowStockCard stockPromise={stockPromise} />
</Suspense>
```

The low-stock card is ready in two seconds but waits for the eight-second sales report, because a boundary shows its content only when everything inside is ready.

Fix: one `<Suspense>` per card.

**3. Making the promise inside the card.**

```jsx
function SalesCard() {
  const sales = use(getTodaySales());
  return <p>{sales.orders} orders</p>;
}
```

Every render makes a new promise, so React waits again, renders again, makes another one... React warns that a component was suspended by an uncached promise.

Fix: create the promise in the page (a server component) and pass it in as a prop.

**4. Forgetting the boundary.**

```jsx
<main>
  <h1>Today at Dhaaga</h1>
  <SalesCard salesPromise={salesPromise} />
</main>
```

The nearest boundary is now `loading.jsx` around the whole page, so the heading waits for the sales again.

Fix: wrap the card in its own `<Suspense fallback={...}>`.

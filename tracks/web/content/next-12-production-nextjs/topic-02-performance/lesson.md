## Hook

Imagine a chai stall where the man makes the tea, then fetches the cup, then finds the sugar, one after another. Every customer waits for all three. A good chaiwala puts the water on, and while it boils he sets out the cups and the sugar. Same work, a third of the wait.

Slow pages usually aren't slow because computers are slow. They're slow because they wait for things one at a time, or carry far more than they need. Next.js gives you tools for both problems. This ticket is about using them.

## Concept

### What a slow page costs

```callout jargon
**Core Web Vitals** are three numbers Google uses to judge how a page feels. **LCP** (Largest Contentful Paint): how soon the main content shows. **INP** (Interaction to Next Paint): how quickly the page reacts to a tap. **CLS** (Cumulative Layout Shift): how much things jump around while loading.
```

Good targets are LCP under 2.5 seconds, INP under 200 milliseconds and CLS under 0.1. You measure them with Lighthouse in DevTools, and better still, from real customers' phones. A customer on a bus with patchy mobile data feels every extra request and every extra kilobyte.

Next.js helps most with three things: sending less JavaScript, rendering ahead of time, and not waiting in a line.

### Server components send no JavaScript

You learned in [Server and client components](/t/next-10-nextjs-fundamentals/topic-05-server-and-client-components) that components are server components unless a file says `"use client"`.

A server component runs on the server and sends **HTML**. Its code never reaches the phone. A page of product details, prices and reviews can be all server components, and the phone downloads almost no JavaScript for it.

A client component's code **is** sent to the phone, along with React, so the browser can run it. Then React **hydrates** it.

```callout jargon
To **hydrate** is to attach a component's event handlers and state to HTML that is already on the screen. Until hydration finishes, a button can be seen but does nothing when tapped.
```

### Client islands

```callout jargon
A **client island** is a small client component inside a page that is otherwise server components. Only the island's code goes to the phone.
```

Put `"use client"` as low in the tree as you can. On Golu's page, only the **Add to cart** button needs a click handler and state. So the button is the island, and the page, the description and the reviews stay on the server.

If you put `"use client"` at the top of the page file instead, the whole page and everything it imports becomes browser code. The phone then downloads and runs it all, just to show text.

### Props that cross to the client

A server component can pass props to a client component. Those props travel to the browser inside the page, packed as data, so React can hydrate the island.

```callout jargon
To **serialise** data is to turn it into text that can travel, like JSON. Everything a server component passes to a client component is serialised and sent with the page.
```

```jsx
<AddToCartButton item={item} reviews={reviews} stock={stock} />   // sends every review to the phone
<AddToCartButton id={item.id} stock={stock} />                     // sends two small values
```

So pass a client component only what it uses. The button needs an id and a stock count, not the whole item and every review. Serialised props must also be plain data: strings, numbers, arrays and plain objects. A function can't be sent, except a server action.

### Static and dynamic rendering

```callout jargon
**Static rendering** builds a page's HTML once, during `next build`, and serves the same copy to everyone. **Dynamic rendering** builds the HTML again on every request, because it depends on that request, like its cookies.
```

A static page is the fastest kind: a CDN can hand out the ready HTML from near the customer. A page becomes dynamic when it reads request data, such as `cookies()`, `headers()` or `searchParams`. The cart is personal, so it must be dynamic. Golu's page is the same for everyone, so it can be static.

When `next build` finishes, it prints each route with a mark that says whether it was built ahead of time or is rendered on demand. Check that list after a change: a page that suddenly turns dynamic is a performance bug.

### Syntax: generateStaticParams

```jsx
export async function generateStaticParams() {
  const items = await listItems();
  return items.map((item) => ({ id: item.id }));
}
```

A dynamic route like `app/items/[id]/page.jsx` has one page per id. Next.js can't guess the ids. `generateStaticParams` tells it: build these pages during `next build`. Each object fills the `[id]` part of the folder name.

Pages for ids not in the list can still be rendered when someone first asks for one. That's useful for an item added after the last build.

### Waterfalls and Promise.all

```callout jargon
A **waterfall** is a chain of requests where each one starts only after the one before it finishes. The waits add up, like water falling from step to step.
```

```jsx
const item = await getItem(id);          // 600 ms
const reviews = await getReviews(id);    // then 600 ms more
const stock = await getStock(id);        // then 600 ms more
```

None of these needs the others' answers, yet each `await` waits before the next call even starts. Start them all, then wait once:

```animate
title: Three calls, two ways
kind: scene
nodes:
  - { id: page, label: Golu's page, emoji: "📄", value: "0 ms", row: 1, col: 0 }
  - { id: item, label: getItem, emoji: "🐘", row: 0, col: 3 }
  - { id: reviews, label: getReviews, emoji: "⭐", row: 1, col: 3 }
  - { id: stock, label: getStock, emoji: "📦", row: 2, col: 3 }
steps:
  - say: One by one, the page asks for the item and waits 600 ms for the answer.
    send: [{ from: page, to: item, label: "ask" }, { from: item, to: page, label: "Golu", back: true }]
    set: { page: "600 ms" }
  - say: Only then does it ask for the reviews. Another 600 ms.
    send: [{ from: page, to: reviews, label: "ask" }, { from: reviews, to: page, label: "2 reviews", back: true }]
    set: { page: "1200 ms" }
  - say: Then the stock. The customer has waited 1.8 seconds, mostly for nothing.
    send: [{ from: page, to: stock, label: "ask" }, { from: stock, to: page, label: "7", back: true }]
    set: { page: "1800 ms" }
    focus: [page]
  - say: With Promise.all, the page asks all three at once. They work at the same time.
    send: [{ from: page, to: item, label: "ask" }, { from: page, to: reviews, label: "ask" }, { from: page, to: stock, label: "ask" }]
    set: { page: "0 ms again" }
  - say: The answers arrive together. The wait is only as long as the slowest call, about 600 ms.
    send: [{ from: item, to: page, label: "Golu", back: true }, { from: reviews, to: page, label: "2", back: true }, { from: stock, to: page, label: "7", back: true }]
    set: { page: "600 ms" }
    focus: [page]
```

### Syntax: Promise.all

```jsx
const [item, reviews, stock] = await Promise.all([getItem(id), getReviews(id), getStock(id)]);
```

`Promise.all` takes a list of promises and waits for all of them. Its answers come back in the same order as the list, so you can destructure them. You met it in [Many requests at once](/t/web-06-async-javascript/topic-08-parallel-requests). If any one fails, the whole `Promise.all` fails.

Only use it for calls that don't depend on each other. If you need the item's maker before you can fetch the maker's other items, those two must stay in order.

### Streaming slow parts

Sometimes one part is truly slow, like reviews from another service. You don't have to make the whole page wait for it.

```jsx
<Suspense fallback={<p>Loading reviews…</p>}>
  <Reviews id={id} />
</Suspense>
```

As in [Streaming with Suspense](/t/next-11-data-and-server/topic-09-streaming-and-suspense), Next.js sends the rest of the page at once with the fallback in place. When `Reviews` is ready, its HTML streams in and replaces the fallback. The customer reads Golu's price while the reviews are still on their way. A `loading.js` file does the same for a whole route.

### Images, fonts and heavy code

Next.js has three more built-in tools you'll use on almost every real page:

- **`next/image`**: `<Image src="/golu.jpg" alt="Golu, an Indigo elephant" width={600} height={600} sizes="(max-width: 600px) 100vw, 600px" />` resizes and compresses photos for each screen, lazy-loads them, and reserves their space, which stops layout shift. Give the main photo `preload`, so the browser fetches it early for a fast LCP.
- **`next/font`**: loads fonts from your own site at build time, with no request to a font service, and keeps text from jumping when the font arrives.
- **`next/dynamic`**: `const SizeChart = dynamic(() => import("./SizeChart.jsx"))` loads a heavy client component's code only when it's actually shown.

## Worked example

The catalogue home page lists every item and shows the yarn colour of the day. The first version looked like this:

```jsx
"use client";

import { useEffect, useState } from "react";

export default function HomePage() {
  const [items, setItems] = useState([]);
  const [colour, setColour] = useState("");
  useEffect(() => {
    fetch("/api/items").then((r) => r.json()).then((data) => {
      setItems(data);
      fetch("/api/colour-of-the-day").then((r) => r.json()).then((c) => setColour(c.name));
    });
  }, []);
  return <ItemList items={items} colour={colour} />;
}
```

It has three problems. The whole page is client code. The phone downloads React and this code, runs it, and only then starts fetching. And the colour waits for the items, a waterfall.

```jsx
import { listItems, colourOfTheDay } from "../lib/shop.js";

export default async function HomePage() {
  const [items, colour] = await Promise.all([listItems(), colourOfTheDay()]);
  return <ItemList items={items} colour={colour.name} />;
}
```

- No `"use client"`: it's a server component, so its code stays on the server and the phone gets finished HTML.
- It reads the data layer directly, with no extra round trip through `/api/items`.
- `Promise.all` starts both calls together.
- It reads no cookies or headers, so it can be rendered statically and served from a CDN.

## Try it

This logs when each call starts and finishes, first one by one and then with `Promise.all`. Run it.

```js run
const log = [];

function slow(name) {
  log.push("start " + name);
  return Promise.resolve(name).then((value) => {
    log.push("done " + value);
    return value;
  });
}

async function oneByOne() {
  await slow("item");
  await slow("reviews");
  await slow("stock");
}

async function together() {
  await Promise.all([slow("item"), slow("reviews"), slow("stock")]);
}

async function main() {
  await oneByOne();
  console.log("one by one: " + log.join(", "));
  log.length = 0;
  await together();
  console.log("together: " + log.join(", "));
}

main();
```

It prints:

```text
one by one: start item, done item, start reviews, done reviews, start stock, done stock
together: start item, start reviews, start stock, done item, done reviews, done stock
```

One by one, each call starts only after the last is done. Together, all three start before any finishes.

1. If each call took 600 ms, how long would each version take?
2. Suppose `getStock` needed the item's `warehouse` from `getItem`. Rewrite `together` so the item comes first, and the reviews still run alongside it.

## Recap

- Server components send HTML and no JavaScript. Keep `"use client"` low in the tree, on small client islands such as one button.
- Every prop passed to a client component is serialised and sent to the phone, so pass only what it uses.
- Static pages are built once and served from a CDN. `generateStaticParams` builds dynamic routes like `/items/[id]` ahead of time.
- Avoid waterfalls: start calls that don't depend on each other together with `Promise.all`, and stream truly slow parts with `Suspense`.
- Use `next/image` for photos, `next/font` for fonts and `next/dynamic` for heavy client code, and watch LCP, INP and CLS.

## Common mistakes

**1. "use client" at the top of a whole page.**

```jsx
"use client";

export default function ItemPage({ item, reviews }) {
```

Symptom: the page works, but the phone downloads and runs the whole page's code, and the page can't be async or read data on the server. Fix: remove the line, and move only the interactive part into its own client component.

**2. Awaiting independent calls one by one.**

```jsx
const item = await getItem(id);
const reviews = await getReviews(id);
```

Symptom: the page takes as long as all the calls added together. The check says all three calls should have started before any answered. Fix: `const [item, reviews, stock] = await Promise.all([...])`.

**3. Passing whole objects to a client component.**

```jsx
<AddToCartButton item={item} reviews={reviews} stock={stock} />
```

Symptom: every review is packed into the page a second time, for a button that never reads them. Fix: pass only what the island uses, such as `id={item.id} stock={stock}`.

**4. Reading cookies on a page that doesn't need them.**

```jsx
const theme = (await cookies()).get("theme");
```

Symptom: the build now marks Golu's page as dynamic. It's rendered again for every visitor, and can't be served ready-made from the CDN. Fix: keep personal data to the pages that need it, or read it in a small client component.

**5. An image with no size.**

```jsx
<img src="/golu.jpg" alt="Golu, an Indigo elephant" />
```

Symptom: the text jumps down when the photo arrives, and Lighthouse reports a high CLS. Fix: use `next/image` with `width` and `height` (or `fill` inside a sized box), so the space is reserved before the photo loads.

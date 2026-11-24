## Hook

When you ring the Aundh shop to ask about your parcel, three things can happen. Raju says "hold on, let me check". Then he either tells you "it's packed" or says "sorry, I can't find that order".

A page that fetches data has the same three moments: waiting, an answer, or a problem. In web-06 you fetched data with plain JavaScript. Now you'll do it inside a React component, so the page shows each moment by itself.

## Concept

### Fetching is a side effect

Asking the server for data reaches outside React, so it goes in an effect. The effect runs after the first render. When the answer arrives, you put it in state, and React renders again with the data.

```animate
title: A component fetches an order
kind: scene
nodes:
  - { id: comp, label: OrderStatus, emoji: "🧶", value: loading }
  - { id: page, label: The page, emoji: "📄", value: "Loading order-17..." }
  - { id: api, label: Dhaaga API, emoji: "🖥️" }
steps:
  - say: The first render has no order yet, so the component shows a loading message.
    focus: [comp, page]
  - say: After that render, the effect runs and sends the request.
    send: { from: comp, to: api, label: "GET /api/orders/order-17" }
  - say: The server takes a moment. The page keeps showing the loading message meanwhile.
    focus: [api]
  - say: The answer comes back as JSON.
    send: { from: api, to: comp, label: "{ status: PACKED }", back: true }
  - say: The effect calls setOrder with the data. New state means a new render.
    set: { comp: "order ready" }
  - say: This render has the order, so the page shows its status.
    set: { page: "Status: PACKED" }
    focus: [page]
```

### Syntax: fetching in an effect

```jsx
useEffect(() => {
  async function load() {
    const res = await fetch(`/api/orders/${orderId}`);
    const data = await res.json();
    setOrder(data);
  }
  load();
}, [orderId]);
```

- `load` is an `async` function made inside the effect, so it can use `await`.
- The effect calls `load()` and finishes straight away. The answer arrives later.
- `[orderId]` is the dependency, because the URL uses it.

### Why the effect itself can't be async

You might try `useEffect(async () => { ... })`. Don't. An `async` function always returns a promise:

```js run
async function load() {
  return "done";
}

const result = load();
console.log(result instanceof Promise);
```

React expects an effect to return a cleanup function or nothing. A promise is neither, so React warns and your cleanup never runs. Keep the `async` function inside the effect.

### Three states: loading, error and data

A fetching component is always in one of three states. Give each one its own look.

```callout jargon
A **loading state** is what the page shows while it waits for an answer. An **error state** is what it shows when the request failed. Showing nothing at all in either case looks like a broken page.
```

```jsx
const [order, setOrder] = useState(null);
const [error, setError] = useState("");

if (error) return <p role="alert">{error}</p>;
if (order === null) return <p>Loading {orderId}...</p>;
return <p>Status: {order.status}</p>;
```

`role="alert"` tells screen readers to read the message out as soon as it appears.

### Syntax: checking for errors

```jsx
try {
  const res = await fetch(`/api/orders/${orderId}`);
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  setOrder(await res.json());
} catch {
  setError(`We couldn't load ${orderId}.`);
}
```

There are two kinds of failure, and you met both in web-06:

- The server answers, but with an error code like 404. `fetch` does **not** throw for this. You check `res.ok` yourself.
- The request never gets an answer, for example with no internet. Then `fetch` throws, and `catch` handles it.

Throwing inside `try` sends both kinds to the same `catch`.

### When the id changes

The customer opens order-17, then taps order-18. The component stays on the page, but its `orderId` prop changes. Because `orderId` is in the dependency array, the effect runs again and fetches order-18.

At the start of the effect, clear the old data. Otherwise the page shows order-17's status under order-18's heading until the new answer arrives.

```jsx
useEffect(() => {
  setOrder(null);
  setError("");
  // ...then fetch
}, [orderId]);
```

### A slow answer can arrive last

Requests don't always come back in the order you sent them. On a slow phone, order-17's answer might arrive after order-18's.

```callout jargon
A **race condition** is a bug where the result depends on which of two things finishes first. Here, the old request "wins" if it is slower.
```

```animate
title: The old answer arrives last
kind: scene
nodes:
  - { id: page, label: The page, emoji: "📄", value: "order-17" }
  - { id: api, label: Dhaaga API, emoji: "🖥️" }
  - { id: flag, label: "order-17's ignore flag", emoji: "🚩", value: "false" }
steps:
  - say: The page asks for order-17. This request will be slow.
    send: { from: page, to: api, label: "GET order-17" }
  - say: The customer taps order-18. React runs the cleanup of order-17's effect, which sets its ignore flag.
    set: { page: "order-18", flag: "true" }
  - say: The new effect asks for order-18.
    send: { from: page, to: api, label: "GET order-18" }
  - say: order-18's answer comes back first, and the page shows it.
    send: { from: api, to: page, label: "SHIPPED", back: true }
  - say: Now order-17's slow answer arrives. Without the flag it would overwrite the page with the wrong order.
    send: { from: api, to: page, label: "PACKED", back: true }
  - say: Its flag says ignore, so the effect drops the answer. The page still shows order-18.
    focus: [flag, page]
```

### Syntax: ignoring a stale answer

```jsx
useEffect(() => {
  let ignore = false;
  load().then((data) => {
    if (!ignore) setOrder(data);
  });
  return () => {
    ignore = true;
  };
}, [orderId]);
```

Each run of the effect gets its own `ignore` variable. The cleanup sets it, so the old run's answer is thrown away. The new run has a fresh `ignore` that is still `false`.

```callout jargon
A **stale** answer is one for a question the page is no longer asking.
```

Here is the same idea in plain JavaScript, with short timers standing in for the network:

```js run
function fakeFetch(id, ms) {
  return new Promise((resolve) => setTimeout(() => resolve(id), ms));
}

let shown = "";
function load(id, ms) {
  let ignore = false;
  fakeFetch(id, ms).then((answer) => {
    if (!ignore) shown = answer;
    console.log(answer, ignore ? "ignored" : "shown");
  });
  return () => {
    ignore = true;
  };
}

const cleanup17 = load("order-17", 20);
cleanup17();
load("order-18", 5);
setTimeout(() => console.log("page shows", shown), 30);
```

### What teams use

Real projects often use a data library instead of writing these effects by hand. **TanStack Query** (also called React Query) and **SWR** handle loading, errors, stale answers, caching and retries for you. Frameworks like Next.js fetch on the server.

```callout tip
Learn the effect version first. Every one of those libraries does the same steps underneath, and you'll know what they're saving you from.
```

## Worked example

Here is the order status page Raju asked for.

```jsx
import { useEffect, useState } from "react";

export function OrderStatus({ orderId }) {
  const [order, setOrder] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let ignore = false;
    setOrder(null);
    setError("");

    async function load() {
      try {
        const res = await fetch(`/api/orders/${orderId}`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        if (!ignore) setOrder(data);
      } catch {
        if (!ignore) setError(`We couldn't load ${orderId}.`);
      }
    }

    load();
    return () => {
      ignore = true;
    };
  }, [orderId]);

  if (error) return <p role="alert">{error}</p>;
  if (order === null) return <p>Loading {orderId}...</p>;
  return (
    <section>
      <h2>{order.id}</h2>
      <p>Status: {order.status}</p>
    </section>
  );
}
```

- The first render shows `Loading order-17...`, because `order` starts as `null`.
- After that render, the effect clears old data and starts `load()`.
- A 404 is not ok, so the code throws, and the `catch` sets the error text.
- When `orderId` changes, React runs the old cleanup, then the effect again for the new id.
- Every `set...` call after an `await` checks `ignore` first, so an old answer can't overwrite a new one.

## Try it

Open the preview. It has a pretend API: order-17 answers slowly, order-18 quickly, and order-99 doesn't exist. Tap order-17, then order-18 straight away. The page should end on order-18.

Now remove the `if (!ignore)` checks and try again. The page flips back to order-17 when its slow answer lands. Put the checks back.

## Recap

- Fetching is a side effect, so it goes in `useEffect`. Make an `async` function inside the effect and call it.
- Show three states: loading, error and data. Start the data as `null`.
- `fetch` doesn't throw for a 404. Check `res.ok`, and catch network failures with `try`/`catch`.
- Put the id in the dependency array so the component fetches again when it changes.
- Use an `ignore` flag, set in the cleanup, so a slow old answer can't overwrite a newer one.

## Common mistakes

**1. Making the effect function async.**

```jsx
useEffect(async () => {
  const res = await fetch(`/api/orders/${orderId}`);
  setOrder(await res.json());
}, [orderId]);
```

React warns in the console that an effect must not return anything besides a function. The returned promise is not a cleanup, so your cleanup never runs.

Fix: put an `async function load()` inside the effect and call `load();`.

**2. Trusting every answer.**

```jsx
const res = await fetch(`/api/orders/${orderId}`);
setOrder(await res.json());
```

For order-99 the server answers 404 with `{ "message": "not found" }`. That object becomes the order, and the page shows `Status:` with nothing after it.

Fix: check `if (!res.ok) throw new Error(...)` before reading the JSON.

**3. Forgetting the id in the dependency array.**

```jsx
useEffect(() => {
  load(orderId);
}, []);
```

The page shows order-17. The customer taps order-18, the heading changes, but the effect never runs again. The status is still order-17's.

Fix: list what the effect uses, `[orderId]`.

**4. Letting a slow answer win.**

```jsx
useEffect(() => {
  load().then((data) => setOrder(data));
}, [orderId]);
```

On a slow connection, the page sometimes ends on the order the customer left. Nothing shows in the console: it's just the wrong data.

Fix: add `let ignore = false`, check `if (!ignore)` before setting state, and return `() => { ignore = true; }`.

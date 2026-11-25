## Hook

When a restaurant is busy, a good waiter says "your food is on its way" instead of vanishing. If the kitchen runs out of paneer, they say "sorry, we're out of paneer, can I suggest something else?" They don't read out the kitchen's stock sheet.

A website needs the same manners. While a page loads, say so. When something fails, say sorry in plain words and offer a way forward. Next.js gives each of these moments its own file.

## Concept

### Special files in a route folder

You already know two special file names: `page.jsx` and `layout.jsx`. Three more handle the moments when a page isn't ready, has failed, or doesn't exist:

| File | Shows when |
|---|---|
| `loading.jsx` | the page is still getting its data |
| `error.jsx` | something threw an error while rendering |
| `not-found.jsx` | the page called `notFound()` |

Each one covers the folder it's in and every folder below it, like a layout does.

### Syntax: loading.jsx

```jsx
export default function Loading() {
  return <p role="status">Loading your orders...</p>;
}
```

A loading file's default export is a plain component. It gets no props. While the page in the same folder is still awaiting its data, Next.js shows this instead, then swaps the page in when it's ready.

```callout jargon
`role="status"` marks a **live region**: when its text appears, screen readers announce it without moving the reader's place. It's the polite way to say "please wait".
```

### How loading works: Suspense

Behind the scenes, Next.js wraps your page in React's `Suspense` component, with your loading file as the fallback:

```jsx
<Suspense fallback={<Loading />}>
  <OrdersPage />
</Suspense>
```

```callout jargon
**Suspense** is a React component that shows a **fallback** while something inside it is still waiting. When the waiting is over, it shows the real content. A `loading.jsx` file is a Suspense boundary that Next.js sets up for you.
```

```animate
title: Opening the orders page on slow 3G
kind: scene
nodes:
  - { id: phone, label: Phone, emoji: "📱", value: "Home" }
  - { id: layout, label: Layout, emoji: "🖼️" }
  - { id: page, label: OrdersPage, emoji: "📄", value: waiting }
  - { id: db, label: Orders database, emoji: "🗄️" }
steps:
  - say: The customer taps My orders. The page has to await the orders, which takes a few seconds today.
    send: { from: phone, to: page, label: "GET /orders" }
  - say: Next.js doesn't wait in silence. Straight away, it sends the layout with loading.jsx where the page will go.
    send: { from: layout, to: phone, label: "layout + Loading...", back: true }
    set: { phone: "Loading your orders..." }
  - say: The header and menu work already. Meanwhile the page is still waiting for the database.
    send: { from: page, to: db, label: "getOrders()" }
  - say: The orders arrive. The page finishes rendering.
    send: { from: db, to: page, label: "3 orders", back: true }
    set: { page: ready }
  - say: Next.js streams the finished page in, and it replaces the loading message.
    send: { from: page, to: phone, label: "the orders", back: true }
    set: { phone: "3 orders" }
```

### Syntax: error.jsx

```jsx
"use client";

export default function OrdersError({ error, retry }) {
  return (
    <div role="alert">
      <h2>We couldn't load your orders.</h2>
      <button onClick={() => retry()}>Try again</button>
    </div>
  );
}
```

An error file catches errors thrown while rendering its folder's pages. Three rules:

- It **must** start with `"use client"`. Error boundaries are a browser feature, as you saw in react-09.
- It gets the thrown value as `error`, and a function `retry`.
- `retry()` asks Next.js to fetch and render the page again. It helps when the problem was a hiccup, like a dropped connection.

```callout jargon
An **error boundary** is a component that catches errors in the components inside it and shows a fallback instead of a blank screen. You built one by hand in react-09. An `error.jsx` file is an error boundary that Next.js puts around the page for you.
```

### Never show the raw error

`error.message` is written for programmers. It can hold server addresses, file paths or database details, like `connect ECONNREFUSED 10.0.0.5:5432`. To a customer, that looks broken or even hacked, and it tells an attacker about your systems.

So an error page says sorry in plain words and offers `Try again`. The details go to the server's logs, where the team can read them.

```callout tip
In a production build, Next.js already hides the message of errors from server components and sends only a short code called `error.digest`. That code matches a line in the server logs.
```

### Syntax: not-found.jsx

```jsx
import Link from "next/link";

export default function OrderNotFound() {
  return (
    <>
      <h1>We couldn't find that order.</h1>
      <Link href="/orders">See all your orders</Link>
    </>
  );
}
```

When a page calls `notFound()`, Next.js shows the nearest `not-found.jsx`, with a 404 status. It's a normal component with no props, so it can be a server component. Always give the customer a way back.

A `not-found.jsx` directly in `app` also covers every address that matches no route at all.

### Where each file catches

An `error.jsx` wraps the page and anything below it in the same folder, but not the layout in that folder. An error in a layout is caught by the `error.jsx` one folder up. The root layout has no folder above it, so it uses a special `global-error.jsx`.

```text
src/app/orders/layout.jsx      an error here is caught by src/app/error.jsx
src/app/orders/error.jsx       catches errors in the page below
src/app/orders/page.jsx
```

## Worked example

The orders section with all three files:

```text
src/app/orders/
  page.jsx        awaits the customer's orders
  loading.jsx     <p role="status">Loading your orders...</p>
  error.jsx       "use client"; a kind sorry and a Try again button
  [id]/
    page.jsx      calls notFound() for an order id that doesn't exist
    not-found.jsx "We couldn't find that order." and a link back
```

- Tapping My orders shows the loading message at once, then the orders.
- If the database is down, the error file shows a sorry message. `Try again` calls `retry()`.
- `/orders/order-999` calls `notFound()`, so the customer sees the not-found page with a link back, not a crash.

## Try it

In this ticket's code, after the checks pass, change the error page to show `{error.message}`. One check fails: it passes in an error mentioning `ECONNREFUSED` and makes sure customers never see it. Change it back.

## Recap

- `loading.jsx` shows straight away while the page awaits its data. Next.js wraps the page in `Suspense` with it as the fallback.
- `error.jsx` must be a client component. It gets `error` and `retry`, and shows a kind message with a `Try again` button.
- Never show `error.message` to customers. Keep the details for the server logs.
- `not-found.jsx` shows when a page calls `notFound()`, and should always offer a way back.
- Each file covers its folder and the folders below it.

## Common mistakes

**1. An error file without "use client".**

```jsx
export default function OrdersError({ error, retry }) {
  return <button onClick={() => retry()}>Try again</button>;
}
```

Next.js stops with an error saying the error file must be a client component.

Fix: put `"use client";` on the first line.

**2. Showing the error's message.**

```jsx
<p>{error.message}</p>
```

The customer reads something like `connect ECONNREFUSED 10.0.0.5:5432`. It frightens them and gives away details about your servers.

Fix: a fixed, kind sentence, like `Please try again in a moment.`

**3. Calling retry while rendering.**

```jsx
<button onClick={retry()}>Try again</button>
```

`retry()` runs at once, during render, not on a click. The page retries over and over without anyone pressing anything.

Fix: pass a function: `onClick={() => retry()}`.

**4. A loading file in the wrong folder.**

```text
src/app/loading.jsx       (meant for the orders page)
```

It covers every page on the site, not just the orders page, and its message talks about orders on pages that have nothing to do with them.

Fix: put `loading.jsx` in the folder of the page that's slow: `src/app/orders/loading.jsx`.

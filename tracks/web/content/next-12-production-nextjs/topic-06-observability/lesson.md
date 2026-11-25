## Hook

A doctor can't open you up to see what's wrong. She takes your temperature, listens to your chest and asks what you ate. From those signs she works out what's happening inside.

Your app in production is the same. You can't open DevTools on a customer's phone in Nashik, or pause the server mid-order. You can only read the signs it leaves: logs, error reports and numbers. This ticket is about making sure it leaves good ones.

## Concept

### Seeing inside a running app

```callout jargon
**Observability** is how well you can tell what a running system is doing from the outside. It rests on three kinds of signal: **logs** (lines describing events), **errors** (reports of what broke, with stack traces) and **metrics** (numbers over time, like orders per minute).
```

On your laptop, a bug shows up in the terminal and the browser console, and you can try again. In production, the bug happened an hour ago, to someone else, on a phone you've never seen. If nothing was recorded, it's gone.

### Where console.log goes

A Next.js app runs in two places, so its logs land in two places:

| Code | Where `console.log` appears |
|---|---|
| Server components, route handlers, server actions, `proxy.js` | the **server's** logs: your terminal in development, the host's log viewer in production |
| Client components | the **customer's** browser console, which you never see |

So a `console.log` in `placeOrder` is useful: it reaches your logs. One in a client button vanishes on the customer's phone. Browser problems need a different tool, covered below.

### Structured logs

```callout jargon
A **structured log** is a log line written as data, usually one JSON object per line, with named fields. A **log level** says how serious it is: `debug`, `info`, `warn` or `error`.
```

```js
console.log(JSON.stringify({ level: "info", msg: "order placed", orderId: "order-17", totalPaise: 90000, ms: 84 }));
```

A sentence like "order 17 placed in 84ms" reads nicely, but is hard to search. A JSON line can be filtered: every `error` in the last hour, every slow order above 1,000 ms, everything about `order-17`. Log viewers are built for this.

Two rules keep logs safe:

- **Never log secrets**: no keys, passwords, session tokens or full cookies.
- **Log as little personal data as you can**: an order id, not the phone number and address. Logs are copied to many places and kept for months.

### error.js and retry

You met `error.js` in [Loading and error pages](/t/next-10-nextjs-fundamentals/topic-07-loading-and-error). It's a client component that Next.js shows when anything inside its folder throws. It gets two props: `error`, the thing thrown, and `retry`, a function that fetches and renders that part again.

```jsx
"use client";

import { useEffect } from "react";

export default function Error({ error, retry }) {
  useEffect(() => {
    reportToTracker(error);
  }, [error]);
  return (
    <div role="alert">
      <h2>Something went wrong</h2>
      <button type="button" onClick={() => retry()}>Try again</button>
    </div>
  );
}
```

A problem that's temporary, like a slow stock service, often goes away on retry. An `error.js` doesn't catch errors in the layout next to it. For the root layout itself, there's `global-error.js`.

### The digest

In production, Next.js doesn't send a server component's real error message to the browser. The message might contain a database address or a customer's details.

```callout jargon
A **digest** is a short id that Next.js gives each server error. The browser gets a generic message plus `error.digest`. The server logs the real error with the same digest.
```

```animate
title: Following a digest
kind: scene
nodes:
  - { id: phone, label: Customer in Nashik, emoji: "📱", row: 1, col: 0 }
  - { id: server, label: Next.js server, emoji: "🖥️", row: 1, col: 2 }
  - { id: logs, label: Server logs, emoji: "📜", row: 0, col: 4 }
  - { id: you, label: You, emoji: "🧑‍💻", row: 2, col: 4 }
steps:
  - say: The customer opens Golu's page. On the server, the stock service times out and the page throws.
    send: { from: phone, to: server, label: "GET /items/DC-1042" }
    set: { server: "error!" }
  - say: The server logs the real error, stock lookup timed out, with a digest.
    send: { from: server, to: logs, label: "error, digest 2417893051" }
  - say: The browser gets only a generic message and the digest. No details leak.
    send: { from: server, to: phone, label: "Something went wrong (2417893051)", back: true }
  - say: The customer sends Raju a screenshot showing the number.
    focus: [phone]
  - say: You search the logs for 2417893051 and find the real error, its time and its route.
    send: [{ from: you, to: logs, label: "search digest" }, { from: logs, to: you, label: "stock timeout, /items/[id]", back: true }]
    focus: [logs]
```

So show the digest on your error page, small, as a reference number. It turns "the site broke" into one log line you can find.

### Syntax: onRequestError

```js
export async function onRequestError(error, request, context) {
  await sendToTracker({ message: error.message, digest: error.digest, path: request.path, route: context.routePath });
}
```

Put a file called `instrumentation.js` in the project root, or in `src/`. If it exports `onRequestError`, Next.js calls it for **every** server error: in rendering, route handlers, server actions and the proxy.

- `request` has the `path`, `method` and `headers`.
- `context` says where it happened, such as `routePath: "/items/[id]"` and `routeType: "render"` or `"action"`.

It's the one place to send every server error to an error-tracking service, so none is missed. If it does async work, `await` it.

### Errors in the browser

Client components fail on the customer's phone, where your server never hears about it.

```callout jargon
An **error-tracking service**, such as Sentry, collects errors from browsers and servers, groups the same error together, and shows its stack trace, how often it happens and on which browsers.
```

Teams add one to every production app. In the browser it listens for uncaught errors and rejected promises, and `error.js` can report what it catches. To make stack traces readable, the service needs your build's **source maps**, uploaded privately rather than served to everyone.

### Metrics and alerts

```callout jargon
A **metric** is a number measured over time, like requests per minute. An **alert** is a message to the team, by phone or chat, when a metric crosses a line you chose.
```

Useful metrics for the store:

- **Error rate**: the share of requests that fail, such as 3 in every 1,000.
- **Slow requests**: how long the slowest 5% of page loads take. This is called **p95** latency.
- **Business signals**: orders per minute. A sudden drop to zero on a Saturday is an incident, even if no error is logged.
- **Web Vitals from real visitors**: Next.js's `useReportWebVitals` hook can send each visitor's LCP, INP and CLS to you.

Alert on what customers feel, such as checkout failing or orders stopping, not on every single error. Too many alerts teach a team to ignore them all.

## Worked example

Here's how the server action that places orders logs its work, written for production.

```js
"use server";

export async function placeOrder(formData) {
  const started = Date.now();
  const itemId = String(formData.get("id"));
  try {
    const order = await saveOrder(itemId, Number(formData.get("qty")));
    console.log(JSON.stringify({ level: "info", msg: "order placed", orderId: order.id, itemId, ms: Date.now() - started }));
    return { ok: true, orderId: order.id };
  } catch (error) {
    console.error(JSON.stringify({ level: "error", msg: "order failed", itemId, error: error.message }));
    return { ok: false, error: "We couldn't place your order. Please try again." };
  }
}
```

- One JSON line per event, with a level, a short message and the fields you'd search by.
- `ms` records how long it took, so slow orders show up in a search.
- It logs the item and order ids, never the customer's phone, address or payment details.
- The customer gets a kind, plain message. The real reason goes to the logs.

## Try it

This reads a few structured log lines and finds the one that matches a digest from a customer's screenshot. Run it.

```js run
const logs = [
  JSON.stringify({ level: "info", msg: "order placed", orderId: "order-17", ms: 84 }),
  JSON.stringify({ level: "error", msg: "stock lookup timed out", digest: "2417893051", route: "/items/[id]" }),
  JSON.stringify({ level: "info", msg: "order placed", orderId: "order-18", ms: 1290 }),
];

const entries = logs.map((line) => JSON.parse(line));
const match = entries.find((entry) => entry.digest === "2417893051");
console.log(match.msg + " on " + match.route);

const slow = entries.filter((entry) => entry.ms > 1000).map((entry) => entry.orderId);
console.log("slow orders: " + slow.join(", "));
```

It prints:

```text
stock lookup timed out on /items/[id]
slow orders: order-18
```

1. Add a fourth line for `order-19` taking 2,400 ms. What does the second line print now?
2. Count the entries whose `level` is `"error"`, and print the error rate out of all entries.

## Recap

- Observability means reading a running app from outside, through logs, errors and metrics.
- `console.log` in server code goes to the server's logs. In client components it stays on the customer's phone.
- Write structured JSON logs with a level and searchable fields, and never log secrets or more personal data than you need.
- In production, server error messages are hidden behind a `digest`. Show it on `error.js`, and search the logs for it. `retry()` tries the failed part again.
- Send every server error to a tracker with `onRequestError` in `instrumentation.js`, catch browser errors with an error-tracking service, and alert on what customers feel.

## Common mistakes

**1. Debugging a server action with the browser console.**

```js
console.log("qty is", qty);   // inside a "use server" action, while watching the browser's DevTools
```

Symptom: nothing appears in DevTools, so it looks like the action never ran. Fix: look in the terminal running `next dev`, or the host's logs in production.

**2. Logging secrets and personal data.**

```js
console.log(JSON.stringify({ msg: "payment", cookie: request.headers.get("cookie"), phone: customer.phone }));
```

Symptom: session tokens and phone numbers now sit in log files, readable by anyone with log access, for months. Fix: log ids and outcomes only, like `orderId` and `status`.

**3. Showing the raw error to customers.**

```jsx
<p>{error.message}</p>
```

Symptom: in development it shows the real message, so you think customers will see it too. In production they see a generic message, and nobody can match it to a log. Fix: show a kind message and `error.digest` as a reference number.

**4. Catching errors and dropping them.**

```js
async function place(itemId, qty) {
  try {
    await saveOrder(itemId, qty);
  } catch (error) {}
}
```

Symptom: orders silently go missing, and the logs show nothing at all. Fix: log the error with its context, tell the customer plainly, and let the tracker see it.

**5. Alerting on everything.**

```text
ALERT: 1 error in the last minute   (x 300 a day)
```

Symptom: the team mutes the channel, and misses the real outage on Saturday. Fix: alert on customer-facing rates, like checkout failures above 2% for five minutes, or orders dropping to zero.

## Hook

At a big mall, a guard at the gate glances at everyone who walks in. Lost visitors get pointed to the right entrance. People without a pass get sent to the desk. It's quick, and it stops most problems early.

But the jeweller inside still checks who you are before opening the case. The gate guard can't know every rule of every shop. Your Next.js app works the same way: a fast check at the gate, and a careful check at the counter.

## Concept

### Proxy: code that runs before every route

```callout jargon
A **proxy** in Next.js is a function in `src/proxy.js` that runs before a request reaches any page or route handler. It can let the request through, send the browser somewhere else, or answer by itself. Before Next.js 16 this file was called **middleware**.
```

It's the gate guard. It sees the address and the cookies of every request it covers, and it runs before any page code.

### Syntax: a proxy file

```js
export function proxy(request) {
  if (request.nextUrl.pathname === "/old") {
    return Response.redirect(new URL("/new", request.url), 308);
  }
}

export const config = {
  matcher: ["/old"],
};
```

- The function is called `proxy` and gets a `NextRequest`, which has `nextUrl` (the address, already parsed) and `cookies`.
- Returning a response, like a redirect, ends the request there.
- Returning nothing lets the request continue to the page.
- `config.matcher` lists which paths the proxy runs for.

### What the proxy does with a request

```animate
title: The proxy at the gate
kind: scene
nodes:
  - { id: phone, label: Ravi's phone, emoji: "📱", value: "no session" }
  - { id: proxy, label: proxy.js, emoji: "🚧" }
  - { id: page, label: "/account/orders page", emoji: "📄" }
  - { id: login, label: "/login page", emoji: "🔑" }
steps:
  - say: Ravi taps My orders, but he isn't logged in. The path matches /account/:path*, so the proxy runs first.
    send: { from: phone, to: proxy, label: "GET /account/orders" }
  - say: The proxy looks for the dhaaga_session cookie. There is none.
    focus: [proxy]
  - say: It answers with a redirect to the login page, remembering where Ravi wanted to go.
    send: { from: proxy, to: phone, label: "307 → /login?next=/account/orders", back: true }
  - say: The browser follows the redirect. The orders page never ran.
    send: { from: phone, to: login, label: "GET /login?next=/account/orders" }
  - say: After logging in, the request has a cookie. The proxy returns nothing, so the request goes on to the page.
    send: [{ from: phone, to: proxy, label: "GET /account/orders + cookie" }, { from: proxy, to: page, label: "continue" }]
    set: { phone: "dhaaga_session=…" }
```

### Syntax: matcher paths

```js
export const config = {
  matcher: ["/account/:path*", "/catalogue/:path*"],
};
```

- `/account/:path*` matches `/account`, `/account/orders` and `/account/orders/order-17`. The `*` means "zero or more parts".
- Without a matcher, the proxy runs for every request, even images and CSS files. Keep it to the paths that need it.
- Matchers must be plain values written in the file, because Next.js reads them when it builds.

### Redirects: temporary or permanent

```callout jargon
A **redirect** is an answer that tells the browser "go to this other address instead". Its `Location` header holds the new address.
```

Two statuses matter most:

| Status | Meaning | Use it for |
|---|---|---|
| 307 | Temporary Redirect | "Log in first", then come back |
| 308 | Permanent Redirect | A page that moved for good, like `/catalogue` to `/items` |

Browsers and search engines remember a permanent redirect, so only use 308 when the old address will never come back.

```js run
const res = Response.redirect(new URL("/login?next=/account/orders", "http://localhost/account/orders"), 307);
console.log(res.status);
console.log(res.headers.get("location"));
```

`new URL(path, request.url)` turns a path into a full address on the same site. The `next=` part lets the login page send the customer back afterwards.

### Reading cookies in the proxy

```js
const id = request.cookies.get("dhaaga_session")?.value;
```

`NextRequest` has its own `cookies`, with the same `get` you used in topic 7. The proxy only checks that a session cookie exists. It doesn't look the session up: the proxy should stay quick, and it runs separately from your app's data.

### The proxy is only the gate

A cookie that exists might be old, or made up. And a matcher can miss a path after someone renames a folder. Next.js's own docs say it plainly: always check again inside the page, action or data layer.

```callout warning
Treat the proxy as a quick first check that sends lost or logged-out visitors to the right place. The real "may this person see this?" check belongs on the server, next to the data.
```

### Syntax: redirect and notFound in server code

```js
import { notFound, redirect } from "next/navigation";

redirect("/login?next=/account/orders");
notFound();
```

- `redirect(path)` sends the browser somewhere else, from a server component, server action or route handler. (`permanentRedirect` is the 308 version.)
- `notFound()` shows the nearest `not-found.jsx` page, with status 404.
- Both work by throwing, like you saw in topic 5. Nothing after them runs, and they must not sit inside `try`.

### Guards: one function that checks and redirects

```callout jargon
A **guard** is a small function a page calls first. It either returns what the page needs, like the logged-in customer, or stops the page with a redirect or a 404.
```

```js
export function requireCustomer(cookieStore, path) {
  const customer = currentCustomer(cookieStore);
  if (!customer) redirect(`/login?next=${path}`);
  return customer;
}
```

Every account page starts with `const customer = requireCustomer(await cookies(), "/account/orders");`. If the line finishes, there is a customer.

### Someone else's order is not found

Ravi changed `order-18` to `order-17` in the address and saw Asha's order. The fix is a guard that checks the order belongs to the customer. But what should Ravi see?

Not "this order belongs to someone else". That tells him order-17 exists. Answer exactly as if it didn't: `notFound()`. A stranger can't tell other people's orders from orders that were never placed.

### Fixed moves can live in next.config.js

For a simple, permanent move with no conditions, `next.config.js` has a `redirects()` list too. It's checked before the proxy runs. Use the proxy when the decision depends on the request, like a cookie.

## Worked example

First, the gate. Old links move for good, and the account pages need a session cookie.

```js
export function proxy(request) {
  const { pathname } = request.nextUrl;

  if (pathname === "/catalogue" || pathname.startsWith("/catalogue/")) {
    const rest = pathname.slice("/catalogue".length);
    return Response.redirect(new URL(`/items${rest}`, request.url), 308);
  }

  if (!request.cookies.get("dhaaga_session")?.value) {
    return Response.redirect(new URL(`/login?next=${pathname}`, request.url), 307);
  }
}

export const config = {
  matcher: ["/account/:path*", "/catalogue/:path*"],
};
```

- `slice` drops `/catalogue` from the front, so `/catalogue/DC-1042` becomes `/items/DC-1042`.
- The matcher only lets `/account` and `/catalogue` paths in, so the cookie check only applies to account pages.
- With a cookie, the function ends without returning, and the request goes on.

Then the guards for the account pages. `currentCustomer` and `findOrder` are done in other files.

```js
import { notFound, redirect } from "next/navigation";
import { currentCustomer } from "./session.js";
import { findOrder } from "./orders.js";

export function requireCustomer(cookieStore, path) {
  const customer = currentCustomer(cookieStore);
  if (!customer) redirect(`/login?next=${path}`);
  return customer;
}

export function getOwnOrder(cookieStore, orderId) {
  const customer = requireCustomer(cookieStore, `/account/orders/${orderId}`);
  const order = findOrder(orderId);
  if (!order || order.phone !== customer.phone) notFound();
  return order;
}
```

- `requireCustomer` returns the customer, or stops with a redirect to the login page.
- `getOwnOrder` uses it first, so logged-out visitors go to the login page too.
- A missing order and someone else's order give the same `notFound()`.

## Try it

When the checks pass, read the order page check in your head for Ravi: he has a session, so `requireCustomer` gives `{ phone: "9876543211" }`. order-17's phone is Asha's, so the condition is true and `notFound()` throws. Now delete the `order.phone !== customer.phone` part and run the checks: the "someone else's order" check fails. Put it back.

## Recap

- `src/proxy.js` (once called middleware) runs before the routes its `config.matcher` lists. Return a response to stop the request, or nothing to let it through.
- Use 307 for "go here first, then come back" and 308 for pages that moved for good. `Response.redirect(new URL(path, request.url), status)` builds one.
- The proxy reads `request.nextUrl.pathname` and `request.cookies`, and stays a quick first check.
- In server code, `redirect()` and `notFound()` from `next/navigation` stop the page by throwing.
- Guards check again next to the data. Someone else's order answers `notFound()`, just like a missing one.

## Common mistakes

**1. Trusting the proxy alone.**

```jsx
export default async function OrderPage({ params }) {
  const { id } = await params;
  return <Order order={findOrder(id)} />;
}
```

The proxy made sure there's a session cookie. But any logged-in customer can open any order by changing the number in the address.

Fix: `const order = getOwnOrder(await cookies(), id);` at the top of the page.

**2. A permanent redirect for the login page.**

```js
export function proxy(request) {
  if (!request.cookies.get("dhaaga_session")?.value) {
    return Response.redirect(new URL("/login", request.url), 308);
  }
}
```

The browser remembers it. After logging in, the customer taps My orders and the browser jumps straight to the login page again, without even asking the server.

Fix: use 307 for anything that depends on who is asking.

**3. No matcher.**

```js
export function proxy(request) {
  if (!request.cookies.get("dhaaga_session")) {
    return Response.redirect(new URL("/login", request.url), 307);
  }
}
```

With no `config`, the proxy runs for every request, including the login page itself and its CSS. Logged-out visitors bounce between redirects, and the browser stops with "too many redirects".

Fix: `export const config = { matcher: ["/account/:path*"] };`.

**4. Telling strangers an order exists.**

```js
if (order.phone !== customer.phone) {
  throw new Error("This order belongs to another customer.");
}
```

Now anyone can test numbers and learn which orders exist, and the error page shows a scary message.

Fix: `notFound()` for both a missing order and someone else's.

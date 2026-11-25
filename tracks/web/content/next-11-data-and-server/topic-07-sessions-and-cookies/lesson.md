## Hook

At a big wedding hall, you hand your shoes to the shoe stand and get a brass token with a number on it. The token doesn't say "Asha's red sandals". It's just a number. The person at the stand keeps the list of which number goes with which shoes.

If you scratched a different number onto your token, you'd most likely get nobody's shoes, because nobody gets that number by chance. That token is how a website should remember you: a meaningless number for the browser, and the real list kept on the server.

## Concept

### The web forgets you

Every request to a server stands alone. When Asha loads "My orders", then taps an order, the server sees two separate requests. Nothing in HTTP itself says they came from the same person.

```callout jargon
HTTP is **stateless**: each request carries everything the server needs, and the server doesn't remember earlier requests by itself.
```

To remember a logged-in customer, the browser must send something with every request that says "it's me again". That something is a cookie.

### Cookies: notes the browser sends back

```callout jargon
A **cookie** is a small named value that a server asks the browser to keep. The browser sends it back with every later request to that site.
```

```animate
title: A cookie travels with every request
kind: scene
nodes:
  - { id: phone, label: Asha's phone, emoji: "📱", value: "no cookie" }
  - { id: next, label: dhaaga.in server, emoji: "🖥️" }
  - { id: sessions, label: Sessions list, emoji: "📋", value: empty }
steps:
  - say: Asha logs in with her phone number and a one-time code.
    send: { from: phone, to: next, label: "POST /login" }
  - say: The server makes a random id and writes down that it belongs to Asha.
    set: { sessions: "f3a9… → 98765 43210" }
    focus: [sessions]
  - say: The answer asks the browser to keep a cookie holding only that random id.
    send: { from: next, to: phone, label: "Set-Cookie: dhaaga_session=f3a9…", back: true }
    set: { phone: "dhaaga_session=f3a9…" }
  - say: Later Asha opens My orders. The browser adds the cookie to the request by itself.
    send: { from: phone, to: next, label: "GET /account  Cookie: dhaaga_session=f3a9…" }
  - say: The server looks the id up in its list and finds Asha. Now it can show her orders, and only hers.
    focus: [sessions, next]
```

### Never trust what a cookie says

The browser belongs to the customer. Anyone can open DevTools and change a cookie's value to anything. So a cookie that says *who you are* is a lie waiting to happen:

```js
cookieStore.set("customer", "9876543210");
```

Change that to someone else's number, and you are them. That's exactly what happened to Asha's orders.

```callout warning
A cookie may hold an id the server looks up, but never a fact the server believes, like a phone number, a role or a price.
```

### Sessions: a random id, remembered on the server

```callout jargon
A **session** is the server's memory of one logged-in visitor. The cookie holds only the **session id**, a long random value that's impossible to guess. The server keeps a list from ids to customers.
```

```js run
const sessions = new Map();

function startSession(id, phone) {
  sessions.set(id, { phone });
}

startSession("f3a9c1d7", "9876543210");
console.log(sessions.get("f3a9c1d7")?.phone);
console.log(sessions.get("9876543211")?.phone);
```

Typing a phone number into the cookie finds nothing, because no session has that id. Real ids come from `crypto.randomUUID()`, which makes values like `3b241101-e2bb-4255-8caf-4136c566a962`.

### Syntax: cookies in Next.js

```js
import { cookies } from "next/headers";

const cookieStore = await cookies();
const id = cookieStore.get("dhaaga_session")?.value;
cookieStore.set("dhaaga_session", id, { httpOnly: true });
cookieStore.delete("dhaaga_session");
```

- `cookies()` is async in Next.js 16, so you `await` it.
- `get(name)` gives `{ name, value }`, or `undefined` when there's no such cookie. So read it with `?.value`.
- `set` and `delete` change the answer's cookies. They only work in server actions and route handlers, because a page that's already streaming can't change its headers.

### Syntax: cookie options

```js
cookieStore.set("dhaaga_session", id, {
  httpOnly: true,
  secure: true,
  sameSite: "lax",
  path: "/",
  maxAge: 60 * 60 * 24 * 7,
});
```

- `httpOnly: true`: JavaScript in the page can't read it, so a sneaky script can't steal the session.
- `secure: true`: the browser only sends it over HTTPS, never over plain HTTP.
- `sameSite: "lax"`: other websites can't make the browser send it along with their own form posts.
- `path: "/"`: it's sent for every page on the site.
- `maxAge`: seconds until the browser throws it away. This is one week.

### Sessions run out

A session should not last forever. A shared phone, a lost laptop, an old cookie: all are risks. So the server stores when each session ends and checks it on every request.

```animate
title: Is this session still good?
kind: code
language: js
code: |
  const WEEK = 7 * 24 * 60 * 60 * 1000;
  const session = { phone: "9876543210", expiresAt: 0 + WEEK };
  let now = WEEK - 1;
  console.log(now >= session.expiresAt ? "expired" : "still logged in");
  now = WEEK;
  console.log(now >= session.expiresAt ? "expired" : "still logged in");
steps:
  - line: 1
    vars: { WEEK: 604800000 }
    say: A week, in milliseconds, because that's what Date.now() counts in.
  - line: 2
    vars: { session: "{ expiresAt: 604800000 }" }
    say: The session started at time 0, so it ends exactly one week later.
  - line: 3
    vars: { now: 604799999 }
    say: One millisecond before the end.
  - line: 4
    out: "still logged in"
    say: now is smaller than expiresAt, so the session is still good.
  - line: 5
    vars: { now: 604800000 }
    say: Now it's exactly the end time.
  - line: 6
    out: "expired"
    say: now has reached expiresAt. The server treats the visitor as logged out.
```

The cookie's `maxAge` and the server's `expiresAt` match, but only the server's check counts. A browser can be told to keep a cookie longer.

### Keep cookie code testable

`cookies()` only works while Next.js is handling a request. So keep the session logic in plain functions that take the cookie store as an argument:

```js
export function currentCustomer(cookieStore, now = Date.now()) {
  const id = cookieStore.get("dhaaga_session")?.value;
  return id ? lookUp(id, now) : null;
}
```

The page or action passes the real one: `currentCustomer(await cookies())`. A test passes a pretend store with the same `get`, `set` and `delete`.

### Logging out

Logging out must do two things. Remove the cookie, so the browser stops sending it. And forget the session on the server, so even a copied cookie stops working.

```callout tip
Real projects often use a library such as Auth.js or a hosted login service for this. They do the same steps: a random session id or a signed token in an httpOnly cookie, checked on the server.
```

## Worked example

Kavya's session module. `now` is a parameter so tests can choose the time.

```js
import { randomUUID } from "node:crypto";

export const SESSION_COOKIE = "dhaaga_session";
export const WEEK_SECONDS = 60 * 60 * 24 * 7;
const sessions = new Map();

export function startSession(cookieStore, phone, now = Date.now()) {
  const id = randomUUID();
  sessions.set(id, { phone, expiresAt: now + WEEK_SECONDS * 1000 });
  cookieStore.set(SESSION_COOKIE, id, {
    httpOnly: true,
    secure: true,
    sameSite: "lax",
    path: "/",
    maxAge: WEEK_SECONDS,
  });
  return id;
}

export function currentCustomer(cookieStore, now = Date.now()) {
  const id = cookieStore.get(SESSION_COOKIE)?.value;
  const session = id ? sessions.get(id) : undefined;
  if (!session) return null;
  if (now >= session.expiresAt) {
    sessions.delete(id);
    return null;
  }
  return { phone: session.phone };
}
```

- The cookie holds a random id. The phone number only lives in the server's Map.
- Every way of failing gives `null`: no cookie, a made-up id, or an old session.
- An expired session is deleted when it's found, so the Map doesn't fill up with old ones.
- `maxAge` is in seconds, but `expiresAt` is in milliseconds, like `Date.now()`.

## Try it

When the checks pass, write a test of your own in your head: what does `currentCustomer` give for a cookie holding `"9876543210"`? Trace it: `sessions.get("9876543210")` is `undefined`, so `null`. That is the bug from the ticket, closed.

## Recap

- HTTP forgets every request, so the browser sends a cookie each time to say who it is.
- A cookie may hold a random session id. The server keeps the list from ids to customers. Never trust a cookie's value as a fact.
- Set session cookies with `httpOnly`, `secure`, `sameSite: "lax"`, `path: "/"` and a `maxAge`.
- Sessions expire: the server stores when, and checks on every request. Logging out deletes the cookie and the session.
- In Next.js 16, `await cookies()` gives the cookie store. Pass it into plain functions so they're easy to test.

## Common mistakes

**1. Putting the customer in the cookie.**

```js
cookieStore.set("customer", phone);
```

Anyone can edit the cookie in DevTools and become any customer. No error shows: the site just obeys.

Fix: store a random id from `randomUUID()` in the cookie, and keep the phone in the server's session list.

**2. Forgetting httpOnly.**

```js
cookieStore.set("dhaaga_session", id);
```

The cookie works, but any script on the page can read it with `document.cookie`. One bad script, like an unsafe review, can send it to a stranger.

Fix: `{ httpOnly: true, secure: true, sameSite: "lax", path: "/" }`.

**3. Reading the cookie without ?.value.**

```js
const id = cookieStore.get("dhaaga_session").value;
```

For a visitor with no cookie, `get` gives `undefined`, and the page crashes with `TypeError: Cannot read properties of undefined (reading 'value')`.

Fix: `cookieStore.get("dhaaga_session")?.value`.

**4. Logging out only in the browser.**

```js
cookieStore.delete("dhaaga_session");
```

The browser forgets the cookie, but the session lives on in the server's list. Anyone who copied the cookie earlier can still use it for a week.

Fix: `sessions.delete(id)` as well.

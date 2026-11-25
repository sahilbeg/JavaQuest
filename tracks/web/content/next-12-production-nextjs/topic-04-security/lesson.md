## Hook

A shop counter has a glass case in front of it. Customers can point at anything, but they can't reach behind and take the cash box. The shopkeeper checks the price on her own list, not the price a customer says. And she tells each customer about their own order, not someone else's.

A Next.js app has a counter too: the line between the browser and your server. Anything on the customer's side can be changed by the customer. This ticket is about keeping the cash box, the price list and other people's orders behind the glass.

## Concept

### The one rule: the browser is not yours

```callout jargon
An **attacker** is anyone using your app in a way you didn't plan, to get something they shouldn't. Usually it's not a movie hacker. It's a curious customer with DevTools open.
```

Everything that reaches the server from a browser can be faked: form fields, hidden inputs, cookies, headers, the URL. A customer can edit a hidden field in DevTools, or skip your page and send a request with `curl`. So the server must treat every incoming value as a claim to check, never as a fact.

Next.js makes the server easy to forget, because server and browser code live side by side in one project. Keep asking of every line: which side of the counter does this run on?

### Secrets stay on the server

```callout jargon
A **secret** is a value that gives power to whoever holds it: a payment gateway key, a database password, an admin token. An **environment variable** is a setting given to the app from outside its code, read with `process.env.NAME`.
```

In Next.js, environment variables are **server-only by default**. `process.env.UPI_SECRET` works in a server component, a route handler or a server action, and is never sent to the browser.

```jsx
const key = process.env.UPI_SECRET;                  // server code only: safe
const api = process.env.NEXT_PUBLIC_API_URL;         // copied into the browser's JavaScript at build time
```

A name starting with **`NEXT_PUBLIC_`** is different. During `next build`, its value is written straight into the JavaScript files every visitor downloads. That's right for public settings, like the site's address. It's a leak for anything secret. You met the same idea in [Environment variables and secrets](/t/next-11-data-and-server/topic-10-environment-and-secrets).

### Syntax: import "server-only"

```js
import "server-only";
```

Put this line at the top of a module that must never reach the browser, like `lib/payments.js`. If a client component ever imports that module, even through another file, `next build` fails with an error. A mistake that would leak a secret becomes a red build instead.

### Server actions are public doors

A server action looks like a function you call from your own form. Under the hood, Next.js gives it an address, and the form sends a `POST` request to it.

```callout jargon
An **endpoint** is an address on a server that answers requests. Every server action is an endpoint, even though you never wrote a URL for it.
```

So anyone who finds that endpoint can call your action with **any fields they like**, without your form, without your page, and without being logged in. Treat every action exactly like a public API route:

- Check who is calling, inside the action itself. A login check on the page doesn't protect the action.
- Check every field: is it there, is it the right kind, is it in range?
- Look up prices, stock and permissions on the server, from your own data.

```animate
title: The one-paisa Golu
kind: scene
nodes:
  - { id: form, label: Buy now form, emoji: "🛒", value: "paise=45000", row: 0, col: 0 }
  - { id: tools, label: DevTools, emoji: "🛠️", row: 2, col: 0 }
  - { id: action, label: placeOrder, emoji: "⚙️", row: 1, col: 3 }
  - { id: db, label: Catalogue, emoji: "📒", value: "DC-1042: 45000", row: 1, col: 5 }
steps:
  - say: The old Buy now form has a hidden field holding Golu's price, so the action can multiply it.
    focus: [form]
  - say: A customer opens DevTools and changes the hidden field to 1. Hidden only means not shown.
    send: { from: tools, to: form, label: "edit" }
    set: { form: "paise=1" }
  - say: The form sends the fields. The old action trusts them and saves an order for 2 paise.
    send: { from: form, to: action, label: "id=DC-1042, qty=2, paise=1" }
    set: { action: "total 2 paise!" }
    focus: [action]
  - say: The fixed action ignores the paise field. It looks Golu up in its own catalogue.
    send: [{ from: action, to: db, label: "getItem(DC-1042)" }, { from: db, to: action, label: "45000", back: true }]
    set: { action: "total 90000" }
  - say: Now the price comes from the server's side of the counter, whatever the form says.
    focus: [db]
```

### Validating input

```callout jargon
To **validate** input is to check that a value is the kind you expect, and in range, before you use it. Reject it with a clear message if not.
```

Every `FormData` value is text, or `null` when the field is missing. Turn it into the type you need, then check it.

```animate
title: Which quantities pass?
kind: code
language: js
code: |
  const valid = (text) => {
    const qty = Number(text);
    return Number.isInteger(qty) && qty >= 1 && qty <= 5;
  };
  console.log(valid("2"));
  console.log(valid("2.5"));
  console.log(valid(""));
steps:
  - line: 1
    say: valid takes the text a form sent, because every FormData value is text.
  - line: 2
    say: Number turns the text into a number. Text that isn't a number becomes NaN.
  - line: 3
    say: A good quantity is a whole number, at least 1 and at most 5. NaN fails isInteger.
  - line: 5
    out: "true"
    say: '"2" becomes 2: whole, and in range.'
  - line: 6
    out: "false"
    say: '"2.5" is not a whole number. Nobody buys half an elephant.'
  - line: 7
    out: "false"
    say: 'An empty field becomes 0, which is below 1. Number("") is 0, a sneaky edge to remember.'
```

Bigger apps use a validation library such as Zod to describe the shape of good input once. The idea is the same.

### Who may see what

```callout jargon
**Authentication** answers "who are you?", usually with a session cookie set at login. **Authorisation** answers "are you allowed to do this?", such as reading this one order.
```

A route like `/api/orders/[id]` must do both. First find the customer from the session cookie, and answer **401** if there is none. Then check that the order belongs to that customer.

If it doesn't, answer **404**, the same as for an order that doesn't exist. A "403 Forbidden" would tell an attacker that order-17 is real, and they could try order-18, order-19, and so on, to count your orders. Guessing ids to read other people's data is so common it has a name: an **IDOR** (insecure direct object reference).

### Send only what's needed

Even for the right customer, don't send the whole record. Pick the fields the page shows:

```js
function answer(order) {
  return Response.json({ id: order.id, itemId: order.itemId, qty: order.qty, totalPaise: order.totalPaise, status: order.status });
}
```

```callout jargon
**Data minimisation** means sending, showing and keeping only the data a feature really needs. Data you never send can never leak.
```

`Response.json(order)` sends every field, including ones added next year, like a payment reference. Building the answer by hand means a new field stays private until someone decides to share it. The same goes for props passed to client components.

### Security headers

Response headers can switch on protections built into every browser. In Next.js you set them for every page in `next.config.mjs`:

```js
export default {
  async headers() {
    return [{ source: "/(.*)", headers: securityHeaders }];
  },
};
```

```callout jargon
**XSS** (cross-site scripting) is when an attacker gets their own script to run inside your page, for example through a product review. A **Content Security Policy** (CSP) is a header listing where scripts, styles and images may come from, so a smuggled script is blocked.
```

The headers that matter most:

| Header | Value | What it stops |
|---|---|---|
| `Content-Security-Policy` | `default-src 'self'; object-src 'none'; frame-ancestors 'none'; ...` | scripts from other sites, old plugins, and your page being shown inside someone else's frame |
| `X-Content-Type-Options` | `nosniff` | the browser guessing that a text file is a script |
| `Referrer-Policy` | `strict-origin-when-cross-origin` | full page addresses leaking to other sites |

React already escapes text in JSX, so a review saying `<script>` shows as words, not code. The danger is `dangerouslySetInnerHTML`, which skips that escaping. A CSP is the second wall behind the first. A stricter CSP uses a **nonce**, a random value made for each request in `proxy.js`, but that forces every page to be dynamic.

## Worked example

Here is a server action that lets a customer stitch a name onto their order, for ₹50. It's written the careful way.

```js
"use server";

import { getOrder, setStitchedName } from "../lib/orders.js";
import { currentCustomer } from "../lib/session.js";

export async function addStitchedName(formData) {
  const customer = await currentCustomer();
  if (!customer) {
    return { ok: false, error: "Please log in" };
  }
  const order = await getOrder(String(formData.get("orderId")));
  if (!order || order.customerId !== customer.id) {
    return { ok: false, error: "Order not found" };
  }
  const name = String(formData.get("name") ?? "").trim();
  if (!/^[A-Za-z ]{1,12}$/.test(name)) {
    return { ok: false, error: "Use 1 to 12 letters" };
  }
  await setStitchedName(order.id, name, 5000);
  return { ok: true };
}
```

Line by line:

- `currentCustomer()` reads the session on the server. The action never takes a customer id from the form.
- The order must exist **and** belong to this customer, with one shared message for both failures.
- The name is trimmed and checked: letters and spaces only, at most 12. That's what fits on a small amigurumi.
- The ₹50 (5000 paise) is written in server code. The form never sends a price.

## Try it

This is a tiny route handler that checks who's asking before it answers. Run it.

```js run
const orders = { "order-17": { owner: "asha", status: "PACKED", phone: "98765 43210" } };

function getOrder(id, customer) {
  if (!customer) return { status: 401, body: { error: "Please log in" } };
  const order = orders[id];
  if (!order || order.owner !== customer) return { status: 404, body: { error: "Order not found" } };
  return { status: 200, body: { id, status: order.status } };
}

console.log(JSON.stringify(getOrder("order-17", null)));
console.log(JSON.stringify(getOrder("order-17", "ravi")));
console.log(JSON.stringify(getOrder("order-17", "asha")));
```

It prints:

```text
{"status":401,"body":{"error":"Please log in"}}
{"status":404,"body":{"error":"Order not found"}}
{"status":200,"body":{"id":"order-17","status":"PACKED"}}
```

1. Ask for `"order-99"` as `"ravi"`. Can Ravi tell the difference between an order that doesn't exist and Asha's order?
2. Change the last line of `getOrder` to send the whole `order`. What would Asha's answer now include that the page never shows?

## Recap

- Everything from the browser can be faked: fields, hidden inputs, cookies and URLs. The server checks every value and looks up prices and permissions itself.
- Environment variables are server-only unless named `NEXT_PUBLIC_`, which are copied into public JavaScript. Mark secret modules with `import "server-only"`.
- Every server action is a public endpoint. Check who is calling and validate every field inside the action.
- Answer 401 when nobody is logged in, and the same 404 for "missing" and "not yours". Send only the fields the page needs.
- Set security headers in `next.config.mjs`: a Content Security Policy, `X-Content-Type-Options: nosniff` and a `Referrer-Policy`.

## Common mistakes

**1. Trusting a price from the form.**

```js
const totalPaise = Number(formData.get("paise")) * qty;
```

Symptom: someone edits the hidden field and buys Golu for one paisa. The check says two Golus should cost 90000 paise, whatever the form says. Fix: `const item = await getItem(id);` and use `item.paise`.

**2. A secret in a NEXT_PUBLIC_ variable.**

```js
const key = process.env.NEXT_PUBLIC_UPI_SECRET;
```

Symptom: the key is inside a JavaScript file every visitor downloads, readable in DevTools. Fix: drop the prefix, read it only in server code, and add `import "server-only"` to the module that uses it.

**3. Checking login only on the page.**

```jsx
if (!customer) redirect("/login");   // in page.jsx, but not in the action the page's form calls
```

Symptom: the page is safe, but anyone can still POST straight to the action. Fix: check the session again inside every server action and route handler.

**4. Telling attackers which orders exist.**

```js
function answer(order, customerId) {
  if (order.customerId !== customerId) return Response.json({ error: "Not your order" }, { status: 403 });
  return Response.json({ id: order.id, status: order.status });
}
```

Symptom: an attacker tries order-1 to order-500 and learns exactly how many orders you have. Fix: answer the same 404 "Order not found" for missing and not-yours.

**5. Sending the whole record.**

```js
function answer(order) {
  return Response.json(order);
}
```

Symptom: the tracking page shows only the status, but the response also carries the phone number and address for anyone watching the network tab. Fix: build the answer from just the fields the page needs.

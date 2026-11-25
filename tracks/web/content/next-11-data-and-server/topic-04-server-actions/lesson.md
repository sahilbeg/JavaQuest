## Hook

At the stall, the order book sits behind the counter. Customers don't write in it. They hand Raju a slip, and Raju writes the order down. The book only changes in one place, by one trusted hand.

Your pages can read data on the server now. But changing data, like marking an order packed, still needs a way to send a "slip" from the browser to the server. In topic 3 you could build an endpoint and `fetch` it. For your own forms, Next.js has something shorter: a server action.

## Concept

### A function the browser can call

```callout jargon
A **server action** is an async function that always runs on the server, but that a form or button in the browser can call. Next.js does the sending for you.
```

You write it like a normal function. You give it to a form as its `action`. When the customer presses the button, the browser sends the form to the server, the function runs there, and the page updates.

```callout jargon
To **mutate** data means to change it: create, update or delete. A server action is the usual way to mutate data in Next.js.
```

### Syntax: a server action

```js
"use server";

export async function markPacked(formData) {
  const orderId = formData.get("orderId");
  // change the data here
}
```

- `"use server"` at the very top of the file marks every export as a server action.
- Each one must be an `async` function.
- When a form calls it, it receives the form's fields as a `FormData` object.

You can also put `"use server"` as the first line inside one async function in a server component. A separate `actions.js` file keeps things tidy, and client components can import from it too.

### Syntax: a form that calls an action

```jsx
<form action={markPacked}>
  <input type="hidden" name="orderId" value="order-17" />
  <button type="submit">Mark packed</button>
</form>
```

`action` takes the function itself, not a URL. A **hidden input** is a field the customer never sees. It's how the form says which order it's about.

This form even works before the page's JavaScript has loaded, because underneath it's a real HTML form that posts to the server.

### What happens when Raju presses the button

```animate
title: A form calls a server action
kind: scene
nodes:
  - { id: browser, label: Raju's browser, emoji: "💻", value: "order-17: NEW" }
  - { id: next, label: Next.js server, emoji: "🖥️" }
  - { id: action, label: markPacked, emoji: "⚡" }
  - { id: data, label: Orders data, emoji: "📒", value: "order-17: NEW" }
steps:
  - say: Raju presses Mark packed on order-17. The browser sends the form to the server as a POST.
    send: { from: browser, to: next, label: "POST orderId=order-17" }
  - say: Next.js knows which action the form belongs to, and calls markPacked with the form's FormData.
    send: { from: next, to: action, label: formData }
  - say: The action checks the order exists and is NEW, then changes it.
    send: { from: action, to: data, label: "set PACKED" }
    set: { data: "order-17: PACKED" }
  - say: It calls revalidatePath("/orders"), so the orders page is built again with the new data.
    focus: [next]
  - say: The fresh page goes back to the browser in the same answer. Raju sees PACKED without reloading.
    send: { from: next, to: browser, label: "updated /orders", back: true }
    set: { browser: "order-17: PACKED" }
```

### Syntax: reading FormData

```js
const orderId = formData.get("orderId");
```

`get(name)` gives the value of the field with that `name`, as text. A field that isn't there gives `null`. FormData is a standard web object, so Node has it too:

```js run
const form = new FormData();
form.append("orderId", "order-17");
form.append("note", "gift wrap");
console.log(form.get("orderId"));
console.log(form.get("missing"));
console.log([...form.keys()].join(", "));
```

### Refresh the page after a change

The orders page is built on the server. After an action changes an order, Next.js doesn't know the page is out of date. You tell it, with the function from topic 2:

```js
import { revalidatePath } from "next/cache";

revalidatePath("/orders");
```

Call it after the change, never before. Then the answer to Raju's click carries the new page.

### Return a result instead of throwing

When something is wrong, like an order that's already shipped, return a small object that says so:

```js
function checkCanPack(orderId, order) {
  if (order.status !== "NEW") return { error: `${orderId} is already ${order.status}.` };
  return null;
}
```

A plain object can travel back to the browser, so a form can show the message. In the next topic, you'll show it with `useActionState`. Returned values must be plain data: strings, numbers, arrays and objects.

### Syntax: passing extra arguments with bind

A Cancel button doesn't need a hidden input. You can fix the first argument in advance with `bind`:

```jsx
<form action={cancelOrder.bind(null, order.id)}>
  <button type="submit">Cancel</button>
</form>
```

`cancelOrder.bind(null, "order-17")` makes a new function where the first argument is already `"order-17"`. The FormData comes after it.

```js run
async function cancelOrder(orderId, formData) {
  return `${orderId} cancelled (the form had ${[...formData.keys()].length} fields)`;
}

const cancel17 = cancelOrder.bind(null, "order-17");
cancel17(new FormData()).then(console.log);
```

`null` is the `this` value, which actions don't use.

### An action is a public door

This is the most important rule about actions. Each one becomes an endpoint that anyone can POST to, with any values they like. The button might only show for NEW orders, but a mischief-maker can still send `orderId=order-18`, or `orderId=anything`.

So every action checks its input, every time:

- Does the order exist?
- Is this change allowed from its current status?
- Later, in topic 7: is this person allowed to do it at all?

```callout warning
Never trust that a button was hidden. The server action is the real gate, so the checks go inside it.
```

## Worked example

Raju's Mark packed action, with its checks.

```js
"use server";

import { revalidatePath } from "next/cache";
import { getOrder, setStatus } from "../../lib/orders.js";

export async function markPacked(formData) {
  const orderId = formData.get("orderId");
  const order = getOrder(orderId);
  if (!order) return { error: `No order ${orderId}.` };
  if (order.status !== "NEW") return { error: `${orderId} is already ${order.status}.` };

  setStatus(orderId, "PACKED");
  revalidatePath("/orders");
  return { ok: true };
}
```

- The id comes from the form's hidden `orderId` field.
- Two checks return early with a message. Nothing changes if either fails.
- Only after the change does `revalidatePath` refresh the orders page.
- `{ ok: true }` tells the caller it worked.

## Try it

Once the checks pass, imagine someone sends `orderId=order-18` by hand. order-18 is already PACKED. Read your action line by line and find the line that stops them. Then remove that check and run the checks again: the "won't pack an order twice" check fails. Put it back.

## Recap

- A server action is an async function in a `"use server"` file. It runs on the server, but forms in the browser can call it.
- Give it to a form with `action={markPacked}`. It receives the form's `FormData`, and `formData.get(name)` reads a field.
- After changing data, call `revalidatePath` so the page shows the change.
- Return plain objects like `{ error }` or `{ ok: true }`. Use `bind` to pass an id as the first argument.
- Every action is a public endpoint, so it checks its input every time.

## Common mistakes

**1. Forgetting "use server".**

```js
export async function markPacked(formData) {
  setStatus(formData.get("orderId"), "PACKED");
}
```

When a server component passes this function to a form, Next.js stops with an error saying functions can't be passed to the browser unless they're server actions.

Fix: put `"use server";` as the first line of `actions.js`.

**2. Trusting the button.**

```js
export async function markPacked(formData) {
  setStatus(formData.get("orderId"), "PACKED");
  revalidatePath("/orders");
}
```

It works in the demo. But a request with `orderId=order-99` crashes on an order that doesn't exist, and a DELIVERED order can be sent back to PACKED.

Fix: look the order up, and return an error unless it exists and is NEW.

**3. Changing data without revalidating.**

```js
export async function markPacked(formData) {
  const orderId = formData.get("orderId");
  // ...the checks...
  setStatus(orderId, "PACKED");
  return { ok: true };
}
```

The data changes, but Raju's page still says NEW until he reloads. He presses the button again and gets "already PACKED".

Fix: call `revalidatePath("/orders")` after the change.

**4. Mixing up the order of bound arguments.**

```js
export async function cancelOrder(formData, orderId) {
  // ...
}
```

The page uses `cancelOrder.bind(null, order.id)`, so the id arrives first. Here `formData` holds the id and `orderId` holds the FormData, and every lookup says "No order [object FormData]."

Fix: bound arguments come first: `cancelOrder(orderId, formData)`.

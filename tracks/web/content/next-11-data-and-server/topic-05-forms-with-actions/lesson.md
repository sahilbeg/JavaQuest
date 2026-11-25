## Hook

At the Aundh shop, Raju reads every order slip before he files it. If the phone number is missing a digit, he doesn't tear the slip up. He hands it back, points at the gap, and says "just this bit, please". Everything else you wrote is still there.

A good web form does the same. In topic 4, your actions returned `{ error }`, but nothing showed it. Now you'll send messages back to the right fields, keep what the customer typed, show that the form is busy, and move to a thank-you page when it works.

## Concept

### Check on the server, always

In react-09 you checked forms in the browser. That's still nice, because the customer sees mistakes at once. But browser checks can be skipped: a form can be sent by a script, or with JavaScript turned off. The server action is the real gate.

```callout jargon
**Validation** means checking that input follows the rules before you use it. **Server-side validation** is validation done by the server, where nobody can skip it.
```

So the action checks every field and, when something is wrong, returns the problems instead of saving anything.

### Syntax: useActionState

```jsx
const [state, formAction, isPending] = useActionState(action, initialState);
```

`useActionState` is a React hook for forms that call actions. It goes in a client component.

- `state` is what the action returned last time, or `initialState` before the first try.
- `formAction` is a wrapped action. Put it on the form: `<form action={formAction}>`.
- `isPending` is `true` while the action is running.

The wrapped action calls yours with **two** arguments: the previous state, then the FormData.

```js
export async function placeOrder(prevState, formData) {
  // check, then return the new state
}
```

### A form's round trip

```animate
title: A wrong phone number comes back with a message
kind: scene
nodes:
  - { id: form, label: OrderForm, emoji: "📝", value: "phone: 98765 4321" }
  - { id: action, label: placeOrder, emoji: "⚡" }
  - { id: data, label: Orders, emoji: "📒", value: "no new order" }
steps:
  - say: The customer presses Place order. isPending becomes true, and the button says Placing order...
    set: { form: "Placing order..." }
  - say: The form's fields travel to the server action, with the previous state first.
    send: { from: form, to: action, label: "(prevState, formData)" }
  - say: The phone has only 9 digits. The action saves nothing and returns the errors and what was typed.
    send: { from: action, to: form, label: "{ errors: { phone }, values }", back: true }
  - say: That object becomes the new state. The form shows the message under Phone and refills the fields.
    set: { form: "Phone needs 10 digits" }
  - say: The customer fixes the number and tries again. This time every check passes.
    send: { from: form, to: action, label: "(prevState, formData)" }
  - say: The action creates the order, then redirects to its page.
    send: { from: action, to: data, label: "createOrder" }
    set: { data: "order-20 NEW", form: "→ /order/order-20" }
```

### Checking one field

Each rule is a small check. Here is the phone rule, step by step:

```animate
title: Is this a 10-digit phone number?
kind: code
language: js
code: |
  const phone = "98765 4321";
  const digits = phone.replace(/\s/g, "");
  const ok = /^\d{10}$/.test(digits);
  console.log(digits.length, ok);
steps:
  - line: 1
    vars: { phone: '"98765 4321"' }
    say: The customer typed this, with a space, like people write numbers in India.
  - line: 2
    vars: { digits: '"987654321"' }
    say: replace removes every space, so only the digits are left.
  - line: 3
    vars: { ok: "false" }
    say: The pattern wants exactly 10 digits, start to end. There are only 9.
  - line: 4
    out: "9 false"
    say: So the action will return a phone error instead of saving the order.
```

### Syntax: returning field errors

```js
const errors = {};
if (name.trim() === "") errors.name = "Please enter your name.";
if (!/^\d{10}$/.test(digits)) errors.phone = "Phone needs 10 digits, like 98765 43210.";
```

Collect every problem in one object, keyed by the field's name. Then, if there are any, return them together with what was typed:

```js
function result(errors, values) {
  if (Object.keys(errors).length > 0) return { errors, values };
  return null;
}
```

The customer sees every mistake at once, not one per try.

### Showing the errors and keeping the values

```jsx
<label htmlFor="phone">Phone</label>
<input id="phone" name="phone" defaultValue={state.values.phone} />
{state.errors.phone && <p role="alert">{state.errors.phone}</p>}
```

- `defaultValue` fills the field from the state, so a mistake doesn't wipe what was typed. After an action, React resets the form's fields to their defaults, so this matters.
- `role="alert"` makes screen readers read the message out, as in react-09.
- Each field's `name` must match the name the action reads.

### Syntax: redirect after success

```js
import { redirect } from "next/navigation";

redirect("/order/order-20");
```

When the order is saved, send the customer to its page. `redirect` doesn't return. It **throws** a special error that Next.js catches and turns into a move to the new page.

That's why `redirect` must never sit inside a `try` block. The `catch` would swallow it:

```js run
function redirect(to) {
  throw new Error("NEXT_REDIRECT " + to);
}

function placeOrderWrong() {
  try {
    redirect("/order/order-20");
  } catch (e) {
    return { errors: { form: "Something went wrong." } };
  }
}

console.log(placeOrderWrong());
```

The order was saved, but the customer sees "Something went wrong." Call `redirect` last, outside any `try`.

### The busy button

While the action runs, `isPending` is `true`. Use it to stop double taps and to say what's happening:

```jsx
<button type="submit" disabled={isPending}>
  {isPending ? "Placing order..." : "Place order"}
</button>
```

```callout tip
A button deep inside a form can read the same thing with `useFormStatus()` from `react-dom`. It returns `{ pending }` for the form it sits in, so a shared SubmitButton component doesn't need props.
```

### It works before JavaScript loads

A form whose `action` is a server action is a real HTML form. On a slow phone, the customer can submit it before React has loaded at all. The server checks it and answers with a full page.

```callout jargon
**Progressive enhancement** means building the page so the basic job works with plain HTML, and JavaScript only makes it nicer. Here, the busy button and instant messages are the "nicer" part.
```

## Worked example

The order action. `createOrder` and `findItem` come from the data files.

```js
"use server";

import { redirect } from "next/navigation";
import { findItem } from "../../lib/catalogue.js";
import { createOrder } from "../../lib/orders.js";

export async function placeOrder(prevState, formData) {
  const values = {
    name: String(formData.get("name") ?? ""),
    phone: String(formData.get("phone") ?? ""),
    itemId: String(formData.get("itemId") ?? ""),
    qty: String(formData.get("qty") ?? ""),
  };

  const errors = {};
  if (values.name.trim() === "") errors.name = "Please enter your name.";
  const digits = values.phone.replace(/\s/g, "");
  if (!/^\d{10}$/.test(digits)) errors.phone = "Phone needs 10 digits, like 98765 43210.";
  if (!findItem(values.itemId)) errors.itemId = "Please choose an item.";
  const qty = Number(values.qty);
  if (!Number.isInteger(qty) || qty < 1 || qty > 5) errors.qty = "Choose between 1 and 5.";
  if (Object.keys(errors).length > 0) return { errors, values };

  const order = createOrder({ name: values.name.trim(), phone: digits, itemId: values.itemId, qty });
  redirect(`/order/${order.id}`);
}
```

- Every value is read as text first, so the form can be refilled exactly as typed.
- All four rules run, and all the problems go back together.
- What is saved is cleaned: the name trimmed, the phone without spaces, the quantity as a number.
- `redirect` comes last, outside any `try`.

## Try it

When the checks pass, read your form as a screen reader would. Tab to Phone: its label says "Phone". Type a short number and press Place order: the message is read out because of `role="alert"`. Now remove `disabled={isPending}` and run the checks: the busy-button check fails. Put it back.

## Recap

- Validate in the server action every time. Browser checks are a nice extra, not the gate.
- `useActionState(action, initialState)` gives `[state, formAction, isPending]`. The action receives `(prevState, formData)`.
- Return `{ errors, values }` with one message per field, and show each under its field with `defaultValue` keeping what was typed.
- On success, save and then `redirect`, which throws, so it never goes inside `try`.
- Use `isPending` to disable the button and say it's busy. The form still works before JavaScript loads.

## Common mistakes

**1. Reading FormData from the first argument.**

```js
export async function placeOrder(formData) {
  const name = formData.get("name");
}
```

With `useActionState`, the first argument is the previous state. The page crashes with `TypeError: formData.get is not a function`.

Fix: `placeOrder(prevState, formData)`.

**2. Wrapping redirect in try/catch.**

```js
export async function placeOrder(prevState, formData) {
  // ...the checks...
  try {
    const order = createOrder(details);
    redirect(`/order/${order.id}`);
  } catch {
    return { errors: { form: "Something went wrong." } };
  }
}
```

The order is saved, but the customer sees "Something went wrong." and orders again.

Fix: keep only the risky call inside `try`, and call `redirect` after the `try` block.

**3. Forgetting to send the values back.**

```js
function checked(errors) {
  if (Object.keys(errors).length > 0) return { errors };
  return null;
}
```

The message shows, but every field is now empty. The customer must type her name and address again, for one wrong digit.

Fix: return `{ errors, values }` and use `defaultValue={state.values.name}` on each field.

**4. A button that can be tapped again.**

```jsx
<button type="submit">Place order</button>
```

On slow 3G nothing seems to happen, so customers tap three times and make three orders.

Fix: `disabled={isPending}`, and change the text to "Placing order..." while it runs.

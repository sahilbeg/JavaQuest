## Hook

At the stall, Raju never takes money for a WhatsApp order until he has checked it. Is there a name? Does the phone number have ten digits? Is the PIN code real? A wrong number means a parcel that never arrives.

You built Dhaaga's order form in the HTML module. It looks right, but it accepts anything. This ticket makes it read what people type, check it, and say clearly what to fix.

## Concept

### Syntax: reading a value

```js
const phone = document.querySelector("#phone");
const typed = phone.value;
```

- `.value` is what is in the box **right now**. Read it when you need it, not once at the start.
- It is **always a string**, even in a number box. Turn it into a number with `Number(...)` before doing maths.
- People type stray spaces. `trim()` removes them from the ends, and `replaceAll(" ", "")` removes them everywhere.

```js
"98765 43210".replaceAll(" ", "");   // "9876543210"
" 411007 ".trim();                  // "411007"
```

### Syntax: the input event

```js
box.addEventListener("input", () => {
  counter.textContent = `${box.value.length}/12`;
});
```

- `input` fires on **every** change to a text box: each letter typed, deleted or pasted.
- It is the event for live feedback, like a letter counter or a search as you type.

### input and change

Two events sound alike, but they fire at different moments.

| Event | Text box | Checkbox or select |
|---|---|---|
| `input` | after every keystroke | each time it changes |
| `change` | once, when you leave the box after changing it | each time it changes |

For text, use `input` when you want to react while people type, and `change` when you only care about the final value.

### Syntax: the submit event

```js
form.addEventListener("submit", (event) => {
  event.preventDefault();
  // read, check, then act
});
```

- `submit` fires on the `<form>` when it is sent: by the submit button, or by pressing Enter in a box.
- Listen on the **form**, not on the button. A click listener on the button misses Enter.

### Syntax: preventDefault

By itself, a submitted form sends its data to a URL and **loads a new page**. That throws away everything your JavaScript was doing.

```callout jargon
A **default action** is what the browser does on its own after an event: a link opens its page, a form loads a new page. `event.preventDefault()` cancels it, so your code decides what happens instead.
```

Call it first in the handler. Then the page stays put, and you can check the values.

```animate
title: Placing an order with a short phone number
kind: scene
nodes:
  - { id: customer, label: Customer, emoji: "🙋", row: 0, col: 0 }
  - { id: form, label: "form#order", emoji: "📝", row: 0, col: 2 }
  - { id: handler, label: Submit handler, emoji: "⚙️", row: 1, col: 2 }
  - { id: errors, label: "p.error", emoji: "⚠️", value: "(empty)", row: 1, col: 4 }
  - { id: reload, label: New page load, emoji: "🔄", value: cancelled, row: 0, col: 4 }
steps:
  - say: A customer types a 9-digit phone number and presses Enter.
    send: { from: customer, to: form, label: Enter }
  - say: The browser fires a submit event on the form, and runs your handler.
    send: { from: form, to: handler, label: "submit event" }
  - say: The first line calls preventDefault. The browser's own action, loading a new page, never happens.
    focus: [reload]
    show: [reload]
  - say: The handler reads the fields with FormData and checks each one. The phone has only 9 digits.
    focus: [handler]
    set: { handler: "1 problem" }
  - say: It writes a clear message next to the phone box, and marks the box as invalid.
    send: { from: handler, to: errors, label: "textContent" }
    set: { errors: "Enter a 10-digit phone number." }
  - say: No order is sent. The customer reads the message, fixes the number and submits again.
    send: { from: errors, to: customer, label: fix it, back: true }
```

### Syntax: FormData

```js
const data = new FormData(form);
const name = data.get("name");
```

- `new FormData(form)` collects every field in the form that has a `name` attribute.
- `data.get("name")` returns that field's value as a string.
- It reads fields by their `name`, not their `id`. Every input needs a `name`.

### Checking the values

Keep the checks in their own function. It takes plain values and returns an object of messages, with one key per bad field.

```js
function validate(details) {
  const errors = {};
  if (details.name.trim() === "") {
    errors.name = "Please enter your name.";
  }
  return errors;
}
```

- An empty object, `{}`, means everything is fine. `Object.keys(errors).length === 0` checks that.
- The function never touches the page, so you can test it without any HTML.
- The submit handler does the page work: read the fields, call `validate`, show the messages.

### Showing errors that everyone can find

Put each message right next to its field, in words that say how to fix it. Then mark the field:

```html
<input id="phone" name="phone" aria-invalid="true" aria-describedby="phone-error">
<p class="error" id="phone-error" data-error-for="phone">Enter a 10-digit phone number.</p>
```

```callout jargon
**aria-invalid** is an attribute that tells screen readers a field's value is wrong. **aria-describedby** points to the element whose text describes the field, so the message is read out with it.
```

On every submit, set each field's message: the error, or `""` when that field is now fine. Otherwise an old message stays after the customer fixes the mistake.

### The browser's own checks

HTML has its own checks too: `required`, `minlength`, `pattern`. They are handy, but their messages can't be styled, and a form with `novalidate` turns them off so your messages show instead.

```callout warning
Checks in the browser are a kindness to the customer, not a lock. Anyone can skip them. The server must check every order again before it trusts it.
```

## Worked example

Offices order coasters in bulk for Diwali gifts. The bulk form takes a quantity and an email address.

```js
const form = document.querySelector("#bulk");

function checkBulk(details) {
  const errors = {};
  const qty = Number(details.qty);
  if (!Number.isInteger(qty) || qty < 10 || qty > 200) {
    errors.qty = "Bulk orders are 10 to 200 pieces.";
  }
  if (!details.email.includes("@")) {
    errors.email = "Enter an email address.";
  }
  return errors;
}

form.addEventListener("submit", (event) => {
  event.preventDefault();
  const data = new FormData(form);
  const details = { qty: data.get("qty"), email: data.get("email").trim() };
  const errors = checkBulk(details);

  for (const field of ["qty", "email"]) {
    form.querySelector(`[data-error-for="${field}"]`).textContent = errors[field] ?? "";
    form.querySelector(`[name="${field}"]`).setAttribute("aria-invalid", String(field in errors));
  }

  if (Object.keys(errors).length === 0) {
    console.log("Sending bulk enquiry", details);
  }
});
```

1. `preventDefault` comes first, so the page never reloads.
2. `FormData` reads both fields by name. The quantity is still a string, so `checkBulk` converts it.
3. The loop updates every field: a message or `""`, and `aria-invalid` as `"true"` or `"false"`.
4. `errors[field] ?? ""` gives the message, or an empty string when there isn't one.
5. Only an empty `errors` object lets the enquiry go.

## Try it

The checking part is plain JavaScript. Run it, then make the email check also refuse an address that ends with `@`.

```js run
function checkBulk(details) {
  const errors = {};
  const qty = Number(details.qty);
  if (!Number.isInteger(qty) || qty < 10 || qty > 200) {
    errors.qty = "Bulk orders are 10 to 200 pieces.";
  }
  if (!details.email.includes("@")) {
    errors.email = "Enter an email address.";
  }
  return errors;
}

console.log(JSON.stringify(checkBulk({ qty: "50", email: "office@example.com" })));
console.log(JSON.stringify(checkBulk({ qty: "5", email: "office" })));
```

It prints `{}` first: no problems. The second line names both problems.

## Recap

- `.value` is what a box holds right now, always as a string. Trim it, and convert numbers.
- `input` fires on every keystroke. `change` fires when a value is committed.
- Listen for `submit` on the form, and call `event.preventDefault()` first so the page doesn't reload.
- `new FormData(form).get("name")` reads a field by its `name`.
- Check values in a plain function that returns messages. Show each next to its field with `aria-invalid`, and clear it once fixed.

## Common mistakes

**1. Listening for click on the submit button.**

```js
submitButton.addEventListener("click", placeOrder);
```

Pressing Enter in a box submits the form without a click, so the checks are skipped. Listen for `submit` on the form.

**2. Forgetting preventDefault.**

```js
form.addEventListener("submit", () => {
  showErrors();
});
```

The messages flash for a moment, then the page reloads and everything typed is gone. Take the event and call `event.preventDefault()` first.

**3. Comparing a value with a number.**

```js
if (qtyBox.value === 10) {
  applyBulkPrice();
}
```

This is never true, because `.value` is the string `"10"`. Convert first: `Number(qtyBox.value) === 10`.

**4. Leaving old messages behind.**

```js
if (errors.phone) {
  phoneError.textContent = errors.phone;
}
```

The customer fixes the number, but "Enter a 10-digit phone number." stays on screen. Always set the message, to `errors.phone ?? ""`.

**5. Reading the value once, at the start.**

```js
const name = nameBox.value;
form.addEventListener("submit", (event) => {
  event.preventDefault();
  console.log(name);   // always ""
});
```

The box was empty when the page loaded, and `name` never changes. Read `.value` (or `FormData`) inside the handler.

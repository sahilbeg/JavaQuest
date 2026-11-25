## Hook

A good order slip checks itself. It has a box for the phone number with ten little squares, so you can't write nine digits by mistake. If you leave the name empty, the person at the counter points at that exact box and says "this one, please".

The custom-order form should work like that slip. It checks its own boxes, points at the wrong one, and only hands over an order once it is right. And it should be one tag you can drop on any page: `<custom-order-form>`.

## Concept

### A form that owns its markup and its rules

The form has three jobs:
1. Draw its fields.
2. Check them when the customer presses Send, and show what's wrong.
3. Hand a clean order to the page.

A custom element can hold all three. The page only writes `<custom-order-form>` and listens for the result.

### Keep the form in the light DOM

This component draws its form as normal children, **without** a shadow root. Forms work best that way:
- Labels, autofill and password managers expect fields in the page.
- A field inside a shadow root doesn't join a `<form>` outside it.
- The page's styles for forms still apply, so it looks like every other form on the site.

```callout tip
Browsers also let a custom element act as a field itself, with `ElementInternals` and `static formAssociated = true`. That's useful for widgets like a colour picker inside someone else's form. A whole form, like this one, doesn't need it.
```

### Syntax: drawing once

```js
class CustomOrderForm extends HTMLElement {
  connectedCallback() {
    if (this.querySelector("form") !== null) {
      return;
    }
    this.innerHTML = formHtml(`order-${nextId++}`);
    this.querySelector("form").addEventListener("submit", (event) => this.#onSubmit(event));
  }

  #onSubmit(event) {
    event.preventDefault();
  }
}
```

- `connectedCallback` runs again every time the element is moved. Without the check, a move would redraw the form and wipe what the customer typed.
- The markup is fixed text we wrote ourselves, so `innerHTML` is safe here. Customer text still goes in with `textContent`.
- `nextId` gives each form its own id prefix, like `order-1`. Ids must be unique on a page, and a component may appear twice.

### Syntax: the submit event

```js
form.addEventListener("submit", (event) => {
  event.preventDefault();
});
```

- Pressing Send, or Enter in a field, fires `submit` on the form.
- By default the browser then sends the form to the server and **loads a new page**. That's the reload Raju saw.
- `event.preventDefault()` stops that, so your code decides what happens.

```callout jargon
An event's **default action** is what the browser does on its own after your listeners run, like following a link or sending a form. `preventDefault()` cancels it.
```

Add `novalidate` to the `<form>` tag too. It turns off the browser's own pop-up checks, so your messages are the only ones the customer sees.

### Reading the values

```js
const values = {
  name: form.elements.namedItem("name").value,
  phone: form.elements.namedItem("phone").value,
  qty: form.elements.namedItem("qty").value,
};
```

`form.elements` holds every field of the form, found by its `name` attribute. Every `.value` is a **string**, even for `type="number"`, so `qty` arrives as `"2"`.

```callout tip
`Object.fromEntries(new FormData(form))` reads every named field at once. It gives the same strings.
```

### Syntax: a validation function

```js
function validateOrder(values) {
  const errors = {};
  if (values.name.trim() === "") {
    errors.name = "Please enter your name.";
  }
  return errors;
}
```

- It takes the values and returns an **errors object**: one message per wrong field. An empty object means everything is fine.
- It's plain JavaScript, with no DOM, so it's easy to test and reuse on the server later.
- `Object.keys(errors).length === 0` asks "any errors?".

### Showing errors that everyone can find

Each field has its own error paragraph, linked to it:

```html
<input id="order-1-phone" name="phone" type="tel" aria-describedby="order-1-phone-error">
<p id="order-1-phone-error" class="error"></p>
```

When the phone is wrong:
- Put the message in the error paragraph.
- Set `aria-invalid="true"` on the input. Screen readers say "invalid entry".
- `aria-describedby` makes a screen reader read the message when the field gets focus.
- Move focus to the **first** wrong field, so the customer lands right on it.

When the field is fixed, empty the message and remove `aria-invalid`.

```callout jargon
**aria-invalid** tells assistive technology that a field's value is wrong. **aria-describedby** names the element whose text describes the field, like its error message.
```

```animate
title: Send is pressed with a bad phone number
kind: scene
nodes:
  - { id: customer, label: Customer, emoji: "🙋", row: 0, col: 0 }
  - { id: form, label: "<custom-order-form>", emoji: "📝", value: idle, row: 0, col: 2 }
  - { id: check, label: validateOrder, emoji: "🔍", row: 1, col: 2 }
  - { id: phone, label: Phone field, emoji: "📞", value: "98765", row: 0, col: 4 }
  - { id: page, label: The page, emoji: "📄", row: 1, col: 4 }
steps:
  - say: The customer presses Send. The form's submit listener runs first, and calls preventDefault. No reload.
    send: { from: customer, to: form, label: submit }
    set: { form: "checking" }
  - say: It reads the three values as strings and hands them to validateOrder.
    send: { from: form, to: check, label: "{ name, phone, qty }" }
  - say: The phone has only 5 digits. validateOrder returns an errors object with one message.
    send: { from: check, to: form, label: "{ phone: ... }", back: true }
  - say: The form writes the message under the phone box, sets aria-invalid, and moves focus there.
    send: { from: form, to: phone, label: "aria-invalid, focus" }
    set: { phone: "invalid, focused" }
  - say: The customer fixes it and presses Send again. No errors this time.
    send: { from: customer, to: form, label: submit }
    set: { phone: "98765 43210", form: "sending" }
  - say: The form sends an order-request event up to the page, with clean values, and empties itself.
    send: { from: form, to: page, label: "order-request" }
    set: { form: idle }
```

### Handing the result up

The form doesn't know where orders go: WhatsApp, the API, or a test. It sends an event, like the picker did:

```js
this.dispatchEvent(new CustomEvent("order-request", {
  detail: { name: "Mrs. Kulkarni", phone: "9876543210", qty: 2 },
  bubbles: true,
}));
```

Send **clean** values: the name trimmed, the phone as digits only, the quantity as a number. Every listener then gets the same tidy shape. Then `form.reset()` empties the fields for the next order.

## Worked example

A small form checks whether Dhaaga delivers to a PIN code. Pune PIN codes start with `411`.

```js
function checkPin(pin) {
  const clean = pin.trim();
  if (!/^\d{6}$/.test(clean)) {
    return "A PIN code has 6 digits.";
  }
  return clean.startsWith("411") ? "" : "We deliver only inside Pune for now.";
}

class PinCheck extends HTMLElement {
  connectedCallback() {
    if (this.querySelector("form") !== null) {
      return;
    }
    this.innerHTML = `
      <form novalidate>
        <label for="pin">PIN code</label>
        <input id="pin" name="pin" inputmode="numeric" aria-describedby="pin-error">
        <p id="pin-error" class="error"></p>
        <button type="submit">Check</button>
      </form>`;
    const form = this.querySelector("form");
    form.addEventListener("submit", (event) => {
      event.preventDefault();
      const input = form.elements.namedItem("pin");
      const message = checkPin(input.value);
      this.querySelector("#pin-error").textContent = message;
      if (message === "") {
        input.removeAttribute("aria-invalid");
        this.dispatchEvent(new CustomEvent("pin-ok", { detail: { pin: input.value.trim() }, bubbles: true }));
      } else {
        input.setAttribute("aria-invalid", "true");
        input.focus();
      }
    });
  }
}
```

1. `checkPin` is the validation function: it returns a message, or `""` when the PIN is fine.
2. The form is drawn once, guarded, in the light DOM.
3. `preventDefault` stops the reload. The value is read from `form.elements`.
4. A wrong PIN gets its message, `aria-invalid` and focus. A good one clears them and sends `pin-ok` up to the page.

## Try it

The validation function runs anywhere, so you can try it here. Run it, then make a quantity of 25 pass by changing the limit to 30.

```js run
function validateOrder(values) {
  const errors = {};
  if (values.name.trim() === "") {
    errors.name = "Please enter your name.";
  }
  if (!/^\d{10}$/.test(values.phone.replace(/\s/g, ""))) {
    errors.phone = "Please enter a 10-digit phone number.";
  }
  const qty = Number(values.qty);
  if (!Number.isInteger(qty) || qty < 1 || qty > 20) {
    errors.qty = "Choose between 1 and 20 pieces.";
  }
  return errors;
}

console.log(Object.keys(validateOrder({ name: "Mrs. Kulkarni", phone: "98765 43210", qty: "2" })));
console.log(Object.keys(validateOrder({ name: "  ", phone: "98765", qty: "25" })));
```

It prints `[]` for the good order, and `[ 'name', 'phone', 'qty' ]` for the bad one.

## Recap

- A form component owns its fields, its checks and its messages. Keep its form in the **light DOM**, drawn **once**.
- Listen for `submit` and call `preventDefault()` first, or the browser reloads the page.
- Read values from `form.elements`. They are all strings.
- A **validation function** turns values into an errors object; empty means fine.
- Show each error in its own paragraph, with `aria-invalid` and `aria-describedby`, and focus the first wrong field.
- Send clean values up in a CustomEvent, then `reset()` the form.

## Common mistakes

**1. Forgetting preventDefault.**

```js
form.addEventListener("submit", () => {
  const errors = validateOrder(readValues(form));
  showErrors(errors);
});
```

The messages flash for a moment, then the page reloads and everything is gone. Call `event.preventDefault()` at the top of the handler.

**2. Treating qty as a number without converting it.**

```js
if (values.qty < 1 || values.qty > 20) {
  errors.qty = "Choose between 1 and 20 pieces.";
}
```

An empty box gives `""`, and `"" < 1` is `true` by luck, but `"2.5"` slips through as a valid quantity. Convert with `Number`, then check `Number.isInteger`.

**3. Showing the error but not marking the field.**

```js
errorText.textContent = errors.phone;
```

Sighted customers see red text. A screen reader user hears nothing, because the field itself doesn't say it's wrong. Set `aria-invalid="true"` and link the message with `aria-describedby`.

**4. Leaving old errors behind.**

```js
if (errors.phone) {
  phone.setAttribute("aria-invalid", "true");
}
```

After the customer fixes the phone, it still says "invalid". Every check must set **or** clear each field: remove `aria-invalid` and empty the message when there is no error.

**5. Drawing the form in every connectedCallback.**

```js
class CustomOrderForm extends HTMLElement {
  connectedCallback() {
    this.innerHTML = formHtml("order");
  }
}
```

Moving the form on the page, for example into a pop-up, wipes what the customer typed. Draw only when there is no form yet.

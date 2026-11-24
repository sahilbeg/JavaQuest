## Hook

You don't stand at the front door all day, waiting for a guest. You go about your day, and the doorbell tells you when someone arrives.

Web pages work the same way. Your code doesn't keep checking "was the button tapped yet?". It tells the browser: "when this button is clicked, run this function". Then it waits for the bell. Until now, Dhaaga's buttons had no bell at all.

## Concept

### What an event is

```callout jargon
An **event** is a message from the browser that something happened: a click, a key press, typing in a box, a form being sent.
```

```callout jargon
A **listener** (or **handler**) is a function you give the browser to run when a certain event happens on a certain element.
```

Every event has a **type**, like `"click"`, and a **target**, the element it happened to. You pick an element and a type, and hand over a function.

### Syntax: addEventListener

```js
element.addEventListener("type", handler);
```

- `"type"` is the event name, as a string: `"click"`, `"input"`, `"keydown"`.
- `handler` is a function. The browser calls it every time that event happens on that element.
- You can add several listeners to one element. They run in the order you added them.

```js
const plus = document.querySelector(".plus");
plus.addEventListener("click", () => {
  console.log("plus was clicked");
});
```

### Pass the function, don't call it

`addEventListener` wants the function itself, to keep for later. So you write its name with **no brackets**:

```js
plus.addEventListener("click", addOne);    // right: hands over the function
plus.addEventListener("click", addOne());  // wrong: runs addOne now, hands over its result
```

The second line runs `addOne` once, while the page loads, and gives the browser `undefined`. Clicks then do nothing, and there is no error.

Node has the same event system, so you can watch a listener wait for its event. `EventTarget` is the part of every element that handles events:

```animate
title: A listener waits for its event
kind: code
language: js
code: |
  const button = new EventTarget();
  let qty = 1;
  button.addEventListener("click", () => {
    qty = qty + 1;
    console.log(`qty is ${qty}`);
  });
  console.log("ready");
  button.dispatchEvent(new Event("click"));
  button.dispatchEvent(new Event("click"));
steps:
  - line: 1
    vars: { button: "EventTarget" }
    say: A stand-in for a button. EventTarget is the part of an element that handles events.
  - line: 2
    vars: { qty: 1 }
    say: The quantity starts at 1.
  - line: 3
    say: addEventListener stores the arrow function for later. It does not run it now.
  - line: 7
    out: "ready"
    say: The code carries on. The handler still hasn't run, and qty is still 1.
  - line: 8
    say: A click arrives. In a browser a tap does this; here dispatchEvent fakes one. The stored handler runs.
  - line: 4
    vars: { qty: 2 }
    say: The handler changes qty.
  - line: 5
    out: "qty is 2"
    say: Then it prints the new value.
  - line: 9
    say: A second click runs the very same handler again.
  - line: 4
    vars: { qty: 3 }
    say: qty was 2, so now it is 3. The variable outlives each click.
  - line: 5
    out: "qty is 3"
    say: One listener, added once, answers every click.
```

### The event object

The browser passes your handler one argument: an **event object** with the details.

```js
plus.addEventListener("click", (event) => {
  console.log(event.type);           // "click"
  console.log(event.target);         // the element that was clicked
  console.log(event.currentTarget);  // the element the listener is on
});
```

- `event.target` is where the event started. If the button holds an icon, a click may land on the icon.
- `event.currentTarget` is always the element you called `addEventListener` on.
- Keyboard events add `event.key`, the key that was pressed.

The next ticket uses the gap between `target` and `currentTarget` for a powerful trick.

```animate
title: A tap on the + button
kind: scene
nodes:
  - { id: finger, label: Customer, emoji: "👆", row: 0, col: 0 }
  - { id: browser, label: Browser, emoji: "🌐", row: 0, col: 2 }
  - { id: plus, label: "button.plus", emoji: "➕", row: 1, col: 2 }
  - { id: handler, label: Your handler, emoji: "⚙️", value: "qty 1", row: 1, col: 4 }
  - { id: qty, label: "span.qty", emoji: "🔢", value: "1", row: 0, col: 4 }
steps:
  - say: Earlier, your code added a click listener to the + button. Now it waits.
    focus: [plus, handler]
  - say: Mr. Fernandes taps +. The browser notices the tap on the button.
    send: { from: finger, to: browser, label: tap }
  - say: It builds an event object, type click, target the + button.
    send: { from: browser, to: plus, label: "click event" }
  - say: The button has a click listener, so the browser calls it and passes the event object in.
    send: { from: plus, to: handler, label: "handler(event)" }
    set: { handler: "qty 2" }
  - say: The handler changes the quantity, then writes it into the page.
    send: { from: handler, to: qty, label: "textContent = '2'" }
    set: { qty: "2" }
  - say: The handler ends. The browser goes back to waiting for the next event.
    focus: [browser]
```

### Common event types

| Event | When it happens | Typical element |
|---|---|---|
| `click` | a tap, a mouse click, or Enter or Space on a button | buttons, links |
| `input` | every change to a text box, as you type | `<input>`, `<textarea>` |
| `change` | a value is committed: a box is ticked, an option picked | checkboxes, `<select>` |
| `submit` | a form is sent | `<form>` |
| `keydown` | a key goes down | anything that has focus |
| `focus` / `blur` | an element gains or loses focus | inputs, buttons |

Forms and `input` get their own ticket soon.

### Syntax: keyboard events

```js
input.addEventListener("keydown", (event) => {
  if (event.key === "Escape") {
    input.value = "";
  }
});
```

- `event.key` is the key's name: `"Enter"`, `"Escape"`, `"ArrowUp"`, or a letter like `"a"`.
- The listener goes on the element that has focus, or on `document` to hear every key.

### State lives in a variable

The quantity has to be remembered between clicks. Keep it in a `let` variable that the handlers can reach.

```js
function setupCounter(button, output) {
  let count = 0;
  button.addEventListener("click", () => {
    count += 1;
    output.textContent = String(count);
  });
}
```

```callout jargon
**State** is the data your page has to remember while it runs, like the quantity a customer picked.
```

This is a **closure**, from the last module. The handler keeps `count` alive after `setupCounter` returns. Call `setupCounter` twice, for two buttons, and each gets its own `count`.

```callout tip
Read values **inside** the handler, at click time. A value read during setup is frozen at what it was then.
```

### Use real buttons

A `<div>` with a click listener works with a mouse, and with nothing else. A `<button>` also clicks with Enter and Space, can be reached with Tab, and is announced as a button by screen readers.

```html
<div class="plus">+</div>                                       <!-- mouse only -->
<button class="plus" type="button" aria-label="One more">+</button>  <!-- everyone -->
```

`type="button"` matters inside a form. Without it, a button submits the form.

### Removing a listener

```js
plus.removeEventListener("click", addOne);
plus.addEventListener("click", celebrate, { once: true });
```

- `removeEventListener` needs the **same function** you added. A fresh arrow function, even an identical-looking one, is a different function.
- `{ once: true }` removes the listener by itself after its first run.

## Worked example

Each product page has a short description and a "Read more" button that shows the long one.

```html
<p class="long" hidden>Golu is crocheted from soft Indigo cotton and stuffed by hand...</p>
<button class="more" type="button" aria-expanded="false">Read more</button>
```

```js
function setupReadMore(button, long) {
  let open = false;

  button.addEventListener("click", () => {
    open = !open;
    long.hidden = !open;
    button.textContent = open ? "Read less" : "Read more";
    button.setAttribute("aria-expanded", String(open));
  });
}

setupReadMore(document.querySelector(".more"), document.querySelector(".long"));
```

1. `open` is the state. It starts `false`, matching the `hidden` text.
2. The arrow function is handed to `addEventListener`, not called.
3. Each click flips `open`, then makes the page match it: the paragraph, the button text and `aria-expanded`.
4. `aria-expanded` tells a screen reader whether the extra text is showing. It must be the string `"true"` or `"false"`.

## Try it

Node has the same `EventTarget` that elements use. Run this, then try adding `{ once: true }` as a third argument to `addEventListener`.

```js run
const bell = new EventTarget();

function ring(event) {
  console.log(`Ding! A ${event.type} event`);
}

bell.addEventListener("press", ring);
console.log("Listener added. Nothing has happened yet.");

bell.dispatchEvent(new Event("press"));
bell.dispatchEvent(new Event("press"));
```

Then change `ring` to `ring()` in `addEventListener`, and see what breaks.

## Recap

- An **event** is the browser saying something happened. A **listener** is the function you give it to run.
- `element.addEventListener("click", handler)` passes the function itself, with no brackets.
- The handler gets an event object: `type`, `target`, `currentTarget`, and `key` for keyboard events.
- Keep state in a `let` variable the handlers share, and read values inside the handler, at event time.
- Use `<button type="button">` for anything clickable, so keyboards and screen readers work too.

## Common mistakes

**1. Calling the handler instead of passing it.**

```js
plus.addEventListener("click", addOne());
```

`addOne` runs once when the page loads. After that, clicks do nothing, and there is no error. Remove the brackets: `addEventListener("click", addOne)`.

**2. Reading a value too early.**

```js
const qty = readQty();
button.addEventListener("click", () => {
  status.textContent = `Added ${qty} to your cart`;
});
```

It always says "Added 1", the quantity when the page loaded. Call `readQty()` inside the handler.

**3. Using onclick and losing a handler.**

```js
plus.onclick = addOne;
plus.onclick = showMessage;
```

Only `showMessage` runs. `onclick` holds one function, so the second line replaces the first. `addEventListener` keeps both.

**4. Making a div clickable.**

```html
<div class="plus">+</div>
```

It works with a mouse only. Keyboard users can't reach it, and screen readers don't call it a button. Use `<button type="button">`.

**5. Removing with a new function.**

```js
plus.addEventListener("click", () => addOne());
plus.removeEventListener("click", () => addOne());
```

The listener stays. Each arrow function is a new, different function. Keep the function in a variable, and pass that same variable to both.

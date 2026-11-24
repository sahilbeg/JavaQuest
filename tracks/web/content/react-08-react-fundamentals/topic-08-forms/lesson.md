## Hook

At the stall, orders are written on a pad. When a customer changes her mind from two coaster sets to three, the line is crossed out and rewritten, then everything is added up again at the end. Sometimes the sum is wrong.

Now picture a pad that adds up as you write, and always knows exactly what is on it. In React, a form can be that pad, because every field lives in state.

## Concept

### Controlled inputs: state holds the value

An HTML input keeps its own text. React would rather keep it in state, so the rest of the component can use it, like a total that updates as you type.

```callout jargon
A **controlled input** is a form field whose value comes from React state, and whose changes go back into that state. State is then the one true copy of what the customer typed.
```

### Syntax: a controlled text input

```jsx
const [name, setName] = useState("");

<input id="name" type="text" value={name} onChange={(event) => setName(event.target.value)} />
```

- `value={name}` makes the box show what state holds.
- `onChange` runs on every key press. It reads the new text from `event.target.value` and stores it.

In React, `onChange` fires on every change, not only when the box loses focus, as it does in plain HTML.

Watch one letter make the full loop:

```animate
title: One letter, one loop
kind: scene
nodes:
  - { id: key, label: "Keyboard", emoji: "⌨️" }
  - { id: input, label: "Name box", emoji: "📝", value: "(empty)" }
  - { id: handler, label: "onChange", emoji: "🪝" }
  - { id: state, label: "name state", emoji: "🧠", value: "\"\"" }
steps:
  - say: The name box shows whatever the name state holds. Right now that is empty text.
    focus: [input, state]
  - say: The customer types A.
    send: { from: key, to: input, label: "A" }
  - say: React calls onChange with the event. event.target.value is now "A".
    send: { from: input, to: handler, label: "event.target.value" }
  - say: The handler stores "A" in state. That asks React to re-render.
    send: { from: handler, to: state, label: "setName(\"A\")" }
    set: { state: "\"A\"" }
  - say: The new render passes value="A" back to the box. State and box agree again.
    send: { from: state, to: input, label: "value=\"A\"" }
    set: { input: "A" }
  - say: Every key press takes this loop. Because state always has the latest text, the rest of the form can use it at any moment.
    focus: [state]
```

### Every input value is text

`event.target.value` is always a string, even from a number box or a quantity dropdown. That sneaks up on you:

```animate
title: 'The text "2" is not the number 2'
kind: code
language: js
code: |
  const typed = "2";
  const price = 15000;
  console.log(typed + 1);
  console.log(price * typed);
  const qty = Number(typed);
  console.log(qty + 1);
steps:
  - line: 1
    vars: { typed: '"2"' }
    say: This is what a quantity dropdown gives you. It is text, with quotes.
  - line: 2
    vars: { price: 15000 }
    say: The coaster set's price, in paise.
  - line: 3
    out: "21"
    say: + with text joins instead of adding. One more than "2" becomes "21".
  - line: 4
    out: "30000"
    say: '* turns text into a number by itself, so this looks fine. That is why the bug can hide for a while.'
  - line: 5
    vars: { qty: 2 }
    say: Number() turns the text into a real number. Do this as soon as the value comes out of the input.
  - line: 6
    out: "3"
    say: Now + adds. Store qty as a number, and every later sum is safe.
```

### Syntax: a select

```jsx
<select id="item" value={code} onChange={(event) => setCode(event.target.value)}>
  <option value="CST">Coaster set (4)</option>
  <option value="BAG">Tote bag</option>
</select>
```

A select works like a text input. `value` picks the shown option, and `event.target.value` is the chosen option's `value`. You can make the options with `map` and keys, like any list.

### Syntax: a checkbox

```jsx
<input id="gift-wrap" type="checkbox" checked={giftWrap} onChange={(event) => setGiftWrap(event.target.checked)} />
```

A checkbox is on or off, so it uses `checked` instead of `value`, and `event.target.checked`, which is `true` or `false`.

### Syntax: a label

```jsx
<label htmlFor="phone">Phone</label>
<input id="phone" type="tel" value={phone} onChange={(event) => setPhone(event.target.value)} />
```

Join each label to its field with `htmlFor` and the same `id`. Screen readers then read "Phone" for the box, and tapping the label puts the cursor in it.

### Syntax: sending the form

```jsx
function handleSubmit(event) {
  event.preventDefault();
  onSubmit({ name, phone });
}

<form onSubmit={handleSubmit}>
  ...
  <button type="submit">Place order</button>
</form>
```

Put `onSubmit` on the `<form>`, not `onClick` on the button. Then both a tap on the button and Enter in a box send the form.

A browser sends a form by loading a new page. `event.preventDefault()` stops that, so your code handles the order and the page stays as it is.

### Work things out from state

```jsx
const totalPaise = price * qty + (giftWrap ? 3000 : 0);
```

The total is not stored in state. It is worked out from state on every render, so it can never disagree with the fields. If a value can be calculated from what you already have, calculate it.

```callout jargon
A **derived value** is one you work out from state or props during the render, like a total. You do not keep it in state of its own.
```

## Worked example

A name can be stitched on any item for ₹50, up to 12 letters. This small form shows how many letters are left and sends the name.

```jsx
import { useState } from "react";

const MAX_LETTERS = 12;

export function StitchForm({ onAdd }) {
  const [text, setText] = useState("");
  const left = MAX_LETTERS - text.length;

  function handleSubmit(event) {
    event.preventDefault();
    onAdd(text.trim());
    setText("");
  }

  return (
    <form onSubmit={handleSubmit}>
      <label htmlFor="stitch">Name to stitch (₹50)</label>
      <input
        id="stitch"
        type="text"
        maxLength={MAX_LETTERS}
        value={text}
        onChange={(event) => setText(event.target.value)}
      />
      <p>{left} letters left</p>
      <button type="submit" disabled={text.trim() === ""}>Add name</button>
    </form>
  );
}
```

Line by line:

- `text` is the one piece of state. `left` is derived from it on every render.
- The input is controlled: `value={text}` and an `onChange` that stores the new text.
- `maxLength` is a normal HTML attribute, in camelCase. It stops typing after 12 letters.
- `handleSubmit` stops the page reload, sends the trimmed name, then clears the box by setting the state back to `""`.
- The button is disabled while the box holds only spaces.

## Try it

Run this, then change the last line to use `Number(fromInput) * 2` and `fromInput * 2`. Which operators turn text into numbers by themselves, and which do not?

```js run
const fromInput = "2";
console.log(fromInput + 1);
console.log(Number(fromInput) + 1);
console.log(typeof fromInput, typeof Number(fromInput));
```

It prints `21`, `3`, and `string number`. Convert once, where the value leaves the input, and you never have to think about it again.

## Recap

- A controlled input takes `value` from state and stores changes with `onChange`. A checkbox uses `checked` and `event.target.checked`.
- `event.target.value` is always text. Use `Number(...)` for quantities and prices.
- Join each `<label>` to its field with `htmlFor` and `id`.
- Handle `onSubmit` on the `<form>`, and call `event.preventDefault()` first.
- Work out totals from state during the render instead of storing them.

## Common mistakes

**1. A value with no onChange.**

```jsx
<input id="name" type="text" value={name} />
```

The box is frozen: typing does nothing. The console warns that you gave a `value` without an `onChange` handler. Add `onChange={(event) => setName(event.target.value)}`.

**2. Forgetting preventDefault.**

```jsx
function handleSubmit() {
  onSubmit({ name, phone });
}
```

The page reloads the moment the customer taps Place order, and everything they typed vanishes. Take the event and call `event.preventDefault()` first.

**3. Using value for a checkbox.**

```jsx
<input type="checkbox" value={giftWrap} onChange={(event) => setGiftWrap(event.target.value)} />
```

Gift wrap is always on after the first tap, because `event.target.value` of a checkbox is the text `"on"`, and any non-empty text is truthy. Use `checked={giftWrap}` and `event.target.checked`.

**4. Keeping the quantity as text.**

```jsx
<select id="qty" value={qty} onChange={(event) => setQty(event.target.value)}>
```

The total still looks right, because `*` converts text. But the order goes out with `qty: "3"`, and a later `qty + 1` gives `"31"`. Store `Number(event.target.value)`.

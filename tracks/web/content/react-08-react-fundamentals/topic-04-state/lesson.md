## Hook

At the stall, the person at the counter counts coaster sets on their fingers while a customer decides. "Three? Four? Okay, three." If they forgot the number every time the customer spoke, the sale would never finish.

A React component forgets everything each time React calls it. To remember a number between calls, it needs a place outside itself to keep it. That place is state.

## Concept

### Why a plain variable is not enough

Here is the quantity picker from the ticket, in a simpler form:

```jsx
function QuantityPicker() {
  let qty = 1;
  return (
    <button type="button" onClick={() => { qty = qty + 1; }}>
      {qty}
    </button>
  );
}
```

Tapping the button does change `qty` to 2. But two things go wrong:

- React does not know anything changed, so it never re-renders. The page still shows 1.
- Even if React did re-render, the function would start again from the top, and `let qty = 1` would run again.

A component's local variables last for one call only. To remember something across renders, React must hold it for you.

### Syntax: useState

```jsx
import { useState } from "react";

const [qty, setQty] = useState(1);
```

`useState(1)` asks React for one remembered value that starts at 1. It gives back an array of two things, which you unpack:

- `qty` is the value for this render.
- `setQty` is a function that changes it and tells React to re-render.

```callout jargon
A **hook** is a React function whose name starts with `use`, like `useState`. Hooks let a component use React features, such as remembered state.
```

```callout jargon
A **setter** is the function `useState` gives you to change the state, like `setQty`. Name it `set` plus the state's name.
```

### What happens when you call the setter

```jsx
<button type="button" onClick={() => setQty(qty + 1)}>+</button>
```

Watch one tap of the + button:

```animate
title: One tap of the + button
kind: scene
nodes:
  - { id: btn, label: "+ button", emoji: "👆" }
  - { id: memory, label: "React's memory", emoji: "🧠", value: "qty: 1" }
  - { id: comp, label: "QuantityPicker()", emoji: "🧩" }
  - { id: page, label: "Page", emoji: "🖥️", value: "1 · ₹150.00" }
steps:
  - say: React keeps qty in its own memory, next to this picker on the page. Right now it holds 1.
    focus: [memory]
  - say: The customer taps +. The click handler calls setQty(2). React stores 2 and plans a re-render.
    send: { from: btn, to: memory, label: "setQty(2)" }
    set: { memory: "qty: 2" }
  - say: React calls QuantityPicker again. The function starts from the top, as every call does.
    send: { from: memory, to: comp, label: "re-render" }
  - say: useState(1) runs again. The 1 is only the starting value, so React ignores it this time.
    send: { from: comp, to: memory, label: "useState(1)" }
  - say: React hands back what it remembered. In this render, qty is 2.
    send: { from: memory, to: comp, label: "[2, setQty]", back: true }
  - say: The new JSX shows 2 and ₹300.00. React updates just those two bits of the page.
    send: { from: comp, to: page, label: "<span>2</span>" }
    set: { page: "2 · ₹300.00" }
```

So calling a setter does two jobs at once. It stores the new value, and it asks React to draw the component again.

### Each render is a snapshot

Inside one render, `qty` never changes. It is a constant for that call, like a photo taken at that moment.

```jsx
function handlePlus() {
  setQty(qty + 1);
  console.log(qty); // still the old value
}
```

The log shows the old number. `setQty` does not change the `qty` you already have. It asks for a new render, and the next render gets a new `qty`.

```callout jargon
A **snapshot** is a value frozen at one moment. Each render sees a snapshot of the state, and calling the setter changes only what the next render sees.
```

### Syntax: the updater function

```jsx
setQty((q) => q + 1);
```

Instead of a value, you can pass a function. React calls it with the newest state and uses what it returns. This matters when you set the same state twice before the next render:

```jsx
function handleAddPair() {
  setQty((q) => q + 1);
  setQty((q) => q + 1);
}
```

Two calls of `setQty(qty + 1)` both use the same snapshot, so together they add only 1. Two updater calls add 2, because each one gets the result of the one before. The Try it section shows this happening.

```callout tip
When the new state depends on the old state, the updater form is never wrong. Many teams use it for every counter.
```

### State belongs to each copy

Put two pickers on the page, and each one remembers its own `qty`:

```jsx
<QuantityPicker name="Golu the elephant" paise={45000} stock={3} />
<QuantityPicker name="Marigold coaster set" paise={15000} stock={10} />
```

Tapping + on Golu's picker changes only Golu's count. React keeps a separate memory for every place a component appears on the page.

### Rules for hooks

- Call `useState` at the top of your component, never inside an `if`, a loop or a nested function.
- Call it only inside a component (or inside another hook).

React matches each `useState` call to its memory by the order of the calls. An `if` could skip one and mix them up.

### State or props?

| | Props | State |
|---|---|---|
| Who sets it | The parent | The component itself, with its setter |
| Can it change? | Only when the parent passes a new value | Yes, by calling the setter |
| Example | the price of a product | how many the customer picked |

## Worked example

The heart button on a product saves it to the customer's wishlist. It needs to remember one thing: saved or not.

```jsx
import { useState } from "react";

export function SaveButton({ name }) {
  const [saved, setSaved] = useState(false);

  return (
    <button
      type="button"
      aria-pressed={saved}
      onClick={() => setSaved(!saved)}
    >
      {saved ? `♥ Saved ${name}` : `♡ Save ${name}`}
    </button>
  );
}
```

Line by line:

- `useState(false)` starts the button as not saved.
- `onClick={() => setSaved(!saved)}` flips the value. `!saved` turns `false` into `true` and back.
- `aria-pressed={saved}` tells screen readers whether the button is on.
- The text uses the `? :` operator to pick one of two labels. Each tap calls the setter, React re-renders, and the label flips.

## Try it

This little model shows how React handles setter calls. Each call goes into a queue, and the queue is worked through before the next render. Run it.

```js run
let stored = 1;
let queue = [];

function setQty(next) {
  queue.push(next);
}

function reRender() {
  for (const next of queue) {
    stored = typeof next === "function" ? next(stored) : next;
  }
  queue = [];
  console.log(`qty is now ${stored}`);
}

// Two calls that both use this render's snapshot
const qty = stored;
setQty(qty + 1);
setQty(qty + 1);
reRender();

// Two calls with updater functions
setQty((q) => q + 1);
setQty((q) => q + 1);
reRender();
```

It prints `qty is now 2`, then `qty is now 4`. The first pair both said "make it 2". The updaters said "add one to whatever it is". Now add a third `setQty((q) => q + 1);` before the last `reRender()` and predict the output.

## Recap

- A component's local variables are forgotten after each render. State is remembered by React between renders.
- `const [qty, setQty] = useState(1);` gives the current value and a setter. The 1 is used only the first time.
- Calling the setter stores the new value and makes React re-render the component.
- Each render sees a snapshot: `qty` does not change until the next render. Use `setQty((q) => q + 1)` when the new value depends on the old.
- Every copy of a component on the page has its own state.

## Common mistakes

**1. Using a plain variable for something that changes.**

```jsx
let qty = 1;
<button type="button" onClick={() => { qty = qty + 1; }}>+</button>
```

The button seems dead: the page never changes. React was not told about the change, and the next render starts at 1 again anyway. Use `const [qty, setQty] = useState(1);` and call `setQty(qty + 1)`.

**2. Assigning to the state variable.**

```jsx
const [qty, setQty] = useState(1);
<button type="button" onClick={() => { qty = qty + 1; }}>+</button>
```

The first tap throws `TypeError: Assignment to constant variable.` State only changes through its setter. Write `onClick={() => setQty(qty + 1)}`.

**3. Calling the setter instead of passing a function.**

```jsx
<button type="button" onClick={setQty(qty + 1)}>+</button>
```

React stops with an error about too many re-renders. `setQty(qty + 1)` runs while drawing, which causes another render, which runs it again, forever. Wrap it in an arrow function: `onClick={() => setQty(qty + 1)}`.

**4. Reading the new value right after setting it.**

```jsx
setQty(qty + 1);
alert(`You picked ${qty}`);
```

The alert shows the old number, because `qty` is this render's snapshot. Work out the new value first: `const next = qty + 1; setQty(next);` and use `next`.

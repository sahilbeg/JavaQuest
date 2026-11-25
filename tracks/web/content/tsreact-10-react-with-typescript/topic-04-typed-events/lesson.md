## Hook

At the stall, Raju listens for many things at once. A customer says "two keychains". Someone taps the counter bell. The UPI speaker announces a payment. Each one is a different kind of event, and each needs a different response.

A page is the same. Clicks, typing and key presses are all events. React lets a component answer each one, and TypeScript tells you exactly what each event carries.

## Concept

### Syntax: an event prop

```tsx
<button type="button" onClick={() => setQty(qty + 1)}>+</button>
```

In web-05 you called `addEventListener`. In React you pass a function in a prop instead. Event props are camelCase: `onClick`, `onChange`, `onKeyDown`, `onSubmit`.

```callout jargon
An **event handler** is a function React calls when an event happens, like a click. You pass it to an event prop such as `onClick`.
```

Pass a function, don't call one. `onClick={addToBag}` hands React the function. `onClick={addToBag()}` calls it while drawing, which is never what you want.

### Inline handlers are typed for you

```tsx
<input onChange={(e) => setText(e.target.value)} />
```

You never wrote a type for `e`, yet TypeScript knows it. The `onChange` prop of an `<input>` expects a handler for input change events. So the arrow function's parameter gets that type from its place.

```callout jargon
**Contextual typing** is TypeScript working out a parameter's type from where the function is used, like a handler passed to `onChange`.
```

Hover over `e` in an editor and it shows `ChangeEvent<HTMLInputElement>`. That is why `e.target.value` is known to be a `string`.

### Syntax: typing a separate handler

```tsx
import type { ChangeEvent } from "react";

function handleChange(e: ChangeEvent<HTMLInputElement>) {
  setText(e.target.value);
}
```

When a handler is its own function, nothing tells TypeScript where it will be used. You write the event type yourself. Leave it out, and strict mode stops with `Parameter 'e' implicitly has an 'any' type.`

### The event types you will use

| Event prop | Event type | Useful parts |
|---|---|---|
| `onClick` | `MouseEvent<HTMLButtonElement>` | `e.currentTarget`, `e.shiftKey` |
| `onChange` on an input | `ChangeEvent<HTMLInputElement>` | `e.target.value`, `e.target.checked` |
| `onChange` on a select | `ChangeEvent<HTMLSelectElement>` | `e.target.value` |
| `onKeyDown` | `KeyboardEvent<HTMLInputElement>` | `e.key`, like `"Enter"` |
| `onSubmit` | `SubmitEvent<HTMLFormElement>` | `e.preventDefault()` |

They all come from React: `import type { ChangeEvent, KeyboardEvent } from "react";`. The part in angle brackets is the element the handler is attached to.

```callout warning
`MouseEvent` and `KeyboardEvent` also exist in the browser, with different shapes. Import React's versions, or write `React.MouseEvent`, so the right one is used.
```

### Syntax: keyboard events

```tsx
import type { KeyboardEvent } from "react";

function handleKeyDown(e: KeyboardEvent<HTMLInputElement>) {
  if (e.key === "Enter") {
    search();
  }
}
```

`e.key` is the key's name as text: `"Enter"`, `"Escape"`, `"a"`, `"A"`. Compare it with `===`.

### Watch a key press travel

```animate
title: Raju presses Enter
kind: scene
nodes:
  - { id: key, label: "Keyboard", emoji: "⌨️" }
  - { id: input, label: "Code box", emoji: "🔤", value: "AMI" }
  - { id: handler, label: "handleKeyDown(e)", emoji: "🧩" }
  - { id: parent, label: "onAdd prop", emoji: "🧾" }
  - { id: page, label: "Message", emoji: "💬", value: "" }
steps:
  - say: Raju has typed AMI in the code box. Now he presses Enter.
    send: { from: key, to: input, label: "Enter" }
  - say: React calls handleKeyDown with a KeyboardEvent. TypeScript knows it has a key property.
    send: { from: input, to: handler, label: "e.key: \"Enter\"" }
  - say: The key is Enter, so the handler checks the code. AMI is a real item code.
    focus: [handler]
  - say: The handler calls the onAdd prop, so the parent can add one amigurumi to the bill.
    send: { from: handler, to: parent, label: "onAdd(\"AMI\")" }
  - say: The handler also sets new state. React re-renders, the box empties, and the message appears.
    send: { from: handler, to: page, label: "setMessage(...)" }
    set: { page: "Added Amigurumi", input: "" }
```

### Syntax: callback props

```tsx
interface QuickAddProps {
  onAdd: (code: ItemCode) => void;
}
```

A component can raise its own events. It takes a function in a prop and calls it when something happens. The parent decides what to do.

The prop's type is a function type. `(code: ItemCode) => void` means: "a function that takes one item code and returns nothing useful". Now `tsc` checks both sides. The child must call it with an `ItemCode`, and the parent must pass a function that accepts one.

```callout jargon
A **callback prop** is a function passed to a component so it can tell its parent that something happened, like `onAdd`. Name them `on` plus what happened.
```

## Worked example

A small search box for the catalogue. It reports each search to its parent, and Escape clears it.

```tsx
import { useState } from "react";
import type { ChangeEvent, KeyboardEvent } from "react";

interface SearchBoxProps {
  onSearch: (text: string) => void;
}

export function SearchBox({ onSearch }: SearchBoxProps) {
  const [text, setText] = useState("");

  function handleChange(e: ChangeEvent<HTMLInputElement>) {
    setText(e.target.value);
  }

  function handleKeyDown(e: KeyboardEvent<HTMLInputElement>) {
    if (e.key === "Enter") {
      onSearch(text.trim());
    } else if (e.key === "Escape") {
      setText("");
    }
  }

  return (
    <div className="search-box">
      <label htmlFor="search">Search</label>
      <input id="search" value={text} onChange={handleChange} onKeyDown={handleKeyDown} />
      <button type="button" onClick={() => onSearch(text.trim())}>Search</button>
    </div>
  );
}
```

Line by line:

- `onSearch: (text: string) => void` is the callback prop. The parent passes what to do with a search.
- `handleChange` and `handleKeyDown` are separate functions, so their event types are written out.
- `value={text}` with `onChange` keeps the box and the state in step. The next ticket covers this in depth.
- The button's handler is inline, so it needs no type. It calls the same callback.

## Try it

A union of key names and a lookup make a small, typed key handler. Run it, then add `"Tab"` to the union and to `ACTIONS`.

```ts run
type Key = "Enter" | "Escape";

const ACTIONS: Record<Key, string> = {
  Enter: "add the item",
  Escape: "clear the box",
};

function onKey(key: string): string {
  if (key === "Enter" || key === "Escape") {
    return ACTIONS[key];
  }
  return "ignore";
}

console.log(onKey("Enter"));
console.log(onKey("Escape"));
console.log(onKey("a"));
```

It prints `add the item`, `clear the box`, then `ignore`. The `if` narrows `key` from `string` to `Key`, so the lookup is allowed.

## Recap

- Event props take a function: `onClick={handleClick}`, never `onClick={handleClick()}`.
- Inline handlers get their event type from the prop. Separate handlers need it written out.
- React's event types name the element: `ChangeEvent<HTMLInputElement>`, `KeyboardEvent<HTMLInputElement>`, `MouseEvent<HTMLButtonElement>`.
- `e.target.value` is a string, `e.target.checked` a boolean, and `e.key` the name of a key.
- Callback props like `onAdd: (code: ItemCode) => void` let a child tell its parent what happened, checked on both sides.

## Common mistakes

**1. Calling the handler instead of passing it.**

```tsx
<button type="button" onClick={addTyped()}>Add</button>
```

`addTyped()` runs while the page is drawn, not on click, and hands `onClick` its result instead of a function. `tsc` catches it: `Type 'void' is not assignable to type 'MouseEventHandler<HTMLButtonElement> | undefined'.` In plain JavaScript, a handler like this that sets state makes React stop with an error about too many re-renders. Write `onClick={addTyped}` or `onClick={() => addTyped()}`.

**2. Leaving a separate handler's event untyped.**

```tsx
function handleChange(e) {
  setText(e.target.value);
}
```

`tsc` stops with `Parameter 'e' implicitly has an 'any' type.` Write `e: ChangeEvent<HTMLInputElement>`.

**3. Using the browser's KeyboardEvent.**

```tsx
function handleKeyDown(e: KeyboardEvent) {
  if (e.key === "Enter") addTyped();
}
```

Without an import, `KeyboardEvent` is the browser's type, and `onKeyDown={handleKeyDown}` fails to type-check. Add `import type { KeyboardEvent } from "react";` and write `KeyboardEvent<HTMLInputElement>`.

**4. Checking the wrong key name.**

```tsx
if (e.key === "enter") addTyped();
```

Nothing happens on Enter, because key names are case-sensitive. The name is `"Enter"`, with a capital E.

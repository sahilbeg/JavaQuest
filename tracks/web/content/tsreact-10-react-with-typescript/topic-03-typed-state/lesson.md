## Hook

At the stall, Raju keeps a sticky note on the counter. "Mint amigurumi, then Rani pink." While the customer chats, the note remembers the order for him.

A React component has no memory of its own. Every time React calls it, it starts again from the top. To remember a customer's choice, it needs React to keep a sticky note for it. That note is called state.

## Concept

### Why a plain variable forgets

```tsx
function ColourButton() {
  let colour = "none";
  return (
    <button type="button" onClick={() => { colour = "Mint"; }}>
      Chosen: {colour}
    </button>
  );
}
```

Tapping the button does set `colour` to `"Mint"`. But the page still says `Chosen: none`, for two reasons:

- React was not told anything changed, so it never draws the component again.
- If it did, the function would start from the top, and `let colour = "none"` would run again.

A component's local variables last for one call only.

```callout jargon
To **render** is to call a component and draw what it returns. A **re-render** is React calling it again, to update the page after something changed.
```

### Syntax: useState

```tsx
import { useState } from "react";

const [qty, setQty] = useState(1);
```

`useState(1)` asks React to remember one value, starting at 1. It returns a pair, which you unpack:

- `qty` is the value for this render.
- `setQty` stores a new value and asks React to re-render.

```callout jargon
**State** is a value React remembers for a component between renders. A **hook** is a React function whose name starts with `use`, like `useState`.
```

```callout jargon
A **setter** is the function `useState` gives you, like `setQty`. Name it `set` plus the state's name.
```

Call hooks at the top of the component, never inside an `if` or a loop. React matches each `useState` to its memory by the order of the calls.

### What happens when you set state

```animate
title: One tap on the Mint button
kind: scene
nodes:
  - { id: btn, label: "Mint button", emoji: "👆" }
  - { id: memory, label: "React's memory", emoji: "🧠", value: "colour: null" }
  - { id: comp, label: "YarnPicker()", emoji: "🧩" }
  - { id: page, label: "Page", emoji: "🖥️", value: "Pick a colour" }
steps:
  - say: React keeps colour in its memory, next to this picker. It starts as null.
    focus: [memory]
  - say: The customer taps Mint. The click handler calls setColour with Mint. React stores it and plans a re-render.
    send: { from: btn, to: memory, label: "setColour(\"Mint\")" }
    set: { memory: "colour: Mint" }
  - say: React calls YarnPicker again. The function runs from the top, as every call does.
    send: { from: memory, to: comp, label: "re-render" }
  - say: useState hands back what it remembered. In this render, colour is Mint, not null.
    send: { from: memory, to: comp, label: "[\"Mint\", setColour]", back: true }
  - say: The new JSX says Colour Mint. React changes just that text on the page.
    send: { from: comp, to: page, label: "Colour: Mint" }
    set: { page: "Colour: Mint" }
```

The starting value is used only on the first render. After that, React gives back whatever was set last.

### TypeScript infers the state type

```tsx
const [qty, setQty] = useState(1);
setQty("2");
```

```text
error TS2345: Argument of type 'string' is not assignable to parameter of type 'SetStateAction<number>'.
```

`useState` is a generic function, like the ones in ts-09. TypeScript infers its type from the starting value. `1` is a number, so `qty` is a `number`, and the setter only accepts numbers.

```callout jargon
`SetStateAction<number>` is React's name for "a number, or a function that turns the old number into a new one". You will meet the function form below.
```

### Syntax: giving state a type

```tsx
const [colour, setColour] = useState<Colour | null>(null);
```

Inference fails when the starting value is narrower than what comes later. `useState(null)` gives state whose type is only `null`:

```tsx
const [colour, setColour] = useState(null);
setColour("Mint");
```

```text
error TS2345: Argument of type '"Mint"' is not assignable to parameter of type 'SetStateAction<null>'.
```

The fix is a type argument in angle brackets. `useState<Colour | null>(null)` says: "it starts as null, but it may hold a Colour later". Then TypeScript makes you check for `null` before you use it as a colour.

### Syntax: arrays in state

```tsx
const [bag, setBag] = useState<Colour[]>([]);

setBag([...bag, "Mint"]);
```

An empty array has the same problem. `useState([])` gives `never[]`, an array that can hold nothing. Write the element type: `useState<Colour[]>([])`.

To add to an array in state, make a new array. `[...bag, "Mint"]` copies the old items and adds one.

```callout jargon
An **immutable update** makes a new value instead of changing the old one. React compares the old and new array. If you `push` into the same array, it sees no change and may not re-render.
```

### Each render is a snapshot

```tsx
setBag([...bag, colour]);
console.log(bag.length); // still the old length
```

Inside one render, `bag` never changes. The setter only changes what the next render sees.

When the new value depends on the old one, you can pass a function instead. React calls it with the newest value:

```tsx
setBag((old) => [...old, colour]);
```

TypeScript knows `old` is a `Colour[]`, because it comes from the state's type.

## Worked example

Baby booties come in three sizes. The size picker starts with nothing chosen, and remembers every size the customer tried.

```tsx
import { useState } from "react";

type Size = "0-3 months" | "3-6 months" | "6-12 months";

export function SizePicker() {
  const [size, setSize] = useState<Size | null>(null);
  const [tried, setTried] = useState<Size[]>([]);

  function choose(next: Size) {
    setSize(next);
    setTried([...tried, next]);
  }

  return (
    <div className="size-picker">
      <button type="button" onClick={() => choose("0-3 months")}>0-3 months</button>
      <button type="button" onClick={() => choose("3-6 months")}>3-6 months</button>
      <p>{size === null ? "Choose a size" : `Size: ${size}`}</p>
      <p>You tried {tried.length} sizes</p>
    </div>
  );
}
```

Line by line:

- `useState<Size | null>(null)` starts with no size, but may hold any `Size` later.
- `useState<Size[]>([])` starts with an empty list of sizes.
- `choose` calls both setters. React re-renders once, with both new values.
- `size === null ? ... : ...` checks for `null` first. In the second branch, TypeScript knows `size` is a `Size`.

## Try it

This model gives you a typed `get` and `set`, like a tiny `useState`. Run it, then add `setBag([...bag(), "Marigold"]);` before the last line and predict the output.

```ts run
function makeState<S>(initial: S) {
  let stored = initial;
  const get = (): S => stored;
  const set = (next: S): void => {
    stored = next;
  };
  return [get, set] as const;
}

type Colour = "Marigold" | "Indigo" | "Mint";

const [colour, setColour] = makeState<Colour | null>(null);
console.log(colour());
setColour("Mint");
console.log(colour());

const [bag, setBag] = makeState<Colour[]>([]);
setBag([...bag(), "Mint"]);
setBag([...bag(), "Indigo"]);
console.log(bag().join(", "));
```

It prints `null`, then `Mint`, then `Mint, Indigo`. Without the `<Colour | null>`, `tsc` would refuse `setColour("Mint")`, because the state could only ever hold `null`. (Running a snippet strips the types without checking them, so only `tsc` catches it.)

## Recap

- A component forgets its local variables after each render. State is remembered by React between renders.
- `const [qty, setQty] = useState(1);` gives the value and a setter. Calling the setter stores the value and re-renders.
- TypeScript infers the state type from the starting value. `useState(1)` is a number.
- When the start is narrower than later values, give a type: `useState<Colour | null>(null)`, `useState<Colour[]>([])`.
- Update arrays by making new ones, like `[...bag, colour]`, never with `push`.

## Common mistakes

**1. Starting with null and no type.**

```tsx
const [colour, setColour] = useState(null);
setColour("Mint");
```

`tsc` says `Argument of type '"Mint"' is not assignable to parameter of type 'SetStateAction<null>'.` Write `useState<Colour | null>(null)`.

**2. Pushing into an array in state.**

```tsx
bag.push(colour);
setBag(bag);
```

The bag on the page may not change. It is the same array, so React sees nothing new. Write `setBag([...bag, colour])`.

**3. Using a value that might be null.**

```tsx
const [colour, setColour] = useState<Colour | null>(null);
setBag([...bag, colour]);
```

`tsc` reports that `Colour | null` is not assignable to `Colour`. Check first: `if (colour !== null) setBag([...bag, colour]);`.

**4. Assigning to the state variable.**

```tsx
const [qty, setQty] = useState(1);
qty = qty + 1;
```

`tsc` says `Cannot assign to 'qty' because it is a constant.` State only changes through its setter: `setQty(qty + 1)`.

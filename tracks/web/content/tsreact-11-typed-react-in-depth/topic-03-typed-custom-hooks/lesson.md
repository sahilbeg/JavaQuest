## Hook

Meera Aunty has one pattern for the amigurumi elephant. She doesn't rewrite it for every order. She keeps the pattern once, and each new elephant is made from it, with its own yarn.

Code works the same way. Four components each have their own copy of "remember this in localStorage", and each copy has a different bug. This ticket turns that copy into one pattern: a custom hook. TypeScript makes sure every component uses it the right way.

## Concept

### A custom hook is a function that uses hooks

You already use hooks that React gives you: `useState` and `useEffect`. You can also write your own.

```callout jargon
A **custom hook** is your own function whose name starts with `use` and which calls other hooks inside. It packs up some stateful logic, so many components can reuse it.
```

A custom hook is not a component. It returns values, not JSX. A component calls it at the top, like `useState`.

### Syntax: a custom hook

```tsx
function useSomething(input: Input): Output {
  const [value, setValue] = useState<Value>(start);
  useEffect(() => { /* ... */ }, [input]);
  return /* what the component needs */;
}
```

Here is a tiny one that toggles a yes/no value, like "Show saved items":

```tsx
import { useState } from "react";

export function useToggle(initial: boolean = false): readonly [boolean, () => void] {
  const [on, setOn] = useState<boolean>(initial);
  const toggle = () => setOn((v) => !v);
  return [on, toggle] as const;
}
```

```tsx
const [showSaved, toggleSaved] = useToggle();
return <button onClick={toggleSaved}>{showSaved ? "Hide saved" : "Show saved"}</button>;
```

### Hooks share logic, not state

Two components that call `useToggle` don't share one boolean. Each call makes its own `useState`, so each component gets its own value. It's one pattern, two elephants, two lots of yarn.

```animate
title: Two components, one hook, two separate states
kind: scene
nodes:
  - { id: hook, label: useToggle, emoji: "🧶" }
  - { id: wish, label: WishList, emoji: "❤️", value: "off" }
  - { id: guide, label: SizeGuide, emoji: "📏", value: "off" }
steps:
  - say: WishList calls useToggle. Inside, useState makes a boolean that belongs to WishList.
    send: { from: hook, to: wish, label: "[false, toggle]" }
  - say: SizeGuide calls the same hook. It gets a brand-new boolean of its own.
    send: { from: hook, to: guide, label: "[false, toggle]" }
  - say: A customer taps Show saved. Only WishList's toggle runs.
    set: { wish: "on" }
    focus: [wish]
  - say: SizeGuide is still off. The hook shared the code, not the value.
    focus: [guide]
```

To share one value between components, you need context. That's the next ticket.

### The rules of hooks

React finds each hook's state by its **order** in the component: the first `useState`, the second, and so on. So every render must call the same hooks in the same order.

- Call hooks at the top of a component or a custom hook.
- Never inside an `if`, a loop, or after an early `return`.
- Name custom hooks `useSomething`. That's how React's lint tools know to check these rules inside them.

```tsx
if (loggedIn) {
  const [saved, setSaved] = useStoredState<string[]>("dhaaga-wishlist", []);
}
```

```text
React Hook "useStoredState" is called conditionally. React Hooks must be called in the exact same order in every component render.
```

That message comes from ESLint's `react-hooks` rules, which most React projects turn on.

### Typing what a hook returns

A hook that returns two things can return a **tuple** (an array with a fixed length and a type per place) or an **object**.

Tuples let each component pick its own names: `const [showSaved, toggleSaved] = useToggle();`. But TypeScript doesn't infer a tuple from an array literal. Without help, `[on, toggle]` becomes "an array of booleans or functions":

```tsx
function useToggle() {
  const [on, setOn] = useState(false);
  return [on, () => setOn((v) => !v)];
}
const [open, toggle] = useToggle();
<button onClick={toggle}>Size guide</button>;
```

```text
error TS2322: Type 'boolean | (() => void)' is not assignable to type 'MouseEventHandler<HTMLButtonElement> | undefined'.
  Type 'boolean' is not assignable to type 'MouseEventHandler<HTMLButtonElement>'.
```

Two fixes, and both are common:

- `return [on, toggle] as const;` makes a `readonly [boolean, () => void]` tuple.
- Or write the return type yourself: `function useToggle(): readonly [boolean, () => void]`.

```callout tip
Return a tuple for one or two values, like `useState` does. For three or more, return an object such as `{ items, add, remove }`. Names stop mattering for a tuple's third place.
```

### Syntax: a generic hook

```tsx
function useStoredState<T>(key: string, initial: T): [T, (next: T) => void] {
  // ...
}
```

You wrote generic functions in the TypeScript course. A hook can be generic too. `T` is whatever the component stores: a list of ids, a colour, a number.

```tsx
const [saved, setSaved] = useStoredState<string[]>("dhaaga-wishlist", []);
setSaved([...saved, "DC-1042"]);   // fine
setSaved("DC-1042");               // error: a string is not a string[]
```

Often you don't need to write `<string[]>`. TypeScript infers `T` from `initial`. But an empty array `[]` says nothing about what goes in it, so naming `T` helps there.

### Syntax: lazy initial state

```tsx
const [value, setValue] = useState<T>(() => readSaved(key, initial));
```

`useState(readSaved(key, initial))` would read localStorage on **every** render, then throw the answer away after the first. Passing a **function** tells React to call it once, for the first render only.

```callout jargon
A **lazy initializer** is a function you give to `useState` instead of a value. React calls it only when the component first appears.
```

### Saved data can be broken

What's in localStorage may be from an old version of the site, or edited by hand. `JSON.parse` throws on broken text. So reading must cope with three cases: nothing saved, broken JSON, and good JSON.

```animate
title: Reading a saved value safely
kind: code
language: ts
code: |
  const storage = new Map<string, string>();
  storage.set("dhaaga-wishlist", '["DC-1042"]');
  function readSaved<T>(key: string, fallback: T): T {
    const saved = storage.get(key);
    if (saved === undefined) return fallback;
    try { return JSON.parse(saved) as T; } catch { return fallback; }
  }
  console.log(readSaved<string[]>("dhaaga-wishlist", []).length);
  storage.set("dhaaga-wishlist", "[oops");
  console.log(readSaved<string[]>("dhaaga-wishlist", []).length);
  console.log(readSaved<string[]>("dhaaga-cart", []).length);
steps:
  - line: 2
    say: A Map stands in for localStorage. The wishlist holds one id, saved as JSON text.
  - line: 8
    out: "1"
    say: The text parses into a list with Golu's id in it. Length 1.
  - line: 9
    say: Now the saved text is broken, as if an old bug wrote half a list.
  - line: 10
    out: "0"
    say: JSON.parse throws, the catch returns the fallback, and the page still works with an empty list.
  - line: 11
    out: "0"
    say: Nothing is saved under dhaaga-cart, so get gives undefined and we use the fallback.
```

The `as T` above trusts that good JSON has the right shape. For stricter checking, a hook can take a type guard too, like last ticket's `isYarnStock`, and fall back when the guard says no.

## Worked example

Here's a different hook: it tells a component whether the customer is online. It shows how a custom hook can hold an effect, too.

```tsx
import { useEffect, useState } from "react";

export function useOnline(): boolean {
  const [online, setOnline] = useState<boolean>(() => navigator.onLine);

  useEffect(() => {
    function update() {
      setOnline(navigator.onLine);
    }
    window.addEventListener("online", update);
    window.addEventListener("offline", update);
    return () => {
      window.removeEventListener("online", update);
      window.removeEventListener("offline", update);
    };
  }, []);

  return online;
}

export function CheckoutButton() {
  const online = useOnline();
  return <button disabled={!online}>{online ? "Pay by UPI" : "You're offline"}</button>;
}
```

- The name starts with `use`, and the hook calls `useState` and `useEffect` at its top.
- The return type is a plain `boolean`, because there's only one value.
- `useState(() => navigator.onLine)` reads the browser once, with a lazy initializer.
- The effect and its cleanup live inside the hook. `CheckoutButton` doesn't need to know about listeners at all.

## Try it

Open the preview. Tap the heart on Golu, then press **Refresh the list**, which unmounts and remounts it like a page refresh. Golu should still be saved.

Then open the browser's DevTools, Application tab, and change `dhaaga-wishlist` to `[oops`. Refresh: the list falls back to empty instead of crashing.

## Recap

- A custom hook is a `use...` function that calls other hooks and returns values for a component.
- Each call of a hook gets its own state. Hooks share logic, not values.
- Call hooks at the top level, in the same order every render: never in an `if`, a loop or after a `return`.
- Return a tuple with `as const` (or a written return type), so each place keeps its own type.
- A generic hook like `useStoredState<T>` works for any type, and a lazy initializer reads storage only once.

## Common mistakes

**1. Returning an array without as const.**

```tsx
function useToggle(initial = false) {
  const [on, setOn] = useState(initial);
  return [on, () => setOn((v) => !v)];
}
```

Using it gives `Type 'boolean | (() => void)' is not assignable to type 'MouseEventHandler<HTMLButtonElement> | undefined'`. Each place became "boolean or function".

Fix: `return [on, toggle] as const;`, or declare the return type `readonly [boolean, () => void]`.

**2. Calling a hook inside an if.**

```tsx
if (!loggedIn) return <p>Please sign in.</p>;
const [saved, setSaved] = useStoredState<string[]>("dhaaga-wishlist", []);
```

When the customer signs in, React sees one more hook than last time and throws `Rendered more hooks than during the previous render.`

Fix: call every hook first, at the top, then do the early `return`.

**3. Reading storage on every render.**

```tsx
const [value, setValue] = useState<T>(readSaved(key, initial));
```

It works, but `readSaved` runs on every render and parses JSON for nothing. With a big cart this gets slow.

Fix: pass a function, `useState<T>(() => readSaved(key, initial))`.

**4. Saving only in state.**

```tsx
function save(next: T) {
  setValue(next);
}
```

The heart fills in, but a refresh empties it again, because nothing reached localStorage.

Fix: `localStorage.setItem(key, JSON.stringify(next));` and then `setValue(next);`.

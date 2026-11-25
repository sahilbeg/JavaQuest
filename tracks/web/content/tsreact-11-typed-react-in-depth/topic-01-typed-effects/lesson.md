## Hook

A good recipe always gives the same dish. Same flour, same sugar, same cake. Rendering a React component should work the same way: same props and state in, same page out.

Some jobs reach outside the recipe, though. Sana wants Escape to close the quick view, so the page must listen to the keyboard. She wants a reminder after a few quiet seconds, so the page needs a timer. Neither is "work out what the page looks like". React has one tool for jobs like these, and TypeScript checks how you use it.

## Concept

### Rendering must stay pure

When React calls your component, it only wants an answer: the JSX to show. It may call your component many times, and at times you don't expect.

```callout jargon
A **pure** function gives the same result for the same inputs and changes nothing outside itself. A **side effect** (or just **effect**) is anything that reaches outside: listening to the keyboard, starting a timer, talking to a server, or changing the tab title.
```

If you call `window.addEventListener` while rendering, you add a new listener on every render, even renders React throws away. So effects get their own place to run: after React has updated the page.

### Syntax: useEffect

```tsx
useEffect(() => {
  // runs after React has put this render on the page
}, [dependencies]);
```

`useEffect` is a hook, like `useState`, so you call it at the top of your component. It takes two things:

- A function with the effect inside. React calls it after the page is updated.
- A **dependency array**: the values the effect reads. React runs the effect again only when one of them changed.

```tsx
import { useEffect } from "react";

export function CartTitle({ count }: { count: number }) {
  useEffect(() => {
    document.title = `(${count}) Dhaaga & Co.`;
  }, [count]);

  return <p>{count} items in your cart</p>;
}
```

```callout jargon
A **dependency** is a value from your component (a prop, a piece of state, or something made from them) that the effect reads. If it changes, the effect is out of date and must run again.
```

### When does an effect run?

React first renders, then puts the changes on the page, then runs effects. Watch it happen for the quick view.

```animate
title: An effect runs after the page updates
kind: scene
nodes:
  - { id: comp, label: QuickView, emoji: "🧶", value: "open: false" }
  - { id: page, label: The page, emoji: "📄", value: "no pop-up" }
  - { id: effect, label: The effect, emoji: "⚡" }
  - { id: win, label: window, emoji: "🪟", value: "0 listeners" }
steps:
  - say: React calls QuickView with open false. The component only returns JSX. It doesn't touch window.
    focus: [comp]
  - say: Sana taps Golu's photo. open becomes true, so React renders again.
    set: { comp: "open: true" }
  - say: React puts the pop-up on the page first.
    send: { from: comp, to: page, label: "dialog" }
    set: { page: "Golu, ₹450.00" }
  - say: Only then does React run the effect, because open is in its dependency array and it changed.
    send: { from: page, to: effect, label: "after render" }
  - say: The effect reaches outside React and starts listening for keys.
    send: { from: effect, to: win, label: "addEventListener" }
    set: { win: "1 listener" }
  - say: Sana presses Escape. The listener calls onClose, and the pop-up closes.
    send: { from: win, to: comp, label: "Escape", back: true }
    set: { comp: "open: false", page: "no pop-up" }
```

### Syntax: the three dependency arrays

```tsx
useEffect(() => { /* ... */ });            // no array: after every render
useEffect(() => { /* ... */ }, []);        // empty array: once, after the first render
useEffect(() => { /* ... */ }, [count]);   // after the first render, then when count changes
```

Most effects want the third form. "No array" is rarely right, because almost anything can cause a render.

React compares each dependency with its value from the last render, using `Object.is`. Here is that check in plain TypeScript:

```ts run
function depsChanged(before: unknown[], after: unknown[]): boolean {
  return after.some((value, i) => !Object.is(value, before[i]));
}

console.log(depsChanged(["DC-1042"], ["DC-1042"]));
console.log(depsChanged([true], [false]));
console.log(depsChanged([{ open: true }], [{ open: true }]));
```

The last line prints `true`. Two objects that look the same are still two different objects. An object made fresh in every render counts as "changed" every time.

### Syntax: cleanup

```tsx
useEffect(() => {
  const id = setInterval(tick, 1000);
  return () => clearInterval(id);
}, []);
```

An effect can return a function. That is its **cleanup**. React calls it before running the effect again, and when the component leaves the page.

```callout jargon
To **unmount** a component means React removes it from the page. **Mount** is the opposite: the first time it appears.
```

Anything you start in an effect, you stop in its cleanup: a timer, a listener, a connection. Without cleanup, a closed quick view keeps listening for Escape, and a timer keeps ticking for a component that is gone.

### What TypeScript expects from an effect

In `@types/react`, the effect function has the type `EffectCallback`. Simplified, it says this:

```ts
type Destructor = () => void;
type EffectCallback = () => void | Destructor;
```

So an effect returns **nothing**, or **a cleanup function**. Both of these are fine, even in one effect:

```tsx
useEffect(() => {
  if (!open) return;                          // nothing to clean up
  window.addEventListener("keydown", handleKey);
  return () => window.removeEventListener("keydown", handleKey);
}, [open]);
```

Anything else is a type error. That catches a real bug, as the next card shows.

### An effect can't be async

It's tempting to write `useEffect(async () => { ... })`. But an `async` function always returns a Promise, and a Promise is not a cleanup function.

```tsx
useEffect(async () => {
  const res = await fetch("/api/stock/DC-1042");
}, []);
```

```text
error TS2345: Argument of type '() => Promise<void>' is not assignable to parameter of type 'EffectCallback'.
  Type 'Promise<void>' is not assignable to type 'void | Destructor'.
```

React would have tried to call that Promise as a cleanup. TypeScript stops it before it runs. The next ticket shows the right way to wait inside an effect.

### Typed listeners

`window.addEventListener` knows its events. For `"keydown"`, TypeScript gives the listener a `KeyboardEvent`, with `key`, `shiftKey` and the rest.

```tsx
function handleKey(event: KeyboardEvent) {
  if (event.key === "Escape") onClose();
}
window.addEventListener("keydown", handleKey);
```

Inside an effect, `KeyboardEvent` is the browser's own type. It is a different type from the one React passes to `onKeyDown`, which you import from `"react"`.

To remove a listener, you must pass the **very same function** you added. A new arrow function that looks the same is a different function. Watch:

```animate
title: Only the same function can be removed
kind: code
language: ts
code: |
  const listeners = new Set<(key: string) => void>();
  const onKey = (key: string) => console.log("got " + key);
  listeners.add(onKey);
  listeners.delete((key: string) => console.log("got " + key));
  console.log(listeners.size);
  listeners.delete(onKey);
  console.log(listeners.size);
steps:
  - line: 1
    vars: { listeners: "Set(0)" }
    say: A Set stands in for the window's list of keydown listeners.
  - line: 2
    vars: { onKey: "(key) => ..." }
    say: We make one named function and keep it in onKey.
  - line: 3
    vars: { listeners: "Set(1)" }
    say: Adding it puts one listener in the list.
  - line: 4
    say: This tries to remove a brand-new arrow function. It looks the same, but it's a different object, so nothing is removed.
  - line: 5
    out: "1"
    say: The listener is still there. A quick view built like this keeps listening after it closes.
  - line: 6
    vars: { listeners: "Set(0)" }
    say: Removing onKey itself works, because it's the very same function.
  - line: 7
    out: "0"
    say: That's why the effect names its handler, then removes that name in the cleanup.
```

### Typed timer ids

`setTimeout` returns an id that you pass to `clearTimeout`. In a browser the id is a `number`. In Node it's an object. Code that is shared, or tested, is safest with a type that fits both:

```ts run
const id: ReturnType<typeof setTimeout> = setTimeout(() => console.log("never"), 1000);
clearTimeout(id);
console.log("cancelled");
```

```callout jargon
`ReturnType<typeof setTimeout>` means "whatever type setTimeout returns here". `typeof setTimeout` is the function's type, and `ReturnType` picks out what it returns.
```

### Strict Mode runs effects twice in development

Many projects wrap the app in `<StrictMode>`. In development only, React then mounts each component, unmounts it, and mounts it again. So every effect runs, cleans up, and runs again.

```callout tip
This is on purpose. If your effect breaks when it runs twice, it is missing its cleanup. A good effect plus its cleanup leaves no trace, so running it twice changes nothing.
```

## Worked example

Here's an effect for a different screen: the Aundh shop's opening sign. It ticks once a minute and listens for the page coming back into view.

```tsx
import { useEffect, useState } from "react";

export function ShopClock({ everyMs }: { everyMs: number }) {
  const [ticks, setTicks] = useState<number>(0);

  useEffect(() => {
    const id: ReturnType<typeof setInterval> = setInterval(() => {
      setTicks((t) => t + 1);
    }, everyMs);

    function handleVisible() {
      if (document.visibilityState === "visible") setTicks((t) => t + 1);
    }
    document.addEventListener("visibilitychange", handleVisible);

    return () => {
      clearInterval(id);
      document.removeEventListener("visibilitychange", handleVisible);
    };
  }, [everyMs]);

  return <p>Checked the time {ticks} times</p>;
}
```

- The effect starts two things: an interval and a listener. The cleanup stops both.
- `id` is typed with `ReturnType<typeof setInterval>`, so it works in the browser and in tests.
- `handleVisible` is a named function, so the cleanup removes the same one it added.
- `setTicks((t) => t + 1)` is the **updater form**. A timer made in the first render would otherwise keep seeing `ticks` as 0.
- `everyMs` is read inside, so it's in the dependency array. A new interval starts if it changes.

## Try it

Open the preview and press **Quick view** on Golu. Press Escape. Nothing happens yet. After your fix, it closes.

Then press **Add a keychain** and wait five seconds for the reminder. Press it again: the wait starts over. Try removing the cleanup from your timer effect and pressing quickly. The old timers are never cancelled, so each one still fires later, for a count that is already out of date.

## Recap

- Rendering should be pure. Listeners, timers and talking to servers are side effects, and they go in `useEffect`.
- React runs an effect after the page is updated, then again whenever a value in its dependency array changes.
- An effect returns nothing or a cleanup function. That is why `useEffect(async () => ...)` is a type error.
- Remove a listener with the same named function you added, in the cleanup.
- Type a timer id as `ReturnType<typeof setTimeout>`, and clear it in the cleanup.

## Common mistakes

**1. Removing a different function.**

```tsx
useEffect(() => {
  window.addEventListener("keydown", (e) => { if (e.key === "Escape") onClose(); });
  return () => window.removeEventListener("keydown", (e) => { if (e.key === "Escape") onClose(); });
}, [onClose]);
```

TypeScript is happy, but the cleanup removes nothing. Closed pop-ups keep listening, and Escape calls `onClose` once for every listener left behind.

Fix: name the handler once, add it, and remove that same name.

**2. Making the effect itself async.**

```tsx
useEffect(async () => {
  await loadStock();
}, []);
```

You see `Type 'Promise<void>' is not assignable to type 'void | Destructor'`. React would treat the Promise as a cleanup.

Fix: keep the effect function plain, and start the async work inside it, as the next ticket shows.

**3. Typing the timer id as number.**

```tsx
const id: number = setTimeout(() => setNudgedAt(count), delayMs);
```

It works in the browser. Where Node's types are loaded, as in many test setups, you see `Type 'Timeout' is not assignable to type 'number'`.

Fix: `const id: ReturnType<typeof setTimeout> = setTimeout(...)`, or let TypeScript infer it.

**4. Forgetting to stop the timer.**

```tsx
useEffect(() => {
  setTimeout(() => setNudgedAt(count), delayMs);
}, [count, delayMs]);
```

Every change to the cart starts another timer, and none are cancelled. Each old timer still fires, for an out-of-date count, and a removed reminder leaves its timer running.

Fix: keep the id and return `() => clearTimeout(id)`.

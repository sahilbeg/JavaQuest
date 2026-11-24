## Hook

A good recipe always gives the same dish. Same flour, same sugar, same cake. Rendering a React component should work the same way: same props and state in, same page out.

But some jobs reach outside the recipe. Sana wants the browser tab to say "(3) Dhaaga & Co.". The sale banner needs a ticking clock. Soon the store will ask the server for stock. None of these are "work out what the page looks like". They are things that happen in the world. React has one tool for them, and this ticket is about it.

## Concept

### Rendering must stay pure

When React calls your component, it only wants an answer: the JSX to show. It may call your component many times, and at times you don't expect.

```callout jargon
A **pure** function gives the same result for the same inputs and changes nothing outside itself. A **side effect** (or just **effect**) is anything that reaches outside: changing the tab title, starting a timer, talking to a server, or writing to localStorage.
```

If you change `document.title` in the middle of rendering, it happens on every render, even renders React throws away. So effects get their own place to run: after React has updated the page.

### Syntax: useEffect

```jsx
useEffect(() => {
  // runs after React has put this render on the page
}, [dependencies]);
```

`useEffect` is a hook, so you call it at the top of your component, like `useState`. It takes two things:

- A function with the effect inside. React calls it after the page is updated.
- A **dependency array**: the values the effect uses. React runs the effect again only when one of them changed.

```jsx
import { useEffect } from "react";

export function CartTitle({ count }) {
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

React first renders, then puts the changes on the page, then runs effects. Watch it happen for the cart title.

```animate
title: An effect runs after the page updates
kind: scene
nodes:
  - { id: comp, label: CartTitle, emoji: "🧶", value: "count 0" }
  - { id: page, label: The page, emoji: "📄", value: "0 items" }
  - { id: effect, label: The effect, emoji: "⚡" }
  - { id: tab, label: Browser tab, emoji: "🗂️", value: "Dhaaga & Co." }
steps:
  - say: "React calls CartTitle with count 0. The component only returns JSX. It does not touch the tab."
    focus: [comp]
  - say: React puts the new text on the page first.
    send: { from: comp, to: page, label: "0 items" }
  - say: Only then does React run the effect, because this is the first render.
    send: { from: page, to: effect, label: "after render" }
  - say: The effect reaches outside React and sets the tab title.
    send: { from: effect, to: tab, label: "document.title" }
    set: { tab: "(0) Dhaaga & Co." }
  - say: A customer adds a coaster set. count becomes 1, so React renders again.
    set: { comp: "count 1", page: "1 item" }
    focus: [comp, page]
  - say: count is in the dependency array and it changed, so the effect runs again.
    send: { from: effect, to: tab, label: "document.title" }
    set: { tab: "(1) Dhaaga & Co." }
```

### Syntax: the three dependency arrays

```jsx
useEffect(() => { /* ... */ });            // no array: after every render
useEffect(() => { /* ... */ }, []);        // empty array: once, after the first render
useEffect(() => { /* ... */ }, [count]);   // after the first render, then when count changes
```

Most effects want the third form. "No array" is rarely right, because almost anything can cause a render.

React compares each dependency with its value from the last render, using `Object.is`. Here is that check in plain JavaScript:

```js run
function depsChanged(before, after) {
  return after.some((value, i) => !Object.is(value, before[i]));
}

console.log(depsChanged([3], [3]));
console.log(depsChanged([3], [4]));
console.log(depsChanged([{ code: "CST" }], [{ code: "CST" }]));
```

The last line prints `true`. Two objects that look the same are still two different objects. An object made fresh in every render counts as "changed" every time.

### Syntax: cleanup

```jsx
useEffect(() => {
  const id = setInterval(tick, 1000);
  return () => clearInterval(id);
}, []);
```

An effect can return a function. That is its **cleanup**. React calls it before running the effect again, and when the component leaves the page.

```callout jargon
To **unmount** a component means React removes it from the page. **Mount** is the opposite: the first time it appears.
```

Anything you start in an effect, you stop in its cleanup: a timer, an event listener, a connection. Without cleanup, a timer keeps ticking after its banner is gone. It may even try to update a component that no longer exists.

### Timers see the values from their own render

The sale countdown starts one timer, once. So its effect has an empty array. But there's a trap. Look at this version:

```jsx
useEffect(() => {
  const id = setInterval(() => setLeft(left - 1), 1000);
  return () => clearInterval(id);
}, []);
```

The arrow function was made during the first render, when `left` was 30. It keeps that 30 forever. Every second it sets `left` to 29 again. The banner gets stuck at 29.

```callout jargon
A **closure** is a function plus the variables it could see when it was made. A **stale closure** is one that still holds old values, because it was made in an old render.
```

The fix is the **updater form** of a state setter: pass a function, and React hands it the newest value.

```jsx
setLeft((l) => l - 1);
```

```animate
title: Why the timer gets stuck, and how the updater form helps
kind: code
language: js
code: |
  function render(left) {
    return () => left - 1;
  }
  const tick = render(30);
  let shown = tick();
  shown = tick();
  console.log(shown);
  const update = (l) => l - 1;
  let state = update(30);
  state = update(state);
  console.log(state);
steps:
  - line: 4
    vars: { tick: "() => left - 1" }
    say: The first render makes the timer's function. It closes over left, which is 30 right now.
  - line: 5
    vars: { shown: 29 }
    say: The first tick works out 30 - 1. That is 29.
  - line: 6
    vars: { shown: 29 }
    say: The second tick still sees 30, the value from the first render. It gives 29 again.
  - line: 7
    out: "29"
    say: That is the stuck banner. The function never sees the new value.
  - line: 8
    vars: { update: "(l) => l - 1" }
    say: The updater form does not remember any value. It takes the current one as its argument.
  - line: 9
    vars: { state: 29 }
    say: React passes 30, and gets back 29.
  - line: 10
    vars: { state: 28 }
    say: Next time React passes 29, the newest value, and gets 28.
  - line: 11
    out: "28"
    say: The countdown keeps going, because each step starts from the latest value.
```

### Strict Mode runs effects twice in development

Many projects wrap the app in `<StrictMode>`. In development only, React then mounts each component, unmounts it, and mounts it again. So every effect runs, cleans up, and runs again.

```callout tip
This is on purpose. If your effect breaks when it runs twice, it is missing its cleanup. A good effect plus its cleanup leaves no trace, so running it twice changes nothing.
```

### You might not need an effect

Effects are for reaching outside React. They are not for working out values. This is wasteful:

```jsx
const [total, setTotal] = useState(0);
useEffect(() => {
  setTotal(paise * qty);
}, [paise, qty]);
```

It renders once with the wrong total, then again with the right one. Just compute it while rendering:

```jsx
const total = paise * qty;
```

## Worked example

Here are both parts of Sana's ticket.

```jsx
import { useEffect, useState } from "react";

export function CartTitle({ count }) {
  useEffect(() => {
    document.title = count > 0 ? `(${count}) Dhaaga & Co.` : "Dhaaga & Co.";
  }, [count]);

  return <p>{count} items in your cart</p>;
}

export function SaleCountdown({ seconds }) {
  const [left, setLeft] = useState(seconds);

  useEffect(() => {
    const id = setInterval(() => {
      setLeft((l) => Math.max(l - 1, 0));
    }, 1000);
    return () => clearInterval(id);
  }, []);

  if (left === 0) return <p>The Diwali sale is over.</p>;
  return <p>Sale ends in {left} s</p>;
}
```

- `CartTitle` lists `count` as its dependency. Adding a keychain changes `count`, so the title updates. A render for any other reason leaves the title alone.
- An empty cart gets the plain name, without "(0)".
- `SaleCountdown` starts one interval after its first render, because its array is empty.
- The updater form `(l) => ...` always starts from the newest `left`. `Math.max(..., 0)` stops it at zero.
- The cleanup stops the interval when the banner leaves the page.

## Try it

Open the preview. Press the add button a few times and watch the browser tab's title. Then take `[count]` off the title effect, so it has no array. It still works, but now it runs after every render, even ones that have nothing to do with the cart.

Next, change the countdown to `setLeft(left - 1)` and watch the banner. It stops at 29. Put the updater form back.

## Recap

- Rendering should be pure. Changing the tab title, timers and talking to servers are side effects, and they go in `useEffect`.
- React runs an effect after the page is updated, then again whenever a value in its dependency array changes.
- An empty array means "once, after the first render". No array means "after every render", which is rarely what you want.
- Return a cleanup function to stop whatever the effect started: timers, listeners, connections.
- A function made in an old render sees old values. Use the updater form, `setLeft((l) => l - 1)`, inside timers.

## Common mistakes

**1. Forgetting to stop the timer.**

```jsx
useEffect(() => {
  setInterval(() => setLeft((l) => l - 1), 1000);
}, []);
```

In development with Strict Mode, the effect runs twice and nothing stops the first timer. The banner drops two seconds every second. After the banner is removed, a timer is still running.

Fix: keep the id and return a cleanup.

```jsx
useEffect(() => {
  const id = setInterval(() => setLeft((l) => l - 1), 1000);
  return () => clearInterval(id);
}, []);
```

**2. Reading state inside a timer that started once.**

```jsx
useEffect(() => {
  const id = setInterval(() => setLeft(left - 1), 1000);
  return () => clearInterval(id);
}, []);
```

The banner shows 29 and never moves again. The timer's function still sees `left` as 30, from the first render.

Fix: use the updater form, `setLeft((l) => l - 1)`.

**3. Leaving out the dependency array.**

```jsx
useEffect(() => {
  document.title = `(${count}) Dhaaga & Co.`;
});
```

The title looks right, but the effect runs after every single render. With a heavier effect, like a call to the server, this gets slow or even loops.

Fix: list what the effect reads, `[count]`.

**4. Putting a fresh object in the array.**

```jsx
const filters = { colour: "Marigold" };
useEffect(() => {
  console.log("filters changed", filters);
}, [filters]);
```

The log prints after every render. `filters` is a new object each time, so `Object.is` says it changed.

Fix: depend on the plain value inside it, `[filters.colour]`, or make the object inside the effect.

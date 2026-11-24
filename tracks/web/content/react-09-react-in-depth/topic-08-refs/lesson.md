## Hook

Everything on the shop's price board is state: change it, and every customer sees the new price. But Raju also keeps a sticky note under the counter: the number of the courier who's coming today. Nobody else needs to see it. Changing it shouldn't make him repaint the price board.

React has the same two kinds of memory. State is the price board: changing it redraws the page. A **ref** is the sticky note: React keeps it safe between renders, but changing it redraws nothing.

## Concept

### Syntax: useRef

```jsx
const box = useRef(initialValue);
box.current = newValue;
```

```callout jargon
A **ref** is an object with one property, `current`. `useRef` gives your component the same ref object on every render, so whatever you put in `current` is still there next time. Changing `current` does **not** make React render again.
```

```animate
title: A ref survives renders, a variable doesn't
kind: code
language: js
code: |
  function makeComponent() {
    const ref = { current: 0 };
    return function render() {
      let local = 0;
      local += 1;
      ref.current += 1;
      console.log(`local ${local}, ref ${ref.current}`);
    };
  }
  const render = makeComponent();
  render();
  render();
steps:
  - line: 10
    say: React makes the ref object once, when the component first appears. It keeps that same object for every render.
  - line: 11
    out: "local 1, ref 1"
    say: The first render makes a fresh local variable. Both end up at 1.
  - line: 12
    out: "local 1, ref 2"
    say: The second render makes a brand-new local, so it starts at 0 again. The ref is the same object as before, so it counts on to 2.
```

That's how React treats refs: one object per component, kept for its whole life.

### State or ref?

Ask one question: **does the page need to show it?**

- Yes, it changes what's on screen: use state. Changing state renders again.
- No, it's only for your code to remember: use a ref. Changing it is silent.

A timer's id, the element you want to focus, and the last width you measured are all things the screen doesn't show. They belong in refs.

### Syntax: a ref to a DOM element

```jsx
const inputRef = useRef(null);
// ...
<input ref={inputRef} />
```

Most of the time React handles the page for you. Sometimes you need the real element: to move the cursor into it, to measure it, or to scroll to it.

```callout jargon
A **DOM node** is the real browser element behind your JSX, like the `<input>` object you used with `querySelector` in web-05. When you pass a ref as the `ref` attribute, React puts the DOM node in `ref.current` once the element is on the page, and sets it back to `null` when it's removed.
```

### Moving focus

```jsx
function SearchBar() {
  const inputRef = useRef(null);
  return (
    <>
      <input ref={inputRef} />
      <button onClick={() => inputRef.current.focus()}>Search</button>
    </>
  );
}
```

```callout jargon
**Focus** is which element the keyboard is typing into right now. `element.focus()` moves it there, and on a phone it also opens the keyboard. The focused element is `document.activeElement`.
```

By the time anyone can click the button, the input is on the page, so `inputRef.current` is the input.

### Syntax: measuring after render

```jsx
useLayoutEffect(() => {
  const width = cardRef.current.getBoundingClientRect().width;
}, []);
```

`getBoundingClientRect()` returns an element's size and position in pixels. You can only measure an element once it's on the page, so measuring happens in an effect.

```callout jargon
**`useLayoutEffect`** works like `useEffect`, but React runs it straight after updating the page and before the browser paints it. Use it to measure and then adjust, so the customer never sees the wrong layout flash for a moment.
```

```animate
title: Measuring a card before the customer sees it
kind: scene
nodes:
  - { id: react, label: React, emoji: "⚛️" }
  - { id: card, label: "<article> card", emoji: "🧶", value: "layout row" }
  - { id: layout, label: useLayoutEffect, emoji: "📏" }
  - { id: screen, label: The screen, emoji: "📱", value: blank }
steps:
  - say: React renders the card with its first guess, a row layout, and puts it on the page.
    send: { from: react, to: card, label: render }
  - say: Before the browser paints, the layout effect runs. The ref points at the real card.
    send: { from: layout, to: card, label: "getBoundingClientRect()" }
  - say: The card is 320 pixels wide, in the narrow cart drawer. That's under 400.
    send: { from: card, to: layout, label: "width 320", back: true }
  - say: The effect sets the layout to stack. React renders again, still before any paint.
    set: { card: "layout stack" }
  - say: The browser paints once, with the stacked card. The customer never sees the squashed row.
    send: { from: card, to: screen, label: paint }
    set: { screen: stacked card }
```

```callout tip
This card measures once, when it appears. To follow a size that keeps changing, browsers have `ResizeObserver`, which you can start in an effect and stop in its cleanup. Often a CSS container query does the job with no JavaScript at all.
```

### Remembering a timer's id

"Notify me" shows a message for 3 seconds. If the customer taps twice, the first timer should be cancelled, so the second tap gets a full 3 seconds. To cancel a timer you need its id, and the id must survive between renders. The screen never shows it, so it goes in a ref.

```jsx
const timerRef = useRef(null);

function handleClick() {
  clearTimeout(timerRef.current);
  setShown(true);
  timerRef.current = setTimeout(() => setShown(false), 3000);
}

useEffect(() => () => clearTimeout(timerRef.current), []);
```

`clearTimeout(null)` does nothing, so the first click is safe. The effect's cleanup cancels a waiting timer when the button leaves the page.

### Don't read refs while rendering

React may render your component at any time, many times. Reading or writing `ref.current` in the body of the component (outside effects and event handlers) gives results that depend on timing.

- During the first render, `inputRef.current` is still `null`: the input doesn't exist yet.
- A value you put in a ref won't show on screen, because nothing renders again.

Use refs in event handlers and effects. If it must appear on the page, it's state.

## Worked example

Here is the search bar from Sana's ticket.

```jsx
import { useRef, useState } from "react";

export function SearchBar() {
  const inputRef = useRef(null);
  const [query, setQuery] = useState("");

  function clear() {
    setQuery("");
    inputRef.current.focus();
  }

  return (
    <div role="search">
      <label htmlFor="search">Search the shop</label>
      <input id="search" ref={inputRef} value={query} onChange={(e) => setQuery(e.target.value)} />
      <button type="button" onClick={() => inputRef.current.focus()}>Search</button>
      <button type="button" onClick={clear}>Clear</button>
    </div>
  );
}
```

- `useRef(null)` starts empty. React fills it with the input once the input is on the page.
- Search moves the focus into the box, so one tap is enough.
- Clear empties the text with state, because the screen shows it. Then it puts the focus back in the box, ready for the next search.
- `type="button"` stops these buttons from submitting a form if the bar ever sits inside one.

## Try it

Open the preview. Press Search and type straight away. Type something, press Clear, and type again without tapping the box.

Then tap "Notify me" twice, about a second apart, and count: the message should stay for a full 3 seconds after the second tap. Now remove the `clearTimeout(timerRef.current);` line from `handleClick` and try again. The first timer hides the message early.

## Recap

- `useRef(initial)` gives an object, `{ current }`, that React keeps between renders. Changing it doesn't render again.
- If the page shows it, use state. If only your code needs it (a timer id, an element), use a ref.
- `ref={myRef}` on an element puts the DOM node in `myRef.current`, so you can call `focus()` or measure it.
- Measure in `useLayoutEffect` with `getBoundingClientRect()`, so the right layout is there before the first paint.
- Read and write refs in event handlers and effects, not while rendering.

## Common mistakes

**1. Focusing during render.**

```jsx
function SearchBar() {
  const inputRef = useRef(null);
  inputRef.current.focus();
  return <input ref={inputRef} />;
}
```

The page crashes with a `TypeError`: you can't read `focus` of `null`. On the first render the input doesn't exist yet.

Fix: focus in an event handler, or in an effect if it must happen when the component appears.

**2. Keeping something the screen shows in a ref.**

```jsx
const count = useRef(0);
return <button onClick={() => (count.current += 1)}>Added {count.current}</button>;
```

The number on the button never changes. The ref changes, but nothing tells React to render again.

Fix: things on screen go in state, `const [count, setCount] = useState(0);`.

**3. Forgetting `.current`.**

```jsx
inputRef.focus();
```

`TypeError: inputRef.focus is not a function`. The ref is the box, not the element.

Fix: `inputRef.current.focus();`.

**4. Losing the timer id.**

```jsx
function handleClick() {
  setShown(true);
  setTimeout(() => setShown(false), 3000);
}
```

Tap twice, a second apart, and the message vanishes 2 seconds after the second tap. The first timer still fires, and nothing can cancel it.

Fix: keep the id in a ref and `clearTimeout(timerRef.current)` before starting a new timer.

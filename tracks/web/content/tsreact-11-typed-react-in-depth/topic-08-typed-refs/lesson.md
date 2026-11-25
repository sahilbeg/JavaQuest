## Hook

Most of the time, React is the one who touches the page. You describe what you want, and React updates the boxes and buttons for you.

But a few jobs need your own hand on one exact element: put the cursor in this box, now. Sana's PIN code boxes need that six times over. React gives you a way to hold on to an element, and TypeScript makes sure you never grab one that isn't there yet.

## Concept

### A ref is a box React doesn't watch

```callout jargon
A **ref** is an object with one property, `current`, that keeps its value between renders. Changing `ref.current` does not re-render the component. Refs are for things the screen doesn't show: a DOM element, a timer id, a count.
```

State is for what the customer sees. When state changes, React renders again. A ref is a private pocket: you can put something in it and take it out later, and React doesn't care.

### Syntax: a ref to an element

```tsx
const inputRef = useRef<HTMLInputElement>(null);
<input ref={inputRef} />
inputRef.current?.focus();
```

`useRef` is a hook. Pass the starting value, `null`, and give the element type in `<...>`. Then hand the ref to an element with the `ref` attribute. React puts the real DOM element into `inputRef.current` after it has put the element on the page.

Each HTML tag has its own type: `HTMLInputElement`, `HTMLButtonElement`, `HTMLHeadingElement`, `HTMLDivElement`. The type tells TypeScript which methods exist, like `focus()` or `select()`.

### What TypeScript knows about ref.current

With `useRef<HTMLInputElement>(null)`, the type of `inputRef.current` is `HTMLInputElement | null`. And TypeScript is right: during the first render the element doesn't exist yet, and after the input leaves the page it's gone again.

```tsx
inputRef.current.focus();
```

```text
error TS18047: 'inputRef.current' is possibly 'null'.
```

Use optional chaining, `inputRef.current?.focus()`, which does nothing when there is no element. Or check with `if (inputRef.current)` first.

```callout tip
In React 19, `useRef` always needs a starting value. `useRef<HTMLInputElement>()` with nothing inside gives `Expected 1 arguments, but got 0.` Pass `null` for elements.
```

### When is ref.current set?

```animate
title: React fills the ref after the element is on the page
kind: scene
nodes:
  - { id: comp, label: SearchBox, emoji: "🔍", value: "rendering" }
  - { id: ref, label: inputRef, emoji: "🫙", value: "current: null" }
  - { id: page, label: The page, emoji: "📄" }
  - { id: effect, label: The effect, emoji: "⚡" }
steps:
  - say: React calls SearchBox. While it renders, the input doesn't exist yet, so the ref is still null.
    focus: [comp, ref]
  - say: React creates the input and puts it on the page.
    send: { from: comp, to: page, label: "<input>" }
  - say: Now React fills the ref with the real input element.
    send: { from: page, to: ref, label: "element" }
    set: { ref: "current: <input>" }
  - say: Effects run after that, so an effect can safely use the element.
    send: { from: ref, to: effect, label: "focus()" }
  - say: The cursor is in the box. Nothing re-rendered, because changing a ref never does.
    set: { comp: "done" }
```

So read `ref.current` in effects and event handlers, never while rendering.

### Syntax: a ref that holds a value

```tsx
const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
const tapsRef = useRef<number>(0);
```

Refs are not only for elements. Anything you need to remember, but not show, can go in one. The classic case is a timer id you need later to cancel the timer.

When the starting value already fits the type, like `0` for a `number`, `current` is just `number`. Only when you start with `null` does the type need `| null`.

Watch why a ref doesn't update the screen on its own:

```animate
title: A ref changes quietly
kind: code
language: ts
code: |
  const taps: { current: number } = { current: 0 };
  function render(): string {
    return `taps: ${taps.current}`;
  }
  let screen = render();
  taps.current = taps.current + 1;
  taps.current = taps.current + 1;
  console.log(screen);
  screen = render();
  console.log(screen);
steps:
  - line: 1
    vars: { taps: "{ current: 0 }" }
    say: A ref is just an object with a current property.
  - line: 5
    vars: { screen: '"taps: 0"' }
    say: The first render shows 0.
  - line: 7
    vars: { taps: "{ current: 2 }" }
    say: Two taps change current to 2. Nothing tells the screen.
  - line: 8
    out: "taps: 0"
    say: The screen still says 0. With a ref, there's no re-render.
  - line: 10
    out: "taps: 2"
    say: Only when something else renders again does the new value show. To show a value, use state.
```

### Refs for many elements

Six boxes need six elements. You can't call `useRef` in a loop, because hooks never go in loops. Instead, keep one ref that holds an array:

```tsx
const boxes = useRef<(HTMLInputElement | null)[]>([]);

<input ref={(el) => { boxes.current[i] = el; }} />
```

```callout jargon
A **callback ref** is a function you pass as `ref`. React calls it with the element when it's on the page, and with `null` when it leaves. You decide where to keep it.
```

Note the braces in `(el) => { boxes.current[i] = el; }`. Without them, `(el) => (boxes.current[i] = el)` returns the element. In React 19 a callback ref may return only nothing or a cleanup function, so TypeScript refuses the version without braces.

### Syntax: passing a ref to your own component

```tsx
import type { Ref } from "react";

function DigitBox({ ref, label }: { ref?: Ref<HTMLInputElement>; label: string }) {
  return <input ref={ref} aria-label={label} />;
}
```

In React 19, `ref` is a normal prop for function components. Type it as `Ref<HTMLInputElement>`, which accepts both a ref object and a callback ref. Older code wraps the component in `forwardRef` to do the same; you'll see it in many projects.

### Don't read refs while rendering

```tsx
return <p>{inputRef.current?.value}</p>;
```

This shows nothing on the first render, and an out-of-date value after that, because changing the input doesn't re-render. If the screen needs a value, keep it in state.

## Worked example

Here's a ref for a different job: after checkout moves to a new step, move the focus to the new step's heading, so screen-reader users hear where they are.

```tsx
import { useEffect, useRef } from "react";

export function StepHeading({ title }: { title: string }) {
  const headingRef = useRef<HTMLHeadingElement>(null);
  const firstRender = useRef<boolean>(true);

  useEffect(() => {
    if (firstRender.current) {
      firstRender.current = false;
      return;
    }
    headingRef.current?.focus();
  }, [title]);

  return (
    <h2 ref={headingRef} tabIndex={-1}>
      {title}
    </h2>
  );
}
```

- `headingRef` holds the `<h2>`, typed `HTMLHeadingElement`. `tabIndex={-1}` lets a heading take focus.
- `firstRender` is a value ref. It remembers "have we rendered before?" without causing a render.
- The effect skips the first render, so the page doesn't jump on load. After that, each new `title` moves the focus.
- `headingRef.current?.focus()` is safe even if the heading is gone.

## Try it

Open the preview. The cursor should start in the first box. Type `411007` without touching the mouse: the focus walks along, and the preview shows the finished PIN code.

Then press Backspace a few times. Each empty box sends you back one. Try removing `event.preventDefault()` and see what changes when you press Backspace in an empty box.

## Recap

- A ref is an object with `current`. It keeps its value between renders, and changing it never re-renders.
- `useRef<HTMLInputElement>(null)` gives `current` the type `HTMLInputElement | null`. Use `?.` before calling methods.
- React fills element refs after the element is on the page, so use them in effects and handlers, not while rendering.
- Value refs hold things the screen doesn't show, like timer ids: `useRef<ReturnType<typeof setTimeout> | null>(null)`.
- For many elements, keep an array in one ref and fill it with callback refs. In React 19, `ref` is a normal prop typed `Ref<T>`.

## Common mistakes

**1. Calling a method without checking for null.**

```tsx
inputRef.current.focus();
```

You see `'inputRef.current' is possibly 'null'`. During the first render, or after the input is removed, there is no element.

Fix: `inputRef.current?.focus();`.

**2. A callback ref that returns the element.**

```tsx
<input ref={(el) => (boxes.current[i] = el)} />
```

TypeScript refuses it: the arrow returns the element, and in React 19 a callback ref may only return nothing or a cleanup function.

Fix: add braces, `ref={(el) => { boxes.current[i] = el; }}`.

**3. Calling useRef in a loop.**

```tsx
const refs = digits.map(() => useRef<HTMLInputElement>(null));
```

It breaks the rules of hooks. If the number of boxes ever changes, React mixes up the hooks.

Fix: one `useRef<(HTMLInputElement | null)[]>([])`, filled with callback refs.

**4. Showing a ref's value on the screen.**

```tsx
const taps = useRef<number>(0);
return <button onClick={() => { taps.current += 1; }}>Tapped {taps.current} times</button>;
```

The count changes, but the button keeps saying 0, because changing a ref doesn't re-render.

Fix: anything the customer sees belongs in state: `const [taps, setTaps] = useState<number>(0);`.

## Hook

In the Aundh shop, every drawer has a label: "Keychains", "Booties", "Gift wrap". Reach into the gift wrap drawer and you know you'll find paper, not scissors.

But sometimes a drawer is empty. And sometimes someone moved the scissors into it. A careful person checks before grabbing.

Pages are the same. When your code reaches for an element, TypeScript wants to know two things: what kind of element is it, and is it there at all?

## Concept

### The DOM has types

With the `dom` library switched on, as it is in this course, TypeScript knows every kind of element and event.

```callout jargon
**DOM types** are the TypeScript types for the page: `Element`, `HTMLElement`, and one type per tag, like `HTMLInputElement`, `HTMLButtonElement` and `HTMLFormElement`. Events have types too: `Event`, `MouseEvent`, `KeyboardEvent`.
```

Each type lists what that kind of element has. An `HTMLInputElement` has `value` and `checked`. A plain `HTMLElement`, like a `<div>`, has neither. So reading `.value` from a div is caught before the page ever loads.

### Syntax: what querySelector returns

```ts
const box = document.querySelector("#qty");
```

`box` has the type `Element | null`. It's `null` because the page might not have a `#qty` at all. It's only `Element`, the most general element type, because TypeScript can't read your HTML to see what `#qty` is.

```ts
const box = document.querySelector("#qty");
box.value = "2";
```

```text
error TS18047: 'box' is possibly 'null'.
```

Even after a null check, `box.value` is still an error: a general `Element` has no `value`.

With a plain tag name as the selector, TypeScript does know the type: `document.querySelector("input")` gives `HTMLInputElement | null`.

### Syntax: querySelector with a type argument

```ts
const box = document.querySelector<HTMLInputElement>("#qty");
```

The type in angle brackets says what you expect to find, so `box` is `HTMLInputElement | null`. You still have to check for `null`.

```callout warning
This is a promise, like `as`. If `#qty` turns out to be a `<div>`, TypeScript still believes it's an input. When you can't be sure, check with `instanceof` instead.
```

### Syntax: checking an element with instanceof

```ts
const box = document.querySelector("#qty");
if (box instanceof HTMLInputElement) {
  box.value = "2";
}
```

`instanceof HTMLInputElement` checks the real element while the page runs. Inside the `if`, `box` is an `HTMLInputElement`. It also rules out `null`, because `null` is never an instance of anything.

### Syntax: typed events

```ts
button.addEventListener("click", (event) => {
  // event is a MouseEvent here
});
```

TypeScript knows which event each name gives you. `"click"` gives a `MouseEvent`, `"keydown"` a `KeyboardEvent`, and `"input"` a plain `Event`. You don't have to write the type yourself.

### Syntax: narrowing event.target

```ts
root.addEventListener("click", (event) => {
  const target = event.target;
  if (target instanceof HTMLButtonElement) {
    console.log(target.dataset.step);
  }
});
```

`event.target` has the type `EventTarget | null`: the thing that was clicked could be any element, or even not an element at all. With event delegation, one listener on a parent, you check the target with `instanceof` before you use it.

`dataset` values are `string | undefined`, because an element might not have that `data-` attribute.

```animate
title: One listener, and a check before every use
kind: scene
nodes:
  - { id: plus, label: "+ button", emoji: "➕", value: 'data-step="1"', row: 0, col: 0 }
  - { id: note, label: "Max 10 note", emoji: "🛈", row: 1, col: 0 }
  - { id: root, label: "Stepper div", emoji: "📦", value: "listening", row: 0, col: 1 }
  - { id: input, label: "qty input", emoji: "🔢", value: "1", row: 0, col: 2 }
steps:
  - say: A customer taps the + button. The click bubbles up to the stepper, where the one listener waits.
    send: { from: plus, to: root, label: "click" }
  - say: event.target is EventTarget | null. The listener checks target instanceof HTMLButtonElement. It is a button, so that passes.
    focus: [root]
  - say: target.dataset.step is "1", not undefined, so the listener can use it as a step.
    focus: [plus]
  - say: The input was found with querySelector and checked for null once, at the start. Now its value is updated to 2.
    send: { from: root, to: input, label: "value = 2" }
    set: { input: "2" }
  - say: Later the customer taps the small note under the stepper. The click bubbles up too.
    send: { from: note, to: root, label: "click" }
  - say: The note is not a button, so the instanceof check fails and nothing happens. No crash, no NaN.
    focus: [root]
```

## Worked example

The cart has a gift-wrap checkbox. Ticking it adds ₹30.

```ts
function setupGiftWrap(root: HTMLElement, onChange: (extraPaise: number) => void): void {
  const box = root.querySelector<HTMLInputElement>('input[name="giftWrap"]');
  if (box === null) {
    throw new Error("The cart has no gift wrap box");
  }
  box.addEventListener("change", () => {
    onChange(box.checked ? 3000 : 0);
  });
}
```

Line by line:

1. `root: HTMLElement` means any element can hold the cart. `onChange` is a typed callback: it gets a number of paise.
2. `querySelector<HTMLInputElement>` says what we expect, so `box` is `HTMLInputElement | null`.
3. The `null` check comes once, at the top. If the page is broken, the error says exactly what's missing, instead of a vague "cannot read properties of null" later.
4. After the check, `box` is an `HTMLInputElement`, even inside the listener, so `box.checked` is allowed.
5. A tick calls `onChange(3000)`; unticking calls `onChange(0)`.

## Try it

The logic part of a stepper doesn't need a page. Run this, then change it so a step can never take the quantity above 10 or below 1.

```ts run
function nextQty(current: string, step: string | undefined): number {
  if (step === undefined) {
    return Number(current);
  }
  return Number(current) + Number(step);
}
console.log(nextQty("1", "1"), nextQty("10", "1"), nextQty("1", "-1"), nextQty("3", undefined));
```

## Recap

- The `dom` library gives every element and event a type: `HTMLInputElement`, `HTMLButtonElement`, `MouseEvent`, and more.
- `querySelector` returns `Element | null`. Check for `null` before you use it.
- `querySelector<HTMLInputElement>(...)` says which element you expect, but it's a promise, not a check.
- `instanceof HTMLInputElement` really checks the element, and rules out `null` too.
- `event.target` is `EventTarget | null`, so narrow it with `instanceof` before using it. `dataset` values may be `undefined`.

## Common mistakes

**1. Using querySelector's result without a null check.**

```ts
const box = document.querySelector<HTMLInputElement>("#qty");
box.value = "2";
```

```text
error TS18047: 'box' is possibly 'null'.
```

Fix: `if (box === null) throw new Error("No quantity box");`, then use it.

**2. Silencing the error with !.**

```ts
const box = document.querySelector<HTMLInputElement>("#qty")!;
box.value = "2";
```

It compiles, because `!` tells TypeScript "this is never null". But if the page has no `#qty`, it still crashes, with a less helpful message. Fix: check for `null` and throw an error that names what's missing.

**3. Reading .value from event.target straight away.**

```ts
root.addEventListener("input", (event) => {
  console.log(event.target.value);
});
```

```text
error TS18047: 'event.target' is possibly 'null'.
```

And even when it isn't null, a general `EventTarget` has no `value`. Fix: `if (event.target instanceof HTMLInputElement) { ... event.target.value ... }`.

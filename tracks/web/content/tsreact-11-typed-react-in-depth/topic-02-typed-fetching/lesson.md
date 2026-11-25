## Hook

A courier brings a sealed box and says "Twenty balls of Marigold yarn". Do you write 20 in the stock book straight away? A careful shopkeeper opens the box first. Sometimes it's Indigo. Sometimes it's empty.

Data from a server is that sealed box. TypeScript can't see inside it, however sure your code sounds. This ticket shows how to fetch in a component, open the box safely, and show the customer the right thing while you wait.

## Concept

### Fetching is a side effect

Asking a server for data reaches outside React, so it goes in an effect. The effect runs after the first render, and again when the thing you ask about changes.

The first render can't have the data yet. So a fetching component always has a moment where it's **loading**. Later, it either has the data, or the request **failed**.

```callout jargon
An **API** is the part of a server that programs talk to. Here, `GET /api/yarn/Marigold` is a request to Dhaaga's stock API, and it answers with JSON like `{ "colour": "Marigold", "grams": 1200 }`.
```

### Syntax: fetching in an effect

```tsx
useEffect(() => {
  async function load() {
    const res = await fetch(url);
    // ... use res
  }
  load();
}, [url]);
```

Last ticket showed that the effect function itself can't be `async`. So you write an `async` function inside it and call it straight away. The effect still returns nothing, or a cleanup.

### res.json() gives you any

Look at the type of `res.json()` in the DOM types: it's `Promise<any>`. So TypeScript lets you do anything with the result, and checks nothing.

```callout jargon
**any** switches type checking off for a value. **unknown** is its safe twin: the value could be anything, so TypeScript makes you check it before you use it.
```

A cast like `await res.json() as YarnStock` doesn't help. It doesn't check the data. It only tells TypeScript to stop asking. If the server sends grams as `"1200"`, your code believes it is a number.

So store the answer as `unknown`:

```tsx
const data: unknown = await res.json();
console.log(data.grams);
```

```text
error TS18046: 'data' is of type 'unknown'.
```

Good. TypeScript now refuses to let you use the answer until you have checked it.

### Syntax: a type guard for the answer

```ts
function isYarnStock(value: unknown): value is YarnStock {
  return typeof value === "object" && value !== null
    && "colour" in value && typeof value.colour === "string"
    && "grams" in value && typeof value.grams === "number";
}
```

You met type guards in the TypeScript course. The return type `value is YarnStock` means: "when I return true, treat `value` as a `YarnStock`".

Each check narrows the type a little more. `typeof value === "object"` lets in `null`, so `value !== null` comes next. `"grams" in value` tells TypeScript the property exists, so `value.grams` is allowed.

Watch the guard check three answers:

```animate
title: A type guard opens the box
kind: code
language: ts
code: |
  function isStock(v: unknown): v is { grams: number } {
    return typeof v === "object" && v !== null && "grams" in v && typeof v.grams === "number";
  }
  console.log(isStock({ grams: 1200 }));
  console.log(isStock({ grams: "1200" }));
  console.log(isStock(null));
steps:
  - line: 1
    say: The guard takes unknown, because it must cope with anything the server sends.
  - line: 4
    out: "true"
    say: An object, not null, with grams that is a number. It passes.
  - line: 5
    out: "false"
    say: grams is text here, the bad deploy Arjun saw. The last check catches it.
  - line: 6
    out: "false"
    say: typeof null is "object", a famous JavaScript quirk. The v !== null check stops it.
```

### One state, three shapes

A fetching component is in exactly one of three situations. A union type says so:

```ts
type Load<T> =
  | { status: "loading" }
  | { status: "error"; message: string }
  | { status: "ready"; data: T };
```

```tsx
const [load, setLoad] = useState<Load<YarnStock>>({ status: "loading" });
```

`status` is the **discriminant**: the field that says which shape you have. After `if (load.status === "ready")`, TypeScript knows `load.data` exists. In the other two shapes, `load.data` is a type error.

```callout tip
Three separate states (`loading`, `error`, `data`) can disagree: loading is true while there's also an error. One union makes impossible combinations impossible to write.
```

### Answers can arrive out of order

Arjun taps Marigold, then quickly Indigo. Two requests are now in flight. On slow 3G, Marigold's answer may come back last.

```animate
title: A slow answer arrives after a fast one
kind: scene
nodes:
  - { id: panel, label: Yarn panel, emoji: "🧶", value: "Marigold" }
  - { id: api, label: Stock API, emoji: "🖥️" }
  - { id: page, label: The page, emoji: "📄", value: "Checking..." }
steps:
  - say: The panel shows Marigold, so its effect asks the API about Marigold.
    send: { from: panel, to: api, label: "GET /api/yarn/Marigold" }
  - say: Arjun taps Indigo before the answer comes. The effect runs again and asks about Indigo.
    set: { panel: Indigo }
    send: { from: panel, to: api, label: "GET /api/yarn/Indigo" }
  - say: Indigo's answer is quick. The page shows Indigo's grams.
    send: { from: api, to: page, label: "Indigo 600 g", back: true }
    set: { page: "Indigo: 600 g" }
  - say: Then Marigold's slow answer lands. Without a guard, it replaces Indigo's number.
    send: { from: api, to: page, label: "Marigold 1200 g", back: true }
    set: { page: "Indigo: 1200 g ❌" }
  - say: The panel now says Indigo with Marigold's grams. Arjun promises an elephant he can't make.
    focus: [page]
```

```callout jargon
A **race condition** is a bug that depends on which of two things finishes first. Here, it depends on which answer arrives last.
```

### Syntax: ignoring stale answers

```tsx
useEffect(() => {
  let ignore = false;
  async function load() {
    const data = await getSomething();
    if (!ignore) setLoad({ status: "ready", data });
  }
  load();
  return () => { ignore = true; };
}, [colour]);
```

Each run of the effect gets its own `ignore` variable. When `colour` changes, React runs the old effect's cleanup, which sets *its* `ignore` to true. That run's late answer is then thrown away.

```callout jargon
A **stale** answer is one for a question you no longer care about: Marigold's grams, after Arjun moved on to Indigo.
```

### Start each request from a clean slate

When `colour` changes, the old grams are still in state until the new answer comes. So the panel would show "Indigo: 1200 g", Marigold's number under Indigo's name.

Set the state back to loading at the start of the effect:

```tsx
setLoad({ status: "loading" });
```

Now the panel says `Checking Indigo yarn...` until Indigo's own answer arrives.

## Worked example

Here's the same pattern for a different screen: the review count under an item. The API answers `{ "itemId": "DC-1042", "count": 12 }`.

```tsx
import { useEffect, useState } from "react";

interface ReviewCount { itemId: string; count: number }

type Load<T> =
  | { status: "loading" }
  | { status: "error"; message: string }
  | { status: "ready"; data: T };

function isReviewCount(value: unknown): value is ReviewCount {
  return typeof value === "object" && value !== null
    && "itemId" in value && typeof value.itemId === "string"
    && "count" in value && typeof value.count === "number";
}

export function Reviews({ itemId }: { itemId: string }) {
  const [load, setLoad] = useState<Load<ReviewCount>>({ status: "loading" });

  useEffect(() => {
    let ignore = false;
    setLoad({ status: "loading" });

    async function fetchCount() {
      try {
        const res = await fetch(`/api/reviews/${itemId}`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data: unknown = await res.json();
        if (!isReviewCount(data)) throw new Error("Unexpected answer");
        if (!ignore) setLoad({ status: "ready", data });
      } catch {
        if (!ignore) setLoad({ status: "error", message: "Reviews are resting." });
      }
    }

    fetchCount();
    return () => { ignore = true; };
  }, [itemId]);

  if (load.status === "loading") return <p>Loading reviews...</p>;
  if (load.status === "error") return <p role="alert">{load.message}</p>;
  return <p>{load.data.count} reviews</p>;
}
```

- `fetch` only rejects when the network fails. A 404 still "succeeds", so `res.ok` is checked by hand.
- `data` is `unknown` until `isReviewCount` says yes. Inside the `if`, TypeScript knows it's a `ReviewCount`.
- One `catch` handles all three failures: no network, a bad status, and a bad shape.
- The last line can read `load.data`, because the two `return`s above narrowed `load` to the `"ready"` shape.

## Try it

Open the preview. The pretend API answers Marigold slowly and Indigo quickly, and sends a broken answer for Mint.

Tap Marigold, then Indigo, fast. After your fix, the panel ends on Indigo's grams. Then take out `if (!ignore)` and try again: Marigold's late answer lands under Indigo's name. Put it back, and tap Mint to see the honest error.

## Recap

- Fetching is a side effect, so it lives in `useEffect`, inside an inner `async` function.
- `res.json()` is `Promise<any>`. Store the result as `unknown`, and check it with a type guard before you use it.
- A cast with `as` checks nothing at runtime. A type guard does.
- One `Load<T>` union (`loading`, `error`, `ready`) keeps the screen in exactly one state.
- Reset to loading when the input changes, and ignore late answers with a flag set in the cleanup.

## Common mistakes

**1. Trusting the answer with a cast.**

```tsx
const stock = (await res.json()) as YarnStock;
setLoad({ status: "ready", data: stock });
```

TypeScript is happy. But when the server sends `"grams": "1200"`, the panel works out `"1200" / 40` and shows nonsense.

Fix: `const data: unknown = await res.json();` and check it with `isYarnStock(data)`.

**2. Forgetting res.ok.**

```tsx
const res = await fetch(`/api/yarn/${colour}`);
const data: unknown = await res.json();
```

A 404 or a 500 still arrives as a normal response. Its body is an error message, which then fails your guard with a confusing message, or worse, passes it.

Fix: `if (!res.ok) throw new Error(...)` before reading the body.

**3. Letting a slow answer win.**

```tsx
useEffect(() => {
  async function load() {
    const res = await fetch(`/api/yarn/${colour}`);
    setLoad({ status: "ready", data: await res.json() });
  }
  load();
}, [colour]);
```

Tap Marigold then Indigo on slow 3G, and Marigold's late answer shows under Indigo.

Fix: `let ignore = false;`, set it to true in the cleanup, and only `setLoad` when `!ignore`.

**4. Three states that disagree.**

```tsx
const [loading, setLoading] = useState(true);
const [error, setError] = useState<string | null>(null);
const [stock, setStock] = useState<YarnStock | null>(null);
```

Forget one `setLoading(false)`, and the page shows "Checking..." above an error. TypeScript can't help, because every mix is allowed.

Fix: one `useState<Load<YarnStock>>`, where each shape is complete.

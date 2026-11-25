## Hook

When one fairy light in a cheap string blows, the whole string goes dark. Better strings are wired so one dead bulb stays dark on its own, and the rest keep shining.

A React page is wired like the cheap string: an error while rendering any component blanks the whole page. This ticket adds the better wiring, called an error boundary, and types it so the fallback always gets a real `Error`.

## Concept

### One broken part blanks the page

If a component throws while React is rendering it, React can't trust anything it was building. So, by default, it removes the whole app from the page. The customer sees white.

```callout jargon
An **error boundary** is a component that catches errors thrown while rendering the components inside it. It shows a **fallback** (a stand-in message) for that part, and the rest of the page keeps working.
```

```animate
title: A boundary keeps the crash inside one box
kind: scene
nodes:
  - { id: page, label: ItemPage, emoji: "🐘", value: "Golu, ₹450.00" }
  - { id: cart, label: Add to cart, emoji: "🛒" }
  - { id: boundary, label: ErrorBoundary, emoji: "🛡️" }
  - { id: similar, label: SimilarItems, emoji: "🧶", value: "rendering" }
links:
  - { from: page, to: cart }
  - { from: page, to: boundary }
  - { from: boundary, to: similar }
steps:
  - say: React renders Golu's page. The heading, price and Add to cart button are fine.
    focus: [page, cart]
  - say: SimilarItems gets an item with no name and throws while rendering.
    set: { similar: "throws!" }
    focus: [similar]
  - say: The error travels up the tree until it meets the nearest error boundary.
    send: { from: similar, to: boundary, label: TypeError }
  - say: The boundary swaps its part for the fallback. Only the suggestions are replaced.
    set: { similar: "fallback shown" }
  - say: Everything outside the boundary stays. The customer can still buy Golu.
    focus: [page, cart]
```

### Syntax: a typed class component

```tsx
class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null };
  render() {
    return this.props.children;
  }
}
```

Error boundaries are the one thing in React that still needs a **class** component. There is no hook for it.

`Component<Props, State>` is generic, like `useState<T>`. The first type is the props, the second the state. Inside the class, `this.props` and `this.state` have those types, and `this.setState` only accepts the right shape.

```callout jargon
A **class component** is a class that extends React's `Component`, with a `render()` method that returns JSX. Before hooks, every component with state was written this way.
```

### Syntax: getDerivedStateFromError

```tsx
static getDerivedStateFromError(error: unknown): State {
  return { error: toError(error) };
}
```

React calls this when a component inside throws while rendering. It returns the new state. The boundary then renders again, and this time `render()` sees the error and shows the fallback.

It's `static`: it belongs to the class, not to one boundary, so it can't use `this`. It only turns the error into state.

### Syntax: componentDidCatch

```tsx
componentDidCatch(error: unknown, info: ErrorInfo) {
  this.props.onError?.(toError(error));
}
```

React calls this after the fallback is on the page. It's the place for side effects, like sending the error to a logging service. `info.componentStack` says which components the error went through.

```callout tip
React also logs every caught error with `console.error`. That's why tests that throw on purpose often silence `console.error`.
```

### Anything can be thrown

JavaScript lets you `throw` any value: an `Error`, but also a string, a number or an object. So TypeScript types a caught value as `unknown`, not `Error`. A small helper turns it into a real `Error`, so the rest of your code can trust `.message`:

```animate
title: Turning anything thrown into an Error
kind: code
language: ts
code: |
  function toError(value: unknown): Error {
    return value instanceof Error ? value : new Error(String(value));
  }
  console.log(toError(new TypeError("name is missing")).message);
  console.log(toError("stock API down").message);
  console.log(toError(404).message);
  console.log(toError("x") instanceof Error);
steps:
  - line: 1
    say: toError takes unknown, because a thrown value really could be anything.
  - line: 4
    out: "name is missing"
    say: A real error, even a TypeError, is already an Error. It's returned as it is.
  - line: 5
    out: "stock API down"
    say: Someone threw a plain string. It becomes the message of a new Error.
  - line: 6
    out: "404"
    say: A number works too. String(404) is "404".
  - line: 7
    out: "true"
    say: Whatever went in, an Error comes out. The fallback can always read error.message.
```

### A typed fallback that can try again

```tsx
fallback: (error: Error, reset: () => void) => ReactNode;
```

The fallback prop is a function, not fixed JSX. The boundary calls it with the error and a `reset` function, and shows whatever it returns. Each page decides its own message and buttons.

```callout jargon
A **render prop** is a prop that is a function returning JSX. The component calls it to let its parent decide what to draw.
```

```tsx
<ErrorBoundary
  fallback={(error, reset) => (
    <div role="alert">
      <p>Similar items are taking a break.</p>
      <button onClick={reset}>Try again</button>
    </div>
  )}
>
  <SimilarItems items={similar} />
</ErrorBoundary>
```

TypeScript infers `error` as `Error` and `reset` as `() => void` from the prop's type, so you don't annotate them here.

### Resetting when the page moves on

A caught error stays in the boundary's state until something clears it. Two ways:

- **Try again:** `reset` sets the state back to `{ error: null }`, and the children render again.
- **A reset key:** when the customer taps another item, the page is the same component with a new `itemId`. The boundary should forget the old error. `componentDidUpdate` compares props:

```tsx
componentDidUpdate(prevProps: Props) {
  if (this.state.error !== null && prevProps.resetKey !== this.props.resetKey) {
    this.setState({ error: null });
  }
}
```

Without this, the Bengaluru customer taps from Golu to the coaster set, and the suggestions stay broken, even though the coaster set's data is fine.

### What boundaries don't catch

Boundaries catch errors thrown **while rendering** the components inside them. They do not catch:

- errors in event handlers, like an `onClick` (use `try`/`catch` there),
- errors in async code, like a `fetch` that fails (use the error state from ticket 2),
- errors in the boundary itself.

```callout tip
Many teams use the small `react-error-boundary` package instead of writing the class. It has the same ideas: a fallback render prop, `resetKeys`, and `onError`.
```

## Worked example

Here's a boundary at the very top of the app, for a different job: if anything at all breaks, show a calm full-page message with Dhaaga's phone number, and report the error.

```tsx
import { Component } from "react";
import type { ErrorInfo, ReactNode } from "react";

interface AppBoundaryProps { children: ReactNode; report: (error: Error, where: string) => void }
interface AppBoundaryState { failed: boolean }

function toError(value: unknown): Error {
  return value instanceof Error ? value : new Error(String(value));
}

export class AppBoundary extends Component<AppBoundaryProps, AppBoundaryState> {
  state: AppBoundaryState = { failed: false };

  static getDerivedStateFromError(): AppBoundaryState {
    return { failed: true };
  }

  componentDidCatch(error: unknown, info: ErrorInfo) {
    this.props.report(toError(error), info.componentStack ?? "");
  }

  render() {
    if (this.state.failed) {
      return (
        <main role="alert">
          <h1>Something went wrong.</h1>
          <p>Please call Raju at the shop on 98765 43210, and we'll take your order by phone.</p>
        </main>
      );
    }
    return this.props.children;
  }
}
```

- `Component<AppBoundaryProps, AppBoundaryState>` types `this.props` and `this.state`.
- This boundary's state is just a `boolean`, because its fallback never shows the message.
- `componentDidCatch` reports the error and where it happened. `componentStack` may be missing, so `?? ""` gives a string.
- There's no reset here. At the top of the app, a reload is the honest fix.

## Try it

Open the preview. Golu's page shows a broken similar item. Before your fix, the whole preview is blank. After it, only the suggestions show the fallback, and **Add to cart** still works.

Then press **Show the coaster set**. Its suggestions are fine, and the boundary clears because `resetKey` changed. Remove `resetKey` from `ItemPage` and try again: the fallback stays.

## Recap

- An error while rendering removes the whole app. An error boundary catches it and shows a fallback for its part only.
- Boundaries are class components: `class ErrorBoundary extends Component<Props, State>`.
- `static getDerivedStateFromError` turns the error into state; `componentDidCatch` is for side effects like logging.
- Anything can be thrown, so treat caught values as `unknown` and turn them into an `Error`.
- A typed fallback render prop `(error: Error, reset: () => void) => ReactNode` lets each page draw its own message, and a `resetKey` clears the error when the page moves on.

## Common mistakes

**1. Wrapping the whole page in one boundary.**

```tsx
<ErrorBoundary fallback={() => <p>Something went wrong.</p>}>
  <ItemPage itemId="DC-1042" />
</ErrorBoundary>
```

A broken suggestion now replaces the heading, the price and Add to cart with one message. Better than white, but Golu still can't be bought.

Fix: wrap only the risky extra, `<SimilarItems>`, so the rest stays.

**2. Typing the caught value as Error.**

```tsx
static getDerivedStateFromError(error: Error) {
  return { error };
}
```

It compiles, but when someone throws a string, `error.message` is `undefined` and the fallback shows nothing.

Fix: take `unknown` and use `toError(error)`.

**3. Doing side effects in getDerivedStateFromError.**

```tsx
static getDerivedStateFromError(error: unknown) {
  sendToLogs(error);
  return { error: toError(error) };
}
```

React may call it more than once while rendering. Logs get duplicates, and in a test the spy counts too many calls.

Fix: log in `componentDidCatch`, which runs once, after the fallback is on the page.

**4. Expecting a boundary to catch a click.**

```tsx
<button onClick={() => { throw new Error("Payment failed"); }}>Pay</button>
```

The boundary never hears about it. Errors in event handlers don't happen while rendering.

Fix: `try`/`catch` in the handler, and put the message in state.

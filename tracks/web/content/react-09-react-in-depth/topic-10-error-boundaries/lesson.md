## Hook

When one plug in a house shorts, the fuse for that room trips. The lights go off in the kitchen, but the fridge in the next room keeps humming. Without fuses, one bad plug could black out the whole building.

In React, an error in one small component can black out the whole page. Raju's ticket is exactly that: one odd review, and Golu's page went white. An error boundary is a fuse for part of your page.

## Concept

### One error can blank the whole page

If any component throws an error while rendering, and nothing catches it, React removes the **entire** page from the screen. React does this on purpose: showing a half-broken page with wrong data could be worse than showing nothing.

In Raju's case, the `Reviews` component runs `review.text.trim()`. One review has no text, so `review.text` is `null`, and calling `.trim()` on `null` throws a `TypeError`. The reviews are at the bottom of the page, but the price, the photo and the Add to cart button all vanish with them.

```animate
title: A boundary stops one crash from spreading
kind: scene
nodes:
  - { id: page, label: ItemPage, emoji: "🐘", value: working }
  - { id: buy, label: "Price and Add to cart", emoji: "🛒", row: 1, col: 0 }
  - { id: fuse, label: ErrorBoundary, emoji: "🧯", value: "no error", row: 1, col: 1 }
  - { id: reviews, label: Reviews, emoji: "⭐", row: 2, col: 1 }
links:
  - { from: page, to: buy }
  - { from: page, to: fuse }
  - { from: fuse, to: reviews }
steps:
  - say: React renders Golu's page. The price and the button render fine.
    focus: [page, buy]
  - say: Reviews meets a review with no text and throws a TypeError while rendering.
    focus: [reviews]
    set: { reviews: crashed }
  - say: The error travels up the tree, looking for a boundary. The nearest one is just above Reviews.
    send: { from: reviews, to: fuse, label: TypeError }
  - say: The boundary catches it and switches to its fallback message instead of the reviews.
    set: { fuse: "showing fallback" }
  - say: Everything outside the boundary is untouched. Customers can still buy Golu.
    focus: [buy]
```

### Syntax: an error boundary

```jsx
class ErrorBoundary extends Component {
  state = { error: null };

  static getDerivedStateFromError(error) {
    return { error };
  }

  render() {
    if (this.state.error) return <p>Something went wrong.</p>;
    return this.props.children;
  }
}
```

```callout jargon
An **error boundary** is a component that catches errors thrown while rendering anything inside it, and shows a **fallback** (a stand-in message) instead of crashing the page.
```

Error boundaries are the one thing in React that you still write as a class. There is no hook for them.

### Class components in one card

Before hooks, every React component with state was a class. You met classes in web-04. A class component looks like this:

```jsx
class Counter extends Component {
  state = { count: 0 };

  render() {
    return <button onClick={() => this.setState({ count: this.state.count + 1 })}>{this.state.count}</button>;
  }
}
```

```callout jargon
A **class component** extends React's `Component`. Its state lives in `this.state`, its props in `this.props`, and it changes state with `this.setState(...)`. Its `render()` method returns the JSX, like a function component's return.
```

You'll read class components in older code. For new components, functions and hooks are the norm, except for boundaries.

### Two methods that catch errors

```jsx
static getDerivedStateFromError(error) {
  return { error };
}

componentDidCatch(error, info) {
  reportToServer(error);
}
```

- `getDerivedStateFromError` runs during rendering, when a child throws. It returns the new state, so the next render shows the fallback. It's `static`: it can't use `this`, it only turns the error into state.
- `componentDidCatch` runs afterwards. It's the place for side effects, like sending the error to a logging service so the team hears about it.

React also logs every caught error with `console.error`, so you'll still see it in the console while developing.

### Syntax: using a boundary

```jsx
<ErrorBoundary fallback="Reviews are taking a break.">
  <Reviews reviews={reviews} />
</ErrorBoundary>
```

Where you put a boundary decides how much of the page a crash can take with it:

- Around a risky part: reviews, recommendations, a map. One crash hides only that part.
- Around the whole app: a last safety net, so customers see a friendly message instead of a white screen.

Most apps use both.

### Letting the customer try again

Some errors come from data that gets fixed: a review is edited, a server sends better data next time. A "Try again" button clears the error, and the boundary renders its children once more:

```jsx
<button onClick={() => this.setState({ error: null })}>Try again</button>
```

If the children are still broken, they throw again, and the fallback comes back. Nothing loops: each try is one click.

### What boundaries don't catch

A boundary only catches errors thrown **while React renders** the components inside it (and in their effects and lifecycle methods). It does not catch:

- Errors in event handlers, like an `onClick` that throws. Those happen later, when the customer clicks. Use `try`/`catch` in the handler.
- Errors in async code, like a failed `fetch` in an effect. Catch those in the async function and put an error message in state, as you did in ticket 2.
- Errors in the boundary itself. A boundary protects its children, not itself.

The same is true of `try`/`catch` in plain JavaScript. It only catches errors thrown while its block is running:

```js run
function handler() {
  try {
    setTimeout(() => {
      try {
        throw new Error("late error");
      } catch (e) {
        console.log("caught inside the timer:", e.message);
      }
    }, 1);
    console.log("try block finished");
  } catch (e) {
    console.log("outer catch:", e.message);
  }
}

handler();
```

The outer `catch` never runs. By the time the timer throws, the `try` block finished long ago. Only a `catch` inside the timer can see the error.

```callout tip
Many teams use the small **react-error-boundary** package. It gives you a ready-made `<ErrorBoundary>` with a fallback, a reset button and an `onError` callback, so you don't write the class yourself. It works exactly like the one you're building.
```

## Worked example

Here is the boundary, and Golu's page using it.

```jsx
import { Component } from "react";

export class ErrorBoundary extends Component {
  state = { error: null };

  static getDerivedStateFromError(error) {
    return { error };
  }

  componentDidCatch(error) {
    this.props.onError?.(error);
  }

  render() {
    if (this.state.error) {
      return (
        <div role="alert">
          <p>{this.props.fallback ?? "Something went wrong."}</p>
          <button onClick={() => this.setState({ error: null })}>Try again</button>
        </div>
      );
    }
    return this.props.children;
  }
}
```

```jsx
export function ItemPage({ reviews }) {
  return (
    <main>
      <h1>Golu the elephant</h1>
      <button>Add to cart</button>
      <ErrorBoundary fallback="Reviews are taking a break. You can still buy Golu.">
        <Reviews reviews={reviews} />
      </ErrorBoundary>
    </main>
  );
}
```

- `state = { error: null }` is a class field: the boundary starts with no error.
- When `Reviews` throws, `getDerivedStateFromError` stores the error, and `render` shows the fallback.
- `componentDidCatch` passes the error to `onError`, if the page gave one. `?.()` calls it only when it exists.
- `role="alert"` makes screen readers announce the message.
- Only the reviews are inside the boundary, so the heading and the button always stay.

## Try it

Open the preview and press the button that posts Vikram's review, the one with stars but no text. Before your fix, the whole page goes white. After it, the fallback sits where the reviews were, and the price and the Add to cart button still work.

Then move `<ErrorBoundary>` so it wraps the whole `<main>`. Now the crash replaces the whole page with the fallback. It's not blank, but nobody can buy Golu. Move it back around just the reviews.

## Recap

- An error thrown while rendering, with nothing to catch it, removes the whole page.
- An error boundary is a class component with `static getDerivedStateFromError`, which turns the error into state, and a `render` that shows a fallback.
- Use `componentDidCatch` to report the error, and a Try again button to clear it with `this.setState({ error: null })`.
- Put boundaries around risky parts, so one crash hides only that part, and one around the whole app as a safety net.
- Boundaries don't catch errors in event handlers or async code. Use `try`/`catch` there.

## Common mistakes

**1. One boundary around everything.**

```jsx
<ErrorBoundary>
  <ItemPage reviews={reviews} />
</ErrorBoundary>
```

The page isn't white any more, but a broken review replaces the whole page with "Something went wrong." Customers still can't buy Golu.

Fix: put a boundary close to the risky part, around `<Reviews>`.

**2. Expecting a boundary to catch a click.**

```jsx
<ErrorBoundary>
  <button onClick={() => JSON.parse("not json")}>Apply coupon</button>
</ErrorBoundary>
```

The fallback never shows. The console shows an uncaught `SyntaxError`, because the error happened in an event handler, after rendering.

Fix: catch it in the handler, and put a message in state.

```jsx
function applyCoupon() {
  try {
    JSON.parse(text);
  } catch {
    setMessage("That coupon code didn't work.");
  }
}
```

**3. Forgetting `static`.**

```jsx
getDerivedStateFromError(error) {
  return { error };
}
```

React warns in the console that `getDerivedStateFromError` must be static, and the boundary doesn't catch anything. The crash takes the page down as before.

Fix: write `static getDerivedStateFromError(error)`.

**4. Trying to write a boundary as a function.**

```jsx
function ErrorBoundary({ children }) {
  try {
    return children;
  } catch {
    return <p>Something went wrong.</p>;
  }
}
```

The `try` finishes before React renders the children, so their errors are never caught here.

Fix: error boundaries must be class components, or use the react-error-boundary package.

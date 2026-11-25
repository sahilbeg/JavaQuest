# Typed error boundaries

Stop one broken suggestion from blanking Golu's whole page.

`src/SimilarItems.tsx` crashes on an item with no name. **Don't fix it.** The boundary has to protect the page from bugs nobody has found yet, too.

In `src/ErrorBoundary.tsx`:

1. **TODO 1:** `toError(value: unknown)` returns `value` itself when it is an `Error`, and otherwise `new Error(String(value))`.
2. **TODO 2:** add `static getDerivedStateFromError(error: unknown)`, returning `{ error: toError(error) }`.
3. **TODO 3:** add `componentDidCatch(error: unknown)`, which calls the `onError` prop with `toError(error)`, if there is one.
4. **TODO 4:** add `componentDidUpdate(prevProps)`: when there is an error and `resetKey` has changed, clear the error.
5. **TODO 5:** in `render()`, while there is an error, return `this.props.fallback(this.state.error, this.reset)`. With no error, render the children. (`reset` is done.)

In `src/ItemPage.tsx`:

6. **TODO 6:** wrap only `<SimilarItems>` in an `ErrorBoundary` with `resetKey={itemId}`. Its fallback is a `<div role="alert">` holding `Similar items are taking a break.` and a **Try again** button that calls `reset`. The heading, price and **Add to cart** stay outside it.

React logs every error a boundary catches with `console.error`. The checks hide those logs.

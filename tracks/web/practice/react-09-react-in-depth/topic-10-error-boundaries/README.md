# When things break: error boundaries

Stop one broken review from blanking Golu's whole page.

`src/Reviews.jsx` crashes on a review with no text. **Don't fix it.** The boundary has to protect the page from bugs nobody has found yet, too.

In `src/ErrorBoundary.jsx`:

1. **TODO 1:** add `static getDerivedStateFromError(error)`, returning `{ error }`.
2. **TODO 2:** add `componentDidCatch(error)`, which calls the `onError` prop with the error, if there is one.
3. **TODO 3:** while there is an error, render `<div role="alert">` with the `fallback` prop as its message, or `Something went wrong.` when there is no `fallback`. With no error, render the children.
4. **TODO 4:** inside that `<div>`, a **Try again** button clears the error so the children render again.

In `src/ItemPage.jsx`:

5. **TODO 5:** wrap only `<Reviews>` in an `ErrorBoundary` whose fallback is `Reviews are taking a break. You can still buy Golu.` The heading, price and Add to cart button stay outside it.

React logs every error a boundary catches with `console.error`. The checks hide those logs.

# Loading code when it is needed: dynamic import

The checkout's code is big, and every customer downloads it with the first page, even if they only browse. Load it only when someone opens the checkout. Change `src/lazy.js` and `src/routes.js`. `src/catalogue.js` and `src/checkout.js` are finished.

In `src/lazy.js`:

1. **TODO 1:** `lazy(loader)` returns a `load()` function. The first call runs `loader()` and keeps its promise. Every later call returns that same promise, so the code downloads once.
2. **TODO 2:** if the download fails, forget the promise (so the next `load()` tries again) and pass the error on.
3. **TODO 3:** `showLazyView(outlet, load, params)` puts `LOADING` in the outlet, waits for `load()`, then draws `module.default(params)` with `replaceChildren`.
4. **TODO 4:** if loading fails, it shows `FAILED` instead. It never throws.

In `src/routes.js`:

5. **TODO 5:** remove the static `import checkoutView from "./checkout.js"`. Give the `/checkout` route `load: lazy(() => import("./checkout.js"))` instead of `view`.

A check reads `src/routes.js` as text (without its comments) to make sure `checkout.js` is no longer imported at the top.

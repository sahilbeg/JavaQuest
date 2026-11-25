# Fetching data into the store

The catalogue and the item screens both need the item list. Load it into the store once, and let every screen read it from there. Change only `src/items.js`. `src/store.js` is finished.

The store's `items` slice looks like `{ status, data, error }`. It starts as `initialItems`: `{ status: "idle", data: [], error: null }`.

1. **TODO 1:** when a request starts, set `status` to `"loading"` and `error` to `null`, keeping `data`.
2. **TODO 2:** fetch `"/api/items"` inside `try...catch`, and throw when `response.ok` is false. On success, set the slice to `{ status: "ready", data, error: null }`. On any failure, set `status` to `"error"` and `error` to `FAILED`, and keep the old `data`. `load` must never throw.
3. **TODO 3:** `load()` does nothing (returns `Promise.resolve()`) when the items are already `"ready"`.
4. **TODO 4:** while a request is in flight, every `load()` returns that same promise. Once it settles, forget it, so a load after an error tries again.
5. **TODO 5:** `selectItem(state, id)` returns the loaded item with that id, or `null`.

Always update the state immutably: `store.setState((state) => ({ ...state, items: { ...state.items, status: "loading" } }))`.

The checks use a pretend server that can be slow, broken or offline.

# Typed custom hooks

Write the "remember it in localStorage" code once, as a typed hook, so the wishlist survives a refresh.

1. **TODO 1** in `src/useToggle.ts`: `useToggle(initial = false)` keeps a boolean in state and returns `[on, toggle]` as a `readonly [boolean, () => void]` tuple. Each call of `toggle()` flips the value.
2. **TODO 2** in `src/useStoredState.ts`: `useStoredState<T>(key, initial, isValid?)` starts from what is saved in `localStorage` under `key`, as JSON. Read it only once, with a lazy initializer: `useState<T>(() => ...)`. Use `initial` when:
   - nothing is saved under `key`,
   - the saved text is broken JSON,
   - `isValid` is given and says the saved value has the wrong shape.
3. **TODO 3** in `src/useStoredState.ts`: return `[value, save]`, where `save(next)` writes `JSON.stringify(next)` under `key` and updates the state.

`src/WishList.tsx` already uses both hooks: the hearts are `Save <item name>` buttons with `aria-pressed`, and **Show saved** lists the saved ids. Don't change it.

`src/main.tsx` shows the wishlist in the preview, with a button that remounts it like a page refresh.

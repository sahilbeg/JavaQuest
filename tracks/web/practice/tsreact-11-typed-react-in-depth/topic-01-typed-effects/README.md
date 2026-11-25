# Typed effects

Make Escape close Golu's quick view, and make the cart reminder appear after a quiet wait.

**Quick view** (`src/QuickView.tsx`)

1. **TODO 1:** while `open` is true, listen for `keydown` on `window`. When the key is `Escape`, call `onClose()`. Use a named handler, typed `(event: KeyboardEvent)`.
2. **TODO 2:** return a cleanup that removes that same handler, so a closed (or removed) quick view stops listening.

**Cart reminder** (`src/CartNudge.tsx`)

3. **TODO 3:** when `count` is above 0, start a `setTimeout`. After `delayMs` it calls `setNudgedAt(count)`, which shows `Still thinking? Your 2 items are saved in the cart.` Type the id as `ReturnType<typeof setTimeout>`.
4. **TODO 4:** return a cleanup that clears the timer. When `count` changes, the wait starts again from zero. A cart with 0 items never shows the reminder.

Put each `useEffect` above the component's `if (...) return null;` line: hooks always run, in the same order, on every render.

`src/main.tsx` shows both in the preview. The checks don't use it.

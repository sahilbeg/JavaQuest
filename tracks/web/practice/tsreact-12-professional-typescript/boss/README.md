# INC-12: The mock TypeScript interview loop

Arjun runs your mock loop. He hands you a previous candidate's answers to three rounds. Each one type-checks, and each one passed the candidate's own quick try. Each one failed the round. Fix them the way an interviewer expects.

`types.ts` is ready: `CartLine` (`{ id, name, paise, qty }`), `MAX_QTY` (5) and the guard `isCartLines`.

## Bronze: the TypeScript round (`src/mergeCarts.ts`, TODO 1)

When a customer logs in, `mergeCarts(saved, current)` combines the cart saved on the server with the cart in this browser.

- One line per item `id`. If an item is in both carts, the quantities add up, but never above `MAX_QTY`.
- The browser's line is newer, so its `name` and `paise` win when an item is in both.
- Saved lines come first, in their order, then the lines that are only in the browser's cart, in theirs.
- A line with `qty` 0 or less is ignored.
- Neither cart is changed. Build new line objects.

## Silver: the React round (`src/useStoredState.ts`, TODOs 2–3)

`useStoredState(key, initial, isValid)` works like `useState`, but remembers the value in `localStorage`. It returns `[value, save]`.

- **TODO 2:** on the first render, read `localStorage.getItem(key)`. Nothing stored, text that isn't JSON, or a parsed value that `isValid` refuses all give `initial`. Don't trust the stored value with `as`.
- **TODO 3:** `save(next)` sets the value **and** writes `JSON.stringify(next)` under `key`. When `key` changes (another customer logs in), read that key's stored value the same way.

## Gold: the type design round (`src/eventBus.ts`, TODOs 4–5)

`createEventBus<Events>()` returns `{ on, once, emit }`, typed so each event name has its own payload type.

- **TODO 4:** `on(type, handler)` returns a function that unsubscribes exactly that one subscription. Calling it twice is harmless. The same function can subscribe twice; each unsubscribe removes one subscription. `emit(type, payload)` calls every handler for `type`, in the order they subscribed. A handler that unsubscribes itself while `emit` runs must not make the next handler miss that event.
- **TODO 5:** `once(type, handler)` runs `handler` for the next event of that type only. It returns an unsubscribe function, so it can be cancelled before it fires.

Everything must type-check in strict mode.

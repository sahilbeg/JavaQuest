# Optimistic updates and undo

Make the cart feel instant: show each change at once, save it in the background, and put things back if the server says no. Change only `src/cartSync.js`. `src/store.js` is finished.

The state looks like `{ lines: [{ id, qty }], undo: null, error: null }`.

1. **TODO 1:** `insertAt(lines, line, index)` returns a **new** array with `line` put in at `index`.
2. **TODO 2:** `changeQty(store, id, qty)` shows the new qty straight away, with the `setQty` helper that is already there.
3. **TODO 3:** it then sends `PATCH /api/cart/<id>` with the new qty as a JSON body, such as `{ "qty": 3 }` (use `JSON.stringify({ qty })`). If the request throws or `response.ok` is false, put **that line's** old qty back and set `error` to `SAVE_FAILED`. It never throws.
4. **TODO 4:** `removeWithUndo(store, id)` takes the line out at once and sets `undo` to the id, so the page can show "Removed. Undo".
5. **TODO 5:** when the timer ends (5 seconds), set `undo` back to `null` and send `DELETE /api/cart/<id>`. If it fails, put the line back where it was with `insertAt`, and set `error` to `REMOVE_FAILED`.
6. **TODO 6:** `undo()` puts the line back where it was and sets `undo` to `null`. Nothing is sent to the server.

The timer, `finish()` and the `open` flag are already written. The checks use fake timers, so they don't really wait five seconds.

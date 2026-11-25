# Testing a single-page app

Build the team's test kit for the SPA, in `src/testkit.js`. `src/app.js` is a finished, small version of the store. `test/testkit.test.js` checks your kit, and `test/app.test.js` uses your kit to test the app, so read it to see the kit in action.

1. **TODO 1:** `visit(path)` changes the address with `history.replaceState(null, "", path)` and fires one `popstate` event on `window`, like Back or a deep link.
2. **TODO 2:** `fakeServer(answers)` returns `{ fetch, calls }`. `fetch(path)` records the path in `calls`. For a path in `answers` it gives `{ ok: true, status: 200 }` with a `json()` that gives `answers[path]`. For any other path it gives `ok: false`, status `404`, and `{ error: "Not found" }` from `json()`.
3. **TODO 3:** `waitFor(check, timeout = 1000)` calls `check()` straight away and then every 10 ms. It resolves with the first truthy value. After `timeout` ms it rejects with `new Error("waitFor timed out after 50 ms")` (with the real timeout in place of 50).
4. **TODO 4:** `textOf(element)` gives the element's `textContent` with every run of spaces and new lines turned into one space, and the ends trimmed.
5. **TODO 5:** `reset()` empties `document.body`, clears `localStorage` and sets the address back to `/`.

`waitFor` uses real timers here, and the checks wait at most a fraction of a second.

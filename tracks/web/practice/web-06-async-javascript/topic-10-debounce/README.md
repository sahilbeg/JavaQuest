# Debounce: searching as you type

The search box sends a request on every key press. Make it wait for a pause, and show only the newest answer. Change `src/search.js`.

**`debounce(fn, ms)`** returns a new function. Each call to it cancels the waiting call and starts a fresh wait. When `ms` milliseconds pass with no new call, `fn` runs once, with the arguments of the last call.

1. **TODO 1:** keep a `timer` variable inside `debounce`, outside the function you return. Each debounced function gets its own timer.
2. **TODO 2:** return `(...args) => { ... }` that calls `clearTimeout(timer)`, then `timer = setTimeout(() => fn(...args), ms)`.

**`createSearch(show, ms = 300)`** returns `onType(text)`, which the page calls on every `input` event. The server answers `GET /api/catalogue?q=golu` with a list of matching items such as `[{ "id": "DC-1042", "name": "Golu the elephant" }]`.

3. **TODO 3:** keep a counter for the newest search inside `createSearch`.
4. **TODO 4:** return `debounce(async (text) => { ... }, ms)`. Inside, count this search and trim the text.
5. **TODO 5:** for empty text, call `show([])` without asking the server. Otherwise fetch `` `/api/catalogue?${new URLSearchParams({ q })}` ``.
6. **TODO 6:** if this is still the newest search, call `show` with the list of names, such as `["Golu the elephant"]`.

Press **Submit** in Cadence, or save the file in your editor. The checks use pretend timers and a pretend search API.

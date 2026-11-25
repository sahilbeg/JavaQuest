# INC-10: The mock interview loop, no framework allowed

Arjun runs your mock loop and plays the interviewer. He hands you a previous candidate's answers to three rounds. Each one passed the candidate's own quick try, and each one failed the round. Fix them the way an interviewer expects.

## Bronze: the JavaScript round (`src/promiseAll.js`, TODO 1)

"Write your own `Promise.all`." What Arjun saw when he tried the candidate's `promiseAll`:

- With three stock requests, the results came back in the order the **answers arrived**, not the order they were asked for.
- `promiseAll([])` never finished.
- `promiseAll([45000, somePromise])` crashed on the plain number.
- When one request failed, the caller waited forever.

`promiseAll(values)` must return a promise that:

- resolves with an array of every value's result, **in the order of `values`**;
- resolves with `[]` straight away for an empty array;
- treats a plain value like `45000` as already answered;
- treats an answer of `0` like any other answer;
- rejects with the first error as soon as any promise rejects, without waiting for the others.

## Silver: the DOM round (`src/starRating.js`, TODOs 2–4)

"Build a star rating for Golu's review form." `createStarRating(root, { label, value, onChange })` draws five `span.star` elements and returns `{ getValue }`. Clicking already works. A keyboard can't reach it, and a screen reader hears five stars with no meaning. Make it a proper **radio group**:

- **TODO 2:** `root` gets `role="radiogroup"` and `aria-label` set to `label`. Each star gets `role="radio"` and an `aria-label`: `"1 star"`, `"2 stars"`, ... `"5 stars"`.
- **TODO 3:** in `render()`, each star's `aria-checked` is `"true"` only for the chosen star, `"false"` for the others. Use a **roving tabindex**: `tabIndex = 0` on the chosen star, or on the first star when nothing is chosen (`0`), and `-1` on every other star.
- **TODO 4:** one `keydown` listener on `root`, working from the focused star:
  - **ArrowRight** and **ArrowDown** choose the next star; 5 wraps to 1.
  - **ArrowLeft** and **ArrowUp** choose the star before; 1 wraps to 5.
  - **Space** (`" "`) chooses the focused star.
  - For these keys only: call `event.preventDefault()`, choose the new star, and move focus to it. Other keys do nothing.

`choose` already calls `onChange` only when the rating really changes.

## Gold: the frontend design round (`src/cachedFetch.js`, TODOs 5–6)

"Product pages ask for the same stock again and again. Put a small cache in front of the API." `createCachedFetcher(fetchJson, { maxAgeMs, now })` returns `{ get, clear }`. `get(url)` returns a promise of the data.

The candidate's version already shares one request between callers who ask at the same time. What goes wrong:

- **TODO 5:** after one failed request (say, an HTTP 503 on sale morning), every later `get` for that address gets the same old error, and the server is never asked again. A failed request must not stay cached: the next `get` after a failure asks the server again. Every caller already waiting still gets the same error. And an old failure must never throw away a **newer** request for the same address that replaced it.
- **TODO 6:** stock is never fetched again, however old it is. Measure an entry's age from when its request **started** (read the clock with `now()` at that moment). An entry is fresh while its age is **less than** `maxAgeMs`. At `maxAgeMs` or older, `get` starts a new request and caches that one instead.

`now` is passed in, so the checks can move the clock without waiting.

# Race conditions and AbortController

Stop old PIN checks from overwriting new ones, and stop a check from spinning forever. Change `src/delivery.js`.

**`createDeliveryChecker(show)`** returns `check(pin)`. It already asks the server and calls `show` with `Delivery to 411045 in 2 days` or `Sorry, we don't deliver to 560001 yet`. The bug: every answer is shown, even a late one for a PIN the customer already fixed.

1. **TODO 1:** inside `createDeliveryChecker` but outside `check`, keep a counter starting at 0. Each checker gets its own counter.
2. **TODO 2:** at the start of `check`, add one to the counter and remember this check's own number.
3. **TODO 3:** after the answer arrives, if the counter has moved on (a newer check started), return without calling `show`.

**`fetchWithTimeout(url, ms)`** works like `fetch(url)` but gives up after `ms` milliseconds.

4. **TODO 4:** make a `new AbortController()` and start a `setTimeout` that calls `controller.abort()` after `ms`.
5. **TODO 5:** `fetch(url, { signal: controller.signal })` and return the response. When the time runs out, fetch rejects with an `AbortError`; let that error reach the caller.
6. **TODO 6:** clear the timer with `clearTimeout` in a `finally` block, so it never fires once the answer is in.

Press **Submit** in Cadence, or save the file in your editor. The checks use a pretend server that answers in whatever order the test chooses, and pretend timers.

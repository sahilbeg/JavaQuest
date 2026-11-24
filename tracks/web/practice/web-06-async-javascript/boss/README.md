# INC-06: Slow 3G shows the wrong stock

The quick-view panel shows the wrong stock when answers arrive out of order, keeps old requests running, and has no loading or error state. Fix `createStockView(el)` in `src/stockView.js`. `LOADING`, `FAILED` and `stockText(stock)` are already written.

**Bronze** (resolves the incident): only the newest request may change the panel.

1. **TODO 1:** inside `createStockView`, keep a counter for the newest request. Each panel gets its own.
2. **TODO 2:** at the start of `show`, add one to the counter and remember this request's number. After the answer arrives, return without touching `el` if a newer request has started.

**Silver:** cancel the old request.

3. **TODO 3:** keep the current `AbortController` next to the counter, starting as `null`.
4. **TODO 4:** at the start of `show`, abort the previous controller (if there is one), make a `new AbortController()`, and call `fetch(url, { signal: controller.signal })`. An aborted request must not throw out of `show` or show an error.

**Gold:** loading and error states.

5. **TODO 5:** before asking, set `el.textContent = LOADING` and `el.dataset.state = "loading"`. With an answer, set the state to `"ready"`.
6. **TODO 6:** wrap the request in `try...catch` and throw when `response.ok` is false. In `catch`, return quietly if this request is no longer the newest, or if `error.name === "AbortError"`. Otherwise set `el.textContent = FAILED` and `el.dataset.state = "error"`. `show` must never throw.

Press **Submit** in Cadence, or save the file in your editor. The checks use a pretend slow server that answers, fails or drops the connection in whatever order each check chooses.

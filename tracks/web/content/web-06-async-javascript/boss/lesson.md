## What happened

The catalogue has a quick-view panel. Tap an item, and the panel asks `GET /api/stock/<id>` and shows the answer. `createStockView(el)` in `stockView.js` returns the `show(id)` function behind it.

On fast Wi-Fi it is fine. On Saturday's 3G, three things went wrong:

1. A customer tapped Golu (`DC-1042`, sold out), then the coaster set (`DC-2001`, 12 left). Golu's answer was slower and arrived last, so the coaster set's panel said **Sold out**.
2. Every tap left the old request running, using the customer's data for an answer nobody wanted.
3. While waiting, the panel kept showing the previous item's stock. When the API failed, it showed `undefined in stock`, or nothing changed at all.

```animate
title: Two taps, one panel
kind: scene
nodes:
  - { id: panel, label: Quick-view panel, emoji: "🪟", value: empty }
  - { id: api, label: dhaaga.in API, emoji: "🖥️", value: idle }
steps:
  - say: The customer taps Golu. The panel asks for DC-1042, and on 3G the answer is slow.
    send: { from: panel, to: api, label: "GET /api/stock/DC-1042" }
    set: { api: "1 waiting" }
  - say: A second later she taps the coaster set. A second request goes out.
    send: { from: panel, to: api, label: "GET /api/stock/DC-2001" }
    set: { api: "2 waiting" }
  - say: The coaster set's answer is quick. The panel shows 12 in stock. So far, so good.
    send: { from: api, to: panel, label: "stock: 12", back: true }
    set: { panel: "12 in stock", api: "1 waiting" }
  - say: Then Golu's slow answer lands, and the panel shows it, under the coaster set's photo.
    send: { from: api, to: panel, label: "stock: 0", back: true }
    set: { panel: "Sold out", api: idle }
  - say: The customer sees a sold-out coaster set and leaves. The old answer won the race.
    focus: [panel]
```

## What you need

This builds on the loading and error states from topic 07 and the race fixes from topic 09. The texts and `stockText(stock)` are already in the file:

| State | `el.textContent` | `el.dataset.state` |
|---|---|---|
| Waiting for the server | `Checking stock...` (`LOADING`) | `"loading"` |
| The answer arrived | `stockText(stock)`: `12 in stock`, or `Sold out` for 0 | `"ready"` |
| The newest request failed | `Couldn't check stock. Try again.` (`FAILED`) | `"error"` |

The server answers `GET /api/stock/DC-2001` with `{ "id": "DC-2001", "stock": 12 }`. For a broken server it answers `500`, and on a dropped connection `fetch` rejects.

```callout tip
Keep the counter and the controller inside `createStockView`, next to each other. Each panel gets its own, and every call to its `show` shares them.
```

## The tiers

1. **Bronze** resolves the incident: only the newest request may change the panel. Number every request, and ignore any answer from an older one, whether it arrives first or last.
2. **Silver**: cancel the old request. Every `fetch` gets a signal from a fresh `AbortController`, and starting a new request aborts the previous one. The aborted request must not throw out of `show` or show an error.
3. **Gold**: show `LOADING` with `data-state="loading"` the moment `show` is called. Throw when `response.ok` is false. When the newest request fails, show `FAILED` with `data-state="error"`, and never throw out of `show`. A failure from an **older** request must never overwrite the newest answer, or flash an error while the newest one is still loading.

## Recap

- Answers can arrive in any order. When they compete for one spot on the page, only the newest request may write to it.
- Aborting the old request saves the customer's data, but the abort shows up as an `AbortError` that must be caught quietly.
- Guard the `catch` as carefully as the success path: an old failure is as stale as an old answer.
- Every request needs a visible loading state and a friendly error state, so the panel is never blank or wrong.

## Common mistakes

**1. Checking for stale answers only on the success path.** The old request fails later, the `catch` runs, and the error message covers the right answer. Check the request number in `catch` too.

**2. Treating an abort as a failure.** Every tap aborts the previous request, so the panel flashes "Couldn't check stock" all the time. Return quietly when `error.name === "AbortError"`.

**3. Keeping the counter at the top of the file.** Two panels on one page then share one counter, and each one ignores the other's answers. Keep it inside `createStockView`.

**4. Reusing one AbortController.** Once aborted, its signal stays aborted, so every later request fails at once. Make a new controller for every request.

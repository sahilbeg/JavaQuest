# INC-10: The mock frontend interview loop

Arjun runs your mock loop. He hands you the answers a previous candidate gave in three rounds. Each one passed the candidate's own quick try. Each one failed the round. Fix them the way an interviewer expects.

## Bronze: the JavaScript round (`src/groupOrdersByDay.js`, TODO 1)

`groupOrdersByDay(orders)` takes orders like `{ id: "order-17", placedAt: "2026-11-08T10:15:00+05:30", paise: 45000 }`. `placedAt` is written in India time.

It returns one entry per day, `{ day, count, totalPaise }`, **oldest day first**. `day` is the date part of `placedAt`, such as `"2026-11-08"`.

| Input | Answer |
|---|---|
| `[]` | `[]` |
| order-17 alone | `[{ day: "2026-11-08", count: 1, totalPaise: 45000 }]` |
| order-17 and order-18 (both on 8 November) | `[{ day: "2026-11-08", count: 2, totalPaise: 60000 }]` |
| order-20, placed `"2026-11-09T02:10:00+05:30"` | `[{ day: "2026-11-09", count: 1, totalPaise: 12000 }]` |

The candidate's answer has four bugs: the loop skips the first order, each order overwrites its day instead of adding to it, `toISOString()` uses UTC (so an order at 02:10 in India lands on the day before), and the days come out in the order they were first seen. Don't change the array you're given.

## Silver: the UI round (`src/Tabs.jsx`, TODOs 2–4)

`<Tabs label="About Golu" tabs={[{ id, label, content }, ...]} />` must follow the tabs pattern:

- A `role="tablist"` with `aria-label={label}`.
- One `<button role="tab">` per tab, with `id="tab-<id>"`, `aria-controls="panel-<id>"` and `aria-selected` (`"true"` only on the selected tab). The first tab starts selected.
- A **roving tabIndex**: the selected tab has `tabIndex={0}`, every other tab `tabIndex={-1}`. So Tab stops on one tab only.
- One `role="tabpanel"` per tab, with `id="panel-<id>"`, `aria-labelledby="tab-<id>"` and `tabIndex={0}`. Only the selected tab's panel shows; the others have the `hidden` attribute.
- Clicking a tab selects it.
- With focus on a tab: **ArrowRight** selects and focuses the next tab, wrapping from the last to the first. **ArrowLeft** goes the other way, wrapping from the first to the last. **Home** and **End** go to the first and last tab. Moving always selects **and** focuses the tab.

## Gold: the React round (`src/useFetch.js`, TODOs 5–7)

`useFetch(url)` returns `{ status, data, error }`, where `status` is `"loading"`, `"success"` or `"error"`.

- It starts as `{ status: "loading", data: null, error: null }`, and goes back to exactly that whenever `url` changes.
- A JSON answer gives `{ status: "success", data, error: null }`.
- An answer whose `ok` is false gives `{ status: "error", data: null, error: "HTTP 500" }` (with the real status number).
- A fetch that fails gives `status: "error"` and the failure's `message` as `error`.
- Each fetch gets the `signal` of its own `AbortController`. The effect's cleanup aborts it, so changing `url` or unmounting cancels the old request. The `AbortError` that follows is **not** an error: ignore it.

The checks use a pretend server, so nothing touches the network.

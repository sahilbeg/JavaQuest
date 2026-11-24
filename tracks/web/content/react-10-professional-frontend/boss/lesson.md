## What happened

Arjun runs your mock loop today, and he plays the interviewer. He hands you the answers a previous candidate gave in three rounds. Each answer worked when the candidate tried it once. Each one still failed the round.

1. **The JavaScript round.** "Group the orders by the day they were placed, with a count and a total for each day." The answer has four bugs that only show on edge cases.
2. **The UI round.** "Build tabs for Golu's product page: Description, Care, Delivery." The answer works with a mouse, and with nothing else.
3. **The React round.** "Write a `useFetch(url)` hook with loading and error states." The answer works on a fast, perfect network, and shows the wrong product on a slow one.

Your job: fix each answer the way an interviewer expects to see it fixed.

```callout jargon
A **mock loop** is a practice interview loop, run by a colleague who plays the interviewers. It feels real, but nothing rides on it except what you learn.
```

### Why the first answer fails

The candidate tried one order in the afternoon. That single try couldn't show any of the four bugs:

| Input | Right answer | The candidate's answer |
|---|---|---|
| order-17 alone | one day, count 1 | nothing: the loop starts at 1 |
| order-17 and order-18, same day | count 2, 60000 paise | count 1, 15000 paise: the second overwrites the first |
| order-20, at 02:10 on 9 November | day `2026-11-09` | day `2026-11-08`: UTC |
| orders from 8, 7 and 9 November | oldest day first | the order they were first seen |

The third bug is the sneakiest. India is 5 hours 30 minutes ahead of UTC, and `toISOString()` always answers in UTC:

```animate
title: One order, two different days
kind: code
language: js
code: |
  const placedAt = "2026-11-09T02:10:00+05:30";
  const utcDay = new Date(placedAt).toISOString().slice(0, 10);
  const indiaDay = placedAt.slice(0, 10);
  console.log(utcDay);
  console.log(indiaDay);
steps:
  - line: 1
    vars: { placedAt: '"2026-11-09T02:10:00+05:30"' }
    say: A customer ordered at 02:10 on 9 November, India time. The +05:30 says how far ahead of UTC that is.
  - line: 2
    vars: { utcDay: '"2026-11-08"' }
    say: toISOString answers in UTC. 02:10 in India is 20:40 the evening before in UTC, so the day goes back to 8 November.
  - line: 3
    vars: { indiaDay: '"2026-11-09"' }
    say: The date written in placedAt is already India's date. Its first ten characters are the right day.
  - line: 4
    out: "2026-11-08"
    say: Grouped this way, a late-night Diwali order lands in the wrong day's report.
  - line: 5
    out: "2026-11-09"
    say: Grouped by the written date, it counts on the day the customer really ordered.
```

### Why the third answer shows the wrong stock

The candidate's hook never cancels anything. When the customer moves from Golu's page to the tote bag's, both requests are still running, and whichever answers **last** wins.

```animate
title: Two requests race on slow 3G
kind: scene
nodes:
  - { id: page, label: Product page, emoji: "📱", value: "loading", row: 1, col: 0 }
  - { id: golu, label: "GET DC-1042", emoji: "🐘", value: slow, row: 0, col: 3 }
  - { id: tote, label: "GET DC-1301", emoji: "👜", value: fast, row: 2, col: 3 }
steps:
  - say: The customer opens Golu's page. useFetch starts the first request on a slow connection.
    send: { from: page, to: golu, label: "fetch" }
  - say: Before it answers, they tap the tote bag. A second request starts.
    send: { from: page, to: tote, label: "fetch" }
  - say: The tote bag's answer arrives first, and the page shows 2 in stock. So far, so good.
    send: { from: tote, to: page, label: "stock 2", back: true }
    set: { page: "tote: 2 in stock" }
  - say: Then Golu's old answer finally lands, and overwrites it. The tote bag's page now shows Golu's stock.
    send: { from: golu, to: page, label: "stock 7", back: true }
    set: { page: "tote: 7 in stock?!" }
    focus: [page]
  - say: With an AbortController, changing the url cancels Golu's request in the cleanup. Its answer never arrives.
    set: { golu: aborted, page: "tote: 2 in stock" }
```

## What you need

You learned each idea in an earlier ticket. Here they meet in one incident.

- Testing by hand, edge cases and the interview method: [Frontend interviews](/t/react-10-professional-frontend/topic-09-interviews).
- Grouping with a `Map` and sorting: [Array methods](/t/web-04-javascript-in-depth/topic-03-array-methods) and [Map and Set](/t/web-04-javascript-in-depth/topic-11-maps-and-sets).
- Dates and time zones: [Dates and times](/t/web-04-javascript-in-depth/topic-10-dates).
- Roles, `aria-*`, focus and refs: [Accessibility in depth](/t/react-10-professional-frontend/topic-02-accessibility-in-depth) and [Refs](/t/react-09-react-in-depth/topic-08-refs).
- `AbortController` and races: [Race conditions and AbortController](/t/web-06-async-javascript/topic-09-races-and-abort), and effect cleanups in [Effects with useEffect](/t/react-09-react-in-depth/topic-01-effects).

### The tabs pattern

```callout jargon
A **roving tabIndex** lets a group of controls share one Tab stop. The active one has `tabIndex={0}`, the rest `tabIndex={-1}`, and the arrow keys move between them. Tab then leaves the group in one press.
```

Tabs follow the WAI-ARIA Authoring Practices pattern. Without a roving tabIndex, a keyboard user must press Tab through every tab to reach the content. With it, Tab lands on the selected tab, the arrows choose, and the next Tab goes straight into the panel. Moving with an arrow both **selects** the tab and **focuses** it, so the screen reader announces the new one.

### What the gold checks do

The checks replace `fetch` with a pretend server. Each request waits until the check answers it, so the checks decide which answer arrives first. An aborted request rejects with an `AbortError`, just like the real `fetch`. The checks change the url while the first request is still waiting, then check that it was aborted, that nothing showed an error, and that only the second product's data appears.

## The tiers

1. **Bronze** resolves the incident. `groupOrdersByDay` counts every order, adds up orders on the same day, takes the day from `placedAt` in India time, sorts the days oldest first, and leaves its input alone.
2. **Silver**: the UI round. A labelled `tablist`, tabs with `aria-selected` and a roving `tabIndex`, one visible `tabpanel` named by its tab, and ArrowLeft, ArrowRight, Home and End that select and focus.
3. **Gold**: the React round. Loading, success and error states, `HTTP <status>` for answers that aren't ok, back to loading on a new url, and an `AbortController` that cancels on url change and unmount, without reporting the abort as an error.

The exact rules are in the README.

## Recap

- Test by hand before you say "done": empty, one item, two on the same day, the boundaries of the day, and the order of the output.
- `toISOString()` is always UTC. A date written with `+05:30` already carries India's day in its first ten characters.
- Accessible tabs need roles, `aria-selected`, `aria-controls`, a roving `tabIndex`, and arrow keys that move both selection and focus.
- A fetching hook needs all three states, must treat `!response.ok` as an error, and must reset when its input changes.
- Cancel stale requests with an `AbortController` in the effect's cleanup, and ignore the `AbortError` it causes.

## Common mistakes

**1. Using toISOString for the day.**

```js
const day = new Date(order.placedAt).toISOString().slice(0, 10);
```

Symptom: the bronze check says order-20 should be on 9 November, but got 8 November. Fix: `order.placedAt.slice(0, 10)`, the date as written in India time.

**2. Sorting by the wrong thing, or not at all.**

```js
const days = Object.values(groups);   // never sorted
```

Symptom: the days come out as 8, 7, 9 November, the order they were first seen. Fix: sort by `day` at the end. Dates written as `YYYY-MM-DD` sort correctly as text.

**3. Arrow keys that select but don't focus.**

```jsx
setSelected(next);   // focus stays on the old tab
```

Symptom: the silver check says ArrowRight should also move focus to Care. Pressing the arrow twice does nothing more, because the key goes to the old tab. Fix: keep each button in a ref array and call `tabRefs.current[next].focus()`.

**4. Every tab in the Tab order.**

```jsx
<button role="tab" aria-selected={index === selected}>
```

Symptom: the second Tab lands on Care instead of the panel. Fix: `tabIndex={index === selected ? 0 : -1}` on each tab, and `tabIndex={0}` on the panel.

**5. Aborting, but reporting the abort as an error.**

```js
fetch(url, { signal: controller.signal })
  .then((response) => response.json())
  .catch((error) => setState({ status: "error", data: null, error: error.message }));
```

Symptom: the gold check says an aborted request is not an error. In the app, every product change would flash an error message for a moment. Fix: `if (error.name === "AbortError") return;` before setting the error state.

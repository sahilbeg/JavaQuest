## The design

Every item in the catalogue gets a small badge next to its name. The page marks each badge with the item it belongs to:

```html
<ul id="catalogue">
  <li>Golu the elephant <span class="badge" data-item="DC-1042"></span></li>
  <li>Marigold coaster set <span class="badge" data-item="DC-2001"></span></li>
  <li>Indigo tote bag <span class="badge" data-item="DC-3001"></span></li>
</ul>
```

The badge's text and colour come from the stock number:

| Stock | Badge text | `data-level` |
|---|---|---|
| 0 | `Sold out` | `out` |
| 1 to 3 | `Only 2 left` (with the real number) | `low` |
| 4 or more | `In stock` | `ok` |

The design colours each level in CSS with `.badge[data-level="low"]` and friends, so your code only sets the text and the attribute.

The badges stay fresh by asking again every 30 seconds. This is called **polling**.

```callout jargon
**Polling** means asking a server the same question again and again on a timer, to notice when the answer changes. It is simple and works everywhere. Bigger sites sometimes have the server push changes instead, but polling every 30 seconds is plenty for a crochet shop.
```

```animate
title: Badges that stay fresh
kind: scene
nodes:
  - { id: page, label: Catalogue badges, emoji: "🏷️", value: empty }
  - { id: timer, label: 30 s timer, emoji: "⏱️", value: idle }
  - { id: api, label: Stock API, emoji: "🖥️", value: idle }
steps:
  - say: The page opens. It asks for all three items at once with Promise.all, straight away.
    send: { from: page, to: api, label: "3 stock requests" }
    set: { api: busy }
  - say: The answers come back together. Each badge gets its text and level.
    send: { from: api, to: page, label: "2, 12, 0", back: true }
    set: { page: "Only 2 left / In stock / Sold out", api: idle, timer: "30 s left" }
  - say: Thirty seconds later the timer rings, and the page asks again.
    send: [{ from: timer, to: page, label: refresh }, { from: page, to: api, label: "3 stock requests" }]
  - say: Someone at the Aundh shop bought the last two Golus. The new answer changes one badge.
    send: { from: api, to: page, label: "0, 12, 0", back: true }
    set: { page: "Sold out / In stock / Sold out" }
  - say: If a refresh fails, the badges keep their last good answer, and the next ring tries again.
    set: { timer: "30 s left" }
    focus: [page]
```

```callout tip
Keep the last good badges when a refresh fails. A badge that says "Only 2 left" from 30 seconds ago is far more useful than a blank one, or an error on every item.
```

## What you build

Everything goes in `badges.js`. Build it bottom-up, small pieces first.

1. `badgeText(stock)` returns `Sold out`, `Only N left` or `In stock` from the table above.
2. `badgeLevel(stock)` returns `out`, `low` or `ok`.
3. `fetchStock(ids)` asks `GET /api/stock/<id>` for every id **at the same time** and returns an object such as `{ "DC-1042": 2, "DC-2001": 12 }`. If any answer isn't ok, it rejects.
4. `renderBadges(root, stockById)` finds every `[data-item]` element inside `root` and sets its text and `data-level`. A badge whose id isn't in `stockById` is left alone.
5. `startLiveStock(root, ms = 30000)` reads the ids from the badges, refreshes them at once, then again every `ms` milliseconds. A failed refresh keeps the old badges and never throws. It returns a `stop()` function that ends the polling.

### Building an object from two lists

`Promise.all` gives you the stocks in the same order as the ids. To turn the two lists into one object, walk the ids with their index:

```js
function pair(ids, stocks) {
  const byId = {};
  ids.forEach((id, i) => {
    byId[id] = stocks[i];
  });
  return byId;
}
```

`pair(["DC-1042", "DC-2001"], [2, 12])` gives `{ "DC-1042": 2, "DC-2001": 12 }`.

## Recap

- Ask for every item at once with `Promise.all`, and pair the answers with their ids by position.
- Keep the badge rules in small pure functions, so they are easy to check on their own.
- Find the badges with `querySelectorAll("[data-item]")`, and write the text and `data-level` for each.
- Poll with `setInterval`, refresh once straight away, and return a function that calls `clearInterval`.
- When a refresh fails, keep the last good badges and try again on the next tick.

## Common mistakes

**1. Waiting 30 seconds for the first badges.** `setInterval` waits a whole interval before its first run, so the badges stay empty for half a minute. Call `refresh()` once yourself before starting the interval.

**2. Letting a failed refresh escape.** If `refresh` throws inside `setInterval`, the error goes unhandled and nothing on the page explains it. Wrap it in `try...catch` and keep the old badges.

**3. Asking for each item one by one.** Twenty items on 3G then take twenty round trips every 30 seconds. Use `Promise.all` so they wait together.

**4. Forgetting to stop.** A page that hides the catalogue but keeps polling wastes the customer's data. Return `() => clearInterval(timer)` and call it when the catalogue goes away.

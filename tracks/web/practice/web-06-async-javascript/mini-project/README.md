# FEAT-06: Live stock badges

Give every catalogue item a stock badge that stays fresh while customers browse. Everything goes in `src/badges.js`.

Each badge in the page looks like `<span class="badge" data-item="DC-1042"></span>`. The API answers `GET /api/stock/DC-1042` with `{ "id": "DC-1042", "stock": 2 }`.

1. **TODO 1:** `badgeText(stock)` returns `Sold out` for 0, `Only 2 left` for 1 to 3 (with the real number), and `In stock` for 4 or more.
2. **TODO 2:** `badgeLevel(stock)` returns `out` for 0, `low` for 1 to 3, and `ok` for 4 or more.
3. **TODO 3 and 4:** `fetchStock(ids)` fetches `/api/stock/<id>` for every id **at the same time** with `Promise.all`, and returns an object such as `{ "DC-1042": 2, "DC-2001": 12 }`. If any answer isn't ok, it rejects.
4. **TODO 5:** `renderBadges(root, stockById)` sets `textContent` and `dataset.level` on every `[data-item]` element inside `root`. Leave a badge alone if its id isn't in `stockById`.
5. **TODO 6 to 8:** `startLiveStock(root, ms = 30000)` reads the ids from the badges, refreshes them once straight away, then every `ms` milliseconds with `setInterval`. A failed refresh keeps the old badges and must not throw. It returns a `stop()` function that calls `clearInterval`.

Press **Submit** in Cadence, or save the file in your editor. The checks use pretend timers and a pretend stock API, so 30 seconds pass in an instant.

# Routing with the History API

Swap the hash router for one with clean URLs like `dhaaga.in/items/DC-1042`. Change only `src/router.js`. `src/match.js` is finished: `matchRoute(routes, path)` gives `{ route, params }` or `null`, as in the last ticket.

`createRouter(routes, outlet, notFound)` returns `{ start, stop, navigate }`.

1. **TODO 1:** `render()` matches `location.pathname` and puts `match.route.view(match.params)` in the outlet with `replaceChildren`, or `notFound(path)` when nothing matches.
2. **TODO 2:** `navigate(path)` adds a history entry with `history.pushState(null, "", path)`, then calls `render()`. (pushState never fires an event, so you must draw yourself.)
3. **TODO 3:** `onClick(event)` finds the tapped link with `event.target.closest("a[data-link]")`. Links without `data-link`, and taps with Ctrl, Cmd (`metaKey`), Shift or Alt held, are left to the browser. Otherwise call `event.preventDefault()` and navigate to the link's `href` attribute.
4. **TODO 4:** `start()` draws once, then listens for `popstate` on `window` (Back and Forward) and for `click` on `document`.
5. **TODO 5:** `stop()` removes both listeners.

Back and Forward must only redraw. They must never call `pushState`.

# INC-09: The back button shows the wrong page

Customers press Back and stay on the same page, old flyer links trap them in a loop, and the tab title is wrong. Fix `createRouter` in `src/router.js`. `src/match.js` is fine.

A route has `path`, `title(params)` and `view(params, query)`, where `query` is a `URLSearchParams` for the part after `?`.

**Bronze** (resolves the incident): Back and Forward show the right page.

1. A customer opens the catalogue, taps Golu and presses Back. The address bar says `/items`, but the screen still shows Golu. After Back or Forward, the screen must show the page for the address bar, including after a deep link.

**Silver:** no extra history entries.

2. `navigate` to the address you are already on (path and query) must not call `pushState`.
3. An old address in `REDIRECTS`, such as `/shop`, leaves exactly **one** entry: the new address, `/items`. When the app **starts** on an old address, swap it with `history.replaceState` (no `pushState`), so Back can never return to it.

**Gold:** the title and the filter follow the address.

4. `document.title` is `"<route title> | Dhaaga & Co."` (or `"Page not found | Dhaaga & Co."`) after `start`, `navigate` **and** Back or Forward.
5. A customer filters the catalogue to `?colour=Indigo`, opens Golu and presses Back: the filter is gone. It must come back with Back, and the view gets it as its `query`. A different query is a different address, so it gets its own entry.

`SITE` and `REDIRECTS` are already at the top of the file.

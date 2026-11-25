# Routing with the hash

Give dhaaga.in a router that picks the screen from the part of the URL after `#`. Change only `src/router.js`. The screens in `src/views.js` are finished, and `src/main.js` starts the router for the preview.

A route is an object like `{ path: "/items/:id", view: (params) => element }`.

1. **TODO 1:** `pathFromHash(hash)` turns `"#/items/DC-1042"` into `"/items/DC-1042"`. An empty hash, or just `"#"`, gives `"/"`.
2. **TODO 2:** `matchOne(pattern, path)` splits both on `"/"`. Different lengths never match. A pattern part starting with `:` saves the path part in the params (`:id` saves `params.id`), but only if that part isn't empty. Every other part must be exactly equal. Return the params object, or `null`.
3. **TODO 3:** `matchRoute(routes, path)` tries the routes in order and returns `{ route, params }` for the first match, or `null`.
4. **TODO 4:** inside `startHashRouter`, `render()` matches the current `location.hash` and puts `match.route.view(match.params)` in the outlet with `replaceChildren`. With no match, it shows `notFound(path)`.
5. **TODO 5:** draw once straight away, redraw on every `hashchange` event on `window`, and return a function that removes the listener.

Open the preview and tap the links. Then press the browser's Back button in the preview: the screens should follow.

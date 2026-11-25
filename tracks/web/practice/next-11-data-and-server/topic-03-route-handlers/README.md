# Route handlers: your own API

Give dhaaga.in a small JSON API for Raju's WhatsApp bot and Arjun's restock scanner.

`src/lib/stock.js` and `src/lib/catalogue.js` are done. `getLeft(id)` gives a number of items left (0 is a real answer), or `undefined` for an unknown item. `addStock(id, count)` adds and returns the new count. `findItem(id)` gives `{ id, name, paise }` or `undefined`.

Change `src/app/api/stock/route.js`:

1. **TODO 1:** `GET /api/stock?id=DC-1042` answers `{ "id": "DC-1042", "left": 7 }` with status 200.
2. **TODO 2:** with no `id`, answer 400 `{ "error": "Add ?id= with an item id." }`. For an unknown item, answer 404 `{ "error": "No item DC-9999." }` (with the real id).
3. **TODO 3:** `POST /api/stock` with a JSON body `{ "id": "DC-4410", "add": 5 }` adds the stock and answers 200 `{ "id": "DC-4410", "left": 17 }`.
4. **TODO 4:** a POST whose body isn't JSON answers 400 `{ "error": "The body must be JSON." }`. If `add` isn't a whole number above 0, answer 400 `{ "error": "add must be a whole number above 0." }`. An unknown item answers 404 `{ "error": "No item DC-9999." }`. Check before you change anything.

Change `src/app/api/items/[id]/route.js`:

5. **TODO 5:** `GET /api/items/DC-1042` answers the item, `{ "id": "DC-1042", "name": "Golu the elephant", "paise": 45000 }`, or 404 `{ "error": "No item DC-9999." }`. Remember that `params` is a Promise.

Answer every time with `Response.json(body, { status })`. The checks call your functions with real `Request` objects.

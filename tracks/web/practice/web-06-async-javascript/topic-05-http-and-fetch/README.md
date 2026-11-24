# HTTP and fetch

Golu's page must ask the server for stock instead of showing a fixed "In stock". Change `src/stock.js`.

The server answers `GET /api/stock/DC-1042` with status 200 and the body `{ "id": "DC-1042", "stock": 3 }`. An unknown id gets a 404.

1. **TODO 1:** `stockUrl(id)` returns the endpoint for one item, for example `/api/stock/DC-1042`.
2. **TODO 2:** `getStock(id)` calls `fetch(stockUrl(id))` and waits for the response. Call `fetch` with just the URL.
3. **TODO 3:** when `response.ok` is false, throw an `Error` whose message includes the status, for example `Stock check failed: 404`.
4. **TODO 4:** otherwise, wait for `response.json()` and return the `stock` number from the body.
5. **TODO 5:** `isInStock(id)` returns `true` when `getStock(id)` gives more than 0, and `false` otherwise.

Press **Submit** in Cadence, or save the file in your editor. The checks use a pretend server, so nothing goes over the internet.

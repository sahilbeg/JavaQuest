# Typing async code

Write the typed client for Dhaaga's stock server in `src/stockApi.ts`. The server answers `GET /api/stock/DC-1042` with `{ "id": "DC-1042", "stock": 3 }`.

1. **TODO 1:** `fetchStock(id)` fetches `/api/stock/<id>` and returns the reply as a `StockReply`. When `response.ok` is false, it throws an `Error` with the message `Stock check failed: 503` (with the real status).
2. **TODO 2:** `stockResult(id)` never rejects. It returns `{ ok: true, stock }` on success, and `{ ok: false, error }` on any failure. `error` is the `Error`'s message, or the thrown value as text when it isn't an `Error`.
3. **TODO 3:** `stockForAll(ids)` starts every request at once with `Promise.all` and returns a `Map` from each id to its stock. If any check fails, it rejects.

The checks use a pretend `fetch`, so no real server is needed. `src/` is type-checked in strict mode first, so a forgotten `await` shows up as a type error.

# Typed APIs end to end

The stock API changed `price_paise` to text for one afternoon, and the store added prices like `"45000" + "12000"`. The types said everything was fine, because the code trusted the JSON with `as`. Make `src/api.ts` check data where it enters the app, and write data in the server's shape where it leaves.

The server's item JSON is `{ id, name, price_paise, stock }`. The app's `Item` is `{ id, name, paise, stock }`.

1. **TODO 1:** `parseItem(data: unknown)` returns an `Item`, or `null` when anything is wrong. `id` must be non-empty text, `name` text, and `price_paise` and `stock` whole numbers of 0 or more. The `Item` is a new object with only its four fields: `paise` comes from `price_paise`, and extra fields are dropped.
2. **TODO 2:** `parseItems(data: unknown)` returns the list of `Item`s, or `null` if `data` isn't an array or any one item is bad. An empty array gives `[]`.
3. **TODO 3:** `fetchItems(url)` never throws. It returns a `Result<Item[]>`:
   - `{ ok: true, value: items }` for a good answer;
   - `{ ok: false, error: "HTTP 503" }` (the real status) when `response.ok` is false;
   - `{ ok: false, error: "The server sent items we don't understand." }` when `parseItems` gives `null`;
   - `{ ok: false, error: <the Error's message> }` when `fetch` or `json()` throws.
4. **TODO 4:** `orderRequest(lines, phone)` builds the body for `POST /api/orders`: `{ items: [{ item_id, qty }], phone }`. Lines with `qty` 0 are left out, and the phone loses its spaces (`98765 43210` becomes `9876543210`).

Use `unknown` and checks, not `as`. The checks use a pretend server, so nothing touches the network.

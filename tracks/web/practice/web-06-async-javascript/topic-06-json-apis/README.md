# Talking to a JSON API

Connect checkout's PIN box and Place order button to Dhaaga's API. Change `src/api.js`.

The API answers:
- `GET /api/delivery?pin=411001` with `200` and `{ "deliverable": true, "days": 2 }` for a Pune PIN (starts with 411), `{ "deliverable": false }` for other PINs, and `400` for a PIN that isn't 6 digits.
- `POST /api/orders` with a JSON body, with `201` and `{ "id": "order-17" }`, or `400` and `{ "error": "PIN must be 6 digits" }`.

1. **TODO 1:** `deliveryUrl(pin)` returns `/api/delivery?pin=411001`. Build the query string with `new URLSearchParams({ pin })`.
2. **TODO 2 and 3:** `checkDelivery(pin)` fetches `deliveryUrl(pin)` and returns the JSON body. When `response.ok` is false, throw an `Error` whose message includes the status.
3. **TODO 4:** `deliveryText(pin, result)` returns `Delivery to 411001 in 2 days` when `result.deliverable` is true, and `Sorry, we don't deliver to 560001 yet` when it isn't (with the real PIN and days).
4. **TODO 5:** `createOrder(order)` sends a `POST` to `/api/orders`. Pass fetch an options object with `method: "POST"`, `headers: { "Content-Type": "application/json" }` (a plain object), and `body: JSON.stringify(order)`.
5. **TODO 6:** read the JSON body. If the answer isn't ok, `throw new Error(data.error)`, so the server's own text reaches the customer. Otherwise return `data.id`.

Press **Submit** in Cadence, or save the file in your editor. The checks use a pretend API, so no real order is placed.

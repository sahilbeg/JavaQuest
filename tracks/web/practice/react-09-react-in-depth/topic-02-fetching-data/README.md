# Fetching data in a component

Make Raju's order status page show what the server says.

Change `src/OrderStatus.jsx`. The component gets an `orderId` prop, like `order-17`.

1. **TODO 1:** in an effect, fetch `/api/orders/<orderId>` and keep the answer in `order`. The answer looks like `{ "id": "order-17", "status": "PACKED" }`.
2. **TODO 2:** if the answer is not ok (like a 404), or the request fails, set `error` to `We couldn't load order-99.` (with the real id).
3. **TODO 3:** fetch again when `orderId` changes. Start each new request from a clean slate, so the page shows `Loading order-18...` until order-18's answer arrives.
4. **TODO 4:** return a cleanup that makes an old request's answer be ignored, so a slow answer for order-17 never replaces order-18.

The JSX at the bottom is done: it shows the error, the loading message or the order.

`src/main.jsx` gives the preview a pretend API: order-17 is slow, order-18 is fast and order-99 doesn't exist. The checks use their own pretend `fetch`.

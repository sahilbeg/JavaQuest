# Security: secrets, headers and input

A security check before launch found three holes. Close them.

## The order action (`src/app/actions.js`, TODOs 1–2)

`placeOrder(formData)` is a server action, which means anyone can call it with any fields, not only our form.

1. **TODO 1:** it multiplies by a `paise` field from the form, so a customer could buy Golu for ₹0.01. Look the item up with `getItem(id)` and use the server's `item.paise`. Ignore any `paise` field.
2. **TODO 2:** check the input before saving anything.
   - An unknown `id` returns `{ ok: false, error: "Unknown item" }`.
   - A `qty` that isn't a whole number from 1 to 5 (like `"0"`, `"6"`, `"2.5"`, `"two"` or `""`) returns `{ ok: false, error: "Pick a quantity from 1 to 5" }`.
   - A good order returns `{ ok: true, orderId, totalPaise }`. The data layer starts with `order-17`, so the next one is `order-18`.

## The order route (`src/app/api/orders/[id]/route.js`, TODOs 3–4)

`GET /api/orders/order-17` shows anyone any order, phone number and address included.

3. **TODO 3:** find the customer: `customerFor(sessionFromCookie(request.headers.get("cookie")))`.
   - No customer (no cookie, or an unknown token): status **401**, `{ error: "Please log in" }`.
   - An order that doesn't exist, **or** belongs to another customer: status **404**, `{ error: "Order not found" }`. Use the same answer for both, so nobody can tell which order numbers exist.
4. **TODO 4:** answer with only `{ id, itemId, qty, totalPaise, status }`. Never send `phone`, `address` or `customerId`.

## Security headers (`src/lib/securityHeaders.js`, TODO 5)

5. **TODO 5:** `next.config.mjs` sends `securityHeaders` with every page. Make it three `{ key, value }` objects:
   - `Content-Security-Policy`: `default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' blob: data:; font-src 'self'; object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors 'none'; upgrade-insecure-requests`
   - `X-Content-Type-Options`: `nosniff`
   - `Referrer-Policy`: `strict-origin-when-cross-origin`

The checks split the policy at each `;`, so the order of directives doesn't matter, but these must be there: `default-src 'self'`, `object-src 'none'`, `base-uri 'self'`, `form-action 'self'` and `frame-ancestors 'none'`. Never `'unsafe-eval'`.

Don't change the other files in `src/lib/`. Asha's session token is `s-7f3a9c` and Ravi's is `s-91bc04`.

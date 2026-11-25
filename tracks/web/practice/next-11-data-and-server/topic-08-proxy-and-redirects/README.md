# Proxy, redirects and guards

Send old links to their new home, send logged-out customers to the login page, and stop customers opening each other's orders.

`src/lib/session.js` and `src/lib/orders.js` are done. `currentCustomer(cookieStore)` gives `{ phone }` for a good `dhaaga_session` cookie, or `null`. `findOrder(id)` gives `{ id, phone, status }` or `undefined`.

Change `src/proxy.js`. It gets a `NextRequest`: `request.nextUrl.pathname` is the path, `request.url` the full address, and `request.cookies.get(name)` gives `{ name, value }` or `undefined`.

1. **TODO 1:** `/catalogue` and every path under it moved for good to `/items`. Answer `Response.redirect(new URL(...), 308)`: `/catalogue` goes to `/items`, and `/catalogue/DC-1042` to `/items/DC-1042`.
2. **TODO 2:** `/account` and every path under it need a `dhaaga_session` cookie. Without one, answer a 307 redirect to `/login?next=<the path>`, for example `http://localhost/login?next=/account/orders`. With one, return nothing, so the request goes on.
3. **TODO 3:** set `config.matcher` to `["/account/:path*", "/catalogue/:path*"]`.

Change `src/lib/guard.js`. Pages call these with `await cookies()`:

4. **TODO 4:** `requireCustomer(cookieStore, path)` returns the customer, or calls `redirect("/login?next=<path>")` when there is none.
5. **TODO 5:** `getOwnOrder(cookieStore, orderId)` first calls `requireCustomer` with the path `/account/orders/<orderId>`. Then it returns the order, or calls `notFound()` when the order doesn't exist **or** belongs to another phone.

The checks use a pretend request, a pretend cookie store, and a pretend `next/navigation` whose `redirect` throws `NEXT_REDIRECT <path>` and whose `notFound` throws `NEXT_NOT_FOUND`.

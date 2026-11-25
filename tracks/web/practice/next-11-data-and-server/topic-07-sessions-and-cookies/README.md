# Sessions and cookies

Replace the "customer" cookie, which anyone could edit, with real sessions.

Change `src/lib/session.js`. Every function takes the cookie store as its first argument: in the app it's `await cookies()` from `next/headers` (see `src/app/login/actions.js`, which is done). Its `get(name)` gives `{ name, value }` or `undefined`, and it has `set(name, value, options)` and `delete(name)`. Times are in milliseconds, like `Date.now()`, and are passed in as `now` so the checks can choose them.

1. **TODO 1:** `startSession(cookieStore, phone, now)` makes a random id with `randomUUID()`, remembers `{ phone, expiresAt: now + one week }` for it in the `sessions` Map, and sets the cookie `dhaaga_session` to the id with the options `{ httpOnly: true, secure: true, sameSite: "lax", path: "/", maxAge: WEEK_SECONDS }`. It returns the id. The phone number must never go into the cookie.
2. **TODO 2:** `currentCustomer(cookieStore, now)` returns `{ phone }` for a good session, and `null` when there is no cookie, when the id isn't in the Map, or when `now` has reached `expiresAt`. Forget an expired session when you find one.
3. **TODO 3:** `endSession(cookieStore)` forgets the session on the server and deletes the cookie.

The checks use a pretend cookie store with the same `get`, `set` and `delete`.

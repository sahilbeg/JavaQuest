# Environment variables and secrets

Keep the UPI gateway key on the server, check the settings when the server starts, and stop fake payment messages.

Change `src/lib/env.js`. Each function takes `env`, an object like `process.env`, as its argument:

1. **TODO 1:** `readServerConfig(env)` returns `{ upiKey, dbUrl }` from `UPI_GATEWAY_KEY` and `ORDERS_DB_URL`. If any of those is missing or empty, it throws one error naming all the missing ones, in that order, like `Missing environment variables: UPI_GATEWAY_KEY, ORDERS_DB_URL.`
2. **TODO 2:** `publicConfig(env)` returns `{ siteUrl, upiId }` from `NEXT_PUBLIC_SITE_URL` and `NEXT_PUBLIC_UPI_ID`, with the defaults `https://dhaaga.in` and `dhaaga@upi` when they're missing. It never reads any other variable.
3. **TODO 3:** `leakedSecrets(env)` returns the names that start with `NEXT_PUBLIC_` and contain `KEY`, `SECRET`, `TOKEN` or `PASSWORD`, sorted A to Z.

Change `src/app/api/upi-webhook/route.js`, which the UPI gateway calls when a customer pays:

4. **TODO 4:** read `process.env.UPI_WEBHOOK_SECRET` inside `POST`, on every request. If it's missing or empty, answer 500 `{ "error": "Webhook is not set up." }`.
5. **TODO 5:** if the request's `x-dhaaga-signature` header isn't exactly the secret, answer 401 `{ "error": "Bad signature." }`. Otherwise answer 200 `{ "ok": true }`.

The checks set `UPI_WEBHOOK_SECRET` with `vi.stubEnv` after your file has loaded, so read it inside the handler.

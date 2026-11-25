## Hook

The Aundh shop has a price board out front and a key to the cash locker. Everyone may read the board. The locker key stays in Meera Aunty's pocket, and it is never pinned to the board, however handy that would be.

Your app has both kinds of setting too. The shop's UPI id can be shown to anyone. The key that lets the server talk to the UPI gateway must never leave the server. This topic is about keeping them apart.

## Concept

### Settings that change from place to place

On your laptop, the app should use a test database and the payment gateway's practice mode. On the real server, it uses the real ones. The code is the same: only the settings differ.

```callout jargon
An **environment variable** is a named setting given to a program from outside its code, by the computer or the hosting service it runs on. In Node, they're all in the object `process.env`, and every value is text.
```

```js run
const env = { ORDERS_DB_URL: "postgres://localhost/dhaaga_dev", PORT: "3000" };
console.log(env.ORDERS_DB_URL);
console.log(env.UPI_GATEWAY_KEY);
console.log(typeof env.PORT);
```

A missing variable is just `undefined`, with no error. And numbers arrive as text, so `PORT` is `"3000"`.

### Syntax: .env files

```text
# .env.local (never committed)
ORDERS_DB_URL=postgres://localhost/dhaaga_dev
UPI_GATEWAY_KEY=test_key_from_the_gateway
NEXT_PUBLIC_UPI_ID=dhaaga@upi
```

Next.js reads these files when it starts and puts each line into `process.env`:

- `.env` holds safe defaults, shared by everyone, and is committed.
- `.env.development` and `.env.production` hold values for just that mode.
- `.env.local` holds your own secrets. It is listed in `.gitignore`, so it never reaches GitHub.

On the real server, you don't use a file. You type the values into the hosting service's settings page.

```callout warning
If a secret is ever committed to Git, deleting the line isn't enough: it stays in the history. Change the key with the service that issued it, straight away.
```

### NEXT_PUBLIC_: values that go to the browser

Server code can read any variable. Browser code can't: the browser has no `process.env`. So when Next.js builds the app, it copies the value of every variable whose name starts with `NEXT_PUBLIC_` into the JavaScript files sent to the browser.

```animate
title: What the build puts in the browser's code
kind: scene
nodes:
  - { id: env, label: ".env.local", emoji: "📄", value: "3 variables" }
  - { id: build, label: next build, emoji: "🏗️" }
  - { id: server, label: Server code, emoji: "🖥️", value: "reads process.env" }
  - { id: browser, label: "Browser JS files", emoji: "🌐", value: empty }
steps:
  - say: The build reads the environment. Two variables are secret. One, NEXT_PUBLIC_UPI_ID, is meant for everyone.
    send: { from: env, to: build, label: "all 3" }
  - say: Server code keeps reading process.env when it runs. Nothing is copied, and nothing leaves the server.
    send: { from: build, to: server, label: "server bundle" }
  - say: In client code, process.env.NEXT_PUBLIC_UPI_ID is replaced by its value, "dhaaga@upi", written into the file.
    send: { from: build, to: browser, label: "\"dhaaga@upi\"" }
    set: { browser: "dhaaga@upi" }
  - say: Anyone can open DevTools and read that file. That's fine for a UPI id.
    focus: [browser]
  - say: Name the gateway key NEXT_PUBLIC_UPI_GATEWAY_KEY, and the build writes it into the same file. That's this ticket's leak.
    set: { browser: "dhaaga@upi, sk_live_…" }
```

```callout jargon
To **inline** a value means to write it straight into the code, replacing the name that pointed to it. The browser's copy can't change later without a new build.
```

### The rule for names

| Name | Who can read it |
|---|---|
| `UPI_GATEWAY_KEY` | server code only |
| `NEXT_PUBLIC_UPI_ID` | everyone, in every browser |

So: anything with KEY, SECRET, TOKEN or PASSWORD in its name never starts with `NEXT_PUBLIC_`. A small check at startup can catch that mistake before it ships.

### Syntax: reading a variable on the server

```js
const key = process.env.UPI_GATEWAY_KEY;
```

Read it in server code: a server component, a server action, a route handler or the data layer. Put `import "server-only";` at the top of a file that touches secrets, as in topic 6, so it can never be imported by a client component.

### Check the settings when the server starts

A missing variable gives `undefined` silently. The app then fails much later, in a confusing way, like the first customer's payment failing at 10 pm. It's better to fail at once, with a clear message.

```callout jargon
To **fail fast** means stopping as soon as something is wrong, with a message that names the problem, instead of carrying on and breaking later.
```

```js run
function missingNames(env, required) {
  return required.filter((name) => !env[name]);
}

const env = { ORDERS_DB_URL: "postgres://localhost/dhaaga_dev", UPI_GATEWAY_KEY: "" };
const missing = missingNames(env, ["UPI_GATEWAY_KEY", "ORDERS_DB_URL"]);
console.log(`Missing environment variables: ${missing.join(", ")}.`);
```

An empty value counts as missing too, because `!""` is `true`.

### Taking env as an argument

```js
export function readServerConfig(env) {
  return { upiKey: env.UPI_GATEWAY_KEY, dbUrl: env.ORDERS_DB_URL };
}

readServerConfig(process.env);
```

Passing `env` in, rather than reading `process.env` inside, makes the function easy to test: a test passes a plain object with exactly the values it wants.

### Proving a message really came from the gateway

When a customer pays, the UPI gateway calls a route handler on dhaaga.in: "order-17 is paid". But anyone can send a request to that address. So the gateway and Dhaaga share a secret, and every real message carries proof of it in a header.

```callout jargon
A **webhook** is a request that another company's server sends to yours when something happens, like a payment. The address is public, so you must check every webhook really came from them.
```

```js
export async function POST(request) {
  const secret = process.env.UPI_WEBHOOK_SECRET;
  if (!secret) return Response.json({ error: "Webhook is not set up." }, { status: 500 });
  if (request.headers.get("x-dhaaga-signature") !== secret) {
    return Response.json({ error: "Bad signature." }, { status: 401 });
  }
  return Response.json({ ok: true });
}
```

- The secret is read inside the handler, every time, so a changed setting is picked up.
- With no secret set up, it refuses everything. Never let "no secret" mean "no check".
- 401 means "I don't know who you are".

```callout tip
Real gateways go one step further: they send a **signature**, a code worked out from the message and the secret, instead of the secret itself. You check it with the gateway's own instructions. The rule stays the same: no proof, no payment.
```

## Worked example

Kavya's settings file. Every function takes `env` as an argument.

```js
const REQUIRED = ["UPI_GATEWAY_KEY", "ORDERS_DB_URL"];

export function readServerConfig(env) {
  const missing = REQUIRED.filter((name) => !env[name]);
  if (missing.length > 0) throw new Error(`Missing environment variables: ${missing.join(", ")}.`);
  return { upiKey: env.UPI_GATEWAY_KEY, dbUrl: env.ORDERS_DB_URL };
}

export function leakedSecrets(env) {
  return Object.keys(env)
    .filter((name) => name.startsWith("NEXT_PUBLIC_"))
    .filter((name) => /KEY|SECRET|TOKEN|PASSWORD/.test(name))
    .sort();
}
```

- `readServerConfig` lists every missing name at once, so you fix them all in one go.
- `leakedSecrets` finds public names that look secret. Run it at startup, and stop if it finds any.
- Neither function reads `process.env` itself. The app passes it in.

## Try it

When the checks pass, imagine Sana's new variable `NEXT_PUBLIC_MAPS_TOKEN`. Run it through `leakedSecrets` in your head: it starts with `NEXT_PUBLIC_` and contains `TOKEN`, so it's reported. Is that right? A maps token meant for browsers can be public, but the check makes someone stop and think, which is the point.

## Recap

- Environment variables are settings from outside the code, in `process.env`, always as text. Missing ones are `undefined`.
- `.env.local` holds your own secrets and is never committed. Production values live in the hosting service.
- Only `NEXT_PUBLIC_` variables reach the browser: their values are written into the JavaScript at build time. Secrets never get that prefix.
- Check required settings when the server starts, and fail fast with a message that names what's missing.
- A webhook must prove it came from the sender. Read the secret at request time, and refuse everything if it isn't set.

## Common mistakes

**1. A secret with the public prefix.**

```js
const key = process.env.NEXT_PUBLIC_UPI_GATEWAY_KEY;
```

It works everywhere, which is the problem. The key is now in a JavaScript file every visitor downloads.

Fix: name it `UPI_GATEWAY_KEY`, read it only in server code, and change the leaked key with the gateway.

**2. Expecting a secret in a client component.**

```jsx
"use client";
export function PayButton() {
  const key = process.env.UPI_GATEWAY_KEY;
  return <button onClick={() => pay(key)}>Pay</button>;
}
```

`key` is `undefined` in the browser, and the payment fails with a confusing gateway error. Next.js is protecting you here.

Fix: do the payment call in a server action, where the key is available.

**3. Letting "no secret" pass.**

```js
export async function POST(request) {
  const secret = process.env.UPI_WEBHOOK_SECRET;
  if (secret && request.headers.get("x-dhaaga-signature") !== secret) {
    return Response.json({ error: "Bad signature." }, { status: 401 });
  }
  return Response.json({ ok: true });
}
```

On a server where someone forgot to set the variable, `secret` is `undefined`, the check is skipped, and anyone can mark orders as paid.

Fix: if the secret is missing, refuse with 500 before anything else.

**4. Reading the secret once, at import.**

```js
const SECRET = process.env.UPI_WEBHOOK_SECRET;

export async function POST(request) {
  // compares with SECRET
}
```

The value is captured when the file is first loaded. If it wasn't set yet, or is changed later, the handler keeps the old value.

Fix: read `process.env.UPI_WEBHOOK_SECRET` inside the handler.

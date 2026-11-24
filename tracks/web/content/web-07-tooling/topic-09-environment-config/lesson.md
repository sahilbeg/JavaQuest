## Hook

Meera Aunty's coaster pattern works in any colour. She does not knit a new pattern for Marigold and another for Indigo. The pattern stays the same, and a small note says "this batch: Indigo".

Code should work the same way. The same code runs on your laptop, on a test copy of the site, and on the live dhaaga.in. Only a few settings change: which API it talks to, whether the Diwali banner is on.

Right now the API address is typed into `cart.js`. So the test copy talked to the real shop. The fix is to keep the code the same and hand it a small note of settings for each place it runs.

## Concept

```callout jargon
**Configuration** is the settings that change from place to place while the code stays the same. An **environment** is one place the code runs: your laptop (development), a test copy (staging), or the live site (production).
```

```callout jargon
An **environment variable** is a named setting, like `VITE_API_URL`, handed to a program from outside its code. Each environment gives the same name a different value.
```

| Setting | Your laptop | Staging | Live dhaaga.in |
|---|---|---|---|
| `VITE_API_URL` | `http://localhost:3000` | `https://staging-api.dhaaga.in` | `https://api.dhaaga.in` |
| `VITE_SHOW_DIWALI` | `true` | `true` | `false` until the sale |

The code never says which one it is. It reads the setting, and does what it says.

### Syntax: import.meta.env

```js
const apiUrl = import.meta.env.VITE_API_URL;
```

In a Vite project, settings arrive in `import.meta.env`, an object Vite fills in for you. Some keys are always there:

| Key | What it holds |
|---|---|
| `MODE` | `"development"` with `npm run dev`, `"production"` with `npm run build` |
| `DEV`, `PROD` | `true` or `false`, matching the mode |
| `BASE_URL` | the path the site lives under, usually `"/"` |

Your own settings must start with `VITE_`. Vite leaves out every other name, so a setting meant only for your laptop never reaches the page by accident.

### Syntax: .env files

```text
VITE_NAME=value
```

Vite reads the values from small text files in the top folder, one `NAME=value` per line:

```text
# .env                 used in every mode
VITE_FREE_DELIVERY_ABOVE=99900

# .env.development     used by npm run dev
VITE_API_URL=http://localhost:3000
VITE_SHOW_DIWALI=true

# .env.production      used by npm run build
VITE_API_URL=https://api.dhaaga.in
```

A file ending in `.local`, like `.env.local`, holds your own personal settings. It is for your laptop only, so put `*.local` in `.gitignore`.

```callout tip
Values are read when the dev server starts, and baked in when you build. After you change a `.env` file, restart `npm run dev`.
```

### Everything is text

An environment variable is always a string, or missing. `VITE_SHOW_DIWALI=false` gives the text `"false"`, not the boolean `false`. And any non-empty text is truthy:

```js run
const env = { VITE_SHOW_DIWALI: "false", VITE_FREE_DELIVERY_ABOVE: "99900" };

if (env.VITE_SHOW_DIWALI) {
  console.log("Diwali banner shown!");
}
console.log(env.VITE_FREE_DELIVERY_ABOVE + 100);
console.log(Number(env.VITE_FREE_DELIVERY_ABOVE) + 100);
```

The banner shows even though the setting says `false`. And `+` on text joins it, giving `99900100`.

So every value must be turned into the right type, once, carefully:

- **Flags:** compare the text: `=== "true"` and `=== "false"`.
- **Numbers:** check the text is only digits, then use `Number(...)`.
- **Addresses:** trim the spaces, and remove a trailing `/`, so `apiUrl + "/orders"` never makes `//orders`.

### Defaults and failing fast

What if a setting is missing? There are two good answers, and one bad one.

- **A safe default.** No `VITE_SHOW_DIWALI`? Then no banner. No free-delivery setting? Use the usual 99900 paise.
- **Refuse to start.** A live site with no API address cannot take orders. Better to stop with a clear error during the build or start-up than to fail on a customer's phone.
- **The bad answer:** a default that is only right on your laptop, like using `http://localhost:3000` in production. The site starts, looks fine, and every order goes nowhere.

```callout jargon
**Failing fast** means stopping as early as possible, with a clear message, when something is wrong. A wrong setting found at start-up costs a minute. Found by a customer, it costs an order.
```

### One config module

Do not read `import.meta.env` all over the code. Read it in one place, check it, and export a finished object:

```js
// src/config.js
export function loadConfig(env) {
  // read, convert, check, fill in defaults
}
```

```js
// src/main.js
import { loadConfig } from "./config.js";

export const config = loadConfig(import.meta.env);
```

The rest of the site imports `config` and uses `config.apiUrl`. This has three wins:

- Every setting is checked once, at start-up.
- The names and defaults are all in one file, easy to review.
- `loadConfig` takes the settings as an argument, so a test can pass in any object it likes.

`Object.freeze(config)` makes the object read-only, so no other code can change a setting halfway through.

### Secrets do not belong in the browser

```callout warning
Everything in `import.meta.env` is copied into the JavaScript bundle. Anyone can open DevTools and read it. Never put a password, a payment key or any other secret in a `VITE_` variable.
```

```animate
title: Where a VITE_ setting ends up
kind: scene
nodes:
  - { id: envfile, label: .env.production, emoji: "📄", value: "VITE_API_URL=…" }
  - { id: build, label: npm run build, emoji: "🛠️" }
  - { id: bundle, label: dist/assets/index.js, emoji: "📦" }
  - { id: phone, label: Any customer's phone, emoji: "📱" }
steps:
  - say: The build reads .env.production. Only names starting with VITE_ are kept.
    send: { from: envfile, to: build, label: "VITE_* values" }
  - say: Vite writes each value straight into the JavaScript, as plain text in the bundle.
    send: { from: build, to: bundle, label: "\"https://api.dhaaga.in\"" }
    set: { bundle: contains the URL }
  - say: Every visitor downloads that bundle. Anyone can open it and read the values.
    send: { from: bundle, to: phone, label: download }
    set: { phone: can read every value }
  - say: An API address is fine to share. A payment key would not be. Secrets stay on the server.
    focus: [phone]
```

Secret keys, like the UPI gateway's, live on the server and never in the website's code. The website asks the server to do the secret work.

## Worked example

Kavya's ticket: one config module, safe defaults, and a refusal to start without an API address in production. Here is `src/config.js`, with its first setting done.

```js
export const DEFAULTS = {
  apiUrl: "http://localhost:3000",
  freeDeliveryAbovePaise: 99900,
  showDiwaliOffer: false,
};

/** The trimmed text of a setting, or undefined when it is missing or blank. */
export function read(env, name) {
  const value = env[name];
  if (typeof value !== "string" || value.trim() === "") return undefined;
  return value.trim();
}

export function loadConfig(env) {
  const mode = read(env, "MODE") ?? "development";

  let apiUrl = read(env, "VITE_API_URL");
  if (apiUrl === undefined) {
    if (mode === "production") {
      throw new Error("VITE_API_URL is missing: the live site cannot take orders without it");
    }
    apiUrl = DEFAULTS.apiUrl;
  }
  apiUrl = apiUrl.replace(/\/+$/, "");

  // ...the delivery limit and the Diwali flag work the same way
}
```

Line by line:

1. `DEFAULTS` holds the safe values, all in one place.
2. `read` treats missing and blank the same way. `"   "` is not a real address.
3. `?? "development"` gives a mode when `MODE` is missing, as it is in a test.
4. Production with no address throws, with a message that names the setting. Anywhere else, the laptop default is fine.
5. `/\/+$/` matches one or more `/` at the end, so `https://api.dhaaga.in/` becomes `https://api.dhaaga.in`.

Your fix finishes `loadConfig`: the delivery limit (digits only, as a number), the Diwali flag (`"true"` or `"false"`, nothing else), and `Object.freeze` on the result.

## Try it

Press Run on the "Everything is text" example.

1. Change the `if` to `if (env.VITE_SHOW_DIWALI === "true")`. Run it. Does the banner still show?
2. Set `VITE_FREE_DELIVERY_ABOVE` to `"₹999"` and run it. What does `Number("₹999")` give? Why should the config refuse it instead?
3. Add `console.log(Object.isFrozen(Object.freeze({ a: 1 })));` and run it.

## Recap

- Configuration is what changes between environments, like the API address. The code stays the same.
- Vite puts settings in `import.meta.env`, from `.env` files. Only names starting with `VITE_` reach the page, and `MODE` says development or production.
- Every environment value is text or missing. Convert flags with `=== "true"`, and numbers only after checking for digits.
- Give safe defaults where one exists, and fail fast with a clear error where none does. Never use a laptop-only default in production.
- Read settings in one config module that takes `env` as an argument. Never put a secret in a `VITE_` variable: it ships to every browser.

## Common mistakes

**1. Treating "false" as false.**

```js
const showDiwaliOffer = Boolean(env.VITE_SHOW_DIWALI);
```

What you see: the Diwali banner appears on the live site in July. `Boolean("false")` is `true`, because the text is not empty.

Fix: compare the text: `value === "true"`. Throw for anything that is neither `"true"` nor `"false"`, so a typo like `ture` is caught.

**2. A laptop default in production.**

```js
const apiUrl = env.VITE_API_URL || "http://localhost:3000";
```

What you see: the live build forgot its `.env.production`. The site loads, and every order fails with `Failed to fetch`, because customers' phones have no server on `localhost`.

Fix: use the default only outside production. In production, throw an `Error` that names `VITE_API_URL`.

**3. Forgetting the VITE_ prefix.**

```text
API_URL=https://api.dhaaga.in
```

What you see: `import.meta.env.API_URL` is `undefined`. Vite left the setting out on purpose.

Fix: name it `VITE_API_URL`. Only `VITE_` names reach the page.

**4. A secret in a VITE_ variable.**

```text
VITE_UPI_SECRET_KEY=sk_live_...
```

What you see: nothing, at first. But the key is now inside `dist/assets/index-*.js`, readable by anyone, and must be replaced at once.

Fix: keep secrets on the server only. The website calls the server, and the server uses the key.

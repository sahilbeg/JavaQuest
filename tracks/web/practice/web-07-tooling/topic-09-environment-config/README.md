# WEB-0709: The test site sent orders to the live shop

Finish `loadConfig(env)` in `src/config.js`. It takes an object like Vite's `import.meta.env` and returns the site's settings, checked and with safe defaults. Every value in `env` is text, or missing.

`read(env, name)` is finished: it gives the trimmed text of a setting, or `undefined` when it is missing or blank.

## What loadConfig returns

```js
{ mode: "production", apiUrl: "https://api.dhaaga.in", freeDeliveryAbovePaise: 99900, showDiwaliOffer: false }
```

- **`mode`**: `MODE`, or `"development"` when it is missing. (Done for you.)
- **`apiUrl`**: `VITE_API_URL` with any trailing `/` removed.
  - Missing in `"production"`: throw an `Error` whose message contains `VITE_API_URL`.
  - Missing in any other mode: `DEFAULTS.apiUrl`.
- **`freeDeliveryAbovePaise`**: `VITE_FREE_DELIVERY_ABOVE` as a number.
  - It must be digits only, like `"149900"`. Anything else (`"₹999"`, `"99.9"`, `"-5"`) throws an `Error` whose message contains `VITE_FREE_DELIVERY_ABOVE`.
  - Missing: `DEFAULTS.freeDeliveryAbovePaise`.
- **`showDiwaliOffer`**: `VITE_SHOW_DIWALI`.
  - `"true"` gives `true`, `"false"` gives `false`.
  - Missing: `false`.
  - Anything else (`"yes"`, `"ture"`) throws an `Error` whose message contains `VITE_SHOW_DIWALI`.
- The returned object is frozen with `Object.freeze`, so nothing can change a setting later.

Change only `src/config.js`. The checks are in `test/config.test.js`.

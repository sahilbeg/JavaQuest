# Type guards

The price list in `src/listings.ts` mixes single products (with `paise`) and gift bundles (with `itemIds` and `bundlePaise`). Write the type guards and the code that uses them.

1. **TODO 1:** `isBundle(listing)` is a type guard: true when the listing has `itemIds`. Use the `in` operator.
2. **TODO 2:** `priceOf(listing)` returns `paise` for a product and `bundlePaise` for a bundle.
3. **TODO 3:** `bundlesOnly(listings)` returns only the bundles, in order, as a `Bundle[]`.
4. **TODO 4:** `isPresent(value)` is true for anything except `null` and `undefined`. `0` and `""` are present.
5. **TODO 5:** `messageFor(error)` turns anything that was thrown into text: an `Error` gives its `message`, a string is shown as it is, and anything else gives `Something went wrong`.

`src/` is type-checked in strict mode before the checks run, so reading `bundlePaise` before you've narrowed is a type error.

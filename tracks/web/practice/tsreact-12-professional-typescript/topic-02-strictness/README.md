# Strict mode and tsconfig in depth

The price checker on the stall's tablet showed **₹NaN** when a customer typed `toString`. The old code told the compiler "trust me" with `as` and `!`. Rewrite the four functions in `src/lookup.ts` so they are honest about missing values, and need no `any`, `as` or `!`.

1. **TODO 1:** `isItemCode(text)` is a type predicate. It returns `true` only for `CST`, `KEY`, `AMI`, `BAG` and `BTY`, written exactly. It must say no to `toString`, `constructor` and `__proto__`, which the `in` operator finds on every object. Use `Object.hasOwn`.
2. **TODO 2:** `priceOf(typed)` trims the text and makes it upper case, so `" bag "` finds `BAG`. It returns the price in paise, or `null` when there is no such item.
3. **TODO 3:** `cheapestLine(lines)` returns the line whose item has the lowest price (per item, not times the quantity), or `null` for an empty cart. On a tie, the first line wins. Don't sort or change `lines`.
4. **TODO 4:** `describeError(error: unknown)` returns an `Error`'s `message`, a thrown string as it is, and `"Something went wrong."` for anything else.

The checks also read `lookup.ts` and fail if the code (not the comments) uses `any`, `as` or a `!` assertion.

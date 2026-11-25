# unknown and validating JSON

Check every item from the WhatsApp import file in `src/parseItem.ts` before the store uses it. Everything starts as `unknown`.

1. **TODO 1:** `isRecord(value)` is true for a plain object: not `null`, not an array, not text or a number.
2. **TODO 2 and 3:** `parseItem(value)` returns `{ ok: true, value: item }` for a good item, with exactly `id`, `name`, `paise` and `stock` (extra fields are left out). Otherwise it returns `{ ok: false, error }` for the first problem, checked in this order:
   - not a plain object: `not an object`
   - `id` isn't text like `DC-1042` (`DC-` followed by digits): `id must be text like DC-1042`
   - `name` isn't text, or is empty or only spaces: `name must be text`
   - `paise` isn't a whole number of 0 or more: `paise must be a whole number of 0 or more`
   - `stock` isn't a whole number of 0 or more: `stock must be a whole number of 0 or more`
3. **TODO 4:** `parseItems(json)` checks a whole file. Not JSON: `not valid JSON`. Not a list: `expected a list of items`. A bad item: `item 2: ` followed by that item's error, counting from 1. All good: `{ ok: true, value: items }`.

`src/` is type-checked in strict mode before the checks run, so you can't read a field of an `unknown` without checking it first.

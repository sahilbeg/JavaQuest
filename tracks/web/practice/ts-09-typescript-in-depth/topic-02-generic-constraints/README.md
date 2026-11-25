# Generic constraints

Write four generic helpers in `src/catalogue.ts`. Each works for any list whose elements have the right fields: items, gift bundles, cart lines.

1. **TODO 1:** `cheapest(list)` returns the element with the fewest `paise`, or `undefined` for an empty list. On a tie, the first one wins. Its constraint, `T extends { paise: number }`, lets you read `paise`.
2. **TODO 2:** `pluck(list, key)` returns every element's value for `key`, in order. `pluck(items, "id")` gives the ids.
3. **TODO 3:** `findBy(list, key, value)` returns the first element whose `key` field is `=== value`, or `undefined`.
4. **TODO 4:** `linesTotal(lines)` adds up `paise * qty` over every line, and gives `0` for no lines.

The signatures are already typed, and `src/` is type-checked in strict mode before the checks run.

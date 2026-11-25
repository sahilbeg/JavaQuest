# Generics: one function, many types

Write the catalogue's shared list helpers once, in `src/collections.ts`. Each one is generic: `T` is whatever the list holds, so the same helper works for items, orders, prices and colours, and TypeScript still knows the types.

1. **TODO 1:** `last(list)` returns the last element, or `undefined` for an empty list.
2. **TODO 2:** `uniqueBy(list, keyOf)` returns a new list with one element per key: the first one seen, in the original order.
3. **TODO 3:** `groupBy(list, keyOf)` returns a `Map` from each key to the elements with that key. The keys keep the order they first appear in.
4. **TODO 4:** `paginate(list, page, size)` returns `{ items, page, totalPages }`. Page 1 is the first page. `totalPages` is at least 1, even for an empty list, and a page past the end has no items.

The signatures are already typed. Your job is the bodies. Before the checks run, `src/` is type-checked in strict mode, so a type error shows up like a compile error.

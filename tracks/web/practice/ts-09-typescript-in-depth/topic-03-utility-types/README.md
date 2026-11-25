# Utility types: Partial, Pick, Omit and Record

Every item type in `src/items.ts` is now built from the master `Item` type with utility types. Finish the four functions that use them.

1. **TODO 1:** `updateItem(item, changes)` returns a new item with `changes` (an `ItemUpdate`) applied on top. Never change the item you were given.
2. **TODO 2:** `toCard(item)` returns an `ItemCard` with exactly `id`, `name` and `paise`, and no other fields.
3. **TODO 3:** `createItem(input, nextNumber)` returns the new item with the id `DC-` followed by the number, for example `DC-1050`.
4. **TODO 4:** `stockByCode(items)` returns a `StockByCode`: the units in stock for each code. Every code is there, with `0` when no item has it.

`src/` is type-checked in strict mode before the checks run.

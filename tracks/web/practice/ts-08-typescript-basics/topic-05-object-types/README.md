# Object types and interfaces

`src/items.ts` describes catalogue items with interfaces. Finish the `Item` interface, then fill in the functions. Every amount is whole paise.

1. **TODO 1:** add `colours` to `Item`: an array of colour names (`string[]`).
2. **TODO 2:** `makeItem(id, name, paise, colours)` returns an `Item` with all four properties. Do TODO 1 first, or TypeScript will refuse the extra `colours` property.
3. **TODO 3:** `describeItem(item)` gives `"DC-1042 Golu the elephant: ₹450.00"`.
4. **TODO 4:** `describeToy(toy)` gives the same text, then the animal in brackets: `"DC-1042 Golu the elephant: ₹450.00 (elephant)"`. An `Amigurumi` is also an `Item`, so you can reuse `describeItem`.
5. **TODO 5:** `inColour(items, colour)` returns the items whose `colours` include that colour, in the same order.
6. **TODO 6:** `orderTotal(lines)` adds up `item.paise` times `qty` for every line.

Press **Submit** to run the checks.

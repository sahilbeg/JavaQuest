# Generics and bounded types

Replace the three copy-pasted shelf classes with one generic `Shelf<T>`, plus two small helpers.

1. **TODO 1:** in `Shelf`, store the capacity given to the constructor.
2. **TODO 2:** make `add` return `false` when the shelf is full. Otherwise store the item and return `true`.
3. **TODO 3:** make `get(index)` return the item at that position.
4. **TODO 4:** make `size()` return how many items the shelf holds.
5. **TODO 5:** make `isFull()` return `true` when the size has reached the capacity.
6. **TODO 6:** in `Pair`, store both values so `first()` and `second()` return them.
7. **TODO 7:** in `GenericTools`, make `lastOf` return the last item, or `null` for an empty list.
8. **TODO 8:** make `maxOf` return the biggest item using `compareTo`, or `null` for an empty list.
9. **TODO 9:** make `sum` add up `doubleValue()` of every number.

Press **Submit** in Cadence, or just save the files in your IDE. The tests run on their own.

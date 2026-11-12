# Build a linked list

When a station is full, riders wait in line. Build `RiderList<T>`, a linked list made of `Node` objects, to hold that line.

Do not use `ArrayList`, `LinkedList` or any other `java.util` list inside `RiderList`. The point is to build the chain of nodes yourself. The `Node` class is already there.

1. **TODO 1:** `addFirst(item)` puts an item at the front.
2. **TODO 2:** `addLast(item)` puts an item at the end. An empty list needs care.
3. **TODO 3:** `removeFirst()` removes the front item and returns it. It returns `null` when the list is empty.
4. **TODO 4:** `get(index)` returns the item at that position, counting from 0. It throws `IndexOutOfBoundsException` when the index is below 0 or not less than `size()`.
5. **TODO 5:** `size()` returns how many items the list holds. Keep a counter up to date in every add and remove.
6. **TODO 6:** `contains(item)` returns `true` when some item `equals` the one you pass in.
7. **TODO 7:** `reverse()` flips the order of the nodes in place.
8. **TODO 8:** `toString()` returns the items like `[a, b, c]`, and `[]` when empty.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

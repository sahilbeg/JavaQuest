# Heaps and priority queues

Iruka needs the repair crew to always fix the most urgent bike next. Build a min-heap by hand, then use Java's own `PriorityQueue` to plan the repairs.

`MinHeap` stores its values in a plain `int[]`. Do not use `PriorityQueue`, any list or any sort inside it. For index `i`, the parent is `(i - 1) / 2` and the children are `2 * i + 1` and `2 * i + 2`.

1. **TODO 1:** in `add`, grow the array to twice its size when it is full.
2. **TODO 2:** in `add`, put the value at the end and sift it up while it is smaller than its parent.
3. **TODO 3:** `poll()` throws `IllegalStateException` when the heap is empty.
4. **TODO 4:** `poll()` removes and returns the smallest value. Move the last value to the top, then sift it down.
5. **TODO 5:** `peek()` returns the smallest value without removing it. It throws `IllegalStateException` when the heap is empty.

`RepairPlanner.order` gets a list of `Repair(bikeId, urgency)` reports. A lower urgency is more urgent.

6. **TODO 6:** build a `Comparator<Repair>` that compares urgency first, then bike id.
7. **TODO 7:** add every repair to a `java.util.PriorityQueue` with that comparator. Poll until it is empty and return the bike ids in that order.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

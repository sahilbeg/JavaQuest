# Binary trees and BSTs

Theo needs a bike registry that lists bikes in id order and finds one fast. Build `BikeRegistry` as a binary search tree of bike numbers, such as `1042` for bike B-1042.

Build it by hand from the `Node` class that is already there. Do not use `TreeMap`, `TreeSet`, `Collections.sort` or any other sort. Write recursive helper methods where they help, such as `private Node insert(Node node, int bike)`.

1. **TODO 1:** `insert(int bike)` puts smaller ids to the left and larger ids to the right. It adds 1 to `size` for a new bike and ignores a duplicate.
2. **TODO 2:** `contains(int bike)` returns `true` when the bike is in the tree.
3. **TODO 3:** `inOrder()` returns every bike in id order, using an in-order traversal.
4. **TODO 4:** `height()` returns the number of nodes on the longest path from the root down. An empty tree has height 0. A tree with one bike has height 1.
5. **TODO 5:** `min()` returns the smallest bike. It throws `NoSuchElementException` when the registry is empty.
6. **TODO 6:** `max()` returns the largest bike. It throws `NoSuchElementException` when the registry is empty.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

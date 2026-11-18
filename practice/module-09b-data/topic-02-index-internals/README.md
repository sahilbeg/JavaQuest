# How indexes work: a B+ tree and an index advisor

Kakashi's "every ride on one bike" report reads every ride in the table. Before you pick its index, build the structure behind one. Two parts:

1. `BPlusTree<K, V>`: the tree most database indexes use, with a counter of the pages each lookup reads.
2. `IndexAdvisor`: the leftmost-prefix rule, which says whether a composite index can help a query.

Do part 1 in TODO order: each piece uses the ones before it. Part 2 does not need part 1.

## 1. `BPlusTree<K extends Comparable<K>, V>(int maxKeys)`

Each node stands for one disk **page**. A node holds at most `maxKeys` keys.

- A **leaf** holds sorted keys and their values: `keys.get(i)` goes with `values.get(i)`. `next` points to the leaf on its right, or is `null` for the last leaf.
- A **branch** holds sorted separator keys and always has one more child than keys.
- A new tree is one empty leaf, so `height()` starts at 1.

Given: the node classes, `height()`, `size()`, `pageReads()`, `resetPageReads()`, `leafKeys()` and `read(node)`. `read(node)` adds 1 to the page counter and hands the node back. `leafKeys()` lists each leaf's keys, walking the `next` links from the leftmost leaf. It does not count reads, and it is handy for debugging.

- **TODO 1, constructor:** throw `IllegalArgumentException` when `maxKeys` is below 3.
- **TODO 2, `childIndex(branch, key)`:** the child to follow is **the number of separator keys that are `<=` key**. So a key equal to a separator goes **right**. With separators `[3, 5]`: key 2 → child 0, key 3 → child 1, key 4 → child 1, key 5 → child 2, key 9 → child 2.
- **TODO 3, `findLeaf(key)`:** start with `read(root)`. While the node is a branch, step into `children.get(childIndex(branch, key))`, passing **each** child through `read(...)`. Return the leaf. This reads exactly `height()` pages.
- **TODO 4, `get(key)`:** find the leaf, then the key inside it. Return its value, or `null`. **Every `get`, found or not, reads exactly `height()` pages.**
- **TODO 5, `insert(node, key, value)`:** returns the `Split` it caused, or `null`.
  - In a leaf: a key already there gets its value **replaced**, and `size()` stays the same. A new key goes in at its sorted place, and `size` goes up by 1. If the leaf now has more than `maxKeys` keys, return `splitLeaf(leaf)`.
  - In a branch: insert into child `childIndex(branch, key)`. If that child split, insert the separator into `keys` at the child's index, and the new right node into `children` just after the child. If the branch now has more than `maxKeys` keys, return `splitBranch(branch)`.
- **TODO 6, `put(key, value)`:** call `insert(root, key, value)`. If the root split, make a new branch root with keys `[separator]` and children `[old root, new right node]`, and add 1 to `height`. That is the **only** way the tree grows taller.
- **TODO 7, `splitLeaf(left)`:** a leaf with `maxKeys + 1` keys splits. **The left leaf keeps the first `(maxKeys + 1) / 2` entries** (integer division), and a new right leaf takes the rest. Link it in: `right.next = left.next`, then `left.next = right`. The separator is the right leaf's **first key**, **copied** up (it stays in the leaf). With `maxKeys` 4, keys `[1, 2, 3, 4, 5]` become `[1, 2]` and `[3, 4, 5]`, with separator 3.
- **TODO 8, `splitBranch(left)`:** a branch with `maxKeys + 1` keys splits. Let `middle = (maxKeys + 1) / 2`. **The key at `middle` moves up** and stays in neither half. The left keeps the keys before it and the first `middle + 1` children. The right takes the keys after it and the remaining children.
- **TODO 9, `range(from, toInclusive)`:** the values of every key with `from <= key <= toInclusive`, in key order, both ends included.
  - If `from > toInclusive`, return an empty list **at once, reading no pages**.
  - Otherwise start at `findLeaf(from)` (that is `height()` reads). Scan its keys in order: **stop as soon as a key is greater than `toInclusive`**, and add the value of each key that is `>= from`.
  - **Move to the next leaf only when you scanned the whole leaf** (every key was `<= toInclusive`). Move with `read(leaf.next)`, which counts one more page. Stop when `next` is `null`.
  - So a range reads `height()` pages, plus one page for each further leaf it moves on to. With `maxKeys` 4 and keys 1 to 7, the leaves are `[1, 2] [3, 4] [5, 6, 7]` and `height()` is 2. `range(3, 4)` reads 3 pages: two down to `[3, 4]`, and one more to see that 5 is past the end.

`null` keys and values throw `NullPointerException` (given).

Time: `get` and `put` are O(log n), with the log taken to the base of the node width. `range` is O(log n + k) for k results. Space: O(n).

## 2. `IndexAdvisor`

Column names are compared **ignoring case**: `BIKE_ID` and `bike_id` are the same column.

- **TODO 10 and 11, `evaluate(indexColumns, equalityColumns, rangeColumn)`:** the query tests each column in `equalityColumns` with `=`, and `rangeColumn` with `<`, `>` or `BETWEEN`. `rangeColumn` is `null` when there is no range.
  - Throw `IllegalArgumentException` when `indexColumns` is null or empty, or when `rangeColumn` is also in `equalityColumns`.
  - **The leftmost-prefix rule:** walk the index columns from the left.
    - A column in `equalityColumns`: it is used. Go on to the next column.
    - The range column: it is used, and **you stop**. Columns after a range cannot narrow the search.
    - Any other column: **stop**. A column the query skips ends the prefix.
  - Return `new IndexUse(columnsUsed > 0, columnsUsed)`.
  - Examples, for `WHERE bike_id = ? AND started_at BETWEEN ? AND ?`: index `(bike_id, started_at)` uses 2; `(started_at, bike_id)` uses 1; `(bike_id, rider_id, started_at)` uses 1; `(rider_id, started_at)` uses 0 and is not usable.
  - Columns the query tests but the index does not hold are ignored.
- **TODO 12, `suggest(equalityColumns, rangeColumn)`:** the index to build: every equality column in **lower case and alphabetical order**, then the range column in lower case, if there is one. Throw `IllegalArgumentException` if the range column is also an equality column.
- **TODO 13, `bestIndex(indexes, equalityColumns, rangeColumn)`:** the position in `indexes` of the index with the most `columnsUsed`. When two tie, the **earlier** one wins. Return `-1` when no index uses any column.

## How the checks work

The tree checks build small trees by hand, then put 3,000 random keys with a fixed seed and compare every answer with a `java.util.TreeMap`. They read `pageReads()` after `resetPageReads()` to check what each `get` and `range` cost. A check gives up after 5 seconds if a loop never ends.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

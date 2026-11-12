# Build a hash table

Build `DockTable`, a small hash table that maps a station name to its free dock count. Build it by hand on an array of chains. Do not use `HashMap` or any other `java.util` map or list inside it.

The class already has a private `Entry` class (a key, a value and a `next` link) and an `Entry[]` with 8 buckets.

1. **TODO 1:** `indexFor(key, buckets)` returns `Math.floorMod(key.hashCode(), buckets)`.
2. **TODO 2:** `get(key)` walks the chain in the key's bucket and returns the value, or `null` if the key is missing. Compare keys with `equals`.
3. **TODO 3:** `put(key, value)` replaces the value when the key is already there. Otherwise it adds a new entry and adds 1 to the size.
4. **TODO 4:** after adding, when `size > 0.75 * bucketCount()`, double the buckets and put every entry into the new array at its new index.
5. **TODO 5:** `remove(key)` unlinks the entry and returns its old value, or `null` if the key is missing.
6. **TODO 6:** `size()` returns how many keys are stored, and `bucketCount()` returns the length of the buckets array.

The tests use `"Aa"` and `"BB"`, two keys with the same `hashCode`, to check that your chains work.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

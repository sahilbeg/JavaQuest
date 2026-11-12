# Merge sort and quicksort

The board report sorts a whole year of ride minutes. Insertion sort is too slow for that. Build the two fast sorts in `FastSorts.java`.

Do not call `Arrays.sort` or any other library sort. You build the sorting by hand. `Arrays.copyOfRange` and `Arrays.toString` are fine. The tests only check results, so this rule is on your honour.

1. **TODO 1 to 3:** `merge(left, right)` takes two sorted arrays and returns one new sorted array with every item from both. The arrays can have different lengths, and one can be empty.
2. **TODO 4 to 6:** `mergeSort(a)` sorts `a` in place. Split it in half, sort each half, then merge them and copy the result back into `a`.
3. **TODO 7 to 9:** `partition(a, low, high)` uses the Lomuto scheme on `a[low]` to `a[high]`, both ends included. The pivot is the last item, `a[high]`. Afterwards every item left of the pivot is `<=` the pivot, and every item right of it is `>` the pivot. Items outside `low..high` do not move. Return the pivot's final index.
4. **TODO 10:** `quickSort(a)` sorts the whole array in place. It calls `quickSort(a, 0, a.length - 1)`.
5. **TODO 11:** `quickSort(a, low, high)` sorts only `a[low]` to `a[high]`, both ends included. Stop when `low >= high`. Otherwise partition, then quicksort the part left of the pivot and the part right of it. Items outside `low..high` do not move.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.

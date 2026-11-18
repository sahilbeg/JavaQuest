# Bubble, selection, insertion sort

The kiosk firmware has no library sort. Write three sorts by hand in `SimpleSorts.java`, so the nightly ride minutes can be sorted on the device.

Do not call `Arrays.sort` or any other library sort. The tests only check results, so this rule is on your honour. Each method sorts the array it is given in place, smallest first.

1. **TODO 1:** `bubble(a)` swaps neighbours while the left one is bigger. You may simply call `bubblePasses(a)` once it works.
2. **TODO 2:** `bubblePasses(a)` bubble sorts `a` with an early stop and returns how many passes it made. Already sorted data takes 1 pass. An empty or one-item array takes 0.
3. **TODO 3:** `selection(a)` finds the smallest value from `i` to the end and swaps it into position `i`.
4. **TODO 4:** `insertion(a)` takes each `key = a[i]` and shifts bigger values one place right until the key fits.
5. **TODO 5:** `swap(a, i, j)` swaps two items with a `temp` variable. All the sorts can use it.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.

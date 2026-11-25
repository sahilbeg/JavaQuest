# Caching and revalidation

Give each piece of data the caching it needs, and clear the old copies when Meera Aunty changes something.

Change `src/lib/api.js`. Each function already calls `getJson(path, options)`; you choose the `options`, which go straight to `fetch`.

1. **TODO 1:** `getCatalogue()` keeps its copy for an hour at most (`revalidate: 3600`) and tags it `"catalogue"`.
2. **TODO 2:** `getStock()` must never be cached. Replace its option with `cache: "no-store"`.
3. **TODO 3:** `getShopHours()` keeps its copy until told otherwise (`cache: "force-cache"`) and tags it `"hours"`.

Change `src/lib/refresh.js`. The admin pages call these right after an edit:

4. **TODO 4:** `afterPriceChange(itemId)` calls `revalidateTag("catalogue", "max")` and `revalidatePath("/items/<itemId>")`, for example `revalidatePath("/items/DC-3007")`.
5. **TODO 5:** `afterHoursChange()` calls `revalidateTag("hours", "max")`.

The checks use a pretend `fetch` and a pretend `next/cache`, and look at what your code passes to them.

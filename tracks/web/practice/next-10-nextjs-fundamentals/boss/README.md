# INC-10: The item page shows the wrong elephant

Every item page, `src/app/items/[id]/page.jsx`, shows Rani the pink elephant (`DC-1038`, the first item in the catalogue), whatever the address. Fix the lookup without changing what the page shows for the right item.

1. **Bronze:** each page shows the item in its address. `/items/DC-1042` shows `<h1>Golu the elephant</h1>`, `₹450.00` and `7 left`. A sold-out item shows `Sold out`.
2. **Silver:** an id that isn't in the catalogue calls `notFound()` (from `next/navigation`) and never shows another item. That includes `DC-9999` and the cut-short `DC-104`, which must not match `DC-1042`.
3. **Gold:** `generateMetadata` names the right item:
   - `title`: the item's name only, like `"Golu the elephant"` (the root layout's template adds ` | Dhaaga & Co.`),
   - `description`: `"Golu the elephant, handmade in Pune. ₹450.00."`,
   - `openGraph`: `{ title, description, url: "/items/DC-1042" }`, with the same title and description,
   - for an unknown id: `{ title: "Item not found" }`, without throwing.

In the checks, `next/navigation` is replaced by a stand-in whose `notFound()` throws `NEXT_NOT_FOUND`, as the real one throws its own error.

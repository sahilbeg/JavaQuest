# The app router: pages from folders

Add two pages to the new Next.js site, and a helper that works out which address a file in `app/` becomes. The home page in `src/app/page.jsx` is done: use it as your model.

1. **TODO 1** in `src/app/about/page.jsx` (the page at `/about`): return an `<h1>` that says `Our story`, and a `<p>` with this text:
   `Dhaaga & Co. started as one table at the Sunday haat on FC Road. Meera Kulkarni, a retired school teacher, has crocheted all her life.`
2. **TODO 2** in `src/app/visit/page.jsx` (the page at `/visit`): return an `<h1>` that says `Visit us`, and a `<ul>` with exactly these two items:
   - `FC Road stall: Saturday and Sunday, 10:00 to 20:00`
   - `Aundh shop: Tuesday to Sunday, 11:00 to 21:00`
3. **TODO 3 to 5** in `src/lib/routes.js`: `routeFor(file)` takes a path like `"app/visit/page.jsx"` and returns its address, or `null` when the file is not a page.

| File | `routeFor` returns |
|---|---|
| `app/page.jsx` | `"/"` |
| `app/visit/page.jsx` | `"/visit"` |
| `app/help/delivery/page.js` | `"/help/delivery"` |
| `app/items/[id]/page.jsx` | `"/items/[id]"` |
| `app/(shop)/cart/page.jsx` | `"/cart"` (route groups are left out) |
| `app/about/layout.jsx`, `app/visit/Hours.jsx` | `null` (not called `page.js` or `page.jsx`) |
| `app/_components/page.jsx` | `null` (anything inside a folder starting with `_` is private) |

The pages keep their `export default`: that's how Next.js finds them.

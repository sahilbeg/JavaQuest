# Links and navigation

Make the site's menu move between pages without a full page load, show which page is open, and make the search box work.

1. **TODO 1** in `src/lib/nav.js`: `isActive(pathname, href)` says whether the menu link to `href` is the current page.
   - `"/"` is active only when `pathname` is exactly `"/"`.
   - Any other `href` is active on itself and on every address under it: `"/items"` is active on `"/items"` and `"/items/DC-1042"`, but not on `"/itemsale"`.
2. **TODO 2** in `src/app/NavBar.jsx`: use `Link` from `next/link` instead of plain `<a>` tags. Keep the same three links: Home `/`, Shop `/items`, Help `/help`.
3. **TODO 3** in `src/app/NavBar.jsx`: give the link for the current page `aria-current="page"`, using `usePathname()` and `isActive`. Other links have no `aria-current` at all.
4. **TODO 4** in `src/app/SearchBox.jsx`: when the form is sent, trim what was typed. Then go to `/items?q=` plus the text encoded with `encodeURIComponent`, using `router.push`. If nothing was typed, go to `/items`.

Both components start with `"use client"`, because they use hooks from `next/navigation`. In the checks, `next/navigation` is replaced by a stand-in that reports the address and records every `push`.

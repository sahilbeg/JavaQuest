# Accessibility and SEO

dhaaga.in doesn't show up on Google, every tab says just "Dhaaga & Co.", and a screen reader user has to hear the whole menu on every page. Fix the frame of the site and what it tells search engines.

1. **TODO 1:** `src/app/layout.jsx`.
   - Give `<html>` the attribute `lang="en-IN"`.
   - Change `metadata` to `{ metadataBase: new URL("https://dhaaga.in"), title: { default: "Dhaaga & Co.", template: "%s | Dhaaga & Co." }, description: "..." }`. Any non-empty description is fine.
2. **TODO 2:** `src/components/SiteShell.jsx`.
   - The first link on the page is `<a href="#main">Skip to content</a>`.
   - The Shop and Cart links sit inside `<nav aria-label="Main">`.
   - The page (`children`) goes inside `<main id="main">`, not a `<div>`.
3. **TODO 3:** `generateMetadata` in `src/app/items/[id]/page.jsx` (remember: `params` is a Promise).
   - For a real item: `title` is the item's name (the layout's template adds the rest), `description` is its description, `alternates` is `{ canonical: "/items/DC-1042" }`, and `openGraph` is `{ title, description, images: [{ url: "/images/DC-1042.jpg", width: 1200, height: 630, alt }] }` with the item's `alt`.
   - For an unknown id: `{ title: "Item not found", robots: { index: false } }`.
4. **TODO 4:** `src/app/robots.js` and `src/app/sitemap.js`.
   - `robots()` returns `{ rules: { userAgent: "*", allow: "/", disallow: ["/cart", "/checkout"] }, sitemap: "https://dhaaga.in/sitemap.xml" }`.
   - `sitemap()` returns one entry per page, each with a full `url`: first `https://dhaaga.in/`, then `https://dhaaga.in/items/<id>` for every item from `listItems()`, in catalogue order. Extra fields like `changeFrequency` are fine.

The item page itself is already done. Don't change `src/lib/`.

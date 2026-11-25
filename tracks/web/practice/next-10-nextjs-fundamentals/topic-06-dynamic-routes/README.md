# Dynamic routes: one page per colour

One page file, `src/app/colours/[colour]/page.jsx`, serves `/colours/indigo`, `/colours/mint` and every other colour. The colours and their slugs are in `src/lib/colours.js`, and the items in `src/lib/catalogue.js`. Each item's `colour` is a colour's `name`, like `"Rani pink"`.

1. **TODO 1**: `generateStaticParams()` returns one object per colour, using the slug: `[{ colour: "marigold" }, { colour: "indigo" }, ...]`, in the order of `COLOURS`.
2. **TODO 2**: in `ColourPage`, `await params` and find the colour with `colourBySlug`. If there's none, like `purple`, call `notFound()`.
3. **TODO 3**: keep the items whose `colour` is that colour's name, in catalogue order. When `(await searchParams).sort` is `"price"`, sort a copy, cheapest first.
4. **TODO 4**: draw the page:
   - `<h1>` with the colour's name, like `Indigo`.
   - When there are items: a link `Cheapest first` to `/colours/<slug>?sort=price`, then a `<ul>` with one `<li>` per item. Each `<li>` holds a link to `/items/<id>` with the item's name, then a space and the price from `rupees`, like `Golu the elephant ₹450.00`.
   - When there are none: `<p>Nothing in Off-white right now.</p>` (with the colour's name), and no list.

Use `Link` from `next/link` for every link. Remember: in Next.js 16, `params` and `searchParams` are Promises.

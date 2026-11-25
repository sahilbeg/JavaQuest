# Metadata: titles and sharing

Give every page a proper title and give each colour page its own preview card. The colour page from ticket 6 is finished in `src/app/colours/[colour]/page.jsx`.

1. **TODO 1** in `src/app/layout.jsx`: export `metadata` with
   - `metadataBase: new URL("https://dhaaga.in")`,
   - `title: { default: "Dhaaga & Co.", template: "%s | Dhaaga & Co." }`,
   - `description: "Handmade crochet from Pune: coasters, keychains, toys, bags and booties."`,
   - `openGraph: { siteName: "Dhaaga & Co.", locale: "en_IN", type: "website" }`.
2. **TODO 2** in the colour page: `describeColour(name, items)` returns the line about a colour, using `rupees` for the cheapest price:
   - no items: `No pieces in Off-white right now.`
   - one item: `1 handmade piece in Marigold, from ₹150.00.`
   - more: `2 handmade pieces in Indigo, from ₹450.00.`
3. **TODO 3** in the colour page: `generateMetadata({ params })`. Await `params` and find the colour.
   - Unknown colour: return `{ title: "Colour not found" }`.
   - Otherwise return `title` (like `"Indigo crochet"`, without the shop's name, because the template adds it), `description` from `describeColour` with the colour's items, and `openGraph` with exactly three fields: the same `title`, the same `description`, and `url: "/colours/indigo"` (with the colour's slug).
   - Nothing else goes in either object: the checks compare them whole.

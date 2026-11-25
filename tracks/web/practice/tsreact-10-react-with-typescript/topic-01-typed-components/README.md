# Typed components and props

Finish Sana's item card and the store header.

**`ItemCard.tsx`**

1. **TODO 1:** Add an optional `badge` prop that holds text to `ItemCardProps`.
2. **TODO 2:** Take `badge` from the props, with `"Handmade"` as its default.
3. **TODO 3:** Show the price in rupees: `<p className="price">` holds `rupees(paise)`, so 45000 shows `₹450.00`.
4. **TODO 4:** Show the badge in `<span className="badge">`.

**`StoreHeader.tsx`**

5. **TODO 5:** Return a `<header className="store-header">` holding `<h1>Dhaaga & Co.</h1>` and `<p className="tagline">Handmade crochet from Pune</p>`.

Keep the card's `<article className="item-card">`, its `<h3>` and its `<p className="colour">`. `src/` is type-checked with `tsc` (strict) before the checks run. The preview shows the header and two cards. Press **Submit** to run the checks.

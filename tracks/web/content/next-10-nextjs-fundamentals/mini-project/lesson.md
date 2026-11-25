## The design

The shop page lives at `/items`. Every choice a customer makes is kept **in the address**, not in state. Then a copied link opens the same list, Back undoes a choice, and the server can draw the page already filtered.

| Address | Shows |
|---|---|
| `/items` | every piece, in catalogue order |
| `/items?colour=indigo` | only Indigo pieces |
| `/items?q=elephant` | pieces whose name contains "elephant", ignoring case |
| `/items?sort=price-asc` | cheapest first (`price-desc`: dearest first) |
| `/items?colour=rani-pink&sort=price-asc` | choices combine |

```callout jargon
Keeping a page's choices in the address is called **URL state**. The address becomes the single place that says what the page shows. Anything that can read the address, from the server to a friend's phone, gets the same page.
```

The page is split the same way as ticket 5:

- `ShopPage` is a **server component**. It awaits `searchParams`, filters the catalogue, and sends a finished list.
- `Filters` is a small **client component**. When a customer picks a colour or a sort, or searches, it works out the new address and calls `router.push`. Nothing is filtered in the browser.

```animate
title: Meera Aunty picks Indigo
kind: scene
nodes:
  - { id: filters, label: Filters (client), emoji: "🎛️", value: "All colours" }
  - { id: router, label: router.push, emoji: "🧭" }
  - { id: page, label: ShopPage (server), emoji: "🖥️", value: "6 of 6" }
  - { id: friend, label: Friend in Nashik, emoji: "📱" }
steps:
  - say: Meera Aunty picks Indigo in the Colour menu.
    set: { filters: Indigo }
  - say: Filters builds the new address from the current choices plus the new one, and pushes it.
    send: { from: filters, to: router, label: "/items?colour=indigo" }
  - say: Next.js asks the server for the shop page at that address. The layout stays on screen.
    send: { from: router, to: page, label: "searchParams { colour: indigo }" }
  - say: ShopPage awaits searchParams and filters the catalogue on the server. Two pieces are left.
    set: { page: "2 of 6" }
  - say: The new list arrives. The address bar now says /items?colour=indigo.
    send: { from: page, to: filters, label: "Indigo list", back: true }
  - say: She copies the link to a friend. The friend's phone asks for the same address and gets the same two pieces.
    send: { from: friend, to: page, label: "GET /items?colour=indigo" }
```

### Syntax: building a query string

```js
const params = new URLSearchParams();
params.set("colour", "indigo");
```

`URLSearchParams` builds the part after `?` for you. It joins the pairs with `&` and encodes each value, so you never glue strings by hand.

```js run
const params = new URLSearchParams();
params.set("colour", "indigo");
params.set("q", "golu elephant");
console.log(params.toString());
console.log("/items?" + params.toString());
```

Notice the space in `golu elephant` becomes `+`. That's the standard way to write a space in a query string, and `searchParams` turns it back into a space for you.

## What you build

Money is in paise, shown with `rupees()` from `src/lib/money.js`. The catalogue and colours in `src/lib/` are finished. Each item's `colour` is a colour's name, like `"Rani pink"`; addresses use the slug, like `rani-pink`.

1. **`queryCatalogue(items, { colour, q, sort })`** in `src/lib/query.js` returns a new array. It never changes the one it's given.
   - `colour` is a slug. Keep only that colour's items. An unknown slug gives an empty list. An empty `colour` keeps everything.
   - `q`: keep items whose name contains it, ignoring case and spaces around it. Empty keeps everything.
   - `sort`: `"price-asc"` is cheapest first, `"price-desc"` dearest first. Anything else keeps catalogue order. Items with the same price keep their catalogue order.
2. **`shopHref({ colour, q, sort })`** in `src/lib/query.js` returns the address for those choices, in the order colour, q, sort, leaving out empty ones: `"/items"`, `"/items?colour=indigo&sort=price-asc"`, `"/items?q=golu+elephant"`.
3. **`ShopPage({ searchParams })`** in `src/app/items/page.jsx`, plus `export const metadata = { title: "Shop", description: "Every handmade piece from Dhaaga & Co., Pune." }`:
   - `<h1>Shop</h1>`, then `<Filters colour={colour} q={q} sort={sort} />`,
   - `<p>Showing 2 of 6 pieces</p>`: how many match, out of the whole catalogue,
   - a `<ul>` with one `<li>` per piece: a `Link` to `/items/<id>` with its name, a space, its price, and `<span> Sold out</span>` when its stock is 0,
   - when nothing matches, `<p>Nothing matches.</p>` and a `Link` `See everything` to `/items`, instead of the list.
4. **`Filters({ colour, q, sort })`** in `src/app/items/Filters.jsx`, a client component:
   - a `<select>` labelled `Colour`: `All colours` (value `""`) then one option per colour (value: slug, text: name), showing `colour`,
   - a `<select>` labelled `Sort`: `Catalogue order` (`""`), `Cheapest first` (`price-asc`), `Dearest first` (`price-desc`), showing `sort`,
   - a search form with an input labelled `Search`, starting with `q`, and a `Search` button,
   - each change, or a search, calls `router.push(shopHref(...))` with the other two choices kept. The search text is trimmed.

## Recap

- Keep the shop's choices in the address: links can be shared, Back works, and the server draws the page already filtered.
- The server page awaits `searchParams` and does the filtering. The client component only changes the address.
- Keep filtering and address-building in plain functions, so they're easy to test and reuse.
- `URLSearchParams` builds query strings safely, and empty choices are left out.

## Common mistakes

**1. Filtering in the browser with state.**

```jsx
const [colour, setColour] = useState("");
const shown = items.filter((item) => !colour || item.colour === colour);
```

It looks the same, but the address never changes. A copied link shows everything, and the whole catalogue had to be sent to the browser.

Fix: push the choice into the address, and filter on the server from `searchParams`.

**2. Losing the other choices.**

```jsx
router.push(`/items?colour=${event.target.value}`);
```

A customer sorting by price who then picks a colour loses the sort.

Fix: build the address from all three choices: `shopHref({ colour: event.target.value, q, sort })`.

**3. Sorting the list you were given.**

```jsx
return items.sort((a, b) => a.paise - b.paise);
```

`sort` changes the array itself, so whoever passed it in now has a sorted list too.

Fix: copy first, `[...items]`, then filter and sort the copy.

**4. Comparing the slug with the colour's name.**

```jsx
items.filter((item) => item.colour === colour);
```

The address says `rani-pink`, but items say `Rani pink`, so nothing ever matches.

Fix: look the colour up with `colourBySlug(colour)` and compare with its `name`.

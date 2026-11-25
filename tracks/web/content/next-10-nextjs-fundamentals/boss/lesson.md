## What happened

On Friday, dhaaga.in moved to Next.js. By Saturday, Raju had three angry messages. Each customer had tapped Golu the elephant on the shop page and seen a page about Rani the pink elephant instead. They paid for Golu on WhatsApp and then asked why the photo was pink.

Arjun checked every item. Golu's page, the tote bag's page, the coaster set's page: all of them show Rani. A WhatsApp preview of any item says Rani too. Worse, an address that doesn't exist at all, like `dhaaga.in/items/DC-9999`, shows Rani instead of a "not found" page. So does a link someone cut short, like `/items/DC-104`.

Every item page is drawn by one file, `src/app/items/[id]/page.jsx`. Something in it never finds the item the address asks for.

```animate
title: Golu's address, Rani's page
kind: scene
nodes:
  - { id: phone, label: Customer, emoji: "📱" }
  - { id: page, label: ItemPage, emoji: "📄" }
  - { id: find, label: findItem, emoji: "🔎", value: "id: ?" }
  - { id: items, label: Catalogue, emoji: "🗄️", value: "items[0]: Rani" }
steps:
  - say: A customer opens dhaaga.in/items/DC-1042, Golu's page.
    send: { from: phone, to: page, label: "GET /items/DC-1042" }
  - say: Next.js calls the page with params. In Next.js 16, params is a Promise holding { id }.
    send: { from: page, to: find, label: "params (a Promise)" }
  - say: findItem reads params.id straight away. Ask yourself what a Promise's id property is.
    focus: [find]
  - say: No item's id matches that, so find gives undefined. The fallback steps in.
    send: { from: items, to: find, label: "no match" }
    set: { find: "fallback" }
  - say: The fallback is the first item in the catalogue. That's Rani, on every page and for every made-up id.
    send: { from: find, to: page, label: "Rani the pink elephant", back: true }
  - say: generateMetadata uses the same lookup, so every WhatsApp preview says Rani too.
    send: { from: page, to: phone, label: "Rani's page", back: true }
```

## What you need

This incident uses ticket 6 (dynamic routes, `await params` and `notFound()`) and ticket 8 (`generateMetadata` and the title template). There's no new syntax.

The file is `src/app/items/[id]/page.jsx`. It has three parts:

- `findItem(params)`: the lookup that both of the others use.
- `generateMetadata({ params })`: the tab title, description and WhatsApp preview.
- `ItemPage({ params })`: the page. It shows the item's name in an `<h1>`, its price like `₹450.00`, its stock like `7 left` (or `Sold out` at 0), and a link back to the shop.

The catalogue is in `src/lib/catalogue.js`. Its first item is `DC-1038`, Rani the pink elephant. Golu is `DC-1042`. The root layout's metadata has the title template `"%s | Dhaaga & Co."`. A `not-found.jsx` page already sits next to the item page, so calling `notFound()` shows it.

## The tiers

1. **Bronze** resolves the incident: each item's page shows that item. `/items/DC-1042` shows Golu the elephant, ₹450.00, 7 left. `/items/DC-1045` shows the Indigo tote bag. The Mint keychain says Sold out.
2. **Silver**: an id that isn't in the catalogue calls `notFound()`, and never falls back to another item. That includes made-up ids like `DC-9999` and cut-short ones like `DC-104`, which must not match `DC-1042`. Rani's own page, `DC-1038`, still shows Rani.
3. **Gold**: the WhatsApp preview is right. For `DC-1042`, `generateMetadata` returns the title `Golu the elephant` (the layout's template adds the shop's name, so don't add it again), the description `Golu the elephant, handmade in Pune. ₹450.00.`, and `openGraph` with the same title and description and `url: "/items/DC-1042"`. For an unknown id, it returns `{ title: "Item not found" }` without throwing.

## Recap

- In Next.js 16, `params` is a Promise. Reading `params.id` without `await` gives `undefined`, so no lookup can match.
- A silent fallback like `?? items[0]` hides that bug: every page quietly shows the first item.
- Match ids exactly with `===`. Prefix matching lets a cut-short address show the wrong item.
- When nothing matches, call `notFound()` so the customer sees the not-found page.
- Metadata needs the same care: the right item, an exact title without a doubled shop name, and a sensible title for unknown ids.

## Common mistakes

**1. Awaiting params but keeping the fallback.**

```jsx
const { id } = await params;
return items.find((item) => item.id === id) ?? items[0];
```

Bronze passes, but `/items/DC-9999` still shows Rani. A customer with a broken link sees the wrong product, priced and ready to order.

Fix: return `null` when nothing matches, and let the page call `notFound()`.

**2. Calling notFound() inside generateMetadata for unknown ids.**

```jsx
if (!item) notFound();
```

It's allowed in Next.js, but gold asks for `{ title: "Item not found" }` here, so the tab still has a clear title. The page itself is what calls `notFound()`.

Fix: in `generateMetadata`, return `{ title: "Item not found" }` when there's no item.

**3. Keeping the shop's name in the title.**

```jsx
title: `${item.name} | Dhaaga & Co.`,
```

The tab says `Golu the elephant | Dhaaga & Co. | Dhaaga & Co.`, because the root layout's template adds the name too.

Fix: `title: item.name`.

## Hook

A parcel has a label on the outside. You can tell what's inside and who sent it without opening it. A courier sorts thousands of parcels by their labels alone.

A web page has labels too, hidden in its `<head>`. Browser tabs read them to name the tab. Google reads them for search results. WhatsApp reads them to build the little preview card. Right now every Dhaaga page has the same label, so they all look alike from outside.

## Concept

### What metadata is

```callout jargon
**Metadata** is information about a page, rather than on it. It lives in the `<head>` as tags like `<title>` and `<meta name="description">`. Visitors don't see it on the page itself, but tabs, search engines and link previews do.
```

```html
<head>
  <title>Indigo crochet | Dhaaga & Co.</title>
  <meta name="description" content="2 handmade pieces in Indigo, from ₹450.00." />
  <meta property="og:title" content="Indigo crochet" />
  <meta property="og:url" content="https://dhaaga.in/colours/indigo" />
</head>
```

In Next.js you never write these tags by hand. You export plain objects, and Next.js writes the tags for you.

### Syntax: the metadata object

```jsx
export const metadata = {
  title: "Visit us",
  description: "Find the FC Road stall and the Aundh shop.",
};
```

A `page.jsx` or `layout.jsx` can export a constant called `metadata`. Next.js reads it on the server and writes the matching tags into the page's `<head>`.

- It only works in server components. A file with `"use client"` can't export metadata.
- A page's metadata adds to its layouts' metadata. Where both set the same field, the page wins.

### Syntax: a title template

```jsx
export const metadata = {
  title: {
    default: "Dhaaga & Co.",
    template: "%s | Dhaaga & Co.",
  },
};
```

Put this in the root layout. Then each page sets only its own short title, and the template adds the shop's name:

- `template` is used for the pages below the layout. `%s` is replaced by the page's own title.
- `default` is used for a page that sets no title at all.

```animate
title: Filling in the template
kind: code
language: js
code: |
  const template = "%s | Dhaaga & Co.";
  const titles = ["Indigo crochet", "Visit us"];
  for (const title of titles) {
    console.log(template.replace("%s", title));
  }
steps:
  - line: 1
    vars: { template: '"%s | Dhaaga & Co."' }
    say: The layout's template. %s marks the spot where a page's own title will go.
  - line: 2
    vars: { titles: '["Indigo crochet", "Visit us"]' }
    say: Two pages, each with its own short title.
  - line: 3
    vars: { title: '"Indigo crochet"' }
    say: Take the first page's title.
  - line: 4
    out: "Indigo crochet | Dhaaga & Co."
    say: Swap it in for %s. That's what the browser tab shows.
  - line: 3
    vars: { title: '"Visit us"' }
    say: Now the second page.
  - line: 4
    out: "Visit us | Dhaaga & Co."
    say: Same template, different title. The shop's name is written once, in the layout.
```

So a page that sets `title: "Indigo crochet | Dhaaga & Co."` itself ends up with the name twice.

### Syntax: generateMetadata

```jsx
export async function generateMetadata({ params }) {
  const { topic } = await params;
  return { title: `Help with ${topic}` };
}
```

A fixed object can't know which colour or item the address is for. For pages with dynamic segments, export an `async` function called `generateMetadata` instead. Next.js calls it with the same `params` (and `searchParams`) the page gets, and uses the object it returns.

- `params` is a Promise here too, so `await` it.
- A file exports either `metadata` or `generateMetadata`, never both.
- It can look up data, just like the page. Next.js shares repeated data requests between the two.

### Open Graph: the preview card

```callout jargon
**Open Graph** is a set of `og:` tags that apps like WhatsApp, Facebook and LinkedIn read to build a link's preview card: its title, description, picture and address. In Next.js, they go in an `openGraph` object inside the metadata.
```

```jsx
openGraph: {
  title: "Indigo crochet",
  description: "2 handmade pieces in Indigo, from ₹450.00.",
  url: "/colours/indigo",
},
```

```animate
title: Sana shares the Indigo page on WhatsApp
kind: scene
nodes:
  - { id: sana, label: Sana's phone, emoji: "📱" }
  - { id: wa, label: WhatsApp's preview bot, emoji: "🤖" }
  - { id: next, label: dhaaga.in, emoji: "🖥️" }
  - { id: card, label: Preview card, emoji: "🪪", value: empty }
steps:
  - say: Sana pastes dhaaga.in/colours/indigo into a chat. WhatsApp wants to show a preview.
    send: { from: sana, to: wa, label: "a link" }
  - say: WhatsApp's bot fetches the page. It reads only the HTML. It never runs JavaScript.
    send: { from: wa, to: next, label: "GET /colours/indigo" }
  - say: Next.js runs generateMetadata on the server and writes the og tags into the head.
    focus: [next]
  - say: The HTML comes back with og:title, og:description and og:url already in it.
    send: { from: next, to: wa, label: "<head> with og: tags", back: true }
  - say: The bot builds the card from those tags. The chat now shows a real preview.
    set: { card: "Indigo crochet: 2 pieces, from ₹450.00" }
```

This only works because Next.js renders on the server. With the old React app, the bot saw an empty page.

### Syntax: metadataBase

```jsx
metadataBase: new URL("https://dhaaga.in"),
```

Preview cards need full addresses, like `https://dhaaga.in/colours/indigo`. Set `metadataBase` once in the root layout. Then any page can write a short address, like `url: "/colours/indigo"`, and Next.js turns it into a full one in the tags.

### Icons and share pictures from files

Some metadata comes from files instead of objects. Drop them in the `app` folder:

- `favicon.ico` or `icon.png` becomes the tab icon.
- `opengraph-image.jpg` becomes the picture on preview cards for that folder's pages.

Next.js writes the tags for these too.

## Worked example

The help centre's topic pages, from ticket 6, with their own titles:

```jsx
// src/app/help/[topic]/page.jsx
const TOPICS = {
  delivery: { title: "Delivery", answer: "Delivery inside Pune costs ₹60, and it's free above ₹999." },
  returns: { title: "Returns", answer: "Unused items can be returned within 7 days." },
};

export async function generateMetadata({ params }) {
  const { topic } = await params;
  const found = TOPICS[topic];
  if (!found) return { title: "Help topic not found" };
  return {
    title: `${found.title} help`,
    description: found.answer,
    openGraph: { title: `${found.title} help`, description: found.answer, url: `/help/${topic}` },
  };
}
```

- It awaits `params` and looks up the topic, exactly as the page does.
- An unknown topic still gets a sensible title. The page itself calls `notFound()`.
- The title is just `Delivery help`. With the root layout's template, the tab says `Delivery help | Dhaaga & Co.`.
- `url` is short. `metadataBase` in the root layout makes it `https://dhaaga.in/help/delivery`.

## Try it

Change the template in the "Filling in the template" animation to `"%s - Dhaaga & Co."` in your head, and work out both lines. Then imagine a page whose title is already `"Visit us | Dhaaga & Co."`: what does the tab say? That double name is the mistake this ticket's checks guard against.

## Recap

- Metadata is information about a page, written as tags in the `<head>`, for tabs, search engines and previews.
- Export a `metadata` object from a layout or page. Next.js writes the tags.
- A title template in the root layout, `"%s | Dhaaga & Co."`, adds the shop's name to every page's own title.
- For dynamic pages, export `async function generateMetadata({ params })` and `await params`.
- `openGraph` builds link previews, and `metadataBase` turns short addresses into full ones.

## Common mistakes

**1. Adding the shop's name yourself.**

```jsx
return { title: `${colour.name} crochet | Dhaaga & Co.` };
```

The tab says `Indigo crochet | Dhaaga & Co. | Dhaaga & Co.`, because the layout's template adds the name again.

Fix: return only the page's own title, `"Indigo crochet"`.

**2. Exporting both metadata and generateMetadata.**

```jsx
export const metadata = { title: "Colours" };
export async function generateMetadata({ params }) {
  // ...
}
```

Next.js stops with an error saying a file can't export both.

Fix: keep one. For a dynamic page, keep `generateMetadata`.

**3. Metadata in a client component.**

```jsx
"use client";
export const metadata = { title: "Shop" };
```

Next.js refuses, because metadata is only read from server components.

Fix: keep the page on the server and move the interactive part into its own client component.

**4. Forgetting to await params.**

```jsx
export async function generateMetadata({ params }) {
  const colour = colourBySlug(params.colour);
  return { title: colour ? `${colour.name} crochet` : "Colour not found" };
}
```

Every colour page's tab says `Colour not found`, because `params.colour` is `undefined`.

Fix: `const { colour: slug } = await params;`, as in the page.

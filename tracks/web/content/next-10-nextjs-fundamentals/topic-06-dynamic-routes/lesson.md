## Hook

A school report card has the same printed form for every student. Only the name and the marks change. Nobody designs a new card for each child: they fill one form with different details.

Dhaaga has 6 items today and will have 600 one day. Making a folder for each would be madness. A dynamic route is the printed form: one page file, filled in from the address.

## Concept

### Syntax: a dynamic segment

```text
src/app/help/[topic]/page.jsx   →  /help/delivery, /help/returns, /help/anything
```

A folder name in **square brackets** is a **dynamic segment**. It matches any value in that part of the address. The name inside the brackets, here `topic`, is what you call the value.

```callout jargon
A **dynamic segment** is a part of the address that can be anything, like the id in `/items/DC-1042`. Its value is a **param** (short for parameter). Next.js collects them into an object called `params`: `{ topic: "returns" }`.
```

It's the same idea as `:id` in React Router, from react-09, with brackets instead of a colon.

### Syntax: reading params

```jsx
export default async function HelpTopicPage({ params }) {
  const { topic } = await params;
  return <h1>Help with {topic}</h1>;
}
```

Next.js passes the page a `params` prop. In Next.js 16, **`params` is a Promise**, so you `await` it before reading it. That's one more reason pages are `async`.

```js run
async function main() {
  const params = Promise.resolve({ topic: "returns" });
  console.log(params.topic);
  const { topic } = await params;
  console.log(topic);
}
main();
```

Reading `params.topic` straight away gives `undefined`, because a Promise doesn't have a `topic` property. Only the value it resolves to does.

```callout tip
Why a Promise? It lets Next.js start work on the page before the address details are ready, and wait for them only where they're used. Older tutorials read `params.id` directly. That was Next.js 14 and before.
```

### Following /colours/indigo

```animate
title: One file, many addresses
kind: scene
nodes:
  - { id: phone, label: Phone, emoji: "📱" }
  - { id: router, label: Next.js, emoji: "🖥️" }
  - { id: folder, label: "colours/[colour]", emoji: "📁" }
  - { id: page, label: ColourPage, emoji: "📄", value: idle }
steps:
  - say: A customer opens dhaaga.in/colours/indigo.
    send: { from: phone, to: router, label: "GET /colours/indigo" }
  - say: Next.js finds the colours folder. Inside, there's no folder called indigo, but there is one in brackets.
    focus: [folder]
  - say: "[colour] matches anything, so Next.js notes the value: colour is indigo."
    send: { from: router, to: page, label: "params: { colour: \"indigo\" }" }
  - say: The page awaits params, looks up Indigo, and draws its items.
    set: { page: "Indigo: 2 items" }
  - say: The HTML goes back to the phone. /colours/mint would use the very same file, with colour set to mint.
    send: { from: page, to: phone, label: "HTML: Indigo", back: true }
```

### Syntax: notFound

```jsx
import { notFound } from "next/navigation";

if (!colour) notFound();
```

A dynamic segment matches anything, including typos like `/colours/purple`. When the value doesn't exist, call `notFound()`. It stops the page and shows the 404 page instead. Like `redirect()`, it works by throwing, so no code after it runs.

Without it, the page would carry on with `null` and crash on something like `colour.name`.

### Syntax: searchParams

```jsx
export default async function Page({ params, searchParams }) {
  const { sort } = await searchParams;
}
```

The part of the address after `?` holds **search params**: `/colours/indigo?sort=price` has `sort` set to `"price"`. Pages get them in a `searchParams` prop, which is also a Promise in Next.js 16.

- A value is a string, like `"price"`. It's `undefined` when it's missing.
- If a name appears twice, like `?tag=a&tag=b`, the value is an array.
- Only pages get `searchParams`. Layouts don't, because they aren't drawn again on every navigation.

### Syntax: generateStaticParams

```jsx
export function generateStaticParams() {
  return [{ topic: "delivery" }, { topic: "returns" }];
}
```

When you know the values ahead of time, export `generateStaticParams` from the page. It returns one params object per page. During `next build`, Next.js renders each of those pages once and saves the HTML, so they load instantly.

```callout jargon
Rendering pages during the build is **static generation**. Pages made this way are files waiting to be sent, so they're the fastest kind. An address that wasn't in the list is still rendered when someone first asks for it.
```

### Catch-all segments

```text
src/app/guides/[...slug]/page.jsx   →  /guides/amigurumi/first-steps
```

Three dots, `[...slug]`, match one or more segments at once. `params.slug` is then an array: `["amigurumi", "first-steps"]`. Use it for deep pages like guides. Most routes only need one `[id]`.

## Worked example

Here are the help centre's topic pages, using every part of this ticket:

```jsx
// src/app/help/[topic]/page.jsx
import { notFound } from "next/navigation";

const TOPICS = {
  delivery: "Delivery inside Pune costs ₹60, and it's free above ₹999.",
  returns: "Unused items can be returned within 7 days.",
};

export function generateStaticParams() {
  return Object.keys(TOPICS).map((topic) => ({ topic }));
}

export default async function HelpTopicPage({ params, searchParams }) {
  const { topic } = await params;
  const answer = TOPICS[topic];
  if (!answer) notFound();

  const { lang } = await searchParams;
  return (
    <>
      <h1>{topic === "delivery" ? "Delivery" : "Returns"}</h1>
      <p>{answer}</p>
      {lang === "mr" && <p>Marathi help is coming soon.</p>}
    </>
  );
}
```

- `[topic]` makes one file serve `/help/delivery` and `/help/returns`.
- `generateStaticParams` lists both, so they're built ahead of time.
- `await params` gives the topic. An unknown one calls `notFound()`, and the 404 page shows.
- `await searchParams` gives `lang` from `?lang=mr`, or `undefined` when it isn't there.

## Try it

Run the snippet in "Syntax: reading params" again, but swap the two `console.log` lines around the `await`. The `undefined` line is the bug you'd see in a page that forgot to await.

## Recap

- A folder in square brackets, like `[colour]`, is a dynamic segment: one page file for many addresses.
- In Next.js 16, `params` and `searchParams` are Promises. `await` them before reading.
- Call `notFound()` when the value doesn't exist, so typos get a 404 page instead of a crash.
- `searchParams` holds the `?` part of the address, as strings, and only pages get it.
- `generateStaticParams` lists the values to build ahead of time.

## Common mistakes

**1. Reading params without await.**

```jsx
export default async function ColourPage({ params }) {
  const colour = colourBySlug(params.colour);
  // ...
}
```

`params.colour` is `undefined`, because `params` is a Promise. Every address finds nothing.

Fix: `const { colour: slug } = await params;`.

**2. The wrong brackets.**

```text
src/app/colours/{colour}/page.jsx
src/app/colours/:colour/page.jsx
```

Neither is a dynamic segment: they're folders with odd names. `/colours/indigo` shows the 404 page.

Fix: square brackets, `[colour]`.

**3. Forgetting notFound.**

```jsx
const colour = colourBySlug(slug);
return <h1>{colour.name}</h1>;
```

For `/colours/purple`, the page crashes with `TypeError: Cannot read properties of null (reading 'name')`. The customer sees an error page instead of a friendly 404.

Fix: `if (!colour) notFound();` before using it.

**4. Sorting the original list.**

```jsx
const list = items.sort((a, b) => a.paise - b.paise);
```

`sort` changes the array it's called on. If that array is shared, every later reader sees it sorted, even without `?sort=price`.

Fix: sort a copy: `[...items].sort((a, b) => a.paise - b.paise)`.

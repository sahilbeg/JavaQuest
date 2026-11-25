## Hook

A photo frame stays on the wall while you swap the photo inside it. You don't build a new frame for every picture.

Dhaaga's site has the same header, delivery banner and footer on every page. Copying them into each page means three copies to keep in step, and one is always forgotten. A layout is the frame: write it once, and every page sits inside it.

## Concept

### Syntax: a layout

```jsx
export default function ShopLayout({ children }) {
  return <section>{children}</section>;
}
```

A **layout** is the default export of a file called `layout.jsx`. Next.js calls it with the page in its `children` prop. Whatever the layout returns around `{children}` appears on every page in that folder, and in every folder below it.

```callout jargon
The **children prop** holds whatever is placed inside a component. You met it in react-08: `<Card><p>Hi</p></Card>` gives `Card` the `<p>` as `children`. Next.js passes each page to its layout the same way.
```

### The root layout

The layout directly in `app` is the **root layout**. Every project needs one, and it wraps every page on the site. It's also the only place that writes the `<html>` and `<body>` tags, because Next.js has no `index.html`.

```jsx
export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
```

- `lang="en"` tells screen readers and search engines the page's language.
- Only the root layout has `<html>` and `<body>`. A nested layout returns ordinary elements, like a `<div>`.

### Nested layouts

A layout in a folder wraps only the pages in that folder and below. Layouts **nest**: the root layout wraps the help layout, which wraps the help page.

```text
src/app/layout.jsx                  wraps every page
src/app/help/layout.jsx             wraps every page under /help
src/app/help/delivery/page.jsx      the page at /help/delivery
```

For `/help/delivery`, Next.js builds this, from the outside in:

```jsx
<RootLayout>
  <HelpLayout>
    <DeliveryPage />
  </HelpLayout>
</RootLayout>
```

The About page at `/about` gets only `RootLayout`, because it isn't inside `help`.

### Moving between pages keeps the layout

When a customer moves from one page to another, Next.js swaps only the part that changed. The layouts stay on screen, and their state stays too.

```animate
title: From Delivery to Returns in the help centre
kind: scene
nodes:
  - { id: root, label: RootLayout, emoji: "🖼️", value: "search: golu" }
  - { id: help, label: HelpLayout, emoji: "🗂️", value: Help centre }
  - { id: page, label: Page, emoji: "📄", value: Delivery }
links:
  - { from: root, to: help }
  - { from: help, to: page }
steps:
  - say: The customer is on /help/delivery. They've typed golu into a search box in the header, which lives in the root layout.
    focus: [root]
  - say: Next.js has drawn three nested parts. The root layout wraps the help layout, which wraps the Delivery page.
    focus: [root, help, page]
  - say: The customer taps Returns, which goes to /help/returns. Both addresses are inside the same two layouts.
    focus: [page]
  - say: Only the page changes. Next.js renders the Returns page and puts it where Delivery was.
    set: { page: Returns }
  - say: The layouts are not drawn again. The search box still says golu, because its state was never thrown away.
    focus: [root, help]
```

```callout jargon
**State** that survives navigation is said to be **preserved**. Because layouts are preserved, a layout is the right home for things that should stay put, like a header, a cart count or a search box.
```

### What a layout can't do

Because a layout isn't drawn again when the page changes, it can't follow every detail of the address. Next.js doesn't give layouts the `?search=...` part of the address, for example.

If you really need a wrapper that starts fresh on every navigation, name the file `template.jsx` instead. It works like a layout, but Next.js mounts a new copy each time. Most sites never need one.

### What goes where

| Put it in... | When it... |
|---|---|
| the root layout | belongs on every page: header, banner, footer |
| a nested layout | belongs on one section, like the help centre |
| the page | belongs to that one address only |

## Worked example

Here is a layout for the account section, with its own heading and a sign-out note:

```jsx
// src/app/account/layout.jsx
export default function AccountLayout({ children }) {
  return (
    <div className="account">
      <h2>Your account</h2>
      {children}
      <p>Not you? Sign out from the menu.</p>
    </div>
  );
}
```

- The file is called `layout.jsx`, so Next.js uses it for `/account` and everything below it.
- It's the default export, and it takes `{ children }`.
- The heading comes before `{children}`, so it shows above every account page. The note comes after, so it shows below.
- It returns a `<div>`, not `<html>`: only the root layout does that.

`/account/orders` now shows the root layout's header, then "Your account", then the orders page, then the note, then the root layout's footer.

## Try it

In this ticket's code, swap the order in the root layout: put `<footer>` above `<main>`. Run the checks. The footer test fails, because the footer must come after the page. Put it back.

Then remove `{children}` from `HelpLayout`. Every help check about the page fails: the frame is there, but the photo is gone.

## Recap

- A layout is the default export of `layout.jsx`. It gets the page as `children` and wraps every page in its folder and below.
- The root layout in `app` is required, wraps every page, and is the only place for `<html>` and `<body>`.
- Layouts nest from the outside in: root layout, then section layouts, then the page.
- Moving between pages keeps the layouts and their state. Only the page is swapped.

## Common mistakes

**1. Forgetting {children}.**

```jsx
export default function RootLayout() {
  return (
    <html lang="en">
      <body>
        <header>Dhaaga & Co.</header>
      </body>
    </html>
  );
}
```

Every page shows only the header. No error appears, because the layout simply never puts the page anywhere.

Fix: take `{ children }` as a prop and place `{children}` where the page belongs.

**2. Writing <html> and <body> in a nested layout.**

```jsx
export default function HelpLayout({ children }) {
  return (
    <html>
      <body>{children}</body>
    </html>
  );
}
```

The page now has two `<html>` elements, one inside the other. React warns that `<html>` cannot be a child of `<body>`, and the page can break during hydration.

Fix: only the root layout writes `<html>` and `<body>`. Nested layouts return a `<div>` or a `<section>`.

**3. Copying the banner into every page.**

```jsx
export default function AboutPage() {
  return (
    <>
      <p>Free delivery in Pune above ₹999</p>
      <h1>Our story</h1>
    </>
  );
}
```

It works until the offer changes. Then one page still says ₹999 while the others say something else.

Fix: put the banner in the root layout, once.

**4. Expecting a layout to start fresh on every page.**

```jsx
// src/app/layout.jsx holds a "Thanks for visiting!" popup that should show on each new page
```

The popup shows on the first page only. Layouts keep their state when the customer moves between pages.

Fix: use `template.jsx`, which starts fresh on each navigation, or put the popup in the page.

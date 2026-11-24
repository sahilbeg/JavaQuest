## Hook

A parcel from Dhaaga has two parts. Inside the box is the crochet toy, which the customer unwraps and enjoys. Outside is the label: who it's for, where it's from, "fragile, handle with care". Nobody admires the label, but without it the parcel never arrives safely.

A web page is the same. The `body` is what people see. The `head` is the label: facts about the page that browsers, phones and search engines read before anything else. Dhaaga's label is nearly blank.

## Concept

Everything inside `<head>` is information *about* the page. None of it shows on the page itself. Instead, it changes how the browser treats the page: which characters to use, what to write on the tab, how wide to draw it on a phone, and what a search engine shows.

```callout jargon
**Metadata** means data about data. A page's metadata is facts about the page, like its title, language and description, rather than its content.
```

### What goes where

- `head` holds metadata: `meta` tags, the `title`, and `link`s to other files such as stylesheets and icons.
- `body` holds everything the visitor sees: headings, text, images, forms.

Don't put visible content in the `head`. The browser moves it into the `body` anyway, and your page's structure gets muddled.

### Syntax: the charset

```html
<meta charset="utf-8">
```

You met this line in the last project: it tells the browser which **character encoding** the file uses. UTF-8 can store every character: ₹, धागा, and emoji.

```callout jargon
A **character encoding** is the rule a computer uses to turn the bytes in a file into letters. UTF-8 is the encoding the whole web now uses.
```

It must be the **first thing** in the `head`. Browsers look for it in the first 1024 bytes of the file. If something long comes first, the browser may already have guessed the wrong encoding and drawn garbage, like `â‚¹` instead of `₹`.

### Syntax: the title

```html
<title>Catalogue | Dhaaga &amp; Co.</title>
```

The `title` is the page's name outside the page. It shows on the browser tab, in bookmarks and in history. It's the blue link in search results. A screen reader reads it first when the page opens.

A good title is short and puts the most specific part first: "Catalogue | Dhaaga & Co.", then "Golu the elephant | Dhaaga & Co." on his own page. With many tabs open, each tab shows only its first few words.

### Syntax: the viewport

```html
<meta name="viewport" content="width=device-width, initial-scale=1">
```

Long ago, websites were all built for big computer screens. When phones arrived, they played safe. They pretended to be about 980 pixels wide, drew the page that big, then shrank it to fit. The text came out tiny.

```callout jargon
The **viewport** is the area of the screen where the page is drawn. On a phone, it's the whole screen below the address bar.
```

The viewport tag tells the phone: "this page is ready for small screens. Use your real width, and don't zoom out." You write this exact line on every page you ever make. Watch what it changes:

```animate
title: A phone opens the catalogue page
kind: scene
nodes:
  - { id: phone, label: "Kavya's phone", emoji: "📱", value: "360 px wide" }
  - { id: page, label: "Catalogue page", emoji: "📄", row: 0, col: 3 }
  - { id: screen, label: "What she sees", emoji: "👀", row: 1, col: 1, value: "?" }
steps:
  - say: Kavya's phone screen is 360 pixels wide. It opens the catalogue page.
    send: { from: phone, to: page, label: "GET /index.html" }
  - say: The page's head has no viewport tag. The phone plays safe and pretends to be 980 pixels wide.
    set: { phone: "pretends 980 px" }
    send: { from: page, to: phone, label: "page, no viewport", back: true }
  - say: It draws the page 980 pixels wide, then shrinks it to fit 360. All the text shrinks with it.
    set: { screen: "tiny text, zoomed out" }
    focus: [screen]
  - say: Now the page has the viewport tag, with width=device-width. The phone uses its real width.
    set: { phone: "360 px wide" }
    send: { from: page, to: phone, label: "page + viewport", back: true }
  - say: The page is drawn 360 pixels wide, with text at its normal size. CSS can then arrange it for a phone.
    set: { screen: "readable text" }
    focus: [screen]
```

### Syntax: the description

```html
<meta name="description" content="Handmade crochet coasters, toys, tote bags and baby booties from Pune.">
```

The `description` is a one- or two-sentence summary of the page. Search engines often show it under the title in their results. Without one, they pick a line from the page, often a strange one.

Keep it under about 160 characters, because search results cut off longer ones. Write it for people: what's on the page, and why they'd want to visit.

```callout tip
`meta` tags with a `name` all follow the same shape: `<meta name="what" content="the value">`. Only `charset` is different.
```

### Syntax: a link to another file

```html
<link rel="icon" href="images/golu-icon.png">
```

The `link` element connects the page to another file. It's a void element. `rel` (relation) says what the file is to this page, and `href` says where it is.

- `rel="icon"` is the little picture on the browser tab, called a **favicon**.
- `rel="stylesheet"` loads a CSS file. You'll write `<link rel="stylesheet" href="style.css">` all through the next project.

Don't mix up `link` and `a`. An `a` is a link people click. A `link` in the head is a connection the browser follows by itself.

### A complete head, in order

```html
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Catalogue | Dhaaga &amp; Co.</title>
  <meta name="description" content="Handmade crochet from Pune.">
  <link rel="icon" href="images/golu-icon.png">
</head>
```

The charset comes first. The order of the rest doesn't matter to the browser, but this order is the one most teams use.

## Worked example

Here's the head for Golu's own page, `items/dc-1042.html`:

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Golu the elephant | Dhaaga &amp; Co.</title>
  <meta name="description" content="Golu, our best-loved amigurumi elephant, crocheted by hand in Pune. ₹450, in five colours.">
  <link rel="icon" href="../images/golu-icon.png">
</head>
```

1. `lang="en"` on `html` and the charset line come first, before anything that could be misread.
2. The viewport line is the same on every page.
3. The title starts with the most specific part, Golu, so it stands out among open tabs.
4. The description is one short sentence, well under 160 characters, about this item only.
5. The icon's `href` starts with `../`, because this page sits one folder down, in `items/`.

## Try it

Open this ticket's editor and look at the head:

- Change the title's text. In the real site, the tab would change. The preview has no tab, so the checks look at the title for you.
- Add a paragraph inside the `head`, like `<p>Hello</p>`. It shows on the page anyway, because the browser moves it into the body. Remove it.
- Count the characters in your description. Under 160 keeps it whole in search results.

## Recap

- The `head` holds facts about the page. The `body` holds what visitors see.
- `<meta charset="utf-8">` comes first in the head, so every character shows right.
- The `title` names the page on tabs, bookmarks and search results. Put the specific part first.
- The viewport tag makes phones use their real width: `width=device-width, initial-scale=1`.
- `<meta name="description">` gives search engines a summary, and `<link rel="icon">` adds the tab icon.

## Common mistakes

**1. Putting the charset line late.**

```html
<head>
  <title>Catalogue | Dhaaga &amp; Co.</title>
  <meta charset="utf-8">
</head>
```

It often still works, but if anything long comes first, the browser guesses the encoding before it sees the line. Then ₹ can turn into garbage. Make `<meta charset="utf-8">` the very first line in the head.

**2. Forgetting the viewport tag.**

```html
<head>
  <meta charset="utf-8">
  <title>Catalogue | Dhaaga &amp; Co.</title>
</head>
```

On a phone, the page is drawn about 980 pixels wide and shrunk, so the text is tiny. Add `<meta name="viewport" content="width=device-width, initial-scale=1">`.

**3. Using content where charset belongs.**

```html
<meta content="utf-8">
```

The browser ignores this, because a `meta` with only `content` means nothing. The charset tag has its own attribute: `<meta charset="utf-8">`.

**4. The same title on every page.**

```html
<title>Dhaaga &amp; Co.</title>
```

With five Dhaaga tabs open, they all look the same, and so do the search results. Name each page first: `Catalogue | Dhaaga &amp; Co.`, `Golu the elephant | Dhaaga &amp; Co.`.

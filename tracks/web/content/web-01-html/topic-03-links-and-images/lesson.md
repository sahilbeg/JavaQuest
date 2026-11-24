## Hook

At the Sunday haat, Raju points at things. "That's Golu, our elephant. Want one? Message us on WhatsApp." Pointing and showing are how a stall sells.

A web page points with **links** and shows with **images**. Right now the catalogue page can do neither. Customers read about Golu but can't see him, and there's no way to reach Dhaaga from the page.

## Concept

Links are what make the web a web. Every page can point to any other page, or to a spot further down itself. Images make a page worth looking at. Both use attributes to say *where* things are.

### Syntax: a link

```html
<a href="https://wa.me/919876543210">Order on WhatsApp</a>
```

A link is an `a` element. The name comes from "anchor". Its `href` attribute holds the address to go to. The content, here "Order on WhatsApp", is the text people click or tap.

```callout jargon
**href** is short for "hypertext reference". It's the address a link goes to. **Hypertext** just means text with links in it.
```

Write link text that says where the link goes. Screen reader users often hear a list of every link on the page, with no text around them. "Order on WhatsApp" makes sense alone. "Click here" doesn't.

### Syntax: absolute and relative URLs

```html
<a href="https://wa.me/919876543210">Order on WhatsApp</a>
<a href="items/dc-1042.html">Meet Golu</a>
```

A **URL** is a web address. There are two kinds of URL you can put in `href`:

- An **absolute URL** starts with `https://` and the site's name. It works from anywhere. Use it to link to another site, like WhatsApp.
- A **relative URL** has no `https://`. The browser starts from the current page's folder and follows the path. Use it to link to files on your own site.

```callout jargon
A **URL** (Uniform Resource Locator) is the full address of something on the web, like `https://dhaaga.in/items/dc-1042.html`.
```

If the catalogue page is `https://dhaaga.in/index.html`, then `items/dc-1042.html` means `https://dhaaga.in/items/dc-1042.html`. Relative links keep working when the site moves to a new name. Watch the browser follow one:

```animate
title: Following a relative link
kind: scene
nodes:
  - { id: phone, label: "Phone browser", emoji: "📱", value: "dhaaga.in/index.html" }
  - { id: server, label: "dhaaga.in server", emoji: "🖥️", row: 0, col: 3 }
  - { id: item, label: "items/dc-1042.html", emoji: "🐘", row: 1, col: 3 }
steps:
  - say: A customer is on the catalogue page, dhaaga.in/index.html. She taps the link "Meet Golu".
    focus: [phone]
  - say: The href is items/dc-1042.html. It has no https://, so it's relative to the current page's folder.
    set: { phone: "items/dc-1042.html?" }
  - say: The browser builds the full URL, https://dhaaga.in/items/dc-1042.html, and asks the server for it.
    send: { from: phone, to: server, label: "GET /items/dc-1042.html" }
  - say: The server finds the file in its items folder.
    show: [item]
    send: { from: server, to: item, label: "find file" }
  - say: The page comes back, and the address bar now shows Golu's page.
    send: { from: server, to: phone, label: "200 OK, Golu's page", back: true }
    set: { phone: "dhaaga.in/items/dc-1042.html" }
```

### Syntax: a link to a spot on the same page

```html
<a href="#catalogue">See the catalogue</a>
<h2 id="catalogue">Catalogue</h2>
```

An `href` that starts with `#` points to a spot on the same page. The browser scrolls to the element whose `id` matches the words after `#`. This is handy on a long page, like a contents list at the top.

The `id` must match exactly, and only one element may have it. `#Catalogue` won't find `id="catalogue"`, because ids care about capital letters.

### Opening in a new tab

You may see `target="_blank"` on links. It opens the link in a new tab. Use it rarely. Most people expect a link to open in the same tab, and the Back button stops working in a new one.

### Syntax: an image

```html
<img src="images/golu.jpg" alt="Golu, a small crocheted elephant in Rani pink yarn" width="300" height="300">
```

`img` shows a picture. It's a void element, so it has no closing tag. Its attributes do all the work:

- `src` (source) is the picture's URL. Like `href`, it can be absolute or relative.
- `alt` (alternative text) describes the picture in words.
- `width` and `height` give its size in pixels. The browser keeps a space that size while the picture loads, so the text doesn't jump.

```callout jargon
A **pixel** is one tiny dot on a screen. `width="300"` means 300 dots wide.
```

### Alt text: the picture in words

`alt` is read aloud by screen readers. It also shows on the page when the picture can't load, on slow 3G or with a wrong `src`. Search engines read it too.

Good alt text says what someone would need to know if they couldn't see the picture:

- **Describe what matters**: "Golu, a small crocheted elephant in Rani pink yarn".
- **Skip "image of" or "photo of"**. Screen readers already say "image".
- **Never use the file name**. "golu.jpg" tells a listener nothing.
- **Keep it short**: one sentence is plenty.

```callout tip
To check your alt text, change `src` to a name that doesn't exist. The browser shows your alt text in the picture's place. Does it still tell the story?
```

## Worked example

Here is a small part for Golu's own page, `items/dc-1042.html`:

```html
<h1>Golu the elephant</h1>
<img src="../images/golu.jpg" alt="Golu, a small crocheted elephant in Rani pink yarn" width="300" height="300">
<p>Item DC-1042. ₹450.00</p>
<p><a href="https://wa.me/919876543210">Order Golu on WhatsApp</a></p>
<p><a href="../index.html">Back to the catalogue</a></p>
```

1. `img` shows Golu's photo. `../` means "go up one folder first", because this page sits inside `items/`.
2. The `alt` text describes Golu for anyone who can't see the photo.
3. `width` and `height` save his space before the photo arrives.
4. The WhatsApp link is absolute, because it goes to another site.
5. The last link is relative. It climbs out of `items/` and back to the catalogue.

## Try it

Paste this into the editor's body:

```html
<p><a href="#prices">Jump to the prices</a></p>
<img src="images/coasters.jpg" alt="Four round crocheted coasters in Marigold and Indigo" width="200" height="200">
<p style="margin-top: 1500px" id="prices">Coaster set: ₹150.00</p>
```

- There's no `coasters.jpg` in the editor, so the preview shows the alt text instead. Does it describe the coasters well?
- Click "Jump to the prices". The page scrolls down to the element with `id="prices"`.
- Change `href` to `#Prices`, with a capital P. The jump stops working.

## Recap

- A link is `<a href="...">text</a>`. The text should say where the link goes.
- Absolute URLs start with `https://` and work from anywhere. Relative URLs start from the current page's folder.
- `href="#name"` jumps to the element with `id="name"` on the same page.
- An image is `<img src="..." alt="..." width="..." height="...">`, with no closing tag.
- Alt text describes the picture for people who can't see it, and shows when the picture fails to load.

## Common mistakes

**1. Leaving out alt.**

```html
<img src="images/golu.jpg">
```

A screen reader may read out the file name, "golu dot jpg", or just say "image". Always add an `alt` that describes the picture.

**2. Forgetting https:// on another site's address.**

```html
<a href="wa.me/919876543210">Order on WhatsApp</a>
```

With no `https://`, this is a relative URL. The browser looks for a folder called `wa.me` on dhaaga.in, and the customer gets a "page not found" error. Write the full `https://wa.me/919876543210`.

**3. Link text that says nothing.**

```html
<p>To order Golu, <a href="items/dc-1042.html">click here</a>.</p>
```

In a list of links, "click here" could go anywhere. Put the meaning in the link itself: `<a href="items/dc-1042.html">Order Golu</a>`.

**4. An id that doesn't match the link.**

```html
<a href="#catalogue">See the catalogue</a>
<h2 id="Catalogue">Catalogue</h2>
```

Nothing happens when you click, because `catalogue` and `Catalogue` are different ids. Make them match exactly, letter for letter.

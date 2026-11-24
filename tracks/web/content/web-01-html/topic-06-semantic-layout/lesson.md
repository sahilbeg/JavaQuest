## Hook

Walk into the Aundh shop. The name board is over the door. The shelves hold the stock. The counter is at the back, where Raju takes payments. You know where everything is without asking, because every shop is laid out roughly the same way.

Web pages have a usual layout too: a header at the top, a menu of links, the main content, and a footer at the bottom. HTML has an element for each of these parts. Dhaaga's page uses plain boxes instead, so nobody can tell which part is which.

## Concept

So far, every element you've used has a meaning: `h1` is the page's name, `ul` is a list, `table` is data. Now you'll give the *big parts* of the page a meaning too.

```callout jargon
**Semantic** means "about meaning". A **semantic element** says what its content is, like `nav` for a menu. Its opposite, `div`, says nothing at all.
```

### div and span: boxes with no meaning

```html
<div class="top">...</div>
<p>Golu is <span class="new">new</span> this week.</p>
```

`div` is a plain box that groups things on their own lines. `span` is a plain box inside a line of text. Neither one means anything. They're handy as hooks for CSS when no other element fits.

The trouble starts when a whole page is built from `div`s. A sighted visitor sees a header and a menu. A screen reader, a search engine or a browser's reading mode sees only boxes inside boxes.

### Syntax: header and footer

```html
<header>
  <h1>Dhaaga &amp; Co.</h1>
</header>
<footer>
  <p>Dhaaga &amp; Co., Aundh, Pune</p>
</footer>
```

`header` holds the introduction to the page: usually the site's name or logo, and often the menu. `footer` holds the closing details: the address, the opening hours, contact links, the small print.

### Syntax: nav

```html
<nav>
  <ul>
    <li><a href="#story">Our story</a></li>
    <li><a href="#catalogue">Catalogue</a></li>
  </ul>
</nav>
```

`nav` wraps the page's main **navigation**: the menu of links to the main parts of the site or page. The links themselves are a list, so they go in a `ul`.

```callout jargon
**Navigation** means the links people use to move around a site, like a menu at the top.
```

Not every group of links is a `nav`. A link inside a paragraph is just a link. Use `nav` for the main menus.

### Syntax: main

```html
<main>
  <h2>Catalogue</h2>
  ...
</main>
```

`main` holds the page's main content: the thing the visitor came for. It leaves out the header, the menu and the footer, which repeat on every page. A page has **exactly one** `main`.

### Landmarks: shortcuts for screen readers

`header`, `nav`, `main` and `footer` are **landmarks**. Screen readers list them in a menu, so a listener can jump straight to one. Without them, a blind customer must listen to the header and menu on every page before reaching the content.

```callout jargon
A **landmark** is a big part of the page that a screen reader can jump to, like the main content or the navigation.
```

```animate
title: Jumping to the main content
kind: scene
nodes:
  - { id: reader, label: Screen reader, emoji: "🔊", row: 1, col: 0 }
  - { id: header, label: "header", emoji: "🪧", row: 0, col: 2 }
  - { id: nav, label: "nav", emoji: "🧭", row: 0, col: 4 }
  - { id: main, label: "main", emoji: "🧶", row: 1, col: 3 }
  - { id: footer, label: "footer", emoji: "📍", row: 2, col: 3 }
steps:
  - say: Mrs. Desai opens dhaaga.in with her screen reader. On the old page, everything was a div, so it read every word from the top.
    focus: [reader]
  - say: Now the page has landmarks. She opens her screen reader's landmarks menu.
    focus: [header, nav, main, footer]
  - say: It lists banner, navigation, main and content info. Those are the screen reader's names for header, nav, main and footer.
    send: { from: header, to: reader, label: "4 landmarks", back: true }
  - say: She picks main. The screen reader jumps over the header and the menu.
    send: { from: reader, to: main, label: "jump to main" }
  - say: It starts reading the catalogue straight away. Later she jumps to the footer to find the shop's hours.
    send: { from: reader, to: footer, label: "jump to footer" }
```

### Syntax: section and article

```html
<section id="prices">
  <h2>Price list</h2>
  ...
</section>
```

`section` groups one part of the content that has its own heading, like "Our story" or "Price list". Give each `section` a heading, usually an `h2`.

`article` is for a piece that would still make sense on its own, copied to another page: a product card, a customer review, a blog post. If a block would make sense shared alone, it's an `article`. If it's a chapter of this page, it's a `section`.

You may also see `aside`, for content beside the main story, like a "Did you know?" box.

### Which element do I use?

Ask these questions in order:

1. Is it the site's name and menu at the top? `header`.
2. Is it the main menu of links? `nav`.
3. Is it the content the visitor came for? `main`.
4. Is it a part of the content with its own heading? `section`, or `article` if it stands alone.
5. Is it the closing details at the bottom? `footer`.
6. None of these, and you only need a box for styling? `div`.

## Worked example

Here's the outline of Golu's item page, built with semantic elements:

```html
<body>
  <header>
    <p>Dhaaga &amp; Co.</p>
    <nav>
      <ul>
        <li><a href="../index.html">Catalogue</a></li>
        <li><a href="../index.html#prices">Prices</a></li>
      </ul>
    </nav>
  </header>
  <main>
    <h1>Golu the elephant</h1>
    <section id="reviews">
      <h2>Reviews</h2>
      <article>
        <h3>Perfect gift</h3>
        <p>My niece sleeps with Golu every night. Anjali, Bengaluru</p>
      </article>
    </section>
  </main>
  <footer>
    <p>Dhaaga &amp; Co., Aundh, Pune</p>
  </footer>
</body>
```

1. `header`, `main` and `footer` sit straight inside `body`, one of each.
2. The menu is a `nav` inside the `header`, with its links in a `ul`.
3. `main` holds what this page is about: Golu.
4. The reviews are a `section` with an `h2`.
5. Each review would make sense alone, so it's an `article`.

## Try it

Paste this into the editor's body:

```html
<div class="top"><p>Dhaaga &amp; Co.</p></div>
<div class="content"><h2>Catalogue</h2><p>Coasters, keychains, toys.</p></div>
<div class="bottom"><p>Aundh, Pune</p></div>
```

- Change each `div` to `header`, `main` or `footer`. Remember to change both the opening and the closing tag.
- The preview looks exactly the same. Semantic elements change what the page *means*, not how it looks. CSS will handle the look.

## Recap

- `div` and `span` are boxes with no meaning. Use them only when no semantic element fits.
- `header` holds the top of the page, `footer` the bottom, and `nav` the main menu.
- `main` holds the page's main content, and a page has exactly one.
- `section` is a part with its own heading. `article` is a piece that makes sense on its own.
- Landmarks let screen reader users jump straight to the part they want.

## Common mistakes

**1. Building the whole page out of divs.**

```html
<div class="header">...</div>
<div class="main">...</div>
```

A class name like `header` means nothing to a browser or a screen reader. Only the element's name does. Use `<header>` and `<main>`.

**2. Two main elements.**

```html
<main><h2>Catalogue</h2></main>
<main><h2>Price list</h2></main>
```

A page has one `main`. Make one `main`, and put a `section` inside it for each part.

**3. Changing only the opening tag.**

```html
<header>
  <h1>Dhaaga &amp; Co.</h1>
</div>
```

The `</div>` doesn't match anything, so the browser ignores it. The header then runs on and swallows the rest of the page. Change the closing tag too: `</header>`.

**4. A section with no heading.**

```html
<section>
  <p>Coaster set: ₹150.00</p>
</section>
```

A section is a part with a title. Without one, a listener can't tell what the part is about. Add a heading, or use a `div` if it's only a box for styling.

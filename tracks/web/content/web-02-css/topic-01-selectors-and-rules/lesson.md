## Hook

Think of a new school uniform. The students are the same. Their names, classes and roll numbers don't change. Only the way they look changes: the colour of the shirt, the badge, the tie.

The catalogue page from the last project is like a class with no uniform. The HTML says what everything *is*: a heading, a price, a list of products. It never says how anything should *look*. So the browser uses its plain defaults: black text, a white page, one font.

In this project, you dress the page in Dhaaga's colours. The language for that is CSS.

## Concept

### What CSS is

CSS is a list of rules about looks. Each rule picks some elements on the page, then says how they should look.

```callout jargon
**CSS** stands for Cascading Style Sheets. A **stylesheet** is a file of CSS rules, usually ending in `.css`, like `style.css`.
```

HTML and CSS live in separate files. The HTML holds the content. The CSS holds the looks. One stylesheet can dress every page of a website, so a colour change happens in one place.

### Syntax: a CSS rule

```css
selector {
  property: value;
}
```

- The **selector** picks which elements the rule is for.
- The **property** names what to change, like the text colour.
- The **value** says what to change it to.

```css
h1 {
  color: darkblue;
  text-align: center;
}
```

This rule finds every `<h1>`, makes its text dark blue, and puts it in the middle of the line.

```callout jargon
A **declaration** is one `property: value;` pair. A rule holds one or more declarations between curly braces `{ }`. Each declaration ends with a semicolon.
```

### Syntax: linking a stylesheet

```html
<link rel="stylesheet" href="style.css">
```

This line goes inside the page's `<head>`. `rel="stylesheet"` tells the browser what kind of file it is. `href` is the file's path, like a link's address.

Without this line, the browser never even opens `style.css`. Your rules sit in the file and do nothing. Watch what happens when the line is there:

```animate
title: How a stylesheet reaches the page
kind: scene
nodes:
  - { id: html, label: index.html, emoji: "📄" }
  - { id: browser, label: Browser, emoji: "🌐", value: reading }
  - { id: css, label: style.css, emoji: "🎨" }
  - { id: page, label: The page, emoji: "📱", value: plain }
steps:
  - say: The browser opens index.html and reads it from the top.
    focus: [html, browser]
  - say: Inside the head it finds a link with rel="stylesheet". That means another file holds the looks.
    focus: [html]
  - say: The browser asks for the file named in href, style.css.
    send: { from: browser, to: css, label: "GET style.css" }
    set: { browser: waiting }
  - say: The stylesheet arrives, full of rules.
    send: { from: css, to: browser, label: "rules", back: true }
    set: { browser: matching }
  - say: For every rule, the browser finds the elements its selector picks, then applies the declarations.
    focus: [browser]
  - say: Now the page is painted with the new looks. Delete the link line, and the page goes back to plain.
    send: { from: browser, to: page, label: "paint" }
    set: { page: styled, browser: done }
```

### Syntax: type selectors

```css
p {
  color: dimgray;
}
```

A **type selector** is just a tag name, with no symbols. `p` picks every `<p>` on the page. `h1` picks every `<h1>`.

It's a wide net. Use it for things that should look the same everywhere, like all headings.

### Syntax: class selectors

```css
.price {
  font-weight: bold;
}
```

A **class selector** starts with a dot. `.price` picks every element with `class="price"` in its HTML, whatever its tag.

```html
<p class="price">₹450.00</p>
```

An element can have several classes, separated by spaces: `class="product sold-out"`. Then both `.product` and `.sold-out` pick it. Classes are the selector you'll use most.

### Syntax: id selectors

```css
#visit {
  font-style: italic;
}
```

An **id selector** starts with a hash, `#`. `#visit` picks the one element with `id="visit"`. An id must be unique: only one element on a page may have it.

```callout tip
Prefer classes for styling. An id works for one element only, and its rules are hard to override later. The ticket on specificity shows why.
```

### Syntax: grouping selectors

```css
h2, h3 {
  color: darkblue;
}
```

A comma between selectors means "and also". This one rule styles every `<h2>` and every `<h3>`. It saves writing the same declarations twice.

### Syntax: descendant selectors

```css
nav a {
  color: darkred;
}
```

A space between two selectors means "inside". `nav a` picks every `<a>` that sits somewhere inside a `<nav>`. Links in the footer are not picked.

```callout jargon
A **descendant** is an element inside another one, at any depth: a child, a grandchild, and so on.
```

Watch the browser decide which links `nav a` picks:

```animate
title: Which links does nav a pick?
kind: scene
nodes:
  - { id: nav, label: "<nav>", emoji: "🧭", row: 0, col: 0 }
  - { id: a1, label: "<a> Catalogue", emoji: "🔗", row: 1, col: 0, value: "?" }
  - { id: a2, label: "<a> Visit us", emoji: "🔗", row: 1, col: 1, value: "?" }
  - { id: footer, label: "<footer>", emoji: "🦶", row: 0, col: 3 }
  - { id: a3, label: "<a> WhatsApp", emoji: "🔗", row: 1, col: 3, value: "?" }
links:
  - { from: nav, to: a1 }
  - { from: nav, to: a2 }
  - { from: footer, to: a3 }
steps:
  - say: The rule is nav a. The browser reads a selector from the right, so it starts with every a on the page.
    focus: [a1, a2, a3]
  - say: The Catalogue link sits inside the nav. It's picked.
    focus: [a1, nav]
    set: { a1: picked }
  - say: The Visit us link is inside the nav too. Picked.
    focus: [a2, nav]
    set: { a2: picked }
  - say: The WhatsApp link lives in the footer. No nav above it, so the rule skips it.
    focus: [a3, footer]
    set: { a3: skipped }
```

### Properties for this ticket

| Property | What it changes | Example value |
|---|---|---|
| `color` | the text colour | `darkblue` |
| `text-align` | where text sits on its line | `center` |
| `font-weight` | how thick the letters are | `bold` |
| `font-style` | slanted or straight letters | `italic` |

Colours can be names like `darkblue` or `darkred`. Browsers know about 140 colour names. The next ticket swaps them for Dhaaga's exact brand colours.

## Worked example

Here is a small stylesheet for the catalogue, one rule per kind of selector:

```css
h1 {
  text-align: center;
}

.price {
  font-weight: bold;
}

#visit {
  font-style: italic;
}

h2, h3 {
  color: darkblue;
}

nav a {
  color: darkred;
}
```

1. `h1` is a type selector. The shop name in the header moves to the middle.
2. `.price` is a class selector. Every element with `class="price"` gets thick letters, so all five prices stand out.
3. `#visit` is an id selector. Only the footer with `id="visit"` turns italic.
4. `h2, h3` is a group. "Our catalogue" and every product name turn dark blue.
5. `nav a` is a descendant selector. Only the two links in the nav turn dark red.

None of these rules changes the HTML. The same page, with the same words, now looks different.

## Try it

Open this ticket's editor and paste this rule at the end of `style.css`. Watch the preview.

```css
.code {
  color: gray;
  font-style: italic;
}
```

Every item code, like `DC-1042`, turns grey and slanted. Now remove the dot, so the selector is `code`. Nothing is grey any more. Why? Without the dot, the browser looks for `<code>` tags, and the page has none.

## Recap

- A CSS rule is a selector plus declarations: `selector { property: value; }`.
- A `<link rel="stylesheet" href="style.css">` in the `<head>` makes the page load your CSS.
- `h1` picks by tag, `.price` picks by class, and `#visit` picks the one element with that id.
- A comma groups selectors (`h2, h3`), and a space means "inside" (`nav a`).
- Prefer classes for styling: they're reusable and easy to override.

## Common mistakes

**1. Forgetting the dot on a class selector.**

```css
price {
  font-weight: bold;
}
```

Nothing happens, and there's no error. Without the dot, the browser looks for a `<price>` tag, and there isn't one. Write `.price` to pick `class="price"`.

**2. A missing semicolon between declarations.**

```css
h1 {
  color: darkblue
  text-align: center;
}
```

Neither line works. With no `;`, the browser reads `darkblue text-align: center` as one broken colour, and throws it away. End every declaration with a semicolon.

**3. The link points at the wrong file.**

```html
<link rel="stylesheet" href="styles.css">
```

The page stays plain. The file is `style.css`, without the s, so the browser gets a "not found" answer. The Network tab in DevTools shows a red 404 for it. Check the spelling of `href`, and keep `rel="stylesheet"`.

**4. Using # for a class.**

```css
#price {
  font-weight: bold;
}
```

`#` picks by id, but the prices have `class="price"`, not `id="price"`. Nothing changes. Use `.` for classes and `#` for ids.

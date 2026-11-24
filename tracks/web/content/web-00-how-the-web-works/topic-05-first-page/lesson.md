## Hook

The first thing made for the stall wasn't a coaster. It was a sign: a piece of card with "Dhaaga & Co." written big, and one line under it, "Handmade crochet from Pune."

It wasn't fancy, but it did its job. People walking past knew whose stall it was and what it sold.

Your first web page is that sign. A name, a heading and two short lines. Small, but real: it opens in any browser on any phone in the world.

## Concept

Every HTML page has the same shape. Learn it once and you'll write it hundreds of times.

### The skeleton of every page

Here's a whole, working page:

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Dhaaga & Co.</title>
</head>
<body>
  <h1>Dhaaga & Co.</h1>
  <p>Handmade crochet from Pune.</p>
</body>
</html>
```

That looks like a lot for two lines of text. Let's take it apart, a few lines at a time.

### Syntax: the doctype

```html
<!doctype html>
```

The very first line says "this is a modern HTML page". It isn't an element, so it has no closing tag. Without it, browsers switch to an old "quirks" mode that copies the bugs of 1990s browsers.

### Syntax: html, head and body

```html
<html lang="en">
<head>
  <!-- facts about the page -->
</head>
<body>
  <!-- what people see -->
</body>
</html>
```

- `<html>` wraps the whole page. Its `lang="en"` attribute says the page is in English.
- `<head>` holds facts about the page. Nothing in it shows on the page itself.
- `<body>` holds everything people see: headings, text, photos and links.

```callout analogy
A parcel has a label on the outside and the gift inside. The head is the label: who it's for and what's in it. The body is the gift.
```

### The two meta lines

The skeleton's `<head>` starts with two `<meta>` lines. A **meta** element gives the browser a fact about the page. It has no closing tag.

- `<meta charset="utf-8">` says how the letters in the file are stored.
- `<meta name="viewport" ...>` tells a phone to use its real screen width, instead of shrinking a laptop-sized page.

Copy both lines exactly, at the top of every page's `<head>`. You'll meet them again soon.

### Syntax: the title

```html
<title>Dhaaga & Co.</title>
```

The `<title>` goes inside `<head>`. It's the name in the browser tab, in bookmarks, and in search results. It doesn't show on the page itself.

### Syntax: headings

```html
<h1>Dhaaga & Co.</h1>
<h2>Coasters</h2>
```

`<h1>` is the main heading, the biggest. Use exactly one per page, for what the whole page is about. `<h2>` to `<h6>` are smaller headings for sections, in order of importance.

### Syntax: paragraphs

```html
<p>Handmade crochet from Pune.</p>
```

`<p>` holds one paragraph of text. The browser puts a little space between paragraphs, like the gap between paragraphs in a book.

```callout warning
Pressing Enter in your file doesn't make a new line on the page. The browser squeezes all spaces and line breaks down to one space. To start a new paragraph, start a new `<p>`.
```

### Syntax: comments

```html
<!-- TODO 1: add a <title> -->
```

A **comment** is a note for people. It starts with `<!--` and ends with `-->`, and the browser ignores everything between. Your exercise file uses comments to mark each TODO. You can leave them in or delete them.

```callout jargon
A **TODO** is a note that says "this part still needs doing". Programmers write them so nothing gets forgotten.
```

### Indenting: a habit for humans

Notice how the lines inside `<head>` and `<body>` start two spaces further in. That's called **indenting**. The browser doesn't care at all. People do: it shows at a glance which element sits inside which.

### How the browser reads your page

```animate
title: The browser reads index.html
kind: scene
nodes:
  - { id: file, label: index.html, emoji: "📄", value: "top line", col: 0 }
  - { id: tab, label: Browser tab, emoji: "🗂️", value: "untitled", row: 0, col: 2 }
  - { id: page, label: The page, emoji: "🖼️", value: "blank", row: 1, col: 2 }
steps:
  - say: The browser reads the file from the top. <!doctype html> says modern HTML, so it uses today's rules.
    focus: [file]
    set: { file: "doctype" }
  - say: Inside <head>, the two meta lines set up the letters and the phone width. Nothing shows yet.
    set: { file: "head: meta" }
  - say: The <title> goes to the browser tab, not the page.
    send: { from: file, to: tab, label: "Dhaaga & Co." }
    set: { tab: "Dhaaga & Co.", file: "head: title" }
  - say: Now the <body>. The <h1> is drawn as a big, bold heading at the top of the page.
    send: { from: file, to: page, label: "<h1>" }
    set: { page: "big heading", file: "body: h1" }
  - say: The <p> comes next, drawn as normal text below the heading, with a little space above it.
    send: { from: file, to: page, label: "<p>" }
    set: { page: "heading + paragraph", file: "body: p" }
  - say: The browser reaches </html>. The page is finished, and it all came from one plain text file.
    set: { file: "done" }
    focus: [tab, page]
```

## Worked example

Here's a page for one item, the coaster set:

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Coaster set | Dhaaga & Co.</title>
</head>
<body>
  <h1>Coaster set (4)</h1>
  <p>Four round crochet coasters in Marigold and Indigo.</p>
  <p>Each set is ₹150.</p>
</body>
</html>
```

1. Line 1 is the doctype, so the browser uses modern rules.
2. Line 2 opens the page and says it's in English.
3. Lines 3 to 7 are the head: the two meta lines, then the title for the tab.
4. Line 8 opens the body. Everything from here to line 12 shows on the page.
5. Line 9 is the one main heading.
6. Lines 10 and 11 are two paragraphs.
7. Lines 12 and 13 close the body and then the whole page, in the reverse order they opened.

```callout tip
In VS Code, open an empty `.html` file, type `!` and press Tab. VS Code writes the whole skeleton for you. Then change its title and fill in the body.
```

## Try it

Your ticket's `index.html` is open in the editor, with the preview next to it.

1. Find the TODO comments. Each one says what goes there.
2. Type the `<title>` inside `<head>`. Watch the preview's tab name change.
3. Add the `<h1>`, then the two paragraphs, inside `<body>`.
4. Try deleting the `</h1>` closing tag for a moment. See how the paragraphs turn big and bold? Put it back.

Then run the checks.

## Recap

- Every page starts with `<!doctype html>`, then `<html lang="en">` wrapping a `<head>` and a `<body>`.
- The **head** holds facts about the page: the two `<meta>` lines and the `<title>` for the tab.
- The **body** holds what people see. Use one `<h1>` main heading and a `<p>` for each paragraph.
- Close every element you open, in reverse order.
- A **comment**, `<!-- like this -->`, is a note the browser ignores.

## Common mistakes

**1. A title that never closes.**

```html
<title>Dhaaga & Co.
</head>
<body>
  <h1>Dhaaga & Co.</h1>
```

The page turns completely blank, and the tab shows a long mess of tags. Inside a title, the browser treats everything as plain text until it sees `</title>`. Close it: `<title>Dhaaga & Co.</title>`.

**2. A typo in a closing tag.**

```html
<h1>Dhaaga & Co.</hl>
<p>Handmade crochet from Pune.</p>
```

That's a lowercase L, not the number 1. The browser ignores `</hl>`, so the heading never ends, and the paragraph shows up huge. Type `</h1>` with the number one.

**3. More than one main heading.**

```html
<h1>Dhaaga & Co.</h1>
<h1>Handmade crochet from Pune.</h1>
```

It looks fine, but the page now has two "most important" headings. Screen readers and search engines get confused about what the page is about. Keep one `<h1>` and use `<p>` for the line under it.

**4. Expecting Enter to make a new line.**

```html
<p>Handmade crochet.
Find us on FC Road.</p>
```

The page shows "Handmade crochet. Find us on FC Road." on one line. The browser turns line breaks into spaces. Use two `<p>` elements for two paragraphs.

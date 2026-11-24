## Hook

Think of a sari shop's parcels. Every parcel gets wrapped in paper, and a label on top says what's inside: "silk, blue, for Mrs. Joshi". The wrapping keeps things apart. The label tells people what to do with it.

HTML works the same way. You wrap each piece of text in tags, and the tags tell the browser what that piece is. Sana's welcome text has no wrapping at all, so the browser runs it all together. Your job is to wrap it properly.

## Concept

**HTML** stands for HyperText Markup Language. "Markup" means extra marks around your text that say what each part is. You don't write HTML to make things pretty. You write it to say what things *are*: a heading, a paragraph, a photo, a link.

```callout jargon
**Markup** is extra text around your content that describes it. In HTML, the markup is the tags in angle brackets, like `<p>`.
```

### Syntax: an element

```html
<p>Handmade crochet from Pune.</p>
```

This whole line is one **element**. It has three parts:

- the **opening tag** `<p>`, which says "a paragraph starts here",
- the **content**, the text the visitor sees,
- the **closing tag** `</p>`, which has a slash and says "the paragraph ends here".

The `p` inside the brackets is the element's name. Browsers don't care about capital letters in names, but everyone writes them in lowercase.

```callout jargon
A **tag** is the part in angle brackets, like `<p>` or `</p>`. An **element** is the whole thing: opening tag, content and closing tag together.
```

### Text with no element

Text that sits loose in the page, outside any element, still shows up. But the browser doesn't know what it is.

```html
<p>Welcome to Dhaaga.</p>
Handmade crochet from Pune.
Every piece is made by hand.
```

The last two lines have no paragraph around them. The browser treats a new line in your file as a plain space, so they run together into one line. Nothing marks them as a paragraph, so later your CSS can't style them as one. Wrap every piece of text in an element that says what it is.

### Syntax: attributes

```html
<p class="tagline">Handmade crochet from Pune.</p>
```

An **attribute** adds extra information to an element. It goes inside the opening tag, after the name and a space. It has a name, an `=` sign, and a value in double quotes.

```callout jargon
An **attribute** is a `name="value"` pair inside an opening tag. It tells the browser something extra about the element, like its class or its language.
```

Two attributes you'll use all the time:

- `class` gives an element a label. CSS and JavaScript use it later to find "every tagline". Many elements can share a class.
- `id` gives an element a name that is unique on the page. Only one element may have `id="prices"`.

An element can have several attributes, separated by spaces: `<p class="tagline" id="top">`.

### Syntax: the lang attribute

```html
<html lang="en">
```

`lang` says which human language the page is written in. `en` means English. Screen readers use it to pick the right voice and accent. Without it, a phone set to Marathi may read English words with Marathi sounds.

```callout jargon
A **screen reader** is an app that reads the page aloud. People who are blind or can't see well use one on their phone or computer. VoiceOver on iPhones and TalkBack on Android are screen readers.
```

### Syntax: nesting

```html
<p>Every piece is made by <strong>Meera Aunty</strong>.</p>
```

Elements can sit inside other elements. This is called **nesting**. Here `strong` sits inside `p`. `strong` marks words as important, and browsers show them in bold.

The rule is simple: **the element you opened last must be closed first.** Like boxes inside boxes, you shut the inner box before the outer one.

```html
<!-- Right: strong opens inside p, and closes inside p -->
<p>Made by <strong>Meera Aunty</strong>.</p>

<!-- Wrong: p closes while strong is still open -->
<p>Made by <strong>Meera Aunty</p></strong>
```

### The page is a tree

When the browser reads your HTML, it builds a family tree of elements. An element inside another is its **child**. The outer one is its **parent**. Watch the browser build the tree for a tiny page:

```animate
title: The browser builds a tree
kind: scene
nodes:
  - { id: html, label: "<html lang=en>", emoji: "🌳", row: 0, col: 2 }
  - { id: head, label: "<head>", emoji: "🧠", row: 1, col: 1 }
  - { id: body, label: "<body>", emoji: "📄", row: 1, col: 3 }
  - { id: title, label: "<title>", emoji: "🏷️", row: 2, col: 1 }
  - { id: p, label: "<p class=note>", emoji: "¶", row: 2, col: 3 }
  - { id: strong, label: "<strong>", emoji: "💪", row: 3, col: 3 }
links:
  - { from: html, to: head }
  - { from: html, to: body }
  - { from: head, to: title }
  - { from: body, to: p }
  - { from: p, to: strong }
steps:
  - say: The browser reads the file from the top. The html element wraps everything, so it becomes the root of the tree.
    focus: [html]
  - say: Inside html come two children. head holds facts about the page. body holds what the visitor sees.
    show: [head, body]
  - say: The title sits inside head. It's the text on the browser tab.
    show: [title]
  - say: The paragraph sits inside body. Its class attribute travels with it, like a label on a parcel.
    show: [p]
  - say: strong opened inside p, so it becomes a child of p. It must close before p closes.
    show: [strong]
  - say: That's the whole page as a tree. CSS and JavaScript will both use this tree later.
    focus: [html, body, p, strong]
```

### Syntax: void elements

```html
<hr>
```

A few elements never have content, so they have no closing tag. They're called **void elements**. `<hr>` draws a line across the page, to separate two parts. You'll meet more soon: `<br>` for a line break, `<img>` for a photo, `<meta>` and `<input>`.

```callout jargon
A **void element** is an element that can't hold content, so it has only an opening tag. `<hr>`, `<br>`, `<img>`, `<meta>` and `<input>` are void elements.
```

Writing `</hr>` is a mistake. Some people write `<hr />` with a slash. That works too, but plain `<hr>` is the normal HTML way.

```callout tip
You'll see `&amp;` in Dhaaga's pages, as in `Dhaaga &amp; Co.`. It's how HTML writes the `&` sign safely. The next ticket explains why.
```

## Worked example

Here is a small, correct page for Dhaaga's stall:

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Dhaaga &amp; Co.</title>
</head>
<body>
  <p class="tagline">Handmade crochet from Pune.</p>
  <hr>
  <p>Open on <strong>Saturday and Sunday</strong>, 10:00 to 20:00.</p>
</body>
</html>
```

1. `<!doctype html>` tells the browser this is a modern HTML page.
2. `<html lang="en">` opens the root element, with an attribute saying the page is English.
3. `head` holds the charset and the tab title. Nothing in it shows on the page itself.
4. The first `p` has a `class` attribute. The text shows as its own paragraph.
5. `<hr>` draws a line. It has no closing tag, because it's a void element.
6. In the last line, `strong` opens and closes inside `p`. The inner box shuts first.

## Try it

Open this ticket's editor. The preview redraws the page as you type. Paste this into the body, then try each change below:

```html
<p>Welcome to Dhaaga.</p>
Handmade crochet from Pune.
Open on <strong>Saturday and Sunday</strong>.
```

- Wrap each of the last two lines in `<p>` and `</p>`. Watch them split onto lines of their own.
- Add `class="tagline"` to the first new paragraph. Nothing changes on screen yet: classes are for CSS, which comes in the next project.
- Add `<hr>` between the first two paragraphs.
- Move `</strong>` to just after `</p>`, and see whether the preview changes. It may not: browsers repair small mistakes quietly. That's exactly why they're easy to miss.

## Recap

- An **element** is an opening tag, its content and a closing tag, like `<p>Hello</p>`.
- **Attributes** like `class`, `id` and `lang` go inside the opening tag as `name="value"`.
- Elements **nest** inside each other, and the one opened last closes first.
- **Void elements** like `<hr>` have no content and no closing tag.
- The browser turns your HTML into a tree of parents and children.

## Common mistakes

**1. Closing tags in the wrong order.**

```html
<p>Made by <strong>Meera Aunty</p></strong>
```

The page may look fine, because browsers quietly repair broken nesting. But the repair is a guess, and on bigger pages the guess goes wrong. Bold text can leak into the next paragraph. Close the inner element first: `<strong>Meera Aunty</strong></p>`.

**2. Forgetting the closing tag.**

```html
<p>Handmade crochet from Pune.
<p>Every piece is made by hand.
```

For `p`, the browser guesses where each one ends. For most elements, like `strong`, it doesn't: everything after it turns bold. Always write the closing tag, `</p>`.

**3. Putting an attribute in the closing tag.**

```html
<p>Handmade crochet</p class="tagline">
```

The browser ignores attributes on a closing tag, so the paragraph gets no class. Attributes always go in the opening tag: `<p class="tagline">Handmade crochet</p>`.

**4. Writing a closing tag for a void element.**

```html
<hr></hr>
```

`<hr>` can't hold anything, so `</hr>` means nothing. Browsers skip it, but it confuses the next person who reads your code. Write just `<hr>`.

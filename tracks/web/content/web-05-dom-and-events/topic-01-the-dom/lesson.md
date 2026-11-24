## Hook

Think of a family tree. Grandma sits at the top. Her children sit below her, and their children below them. Ask anyone in the tree "who is your parent?" and they can answer.

A web page works the same way inside the browser. Your HTML file is only text. The browser turns it into a tree of objects, and JavaScript talks to that tree. So far your JavaScript has lived in the console. From this ticket on, it reaches into the page itself.

## Concept

### From HTML text to a tree

When a phone downloads `index.html`, it gets plain text. The browser reads that text and builds objects from it, one for every tag.

```callout jargon
**Parsing** means reading text and turning it into a structure a program can work with. The browser parses HTML into objects.
```

```callout jargon
The **DOM** (Document Object Model) is the tree of objects the browser builds from your HTML. JavaScript reads and changes the page through the DOM, never through the HTML text.
```

```animate
title: The browser turns HTML into a tree
kind: scene
nodes:
  - { id: html, label: "index.html", emoji: "📄", row: 0, col: 0 }
  - { id: browser, label: Browser, emoji: "🌐", row: 0, col: 2 }
  - { id: doc, label: document, emoji: "🌳", row: 1, col: 2 }
  - { id: ul, label: "ul#products", emoji: "📋", row: 2, col: 2 }
  - { id: li1, label: "li Golu", emoji: "🐘", row: 3, col: 1 }
  - { id: li2, label: "li Coasters", emoji: "🟡", row: 3, col: 3 }
  - { id: js, label: Your JavaScript, emoji: "⚙️", row: 1, col: 4 }
links:
  - { from: doc, to: ul }
  - { from: ul, to: li1 }
  - { from: ul, to: li2 }
steps:
  - say: The phone downloads index.html. It is only text, angle brackets and words.
    focus: [html]
    show: [html, browser]
  - say: The browser reads the text from top to bottom. This reading is called parsing.
    send: { from: html, to: browser, label: "HTML text" }
    set: { browser: parsing }
  - say: It builds an object called document. Every other object hangs below it.
    show: [doc]
  - say: The ul tag becomes an element object, a child of the document. The html and body elements sit between them, left out here.
    show: [ul]
  - say: Each li becomes a child of the ul. The ul is their parent.
    show: [li1, li2]
    set: { browser: done }
  - say: Your JavaScript never reads the HTML text. It asks the tree, for example for the list's children.
    show: [js]
    send: { from: js, to: ul, label: "list.children" }
  - say: The tree answers with live objects. Change one, and the screen changes too.
    send: { from: ul, to: js, label: "2 elements", back: true }
```

### The document object

The browser gives your script one ready-made variable: `document`. It is the root of the tree, and every search starts from it.

```js
console.log(document.title);         // the text in <title>
console.log(document.body);          // the <body> element
console.log(document.documentElement); // the <html> element
```

```callout jargon
An **element** is the object for one tag, such as `<ul>` or `<li>`. It has properties you can read, like its children and its text.
```

To pick out one element, you will use `document.querySelector("#products")`. The next ticket covers it properly. For now, read it as "find the element with id `products`".

### Syntax: walking the tree

```js
const list = document.querySelector("#products");
list.children;           // its child elements
list.firstElementChild;  // the first child element, or null
list.lastElementChild;   // the last child element, or null
list.parentElement;      // the element it sits inside
```

- Every element knows its family. You can walk down to children and up to the parent.
- `children.length` tells you how many child elements there are.
- `firstElementChild` is `null` when there are no children, so check before you use it.

```callout analogy
The DOM is like a family tree on a wall. You can start at any person, then walk to a parent or to a child. You never need to read the whole wall again.
```

### Elements and text nodes

Look at this HTML closely:

```html
<ul id="products">
  <li>Golu the elephant</li>
  <li>Marigold coaster set</li>
</ul>
```

There are line breaks and spaces between the tags. The browser keeps them, as **text nodes**.

```callout jargon
A **node** is any object in the DOM tree. Elements are one kind of node. **Text nodes** are another: they hold text, including the spaces and line breaks between tags.
```

So this list has two kinds of children:

| Property | What it holds | Count for the list above |
|---|---|---|
| `list.childNodes` | every node: elements and text | 5 |
| `list.children` | only elements | 2 |

The 5 are: space, `li`, space, `li`, space. When you want the products, use `children`.

### Syntax: textContent

```js
const first = list.firstElementChild;
const name = first.textContent.trim();
```

- `textContent` is all the text inside an element, with the tags taken out.
- It keeps any spaces and line breaks from the HTML, so `.trim()` them off.
- For the `<ul>` itself, `textContent` joins the text of every `<li>` inside it.

### children is not an array

`list.children` looks like an array. It has a `length` and numbered items. But it is an **HTMLCollection**, and it has no `map` or `filter`.

```callout jargon
An **HTMLCollection** is a list-like object the DOM gives you. You can loop over it with `for...of` and read `length`, but array methods are missing.
```

Spread it into a real array first, as you did with other values in the last module:

```js
const names = [...list.children].map((item) => item.textContent.trim());
```

It is also **live**: add a new `<li>`, and `list.children.length` grows on its own.

### When your script runs

The browser parses HTML from top to bottom. A plain `<script>` runs the moment the browser reaches it. If it sits in `<head>`, the `<body>` has not been parsed yet.

```html
<head>
  <script src="old.js"></script>          <!-- runs now: the list does not exist yet -->
  <script type="module" src="main.js"></script> <!-- waits until the page is parsed -->
</head>
```

```callout jargon
**Deferred** means "run later". A script with `type="module"` (or the `defer` attribute) is deferred: it runs after the whole page is parsed, so every element already exists.
```

Every page in this course loads `main.js` with `type="module"`. That is why `main.js` can find the list straight away.

### Seeing the DOM in DevTools

Open DevTools and pick the **Elements** panel. What you see there is the DOM, not your HTML file. When JavaScript changes the page, this panel changes too, but the file never does.

```callout tip
Click an element in the Elements panel, then type `$0` in the Console. `$0` is the element you picked. Try `$0.children` or `$0.textContent`.
```

## Worked example

Meera Aunty's stall page lists its opening days. This code reads that list.

```html
<section id="hours">
  <h2>Stall hours</h2>
  <ul id="days">
    <li>Saturday</li>
    <li>Sunday</li>
  </ul>
</section>
```

```js
const days = document.querySelector("#days");

console.log(days.childNodes.length);   // 5
console.log(days.children.length);     // 2

const names = [...days.children].map((day) => day.textContent.trim());
console.log(names);                    // ["Saturday", "Sunday"]

console.log(days.firstElementChild.textContent); // "Saturday"
console.log(days.parentElement.id);    // "hours"
```

1. `querySelector("#days")` finds the `<ul>`.
2. `childNodes` counts 5: two `<li>` elements and three runs of spaces.
3. `children` counts only the 2 elements.
4. Spreading `children` gives a real array, so `map` works. `trim()` removes stray spaces.
5. `parentElement` walks up one step, to the `<section>`. Its `id` is `hours`.

## Try it

Open the preview of your ticket, then open its DevTools Console. Paste these lines one at a time.

```js
const list = document.querySelector("#products");
list.childNodes.length;
list.children.length;
list.lastElementChild.textContent;
list.parentElement.id;
document.title;
```

Now change one `<li>` in `index.html`, or add a new one. Run the lines again and watch the numbers follow the page.

## Recap

- The browser parses HTML into the **DOM**, a tree of objects. JavaScript works with the tree, not the text.
- `document` is the root. Every element knows its `children`, `parentElement`, `firstElementChild` and `lastElementChild`.
- `childNodes` includes text nodes, the spaces between tags. `children` holds only elements.
- `textContent` reads an element's text. Trim it. Spread `children` into an array before using `map`.
- A `type="module"` script runs after the page is parsed, so the elements exist when it starts.

## Common mistakes

**1. Counting with childNodes.**

```js
const count = list.childNodes.length;   // 7 for a list of 3
```

The page says 7 items when there are 3, because the spaces between tags count as text nodes. Use `list.children.length`.

**2. Calling map on children.**

```js
const names = list.children.map((item) => item.textContent);
```

The console shows `TypeError: list.children.map is not a function`. An HTMLCollection is not an array. Spread it first: `[...list.children].map(...)`.

**3. Running the script before the element exists.**

```html
<head>
  <script src="main.js"></script>
</head>
```

`main.js` runs before the body is parsed, so `document.querySelector("#products")` returns `null`. The next line shows `TypeError: Cannot read properties of null (reading 'children')`. Load the script with `type="module"` (or `defer`).

**4. Forgetting to trim.**

```js
const name = item.textContent;   // "\n    Golu the elephant\n  "
```

Here the `<li>` was written over three lines in the HTML. A comparison like `name === "Golu the elephant"` is `false`, because the line breaks from the HTML are still there. Use `item.textContent.trim()`.

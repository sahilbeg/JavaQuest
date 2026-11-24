## Hook

Think about how the FC Road stall was set up on its first Sunday.

First came the frame: a folding table, a board for prices, and a basket for the coasters. Next came the look: an Indigo cloth on the table and Marigold bunting along the top. Last came the action: someone behind the table, answering questions and taking payments.

A web page is built the same way, in three layers. Each layer has its own language, and each language has one job.

## Concept

Every page you see is made from three languages:

- **HTML** says what is on the page.
- **CSS** says how it looks.
- **JavaScript** says what it does.

### HTML: what is on the page

**HTML** marks up text so the browser knows what each piece is. This is a heading. That is a paragraph. This is a link.

```html
<h1>Dhaaga & Co.</h1>
<p>Handmade crochet from Pune.</p>
```

The browser shows the first line as a big heading and the second as normal text.

```callout jargon
**HTML** stands for HyperText Markup Language. To **mark up** text means to label its parts, like a teacher writing "title" next to a heading.
```

### Syntax: an HTML element

```html
<p>Handmade crochet from Pune.</p>
```

- `<p>` is the **opening tag**. It says "a paragraph starts here".
- `Handmade crochet from Pune.` is the **content**, the text people see.
- `</p>` is the **closing tag**. The slash means "the paragraph ends here".

The whole thing, from `<p>` to `</p>`, is one **element**.

```callout jargon
A **tag** is a name inside angle brackets, like `<p>` or `</p>`. An **element** is an opening tag, its content and its closing tag together.
```

### Attributes: extra details on a tag

Some elements need more information. A link needs to know where it goes. That detail goes inside the opening tag, as an **attribute**:

```html
<a href="https://dhaaga.in/prices">See our prices</a>
```

- `a` makes a link.
- `href="https://dhaaga.in/prices"` is an attribute: a name, an equals sign, and a value in quotes.
- `See our prices` is the text people tap.

```callout jargon
An **attribute** is a name and a value inside an opening tag. It adds detail to the element, like where a link goes.
```

### Elements inside elements

Elements can sit inside other elements. This is called **nesting**:

```html
<p>Open <strong>Saturday and Sunday</strong>, 10:00 to 20:00.</p>
```

Here `<strong>` sits inside `<p>`. It makes "Saturday and Sunday" stand out. The inner element must close before the outer one does, like boxes packed inside boxes.

### CSS: how it looks

**CSS** decides colours, sizes, spacing and layout. Without CSS, every page looks like a plain black-and-white document.

```css
h1 {
  color: #2E3A8C;
}
```

This makes every `<h1>` heading Indigo, Dhaaga's brand colour. `#2E3A8C` is a colour written as a code, which a later ticket explains.

```callout jargon
**CSS** stands for Cascading Style Sheets. A **stylesheet** is a file full of CSS rules.
```

### Syntax: a CSS rule

```css
p {
  color: #2E3A8C;
  font-size: 18px;
}
```

- `p` is the **selector**. It picks which elements the rule styles: here, every paragraph.
- Inside the curly braces are **declarations**. Each has a **property**, a colon, a **value** and a semicolon.
- `color` sets the text colour, and `font-size` sets how big the letters are.

### JavaScript: what it does

**JavaScript** makes a page react. It can open a menu when you tap it, add up a cart, or check a form before it is sent.

```js
console.log("Namaste from Dhaaga!");
```

This line prints a message. Ticket WEB-0006 shows you exactly where it appears.

```callout jargon
**JavaScript** is a programming language that runs in every browser. Despite the name, it has nothing to do with Java. The similar names are an old marketing decision.
```

### Three files, side by side

Each language usually lives in its own file:

| File | Language | Job |
|---|---|---|
| `index.html` | HTML | What is on the page |
| `style.css` | CSS | How it looks |
| `main.js` | JavaScript | What it does |

The HTML file is the boss. It tells the browser to fetch the other two:

```html
<link rel="stylesheet" href="style.css">
<script type="module" src="main.js"></script>
```

`<link>` fetches the stylesheet. `<script>` fetches the JavaScript and runs it. Each one becomes its own request to the server.

### Watch the layers arrive

```animate
title: Three files build one page
kind: scene
nodes:
  - { id: html, label: index.html, emoji: "📄", row: 0, col: 0 }
  - { id: css, label: style.css, emoji: "🎨", row: 1, col: 0 }
  - { id: js, label: main.js, emoji: "⚙️", row: 2, col: 0 }
  - { id: page, label: The page, emoji: "📱", value: "empty", row: 1, col: 2 }
steps:
  - say: The browser asks for index.html first. It doesn't know about the other files yet.
    focus: [html]
  - say: The HTML arrives. The page now has a heading, the hours and a WhatsApp link, but in plain black text.
    send: { from: html, to: page, label: "heading, text, link" }
    set: { page: "plain text" }
  - say: The HTML says to fetch style.css. The browser asks for it with a second request.
    show: [css]
    focus: [css]
  - say: The CSS rules arrive. The heading turns Indigo and the button turns Marigold. Same content, new look.
    send: { from: css, to: page, label: "colours, sizes" }
    set: { page: "styled" }
  - say: The HTML also says to fetch main.js. The browser asks for it too.
    show: [js]
    focus: [js]
  - say: The JavaScript runs. Now the button reacts when someone taps it. The page is complete.
    send: { from: js, to: page, label: "tap = open WhatsApp" }
    set: { page: "styled and working" }
```

## Worked example

Here is the visiting card from the ticket, one layer at a time.

**The HTML** puts the content on the page:

```html
<h1>Dhaaga & Co.</h1>
<p>Open Saturday and Sunday, 10:00 to 20:00.</p>
<button id="hours">When are you open?</button>
```

1. `<h1>` is the main heading, with the business name.
2. `<p>` is a paragraph with the hours.
3. `<button>` makes a button. Its `id` attribute gives it a name, so JavaScript can find it.

**The CSS** gives it Dhaaga's colours:

```css
h1 {
  color: #2E3A8C;
}

button {
  background: #F2A81D;
}
```

The heading becomes Indigo, and the button becomes Marigold.

**The JavaScript** makes the button do something:

```js
const button = document.querySelector("#hours");
button.addEventListener("click", () => {
  alert("We are open Saturday and Sunday, 10:00 to 20:00.");
});
```

It finds the button by its id. When someone taps it, a small box pops up with the hours. Don't worry about every word yet. Later tickets explain each one.

```callout tip
Take away any one layer and the other two still work. Without CSS the page is plain. Without JavaScript the button does nothing. Without HTML there is nothing at all.
```

## Try it

You can run JavaScript right here. Press **Run**:

```js run
console.log("Namaste from Dhaaga!");
console.log("Coasters, keychains and tote bags, handmade in Pune.");
```

Each `console.log` prints one line. Change the text inside the quotes to anything you like and press **Run** again.

Then look back at the HTML in the worked example. Can you point to an opening tag, a closing tag, an attribute and some content?

## Recap

- **HTML** says what is on the page, **CSS** says how it looks, and **JavaScript** says what it does.
- An **element** is an opening tag, content and a closing tag: `<p>Hello</p>`.
- An **attribute** adds detail inside an opening tag: `href="https://dhaaga.in"`.
- A **CSS rule** is a selector plus declarations: `h1 { color: #2E3A8C; }`.
- The HTML file pulls in the others with `<link>` for CSS and `<script>` for JavaScript.

## Common mistakes

**1. Forgetting a closing tag.**

```html
<h1>Dhaaga & Co.
<p>Handmade crochet from Pune.</p>
```

The browser never sees the heading end. The paragraph lands inside the heading, so it shows up huge and bold. Close every element: `<h1>Dhaaga & Co.</h1>`.

**2. Missing a semicolon in CSS.**

```css
h1 {
  color: #2E3A8C
  font-size: 32px;
}
```

The browser reads `#2E3A8C font-size: 32px` as one value for `color`. It doesn't understand it, so it skips it. The heading stays black and normal-sized. End every declaration with `;`.

**3. Linking a file with the wrong name.**

```html
<link rel="stylesheet" href="styles.css">
```

The file is called `style.css`, without the s. The request for `styles.css` gets a 404, and the page shows up with no colours at all. Copy file names exactly.

**4. Mixing up Java and JavaScript.**

They are different languages, with different rules and different uses. When you look up help, search for "JavaScript" or "JS", or you'll get answers about Java.

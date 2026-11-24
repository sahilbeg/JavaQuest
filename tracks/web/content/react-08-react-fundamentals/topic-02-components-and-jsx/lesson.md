## Hook

Think of a rubber stamp with the Dhaaga logo. You carve it once. Then you stamp every bag, every receipt and every price card with it. If the logo changes, you carve one new stamp, not a hundred new bags.

A React component is that stamp for a piece of a page. You write the store header once, and every page stamps it in.

## Concept

### Syntax: a function component

```jsx
function StoreHeader() {
  return <h1>Dhaaga & Co.</h1>;
}
```

A component is a plain JavaScript function. It takes no special keyword. It returns JSX: a description of what to show.

The name must start with a capital letter. React uses that to tell your components apart from HTML tags. `<h1>` is an HTML tag, and `<StoreHeader>` is your component.

### Syntax: using a component

```jsx
function StoreFront() {
  return (
    <main>
      <StoreHeader />
      <StoreHeader />
    </main>
  );
}
```

You use a component like a tag. Here `StoreFront` shows the header twice, and each `<StoreHeader />` calls the function once.

The round brackets after `return` let the JSX start on the next line. Without them, JavaScript ends the `return` at the line break, and nothing is shown.

```callout jargon
**Composition** means building a bigger component out of smaller ones, like `StoreFront` holding `StoreHeader`. Most React pages are components inside components.
```

### Syntax: export and import

```jsx
// StoreHeader.jsx
export function StoreHeader() {
  return <h1>Dhaaga & Co.</h1>;
}

// StoreFront.jsx
import { StoreHeader } from "./StoreHeader.jsx";
```

Components are shared with the same `export` and `import` you used for modules. Usually each component file exports one or a few related components.

### Rule 1: return one parent

A component returns one thing. Two tags side by side are two things, and the build stops with a syntax error.

```jsx
// Wrong: two parents
function Hours() {
  return (
    <h2>Opening hours</h2>
    <p>Sat and Sun</p>
  );
}
```

Wrap them in one element, such as a `<section>`, or in a fragment when you don't want an extra element.

### Syntax: a fragment

```jsx
function Hours() {
  return (
    <>
      <h2>Opening hours</h2>
      <p>Sat and Sun</p>
    </>
  );
}
```

```callout jargon
A **fragment** is an empty tag, `<>` and `</>`, that groups elements without adding anything to the page. The `<h2>` and `<p>` land directly in whatever holds `Hours`.
```

### Rule 2: close every tag

In HTML, `<img>` and `<br>` can stay open. In JSX every tag must be closed. A tag with nothing inside closes itself with `/>`:

```jsx
<img src="golu.png" alt="Golu the crocheted elephant" />
```

Your own components close the same way: `<StoreHeader />`.

### Rule 3: attributes are camelCase

JSX attributes become JavaScript property names, so a few change their spelling:

| HTML | JSX |
|---|---|
| `class="price"` | `className="price"` |
| `for="phone"` | `htmlFor="phone"` |
| `onclick` | `onClick` |
| `tabindex` | `tabIndex` |

`class` and `for` are special words in JavaScript, so React uses `className` and `htmlFor`. Everything else is camelCase. `alt`, `src` and `id` stay the same.

### Syntax: curly braces

```jsx
<p>{TAGLINE}</p>
```

Curly braces are a window from JSX back into JavaScript. Anything that gives a value can go inside: a variable, `2 + 3`, a function call, or `item.name`.

```jsx
const TAGLINE = "Handmade crochet from Pune";
const photo = "golu.png";

function Intro() {
  return (
    <section className="intro">
      <p>{TAGLINE}</p>
      <p>{4 * 150} rupees buys four coaster sets.</p>
      <img src={photo} alt="Golu the crocheted elephant" />
    </section>
  );
}
```

- Text attributes use quotes: `alt="..."`. A JavaScript value uses braces: `src={photo}`. Never both, like `src="{photo}"`.
- Only values fit in braces. An `if` or a `for` is a statement, not a value, so it cannot go there. Later tickets show what to use instead.
- A comment inside JSX is written `{/* like this */}`.

### What JSX turns into

The browser never sees JSX. When you save, a build tool rewrites each tag as a function call. React then turns the calls into objects and the objects into elements.

```animate
title: From JSX to the page
kind: scene
nodes:
  - { id: file, label: "StoreHeader.jsx", emoji: "📄", value: "<h1>Dhaaga & Co.</h1>" }
  - { id: build, label: "Build tool", emoji: "🔧" }
  - { id: call, label: "Function call", emoji: "📞" }
  - { id: obj, label: "Object", emoji: "📋" }
  - { id: page, label: "Page", emoji: "🖥️" }
steps:
  - say: You write a tag inside your component. It looks like HTML, but it lives in a JavaScript file.
    focus: [file]
  - say: When you save, the build tool rewrites the tag as an ordinary function call.
    send: { from: file, to: build, label: "<h1>Dhaaga & Co.</h1>" }
  - say: The tag name becomes the first argument. The attributes and the text inside are packed into one object.
    send: { from: build, to: call, label: "jsx(\"h1\", { children: \"Dhaaga & Co.\" })" }
  - say: When React calls your component, that call returns a plain object. This is one piece of the virtual DOM.
    send: { from: call, to: obj, label: "{ type: \"h1\" }" }
  - say: React reads the object and creates a real h1 element on the page.
    send: { from: obj, to: page, label: "<h1>" }
    set: { page: "Dhaaga & Co." }
```

## Worked example

The store's footer shows the shop's opening days and phone number. Each contact line has the same shape, so it becomes its own component.

```jsx
const PHONE = "98765 43210";

function ContactLine() {
  return (
    <p className="contact">
      Call or WhatsApp: <a href="tel:+919876543210">{PHONE}</a>
    </p>
  );
}

export function SiteFooter() {
  return (
    <footer className="site-footer">
      <h2>Visit us</h2>
      <p>Aundh shop, open Tue to Sun, 11:00 to 21:00</p>
      <ContactLine />
      {/* The Diwali pop-up address goes here in October */}
    </footer>
  );
}
```

Line by line:

- `ContactLine` is a small component. It returns one `<p>`, with `className` instead of `class`.
- `{PHONE}` puts the value of the constant into the link text.
- `SiteFooter` returns one parent, the `<footer>`. Inside it, `<ContactLine />` stamps in the contact line.
- The `{/* ... */}` comment is not shown on the page.
- `export` lets `main.jsx` or another component import `SiteFooter`.

## Try it

This is roughly what the build tool makes of `<h1 className="title">Dhaaga & Co.</h1>`. The `jsx` function here is a stand-in for React's own. Run it, then change `"h1"` to `"h2"` and add a second prop, `id: "top"`.

```js run
function jsx(type, props) {
  return { type, props };
}

const element = jsx("h1", { className: "title", children: "Dhaaga & Co." });
console.log(element.type);
console.log(element.props.className);
console.log(element.props.children);
```

It prints `h1`, `title` and `Dhaaga & Co.`. A JSX tag is just a neat way of writing a call like this.

In this ticket's editor, the preview pane shows your components as you save. Change the tagline and watch the page change.

## Recap

- A component is a function, named with a capital letter, that returns JSX. You use it like a tag: `<StoreHeader />`.
- A component returns one parent element. A fragment, `<>...</>`, groups elements without adding one.
- Every JSX tag is closed, and attributes are camelCase: `className`, `htmlFor`, `onClick`.
- Curly braces put a JavaScript value into JSX: `<p>{TAGLINE}</p>` or `src={photo}`.
- Big components are built from small ones. That is composition.

## Common mistakes

**1. Naming a component with a small letter.**

```jsx
function storeHeader() {
  return <h1>Dhaaga & Co.</h1>;
}

<storeHeader />
```

Nothing appears. React treats `<storeHeader>` as an unknown HTML tag, and the console warns that the tag is unrecognised. Start the name with a capital letter: `StoreHeader`.

**2. Writing class instead of className.**

```jsx
<p class="price">₹150.00</p>
```

The page may still look right, but the console warns about an invalid DOM property and suggests `className`. Tests and other tools look for `className`. Use `<p className="price">`.

**3. Returning two elements side by side.**

```jsx
return (
  <h1>Dhaaga & Co.</h1>
  <p>Handmade crochet from Pune</p>
);
```

The build fails with a syntax error at the second tag, because a function can return only one thing. Wrap both in a `<header>` or a fragment.

**4. Putting the JSX on the line after a bare return.**

```jsx
function StoreHeader() {
  return
    <h1>Dhaaga & Co.</h1>;
}
```

The component shows nothing. JavaScript ends `return` at the line break, so the function returns `undefined`. Start the JSX on the same line, or open a round bracket: `return (`.

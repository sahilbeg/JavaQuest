## Hook

Think of a scoreboard at a school cricket match. One person writes the runs on the board. Another writes the wickets. A third updates the overs. If one of them looks away, the board lies.

The cart you built with the DOM works the same way. When a customer adds Golu the elephant, your code must update the badge, the total, the drawer list and the button. Forget one place, and the page says 2 items while the drawer shows 3.

Now picture a scoreboard that reads the scorer's book and redraws itself. Nobody updates the board by hand. That is the idea behind React.

## Concept

### The trouble with updating by hand

Here is the kind of code you wrote for the cart drawer. Every change to the data needs a matching change to the page.

```js
function addToCart(cart, item) {
  cart.push(item);
  document.querySelector("#count").textContent = String(cart.length);
  document.querySelector("#total").textContent = String(cart.length * item.paise);
  // The drawer list is not updated here. Nobody noticed.
}
```

The data says one thing and the page says another. With ten buttons and five places that show the cart, the number of updates grows fast.

```callout jargon
**Imperative** code says *how*, step by step: find this element, change its text, add that class. **Declarative** code says *what*: "the badge shows the number of items in the cart".
```

### Declarative: describe the result

React lets you write declarative pages. You write one function that says what the badge looks like for any cart. React keeps the page matching it.

```jsx
function CartBadge({ count }) {
  return <span className="badge">Cart ({count})</span>;
}
```

You never write "change the badge". When `count` changes, React updates the page for you. A short way to say this is **UI = f(state)**: the page is a function of the data.

```callout jargon
**State** is the data a page remembers while it is open and that can change, like the items in the cart or the text in a search box.
```

### Components: building blocks for pages

```callout jargon
A **component** is a JavaScript function that returns a description of one part of the page. Its name starts with a capital letter, like `CartBadge`.
```

A page is built from components inside components, like a tiffin box with smaller boxes inside. The store might look like this:

```text
App
├── StoreHeader
│   └── CartBadge
├── Catalogue
│   ├── ProductCard (Golu the elephant)
│   └── ProductCard (Marigold coaster set)
└── Footer
```

You write `ProductCard` once and use it for every product. Fix a bug in it, and every card is fixed.

### JSX: HTML-like code inside JavaScript

The `<span className="badge">` inside the function above is not a string and not HTML. It is **JSX**.

```callout jargon
**JSX** is a way to write tags inside JavaScript. A build tool, such as the one inside Vite, turns each tag into an ordinary function call before the browser sees it.
```

The next ticket covers JSX properly. For now, know two things. Tags that start with a capital letter are your components. Curly braces, like `{count}`, put a JavaScript value into the page.

### Rendering and re-rendering

```callout jargon
To **render** means to call a component and turn what it returns into elements on the page. A **re-render** is React calling the component again because its data changed.
```

Watch what happens when a customer taps Add:

```animate
title: One tap, one small change
kind: scene
nodes:
  - { id: tap, label: "Add button", emoji: "👆" }
  - { id: state, label: "State", emoji: "🧠", value: "count: 0" }
  - { id: comp, label: "CartBadge()", emoji: "🧩" }
  - { id: vdom, label: "Virtual DOM", emoji: "📋" }
  - { id: dom, label: "Real page", emoji: "🖥️", value: "Cart (0)" }
steps:
  - say: A customer taps Add on Golu the elephant.
    focus: [tap]
  - say: The click changes the state. Nothing on the page has changed yet.
    send: { from: tap, to: state, label: "count + 1" }
    set: { state: "count: 1" }
  - say: React sees new state, so it calls the component function again. This is a re-render.
    send: { from: state, to: comp, label: "count = 1" }
  - say: The function returns a fresh description of the badge. It is made of plain objects, not real elements.
    send: { from: comp, to: vdom, label: "<span>Cart (1)</span>" }
  - say: React compares the new description with the old one. Only the text inside the badge is different.
    focus: [vdom]
  - say: React changes just that one piece of text. The header, the photos and the buttons are left alone.
    send: { from: vdom, to: dom, label: "text: Cart (1)" }
    set: { dom: "Cart (1)" }
```

### The virtual DOM

```callout jargon
The **virtual DOM** is React's description of the page, made of plain JavaScript objects. It is cheap to build, because no real elements are touched.
```

Changing real elements is slow compared with building objects. So React builds the new description first. Then it changes only the real elements that need it.

### Reconciliation: finding what changed

```callout jargon
**Reconciliation** is React comparing the old description with the new one to find the smallest set of changes for the real page.
```

React's real comparison is clever, but the idea is simple. Here it is with two lists of text:

```animate
title: Compare, then change only what differs
kind: code
language: js
code: |
  const before = ["Golu", "₹450.00", "Cart (0)"];
  const after = ["Golu", "₹450.00", "Cart (1)"];
  for (let i = 0; i < after.length; i++) {
    if (before[i] !== after[i]) {
      console.log(`change ${i}: ${after[i]}`);
    }
  }
steps:
  - line: 1
    vars: { before: '["Golu", "₹450.00", "Cart (0)"]' }
    say: This is the old description of three pieces of the page.
  - line: 2
    vars: { after: '["Golu", "₹450.00", "Cart (1)"]' }
    say: This is the new description, after the customer tapped Add.
  - line: 3
    vars: { i: 0 }
    say: Walk both lists side by side, starting at position 0.
  - line: 4
    say: '"Golu" equals "Golu". Nothing to change here.'
  - line: 3
    vars: { i: 1 }
    say: Position 1 holds the price in both lists. Same again.
  - line: 3
    vars: { i: 2 }
    say: Position 2 is the badge.
  - line: 4
    say: '"Cart (0)" is not "Cart (1)". This one differs.'
  - line: 5
    out: "change 2: Cart (1)"
    say: Only position 2 gets changed. React does the same on a much bigger tree of objects.
```

### Syntax: starting a React app

```jsx
import { createRoot } from "react-dom/client";
import { App } from "./App.jsx";

createRoot(document.getElementById("root")).render(<App />);
```

This lives in `main.jsx`. It finds the empty `<div id="root">` in the page and tells React to draw `App` inside it. From then on, React owns everything inside that div.

```callout jargon
The **root** is the one element React draws your app into. `createRoot` connects React to it, and `render` puts the first component there.
```

```callout tip
A Vite project made with the React template already has this file and an `index.html` with `<div id="root"></div>`. You set up Vite in the tooling project.
```

### A library, not the whole house

React only handles the page: components, state and updates. It does not fetch data, change pages or store things for you. Teams add other tools for that, such as React Router for pages. That is why React is called a **library**, not a framework.

## Worked example

Here is the "page as a function of the data" idea in plain JavaScript. There is no React yet, just a function that turns state into text.

```js run
function cartBadge(state) {
  return `Cart (${state.items.length})`;
}

let state = { items: [] };
console.log(cartBadge(state));

state = { items: ["DC-1042"] };
console.log(cartBadge(state));

state = { items: ["DC-1042", "DC-1043"] };
console.log(cartBadge(state));
```

Line by line:

- `cartBadge` never changes anything. It only describes the badge for the state it is given.
- Each time the state changes, we call it again and get the right text: `Cart (0)`, `Cart (1)`, `Cart (2)`.
- Nowhere do we say "add one to the badge". The badge can never fall out of step with the data.

React does the same, with JSX instead of text, and it calls the function for you:

```jsx
function CartBadge({ items }) {
  return <span className="badge">Cart ({items.length})</span>;
}
```

## Try it

This tiny "React" turns a tree of plain objects into HTML text. Run it. Then change `CartBadge(2)` to `CartBadge(5)` and run it again. Only the badge part of the output changes.

```js run
function render(node) {
  if (typeof node === "string") return node;
  const inside = node.children.map(render).join("");
  return `<${node.type}>${inside}</${node.type}>`;
}

function CartBadge(count) {
  return { type: "span", children: ["Cart (", String(count), ")"] };
}

const tree = {
  type: "header",
  children: [{ type: "h1", children: ["Dhaaga & Co."] }, CartBadge(2)],
};
console.log(render(tree));
```

`tree` is a little virtual DOM: objects that describe the page. `render` walks it and builds the real thing, here as text.

## Recap

- Updating the page by hand means every change must be repeated in every place, and one miss makes the page lie.
- React is declarative: you describe what the page looks like for the current state, and React keeps it that way.
- A component is a function, named with a capital letter, that returns JSX describing part of the page.
- When state changes, React re-renders the component, compares the new virtual DOM with the old one, and changes only what differs.
- `createRoot(...).render(<App />)` in `main.jsx` starts the app inside `<div id="root">`.

## Common mistakes

**1. Changing the page by hand inside a React app.**

```js
document.querySelector(".badge").textContent = "Cart (9)";
```

It seems to work. Then the next re-render puts back whatever the component describes, and your change vanishes. In React, change the state instead, and let the component describe the badge.

**2. Thinking the component runs only once.**

```jsx
function CartBadge({ count }) {
  console.log("drawing the badge");
  return <span>Cart ({count})</span>;
}
```

The message prints every time `count` changes, not just once. React calls the component on every re-render. Keep component functions quick, and never start slow work in them.

**3. Thinking React replaces JavaScript.**

React is JavaScript. Everything you learned still applies: `map`, `filter`, objects, modules and `===`. React just calls your functions and updates the page. If you are stuck on a React bug, it is often a plain JavaScript bug.

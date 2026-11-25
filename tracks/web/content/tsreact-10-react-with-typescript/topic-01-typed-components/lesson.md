## Hook

Sana's design for dhaaga.in has thirty item cards. Every card has the same shape: a name, a price, a colour and a little badge. Only the words change.

Writing thirty copies of the same HTML is like hand-writing thirty price tags. One typo, and one tag is wrong. A printed tag with blanks is better: you design it once, then fill in the blanks for each item.

React lets you build a page from pieces like that. TypeScript then checks that every blank is filled with the right kind of value.

## Concept

### What React is

In web-05 you changed the page yourself: find an element, set its text, add a class. Every change was a step you had to remember.

React works the other way round. You describe what the page should look like for the data you have. React works out which elements to create or change.

```callout jargon
**React** is a JavaScript library for building pages out of components. A **library** is code someone else wrote that your code calls.
```

```callout jargon
A **component** is a function that returns a description of a piece of the page. Its name starts with a capital letter, like `ItemCard`.
```

React files that contain these descriptions end in `.tsx` in TypeScript (or `.jsx` in plain JavaScript).

### Syntax: a component

```tsx
export function StoreHeader() {
  return <h1>Dhaaga & Co.</h1>;
}
```

A component is an ordinary function. It returns JSX, which looks like HTML but lives inside your code. You use a component like a tag: `<StoreHeader />`.

```callout jargon
**JSX** is the HTML-like syntax inside `.tsx` files. A build tool turns each tag into a function call before the browser sees it.
```

The capital letter matters. `<h1>` is an HTML tag. `<StoreHeader />` is your component. React tells them apart by the first letter.

### JSX rules

JSX is strict in a few places where HTML is relaxed.

| Rule | Example |
|---|---|
| Return one parent element | wrap two tags in a `<header>` |
| Close every tag | `<img src="golu.png" alt="Golu" />` |
| `className`, not `class` | `<p className="price">` |
| `htmlFor`, not `for` | `<label htmlFor="phone">` |
| Curly braces put a value in | `<p>{name}</p>` |

Anything that gives a value fits inside `{ }`: a variable, a sum, a function call. An `if` statement does not, because it is not a value.

If a `return` is followed by JSX on the next line, wrap the JSX in round brackets: `return ( ... );`. Otherwise JavaScript ends the `return` at the line break.

### Syntax: typed props

```tsx
interface ItemCardProps {
  name: string;
  paise: number;
}

function ItemCard({ name, paise }: ItemCardProps) {
  return <h3>{name} costs {paise} paise</h3>;
}
```

```callout jargon
**Props** (short for properties) are the values a component is given, written like attributes: `<ItemCard name="Golu the elephant" paise={45000} />`.
```

React calls your component with one object holding every prop. You describe that object with an interface, exactly as you did in ts-08. Destructuring takes the props out by name.

Text props use quotes. Every other value, like a number, goes in braces: `paise={45000}`.

### TypeScript checks every use

Now the interface is a contract. Leave out a prop, and `tsc` stops before anything runs:

```tsx
<ItemCard name="Golu the elephant" />
```

```text
error TS2741: Property 'paise' is missing in type '{ name: string; }' but required in type 'ItemCardProps'.
```

Pass the wrong kind of value, and it stops too:

```tsx
<ItemCard name="Golu the elephant" paise="45000" />
```

```text
error TS2322: Type 'string' is not assignable to type 'number'.
```

In plain React, both mistakes would reach the page. A customer would see a blank price, or a total like `₹NaN`.

### Syntax: optional props and defaults

```tsx
interface BadgeProps {
  text?: string;
}

function Badge({ text = "New" }: BadgeProps) {
  return <span className="badge">{text}</span>;
}
```

`text?: string` makes the prop optional. Callers may write `<Badge />` or `<Badge text="Sale" />`.

The `= "New"` in the destructuring is a default value. It is used whenever `text` is `undefined`, so inside the function `text` is always a `string`.

### Putting it on the page

One file starts the app. It finds the empty `<div id="root">` in the page and asks React to draw into it.

```tsx
import { createRoot } from "react-dom/client";
import { StoreHeader } from "./StoreHeader.tsx";

const root = document.getElementById("root");
if (root) {
  createRoot(root).render(<StoreHeader />);
}
```

`getElementById` returns `HTMLElement | null`, as you saw in ts-09. The `if` narrows it, so `createRoot` gets a real element.

```animate
title: From ItemCard to the page
kind: scene
nodes:
  - { id: main, label: "main.tsx", emoji: "📄", value: "<ItemCard ... />" }
  - { id: tsc, label: "tsc", emoji: "🔍" }
  - { id: card, label: "ItemCard()", emoji: "🧩" }
  - { id: react, label: "React", emoji: "⚛️" }
  - { id: page, label: "Page", emoji: "🖥️", value: "empty #root" }
steps:
  - say: main.tsx asks for an ItemCard with a name and a price in paise.
    focus: [main]
  - say: Before anything runs, tsc compares the props with ItemCardProps. Every prop is there, with the right type.
    send: { from: main, to: tsc, label: "name: string, paise: number" }
  - say: React calls ItemCard with one object holding all the props.
    send: { from: react, to: card, label: "{ name, paise }" }
  - say: ItemCard returns JSX, a description of an article with a heading and a price.
    send: { from: card, to: react, label: "<article>...", back: true }
  - say: React creates the real elements inside the root div. The card appears.
    send: { from: react, to: page, label: "create elements" }
    set: { page: "Golu the elephant ₹450.00" }
```

## Worked example

The website footer shows where to find Dhaaga. Each place has the same shape, so it becomes a typed component.

```tsx
interface PlaceCardProps {
  place: string;
  days: string;
  hours: string;
  note?: string;
}

export function PlaceCard({ place, days, hours, note = "Pay by cash, UPI or card" }: PlaceCardProps) {
  return (
    <section className="place-card">
      <h2>{place}</h2>
      <p className="when">
        {days}, {hours}
      </p>
      <p className="note">{note}</p>
    </section>
  );
}
```

Line by line:

- `PlaceCardProps` lists four props. Three are required, and `note` is optional.
- The destructuring gives `note` a default, so the stall card can leave it out.
- The JSX returns one parent, a `<section>`, with `className` for the CSS class.
- `{days}, {hours}` puts two values in one line of text, with a comma between them.

You would use it like this:

```tsx
<PlaceCard place="FC Road stall" days="Sat and Sun" hours="10:00 to 20:00" />
<PlaceCard place="Aundh shop" days="Tue to Sun" hours="11:00 to 21:00" note="Gift wrap on request" />
```

## Try it

Props are just a typed object passed to a function. This snippet does the same work without React, so you can run it. Run it, then call `describeCard` with `badge: "Diwali special"`.

```ts run
interface ItemCardProps {
  name: string;
  paise: number;
  badge?: string;
}

function describeCard({ name, paise, badge = "Handmade" }: ItemCardProps): string {
  return `${name} | ${paise / 100} rupees | ${badge}`;
}

console.log(describeCard({ name: "Golu the elephant", paise: 45000 }));
console.log(describeCard({ name: "Mint keychain", paise: 12000, badge: "New" }));
```

It prints `Golu the elephant | 450 rupees | Handmade`, then `Mint keychain | 120 rupees | New`. The first call left out `badge`, so the default filled it in.

In this ticket's editor, the preview shows your components as you save. Try `paise="45000"` in `main.tsx` and watch the check fail.

## Recap

- A component is a function, named with a capital letter, that returns JSX. You use it like a tag: `<ItemCard />`.
- JSX returns one parent, closes every tag, and uses `className`. Curly braces put a value into it.
- Props are one object. Describe them with an interface, and `tsc` checks every place the component is used.
- `badge?: string` makes a prop optional. A default in the destructuring, `badge = "Handmade"`, fills it in.
- `createRoot(root).render(...)` draws the app into `#root`, after an `if` checks the element exists.

## Common mistakes

**1. Leaving the props untyped.**

```tsx
function ItemCard({ name, paise }) {
  return <h3>{name}</h3>;
}
```

In strict mode, `tsc` stops with `Binding element 'name' implicitly has an 'any' type.` Write the interface and add `: ItemCardProps` after the braces.

**2. Passing a number as text.**

```tsx
<ItemCard name="Golu the elephant" paise="45000" />
```

`tsc` says `Type 'string' is not assignable to type 'number'.` Quotes always make text. Write `paise={45000}`.

**3. Naming a component with a small letter.**

```tsx
function itemCard() {
  return <h3>Golu the elephant</h3>;
}
```

`<itemCard />` is treated as an HTML tag that doesn't exist, so the card never appears. Name it `ItemCard`.

**4. Writing class instead of className.**

```tsx
<p class="price">₹450.00</p>
```

`tsc` reports that `class` does not exist on the paragraph's props. JSX uses `className`, because `class` is a keyword in JavaScript.

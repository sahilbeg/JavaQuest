## Hook

At the Aundh shop, Raju packs every gift in the same Marigold box. The box doesn't care what goes inside: booties, a keychain, or Golu himself. It just has the Dhaaga label on top and room for anything.

Pages need boxes like that too. An info box with a title, and whatever content fits that day. In React, the content you put inside a component is called its children.

## Concept

### Composition: components inside components

In the last ticket, each component stood alone. Real pages are built by putting components inside other components.

```tsx
function StorePage() {
  return (
    <main>
      <StoreHeader />
      <ItemCard name="Golu the elephant" paise={45000} colour="Indigo" />
    </main>
  );
}
```

```callout jargon
**Composition** means building a bigger component out of smaller ones. `StorePage` is composed of `StoreHeader` and `ItemCard`.
```

Small components are easy to read, test and reuse. A page is a tree of them.

### Syntax: the children prop

```tsx
import type { ReactNode } from "react";

interface BoxProps {
  label: string;
  children: ReactNode;
}

function Box({ label, children }: BoxProps) {
  return (
    <div className="box">
      <h2>{label}</h2>
      {children}
    </div>
  );
}
```

You use it with an opening and a closing tag:

```tsx
<Box label="Gift wrap">
  <p>₹30, with a handwritten card.</p>
</Box>
```

Everything between `<Box>` and `</Box>` reaches the component as one prop, called `children`. `Box` decides where to put it, by writing `{children}` in its JSX.

```callout jargon
**children** is the prop that holds whatever you write between a component's opening and closing tags.
```

### Syntax: ReactNode

```tsx
children: ReactNode;
```

`ReactNode` is React's type for "anything React can show". It includes:

- text and numbers: `"Free above ₹999"`, `42`
- JSX elements: `<p>...</p>`, `<ItemCard ... />`
- arrays of those
- `null`, `undefined`, `true` and `false`, which show nothing

```callout jargon
**ReactNode** is the type of anything React can draw. You import it with `import type { ReactNode } from "react";`.
```

Use `ReactNode` for children almost always. A narrower type, like `string`, refuses elements. That makes the box much less useful.

### TypeScript checks children too

If the props interface has no `children`, passing some is a type error:

```tsx
interface BoxProps {
  label: string;
}

<Box label="Delivery">
  <p>Inside Pune: ₹60.00</p>
</Box>
```

```text
error TS2322: Type '{ label: string; children: Element; }' is not assignable to type 'IntrinsicAttributes & BoxProps'.
  Property 'children' does not exist on type 'IntrinsicAttributes & BoxProps'.
```

The error is a good thing. A component that ignores its children would silently drop them, and the page would lose content.

```callout tip
React also offers `PropsWithChildren<{ label: string }>`, which adds an optional `children?: ReactNode`. Writing `children` in your own interface says more clearly whether it is required.
```

### Syntax: slots

```tsx
interface LayoutProps {
  header: ReactNode;
  children: ReactNode;
}
```

Sometimes one box of content is not enough. A layout may need a header part and a main part. Any prop can be a `ReactNode`, so you can pass JSX in an ordinary prop:

```tsx
<Layout header={<h1>Dhaaga & Co.</h1>}>
  <ItemCard name="Golu the elephant" paise={45000} colour="Indigo" />
</Layout>
```

```callout jargon
A **slot** is a prop that holds a piece of page, like `header` here. The component decides where each slot goes.
```

### Syntax: fragments

```tsx
function Prices() {
  return (
    <>
      <p>Coaster set: ₹150.00</p>
      <p>Keychain: ₹120.00</p>
    </>
  );
}
```

A component returns one parent. When you don't want an extra `<div>`, wrap the elements in a fragment, `<>` and `</>`. The two paragraphs land directly inside whatever holds `Prices`.

```callout jargon
A **fragment** is an empty tag, `<>...</>`, that groups elements without adding one to the page.
```

### How children travel

```animate
title: A Panel receives its children
kind: scene
nodes:
  - { id: page, label: "StorePage", emoji: "🧩" }
  - { id: panel, label: "Panel", emoji: "📦", value: "title: Delivery" }
  - { id: kids, label: "children", emoji: "🎁", value: "<p>Inside Pune: ₹60.00</p>" }
  - { id: dom, label: "Page", emoji: "🖥️" }
steps:
  - say: StorePage writes a Panel with a paragraph between its tags.
    focus: [page]
  - say: React calls Panel with one props object. The title is there, and the paragraph arrives as children.
    send: { from: page, to: panel, label: "{ title, children }" }
    show: [kids]
  - say: Panel doesn't look inside children. It only decides where they go, under its heading.
    focus: [panel, kids]
  - say: React draws the section, the heading, and the paragraph inside the body.
    send: { from: panel, to: dom, label: "<section>...</section>" }
    set: { dom: "Delivery / Inside Pune: ₹60.00" }
```

## Worked example

A notice box for special days, like the Diwali hours. It has a heading, any content, and an optional footer slot.

```tsx
import type { ReactNode } from "react";

interface NoticeProps {
  heading: string;
  children: ReactNode;
  footer?: ReactNode;
}

export function Notice({ heading, children, footer }: NoticeProps) {
  return (
    <aside className="notice">
      <h2>{heading}</h2>
      <div className="notice-body">{children}</div>
      <footer>{footer}</footer>
    </aside>
  );
}
```

Line by line:

- `children: ReactNode` is required, because a notice with nothing in it makes no sense.
- `footer?: ReactNode` is an optional slot. When it is left out, it is `undefined`, which shows nothing.
- `Notice` places each part. The code that uses it decides what each part holds:

```tsx
<Notice heading="Diwali hours" footer={<small>See you at the pop-up!</small>}>
  <p>Koregaon Park pop-up: every day, 10:00 to 22:00</p>
  <p>Aundh shop: as usual</p>
</Notice>
```

## Try it

React skips `null`, `undefined`, `true` and `false` when it draws children, and joins the rest. This little function does the same for text. Run it, then add `0` to the second list and predict what shows.

```ts run
type Child = string | number | boolean | null | undefined | Child[];

function toText(child: Child): string {
  if (child === null || child === undefined || typeof child === "boolean") {
    return "";
  }
  if (Array.isArray(child)) {
    return child.map(toText).join("");
  }
  return String(child);
}

console.log(toText(["Open ", 11, ":00 to ", 21, ":00"]));
console.log(toText([null, "Tue to Sun", false, undefined]));
```

It prints `Open 11:00 to 21:00`, then `Tue to Sun`. A `0` would show, because it is a number, not a boolean. Keep that in mind for later tickets.

## Recap

- Composition: pages are components inside components.
- Whatever sits between a component's tags arrives as `children`. The component places it with `{children}`.
- Type children as `ReactNode`, imported with `import type { ReactNode } from "react";`.
- Any prop can hold JSX. Several `ReactNode` props are slots, like `header` and `children`.
- A fragment, `<>...</>`, groups elements without adding one to the page.

## Common mistakes

**1. Taking children but never showing them.**

```tsx
function Panel({ title, children }: PanelProps) {
  return <section className="panel"><h2>{title}</h2></section>;
}
```

The box shows its title and nothing else. No error appears, because ignoring a prop is allowed. Put `{children}` where the content belongs.

**2. Typing children as string.**

```tsx
interface PanelProps {
  title: string;
  children: string;
}
```

`<Panel title="Delivery"><p>₹60.00</p></Panel>` fails with `This JSX tag's 'children' prop expects type 'string' which requires multiple children, but only a single child was provided.` Use `children: ReactNode`.

**3. Forgetting children in the interface.**

```tsx
interface PanelProps {
  title: string;
}
```

Every `<Panel>` with content inside fails with `Property 'children' does not exist on type 'IntrinsicAttributes & PanelProps'.` Add `children: ReactNode`.

**4. Returning two elements without a parent.**

```tsx
function Prices() {
  return (
    <p>Coaster set: ₹150.00</p>
    <p>Keychain: ₹120.00</p>
  );
}
```

The build stops with a syntax error at the second tag, because a function returns one thing. Wrap both in a fragment, `<>...</>`, when you don't want an extra element.

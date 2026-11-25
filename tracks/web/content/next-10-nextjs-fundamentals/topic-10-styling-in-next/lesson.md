## Hook

In a school, "the Science room" could mean three different rooms in three different buildings. So each building adds its own label: "B-block Science room". Same word, no mix-ups.

On the new site, the Help page and the item cards both use a class called `.card`. CSS has only one big building, so both rules land on both. The item cards get the Help page's grey box. Next.js can give each component's class names their own label.

## Concept

### Three ways to style

Next.js supports several ways to write CSS. Most sites mix two of them:

| Way | Good for |
|---|---|
| **Global CSS** (`globals.css`) | the base of every page: colours, fonts, `body`, links |
| **CSS Modules** (`Name.module.css`) | one component's own styles, safe from clashes |
| **Tailwind CSS** | small ready-made classes written straight in `className` |

Everything you learned in web-02 still applies. The new part is how the CSS gets into the page.

### Syntax: global CSS

```jsx
// src/app/layout.jsx
import "./globals.css";
```

A global stylesheet is imported once, in the root layout, with a bare `import`. Every rule in it applies to every page. Next.js bundles it and adds the `<link>` tag for you.

Put only site-wide things in it: brand colours as custom properties, the `body`, links and headings.

```css
:root {
  --marigold: #f2a81d;
  --indigo: #2e3a8c;
}

body {
  color: var(--indigo);
}
```

```callout jargon
A **custom property** (or CSS variable) is a value with a name starting with `--`, set once, often on `:root`, and used anywhere with `var(--name)`. You met them in web-02. Change Sana's Indigo in one place, and the whole site follows.
```

### Why global class names clash

Every class name in global CSS shares one space. If `help.css` says `.card { background: #eee; }` and the item cards also use `class="card"`, the item cards get a grey background too. On a big site, with many people writing CSS, this happens all the time.

### Syntax: a CSS Module

```css
/* src/app/items/ItemCard.module.css */
.card {
  border: 2px solid var(--marigold);
}
```

```jsx
import styles from "./ItemCard.module.css";

<article className={styles.card}>...</article>
```

A file whose name ends in `.module.css` is a **CSS Module**. Importing it gives an object, `styles`, with one property per class. `styles.card` holds a unique class name that Next.js made for this file only.

```callout jargon
**Scoped** styles apply to one part of the page only. A CSS Module scopes its classes to the components that import it, by giving each class a unique name when the site is built.
```

### How the names become unique

```animate
title: Two .card classes, no clash
kind: scene
nodes:
  - { id: help, label: Help.module.css, emoji: "📄", value: ".card" }
  - { id: item, label: ItemCard.module.css, emoji: "📄", value: ".card" }
  - { id: build, label: next build, emoji: "⚙️" }
  - { id: page, label: The page's CSS, emoji: "🎨" }
steps:
  - say: Two files both have a class called card. In plain global CSS, they would fight.
    focus: [help, item]
  - say: When the site is built, Next.js reads each module.
    send: [{ from: help, to: build, label: ".card" }, { from: item, to: build, label: ".card" }]
  - say: It renames each class, adding the file's name and a short code.
    set: { help: ".Help_card__a1b2", item: ".ItemCard_card__x7y8" }
  - say: The page's CSS now has two different classes. Neither can touch the other's elements.
    send: [{ from: build, to: page, label: ".Help_card__a1b2" }, { from: build, to: page, label: ".ItemCard_card__x7y8" }]
  - say: In ItemCard.jsx, styles.card holds "ItemCard_card__x7y8", so the card gets its own style and nothing else.
    focus: [item]
```

Because the real name is made at build time, you must always write `styles.card`, never the plain text `"card"`.

### Syntax: combining classes

```jsx
className={`${styles.chip} ${styles.active}`}
```

An element can have several classes, separated by spaces. With modules, join the generated names in a template literal. To add a class only sometimes, choose with a condition:

```js run
const styles = { chip: "Filters_chip__x7f2", active: "Filters_active__k9q1" };
for (const selected of [false, true]) {
  console.log(selected ? `${styles.chip} ${styles.active}` : styles.chip);
}
```

Class names in a module are easiest to use in camelCase, like `.soldOut`, so you can write `styles.soldOut`. A name with dashes, like `.sold-out`, needs `styles["sold-out"]`.

### Tailwind CSS

```jsx
<p className="font-bold text-indigo-900">₹450.00</p>
```

Many Next.js teams use **Tailwind CSS**. Instead of writing your own rules, you combine small ready-made classes, each doing one thing: `font-bold`, `text-indigo-900`, `p-4`. `create-next-app` can set it up for you. It can't clash either, because every class means the same thing everywhere. Dhaaga sticks to plain CSS and modules for now.

### Styles and server components

Global CSS, CSS Modules and Tailwind all work in server components, because they're plain CSS files read at build time. Some older "CSS-in-JS" libraries create styles with JavaScript while the page runs. Those only work in client components, which is one reason many teams have moved to the options above.

## Worked example

The filter buttons above the shop list, with their own module:

```css
/* src/app/items/Filters.module.css */
.chip {
  border: 1px solid var(--indigo);
  border-radius: 999px;
  padding: 0.25rem 0.75rem;
}

.active {
  background: var(--indigo);
  color: var(--off-white);
}
```

```jsx
// src/app/items/Filters.jsx
import styles from "./Filters.module.css";

export function ColourChip({ name, selected }) {
  return <span className={selected ? `${styles.chip} ${styles.active}` : styles.chip}>{name}</span>;
}
```

- The file ends in `.module.css`, so its classes are scoped to this component.
- `.chip` and `.active` use the brand's custom properties from `globals.css`, which every page has.
- The selected chip gets both classes, joined with a space.
- Another component can have its own `.chip` without any clash.

## Try it

Run the snippet in "Syntax: combining classes" and change the class names. Then think about this ticket: what should a card for the Mint keychain, which is sold out, get as its `className`?

## Recap

- Import `globals.css` once in the root layout. Keep site-wide things in it: custom properties, `body`, links.
- A `.module.css` file is a CSS Module. Import it as `styles` and use `className={styles.card}`.
- Modules get unique class names at build time, so two components can both have `.card` without a clash.
- Join several module classes with a space in a template literal, and choose extra classes with a condition.
- Tailwind is a popular alternative. All three work in server components.

## Common mistakes

**1. Writing the class name as text.**

```jsx
import styles from "./ItemCard.module.css";

<article className="card">...</article>
```

The card has no border. The real class is something like `ItemCard_card__x7y8`, so `"card"` matches nothing, or worse, matches a global `.card`.

Fix: `className={styles.card}`.

**2. A misspelled class.**

```jsx
<p className={styles.prise}>₹450.00</p>
```

No error appears. `styles.prise` is `undefined`, so the element gets no class at all.

Fix: check the spelling against the `.module.css` file, here `styles.price`.

**3. Global selectors in a module.**

```css
/* ItemCard.module.css */
:root {
  --card-border: #f2a81d;
}
```

Next.js stops with an error saying the selector is not pure. A module's rules must target its own classes.

Fix: put `:root`, `body` and other site-wide rules in `globals.css`.

**4. Importing the global stylesheet in every component.**

```jsx
// in ItemCard.jsx, Filters.jsx, NavBar.jsx...
import "../globals.css";
```

It works, but site-wide rules now look as if they belong to each component, and it's hard to tell where a style comes from or whether a file can be deleted.

Fix: import `globals.css` once, in the root layout.

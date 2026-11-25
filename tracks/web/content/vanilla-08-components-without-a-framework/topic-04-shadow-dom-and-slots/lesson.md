## Hook

A jeweller's shop window has a glass front. You can see the display, but you can't reach in and move things. The jeweller, though, leaves a few empty stands at the front, where today's pieces go.

**Shadow DOM** is that glass. A component keeps its own markup and styles behind it, safe from the page's CSS. **Slots** are the empty stands: the page puts its own content into them, and the component decides where it shows.

## Concept

### Styles leak both ways

The blog post has a rule for its own headings:

```css
h3 { color: purple; font-family: cursive; }
```

Every product card inside the post has an `<h3>` too, so every card turns purple. It works the other way as well: a card that styles `p` can repaint the blog's paragraphs. CSS rules apply to the whole page, so a component's look is never really its own.

### Syntax: attachShadow

```js
this.attachShadow({ mode: "open" });
```

- It gives the element a **shadow root**: a separate, hidden tree of elements that belongs to it.
- What you put there is drawn inside the element, but the page's CSS and `document.querySelector` can't reach it.
- With `mode: "open"`, your own code can still get in through `element.shadowRoot`. Use open unless you have a strong reason not to.

```callout jargon
**Shadow DOM** is a hidden tree of elements attached to one element, its **shadow root**. Styles inside it stay inside, and the page's styles stay out. The element's normal children are called its **light DOM**.
```

### Attach it once, in the constructor

```js
class DhaagaCard extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: "open" }).innerHTML = CARD_HTML;
  }
}
```

- `attachShadow` returns the shadow root, so you can fill it straight away.
- An element can have only **one** shadow root. A second `attachShadow` throws an error.
- The constructor runs once per element. `connectedCallback` runs every time the element is moved on the page, so it's the wrong place for this.

### Styles inside stay inside

Put a `<style>` element inside the shadow root:

```js
const CARD_HTML = `
  <style>
    :host { display: block; border: 2px solid #2E3A8C; border-radius: 12px; }
    h3 { color: #2E3A8C; }
  </style>
  <article><h3>Golu the elephant</h3></article>
`;
```

- `h3 { color: #2E3A8C; }` styles only the `<h3>` inside this card's shadow root. The blog's headings don't change.
- The blog's `h3 { color: purple; }` can't reach in, so the card stays Indigo.
- Short, simple selectors are safe again, because each component has its own little world.
- One thing still flows in: **inherited** values, like `color` and `font-family`, set on the card or its parents. Page rules that pick elements, like `h3 { ... }`, never do.

### Syntax: :host

```css
:host { display: block; }
:host([sale]) { border-color: #F2A81D; }
```

- Inside a shadow root, `:host` means the custom element itself: the `<dhaaga-card>` on the page.
- `:host([sale])` matches only when the element has a `sale` attribute.
- Custom elements are `display: inline` by default, so most cards need `:host { display: block; }`.

### Syntax: slots

```html
<slot name="title"></slot>
<slot></slot>
```

- A `<slot>` is a hole in the shadow markup. The element's light DOM children are shown through it.
- A child with `slot="title"` goes into the slot named `title`.
- A slot with no name is the **default slot**. Every child without a `slot` attribute goes there.

```callout jargon
A **slot** is a placeholder in a component's shadow DOM where the page's own content appears. Putting content into one is called **slotting**.
```

The page writes this:

```html
<dhaaga-card>
  <h3 slot="title">Golu the elephant</h3>
  <p>Crocheted in Indigo, about 15 cm tall.</p>
</dhaaga-card>
```

```animate
title: Children find their slots
kind: scene
nodes:
  - { id: title, label: "h3 slot=title", emoji: "🏷️", row: 0, col: 0 }
  - { id: blurb, label: "p (no slot)", emoji: "📝", row: 1, col: 0 }
  - { id: header, label: "slot name=title", emoji: "🔲", value: empty, row: 0, col: 3 }
  - { id: main, label: "default slot", emoji: "🔲", value: empty, row: 1, col: 3 }
  - { id: footer, label: "slot name=price", emoji: "🔲", value: "Price on request", row: 2, col: 3 }
steps:
  - say: The card's light DOM has two children. Its shadow DOM has three slots, waiting.
    focus: [title, blurb]
  - say: The h3 says slot="title", so it is shown through the slot with that name.
    send: { from: title, to: header, label: "slot=title" }
    set: { header: "Golu the elephant" }
  - say: The p has no slot attribute, so it goes to the default slot.
    send: { from: blurb, to: main, label: "no name" }
    set: { main: "Crocheted in Indigo..." }
  - say: Nothing asked for the price slot, so it shows its own fallback text.
    focus: [footer]
  - say: The h3 and p never moved. They are still the card's children, so the page can still find and style them.
    focus: [title, blurb]
```

### Fallback content

```html
<slot name="price">Price on request</slot>
```

Whatever sits between `<slot>` and `</slot>` is **fallback content**. It shows only when nothing is slotted there. Once the page adds `<p slot="price">₹450.00</p>`, the fallback hides.

### Slotted content stays in the light DOM

Slotting shows a child in a new place, but it doesn't move it:
- `card.querySelector("h3")` still finds the slotted heading.
- The page's own CSS still styles it, because it is the page's element.
- Inside the shadow root, `slot.assignedElements()` lists what was slotted there.

From inside, you can style slotted children lightly with `::slotted(h3) { margin: 0; }`. Only the slotted element itself can be matched, not what is inside it. If the page's CSS sets the same property, the page's rule wins.

## Worked example

The yarn shelf at the shop shows each colour as a swatch with a label. The page decides the label; the swatch decides its own look.

```js
const SWATCH_HTML = `
  <style>
    :host { display: inline-flex; align-items: center; gap: 0.5rem; }
    .chip { width: 1.5rem; height: 1.5rem; border-radius: 50%; background: var(--colour, #ccc); }
  </style>
  <span class="chip" part="chip"></span>
  <slot>Unnamed colour</slot>
`;

class YarnSwatch extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: "open" }).innerHTML = SWATCH_HTML;
  }
}

if (!customElements.get("yarn-swatch")) {
  customElements.define("yarn-swatch", YarnSwatch);
}
```

```html
<yarn-swatch style="--colour: #F2A81D">Marigold, 420 g</yarn-swatch>
```

1. The shadow root is attached once, in the constructor.
2. `.chip` is a tiny class name, but it can't clash with anything: it lives in the swatch's own tree.
3. The default slot shows the text "Marigold, 420 g". Without text, the fallback "Unnamed colour" shows.
4. `var(--colour, #ccc)` reads a CSS variable set on the page. Variables are inherited, so they pass into a shadow root, and they are the way a component invites the page to style it. The styling ticket uses them more.

## Try it

Slot assignment follows a simple rule: the `slot` attribute picks the slot, and no attribute means the default one. This plain JavaScript copies that rule. Run it, then give the second `p` the slot `price`.

```js run
const children = [
  { tag: "h3", slot: "title", text: "Golu the elephant" },
  { tag: "p", slot: null, text: "Crocheted in Indigo." },
  { tag: "p", slot: null, text: "About 15 cm tall." },
];

const slots = { title: [], default: [], price: [] };
for (const child of children) {
  slots[child.slot ?? "default"].push(child.text);
}

for (const [name, texts] of Object.entries(slots)) {
  console.log(`${name}: ${texts.length > 0 ? texts.join(" | ") : "(fallback)"}`);
}
```

It prints the title, then both paragraphs in the default slot, then `price: (fallback)`.

## Recap

- **Shadow DOM** gives an element its own hidden tree. Styles inside stay in, and the page's styles stay out.
- Attach it once, in the constructor: `this.attachShadow({ mode: "open" })`. Reach it later with `element.shadowRoot`.
- `:host` styles the custom element itself; `:host([sale])` styles it only with that attribute.
- **Slots** show the page's children inside the shadow markup: `slot="name"` picks a named slot, and the rest go to the default slot.
- Text inside a `<slot>` is **fallback content**. Slotted children stay in the light DOM.

## Common mistakes

**1. Attaching the shadow root in connectedCallback.**

```js
class DhaagaCard extends HTMLElement {
  connectedCallback() {
    this.attachShadow({ mode: "open" }).innerHTML = CARD_HTML;
  }
}
```

The first time works. Move the card to another list, and connectedCallback runs again: `NotSupportedError: Failed to execute 'attachShadow' on 'Element': Shadow root cannot be created on a host which already hosts a shadow tree.` Attach it in the constructor.

**2. Looking for shadow content with document.querySelector.**

```js
document.querySelector("dhaaga-card article");
```

It gives `null`. The article lives in the card's shadow root, which the page's searches can't see. Search inside it: `card.shadowRoot.querySelector("article")`.

**3. Misspelling a slot name.**

```html
<h3 slot="tittle">Golu the elephant</h3>
```

The heading disappears. No slot is called `tittle`, and a child with an unknown slot name isn't shown at all. Match the names exactly.

**4. Forgetting display: block on :host.**

```css
:host { border: 2px solid #2E3A8C; }
```

The border doesn't draw one neat box around the card. It breaks into thin pieces around the lines of content. Custom elements are inline by default. Add `display: block;`.

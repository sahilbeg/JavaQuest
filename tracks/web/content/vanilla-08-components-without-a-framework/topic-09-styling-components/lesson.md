## Hook

In a block of flats, two neighbours share one wall. One of them paints her side yellow. The paint soaks through, and the other flat now has a yellow stain. Neither of them meant any harm. The wall was simply shared.

CSS is one big shared wall. Every rule can reach every element on the page. This ticket gives each component its own wall: names nobody else uses, colours kept in one place, and a few doors that are left open on purpose.

## Concept

### Why styles clash

Sana's rule was for the cart's savings line:

```css
.price { color: #b00020; }
```

But the product cards also used `.price`, so every price in the catalogue turned red. Short, common names like `.price`, `.title` or `h3` are used by many parts of the page. A rule written for one part hits them all.

Shadow DOM, from earlier in this module, is one fix. But not every component should have a shadow root: light DOM forms, for one, work better without. Those need another fix: **names that can't clash**.

### Syntax: BEM names

```css
.block {}
.block__element {}
.block--modifier {}
```

- The **block** is the component: `.product-card`.
- An **element** is a part of it, joined with two underscores: `.product-card__name`, `.product-card__price`.
- A **modifier** is a version of it, joined with two dashes: `.product-card--sold-out`.

Every class starts with the block's name, so `.product-card__price` can never be hit by a rule for the cart's `.cart-line__price`.

```callout jargon
**BEM** (Block, Element, Modifier) is a naming habit for CSS classes. Each class names its component first, so styles for one component can't leak into another.
```

### Flat, one-class selectors

With BEM, almost every selector is a single class:

```css
.product-card__price { font-weight: 700; }
.product-card--sold-out { opacity: 0.5; }
```

- No `h3`, no `.product-card p span`. Element names and long chains break when the HTML changes, and they reach things you didn't mean.
- One class each means every rule has the same **specificity**, so the rule written later wins. No `!important` wars.

```callout jargon
**Specificity** is how the browser decides which of two matching rules wins: ids beat classes, and classes beat element names. Keeping every selector to one class keeps it simple.
```

### Syntax: design tokens

```css
:root {
  --dhaaga-marigold: #F2A81D;
  --dhaaga-indigo: #2E3A8C;
}
.product-card__name { color: var(--dhaaga-indigo); }
```

- Brand colours live once, on `:root`, as CSS custom properties.
- Every component uses `var(--dhaaga-indigo)` instead of typing `#2E3A8C` again.
- When Sana tweaks the Indigo, one line changes, and every component follows.

```callout jargon
A **design token** is one named design decision, like a brand colour or a spacing size, stored in one place and used everywhere.
```

### Custom properties reach into shadow roots

Page rules can't pick elements inside a shadow root. But inherited values, like `color`, still flow in from the host. **Custom properties inherit too**, straight through the shadow boundary. That makes them the official door for theming a component.

```css
:host { background: var(--sale-badge-bg, #F2A81D); }
```

- Inside the badge, the background reads `--sale-badge-bg`.
- If the page set it, that colour is used. If not, the fallback after the comma, Marigold, is used.

```animate
title: A page recolours a badge it can't reach into
kind: scene
nodes:
  - { id: page, label: "Gift guide CSS", emoji: "🎨", value: "--sale-badge-bg: Mint", row: 0, col: 0 }
  - { id: badge, label: "<sale-badge>", emoji: "🏷️", row: 0, col: 2 }
  - { id: shadow, label: "badge's shadow style", emoji: "🔒", value: "var(--sale-badge-bg, Marigold)", row: 1, col: 2 }
  - { id: screen, label: On screen, emoji: "🖼️", value: Marigold, row: 0, col: 4 }
steps:
  - say: Without any page CSS, the badge's shadow style uses its fallback, Marigold.
    focus: [shadow, screen]
  - say: "The gift guide tries span { background: green; }. It can't reach the span inside the shadow root. Nothing changes."
    send: { from: page, to: badge, label: "span { ... }" }
  - say: Then it sets --sale-badge-bg on the badge. Custom properties inherit, like color, straight through the boundary.
    send: { from: page, to: badge, label: "--sale-badge-bg: Mint" }
  - say: Inside, var(--sale-badge-bg) now finds Mint, and uses it instead of the fallback.
    send: { from: badge, to: shadow, label: inherits }
  - say: The badge turns Mint. The page changed only what the component chose to expose.
    send: { from: shadow, to: screen, label: render }
    set: { screen: Mint }
```

### Syntax: ::part

```css
sale-badge::part(label) { text-transform: uppercase; }
```

- Inside the shadow root, an element can be marked `part="label"`.
- From outside, `sale-badge::part(label)` styles that one element, and nothing else inside.
- Custom properties let the page change **values** the component uses. Parts let it style **whole elements** the component chose to show.

### :host and the hidden attribute

A shadow style like `:host { display: inline-block; }` beats the browser's own rule that hides elements with the `hidden` attribute. So `badge.hidden = true` stops working: the badge stays on screen.

Add one more rule to every component that sets `display` on `:host`:

```css
:host([hidden]) { display: none; }
```

## Worked example

Each line in the cart drawer is a `cart-line` block, in the light DOM:

```html
<li class="cart-line cart-line--gift">
  <span class="cart-line__name">Golu the elephant</span>
  <span class="cart-line__qty">2</span>
  <span class="cart-line__price">₹900.00</span>
</li>
```

```css
:root {
  --dhaaga-marigold: #F2A81D;
  --dhaaga-indigo: #2E3A8C;
}

.cart-line { display: flex; gap: 0.5rem; color: var(--dhaaga-indigo); }
.cart-line__name { flex: 1; }
.cart-line__price { font-weight: 700; }
.cart-line--gift { border-left: 4px solid var(--dhaaga-marigold); }
```

```js
function cartLineClass(line) {
  return line.giftWrap ? "cart-line cart-line--gift" : "cart-line";
}
```

1. The block is `cart-line`. Its parts are `cart-line__name`, `__qty` and `__price`.
2. A gift-wrapped line gets the modifier `cart-line--gift` as well as the block class.
3. Colours come from the tokens on `:root`, never typed again.
4. The card's `.product-card__price` and the line's `.cart-line__price` can never hit each other.

## Try it

BEM names follow a pattern, so code can build them. Run this, then add a `sale` modifier that is `true`.

```js run
function bem(block, element, modifiers = {}) {
  const base = element ? `${block}__${element}` : block;
  const extra = Object.entries(modifiers)
    .filter(([, on]) => on)
    .map(([name]) => `${base}--${name}`);
  return [base, ...extra].join(" ");
}

console.log(bem("product-card"));
console.log(bem("product-card", "price"));
console.log(bem("product-card", null, { "sold-out": true, sale: false }));
```

It prints `product-card`, then `product-card__price`, then `product-card product-card--sold-out`. The `sale` modifier is `false`, so it's left out.

## Recap

- CSS is shared by the whole page, so short names like `.price` or `h3` clash between components.
- **BEM**: `.block`, `.block__element`, `.block--modifier`. One class per selector keeps **specificity** flat.
- Keep brand colours as **design tokens** on `:root`, and use them with `var(...)`.
- Custom properties inherit **into** shadow roots: `var(--name, fallback)` is the door for theming.
- `::part(name)` styles an element the component marked with `part`. Add `:host([hidden]) { display: none; }` when `:host` sets `display`.

## Common mistakes

**1. Styling by element name.**

```css
.product-card h3 { color: var(--dhaaga-indigo); }
```

It works until the name becomes an `h2`, or until a blog's `h3` sits inside the card. Give the part its own class: `.product-card__name`.

**2. Forgetting the block class with a modifier.**

```html
<article class="product-card--sold-out">...</article>
```

The card loses all its normal styles, because `.product-card` no longer matches. A modifier is added **on top of** the block: `class="product-card product-card--sold-out"`.

**3. Typing the colour again instead of using the token.**

```css
.product-card__add { background: #F2A81D; }
```

When Sana adjusts Marigold, this button keeps the old shade. Use `var(--dhaaga-marigold)`.

**4. A var with no fallback inside a shadow root.**

```css
:host { background: var(--sale-badge-bg); }
```

On a page that never sets `--sale-badge-bg`, the badge has no background at all. Give a fallback: `var(--sale-badge-bg, #F2A81D)`.

**5. Hiding a component that sets display.**

```css
:host { display: inline-block; }
```

`badge.hidden = true` does nothing, because `:host` overrides the browser's hiding rule. Add `:host([hidden]) { display: none; }`.

## Hook

Picture five friends sitting on a bench. If the bench is long, they spread out. If a sixth friend arrives, everyone shuffles closer. If the bench is too short, someone moves to the bench behind. Nobody needs exact seat numbers: they flex to fit.

Right now the catalogue's cards are stacked like books in a pile: one tall column. On a laptop, that wastes most of the screen. **Flexbox** lets the cards sit side by side like friends on a bench, sharing the space and moving to a new row when they run out of room.

## Concept

### Flex containers and flex items

```css
.products {
  display: flex;
}
```

`display: flex` turns an element into a **flex container**. Its direct children become **flex items**, and they line up in a row.

```callout jargon
A **flex container** is the parent with `display: flex`. Its **flex items** are its direct children only. Grandchildren are not flex items, unless their own parent is a flex container too.
```

So `display: flex` on `.products` lines up the `<li>` cards. The `<h3>` and `<p>` inside each card are not affected.

### The two axes

Flexbox thinks in two directions.

- The **main axis** is the direction items flow in. By default, it runs left to right, across the row.
- The **cross axis** runs the other way, top to bottom.

```css
flex-direction: row;      /* main axis left to right (the default) */
flex-direction: column;   /* main axis top to bottom */
```

`flex-direction: column` turns the main axis on its side. The items stack from top to bottom, and the cross axis now runs left to right.

### Syntax: justify-content

```css
justify-content: space-between;
```

`justify-content` spreads the items along the **main** axis.

| Value | What happens |
|---|---|
| `flex-start` | items bunch at the start (the default) |
| `center` | items bunch in the middle |
| `flex-end` | items bunch at the end |
| `space-between` | first item at the start, last at the end, equal gaps between |

```animate
title: Spreading the header
kind: scene
nodes:
  - { id: header, label: ".site-header", emoji: "🧵", value: "display: block" }
  - { id: brand, label: "Brand", emoji: "🐘", row: 1, col: 0, value: "own line" }
  - { id: nav, label: "Nav", emoji: "🧭", row: 2, col: 0, value: "own line" }
steps:
  - say: The header holds two blocks, the brand and the nav. As blocks, they stack, one line each.
    focus: [header, brand, nav]
  - say: display flex makes the header a flex container. Its two children line up in one row, both at the left.
    set: { header: "display: flex", brand: "left", nav: "next to brand" }
  - say: justify-content space-between pushes the first child to the start and the last to the end.
    set: { header: "space-between", nav: "far right" }
    focus: [brand, nav]
  - say: The brand is taller than the nav, so the nav sits at the top. align-items center lines them up through the middle.
    set: { header: "centred across" }
    focus: [nav]
```

### Syntax: align-items

```css
align-items: center;
```

`align-items` lines items up along the **cross** axis. In a row, that means up and down.

- `stretch` (the default) makes every item as tall as the tallest one.
- `flex-start`, `center` and `flex-end` line them up at the top, middle or bottom.

```callout tip
To remember which is which: **justify** works along the main axis, the way the items flow. **align** works across it.
```

### Syntax: gap

```css
gap: 1rem;
```

`gap` puts space **between** flex items, but not around the outside. It's cleaner than giving every item a margin, because the first and last items don't get an extra gap at the edges.

### Syntax: flex-wrap

```css
flex-wrap: wrap;
```

By default, flex items stay on one line and squeeze to fit, however small. Five cards on a phone would each be paper-thin.

`flex-wrap: wrap` lets items move to a new line when there isn't room. Cards that would get too thin jump down to the next row.

### Syntax: the flex shorthand

```css
flex: 1 1 14rem;
```

The `flex` property goes on the **items**. It holds three values:

1. **grow:** may the item get bigger to fill spare space? `1` means yes, `0` means no.
2. **shrink:** may it get smaller when space is short? `1` means yes.
3. **basis:** its starting size before growing or shrinking, here `14rem`.

So `flex: 1 1 14rem` means "start at 14rem, then grow or shrink to fill the row fairly". With `flex-wrap: wrap`, a card won't shrink much below 14rem. It wraps instead.

Watch how growing shares out the spare space on a row:

```animate
title: Sharing the spare space
kind: code
language: js
code: |
  const row = 600;
  const basis = 160;
  const cards = 3;
  const spare = row - basis * cards;
  const share = spare / cards;
  console.log("each card: " + (basis + share) + "px");
steps:
  - line: 1
    vars: { row: 600 }
    say: The row is 600px wide.
  - line: 2
    vars: { basis: 160 }
    say: Each card starts at its basis of 160px.
  - line: 3
    vars: { cards: 3 }
    say: Three cards fit on this row.
  - line: 4
    vars: { spare: 120 }
    say: They use 480px, so 120px is spare.
  - line: 5
    vars: { share: 40 }
    say: Every card has grow set to 1, so they split the spare space equally, 40px each.
  - line: 6
    out: "each card: 200px"
    say: Each card ends up 200px wide, and the row is filled edge to edge with no gap left over.
```

### Pushing one item away with auto margins

```css
.buy {
  margin-top: auto;
}
```

In a flex container, `margin-top: auto` on an item soaks up all the spare space above it. In a column, that pushes the item to the bottom.

Cards in a row stretch to the height of the tallest card. If each card is also a column flex container, `margin-top: auto` sends every Order button to the bottom of its card. The buttons line up, even when names wrap to two lines.

## Worked example

Here is the flexbox CSS for the header and the cards:

```css
.site-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

nav {
  display: flex;
  gap: 1rem;
}

.products {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.product {
  flex: 1 1 14rem;
  display: flex;
  flex-direction: column;
}

.buy {
  margin-top: auto;
  align-self: flex-start;
}
```

1. The header is a flex row: the brand goes left, the nav goes right, and both sit through the middle.
2. The nav is a flex row too, with 1rem between its two links.
3. The products list is a wrapping flex row. Cards start at 14rem, grow to fill each row, and wrap when they run out of room.
4. Each card is also a column flex container. A box can be a flex item and a flex container at the same time.
5. `margin-top: auto` pushes each button to the bottom of its card. `align-self: flex-start` stops the button stretching across the whole card width, which column items do by default.

```callout jargon
`align-self` is `align-items` for one item. It overrides the container's setting for just that item.
```

## Try it

Paste this at the end of `style.css`, and widen and narrow the preview:

```css
.products {
  justify-content: center;
}

.product {
  flex: 0 0 12rem;
}
```

Now the cards don't grow or shrink: every one is exactly 12rem, and each row is centred. Try `justify-content: space-between` instead. Then change the flex to `1 1 12rem` and see the cards stretch to fill each row again.

## Recap

- `display: flex` makes a flex container, and its direct children line up as flex items.
- The main axis follows `flex-direction`. `justify-content` works along it, and `align-items` works across it.
- `gap` adds space between items, and `flex-wrap: wrap` lets them move to a new line.
- `flex: 1 1 14rem` means grow, shrink, and start at 14rem.
- `margin-top: auto` in a column pushes an item to the bottom.

## Common mistakes

**1. Putting display: flex on the items instead of the parent.**

```css
.product {
  display: flex;
}
```

The cards still stack in a column. Their children line up in rows inside each card instead. Put `display: flex` on the parent, `.products`, to line up the cards.

**2. Forgetting flex-wrap.**

```css
.products {
  display: flex;
}
```

On a phone, all five cards squeeze into one row, each a few words wide. Flex items stay on one line by default. Add `flex-wrap: wrap;`.

**3. Mixing up justify-content and align-items.**

```css
.site-header {
  display: flex;
  align-items: space-between;
}
```

Nothing spreads out. `space-between` is not a valid value for `align-items`, so the line is dropped. Spreading along the row is `justify-content`'s job.

**4. Margins on every card instead of gap.**

```css
.product {
  margin-right: 1rem;
}
```

The last card in each row also gets 1rem on its right, so the row looks off-centre. Use `gap: 1rem` on the container. It only adds space between items.

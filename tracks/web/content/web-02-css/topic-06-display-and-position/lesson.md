## Hook

Think of a notice board at school. Most notices are pinned in rows, one under the other. But someone sticks a bright "NEW!" star on the corner of one notice. And the school's name is painted at the top of the board, where it never moves.

Web pages work the same way. Most elements sit in rows, in the order the HTML lists them. This ticket teaches the two properties that break those rows when you need to: `display` changes how a box sits in the line, and `position` lets you pin a box somewhere exact.

## Concept

### Normal flow

Without any layout CSS, the browser places elements in **normal flow**: one after another, in the order the HTML lists them.

```callout jargon
**Normal flow** is the browser's default way of laying out a page. It's like writing in a notebook: top to bottom, and left to right within a line.
```

How an element sits in that flow depends on its `display` value. Every element has one by default.

### Syntax: display block and inline

```css
display: block;
display: inline;
```

- A **block** element starts on a new line and stretches across the full width. `<p>`, `<h1>`, `<li>`, `<section>` and `<div>` are block by default.
- An **inline** element sits inside a line of text, only as wide as its content. `<a>`, `<span>` and `<strong>` are inline by default.

An inline element ignores `width` and `height`. Its top and bottom padding get painted, but they don't push other lines away. That's why an Order link with padding looks squashed, its background spilling over the text above.

```animate
title: Block and inline in the flow
kind: scene
nodes:
  - { id: h3, label: "<h3> block", emoji: "🟦", value: "own line", row: 0, col: 0 }
  - { id: p, label: "<p> block", emoji: "🟦", value: "own line", row: 1, col: 0 }
  - { id: span, label: "<span> inline", emoji: "🟨", value: "in the line", row: 2, col: 0 }
  - { id: a, label: "<a> inline", emoji: "🟨", value: "in the line", row: 2, col: 1 }
steps:
  - say: The h3 is a block. It takes a whole line, from edge to edge.
    focus: [h3]
  - say: The p is a block too, so it starts a new line below the h3.
    focus: [p]
  - say: The span is inline. It sits in a line, only as wide as its words.
    focus: [span]
  - say: The a is inline as well, so it sits right next to the span on the same line.
    focus: [a]
  - say: Give the a some padding. It gets wider, but its top and bottom padding don't push the lines apart.
    set: { a: "squashed" }
    focus: [a]
  - say: Switch the a to inline-block. It stays in the line, but now its padding and height count.
    set: { a: "real button" }
```

### Syntax: inline-block

```css
.buy {
  display: inline-block;
  padding: 0.5rem 1rem;
}
```

`inline-block` mixes the two. The element still sits in the line, like inline. But it keeps its own width, height and padding, like a block. This is how a link becomes a proper button.

### Syntax: display none

```css
.sold-out .buy {
  display: none;
}
```

`display: none` removes the element from the page completely. It takes no space, and screen readers skip it too. Use it for things that shouldn't be there at all, like an Order button for something sold out.

### Syntax: position relative

```css
.product {
  position: relative;
}
```

Every element starts with `position: static`, which means "stay in normal flow". `position: relative` keeps the element in its normal spot. It does two things:

1. You can nudge it with `top`, `left`, `right` and `bottom`, and the space it left stays empty.
2. It becomes an **anchor** for any `absolute` element inside it.

The second job is the one you'll use most.

### Syntax: position absolute

```css
.badge {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
}
```

`position: absolute` lifts the element out of normal flow. Other elements act as if it isn't there. Then `top`, `right`, `bottom` and `left` place it, measured from the edges of its **containing block**.

```callout jargon
The **containing block** of an absolute element is its nearest ancestor with a `position` other than `static`. If there isn't one, it's the whole page.
```

`top: 0.5rem; right: 0.5rem` means "0.5rem from the top edge and 0.5rem from the right edge". Watch the badge look for its anchor:

```animate
title: Where does the New badge go?
kind: scene
nodes:
  - { id: page, label: The page, emoji: "📄", value: static }
  - { id: card, label: "Golu's card", emoji: "🐘", value: static, row: 1, col: 0 }
  - { id: badge, label: "New badge", emoji: "⭐", value: "absolute", row: 2, col: 0 }
links:
  - { from: page, to: card }
  - { from: card, to: badge }
steps:
  - say: The badge is absolute, with top and right set. It looks up for its nearest positioned ancestor.
    focus: [badge]
  - say: Golu's card is static, the default. That doesn't count, so the badge keeps looking.
    focus: [card]
  - say: No positioned ancestor at all, so the page is the anchor. The badge flies to the top-right corner of the whole page.
    focus: [page]
    set: { badge: "page corner" }
  - say: Now give the card position relative. Nothing about the card moves.
    set: { card: relative }
    focus: [card]
  - say: The badge looks up again, and finds the card first. It pins itself to the card's top-right corner.
    set: { badge: "card corner" }
    focus: [card, badge]
```

### Syntax: position sticky

```css
.site-header {
  position: sticky;
  top: 0;
}
```

A **sticky** element stays in normal flow until you scroll it to the edge given by `top`. Then it sticks there while the rest of the page slides under it. `top: 0` means "stick at the very top of the screen".

Without `top`, sticky does nothing, because the browser doesn't know where to stick it.

### Syntax: position fixed

```css
.whatsapp-button {
  position: fixed;
  bottom: 1rem;
  right: 1rem;
}
```

`position: fixed` pins an element to the screen itself. It stays in the same spot however far you scroll, like a floating "Chat on WhatsApp" button. Unlike sticky, it's out of normal flow from the start.

### Which goes on top?

When positioned elements overlap, the one later in the HTML is drawn on top. `z-index: 10` lifts an element above others with a smaller number. You'll need it when a sticky header and an absolute badge meet as you scroll.

```callout jargon
**z-index** is a stacking number. Think of the screen as a pile of sheets: a higher z-index means a sheet nearer the top.
```

## Worked example

Here is the CSS for the badge, the buttons and the header:

```css
.site-header {
  position: sticky;
  top: 0;
  z-index: 10;
}

.product {
  position: relative;
}

.badge {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
}

.buy {
  display: inline-block;
  padding: 0.5rem 1rem;
}

.sold-out .buy {
  display: none;
}
```

1. The header stays in its place until you scroll, then sticks at the top. `z-index: 10` keeps it above the cards as they slide under.
2. Every product card becomes an anchor. It doesn't move at all.
3. The badge leaves the flow and pins 0.5rem from the top-right corner of Golu's card.
4. Each Order link becomes inline-block, so its padding makes a real button.
5. On the sold-out booties card, the button is removed completely.

## Try it

Paste this at the end of `style.css`, then scroll the preview:

```css
.badge {
  position: absolute;
  top: 0;
  left: 0;
}
```

If `.product` has `position: relative`, the badge sits in the top-left corner of Golu's card. Now delete `position: relative` from `.product`. Where does the badge go? Put it back, then try `top: -0.5rem` to make the badge hang over the card's edge.

## Recap

- Block elements take a whole line. Inline elements sit in a line and ignore width and height.
- `inline-block` sits in a line but keeps its own size and padding: good for buttons.
- `display: none` removes an element completely, from the screen and from screen readers.
- `position: absolute` places an element against its nearest positioned ancestor, so give the parent `position: relative`.
- `position: sticky` with `top: 0` keeps an element at the top once you scroll to it. `fixed` pins it to the screen.

## Common mistakes

**1. An absolute badge with no positioned parent.**

```css
.badge {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
}
```

The badge appears at the top-right of the whole page, not on Golu's card. Nothing above it is positioned, so the page is its anchor. Add `.product { position: relative; }`.

**2. Sticky with no top.**

```css
.site-header {
  position: sticky;
}
```

The header scrolls away like normal. Sticky needs to know where to stick. Add `top: 0;`.

**3. Padding on an inline link.**

```css
.buy {
  padding: 0.5rem 1rem;
}
```

The Marigold background spills over the price above, and the button doesn't push anything away. An `<a>` is inline. Add `display: inline-block;`.

**4. Using visibility to remove something.**

```css
.sold-out .buy {
  visibility: hidden;
}
```

The button is invisible, but it still takes up space, leaving an odd gap. `visibility: hidden` only hides the paint. Use `display: none;` to remove the box completely.

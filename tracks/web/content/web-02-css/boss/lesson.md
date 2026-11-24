## What happened

Last week, the catalogue got product photos and a new layout. It was built and checked on a laptop, where it looked lovely: four neat columns, big photos. Nobody opened it on a phone.

Most of Dhaaga's customers are on Android phones about 360px wide. On those phones, three things go wrong at once:

1. `main` has `width: 1200px`. A 1200px box can't fit on a 360px screen, so the page runs off the right edge.
2. The product grid always has four columns. On a phone, each column is a sliver a few words wide.
3. Each photo is 600px wide, and nothing tells it to shrink. Photos burst out of their cards.

Then someone added `overflow-x: hidden` to `body` as a quick fix. That stopped the sideways scrolling, but it didn't make anything fit. It just cut off everything past the screen's edge, including the Order buttons.

```callout jargon
**Overflow** is content that doesn't fit inside its box and spills out. `overflow-x: hidden` hides whatever spills out sideways, so you can't scroll to it or see it.
```

```animate
title: A 1200px page on a 360px phone
kind: scene
nodes:
  - { id: phone, label: "Phone, 360px", emoji: "📱", value: "fits?" }
  - { id: main, label: main, emoji: "📦", value: "width: 1200px", row: 1, col: 0 }
  - { id: buy, label: Order button, emoji: "🟧", value: "at 1,100px", row: 1, col: 2 }
steps:
  - say: The phone's screen is 360px wide. The browser starts laying out main.
    focus: [phone, main]
  - say: main insists on 1200px. It can't shrink, so most of it hangs off the right edge.
    set: { phone: "scrolls sideways" }
    focus: [main]
  - say: The Order button on the last card sits far to the right, around 1,100px in. Customers would have to scroll sideways to find it.
    focus: [buy]
  - say: The quick fix, overflow-x hidden on body, cuts off everything past 360px. No more sideways scroll, but the button is simply gone.
    set: { phone: "cut off", buy: hidden }
  - say: The real fix is to let main shrink. With max-width 1200px instead, main is 360px wide on the phone and the cards wrap inside it.
    set: { main: "max-width: 1200px", phone: fits, buy: visible }
```

## What you need

Everything here builds on this project: the box model, grid and phone-first media queries.

### Syntax: max-width

```css
main {
  max-width: 1200px;
  margin: 0 auto;
}
```

`width: 1200px` says "always exactly 1200px". `max-width: 1200px` says "as wide as the space allows, but **never more** than 1200px".

On a laptop, `main` stops at 1200px, and `margin: 0 auto` centres it. On a phone, it's simply as wide as the screen.

```callout tip
Big boxes should almost never have a fixed `width` in px. Let them fill the space, and cap them with `max-width`.
```

### Syntax: images that shrink

```css
.product img {
  max-width: 100%;
  height: auto;
}
```

An image is drawn at its own size unless CSS says otherwise. A 600px photo stays 600px, even in a 300px card.

`max-width: 100%` means "never wider than the box you're in". `height: auto` keeps the photo's shape as it shrinks, so Golu doesn't look squashed.

### A phone-first grid

The grid needs the same phone-first idea as the header and the footer: no columns for phones, more columns as the screen grows.

```css
.products {
  display: grid;
  gap: 1.5rem;
}

@media (min-width: 40rem) {
  .products {
    grid-template-columns: repeat(2, 1fr);
  }
}
```

With no `grid-template-columns`, a grid has one column, and every card gets the phone's full width. Inside the media query, wider screens get two columns. You can add a second, wider query for four.

### Why hiding the overflow isn't a fix

`overflow-x: hidden` on `body` hides the symptom. The page is still 1200px wide inside; you just can't see past 360px. When something spills out, find the box that is too wide and let it shrink.

```callout tip
In DevTools, the device toolbar shows the page at 360px. Content that sticks out past the right edge points straight at the box that is too wide.
```

## The tiers

1. **Bronze** resolves the incident: `main` has no fixed width in px, and gets `max-width: 1200px` instead. No rule outside a media query sets a `width` wider than a phone.
2. **Silver:** the grid is phone first. The base `.products` rule gives no columns (or just `1fr`), a `min-width` media query adds more, and there are no `max-width` media queries.
3. **Gold:** every photo fits its card, with `max-width: 100%` and `height: auto` on `.product img`. And `body` no longer hides its overflow.

## Recap

- A fixed `width` in px on a big box breaks small screens. Use `max-width` to cap it instead.
- Phone first: the base grid has one column, and `min-width` queries add more.
- `max-width: 100%` and `height: auto` stop images from bursting out of their boxes.
- `overflow-x: hidden` hides a layout bug. Find the box that is too wide and let it shrink.

## Common mistakes

**1. Shrinking the width instead of removing it.**

```css
main {
  width: 360px;
}
```

It fits one phone, but laptops now get a narrow strip, and a 320px phone still overflows. Use `max-width: 1200px`, which works on every screen.

**2. Using max-width media queries to undo the desktop grid.**

```css
.products {
  grid-template-columns: repeat(4, 1fr);
}

@media (max-width: 40rem) {
  .products {
    grid-template-columns: 1fr;
  }
}
```

It works, but it is desktop first: phones load four columns and then undo them. The silver tier wants phone first: no columns in the base rule, and `min-width` queries that add them.

**3. Giving images a fixed width.**

```css
.product img {
  width: 300px;
}
```

That fits a laptop card, but a 280px card on a small phone still overflows. `max-width: 100%` follows whatever width the card has.

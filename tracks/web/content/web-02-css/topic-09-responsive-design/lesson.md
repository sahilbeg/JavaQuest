## Hook

Think of a rangoli drawn at a doorstep. On a small step, it's one neat circle. Outside a big house, the same pattern spreads out, with more petals and a wider border. It's still the same rangoli. It just fits the space it's given.

Dhaaga's customers mostly arrive from a WhatsApp link, on a phone. Some open the same page on a laptop at work. One page has to look right on a screen 360 pixels wide and on one 1,400 pixels wide. Making a page fit every screen like this is called **responsive design**.

## Concept

### Screens come in every size

```callout jargon
**Responsive design** means one page whose layout adapts to the screen it's shown on. There's no separate "phone site": the same HTML and CSS respond to the screen size.
```

A cheap Android phone might be 360px wide. A tablet is around 768px, and a laptop 1,280px or more. A layout that needs 1,000px of width can't fit on a phone. It gets cut off, or the reader has to scroll sideways.

The answer is to let the layout change: one column on a phone, more columns when there's room.

### Syntax: the viewport meta tag

```html
<meta name="viewport" content="width=device-width, initial-scale=1">
```

This line goes in the `<head>`. It matters more than any CSS in this ticket.

Without it, phones assume the page was built for a laptop. They pretend to be about 980px wide, draw the page at that size, and then shrink everything to fit. The text becomes tiny, and your phone styles never apply.

`width=device-width` says "use the phone's real width". `initial-scale=1` says "don't zoom in or out to start".

```callout jargon
The **viewport** is the part of the page you can see on the screen, like the window of a train. On a phone, it's the phone's screen.
```

### Syntax: media queries

```css
@media (min-width: 48rem) {
  h1 {
    font-size: 2.5rem;
  }
}
```

A **media query** is a block of rules that only apply when a condition is true. `(min-width: 48rem)` means "when the viewport is at least 48rem wide". On a narrower screen, the browser skips every rule inside the block.

Rules inside a media query still follow the cascade. They come later in the file, so on a tie they win, but only while the condition is true.

```callout jargon
A **breakpoint** is the width where the layout changes, like 48rem here. Pick breakpoints where your design starts to look cramped or stretched, not for one particular phone model.
```

### min-width and max-width queries

```css
@media (min-width: 48rem) { /* 48rem wide or more */ }
@media (max-width: 48rem) { /* 48rem wide or less */ }
```

`min-width` means "this wide **or wider**". `max-width` means "this wide **or narrower**". Both work, but they lead to two different ways of writing CSS.

### Phone first

**Phone first** (often called **mobile first**) means:

1. Write the base styles, with no media query, for the smallest screen.
2. Add `min-width` media queries that change things as the screen gets wider.

Why start small? A phone layout is usually simpler: one column, things stacked. Wider screens **add** columns and space. Adding is easier than taking away. And if a media query ever fails, phones, where most customers are, still get the right layout.

Watch one stylesheet answer two different screens:

```animate
title: One stylesheet, two screens
kind: scene
nodes:
  - { id: css, label: style.css, emoji: "🎨", value: "base + @media (min-width: 48rem)" }
  - { id: phone, label: Phone, emoji: "📱", value: "360px", row: 1, col: 0 }
  - { id: laptop, label: Laptop, emoji: "💻", value: "1280px", row: 1, col: 2 }
steps:
  - say: Both screens get the same stylesheet. First, every screen applies the base rules, written for phones.
    send: [{ from: css, to: phone, label: "base rules" }, { from: css, to: laptop, label: "base rules" }]
  - say: The phone checks the media query. 48rem is 768px, and the phone is only 360px. The condition is false.
    focus: [phone]
  - say: So the phone skips the whole block. The header stays stacked and the shop timings stay in one column.
    set: { phone: "one column" }
  - say: The laptop checks too. 1280px is at least 768px, so the condition is true.
    focus: [laptop]
  - say: The laptop applies the rules inside the block. The header becomes a row and the timings sit side by side.
    send: { from: css, to: laptop, label: "media rules" }
    set: { laptop: "two columns" }
```

### Why rem in a media query

In a media query, `1rem` is always the browser's base text size, usually 16px. So `48rem` is 768px for most people.

If a reader has made their text bigger, `48rem` grows too. A laptop with very large text then gets the roomier phone layout, which is what that reader needs. A `768px` breakpoint wouldn't notice.

Here is the breakpoint maths as a program:

```animate
title: Which screens pass min-width 48rem?
kind: code
language: js
code: |
  const breakpoint = 48 * 16;
  const screens = [360, 768, 1280];
  for (const width of screens) {
    console.log(width + "px: " + (width >= breakpoint ? "wide" : "phone"));
  }
steps:
  - line: 1
    vars: { breakpoint: 768 }
    say: With the usual 16px base size, 48rem is 768px.
  - line: 2
    vars: { screens: "[360, 768, 1280]" }
    say: Three screens to test, a small phone, a tablet and a laptop.
  - line: 3
    vars: { width: 360 }
    say: First, the 360px phone.
  - line: 4
    out: "360px: phone"
    say: 360 is less than 768, so min-width is false. The phone keeps the base styles.
  - line: 3
    vars: { width: 768 }
    say: Next, a 768px tablet, exactly at the breakpoint.
  - line: 4
    out: "768px: wide"
    say: min-width means at least, so exactly 768 counts. The tablet gets the wide styles.
  - line: 3
    vars: { width: 1280 }
    say: Last, the 1280px laptop.
  - line: 4
    out: "1280px: wide"
    say: Well past the breakpoint, so the laptop gets the wide styles too.
```

### Trying sizes without a phone

Your browser's DevTools has a **device toolbar**. It shows the page at a phone's width, like 360px, without needing a phone. Drag its edge to watch the layout change at each breakpoint. This ticket's preview also gets narrower and wider when you resize it.

## Worked example

Here is the header and the footer, phone first:

```css
.site-header {
  display: flex;
  flex-direction: column;
}

h1 {
  font-size: 1.75rem;
}

.places {
  display: grid;
  gap: 1rem;
}

@media (min-width: 48rem) {
  .site-header {
    flex-direction: row;
    justify-content: space-between;
  }

  h1 {
    font-size: 2.5rem;
  }

  .places {
    grid-template-columns: 1fr 1fr;
  }
}
```

1. On a phone, the brand and the nav stack in a column, so neither gets squeezed.
2. The shop name is a little smaller on a phone, at 1.75rem.
3. The footer's two shops sit in a one-column grid, with 1rem between them.
4. From 48rem up, the header becomes a row with the nav on the right.
5. The shop name grows to 2.5rem, and the two shops sit side by side.

The base rules never mention a column count for `.places`. Phones get one column simply because nothing asked for more.

## Try it

Paste this at the end of `style.css`, then make the preview narrow and wide:

```css
@media (min-width: 60rem) {
  body {
    background-color: #f2a81d;
  }
}
```

The page turns Marigold once the preview is at least 60rem wide. Change `min-width` to `max-width`. Now it's Marigold only on narrow screens. Which one would you use to add a sidebar on laptops?

## Recap

- Responsive design means one page whose layout adapts to the screen size.
- The viewport meta tag makes phones use their real width. Without it, phone styles never kick in.
- `@media (min-width: 48rem) { ... }` applies its rules only on screens at least 48rem wide.
- Phone first: write the base styles for phones, then add `min-width` queries for wider screens.
- Use `rem` for breakpoints, and test narrow widths with the DevTools device toolbar.

## Common mistakes

**1. Forgetting the viewport meta tag.**

```html
<head>
  <meta charset="utf-8">
  <title>Catalogue | Dhaaga &amp; Co.</title>
</head>
```

On a phone, the whole page looks like a tiny laptop page, and your phone layout never appears. Add `<meta name="viewport" content="width=device-width, initial-scale=1">`.

**2. Desktop first with max-width.**

```css
.places {
  grid-template-columns: 1fr 1fr;
}

@media (max-width: 48rem) {
  .places {
    grid-template-columns: 1fr;
  }
}
```

This works, but phones load the laptop layout first and then undo it. Every new wide-screen rule needs a matching undo. Write the phone layout as the base and add a `min-width` query instead.

**3. No round brackets around the condition.**

```css
@media min-width: 48rem {
  h1 {
    font-size: 2.5rem;
  }
}
```

The browser can't read the condition, so the block never applies, even on a laptop. The condition needs round brackets: `@media (min-width: 48rem)`.

**4. Putting the media query before the base rule.**

```css
@media (min-width: 48rem) {
  h1 {
    font-size: 2.5rem;
  }
}

h1 {
  font-size: 1.75rem;
}
```

The laptop heading stays small. Both rules have the same specificity, so the later one wins, and here the later one is the base rule. Put media queries after the base rules they change.

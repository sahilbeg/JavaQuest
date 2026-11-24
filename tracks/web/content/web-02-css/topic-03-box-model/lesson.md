## Hook

Think of a framed photo on a wall. There's the photo itself. Around it is a white mount board, so the photo doesn't touch the frame. Then comes the frame. Then there's the empty wall between this frame and the next one.

Every element on a web page is built just like that. The catalogue's products are photos with no mount, no frame and no gap. That's why they run into each other. This ticket gives each one all four layers.

## Concept

### Every element is a box

The browser draws every element as a rectangle, even a round-looking button or a single word. Each box has four layers, from the inside out:

1. **Content:** the text or picture itself.
2. **Padding:** space inside the box, around the content.
3. **Border:** a line around the padding.
4. **Margin:** space outside the border, pushing other boxes away.

```callout jargon
The **box model** is this rule that every element is a box with content, padding, border and margin. DevTools draws it for any element you inspect.
```

```animate
title: Building one product card
kind: scene
nodes:
  - { id: content, label: Content, emoji: "🐘", value: "Golu, ₹450.00" }
  - { id: padding, label: Padding, emoji: "⬜", value: "0" }
  - { id: border, label: Border, emoji: "🖼️", value: none }
  - { id: margin, label: Margin, emoji: "↕️", value: "0" }
steps:
  - say: Start with the content, Golu's name and price. With nothing around it, the text touches the card's edge.
    focus: [content]
  - say: Padding adds space inside the box. Golu's text now has room to breathe.
    set: { padding: "1rem" }
    focus: [padding]
  - say: The border is drawn around the padding, like a frame around a mount board.
    set: { border: "2px Indigo" }
    focus: [border]
  - say: Margin is space outside the border. It pushes the next card, the coaster set, away.
    set: { margin: "1rem below" }
    focus: [margin]
  - say: Padding takes the box's background colour. Margin is always see-through, so the page shows through it.
    focus: [padding, margin]
```

### Syntax: padding

```css
padding: 1rem;
```

`padding` sets the space inside the box on all four sides. The box's background colour fills the padding too.

You can also set one side at a time: `padding-top`, `padding-right`, `padding-bottom` and `padding-left`.

### Syntax: shorthand with several values

```css
padding: 1rem 2rem;
```

A **shorthand** property sets several sides in one line. The number of values decides which sides get which:

| Written | Means |
|---|---|
| `padding: 1rem;` | all four sides 1rem |
| `padding: 1rem 2rem;` | top and bottom 1rem, left and right 2rem |
| `padding: 1rem 2rem 3rem 4rem;` | top, right, bottom, left |

```callout tip
For four values, think of a clock: start at 12 (top) and go clockwise through 3 (right), 6 (bottom) and 9 (left).
```

`margin` works the same way.

### Syntax: border

```css
border: 2px solid #2e3a8c;
```

`border` is a shorthand too. It takes three things: a width, a style and a colour.

- Width: how thick the line is, like `2px`.
- Style: `solid`, `dashed`, `dotted`, or `none`.
- Colour: any colour, like Indigo.

Without a style, there is no line at all, even with a width and a colour.

### Syntax: rounded corners

```css
border-radius: 8px;
```

`border-radius` rounds the box's corners. Bigger numbers make rounder corners. It works even when there's no border, by rounding the background.

### Syntax: margin

```css
margin-bottom: 1rem;
```

`margin` adds see-through space outside the border. It pushes the next box away. `margin-bottom: 1rem` puts a 1rem gap under each card.

```callout jargon
**See-through** here means margin has no colour of its own. Whatever is behind the box, usually the page, shows through.
```

### The browser's own spacing

Browsers add some spacing before your CSS runs:

- `body` gets about 8px of margin, so nothing touches the screen edge.
- A list, like `<ul>`, gets 40px of padding on the left, to make room for its bullets.
- Headings and paragraphs get margins above and below.

When you build your own layout, you often reset these to 0 first. `body { margin: 0; }` lets the header band run edge to edge.

### Syntax: box-sizing

```css
* {
  box-sizing: border-box;
}
```

Here's a surprise. By default, `width` sets only the **content**. Padding and border are added on top. A card with `width: 300px`, `padding: 16px` and a 2px border is really 336px wide.

`box-sizing: border-box` changes that. Now `width` includes the padding and border, and the content shrinks to fit. A 300px card stays 300px. Watch the sums:

```animate
title: Two ways to measure a box
kind: code
language: js
code: |
  const width = 300;
  const padding = 16;
  const border = 2;
  const contentBox = width + 2 * padding + 2 * border;
  const borderBox = width;
  console.log("content-box: " + contentBox + "px");
  console.log("border-box: " + borderBox + "px");
steps:
  - line: 1
    vars: { width: 300 }
    say: The card asks for width 300px.
  - line: 2
    vars: { padding: 16 }
    say: It has 16px of padding on every side.
  - line: 3
    vars: { border: 2 }
    say: And a 2px border all round.
  - line: 4
    vars: { contentBox: 336 }
    say: The default way adds padding and border on both sides. 300 + 32 + 4 = 336px. Wider than asked.
  - line: 5
    vars: { borderBox: 300 }
    say: With border-box, the 300px already includes them. The content just gets a little narrower.
  - line: 6
    out: "content-box: 336px"
    say: The default box spills 36px past what you asked for.
  - line: 7
    out: "border-box: 300px"
    say: The border-box card is exactly 300px. That's why most sites set border-box on every element.
```

```callout jargon
`*` is the **universal selector**. It picks every element on the page.
```

## Worked example

Here is the CSS that turns each product into a card:

```css
* {
  box-sizing: border-box;
}

body {
  margin: 0;
}

.products {
  list-style: none;
  padding: 0;
}

.product {
  padding: 1rem;
  border: 2px solid #2e3a8c;
  border-radius: 8px;
  margin-bottom: 1rem;
}
```

1. `*` gives every element `border-box`, so widths mean what they say.
2. `body { margin: 0; }` removes the browser's 8px gap around the page.
3. `.products` drops the bullets and the browser's 40px left padding. The cards now start at the left edge of `main`.
4. Each `.product` gets 1rem of room inside, a 2px Indigo frame, rounded corners, and a 1rem gap below it.

## Try it

Paste this at the end of `style.css` and watch the preview:

```css
.product {
  padding: 2rem 0.5rem;
  border: 4px dashed #f2a81d;
}
```

The cards get tall and narrow inside, with a dashed Marigold frame. Now change `dashed` to `none`. The frame disappears, even though the width and colour are still there.

Here are the box sums as a small program. Change `padding` to 24 and run it again:

```js run
const width = 300;
const padding = 16;
const border = 2;
console.log("content-box: " + (width + 2 * padding + 2 * border) + "px");
console.log("border-box: " + width + "px");
```

## Recap

- Every element is a box: content, then padding, then border, then margin.
- Padding is space inside and takes the background colour. Margin is see-through space outside.
- `border` needs a width, a style and a colour, like `2px solid #2e3a8c`.
- Shorthands take 1, 2 or 4 values. Four values go clockwise from the top.
- `box-sizing: border-box` makes `width` include padding and border. Reset the browser's spacing with `margin: 0` and `padding: 0`.

## Common mistakes

**1. A border with no style.**

```css
.product {
  border: 2px #2e3a8c;
}
```

No frame appears. The style is missing, and without `solid`, `dashed` or `dotted` there's nothing to draw. Write `border: 2px solid #2e3a8c;`.

**2. Using margin for space inside the card.**

```css
.product {
  border: 2px solid #2e3a8c;
  margin: 1rem;
}
```

The text still touches the frame. Margin is outside the border. For room between the text and the frame, use `padding`.

**3. Mixing up the order of four values.**

```css
.site-header {
  padding: 1rem 2rem 1rem 0;
}
```

The text touches the left edge, not the right. Four values go top, right, bottom, left. To keep 2rem on both sides, write `padding: 1rem 2rem;`.

**4. Forgetting the list's own padding.**

```css
.products {
  list-style: none;
}
```

The bullets go, but the cards still start 40px in from the left. The browser gave the list padding for the bullets. Add `padding: 0;`.

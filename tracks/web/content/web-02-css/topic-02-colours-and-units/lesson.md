## Hook

Ask a friend to buy "dark blue" wool. They might come back with navy, royal blue or almost black. All of them are dark blue. None of them is *your* blue.

A brand needs one exact blue, the same on a phone, a laptop, a printed bag and a shop sign. Colour names like `darkblue` can't promise that. A colour **code** can.

This ticket swaps the stand-in names for Dhaaga's exact colours. It also fixes sizes, so the shop name reads well on a small screen.

## Concept

### How a screen makes colour

Every dot on a screen is made of three tiny lights: red, green and blue. Each light can glow from 0 (off) to 255 (full). Mixing them makes every colour you see.

- All three at 255 make white.
- All three at 0 make black.
- Full red, some green and a little blue make an orange-yellow, like marigold.

CSS lets you write those three numbers in two ways: as **hex** or as **rgb()**.

```callout jargon
A **pixel** is one dot of colour on the screen. A phone screen has millions of them.
```

### Syntax: hex colours

```css
color: #2e3a8c;
```

A **hex colour** is a `#` followed by six characters, in three pairs: red, green, blue. Each pair is a number from `00` (off) to `ff` (full).

```callout jargon
**Hex**, short for hexadecimal, counts with 16 digits: 0 to 9, then a to f. So `ff` means 255, and `80` means 128.
```

Capital and small letters mean the same: `#2E3A8C` and `#2e3a8c` are the same Indigo. Watch Marigold split into its three lights:

```animate
title: Reading #F2A81D
kind: scene
nodes:
  - { id: hex, label: "#F2A81D", emoji: "🎨" }
  - { id: red, label: Red, emoji: "🔴", value: "?", row: 1, col: 0 }
  - { id: green, label: Green, emoji: "🟢", value: "?", row: 1, col: 1 }
  - { id: blue, label: Blue, emoji: "🔵", value: "?", row: 1, col: 2 }
  - { id: dot, label: One pixel, emoji: "🟡", value: "dark", row: 0, col: 3 }
steps:
  - say: "The browser reads Marigold's hex code in three pairs: F2, A8 and 1D."
    focus: [hex]
  - say: "F2 is the red pair. In hex that is 242, nearly full red."
    send: { from: hex, to: red, label: F2 }
    set: { red: "242" }
  - say: "A8 is the green pair: 168, a bit more than half."
    send: { from: hex, to: green, label: A8 }
    set: { green: "168" }
  - say: "1D is the blue pair: just 29, so blue is almost off."
    send: { from: hex, to: blue, label: 1D }
    set: { blue: "29" }
  - say: Lots of red, plenty of green, hardly any blue. Together they glow a warm marigold yellow.
    focus: [red, green, blue, dot]
    set: { dot: marigold }
```

### Syntax: rgb colours

```css
color: rgb(46, 58, 140);
```

`rgb()` writes the same three numbers in plain decimal, from 0 to 255. `rgb(46, 58, 140)` is exactly the same colour as `#2e3a8c`.

Use whichever your designer hands you. Designers usually share hex codes, so you'll see hex most.

### Named colours

```css
color: darkblue;
```

Browsers know about 140 colour names, like `white`, `black`, `gray` and `darkblue`. They're handy for quick tests. For a brand, use the exact codes instead.

### Syntax: background colours

```css
background-color: #faf7f0;
```

`color` paints the text. `background-color` paints the box behind it. An element can have both: Indigo text on an Off-white page, or Off-white text on an Indigo header.

### Contrast: can people read it?

Text must stand out from its background. This is called **contrast**. Light yellow text on a cream page is very hard to read, especially in bright sunlight at a stall.

```callout jargon
**Contrast** is how different two colours look in brightness. High contrast, like dark Indigo on Off-white, is easy to read.
```

That's why Marigold is used as a *background*, behind dark Indigo text. It is never the colour of small text on the Off-white page.

### Syntax: pixels

```css
border-width: 2px;
```

`px` means pixels. It's a fixed size that never changes with the reader's settings. It suits thin lines and small details, like a 2px border.

### Syntax: rem

```css
font-size: 2rem;
```

`rem` is a size compared to the reader's normal text size. Most browsers start at 16px, so `1rem` is 16px and `2rem` is 32px.

```callout jargon
**rem** stands for "root em". The root is the `<html>` element, and its font size is the base that every `rem` is measured from.
```

Why not just write `32px`? Some readers turn up the text size on their phone, like a grandparent who finds small text hard. `rem` grows with that setting. `px` does not:

```animate
title: rem follows the reader's setting
kind: scene
nodes:
  - { id: setting, label: Phone text size, emoji: "⚙️", value: "16px" }
  - { id: remh, label: "h1 at 2rem", emoji: "🅰️", value: "32px", row: 1, col: 0 }
  - { id: pxh, label: "h1 at 32px", emoji: "🔤", value: "32px", row: 1, col: 2 }
steps:
  - say: The phone starts at the usual 16px text size. 2rem is 2 times 16, so 32px. Both headings look the same.
    focus: [setting, remh, pxh]
  - say: Meera Aunty's friend turns up the text size to 20px, because small text is hard for her.
    set: { setting: "20px" }
    focus: [setting]
  - say: The rem heading follows. 2 times 20 is 40px, so it grows with everything else.
    set: { remh: "40px" }
    focus: [remh]
  - say: The px heading ignores her setting and stays at 32px. Use rem for text, so every reader gets the size they chose.
    focus: [pxh]
```

### Syntax: percentages

```css
width: 90%;
```

A **percentage** is a size compared to something else, usually the parent box. `width: 90%` means 90% as wide as the box it sits in. On a phone that is narrow. On a laptop it is wide.

### Which unit when

| Unit | Measured from | Good for |
|---|---|---|
| `px` | nothing, it's fixed | thin borders, small details |
| `rem` | the reader's base text size | font sizes and spacing |
| `%` | the parent box | widths that follow the screen |

## Worked example

Here is the catalogue in Dhaaga's colours:

```css
body {
  background-color: #faf7f0;
  color: #2e3a8c;
}

.site-header {
  background-color: #2e3a8c;
  color: #faf7f0;
}

.buy {
  background-color: #f2a81d;
  color: #2e3a8c;
}

h1 {
  font-size: 2rem;
}
```

1. The whole page gets the Off-white background and Indigo text. Every element inside `body` uses that text colour unless a rule says otherwise.
2. The header flips the colours: an Indigo band with Off-white text.
3. Each Order link becomes a Marigold patch with Indigo text. Dark on bright is easy to read.
4. The shop name is `2rem`: twice the reader's normal text size, whatever they chose.

## Try it

Paste this at the end of `style.css` in the ticket's editor:

```css
.tagline {
  font-size: 1.25rem;
  color: rgb(242, 168, 29);
}
```

The tagline turns Marigold on the Indigo header. Now try `color: rgb(255, 255, 255)` (white), then `rgb(0, 0, 0)` (black). Which one is hardest to read on Indigo?

Here is the rem maths as a tiny program. Press **Run**, then change `basePx` to 20 and run it again:

```js run
const basePx = 16;
const sizes = [0.875, 1, 1.25, 2];
for (const rem of sizes) {
  console.log(rem + "rem = " + rem * basePx + "px");
}
```

## Recap

- Screens mix red, green and blue light. A hex code like `#2e3a8c` gives the three amounts in pairs.
- `rgb(46, 58, 140)` is the same colour in decimal. Names like `darkblue` are fine for tests, not for a brand.
- `color` paints text, and `background-color` paints the box behind it. Keep the contrast high.
- `px` is fixed, `rem` follows the reader's text size, and `%` follows the parent box.
- Use `rem` for text sizes, so readers who need bigger text get it.

## Common mistakes

**1. Forgetting the # on a hex colour.**

```css
body {
  color: 2e3a8c;
}
```

The text stays black. Without `#`, the browser doesn't know it's a colour, so it drops the declaration. Write `#2e3a8c`.

**2. Using color when you meant the background.**

```css
.site-header {
  color: #2e3a8c;
}
```

The header text turns Indigo, but the band behind it stays Off-white. `color` is for text. Use `background-color` for the box.

**3. A space between the number and the unit.**

```css
h1 {
  font-size: 2 rem;
}
```

The heading stays its default size. The browser reads `2` and `rem` as two separate things, so the value is broken and dropped. Write `2rem`, with no space.

**4. Light text on a light background.**

```css
.price {
  color: #f2a81d;
}
```

There's no error, but Marigold prices on the Off-white page are very hard to read in sunlight. Keep text dark on light backgrounds, and use Marigold behind dark text.

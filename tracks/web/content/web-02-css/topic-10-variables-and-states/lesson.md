## Hook

A tailor writes your measurements on a card once: chest, waist, sleeve. Every kurta they stitch for you reads from that card. If you grow taller, they change one number, and every new kurta fits.

Right now the catalogue's CSS has no card. The Indigo code is typed again and again, in seven places. To make it a touch darker, you'd hunt for every copy and hope you didn't miss one. This ticket gives the stylesheet its measurement card: **custom properties**. Then it makes the buttons respond when people point at them or reach them with the keyboard.

## Concept

### Syntax: custom properties

```css
:root {
  --indigo: #2e3a8c;
}
```

A **custom property** is a value you name yourself. Its name starts with two dashes, like `--indigo` or `--card-gap`. You set it like any other property.

```callout jargon
Custom properties are often called **CSS variables**. A **variable** is a named box that holds a value, so you can use the name instead of repeating the value.
```

Names are case-sensitive: `--Indigo` and `--indigo` are two different properties. Stick to small letters and dashes.

### Syntax: the :root selector

```css
:root {
  --marigold: #f2a81d;
  --indigo: #2e3a8c;
  --off-white: #faf7f0;
}
```

`:root` picks the very top element of the page, `<html>`. Custom properties are inherited, like `color`. So a property set on `:root` reaches every element on the page.

That makes `:root` the natural home for the brand's colours: one card that every rule can read.

### Syntax: var()

```css
.buy {
  background-color: var(--marigold);
}
```

`var(--marigold)` reads the custom property and puts its value here. The browser looks for `--marigold` on the element itself, then on its parent, and so on up to `:root`.

Watch one change reach every rule:

```animate
title: One line changes the whole page
kind: scene
nodes:
  - { id: root, label: ":root", emoji: "🗂️", value: "--indigo: #2e3a8c" }
  - { id: header, label: Header band, emoji: "🧵", value: "#2e3a8c", row: 1, col: 0 }
  - { id: text, label: Body text, emoji: "📝", value: "#2e3a8c", row: 1, col: 1 }
  - { id: border, label: Card borders, emoji: "🖼️", value: "#2e3a8c", row: 1, col: 2 }
links:
  - { from: root, to: header }
  - { from: root, to: text }
  - { from: root, to: border }
steps:
  - say: Three rules use var(--indigo). None of them holds the hex code itself.
    focus: [header, text, border]
  - say: Each one looks up the tree for --indigo, and finds it on :root.
    send: [{ from: header, to: root, label: "--indigo?" }, { from: text, to: root, label: "--indigo?" }, { from: border, to: root, label: "--indigo?" }]
  - say: Meera Aunty wants a darker Indigo. You change one line, on :root.
    set: { root: "--indigo: #26307a" }
    focus: [root]
  - say: Every rule reads the new value. The header, the text and the borders all change together.
    set: { header: "#26307a", text: "#26307a", border: "#26307a" }
```

### Syntax: a fallback value

```css
color: var(--accent, #2e3a8c);
```

A second value after a comma is a **fallback**. If `--accent` isn't set anywhere, the browser uses `#2e3a8c` instead. Without a fallback, a missing property makes the declaration act as if it wasn't written.

### Changing a variable for one part of the page

```css
.product {
  --card-border: #2e3a8c;
  border: 2px solid var(--card-border);
}

.sold-out {
  --card-border: #5a5a5a;
}
```

A custom property can be set again on a smaller part of the page. The new value applies to that element and everything inside it. The rest of the page keeps the old one.

Here every card reads `--card-border`. The sold-out card sets its own `--card-border`, so only its frame turns grey. The `border` line is written once.

```callout tip
This is how themes work. A class on a big element, like `<body>`, can swap a whole set of colours at once, without touching a single component rule.
```

### Syntax: :hover

```css
.buy:hover {
  background-color: var(--indigo);
  color: var(--off-white);
}
```

`:hover` is a **pseudo-class**. It picks an element only while the mouse pointer is over it. Here the Marigold button turns Indigo when someone points at it. That tells them "yes, this is clickable".

```callout jargon
A **pseudo-class** is a word after a colon, like `:hover`, that picks elements by their state, not by their tag or class. It counts as a class in specificity.
```

Phones have no mouse pointer, so `:hover` mostly matters on laptops. Never hide something important behind hover alone.

### Syntax: :focus-visible

```css
.buy:focus-visible {
  outline: 3px solid var(--indigo);
  outline-offset: 2px;
}
```

Some people use the page with a keyboard, not a mouse. They press Tab to move from link to link. The element they've reached has **focus**.

`:focus-visible` picks an element when it has focus and the browser thinks a ring should show, which mostly means keyboard use. A clear ring tells keyboard users where they are.

```callout jargon
An **outline** is a line drawn around an element, outside its border. Unlike a border, it takes no space, so the layout doesn't shift when it appears. `outline-offset` moves it a little away from the edge.
```

```animate
title: Reaching the Order button by keyboard
kind: scene
nodes:
  - { id: user, label: Keyboard user, emoji: "⌨️" }
  - { id: nav, label: Catalogue link, emoji: "🔗", value: "", row: 1, col: 0 }
  - { id: buy, label: "Golu's Order button", emoji: "🟧", value: "", row: 1, col: 2 }
steps:
  - say: A customer uses the keyboard. She presses Tab to move through the links.
    focus: [user]
  - say: Focus lands on the Catalogue link first. :focus-visible gives it a ring, so she knows where she is.
    send: { from: user, to: nav, label: Tab }
    set: { nav: ring }
  - say: She presses Tab again. Focus moves to Golu's Order button.
    send: { from: user, to: buy, label: Tab }
    set: { nav: "", buy: ring }
  - say: The button shows a 3px Indigo ring. She presses Enter, and the link opens.
    focus: [buy]
  - say: Without a visible ring, she would be pressing Tab blind, with no idea which button Enter will press.
```

### Grouping states

```css
nav a:hover,
nav a:focus-visible {
  text-decoration: underline;
}
```

Mouse users and keyboard users should get the same signal. Group the two pseudo-classes, so pointing and tabbing show the same underline.

## Worked example

Here is the measurement card and the button states:

```css
:root {
  --marigold: #f2a81d;
  --indigo: #2e3a8c;
  --off-white: #faf7f0;
}

body {
  background-color: var(--off-white);
  color: var(--indigo);
}

.buy {
  background-color: var(--marigold);
  color: var(--indigo);
}

.buy:hover {
  background-color: var(--indigo);
  color: var(--off-white);
}

.buy:focus-visible {
  outline: 3px solid var(--indigo);
  outline-offset: 2px;
}
```

1. `:root` holds the three brand colours, each written exactly once.
2. `body` and `.buy` read them with `var()`. No hex code appears anywhere else.
3. Pointing at a button swaps its colours to Indigo and Off-white.
4. Tabbing to a button draws a 3px Indigo ring, 2px away from its edge.
5. To make the Indigo darker, change one line in `:root`. Every rule follows.

## Try it

Paste this at the end of `style.css`:

```css
:root {
  --marigold: #e05a8a;
}
```

Every Order button and the New badge turn Rani pink, from one line. Delete it again. Then press Tab in the preview a few times and watch the focus ring move from link to link.

## Recap

- A custom property is a name starting with `--`. Read it with `var(--name)`.
- Set brand values once on `:root`, so every element can read them.
- `var(--name, fallback)` gives a backup value if the property isn't set.
- Setting a custom property on a smaller element changes it for that element and everything inside it.
- `:hover` styles an element under the mouse, and `:focus-visible` styles it when the keyboard reaches it. Give both the same signal.

## Common mistakes

**1. Forgetting var() when you use a custom property.**

```css
.buy {
  background-color: --marigold;
}
```

The button has no background. `--marigold` on its own isn't a colour, so the browser drops the line. Write `var(--marigold)`.

**2. Only one dash in the name.**

```css
:root {
  -indigo: #2e3a8c;
}
```

Nothing reads it. A custom property needs two dashes at the start: `--indigo`.

**3. Removing the focus ring and putting nothing back.**

```css
.buy:focus {
  outline: none;
}
```

Keyboard users can no longer see which button they're on. If you don't like the browser's ring, replace it with your own, like `outline: 3px solid var(--indigo);`.

**4. A space between the selector and the colon.**

```css
.buy :hover {
  background-color: var(--indigo);
}
```

Nothing happens when you point at the button. The space means "inside", so this picks hovered elements inside `.buy`. Write `.buy:hover`, with no space.

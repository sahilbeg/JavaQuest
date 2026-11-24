## Hook

Look at a wedding card and an electricity bill. Before you read a single word, you know which is which. The letters on the wedding card curl and flow. The bill's letters are plain and stiff.

The shape of letters sets a mood. Dhaaga's catalogue should feel warm and handmade, but the prices must still be easy to read on a cheap phone in bright sun. This ticket picks the fonts, sizes and spacing that do both.

## Concept

### Syntax: font-family

```css
font-family: Georgia, serif;
```

`font-family` picks the **font**: the design of the letters. You give it a list of fonts, separated by commas. The browser uses the first one it can find.

```callout jargon
A **font** is one design for every letter, number and sign. Georgia, Arial and Roboto are fonts. Each one shapes an "a" differently.
```

Font names with spaces need quotes: `"Times New Roman"`. One-word names like `Georgia` don't.

### Font stacks and fallbacks

A browser can only use fonts the phone or laptop already has, unless the site sends one. Georgia is on most laptops, but many Android phones don't have it.

So a `font-family` list is a **font stack**: your first choice, then backups. The last item should always be a **generic family**, a kind of font every device has.

| Generic family | Looks like |
|---|---|
| `serif` | letters with small feet, like a newspaper |
| `sans-serif` | clean letters with no feet |
| `monospace` | every letter the same width, like code |
| `system-ui` | the phone or laptop's own interface font |

```callout jargon
A **fallback** is the backup a browser uses when the first choice isn't there. **Serifs** are the small feet at the ends of letter strokes. Sans means without.
```

Watch an Android phone work through a stack:

```animate
title: Walking down the font stack
kind: scene
nodes:
  - { id: css, label: "Georgia, serif", emoji: "📜" }
  - { id: phone, label: Raju's Android phone, emoji: "📱", value: looking }
  - { id: georgia, label: Georgia, emoji: "❌", row: 1, col: 0 }
  - { id: serif, label: The phone's serif font, emoji: "✅", row: 1, col: 2 }
steps:
  - say: The heading asks for Georgia first, then any serif font.
    focus: [css]
  - say: The phone looks for Georgia in its own fonts.
    send: { from: phone, to: georgia, label: "have it?" }
  - say: Georgia isn't installed on this phone. No error, the browser just moves on to the next name.
    send: { from: georgia, to: phone, label: "no", back: true }
  - say: Next in the list is serif, a generic family. Every phone has some serif font.
    send: { from: phone, to: serif, label: "have it?" }
  - say: Found one. The heading still gets letters with feet, close to the design. Without serif at the end, it would fall back to the browser's default.
    send: { from: serif, to: phone, label: "yes", back: true }
    set: { phone: "serif font" }
```

```callout tip
Websites can also send their own font files, called **web fonts**, so every visitor sees the same letters. You'll meet them in real projects. They cost download time, so a good stack still matters.
```

### Syntax: font-size and font-weight

```css
font-size: 1.25rem;
font-weight: 700;
```

`font-size` sets how big the letters are. Use `rem`, as in the colours ticket.

`font-weight` sets how thick the strokes are, from 100 (very thin) to 900 (very heavy). `400` is normal and `700` is bold. The words `normal` and `bold` mean the same as 400 and 700.

### Syntax: line-height

```css
line-height: 1.5;
```

`line-height` sets the height of each line of text. A plain number with no unit means "times the font size". With 16px text, `1.5` gives lines 24px tall.

Long text with lines too close together is tiring to read. Around `1.5` is comfortable for body text.

```callout tip
Leave line-height without a unit. A plain `1.5` follows each element's own font size, so big headings get big lines and small text gets small ones.
```

### Syntax: text styles

```css
font-style: italic;
text-transform: uppercase;
letter-spacing: 0.1em;
text-decoration: none;
```

| Property | What it does |
|---|---|
| `font-style: italic` | slants the letters |
| `text-transform: uppercase` | shows the text in capitals, without changing the HTML |
| `letter-spacing: 0.1em` | adds space between letters |
| `text-decoration: none` | removes the underline links get by default |

`text-transform` only changes how text *looks*. The HTML still says "Our catalogue", so a screen reader reads it normally.

### Syntax: the em unit

```css
letter-spacing: 0.1em;
```

`em` is like `rem`, but measured from the element's **own** font size, not the base size. A heading at 32px with `letter-spacing: 0.1em` gets 3.2px between letters. Small text gets a smaller gap. That keeps the spacing in proportion.

## Worked example

Here are the font choices for the catalogue:

```css
body {
  font-family: system-ui, sans-serif;
  line-height: 1.5;
}

h1, h2 {
  font-family: Georgia, serif;
}

h2 {
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.price {
  font-size: 1.25rem;
}

nav a {
  text-decoration: none;
}
```

1. `body` sets the main font. `system-ui` is the phone's own font, which loads instantly and reads well. `sans-serif` is the backup.
2. Every element inside `body` **inherits** that font and line height, so the whole page changes with one rule.
3. `h1` and `h2` get Georgia, a warm serif, with `serif` as the fallback.
4. Section titles like "Our catalogue" show in spaced capitals.
5. Prices get bigger, so they're easy to find.
6. The nav links lose their underline. They already sit in a band that looks clickable.

```callout jargon
To **inherit** means an element takes a value from the element around it. Text properties like `font-family`, `color` and `line-height` are inherited. Box properties like `padding` and `border` are not.
```

## Try it

Paste this at the end of `style.css`:

```css
.tagline {
  font-family: "Courier New", monospace;
  letter-spacing: 0.3em;
}
```

The tagline looks typed on an old typewriter. Now change `"Courier New"` to a made-up name, like `Golu Sans`. The browser can't find it, so it falls back to `monospace`. Finally, delete `, monospace` too. What does the browser fall back to now?

## Recap

- `font-family` takes a list of fonts. The browser uses the first one it has.
- End every font stack with a generic family like `serif`, `sans-serif` or `system-ui`.
- `font-weight` runs from 100 to 900: 400 is normal and 700 is bold.
- Give body text a unitless `line-height` around 1.5.
- `text-transform`, `letter-spacing` and `text-decoration` change how text looks without changing the HTML.

## Common mistakes

**1. A font stack with no generic family at the end.**

```css
h1 {
  font-family: Georgia;
}
```

On phones without Georgia, the heading falls back to the browser's default font, which may not match at all. End the list with a generic family: `Georgia, serif`.

**2. A font name with spaces and no quotes.**

```css
body {
  font-family: Times New Roman, serif;
}
```

This usually works, but a name with special characters or numbers can break the whole list. Quote names with spaces to be safe: `"Times New Roman", serif`.

**3. A line-height with a fixed unit.**

```css
body {
  line-height: 24px;
}
```

Body text looks fine, but the 32px `h1` now has 24px lines. Its lines overlap when the name wraps on a phone. Use a plain number: `line-height: 1.5;`.

**4. Typing capitals into the HTML for style.**

```html
<h2>OUR CATALOGUE</h2>
```

Some screen readers spell all-capital words out letter by letter. Write normal text in the HTML, and use `text-transform: uppercase` in the CSS.

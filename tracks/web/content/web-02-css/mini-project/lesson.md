## The design

For Diwali, the catalogue gets a festive look: deep red and gold, on a warm cream page. After the festival, it goes back to Dhaaga's everyday Indigo and Marigold. Switching between the two must be **one change**, not a hunt through the stylesheet.

```callout jargon
A **theme** is a set of values, mostly colours, that changes the look of a whole site without changing its layout or content. A **design token** is one named value in a theme, like the page colour.
```

The trick is to name each custom property by its **job**, not by its colour. A property called `--marigold` can't sensibly hold Diwali gold. A property called `--accent` can hold either.

| Token | Its job | Everyday | Diwali |
|---|---|---|---|
| `--brand` | header band, headings, prices | `#2e3a8c` (Indigo) | `#8c1d40` (deep red) |
| `--accent` | Order buttons, the New badge | `#f2a81d` (Marigold) | `#ffc93c` (diya gold) |
| `--page` | the page, and text on the header | `#faf7f0` (Off-white) | `#fff4e0` (warm cream) |
| `--ink` | body text, and text on buttons | `#2e3a8c` (Indigo) | `#4a1020` (dark maroon) |

Every rule in the stylesheet reads these four tokens. Not one rule outside the theme types a colour code.

The switch is a class on `<body>`: `<body class="theme-diwali">` turns the festival on, and deleting the class turns it off.

```animate
title: One class swaps the whole look
kind: scene
nodes:
  - { id: body, label: "<body>", emoji: "🪔", value: "no class" }
  - { id: tokens, label: Tokens, emoji: "🎨", value: "everyday", row: 0, col: 2 }
  - { id: header, label: Header band, emoji: "🧵", value: Indigo, row: 1, col: 0 }
  - { id: buy, label: Order buttons, emoji: "🟧", value: Marigold, row: 1, col: 1 }
  - { id: page, label: Page, emoji: "📄", value: Off-white, row: 1, col: 2 }
steps:
  - say: Without a theme class, every element finds the tokens on :root. The catalogue has its everyday look.
    focus: [header, buy, page]
  - say: Meera Aunty's switch. The body gets the class theme-diwali.
    set: { body: "theme-diwali" }
    focus: [body]
  - say: The .theme-diwali rule sets the same four tokens again, on body. Everything inside body now finds those values first.
    set: { tokens: Diwali }
    send: { from: body, to: tokens, label: "--brand, --accent, ..." }
  - say: Not one component rule changed, yet the header turns deep red, the buttons diya gold, and the page warm cream.
    set: { header: deep red, buy: diya gold, page: warm cream }
    focus: [header, buy, page]
  - say: After Diwali, delete the class. The tokens fall back to :root, and the everyday look returns.
    set: { body: "no class", tokens: everyday, header: Indigo, buy: Marigold, page: Off-white }
```

## What you build

1. In `:root`, replace the colour-named properties with the four tokens and their everyday values.
2. Make every rule read the tokens. Each brand colour, and each `var(--marigold)`, `var(--indigo)` or `var(--off-white)`, becomes the token for its job. The greys and the red of the Sold out label stay as they are.
3. Add a `.theme-diwali` rule that sets the four tokens to their Diwali values.
4. Add a festive touch that only the Diwali theme has: a dashed gold line under the header.
5. Switch the theme on: give `<body>` the class `theme-diwali` in `index.html`.

### Syntax: setting tokens again for a theme

```css
:root {
  --brand: #2e3a8c;
}

.theme-diwali {
  --brand: #8c1d40;
}
```

Custom properties are inherited. An element reads the value set on the nearest element above it, or on itself. `<body class="theme-diwali">` is closer to every element on the page than `:root` is, so its values win.

The rules that use `var(--brand)` don't know or care which theme is on. That's what keeps the switch to one class.

### Syntax: a rule for one theme only

```css
.theme-diwali .site-header {
  border-bottom: 4px dashed var(--accent);
}
```

This descendant selector picks the header only when it sits inside an element with `theme-diwali`. Without the class, the rule matches nothing, so the everyday header has no dashed line.

## Recap

- Name tokens by their job (`--brand`, `--accent`, `--page`, `--ink`), not by their colour.
- Set the everyday values on `:root`, and the festival values on `.theme-diwali`.
- Every component reads tokens with `var()`, so no rule outside the theme types a colour code.
- A class on `<body>` is the switch. A selector like `.theme-diwali .site-header` adds touches only one theme has.

## Common mistakes

**1. Writing Diwali colours straight into component rules.**

```css
.theme-diwali .buy {
  background-color: #ffc93c;
}
```

It works for the button, but now every component needs its own Diwali rule, and the badge still shows Marigold. Set `--accent` once on `.theme-diwali`, and let `.buy` and `.badge` both read it.

**2. Keeping colour names as token names.**

```css
.theme-diwali {
  --marigold: #ffc93c;
}
```

A property called `--marigold` that holds gold is confusing to the next person who reads it. Use names for jobs: `--accent`.

**3. Putting the class on the wrong element.**

```html
<main class="theme-diwali">
```

Only the product cards change. The header and the footer sit outside `<main>`, so they never see the Diwali tokens. Put the class on `<body>`, which holds everything.

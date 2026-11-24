## Hook

Meera Aunty keeps two lists on the stall's notebook. One is "Things we make": coasters, keychains, toys, bags, booties. The order doesn't matter. The other is "Before we open": put up the table, lay out the stock, stick up the UPI QR code. That order matters a lot.

HTML has an element for each kind of list. Right now Dhaaga's page fakes its lists with line breaks, so nobody can tell they're lists at all.

## Concept

A list groups items that belong together. When you mark a list as a list, the browser adds bullets or numbers for you. Screen readers also announce "list, 5 items", so a listener knows how long it is before it starts.

### Syntax: an unordered list

```html
<ul>
  <li>Coaster set (4)</li>
  <li>Keychain</li>
</ul>
```

`ul` means **unordered list**: the items have no special order. Browsers draw a bullet before each one.

Each item is an `li` element, short for **list item**. Only `li` elements may sit directly inside a `ul`. Put any text, links or pictures inside the `li`, never loose between them.

```callout jargon
A **bullet** is the small dot that browsers draw before each item of an unordered list.
```

### Syntax: an ordered list

```html
<ol>
  <li>Pick an item from the catalogue.</li>
  <li>Send us its name on WhatsApp.</li>
</ol>
```

`ol` means **ordered list**: the order matters, like steps in a recipe. Browsers number the items 1, 2, 3.

You don't type the numbers. The browser counts the `li` elements. Add a step in the middle and every number after it moves up by itself:

```animate
title: The browser does the counting
kind: scene
nodes:
  - { id: ol, label: "<ol>", emoji: "🔢", row: 0, col: 1 }
  - { id: pick, label: "Pick an item", emoji: "🧸", row: 1, col: 0, value: "1." }
  - { id: send, label: "Send us its name", emoji: "💬", row: 1, col: 1, value: "2." }
  - { id: colour, label: "Choose a colour", emoji: "🎨", row: 2, col: 1, value: "2." }
  - { id: pay, label: "Pay by UPI", emoji: "💸", row: 1, col: 2, value: "3." }
links:
  - { from: ol, to: pick }
  - { from: ol, to: send }
  - { from: ol, to: pay }
steps:
  - say: An ol with three li elements. The browser numbers them 1, 2 and 3. None of those numbers are in the HTML.
    focus: [pick, send, pay]
  - say: Meera Aunty wants a new step after picking an item. You add one li in the HTML, as the second item.
    show: [colour]
  - say: The browser counts again. The new step is item 2.
    set: { colour: "2.", send: "3." }
    focus: [colour, send]
  - say: Everything after it moves up by one. Paying is now step 4, and you didn't change a single number.
    set: { pay: "4." }
    focus: [pay]
  - say: A screen reader says "list, 4 items" first, then "1 of 4", "2 of 4" as it reads.
    focus: [ol]
```

### ul or ol?

Ask one question: **if you shuffled the items, would the list still be right?**

- Yes: use `ul`. Five products in any order are still the five products.
- No: use `ol`. Paying before picking an item makes no sense.

```callout tip
A row of links at the top of a site is a list too: a list of places to go. Teams usually put those links in a `ul`. You'll do that in a later ticket.
```

### Syntax: a nested list

```html
<ul>
  <li>Amigurumi (small toy)
    <ul>
      <li>Marigold</li>
      <li>Indigo</li>
    </ul>
  </li>
  <li>Tote bag</li>
</ul>
```

A list can sit inside a list item. This is a **nested list**. The inner `ul` goes *inside* the `li` it belongs to, after that item's text and before its `</li>`.

Browsers indent the inner list and often use a different bullet. Screen readers say "list, 2 items, nested", so listeners know these colours belong to the amigurumi.

```callout warning
The inner list must sit inside an `li`, not straight inside the outer `ul`. A `ul` may only hold `li` elements.
```

### Lists of pairs

There's a third kind, the **description list**, `dl`. It holds pairs: a term (`dt`) and its description (`dd`). It suits glossaries, or a list like "Item code: CST". You'll use `ul` and `ol` far more often.

```html
<dl>
  <dt>CST</dt>
  <dd>Coaster set (4)</dd>
</dl>
```

## Worked example

Here's the "Before we open" list, plus the stall's stock box:

```html
<h3>Before we open</h3>
<ol>
  <li>Put up the table.</li>
  <li>Lay out the stock.</li>
  <li>Stick up the UPI QR code.</li>
</ol>

<h3>In the stock box</h3>
<ul>
  <li>Keychains</li>
  <li>Yarn
    <ul>
      <li>Marigold, 400 g</li>
      <li>Indigo, 250 g</li>
    </ul>
  </li>
</ul>
```

1. The opening steps happen in order, so they go in an `ol`. The browser numbers them 1 to 3.
2. The stock box has no order, so it's a `ul`.
3. The yarn colours belong to "Yarn", so their `ul` sits inside the Yarn `li`, before its `</li>`.

## Try it

Paste this into the editor's body:

```html
<ol>
  <li>Pick an item.</li>
  <li>Pay by UPI.</li>
</ol>
```

- Add `<li>Send us its name on WhatsApp.</li>` between the two items. Watch the numbers change by themselves.
- Change `ol` to `ul` in both tags. The numbers become bullets. Which one fits these steps?
- Nest a list of colours inside "Pick an item." and watch it indent.

## Recap

- `ul` is a list where order doesn't matter. Browsers show bullets.
- `ol` is a list where order matters. Browsers number the items for you.
- Every item is an `li`, and only `li` elements sit straight inside a `ul` or `ol`.
- A nested list goes inside the `li` it belongs to.
- Real lists let screen readers say how many items there are.

## Common mistakes

**1. Faking a list with br.**

```html
<p>Coaster set (4)<br>Keychain<br>Tote bag</p>
```

It looks like a list, but it's one paragraph. A screen reader can't say "list, 3 items", and CSS can't style each item. Use a `ul` with one `li` per item.

**2. Typing the numbers yourself.**

```html
<ol>
  <li>1. Pick an item.</li>
  <li>2. Pay by UPI.</li>
</ol>
```

The page shows "1. 1. Pick an item." The browser already numbers an `ol`. Leave the numbers out.

**3. Putting the nested list outside the li.**

```html
<ul>
  <li>Amigurumi (small toy)</li>
  <ul>
    <li>Marigold</li>
  </ul>
</ul>
```

A `ul` may only hold `li` elements, so this is invalid HTML. Screen readers may not tie the colours to the amigurumi. Move the inner `ul` inside the amigurumi's `li`, before `</li>`.

**4. Text loose inside a ul.**

```html
<ul>
  Our products:
  <li>Keychain</li>
</ul>
```

Only `li` elements belong in a `ul`. Put a title like "Our products" in a heading or paragraph above the list.

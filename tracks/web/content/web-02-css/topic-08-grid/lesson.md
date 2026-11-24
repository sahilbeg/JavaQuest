## Hook

Look at the shelves in a sweet shop. Every jar sits in a column and a row. If one jar is missing, the gap stays a jar-sized gap. Nothing slides sideways to fill it, and the jars on the bottom shelf line up with the ones above.

Flexbox is great for a single row, like the header. But with cards across several rows, each row is worked out on its own, so the last row can stretch strangely. **Grid** works like the shop shelf: you draw the columns first, and every card drops into a cell.

## Concept

### Grid containers

```css
.products {
  display: grid;
}
```

`display: grid` turns an element into a **grid container**. Its direct children become **grid items**. On its own it does little: there is still one column. The magic comes when you describe the columns.

```callout jargon
A **track** is one column or one row of a grid. A **cell** is where a column and a row cross. **Grid lines** are the edges between tracks, numbered from 1.
```

### Syntax: grid-template-columns

```css
grid-template-columns: 200px 200px 200px;
```

`grid-template-columns` lists the width of each column, separated by spaces. Three widths make three columns. The items fill the cells left to right, then start a new row, just like writing in squared notebook paper.

Fixed widths like `200px` don't fit every screen, though. That's what the `fr` unit is for.

### Syntax: the fr unit

```css
grid-template-columns: 1fr 1fr 1fr;
```

`fr` means a **fraction** of the free space. The browser takes the container's width, removes the gaps, and shares the rest among the `fr` columns.

- `1fr 1fr 1fr` makes three equal columns.
- `2fr 1fr` makes the first column twice as wide as the second.

Watch the browser work out the columns for a 900px wide list with 24px gaps:

```animate
title: How wide is 1fr?
kind: code
language: js
code: |
  const width = 900;
  const gap = 24;
  const columns = 3;
  const free = width - gap * (columns - 1);
  const oneFr = free / columns;
  console.log("1fr = " + oneFr + "px");
  console.log("span 2 = " + (oneFr * 2 + gap) + "px");
steps:
  - line: 1
    vars: { width: 900 }
    say: The products list is 900px wide on this laptop.
  - line: 2
    vars: { gap: 24 }
    say: There's a 24px gap between columns.
  - line: 3
    vars: { columns: 3 }
    say: The grid has three 1fr columns, so there are two gaps between them.
  - line: 4
    vars: { free: 852 }
    say: The gaps use 48px. That leaves 852px of free space to share.
  - line: 5
    vars: { oneFr: 284 }
    say: Three equal shares of 852 are 284px each. That's what 1fr means here.
  - line: 6
    out: "1fr = 284px"
    say: Every card is 284px wide, on every row. No row can stretch on its own.
  - line: 7
    out: "span 2 = 592px"
    say: A card spanning two columns covers two tracks and the gap between them, 284 + 24 + 284.
```

### Syntax: repeat()

```css
grid-template-columns: repeat(3, 1fr);
```

`repeat(3, 1fr)` is a short way to write `1fr 1fr 1fr`. The first number is how many times, and the second is the track size. It's easier to read, and easy to change to 4 columns later.

### Syntax: gap in a grid

```css
gap: 1.5rem;
```

`gap` works just like in flexbox: space between the columns and between the rows, never around the outside. You can set them apart with `column-gap` and `row-gap`.

### Syntax: spanning several columns

```css
.featured {
  grid-column: span 2;
}
```

`grid-column` goes on a grid **item**. `span 2` means "take up two columns instead of one". The next items flow into whatever cells are left.

You can also place an item between grid lines: `grid-column: 1 / 3` means "from line 1 to line 3", which also covers two columns.

```animate
title: Filling the shelf
kind: scene
nodes:
  - { id: golu, label: "Golu (span 2)", emoji: "🐘", row: 0, col: 0 }
  - { id: cst, label: Coaster set, emoji: "🟡", row: 0, col: 2 }
  - { id: key, label: Keychain, emoji: "🔑", row: 1, col: 0 }
  - { id: bag, label: Tote bag, emoji: "👜", row: 1, col: 1 }
  - { id: bty, label: Baby booties, emoji: "🧦", row: 1, col: 2 }
steps:
  - say: The grid has three equal columns. Golu's card comes first, and it asks for two of them.
    show: [golu]
    focus: [golu]
  - say: Golu fills cells 1 and 2 of the first row. One cell is left on that row.
    focus: [golu]
  - say: The coaster set is next. It needs one cell, and the third cell is free, so it goes there.
    show: [cst]
    focus: [cst]
  - say: The first row is full. The keychain starts the second row, in column 1.
    show: [key]
    focus: [key]
  - say: The tote bag and the booties fill columns 2 and 3. Every card lines up with the column above it.
    show: [bag, bty]
    focus: [bag, bty]
```

### Grid or flexbox?

Both lay out boxes, and they work well together.

| | Flexbox | Grid |
|---|---|---|
| Thinks in | one direction: a row or a column | two directions: rows and columns together |
| Sizes come from | the items' content | the tracks you draw |
| Good for | a header, a nav, the parts inside a card | a page layout, a product grid |

The catalogue uses both. The header and each card's insides stay flexbox. The list of cards becomes a grid.

### Columns that fit the screen

```css
grid-template-columns: repeat(auto-fill, minmax(14rem, 1fr));
```

There's also a way for the grid to choose the number of columns itself. `minmax(14rem, 1fr)` means "at least 14rem, at most one share". `auto-fill` fits as many of those columns as the row has room for. You'll meet another way, media queries, in the next ticket.

## Worked example

Here is the grid for the catalogue:

```css
.products {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.featured {
  grid-column: span 2;
}
```

1. The list becomes a grid container, so each card is a grid item.
2. `repeat(3, 1fr)` draws three equal columns that share the list's width.
3. `gap: 1.5rem` puts space between columns and between rows.
4. Golu's card has `class="product featured"`, so it covers two columns. The coaster set fills the last cell of the first row.
5. The other three cards fill the second row. Every card is exactly as wide as the card above it.

The old flexbox lines on `.products`, `display: flex` and `flex-wrap: wrap`, must go. `flex: 1 1 14rem` on the cards can go too, because grid items take their size from the tracks.

## Try it

Paste this at the end of `style.css`:

```css
.products {
  grid-template-columns: 2fr 1fr 1fr;
}
```

The first column becomes twice as wide as the others. Now try `repeat(4, 1fr)`, then `repeat(auto-fill, minmax(10rem, 1fr))`, and make the preview narrower and wider. Which one keeps the cards a sensible size on every width?

## Recap

- `display: grid` makes a grid container, and its direct children become grid items.
- `grid-template-columns` lists the column widths. `repeat(3, 1fr)` makes three equal columns.
- `fr` is a share of the free space, after the gaps are taken out.
- `grid-column: span 2` makes one item cover two columns.
- Use grid for rows and columns together, and flexbox for a single row or column.

## Common mistakes

**1. Leaving the old flexbox lines in.**

```css
.products {
  display: flex;
  flex-wrap: wrap;
  grid-template-columns: repeat(3, 1fr);
}
```

The cards still wrap like flex items, and the columns do nothing. `grid-template-columns` only works on a grid container. Change `display: flex` to `display: grid`, and delete `flex-wrap`.

**2. Putting grid-column on the container.**

```css
.products {
  display: grid;
  grid-column: span 2;
}
```

Golu's card stays one column wide. `grid-column` belongs on the item that should span, so write it in `.featured`.

**3. Writing repeat without a comma.**

```css
.products {
  grid-template-columns: repeat(3 1fr);
}
```

The grid falls back to one column. `repeat` needs a comma between the count and the size: `repeat(3, 1fr)`.

**4. Using percentages with gaps.**

```css
.products {
  grid-template-columns: 33.33% 33.33% 33.33%;
  gap: 1.5rem;
}
```

The three columns already fill 100%, and the gaps add 3rem more. The grid sticks out past the right edge. `fr` shares only the space left after the gaps, so use `repeat(3, 1fr)`.

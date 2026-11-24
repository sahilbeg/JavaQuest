## Hook

The stall has a price board: a chalkboard with a code, a name and a price on each line. People read across a row to find a price. Some read down a column to find the cheapest thing.

That's a **table**: information in rows and columns. Dhaaga's page needs the same board, so customers stop asking "how much?" on WhatsApp.

## Concept

A table is for **data** that has rows and columns, where each column means the same kind of thing. HTML builds a table one row at a time, and each row is a line of cells.

```callout jargon
**Data** means facts, like codes, names and prices. **Tabular data** is data that fits neatly in rows and columns.
```

### Syntax: rows and cells

```html
<table>
  <tr>
    <td>CST</td>
    <td>Coaster set (4)</td>
    <td>₹150.00</td>
  </tr>
</table>
```

- `table` wraps the whole table.
- `tr` is a **table row**.
- `td` is a **table data cell**: one box in the row.

There's no element for a column. A column is just the first cell of every row, or the second, and so on. So every row needs its cells in the same order.

### Syntax: header cells

```html
<tr>
  <th scope="col">Code</th>
  <th scope="col">Item</th>
  <th scope="col">Price</th>
</tr>
```

`th` is a **header cell**: a title for a column or a row. Browsers show it in bold and centred.

The `scope` attribute says what the header is the title of:

- `scope="col"` means "I'm the title of the column below me".
- `scope="row"` means "I'm the title of the row I'm in".

In the price list, each code names its row, so the code cell is a `th` with `scope="row"`:

```html
<tr>
  <th scope="row">BAG</th>
  <td>Tote bag</td>
  <td>₹899.00</td>
</tr>
```

### Why headers matter

A sighted customer glances up to see which column they're in. A screen reader user can't glance. Instead, the screen reader reads the headers along with each cell. Watch it read one price:

```animate
title: A screen reader reads a cell
kind: scene
nodes:
  - { id: reader, label: Screen reader, emoji: "🔊", row: 0, col: 0 }
  - { id: colhead, label: "th Price (scope=col)", emoji: "🏷️", row: 0, col: 3 }
  - { id: rowhead, label: "th BAG (scope=row)", emoji: "🏷️", row: 2, col: 1 }
  - { id: cell, label: "td ₹899.00", emoji: "💰", row: 2, col: 3 }
links:
  - { from: colhead, to: cell }
  - { from: rowhead, to: cell }
steps:
  - say: Mr. Fernandes uses a screen reader. He moves into the table, down to the BAG row.
    focus: [reader]
  - say: He steps right, into the price cell. On its own, "899 rupees" could mean anything.
    send: { from: reader, to: cell, label: "next cell" }
  - say: The cell sits in a column whose th has scope col. The screen reader looks up and finds "Price".
    focus: [colhead, cell]
  - say: It sits in a row whose th has scope row. The screen reader finds "BAG".
    focus: [rowhead, cell]
  - say: So he hears "BAG, Price, 899 rupees". With td cells instead of th, he'd hear only the number.
    send: { from: cell, to: reader, label: "BAG, Price, ₹899.00", back: true }
```

### Syntax: a caption

```html
<table>
  <caption>Our prices</caption>
  ...
</table>
```

`caption` gives the table a title. It must be the very first thing inside `table`. Screen readers announce it when someone reaches the table, so they know what it holds before they hear a single cell.

### Syntax: thead and tbody

```html
<table>
  <caption>Our prices</caption>
  <thead>
    <tr><th scope="col">Code</th><th scope="col">Item</th><th scope="col">Price</th></tr>
  </thead>
  <tbody>
    <tr><th scope="row">CST</th><td>Coaster set (4)</td><td>₹150.00</td></tr>
  </tbody>
</table>
```

Tables can be split into parts:

- `thead` holds the row (or rows) of column titles.
- `tbody` holds the body: the data rows.
- `tfoot` holds a footer row, like a total. It's optional.

These parts help screen readers, and CSS can style them separately. On a long printed table, browsers can repeat the `thead` on each page.

### Cells that span

A cell can stretch across more than one column with `colspan`, or more than one row with `rowspan`. For example, `<td colspan="2">Free delivery above ₹999</td>` fills two columns. Use them sparingly: they make tables harder to read aloud.

### Tables are for data, not layout

Long ago, people built whole page layouts out of tables: a column for the menu, a column for the content. Don't. Screen readers would read the page as one big table of nonsense. Use tables only for real rows and columns of data. CSS handles layout, as you'll see in the next project.

## Worked example

Here's a table of the stall's opening hours:

```html
<table>
  <caption>When to find us</caption>
  <thead>
    <tr>
      <th scope="col">Place</th>
      <th scope="col">Days</th>
      <th scope="col">Hours</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th scope="row">FC Road stall</th>
      <td>Saturday and Sunday</td>
      <td>10:00 to 20:00</td>
    </tr>
    <tr>
      <th scope="row">Aundh shop</th>
      <td>Tuesday to Sunday</td>
      <td>11:00 to 21:00</td>
    </tr>
  </tbody>
</table>
```

1. `caption` comes first and names the table.
2. `thead` has one row of three column titles, each a `th` with `scope="col"`.
3. In `tbody`, each row starts with the place's name. It titles that row, so it's a `th` with `scope="row"`.
4. Every row has three cells, in the same order as the titles.

## Try it

Paste the worked example into the editor's body, then:

- Add a row for the Koregaon Park pop-up: Diwali weeks only, 12:00 to 22:00.
- Delete the `td` for Days in one row and see the cells slide left under the wrong titles. Put it back.
- Change the caption's text and notice it shows above the table.

## Recap

- A table is rows (`tr`) of cells. `td` holds data, and `th` holds a header.
- `scope="col"` makes a `th` the title of its column. `scope="row"` makes it the title of its row.
- `caption` names the table, and comes first inside it.
- `thead` holds the column titles and `tbody` holds the data rows.
- Use tables for rows and columns of data, never for page layout.

## Common mistakes

**1. Using td for the titles.**

```html
<tr>
  <td>Code</td>
  <td>Item</td>
  <td>Price</td>
</tr>
```

It looks like a title row once you make it bold, but a screen reader treats it as data. It can't read "Price" along with each price. Use `<th scope="col">`.

**2. A row with a missing cell.**

```html
<tr>
  <th scope="row">AMI</th>
  <td>₹450.00</td>
</tr>
```

The price slides into the Item column, and the Price column is empty. Every row needs one cell per column, in the same order.

**3. Putting the caption in the wrong place.**

```html
<table>
  <tr><th scope="col">Code</th></tr>
  <caption>Our prices</caption>
</table>
```

`caption` must be the first child of `table`. Move it to just after `<table>`.

**4. Building the page's layout with a table.**

```html
<table>
  <tr>
    <td>Menu</td>
    <td>The whole catalogue page...</td>
  </tr>
</table>
```

A screen reader announces a table and reads the page cell by cell. Use real elements for each part of the page, and CSS for the layout.

## Hook

Imagine a classroom where a new student joins. The teacher could add one more desk. Or she could throw out every desk, buy new ones, and make everybody sit down again. The second way works too, but everyone's open notebook, pencil and half-written answer are gone.

Redrawing a whole list with `replaceChildren` is the second way. It's simple, and for a list nobody touches, it's fine. But when people type, tick or focus things in the list, each redraw throws their work away. This ticket teaches the first way: keep the desks, add one.

## Concept

### Elements remember things

Since the DOM module, you have redrawn lists from data: build fresh elements, then `replaceChildren`. The data is safe. But some things live **only in the elements**, not in your data:
- the text typed in an input, before anyone saves it,
- whether a checkbox is ticked,
- which element has focus, and where the cursor is,
- how far a box is scrolled.

Throw an element away, and all of that goes with it. A fresh copy starts empty.

```callout jargon
**Element state** is what an element remembers on its own: typed text, ticks, focus, scroll. Your data doesn't hold it, so a redraw that replaces the element loses it.
```

### Syntax: a key on every row

```html
<li data-key="order-17">...</li>
```

To keep a row, you must recognise it on the next update. Give every row a **key**: the id of the thing it shows.
- On the next update, the item `order-17` finds the row with `data-key="order-17"`.
- The key comes from the data, like an order id, so it never changes while the order exists.

```callout jargon
A **key** is a value that says which piece of data an element shows. It lets an update find the same element again instead of making a new one.
```

### The plan: keep, make, remove, move

When new data arrives, each item falls into one of these cases:

| The item... | What happens to its row |
|---|---|
| was there before | **keep** the same element, and update its text |
| is new | **make** a new row |
| is gone | **remove** its row |
| changed place | **move** its row to the new place |

Only new rows are made. Kept rows keep their ticks and typed text, and their focus too when they don't have to move.

```callout jargon
To **reconcile** means to bring the page in line with new data by changing only what's different, instead of drawing it all again.
```

### Watch the plan run

This version uses strings for rows, so it runs in Node:

```animate
title: Which rows stay, which are new, which go
kind: code
language: js
code: |
  const rows = new Map([["order-17", "row A"], ["order-18", "row B"]]);
  const next = ["order-18", "order-19"];
  for (const id of next) {
    const row = rows.get(id) ?? `new row for ${id}`;
    rows.delete(id);
    console.log(`${id}: ${row}`);
  }
  console.log(`remove: ${[...rows.keys()].join(", ")}`);
steps:
  - line: 1
    say: Before changing anything, the old rows go into a Map, from key to row. Two orders are on screen.
  - line: 2
    vars: { next: '["order-18", "order-19"]' }
    say: New data arrives. order-17 is done, order-18 stays, and order-19 is new.
  - line: 4
    vars: { id: '"order-18"', row: '"row B"' }
    say: order-18 finds its row in the Map. It is kept, the very same row B.
  - line: 5
    say: The row is taken out of the Map, so it won't be removed at the end.
  - line: 6
    out: "order-18: row B"
    say: Row B stays, with whatever was ticked in it.
  - line: 4
    vars: { id: '"order-19"', row: '"new row for order-19"' }
    say: order-19 isn't in the Map, so a new row is made for it. Only this one.
  - line: 6
    out: "order-19: new row for order-19"
    say: The new row joins the list.
  - line: 8
    vars: { id: ~, row: ~ }
    out: "remove: order-17"
    say: Whatever is left in the Map belongs to items that are gone. Only order-17's row is removed.
```

### Syntax: reconcile

```js
reconcile(list, items, { key, create, update });
```

- `key(item)` gives the item's key, like `(order) => order.id`.
- `create(item)` makes a new, empty row. It runs only for new keys.
- `update(row, item)` fills a row from its item: text, classes, `disabled`. It runs for **every** row, new or kept, so kept rows show fresh data too.

The function works through the items in order. Before it returns, the list holds exactly one row per item, in the items' order.

### Moving without remaking

To put a kept row in a new place, insert it there:

```js
list.insertBefore(row, list.children[index] ?? null);
```

- If `row` is already somewhere in the page, `insertBefore` **moves** it. It is never copied.
- With `null` as the second argument, the row goes to the end.
- Moving takes the row out and puts it back, so anything focused inside it loses focus. Ticks and typed text stay.
- So check first: `if (list.children[index] !== row)`. A row that's already in its place is left alone.

### Keys must be stable and unique

- **Stable:** the same item gets the same key every time. An order id is stable. The row's position, `index`, is not: when order-17 leaves, every order below it gets a new index, and rows end up showing the wrong order's ticks.
- **Unique:** two items never share a key in one list, or one row would be used for both.

```callout tip
This is exactly what frameworks do for you. React asks for a `key` on every list item, and Lit has a `repeat` helper. Writing it by hand once shows you why they ask.
```

## Worked example

The stall tablet sorts yarn by grams left. Each row has a notes box where Raju types things like "ask the supplier".

```js
const shelf = document.querySelector("#yarn");

function update(yarn) {
  reconcile(shelf, [...yarn].sort((a, b) => a.grams - b.grams), {
    key: (entry) => entry.colour,
    create: () => {
      const row = document.createElement("li");
      row.innerHTML = `<span class="text"></span> <input class="note" aria-label="Note">`;
      return row;
    },
    update: (row, entry) => {
      row.querySelector(".text").textContent = `${entry.colour}: ${entry.grams} g`;
      row.classList.toggle("low", entry.grams < 100);
    },
  });
}

update([{ colour: "Mint", grams: 135 }, { colour: "Marigold", grams: 420 }]);
update([{ colour: "Mint", grams: 95 }, { colour: "Marigold", grams: 420 }, { colour: "Indigo", grams: 60 }]);
```

1. The key is the colour, which never changes. The rows are sorted, so their order can change.
2. `create` makes the row's shape once: the text and an empty note box. The note box's label is fixed, so `innerHTML` is safe here.
3. `update` fills the text and the `low` class, every time. It never touches the note.
4. On the second update, Indigo is new and goes to the top. Mint and Marigold are kept and only their text changes. A note Raju was typing in Mint's box is still there.

## Try it

The heart of reconciling is comparing two lists of keys. Run this, then add `order-21` to `after` and see which list it lands in.

```js run
const before = ["order-17", "order-18", "order-19"];
const after = ["order-19", "order-17", "order-20"];

const kept = after.filter((id) => before.includes(id));
const added = after.filter((id) => !before.includes(id));
const removed = before.filter((id) => !after.includes(id));

console.log(`kept: ${kept.join(", ")}`);
console.log(`added: ${added.join(", ")}`);
console.log(`removed: ${removed.join(", ")}`);
```

It prints `kept: order-19, order-17`, `added: order-20` and `removed: order-18`. Two rows are moved, one is made and one is thrown away.

## Recap

- Some things live only in elements: typed text, ticks, focus, scroll. Replacing an element loses this **element state**.
- Give every row a **key** from its data. It must be **stable** and **unique**, so never the index.
- To **reconcile**: keep rows whose keys stay, make rows for new keys, remove rows for keys that are gone, and move rows into the new order.
- `create` runs only for new rows. `update` runs for every row.
- `insertBefore` moves an existing element. Move a row only when it isn't already in its place.

## Common mistakes

**1. Redrawing everything after all.**

```js
list.replaceChildren(...orders.map((order) => {
  const row = create(order);
  update(row, order);
  return row;
}));
```

It looks right, and the text is correct. But every row is new, so every tick and every half-typed note is gone. Keep rows whose keys are still there.

**2. Using the index as the key.**

```js
orders.forEach((order, index) => {
  row.dataset.key = String(index);
});
```

When order-17 is packed and leaves, order-18 moves up to index 0 and is handed order-17's row, ticks included. Use the order's own id.

**3. Filling the row only in create.**

```js
create: (order) => {
  const row = document.createElement("li");
  row.textContent = `${order.id}: ${order.status}`;
  return row;
}
```

The status is right once. When it changes from "new" to "paid", the kept row still says "new", because `create` never runs for it again. Put everything that can change in `update`.

**4. Forgetting to remove old rows.**

```js
for (const item of items) {
  const row = old.get(key(item)) ?? create(item);
  update(row, item);
  list.append(row);
}
```

New and kept rows are right, but rows for finished orders stay on screen for ever. After the loop, remove every row left in the Map.

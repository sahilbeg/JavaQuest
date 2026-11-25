## Hook

The yarn shelf at the Aundh shop has a label under every cone. When Arjun moves the cones around, the labels move with them. If the labels stayed put while the cones moved, every label would be wrong.

React lists work the same way. Each row needs a label that belongs to its item, so React can follow it when the list changes. That label is the key.

## Concept

### Syntax: a list from an array

```tsx
interface Yarn {
  id: string;
  colour: Colour;
  grams: number;
}

function YarnList({ yarns }: { yarns: Yarn[] }) {
  return (
    <ul>
      {yarns.map((yarn) => (
        <li key={yarn.id}>{yarn.colour}: {yarn.grams} g</li>
      ))}
    </ul>
  );
}
```

JSX can show an array of elements. `map` turns each `Yarn` into an `<li>`, and React draws them in order.

TypeScript follows the types through `map`. Because `yarns` is a `Yarn[]`, `yarn` is a `Yarn`. A typo like `yarn.colur` is caught at once.

### Syntax: keys

```tsx
<li key={yarn.id}>...</li>
```

Every element made in a loop needs a `key` prop: text or a number, unique among its siblings, that stays the same for that item.

```callout jargon
A **key** is a label React uses to match each list row with its item between renders. It is not shown on the page.
```

When the list changes, React compares old keys with new ones. Same key: the same row, updated. A new key: a new row. A missing key: that row is removed.

Use an id from the data. Two cones can both be Mint, so the colour is not unique. An array index changes when rows move, so it doesn't belong to the item either.

### Why keys matter

```animate
title: Sorting a keyed list
kind: scene
nodes:
  - { id: r1, label: "key yarn-1", emoji: "🟡", value: "Marigold: 120 g", row: 0, col: 0 }
  - { id: r2, label: "key yarn-2", emoji: "🟢", value: "Mint: 30 g", row: 0, col: 1 }
  - { id: r3, label: "key yarn-3", emoji: "🟢", value: "Mint: 75 g", row: 0, col: 2 }
  - { id: react, label: "React", emoji: "⚛️", row: 1, col: 1 }
steps:
  - say: Three rows, each keyed by its cone's id. Two of them are Mint.
    focus: [r1, r2, r3]
  - say: Arjun taps Fewest grams first. The component renders the rows in a new order.
    focus: [react]
  - say: React matches by key. yarn-2 is still yarn-2, so it moves that row instead of rewriting it.
    send: { from: react, to: r2, label: "move to top" }
  - say: Every row keeps its own text and anything inside it. Nothing is drawn twice.
    focus: [r2, r3, r1]
  - say: With the colour as the key, both Mint rows would say Mint. React would warn and could mix them up.
    focus: [r2, r3]
```

If you forget the key, React logs a warning in the console: `Each child in a list should have a unique "key" prop.`

### Syntax: readonly arrays

```tsx
interface YarnShelfProps {
  yarns: readonly Yarn[];
}
```

A component must never change its props. They belong to the parent. `readonly Yarn[]` makes TypeScript enforce that: methods that change the array, like `push` and `sort`, are type errors.

```tsx
yarns.sort((a, b) => a.grams - b.grams);
```

```text
error TS2339: Property 'sort' does not exist on type 'readonly Yarn[]'.
```

`map`, `filter`, `reduce` and `slice` still work, because they make new arrays or values.

```callout jargon
A **readonly array** is an array type without the changing methods. You can read it, but `tsc` refuses `push`, `pop`, `sort` and assigning to an index.
```

### Syntax: sorting a copy

```tsx
const sorted = [...yarns].sort((a, b) => a.grams - b.grams);
```

`[...yarns]` makes a new, ordinary array with the same items. Sorting it leaves the parent's array alone.

The compare function returns a negative number when `a` comes first. `a.grams - b.grams` puts the fewest grams first.

```callout tip
Newer JavaScript has `yarns.toSorted(...)`, which returns a sorted copy in one step. It needs the ES2023 library, which this course has. `[...yarns].sort(...)` works everywhere.
```

### Derived values

```tsx
const total = yarns.reduce((sum, yarn) => sum + yarn.grams, 0);
const low = yarns.filter((yarn) => yarn.grams < 40);
```

The total and the low cones come straight from the props. Work them out on every render; never copy them into state.

```callout jargon
A **derived value** is worked out from props or state during the render, like a total. It can never be out of date, because it is never stored.
```

## Worked example

The Diwali pop-up shows its stock as a table, with sold-out rows last.

```tsx
interface StockRow {
  id: string;
  name: string;
  left: number;
}

export function StockTable({ rows }: { rows: readonly StockRow[] }) {
  const ordered = [...rows].sort((a, b) => Number(a.left === 0) - Number(b.left === 0));
  const totalLeft = rows.reduce((sum, row) => sum + row.left, 0);

  return (
    <table>
      <tbody>
        {ordered.map((row) => (
          <tr key={row.id} className={row.left === 0 ? "sold-out" : undefined}>
            <td>{row.name}</td>
            <td>{row.left}</td>
          </tr>
        ))}
      </tbody>
      <tfoot>
        <tr>
          <td>Total</td>
          <td>{totalLeft}</td>
        </tr>
      </tfoot>
    </table>
  );
}
```

Line by line:

- `rows` is readonly, so the sort works on a copy.
- `Number(a.left === 0)` is 1 for a sold-out row and 0 for others, so sold-out rows sink to the bottom.
- Each `<tr>` is keyed by the row's id. `className={... ? "sold-out" : undefined}` adds the class only when needed; `undefined` adds no attribute.
- `totalLeft` is derived on every render.

## Try it

Sorting a copy keeps the original order safe. Run it, then change `[...shelf]` to just `shelf` and see what happens to the first line.

```ts run
interface Yarn {
  id: string;
  colour: string;
  grams: number;
}

const shelf: Yarn[] = [
  { id: "yarn-1", colour: "Marigold", grams: 120 },
  { id: "yarn-2", colour: "Mint", grams: 30 },
  { id: "yarn-3", colour: "Mint", grams: 75 },
];

const fewestFirst = [...shelf].sort((a, b) => a.grams - b.grams);
console.log(shelf.map((y) => y.id).join(" "));
console.log(fewestFirst.map((y) => y.id).join(" "));
console.log(shelf.reduce((sum, y) => sum + y.grams, 0));
```

It prints `yarn-1 yarn-2 yarn-3`, then `yarn-2 yarn-3 yarn-1`, then `225`. Without the copy, the shelf itself would be sorted, and the first line would change too.

## Recap

- `items.map((item) => <li key={item.id}>...</li>)` draws one row per item. TypeScript knows each item's type.
- Every row made in a loop needs a key that is unique and belongs to its item. Use the id, not the index or a name.
- Type array props as `readonly Item[]`, so `tsc` stops you from changing the parent's data.
- Sort a copy: `[...items].sort(...)`.
- Work out totals and filtered lists on every render. They are derived values, not state.

## Common mistakes

**1. Forgetting the key.**

```tsx
{yarns.map((yarn) => <li>{yarn.colour}</li>)}
```

The list shows, but the console warns `Each child in a list should have a unique "key" prop.` Rows can mix up their contents when the list changes. Add `key={yarn.id}`.

**2. A key that isn't unique.**

```tsx
{yarns.map((yarn) => <li key={yarn.colour}>{yarn.colour}</li>)}
```

With two Mint cones, React warns about two children with the same key, and one row may vanish or repeat. Keys must be unique: use the id.

**3. Sorting the props in place.**

```tsx
const shown = yarns.sort((a, b) => a.grams - b.grams);
```

With `readonly Yarn[]`, `tsc` says `Property 'sort' does not exist on type 'readonly Yarn[]'.` Without `readonly`, it would quietly reorder the parent's array. Write `[...yarns].sort(...)`.

**4. Putting the key on the wrong element.**

```tsx
{yarns.map((yarn) => (
  <li>
    <span key={yarn.id}>{yarn.colour}</span>
  </li>
))}
```

React still warns, because the key must be on the outermost element the `map` returns: the `<li>`.

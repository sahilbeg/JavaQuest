## Hook

Raju has one set of glass jars at the stall. Today they hold keychains, tomorrow buttons, next week yarn samples. The jar doesn't care what goes in. But once a jar is labelled "keychains", nobody puts booties in it.

A generic component is that jar. It works with any type of item, and once you fill it with one type, TypeScript keeps it to that type.

## Concept

### One shape, many types

The site has three pickers. Each shows a row of buttons, marks the chosen one, and reports a tap:

- yarn colours, which are strings like `"Mint"`
- gift boxes, which are objects like `{ id: "box-s", label: "Small box", paise: 3000 }`
- delivery slots, which are objects with a day and a time

Writing three copies means three places to fix every bug. Typing the options as `unknown[]` or `any[]` loses all the checks: the parent would get back an `unknown` and have to guess what it is.

In ts-09 you solved this for functions with generics. Components are functions, so the same idea works.

### Syntax: a generic component

```tsx
interface PickListProps<T> {
  options: readonly T[];
  onPick: (option: T) => void;
}

function PickList<T>({ options, onPick }: PickListProps<T>) {
  ...
}
```

`T` is a type parameter: a placeholder for "the type of one option". The props interface takes it too, so `options` and `onPick` agree on the same `T`.

```callout jargon
A **generic component** has a type parameter, like `PickList<T>`. Each use fills it with one type, and every prop that mentions `T` must agree.
```

```callout warning
In a `.tsx` file, an arrow function can't start with a bare `<T>`, because it looks like a JSX tag. Write `<T,>(props: PickListProps<T>) => ...`, or use a `function` declaration as here.
```

### TypeScript infers T from the props

```tsx
<PickList options={BOXES} onPick={(box) => setBox(box)} />
```

You don't write `<PickList<GiftBox> ...>`. TypeScript sees that `options` is a `GiftBox[]`, so `T` is `GiftBox`. Then `box` in `onPick` is a `GiftBox` too, with all its fields.

```animate
title: How T gets its type
kind: scene
nodes:
  - { id: options, label: "options={BOXES}", emoji: "📦", value: "GiftBox[]" }
  - { id: tsc, label: "tsc", emoji: "🔍", value: "T = ?" }
  - { id: pick, label: "onPick", emoji: "👆", value: "(box: ?) => void" }
  - { id: render, label: "renderOption", emoji: "🎨", value: "(box: ?) => ReactNode" }
steps:
  - say: The parent passes an array of gift boxes as options.
    focus: [options]
  - say: tsc matches readonly T[] with GiftBox[], so T must be GiftBox.
    send: { from: options, to: tsc, label: "T[] = GiftBox[]" }
    set: { tsc: "T = GiftBox" }
  - say: Every other prop that mentions T now uses GiftBox. onPick gets a GiftBox.
    send: { from: tsc, to: pick, label: "T → GiftBox" }
    set: { pick: "(box: GiftBox) => void" }
  - say: renderOption gets a GiftBox too, so box.label and box.paise are checked.
    send: { from: tsc, to: render, label: "T → GiftBox" }
    set: { render: "(box: GiftBox) => ReactNode" }
```

### Syntax: a render prop

```tsx
interface PickListProps<T> {
  renderOption: (option: T) => ReactNode;
}

<button>{renderOption(option)}</button>
```

`PickList` doesn't know what a `T` looks like, so it can't decide what to show. It asks the caller, with a function prop that turns one option into something to draw.

```callout jargon
A **render prop** is a function prop that returns something to draw, like `renderOption`. The component calls it; the caller decides what it shows.
```

```tsx
<PickList options={BOXES} renderOption={(box) => `${box.label}, ${rupees(box.paise)}`} ... />
```

### Syntax: a key function

```tsx
interface PickListProps<T> {
  getKey: (option: T) => string;
}

{options.map((option) => (
  <button key={getKey(option)} ...>
```

Every list row needs a key, but `PickList` can't know which field is unique. So the caller passes `getKey` too: `(box) => box.id` for boxes, `(colour) => colour` for colours.

The same function can tell which option is selected. Two objects with the same id are the same option, even if they are different copies:

```tsx
const pressed = selected !== null && getKey(selected) === getKey(option);
```

### Syntax: a constraint instead of a key function

```tsx
function IdList<T extends { id: string }>({ items }: { items: readonly T[] }) {
  return (
    <ul>
      {items.map((item) => (
        <li key={item.id}>{item.id}</li>
      ))}
    </ul>
  );
}
```

When every option will have an `id`, a constraint from ts-09 does the job. `T extends { id: string }` means "any type, as long as it has an id". Then `item.id` is allowed inside.

The catch: plain strings like `"Mint"` have no `id`, so they can't be used. That is why `PickList` takes `getKey` instead.

## Worked example

A table that can show any list of rows. The caller describes the columns.

```tsx
import type { ReactNode } from "react";

interface Column<T> {
  header: string;
  cell: (row: T) => ReactNode;
}

interface DataTableProps<T> {
  rows: readonly T[];
  columns: readonly Column<T>[];
  getKey: (row: T) => string;
}

export function DataTable<T>({ rows, columns, getKey }: DataTableProps<T>) {
  return (
    <table>
      <thead>
        <tr>
          {columns.map((column) => (
            <th key={column.header}>{column.header}</th>
          ))}
        </tr>
      </thead>
      <tbody>
        {rows.map((row) => (
          <tr key={getKey(row)}>
            {columns.map((column) => (
              <td key={column.header}>{column.cell(row)}</td>
            ))}
          </tr>
        ))}
      </tbody>
    </table>
  );
}
```

Used for orders:

```tsx
<DataTable
  rows={orders}
  getKey={(order) => order.id}
  columns={[
    { header: "Order", cell: (order) => order.id },
    { header: "Total", cell: (order) => rupees(order.paise) },
  ]}
/>
```

Line by line:

- `Column<T>` is generic too, so each `cell` gets a whole row of type `T`.
- `T` is inferred from `rows`. Inside `cell`, `order` is an order, so a typo in `order.paise` is caught.
- The table never looks inside a row itself. It only calls `getKey` and `cell`.

## Try it

A generic `pick` function works the same way as the component. Run it, then call it with an array of numbers.

```ts run
function pickNext<T>(options: readonly T[], current: T, getKey: (option: T) => string): T {
  const at = options.findIndex((option) => getKey(option) === getKey(current));
  return options[(at + 1) % options.length];
}

const colours = ["Marigold", "Indigo", "Mint"] as const;
console.log(pickNext(colours, "Indigo", (c) => c));

interface GiftBox {
  id: string;
  label: string;
}
const boxes: GiftBox[] = [
  { id: "box-s", label: "Small box" },
  { id: "box-l", label: "Large box" },
];
console.log(pickNext(boxes, { id: "box-l", label: "Large box" }, (b) => b.id).label);
```

It prints `Mint`, then `Small box`. The second call passed a copy of the large box, but `getKey` matched it by id, and the list wrapped round to the start.

## Recap

- A generic component, `function PickList<T>(props: PickListProps<T>)`, works for any type while keeping every prop in agreement.
- TypeScript infers `T` from the props, usually from the array you pass.
- Render props like `renderOption: (option: T) => ReactNode` let the caller decide what to show.
- A key function, `getKey: (option: T) => string`, gives keys and identity without knowing what `T` is.
- `T extends { id: string }` is the other way, when every option has an id.

## Common mistakes

**1. Typing options as any.**

```tsx
interface PickListProps {
  options: any[];
  onPick: (option: any) => void;
}
```

It compiles, but the parent gets `any` back, and a typo like `box.lable` slips through. Use a type parameter, `T`.

**2. Showing a generic option directly.**

```tsx
<button>{option}</button>
```

`tsc` refuses it, because a `T` might not be something React can draw. With object options it would also crash with `Objects are not valid as a React child`. Call `renderOption(option)` instead.

**3. Using the index as the key.**

```tsx
{options.map((option, i) => <button key={i}>{renderOption(option)}</button>)}
```

Keys stop belonging to their options when the list changes. Use `key={getKey(option)}`.

**4. Comparing objects with ===.**

```tsx
aria-pressed={selected === option}
```

A selected box loaded again from the server is a different object, so nothing looks pressed. Compare keys: `getKey(selected) === getKey(option)`.

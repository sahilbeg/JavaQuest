## Hook

Sana keeps a swatch book: a few approved colours, a few button shapes, each with a name. When someone asks for "a slightly different blue", she points at the book.

A component library is that book in code. A union type is its list of approved choices. Ask for a style that isn't in the book, and TypeScript refuses before the page is built.

## Concept

### Syntax: className in TSX

```tsx
<button className="btn btn--primary">Add to cart</button>
```

In web-02 you styled pages with classes in a CSS file. React keeps that: `className` takes a string of class names, separated by spaces. The CSS file stays as it is.

```callout jargon
A **modifier class** is an extra class that changes one thing about a base style, like `btn--danger` on top of `btn`. The double dash is a common naming habit.
```

### Syntax: variants as a union

```tsx
type ButtonVariant = "primary" | "secondary" | "danger";

interface ButtonProps {
  variant: ButtonVariant;
}

function Button({ variant }: ButtonProps) {
  return <button className={`btn btn--${variant}`}>Add to cart</button>;
}
```

A plain `variant: string` would accept `"primay"`, and the button would silently lose its colour. With a union, a typo fails:

```tsx
<Button variant="primay" />
```

```text
error TS2820: Type '"primay"' is not assignable to type 'ButtonVariant'. Did you mean '"primary"'?
```

Editors also offer the three choices as you type, so nobody has to remember them.

```callout jargon
A **variant** is one of a fixed set of looks for the same component, like a primary or a danger button.
```

### Building a class list

```tsx
const classes = ["btn", `btn--${variant}`, `btn--${size}`, className].filter(Boolean).join(" ");
```

Classes often come from several places: the base class, the variant, the size, and maybe one extra class from the parent.

- The array holds every part. `className` may be `undefined`.
- `filter(Boolean)` drops the empty ones, so no stray `undefined` ends up in the string.
- `join(" ")` makes one string, like `"btn btn--danger btn--sm wide"`.

```callout tip
Teams often use a tiny library called `clsx` for this. It does the same job: join the class names that are present.
```

### Watch a class list being built

```animate
title: Building the button's classes
kind: code
language: ts
code: |
  type Variant = "primary" | "danger";

  function classes(variant: Variant, extra?: string): string {
    return ["btn", `btn--${variant}`, extra].filter(Boolean).join(" ");
  }

  console.log(classes("primary"));
  console.log(classes("danger", "wide"));
steps:
  - line: 1
    say: Only two variants are allowed. Anything else is a type error.
  - line: 7
    vars: { variant: '"primary"', extra: undefined }
    say: The first call passes no extra class, so extra is undefined.
  - line: 4
    say: The array is btn, btn--primary and undefined. filter(Boolean) drops the undefined.
    out: "btn btn--primary"
  - line: 8
    vars: { variant: '"danger"', extra: '"wide"' }
    say: The second call passes an extra class from the parent.
  - line: 4
    say: Nothing is empty this time, so all three parts are joined with spaces.
    out: "btn btn--danger wide"
```

### Syntax: passing native props through

```tsx
import type { ComponentPropsWithoutRef } from "react";

type ButtonProps = ComponentPropsWithoutRef<"button"> & {
  variant?: ButtonVariant;
};

function Button({ variant = "primary", ...rest }: ButtonProps) {
  return <button className={`btn btn--${variant}`} {...rest} />;
}
```

A real button also needs `onClick`, `disabled`, `aria-label`, `type` and children. Listing them all by hand would take forever.

`ComponentPropsWithoutRef<"button">` is every prop a normal `<button>` accepts. The `&` adds your own on top. `...rest` collects what you didn't name, and `{...rest}` passes it all to the real button.

```callout jargon
**Native props** are the props a plain HTML element accepts, like `disabled` or `onClick`. Passing them through lets your component act like the element it wraps.
```

### Syntax: style objects

```tsx
import type { CSSProperties } from "react";

const style: CSSProperties = { backgroundColor: "#F2A81D", width: 24, height: 24 };

<span className="swatch" style={style} />
```

For a value that changes per item, like a yarn's exact colour, use the `style` prop. It takes an object, not a string:

- property names are camelCase: `backgroundColor`, not `background-color`
- numbers become pixels: `width: 24` means `24px`
- `CSSProperties` checks the names, so a typo like `backgroundColour` is a type error

Use classes for the design, and `style` only for values that come from data.

### Syntax: a Record of colours

```tsx
type Colour = "Marigold" | "Indigo" | "Rani pink" | "Mint" | "Off-white";

const SWATCH_HEX: Record<Colour, string> = {
  Marigold: "#F2A81D",
  Indigo: "#2E3A8C",
  "Rani pink": "#E0218A",
  Mint: "#98D7C2",
  "Off-white": "#FAF7F0",
};
```

`Record<Colour, string>` must have a key for every colour. Add a new yarn colour to `Colour` and forget its hex code, and `tsc` points straight at this object.

## Worked example

A tag that marks items as new, on sale or few left. Each tone has its own class, and the parent may add more.

```tsx
import type { ComponentPropsWithoutRef } from "react";

type TagTone = "new" | "sale" | "low";

type TagProps = ComponentPropsWithoutRef<"span"> & {
  tone: TagTone;
};

const LABELS: Record<TagTone, string> = {
  new: "New",
  sale: "Diwali offer",
  low: "Few left",
};

export function Tag({ tone, className, ...rest }: TagProps) {
  const classes = ["tag", `tag--${tone}`, className].filter(Boolean).join(" ");
  return (
    <span className={classes} {...rest}>
      {LABELS[tone]}
    </span>
  );
}
```

Line by line:

- `TagProps` is every `<span>` prop plus a required `tone`.
- `LABELS` is a `Record`, so each tone must have a label.
- `className` is taken out of the props, so the parent's class is added to the list instead of replacing it.
- `{...rest}` passes anything else, like `title` or `id`, to the span.

## Try it

Run this, then add a `"ghost"` variant to the union and to `LOOKS`.

```ts run
type Variant = "primary" | "secondary" | "danger";

const LOOKS: Record<Variant, string> = {
  primary: "Indigo button with white text",
  secondary: "Off-white button with an Indigo border",
  danger: "Red button for Remove",
};

function describe(variant: Variant, size: "sm" | "md" = "md"): string {
  return `${size === "sm" ? "Small" : "Medium"} ${LOOKS[variant]}`;
}

console.log(describe("primary"));
console.log(describe("danger", "sm"));
```

It prints `Medium Indigo button with white text`, then `Small Red button for Remove`.

## Recap

- `className` takes a string of classes. Build it from parts with `[...].filter(Boolean).join(" ")`.
- Type variants and sizes as unions, so a typo like `"primay"` is a type error.
- `ComponentPropsWithoutRef<"button"> & { ... }` adds your props to every native button prop. Pass the rest on with `{...rest}`.
- The `style` prop takes a `CSSProperties` object: camelCase names, numbers in pixels.
- `Record<Colour, string>` makes `tsc` check that every colour has a value.

## Common mistakes

**1. Typing a variant as string.**

```tsx
interface ButtonProps {
  variant: string;
}
```

`<Button variant="primay" />` compiles, and the button quietly loses its style. Use a union of the allowed variants.

**2. Overwriting the parent's className.**

```tsx
function Button({ variant = "primary", ...rest }: ButtonProps) {
  return <button className={`btn btn--${variant}`} {...rest} />;
}
```

When the parent passes `className="wide"`, it is inside `rest`, and it replaces the button's classes. Take `className` out of the props and add it to the list.

**3. A style string instead of an object.**

```tsx
<span style="background-color: #F2A81D" />
```

`tsc` reports that the string has no properties in common with the style type. Write `style={{ backgroundColor: "#F2A81D" }}`.

**4. Kebab-case style names.**

```tsx
const style: CSSProperties = { "background-color": "#F2A81D" };
```

`tsc` refuses the unknown property. Style objects use camelCase: `backgroundColor`.

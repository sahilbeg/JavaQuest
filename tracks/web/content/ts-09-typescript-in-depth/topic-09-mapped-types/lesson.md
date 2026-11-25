## Hook

Sana has a stencil for price tags. She lays it over any card, sprays once, and every box appears in the right place.

The custom-order form has a name, a phone number, a PIN code and a quantity. Its error messages need one box per field. So does the "has the customer touched this field yet?" list. Writing each of those by hand is how the PIN code got forgotten.

A **mapped type** is a stencil for types. You lay it over a type, and it makes one new field for every field the type has.

## Concept

### Syntax: a mapped type

```ts
type Touched<T> = { [K in keyof T]: boolean };
```

Read it as: "for every key K of T, make a field K whose type is boolean". `keyof T` gives the keys, and `in` loops over them, a bit like `for...of` for types.

```callout jargon
A **mapped type** builds an object type by going over a union of keys, usually `keyof T`, and giving each key a type. `[K in Keys]` is the loop.
```

```ts
interface OrderForm {
  name: string;
  phone: string;
  pincode: string;
  qty: number;
}
type FormTouched = Touched<OrderForm>;
```

`FormTouched` is `{ name: boolean; phone: boolean; pincode: boolean; qty: boolean }`. Add a field to `OrderForm`, and it appears here too, so it can't be forgotten.

### The new type can use the old one

The type of each field may use K, so a mapped type can keep or change the original types.

```ts
type Copy<T> = { [K in keyof T]: T[K] };
type Labels<T> = { [K in keyof T]: string };
type Checkers<T> = { [K in keyof T]: (value: T[K]) => boolean };
```

`Copy` keeps every type as it was, `T[K]`. `Labels` turns every field into a string. `Checkers` gives each field a function that checks a value of that field's own type.

```animate
title: One stencil, laid over every field
kind: scene
nodes:
  - { id: form, label: OrderForm, emoji: "📝", value: "name, phone, pincode, qty", row: 0, col: 0 }
  - { id: map, label: "{ [K in keyof T]?: string }", emoji: "🧩", row: 0, col: 1 }
  - { id: errors, label: "FormErrors<OrderForm>", emoji: "⚠️", value: "empty", row: 0, col: 2 }
steps:
  - say: The form type has four fields. keyof gives their names as a union.
    focus: [form]
  - say: The mapped type takes the first key, name, and makes an optional string field for it.
    send: { from: form, to: map, label: "name" }
    set: { errors: "name?" }
  - say: Then phone, with the same stencil.
    send: { from: form, to: map, label: "phone" }
    set: { errors: "name?, phone?" }
  - say: Then pincode. This is the field the hand-written type forgot.
    send: { from: form, to: map, label: "pincode" }
    set: { errors: "name?, phone?, pincode?" }
  - say: Last, qty. Its type was number, but in the errors type it becomes an optional string message.
    send: { from: form, to: map, label: "qty" }
    set: { errors: "name?, phone?, pincode?, qty?" }
  - say: Add a fifth field to OrderForm, and FormErrors grows by itself. Nothing can drift.
    focus: [errors]
```

### Syntax: modifiers in mapped types

```ts
type FormErrors<T> = { [K in keyof T]?: string };
type Frozen<T> = { readonly [K in keyof T]: T[K] };
type Complete<T> = { [K in keyof T]-?: T[K] };
```

A `?` after the brackets makes every field optional. `readonly` in front makes every field read-only. `-?` removes the question marks.

That's how the utility types from earlier are built. `Partial<T>` is `{ [K in keyof T]?: T[K] }`, and `Readonly<T>` is `{ readonly [K in keyof T]: T[K] }`.

### Syntax: template literal types

```ts
type ItemId = `DC-${number}`;
```

A **template literal type** looks like a template string, but it describes a pattern of strings. `ItemId` is any string that starts with `DC-` and continues with a number.

```callout jargon
A **template literal type** is a string type written with backticks and `${...}` holes. Each hole holds a type, like `number`, `string` or a union of literals.
```

```ts
const golu: ItemId = "DC-1042";
const wrong: ItemId = "AMI-1042";
```

```text
error TS2322: Type '"AMI-1042"' is not assignable to type '`DC-${number}`'.
```

### Template literals with unions

Put a union in a hole, and you get every combination.

```ts
type Colour = "Mint" | "Indigo";
type Size = "S" | "L";
type Variant = `${Colour}-${Size}`;
```

`Variant` is `"Mint-S" | "Mint-L" | "Indigo-S" | "Indigo-L"`. A typo like `"Indgo-S"` is caught.

### Keys at run time

Types disappear when the code runs, so a mapped type can't make the values for you. At run time, you loop over the object's keys with `Object.keys`. It returns `string[]`, so you tell TypeScript they're keys of T.

```ts run
interface OrderForm {
  name: string;
  phone: string;
  pincode: string;
  qty: number;
}
type Touched<T> = { [K in keyof T]: boolean };

function nothingTouched<T extends object>(form: T): Touched<T> {
  const touched = {} as Touched<T>;
  for (const key of Object.keys(form) as (keyof T)[]) {
    touched[key] = false;
  }
  return touched;
}

const form: OrderForm = { name: "Asha", phone: "98765 43210", pincode: "411004", qty: 1 };
console.log(nothingTouched(form));
```

It prints `{ name: false, phone: false, pincode: false, qty: false }`. The `as` on the empty object is needed because it really is empty until the loop fills it.

## Worked example

Sana's brand colours become CSS variables on the page, like `--marigold`. A template literal type keeps every variable name in the right form.

```ts run
type Theme = {
  marigold: string;
  indigo: string;
  offWhite: string;
};
type CssVar = `--${keyof Theme}`;

function toCssVars(theme: Theme): Record<CssVar, string> {
  const vars = {} as Record<CssVar, string>;
  for (const key of Object.keys(theme) as (keyof Theme)[]) {
    const name: CssVar = `--${key}`;
    vars[name] = theme[key];
  }
  return vars;
}

const vars = toCssVars({ marigold: "#F2A81D", indigo: "#2E3A8C", offWhite: "#FAF7F0" });
console.log(vars["--marigold"], vars["--offWhite"]);
console.log(Object.keys(vars).join(" "));
```

Line by line:

1. `keyof Theme` is `"marigold" | "indigo" | "offWhite"`, so `CssVar` is `"--marigold" | "--indigo" | "--offWhite"`.
2. `Record<CssVar, string>` has exactly those three keys, so `vars["--marigld"]` would be a type error.
3. The loop goes over the real keys, and `` `--${key}` `` builds each variable name. Its type is checked against `CssVar`.
4. It prints `#F2A81D #FAF7F0`, then `--marigold --indigo --offWhite`.

## Try it

Write `type Checks<T> = { [K in keyof T]: (value: T[K]) => boolean }` and make a `Checks<OrderForm>` object with a check for each field. Leave one out and read the error.

```ts run
type Size = "S" | "L";
type Colour = "Mint" | "Indigo";
type Variant = `${Colour}-${Size}`;
const picked: Variant = "Mint-L";
console.log(picked);
```

## Recap

- A mapped type, `{ [K in keyof T]: ... }`, makes one field for every key of T, so it follows T as T changes.
- Each field's type can use `T[K]`, the original field's type.
- `?`, `readonly` and `-?` change every field at once. `Partial` and `Readonly` are built this way.
- A template literal type, like `` `DC-${number}` ``, describes a pattern of strings. Unions in its holes make every combination.
- At run time, loop over `Object.keys(obj) as (keyof T)[]` to fill a mapped type's values.

## Common mistakes

**1. Writing the errors type by hand.**

```ts
type OrderErrors = {
  name?: string;
  phone?: string;
  qty?: string;
};
```

When `pincode` was added to the form, this type didn't change, and its message was never shown. Fix: `type OrderErrors = { [K in keyof OrderForm]?: string };`.

**2. Expecting a type to make values.**

```ts
type Touched<T> = { [K in keyof T]: boolean };
const touched: Touched<OrderForm> = {};
```

TypeScript refuses: `name`, `phone`, `pincode` and `qty` are missing. A type only describes values; it can't fill them in. Fix: build the object with a loop over `Object.keys`, as `nothingTouched` does.

**3. Using Object.keys without saying they're keys.**

```ts
function nothingTouched<T extends object>(form: T): Touched<T> {
  const touched = {} as Touched<T>;
  for (const key of Object.keys(form)) {
    touched[key] = false;
  }
  return touched;
}
```

TypeScript refuses `touched[key]`, because `key` is any `string`, not one of T's keys. Fix: `Object.keys(form) as (keyof T)[]`.

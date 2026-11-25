## Hook

The courier who picks up Dhaaga's parcels has one rule: "I'll take any parcel, as long as it has an address label."

He doesn't care what is inside. A coaster set, a tote bag, a box of booties: all fine. But a parcel without a label is refused at the door, because he can't do his job without it.

Generics can have the same kind of rule. "Any type T, as long as it has a `paise` field." That rule is a **constraint**.

## Concept

### A plain T can't assume anything

Inside a generic function, T could be anything: a number, a string, an item. So TypeScript won't let you read a field from it.

```ts
function inStockOnly<T>(list: T[]): T[] {
  return list.filter((item) => item.stock > 0);
}
```

```text
error TS2339: Property 'stock' does not exist on type 'T'.
```

TypeScript is right. Someone could call `inStockOnly(["Mint", "Indigo"])`, and strings have no `stock`.

### Syntax: extends

```ts
function inStockOnly<T extends { stock: number }>(list: T[]): T[] {
  return list.filter((item) => item.stock > 0);
}
```

`T extends { stock: number }` means "T can be any type, as long as it has a `stock` field that is a number". Inside, reading `item.stock` is now allowed.

```callout jargon
A **constraint** limits what a type parameter may be. You write it with `extends` after the parameter's name: `<T extends Shape>`. T must have at least everything `Shape` has.
```

The shape can be an interface name too: `<T extends Stocked>`, where `interface Stocked { stock: number }`.

### The constraint keeps the full type

Why not just write `list: { stock: number }[]`? Because then the function forgets everything else about your items.

```ts
function inStockPlain(list: { stock: number }[]): { stock: number }[] {
  return list.filter((item) => item.stock > 0);
}
const items = [{ id: "DC-1042", name: "Golu the elephant", stock: 3 }];
const left = inStockPlain(items);
console.log(left[0].name);
```

```text
error TS2339: Property 'name' does not exist on type '{ stock: number; }'.
```

With `<T extends { stock: number }>`, T is the whole item type. What comes out still has `id` and `name`.

```ts run
function inStockOnly<T extends { stock: number }>(list: T[]): T[] {
  return list.filter((item) => item.stock > 0);
}
const items = [
  { id: "DC-1042", name: "Golu the elephant", stock: 3 },
  { id: "DC-1047", name: "Baby booties", stock: 0 },
];
const yarn = [
  { colour: "Mint", grams: 0, stock: 0 },
  { colour: "Indigo", grams: 200, stock: 4 },
];
console.log(inStockOnly(items).map((item) => item.name).join(", "));
console.log(inStockOnly(yarn).map((ball) => ball.colour).join(", "));
```

It prints `Golu the elephant`, then `Indigo`. Items and yarn have different shapes, but both have `stock`, so both fit.

### What doesn't fit is refused

```ts
const colours = ["Mint", "Indigo"];
inStockOnly(colours);
```

```text
error TS2345: Argument of type 'string[]' is not assignable to parameter of type '{ stock: number; }[]'.
```

Like the courier at the door, TypeScript refuses the call before the code ever runs.

### Syntax: keyof

```ts
type ItemKey = keyof Item;
```

`keyof` turns an object type into a union of its property names. If `Item` has `id`, `name` and `paise`, then `keyof Item` is `"id" | "name" | "paise"`.

```callout jargon
**keyof** is a type operator. `keyof T` is the union of T's property names, as literal types.
```

### Syntax: indexed access types

```ts
type Price = Item["paise"];
```

Square brackets on a type look up the type of one property. If `paise` is a `number`, then `Item["paise"]` is `number`.

```callout jargon
An **indexed access type**, like `Item["paise"]`, is the type of one property of another type. If the property's type changes, this type follows it.
```

### Syntax: K extends keyof T

```ts
function valueOf<T, K extends keyof T>(obj: T, key: K): T[K] {
  return obj[key];
}
```

Put the two together. K must be one of T's property names, and the result has exactly that property's type, `T[K]`.

```animate
title: K is a key, T[K] is its type
kind: code
language: ts
code: |
  function valueOf<T, K extends keyof T>(obj: T, key: K): T[K] {
    return obj[key];
  }
  const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000 };
  const name = valueOf(golu, "name");
  const paise = valueOf(golu, "paise");
  console.log(name, paise);
steps:
  - line: 4
    vars: { golu: '{ id, name, paise }' }
    say: golu has three fields, so keyof its type is "id" | "name" | "paise".
  - line: 5
    say: "This call infers T as golu's type and K as \"name\". The key is one of the three, so the call is allowed."
  - line: 2
    vars: { key: '"name"' }
    say: Inside, obj[key] reads the field. Its type is T[K], which is T["name"], which is string.
  - line: 5
    vars: { key: ~, name: '"Golu the elephant"' }
    say: So name is a string, not a vague unknown.
  - line: 6
    vars: { paise: 45000 }
    say: Same function, key "paise". Now the result type is T["paise"], a number.
  - line: 7
    out: "Golu the elephant 45000"
    say: Each call got the exact type of the field it asked for.
```

A key that isn't there is caught:

```ts
valueOf(golu, "price");
```

```text
error TS2345: Argument of type '"price"' is not assignable to parameter of type '"id" | "name" | "paise"'.
```

## Worked example

The cart page often needs to find something by its id. Here is one lookup builder for any list whose elements have an `id`.

```ts run
function byId<T extends { id: string }>(list: T[]): Map<string, T> {
  const lookup = new Map<string, T>();
  for (const item of list) {
    lookup.set(item.id, item);
  }
  return lookup;
}

const items = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-1044", name: "Mint keychain", paise: 12000 },
];
const orders = [{ id: "order-17", customer: "Mrs. Rao" }];

console.log(byId(items).get("DC-1044")?.name);
console.log(byId(orders).get("order-17")?.customer);
console.log(byId(items).get("DC-9999")?.name);
```

Line by line:

1. `<T extends { id: string }>` lets the function read `item.id`, and nothing else.
2. The Map's values are T, the full type. So `.get(...)` gives back an item with its `name`, or an order with its `customer`.
3. `?.` is there because `get` returns `T | undefined`: the id might not be in the list.
4. It prints `Mint keychain`, `Mrs. Rao` and `undefined`.

## Try it

Write `priciest<T extends { paise: number }>(list: T[]): T | undefined`, which returns the element with the most paise. Try it on the items above. Then try calling it with `orders`, and read the error TypeScript shows in your editor.

```ts run
function totalPaise<T extends { paise: number }>(list: T[]): number {
  return list.reduce((sum, item) => sum + item.paise, 0);
}
console.log(totalPaise([{ code: "CST", paise: 15000 }, { code: "KEY", paise: 12000 }]));
```

## Recap

- A plain type parameter can't assume anything, so you can't read fields from it.
- `<T extends { paise: number }>` is a constraint: T must have at least those fields, and you may use them.
- A constraint keeps the full type, so what comes out still has every field of what went in.
- `keyof T` is the union of T's property names, and `T["paise"]` is the type of one property.
- `<T, K extends keyof T>` takes a key that must exist on T, and `T[K]` is that property's type.

## Common mistakes

**1. Reading a field from a T with no constraint.**

```ts
function soldOut<T>(list: T[]): T[] {
  return list.filter((item) => item.stock === 0);
}
```

```text
error TS2339: Property 'stock' does not exist on type 'T'.
```

Fix: say what T must have: `<T extends { stock: number }>`.

**2. Replacing T with the constraint's shape.**

```ts
function soldOut(list: { stock: number }[]): { stock: number }[] {
  return list.filter((item) => item.stock === 0);
}
```

It compiles, but every caller loses their fields: the result only has `stock`. Fix: keep T and constrain it, so the function returns `T[]`.

**3. Typing the key as string.**

```ts
const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000 };
function field(key: string) {
  return golu[key];
}
```

```text
error TS7053: Element implicitly has an 'any' type because expression of type 'string' can't be used to index type '{ id: string; name: string; paise: number; }'.
```

Any text could be passed, even `"price"`, so TypeScript can't know the result's type. Fix: take the key as `K extends keyof T`, as `valueOf` does, and return `T[K]`.

## Hook

Meera Aunty keeps one master card for every item: id, name, colour, price and stock. When she makes a price tag, she doesn't write a new card from scratch. She copies the master card and covers up the boxes the tag doesn't need.

If she changes the price on the master card, every copy made from it is right too.

TypeScript has tools that do this with types. They are called **utility types**, and they build new types from one master type.

## Concept

### The problem: copies that drift apart

Here is Dhaaga's master type for an item.

```ts
interface Item {
  id: string;
  name: string;
  colour: string;
  paise: number;
  stock: number;
}
```

The edit form, the product card and the new-item form all need something close to it, but not the same. Writing each one by hand makes copies. When someone renames a field on `Item`, the copies don't change, and the card quietly breaks.

```callout jargon
A **utility type** is a generic type that comes with TypeScript and builds a new type from one you already have. `Partial`, `Pick`, `Omit`, `Record`, `Readonly` and `Required` are the common ones.
```

### Syntax: Partial

```ts
type ItemChanges = Partial<Item>;
```

`Partial<T>` makes every field optional. It is perfect for an edit form, which sends only the fields the person changed.

```ts run
interface Item {
  id: string;
  name: string;
  paise: number;
  stock: number;
}
const golu: Item = { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 3 };
const changes: Partial<Item> = { paise: 42000 };
const updated: Item = { ...golu, ...changes };
console.log(updated.name, updated.paise);
```

It prints `Golu the elephant 42000`. The spread copies `golu`, then the changes replace only what they hold.

### Syntax: Pick and Omit

```ts
type ItemCard = Pick<Item, "id" | "name" | "paise">;
type NewItem = Omit<Item, "id">;
```

`Pick<T, Keys>` keeps only the fields you name. `Omit<T, Keys>` keeps everything except the fields you name. Both take a union of keys, and TypeScript checks that the keys exist.

A product card only needs three fields, so it picks them. A new item has no id until the shop gives it one, so it omits `id`.

```animate
title: Building types from one master type
kind: scene
nodes:
  - { id: item, label: "Item", emoji: "🧶", value: "id, name, colour, paise, stock", row: 0, col: 1 }
  - { id: partial, label: "Partial<Item>", emoji: "✏️", row: 1, col: 0 }
  - { id: pick, label: "Pick<Item, id | name | paise>", emoji: "🏷️", row: 1, col: 1 }
  - { id: omit, label: "Omit<Item, id>", emoji: "🆕", row: 1, col: 2 }
steps:
  - say: Item is the master type. Every other type here is built from it, not written by hand.
    focus: [item]
  - say: Partial copies every field and makes each one optional. The edit form sends only what changed.
    send: { from: item, to: partial, label: "all fields, optional" }
    set: { partial: "id?, name?, colour?, paise?, stock?" }
  - say: Pick copies only the fields you name. The product card gets id, name and paise.
    send: { from: item, to: pick, label: "3 fields" }
    set: { pick: "id, name, paise" }
  - say: Omit copies everything except the fields you name. A new item has no id yet.
    send: { from: item, to: omit, label: "all but id" }
    set: { omit: "name, colour, paise, stock" }
  - say: Now rename colour on Item. All three built types change with it, so nothing drifts apart.
    set: { item: "id, name, shade, paise, stock", partial: "id?, name?, shade?, paise?, stock?", omit: "name, shade, paise, stock" }
    focus: [item, partial, omit]
```

### Syntax: Record

```ts
type StockByColour = Record<string, number>;
type PriceList = Record<"CST" | "KEY" | "AMI", number>;
```

`Record<Keys, Value>` is an object type whose keys are `Keys` and whose values are all `Value`. With a union of literal keys, every key must be there.

```ts run
type Code = "CST" | "KEY" | "AMI" | "BAG" | "BTY";
const prices: Record<Code, number> = {
  CST: 15000,
  KEY: 12000,
  AMI: 45000,
  BAG: 89900,
  BTY: 35000,
};
const cartCodes: Code[] = ["AMI", "CST", "CST"];
let total = 0;
for (const code of cartCodes) {
  total += prices[code];
}
console.log(total);
```

It prints `75000`. Leave `BTY` out of `prices`, and TypeScript says `Property 'BTY' is missing`, so a new item code can never be forgotten.

### Syntax: Readonly and Required

```ts
type FrozenItem = Readonly<Item>;
type FullItem = Required<ItemChanges>;
```

`Readonly<T>` marks every field `readonly`, so it can't be assigned after the object is made. `Required<T>` does the opposite of `Partial`: every optional field becomes required.

```ts
const golu: Readonly<Item> = { id: "DC-1042", name: "Golu the elephant", colour: "Indigo", paise: 45000, stock: 3 };
golu.paise = 1;
```

```text
error TS2540: Cannot assign to 'paise' because it is a read-only property.
```

Like `as const`, this only exists for TypeScript. When the code runs, the object can still change.

### Combining utility types

Utility types are just types, so they nest.

```ts
type ItemUpdate = Partial<Omit<Item, "id">>;
```

Read it from the inside out: take `Item`, drop `id`, then make the rest optional. The edit form may change anything except the id.

## Worked example

A delivery of new stock arrives at the Aundh shop. It only lists the codes that came in.

```ts run
type Code = "CST" | "KEY" | "AMI" | "BAG" | "BTY";
type Stock = Record<Code, number>;

function restock(stock: Readonly<Stock>, delivery: Partial<Stock>): Stock {
  const next: Stock = { ...stock };
  for (const code of Object.keys(delivery) as Code[]) {
    next[code] += delivery[code] ?? 0;
  }
  return next;
}

const aundh: Stock = { CST: 4, KEY: 10, AMI: 2, BAG: 1, BTY: 0 };
const after = restock(aundh, { AMI: 5, BTY: 3 });
console.log(after.AMI, after.BTY, after.KEY);
console.log(aundh.AMI);
```

Line by line:

1. `Stock` is a `Record` with every code, so every count must be there.
2. `delivery` is `Partial<Stock>`: a delivery may bring only some codes.
3. `stock` is `Readonly`, so the function can't change the shop's numbers by mistake. It copies them with a spread instead.
4. `Object.keys` returns `string[]`, so `as Code[]` tells TypeScript these keys are codes. `delivery[code] ?? 0` covers a missing code.
5. It prints `7 3 10`, then `2`: the old stock was never touched.

## Try it

Make a `type PriceTag = Pick<Item, "name" | "paise">` and write `tag(item: Item): PriceTag`. Then try adding `stock` to the object it returns, and read the error.

```ts run
interface Item {
  id: string;
  name: string;
  paise: number;
}
type NewItem = Omit<Item, "id">;
const draft: NewItem = { name: "Indigo tote bag", paise: 89900 };
const saved: Item = { ...draft, id: "DC-1050" };
console.log(saved.id, saved.name);
```

## Recap

- Utility types build new types from one master type, so they can't drift apart from it.
- `Partial<T>` makes every field optional; `Required<T>` makes every field required.
- `Pick<T, "a" | "b">` keeps the named fields; `Omit<T, "a">` keeps the rest.
- `Record<Keys, Value>` is an object with those keys and one value type. With literal keys, every key must be present.
- `Readonly<T>` stops fields being assigned, and utility types nest: `Partial<Omit<Item, "id">>`.

## Common mistakes

**1. Writing a second copy of a type by hand.**

```ts
interface ItemCard {
  id: string;
  name: string;
  color: string;
}
```

When `Item` changes, this copy doesn't, and the card reads a field that no longer exists. Fix: `type ItemCard = Pick<Item, "id" | "name" | "colour">`.

**2. Naming a key that doesn't exist.**

```ts
type ItemCard = Pick<Item, "id" | "price">;
```

```text
error TS2344: Type '"id" | "price"' does not satisfy the constraint 'keyof Item'.
```

`Pick` only takes real keys of `Item`. Fix: use the real name, `"paise"`.

**3. Expecting Partial to fill in missing fields.**

```ts run
interface Item {
  name: string;
  paise: number;
}
const changes: Partial<Item> = { paise: 42000 };
console.log(changes.name);
```

It prints `undefined`. `Partial` only changes the type: the object still has just the fields you gave it. Fix: spread it over a full object, `{ ...item, ...changes }`, before you use it as an `Item`.

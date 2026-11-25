## Hook

Sana keeps one card for every product: a photo, the id, the name, the price and the yarn colours. Every card has the same boxes, in the same places. Anyone can pick up a card and find the price at once.

Objects in code need a card like that too. TypeScript lets you write the shape of an object down once and give it a name. Then every object that claims to be an item must fill in every box.

## Concept

### Syntax: an object type

```ts
let item: { id: string; paise: number };
```

An **object type** lists each property and its type, between curly braces. Separate them with `;` (a `,` works too). Now `item` must have exactly these properties:

```ts
const item: { id: string; paise: number } = { id: "DC-1042" };
```

```text
error TS2741: Property 'paise' is missing in type '{ id: string; }' but required in type '{ id: string; paise: number; }'.
```

```callout jargon
An **object type** describes the shape of an object: which properties it has, and the type of each one.
```

### Syntax: interface

```ts
interface Item {
  id: string;
  name: string;
  paise: number;
}
```

Writing the same object type in every function gets long. An **interface** gives the shape a name. Interface names start with a capital letter, like classes. Use the name anywhere a type goes:

```ts run
interface Item {
  id: string;
  name: string;
  paise: number;
}

function priceTag(item: Item): string {
  return `${item.name}: ₹${(item.paise / 100).toFixed(2)}`;
}

const golu: Item = { id: "DC-1042", name: "Golu the elephant", paise: 45000 };
console.log(priceTag(golu));
```

It prints `Golu the elephant: ₹450.00`. Inside `priceTag`, the editor lists `id`, `name` and `paise` as soon as you type `item.`.

```callout jargon
An **interface** is a named object type. It describes a shape, and every object of that type must fit it. It's only a type, so it's erased before the code runs.
```

### Extra properties are refused

When you write an object literal straight into a typed box, TypeScript also refuses properties the type doesn't have:

```ts
interface Item {
  id: string;
  name: string;
  paise: number;
}
const bag: Item = { id: "DC-2001", name: "Tote bag", paise: 89900, discount: 10 };
```

```text
error TS2353: Object literal may only specify known properties, and 'discount' does not exist in type 'Item'.
```

That sounds strict, but it catches real mistakes: a property nobody will ever read, or a misspelt name like `colour` for `colours`. If items really can have a discount, add it to the interface.

### Syntax: readonly properties

```ts
interface Item {
  readonly id: string;
  paise: number;
}
```

`readonly` before a property means it can be set when the object is made, and never changed after. An item's id should never change, because orders refer to it:

```ts
interface Item {
  readonly id: string;
  paise: number;
}
const golu: Item = { id: "DC-1042", paise: 45000 };
golu.paise = 42000;
golu.id = "DC-9999";
```

```text
error TS2540: Cannot assign to 'id' because it is a read-only property.
```

Changing the price on line 6 is fine. Changing the id on line 7 is the error.

### Objects inside objects

A property's type can be another interface, or an array of them. That's how you describe real data:

```ts
interface Item {
  id: string;
  paise: number;
}
interface OrderLine {
  item: Item;
  qty: number;
}
interface Order {
  id: string;
  lines: OrderLine[];
}
```

`order.lines[0].item.paise` is a `number`, and TypeScript checks every step of that path.

### Syntax: extends

```ts
interface Amigurumi extends Item {
  animal: string;
}
```

`extends` makes a new interface with everything from `Item`, plus more. An `Amigurumi` has `id`, `name`, `paise` and `animal`. Because it has everything an `Item` has, you can pass it anywhere an `Item` is expected:

```animate
title: An Amigurumi goes where an Item is expected
kind: code
language: ts
code: |
  interface Item {
    id: string;
    paise: number;
  }
  interface Amigurumi extends Item {
    animal: string;
  }
  function priceLabel(item: Item): string {
    return `${item.id}: ${item.paise / 100}`;
  }
  const golu: Amigurumi = { id: "DC-1042", paise: 45000, animal: "elephant" };
  console.log(priceLabel(golu));
  console.log(golu.animal);
steps:
  - line: 1
    say: Item describes a shape with an id and a price. Nothing runs here. Interfaces are erased before the code runs.
  - line: 5
    say: Amigurumi takes everything from Item and adds an animal.
  - line: 11
    objects: { o1: { type: Amigurumi, fields: { id: '"DC-1042"', paise: 45000, animal: '"elephant"' } } }
    vars: { golu: "@o1" }
    say: golu must have all three properties. Leave one out, and tsc refuses the line.
  - line: 9
    vars: { item: "@o1" }
    say: priceLabel wants an Item. golu has an id and a price, so it fits. Inside, only id and paise can be used.
  - line: 12
    vars: { item: ~ }
    out: "DC-1042: 450"
    say: The label uses the two Item properties.
  - line: 13
    out: "elephant"
    say: Outside, golu is still an Amigurumi, so its animal is there too.
```

```callout jargon
TypeScript uses **structural typing**: it compares shapes, not names. Any object with the right properties fits a type, whether or not it was built with that type in mind.
```

## Worked example

An order holds a customer and some lines. Here are the three shapes, and a summary built from them:

```ts run
interface Customer {
  name: string;
  phone: string;
}

interface OrderLine {
  code: string;
  paise: number;
  qty: number;
}

interface Order {
  readonly id: string;
  customer: Customer;
  lines: OrderLine[];
}

function orderSummary(order: Order): string {
  let total = 0;
  for (const line of order.lines) {
    total += line.paise * line.qty;
  }
  return `${order.id} for ${order.customer.name}: ${order.lines.length} lines, ₹${(total / 100).toFixed(2)}`;
}

const order: Order = {
  id: "order-17",
  customer: { name: "Asha", phone: "98765 43210" },
  lines: [
    { code: "CST", paise: 15000, qty: 2 },
    { code: "BTY", paise: 35000, qty: 1 },
  ],
};
console.log(orderSummary(order));
```

1. `Customer` and `OrderLine` are small shapes. `Order` is built from them: one customer and an array of lines.
2. The order's `id` is `readonly`, because WhatsApp messages and receipts refer to it.
3. In the loop, `line` is an `OrderLine`, so `line.paise * line.qty` is checked to be number times number.
4. `order.customer.name` is checked at every step. Write `order.customer.nmae`, and `tsc` points at it.
5. It prints `order-17 for Asha: 2 lines, ₹650.00`.

## Try it

Run this. Then add a `colours: string[]` property to `Item`, and see which line `tsc` would complain about until you give `golu` some colours.

```ts run
interface Item {
  readonly id: string;
  name: string;
  paise: number;
}
const golu: Item = { id: "DC-1042", name: "Golu the elephant", paise: 45000 };
console.log(`${golu.id}: ${golu.name}`);
```

## Recap

- An object type like `{ id: string; paise: number }` lists each property and its type.
- An interface gives an object type a name, so every function can share one shape.
- Objects must have every property in the type, and object literals may not add unknown ones.
- `readonly` properties can be set when the object is made, and never changed after.
- `interface Amigurumi extends Item` adds to a shape. TypeScript compares shapes, so an `Amigurumi` fits wherever an `Item` does.

## Common mistakes

**1. Leaving out a property.**

```ts
interface Item {
  id: string;
  paise: number;
}
const key: Item = { id: "DC-0007" };
```

```text
error TS2741: Property 'paise' is missing in type '{ id: string; }' but required in type 'Item'.
```

Every property in the interface must be there. Add `paise: 12000`. If some items really don't have one, you'll learn optional properties in ticket 8.

**2. Changing a readonly property.**

```ts
interface Order {
  readonly id: string;
}
const order: Order = { id: "order-17" };
order.id = "order-18";
```

```text
error TS2540: Cannot assign to 'id' because it is a read-only property.
```

The id is fixed on purpose. Make a new order object instead of renaming an old one.

**3. Adding a property the interface doesn't know.**

```ts
interface Item {
  id: string;
  paise: number;
}
const bag: Item = { id: "DC-2001", paise: 89900, discount: 10 };
```

```text
error TS2353: Object literal may only specify known properties, and 'discount' does not exist in type 'Item'.
```

Either the name is a typo, or the interface is missing a property. Fix whichever is wrong.

**4. Using an interface as if it were a value.**

```ts
interface Item {
  id: string;
}
const thing = { id: "DC-1042" };
console.log(thing instanceof Item);
```

```text
error TS2693: 'Item' only refers to a type, but is being used as a value here.
```

Interfaces are erased before the code runs, so there's nothing to check against at run time. Check the properties instead, which you'll learn in ticket 7.

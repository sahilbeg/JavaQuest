## Hook

Every product on the stall table has a little card. "Name: Amigurumi. Price: ₹450. Colour: Indigo." Each fact has a label in front of it, so nobody mixes up the price with the stock count.

In the last topic, the codes and the prices lived in two separate arrays, matched only by position. One slip and every price shifts. An **object** keeps all the facts about one thing together, each with its own label.

## Concept

```callout jargon
An **object** is a group of labelled values. Each label-and-value pair is a **property**. The label is called the property's **key**.
```

### Syntax: an object literal

```js
const item = { code: "AMI", name: "Amigurumi", paise: 45000 };
```

Curly braces make an object. Inside, each property is `key: value`, with commas between them. A longer object reads better over several lines:

```js
const item = {
  code: "AMI",
  name: "Amigurumi",
  paise: 45000,
};
```

The comma after the last property is allowed, and it makes adding the next one easier.

### Syntax: reading a property with a dot

```js
const name = item.name;
```

Write the object, a dot, and the key. You get the value stored under that label.

```js run
const item = { code: "AMI", name: "Amigurumi", paise: 45000 };
console.log(item.name);
console.log(item.paise / 100);
console.log(item.colour);
```

This prints `Amigurumi`, then `450`, then `undefined`. There's no `colour` property, so reading it gives `undefined` rather than an error.

### Syntax: square brackets

```js
const value = item[key];
```

Square brackets take the key as a string. Use them when the key is held in a variable, like a code a customer typed:

```js run
const prices = { CST: 15000, KEY: 12000 };
const code = "KEY";
console.log(prices[code]);
console.log(prices["CST"]);
console.log(prices.code);
```

This prints `12000`, `15000` and `undefined`. `prices[code]` uses the value inside `code`, which is `"KEY"`. But `prices.code` looks for a property literally named `code`, and there isn't one.

### Syntax: changing and adding properties

```js
item.stock = 3;
```

Assigning to a property changes it. If the property doesn't exist yet, it's added. Watch a cart line change:

```animate
title: A cart line changes
kind: code
language: js
code: |
  const line = { code: "AMI", paise: 45000, qty: 1 };
  line.qty = line.qty + 1;
  line.giftWrap = true;
  const cost = line.paise * line.qty;
  console.log(cost);
  console.log(line.colour);
steps:
  - line: 1
    objects: { l1: { type: Object, fields: { code: '"AMI"', paise: 45000, qty: 1 } } }
    vars: { line: "@l1" }
    say: An object with three labelled properties. The box line points to it, just like a box points to an array.
  - line: 2
    objects: { l1: { type: Object, fields: { code: '"AMI"', paise: 45000, qty: 2 } } }
    say: The customer wants a second amigurumi. line.qty reads 1, adds 1, and stores 2 back under the same label.
  - line: 3
    objects: { l1: { type: Object, fields: { code: '"AMI"', paise: 45000, qty: 2, giftWrap: true } } }
    say: There was no giftWrap property, so assigning one adds it.
  - line: 4
    vars: { cost: 90000 }
    say: Read two properties and multiply. 45000 times 2 is 90000.
  - line: 5
    out: "90000"
    say: The cost of the line, in paise.
  - line: 6
    out: undefined
    say: There is no colour property, so reading it gives undefined.
```

`const line` keeps `line` pointing at the same object, but the object's properties can still change. It works just like a `const` array.

### Objects inside objects

A property's value can be another object. That's how a whole price list fits in one box:

```js run
const catalogue = {
  AMI: { name: "Amigurumi", paise: 45000 },
  KEY: { name: "Keychain", paise: 12000 },
};
console.log(catalogue.AMI.name);
console.log(catalogue["KEY"].paise);
```

This prints `Amigurumi`, then `12000`. Read it left to right: from the catalogue, take `AMI`, then from that, take `name`.

### Arrays of objects

A cart is an array of lines, and each line is an object. A loop walks through them:

```js run
const cart = [
  { code: "KEY", qty: 2 },
  { code: "CST", qty: 1 },
];
let items = 0;
for (const line of cart) {
  items = items + line.qty;
}
console.log(items + " items in " + cart.length + " lines");
```

This prints `3 items in 2 lines`. Each round, `line` is one whole object, so `line.qty` reads its quantity.

### Syntax: shorthand properties

```js
const line = { code, qty };
```

When a property should have the same name as the variable holding its value, write the name once. `{ code, qty }` means `{ code: code, qty: qty }`.

### Syntax: Object.keys

```js
const codes = Object.keys(catalogue);
```

`Object.keys` gives an array of an object's keys, in the order they were added.

```js run
const catalogue = { CST: 15000, KEY: 12000, AMI: 45000 };
const codes = Object.keys(catalogue);
console.log(codes.join(", "));
console.log(codes.length);
```

This prints `CST, KEY, AMI`, then `3`.

### Printing an object

`console.log` shows a small object with its keys and values:

```js run
const line = { code: "AMI", qty: 2 };
console.log(line);
```

This prints `{ code: 'AMI', qty: 2 }`. Like arrays, objects are held by reference, so two separate objects are never `===`, even with the same properties.

## Worked example

The catalogue and an order, priced together:

```js run
const catalogue = {
  CST: { name: "Coaster set (4)", paise: 15000 },
  BAG: { name: "Tote bag", paise: 89900 },
};
const order = [
  { code: "BAG", qty: 1 },
  { code: "CST", qty: 2 },
];
let total = 0;
for (const line of order) {
  const item = catalogue[line.code];
  const cost = item.paise * line.qty;
  console.log(`${line.qty} x ${item.name}: ${cost} paise`);
  total = total + cost;
}
console.log(`Total: ${total} paise`);
```

1. `catalogue` maps each code to an object with a name and a price.
2. Each round, `catalogue[line.code]` uses brackets, because the code is inside a variable.
3. Round 1 prints `1 x Tote bag: 89900 paise`. Round 2 prints `2 x Coaster set (4): 30000 paise`.
4. The last line prints `Total: 119900 paise`.

## Try it

Run it. Then add a `stock: 3` property, print it, and take 1 away from it after a sale.

```js run
const item = { code: "DC-1042", name: "Golu the elephant", paise: 45000 };
item.colour = "Indigo";
console.log(`${item.name} (${item.code}) in ${item.colour}: ${item.paise / 100} rupees`);
```

## Recap

- An object groups labelled values: `{ code: "AMI", paise: 45000 }`.
- Read a property with a dot, `item.paise`, or with brackets when the key is in a variable, `catalogue[code]`.
- Assigning to a property changes it, or adds it if it wasn't there.
- A missing property is `undefined`, and reading a property of `undefined` crashes.
- Objects can hold objects, and arrays can hold objects, like a cart of lines.

## Common mistakes

**1. Using a dot when the key is in a variable.**

```js
const prices = { KEY: 12000 };
const code = "KEY";
console.log(prices.code);
```

This prints `undefined`, because it looks for a property called `code`. Use brackets: `prices[code]`.

**2. Reading a property of something that isn't there.**

```js run crash
const catalogue = { CST: { paise: 15000 } };
const item = catalogue["BAG"];
console.log(item.paise);
```

JavaScript says `TypeError: Cannot read properties of undefined (reading 'paise')`. There's no `BAG`, so `item` is `undefined`. Check first: `if (item === undefined) { ... }`.

**3. A misspelt property name.**

```js
const line = { code: "AMI", paise: 45000, qty: 2 };
console.log(line.price * line.qty);
```

This prints `NaN`. There's no `price` property, so it's `undefined`, and `undefined * 2` is `NaN`. Use the exact key: `line.paise`.

**4. A missing comma between properties.**

```js nocompile
const item = {
  code: "AMI"
  paise: 45000
};
```

JavaScript stops with a `SyntaxError` that points at `paise`. Put a comma after every property: `code: "AMI",`.

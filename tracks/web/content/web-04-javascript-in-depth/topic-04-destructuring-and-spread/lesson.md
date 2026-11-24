## Hook

A box of yarn arrives at the Aundh shop. You don't carry the whole box around all day. You take out the two balls you need and put them on the table.

Code often gets a big object, like a catalogue item, when it only needs two of its fields. Destructuring is how you take out just those. Spread does the opposite: it pours everything from one box into a new one.

## Concept

### Syntax: object destructuring

```js
const { name, paise } = item;
```

This makes two variables, `name` and `paise`, and fills them from the properties with the same names. It is a short way to write `const name = item.name;` and `const paise = item.paise;`.

```js run
const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000 };
const { name, paise } = golu;
console.log(name);
console.log(paise);
```

```callout jargon
**Destructuring** means unpacking values from an object or array into separate variables, in one statement.
```

### Renaming and default values

```js
const { paise: price, colour = "Off-white" } = item;
```

`paise: price` takes the `paise` property but names the variable `price`. `colour = "Off-white"` is a default: it is used when the item has no `colour`.

```js run
const keychain = { id: "DC-1044", paise: 12000 };
const { paise: price, colour = "Off-white" } = keychain;
console.log(price, colour);
```

It prints `12000 Off-white`. A default is used only when the value is `undefined`, meaning missing. A value of `null` or `""` is kept as it is.

```callout jargon
A **default value** is the value a variable gets when the thing it was unpacked from is `undefined`.
```

### Destructuring in a parameter list

You can destructure right where a function receives its argument. The function then reads like a list of the fields it needs.

```js
function priceTag({ name, paise }) {
  return name + ": " + paise + " paise";
}
```

Calling `priceTag(golu)` unpacks `golu` on the way in. Fields the function doesn't name are simply ignored.

### Syntax: array destructuring

```js
const [first, second] = items;
```

Arrays are unpacked by position, not by name. The first variable gets item 0, the next gets item 1, and so on.

```js run
const prices = [12000, 15000, 45000];
const [cheapest, next] = prices;
console.log(cheapest, next);
let a = "Marigold";
let b = "Indigo";
[a, b] = [b, a];
console.log(a, b);
```

It prints `12000 15000`, then `Indigo Marigold`. The last trick swaps two variables without a third one.

### Syntax: rest in destructuring

```js
const [first, ...others] = items;
```

The three dots gather everything left over into a new array. With objects, they gather the leftover properties into a new object.

```js run
const [first, ...others] = ["DC-1042", "DC-1043", "DC-1044"];
console.log(first);
console.log(others.join(", "));
const { id, ...details } = { id: "DC-1045", code: "BAG", paise: 89900 };
console.log(id, Object.keys(details).join(", "));
```

`others` holds the last two ids. `details` holds every property except `id`, so its keys are `code, paise`. The rest part must always come last.

```callout jargon
**Rest** syntax (`...name`) collects "the rest" of the values into one array or object.
```

### Syntax: spread

```js
const all = [...stall, ...shop];
const copy = { ...item, stock: 0 };
```

The same three dots, used where values are being built, spread a collection out. `[...stall, ...shop]` makes one new array from two. `{ ...item, stock: 0 }` makes a new object with every property of `item`, then sets `stock`.

```js run
const stall = ["CST", "KEY"];
const shop = ["AMI", "BAG"];
const everything = [...stall, ...shop, "BTY"];
console.log(everything.join(", "));

const golu = { id: "DC-1042", paise: 45000, stock: 3 };
const onSale = { ...golu, paise: 40500 };
console.log(onSale.paise, golu.paise, onSale.stock);
```

It prints `CST, KEY, AMI, BAG, BTY`, then `40500 45000 3`. The original `golu` still costs 45000. When the same property appears twice, the later one wins.

```callout jargon
**Spread** syntax (`...value`) expands an array or object in place, so its items or properties are copied into a new array, object or function call.
```

### Rest and spread, side by side

Watch spread copy an object, and destructuring unpack the copy.

```animate
title: Copy with spread, unpack with destructuring
kind: code
language: js
code: |
  const golu = { id: "DC-1042", paise: 45000, stock: 3 };
  const sold = { ...golu, stock: 2 };
  const { id, stock } = sold;
  console.log(id, stock, golu.stock);
steps:
  - line: 1
    objects: { o1: { type: Object, fields: { id: '"DC-1042"', paise: 45000, stock: 3 } } }
    vars: { golu: "@o1" }
    say: golu points to an item object with three properties.
  - line: 2
    objects: { o2: { type: Object, fields: { id: '"DC-1042"', paise: 45000, stock: 3 } } }
    say: The spread copies every property of golu into a brand new object.
  - line: 2
    objects: { o2: { type: Object, fields: { id: '"DC-1042"', paise: 45000, stock: 2 } } }
    vars: { sold: "@o2" }
    say: Then stock 2 is written after the copy, so it wins. sold points to the new object.
  - line: 3
    vars: { id: '"DC-1042"', stock: 2 }
    say: Destructuring makes two variables from sold, using the property names id and stock.
  - line: 4
    out: "DC-1042 2 3"
    say: The new object says 2 in stock. The original golu still says 3, because spread made a copy.
```

### Syntax: rest parameters

```js
function totalOf(...prices) {}
```

In a parameter list, `...prices` gathers every argument into one array. The function can then take any number of prices.

```js run
function totalOf(...prices) {
  let sum = 0;
  for (const p of prices) {
    sum = sum + p;
  }
  return sum;
}
console.log(totalOf(15000, 12000));
console.log(totalOf(45000, 3000, 5000));
console.log(totalOf());
const prices = [15000, 89900, 12000];
console.log(Math.max(...prices));
```

It prints `27000`, `53000`, `0` and `89900`. The last line is spread again: it hands the array's items to `Math.max` as separate arguments.

```callout tip
The dots mean "gather" when they receive values (on the left of `=`, or in a parameter list). They mean "spread" when they hand values out (in an array, an object or a call).
```

## Worked example

A short stock tag for each item, with a default for items that haven't been counted yet.

```js run
function shortTag({ code, paise, stock = 0 }) {
  return code + " ₹" + (paise / 100).toFixed(2) + ", " + stock + " left";
}

function totalOf(...prices) {
  return prices.reduce((sum, p) => sum + p, 0);
}

const golu = { id: "DC-1042", code: "AMI", paise: 45000, stock: 3 };
const newBag = { id: "DC-1048", code: "BAG", paise: 89900 };

console.log(shortTag(golu));
console.log(shortTag(newBag));

const restocked = { ...newBag, stock: 6 };
console.log(shortTag(restocked));

const [cheap, ...rest] = [12000, 15000, 45000];
console.log(cheap, rest.length);
console.log(totalOf(15000, 12000, 3000));
```

Line by line:

1. `shortTag` unpacks three fields in its parameter list. `stock` falls back to 0 when it is missing.
2. Golu has stock, so the first tag is `AMI ₹450.00, 3 left`.
3. The new bag has no `stock` property, so the default gives `BAG ₹899.00, 0 left`.
4. `{ ...newBag, stock: 6 }` makes a new object with stock added. `newBag` itself is unchanged.
5. Array destructuring takes 12000 as `cheap` and gathers the other two into `rest`, so it prints `12000 2`.
6. `totalOf` gathers three arguments into an array and adds them: `30000`.

## Try it

Give `golu` a `colour` of `"Indigo"`. Then write a `colourTag({ name, colour = "Off-white" })` function and call it on both items.

```js run
const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000 };
const plainKey = { id: "DC-1044", name: "Owl keychain", paise: 12000 };
const { name, paise } = golu;
console.log(name + " costs " + paise + " paise");
const both = [golu, plainKey];
const [firstItem, secondItem] = both;
console.log(secondItem.name);
```

## Recap

- `const { name, paise } = item;` unpacks properties by name. `paise: price` renames, and `colour = "Off-white"` sets a default for missing values.
- `const [first, second] = list;` unpacks by position.
- Rest (`...others`) gathers leftovers into an array or object, and it must come last.
- Spread (`[...a, ...b]`, `{ ...item, stock: 0 }`) copies into something new. A later property wins.
- A rest parameter (`function totalOf(...prices)`) takes any number of arguments as one array.

## Common mistakes

**1. Destructuring something that is undefined.**

```js run crash
const items = [{ id: "DC-1042", name: "Golu the elephant" }];
const { name } = items.find((item) => item.id === "DC-9999");
console.log(name);
```

Node stops with a `TypeError` that starts `Cannot destructure property 'name'`. `find` returned `undefined`, and there is nothing to unpack. Fix: check the result first, or write `items.find(...) ?? {}` so there is always an object.

**2. Putting the spread after the new value.**

```js run
const golu = { id: "DC-1042", stock: 3 };
const soldOut = { stock: 0, ...golu };
console.log(soldOut.stock);
```

It prints `3`, not `0`. The spread came last, so golu's `stock` overwrote the new one. Fix: spread first, then change: `{ ...golu, stock: 0 }`.

**3. Putting the rest part anywhere but last.**

```js nocompile
const [...others, last] = ["DC-1042", "DC-1043"];
```

Node refuses to run it: `SyntaxError: Rest element must be last element`. Fix: rest only ever goes at the end. To get the last item, use `items[items.length - 1]`.

**4. Expecting a default to replace null.**

```js run
const { colour = "Off-white" } = { colour: null };
console.log(colour);
```

It prints `null`. Defaults only replace `undefined`. Fix: if your data uses `null` for "no colour", write `const shown = colour ?? "Off-white";`.

## Hook

Meera Aunty keeps a yarn register: one line per colour, with the grams left. When new yarn arrives, she finds the colour's line and adds to it. She never writes Indigo on two lines.

She also keeps a guest list for the Diwali pop-up. If someone signs twice, they still count once. JavaScript has a built-in tool for each of these: a `Map` for the register, and a `Set` for the guest list.

## Concept

### Syntax: Map

```js
const yarn = new Map();
yarn.set("Marigold", 500);
yarn.get("Marigold");
```

A `Map` stores pairs: a key, and the value for that key. `set` adds or replaces a pair, `get` reads a value, `has` checks for a key, `delete` removes one, and `size` counts the pairs.

```js run
const yarn = new Map();
yarn.set("Marigold", 500);
yarn.set("Indigo", 320);
yarn.set("Marigold", 460);
console.log(yarn.get("Marigold"));
console.log(yarn.has("Mint"), yarn.size);
yarn.delete("Indigo");
console.log([...yarn.keys()].join(", "));
```

It prints `460`, then `false 2`, then `Marigold`. Setting Marigold a second time replaced its value; it didn't add a second Marigold.

```callout jargon
A **Map** is a collection of **key**-value pairs, where each key appears once. One pair is called an **entry**.
```

### Counting with a Map

A Map is perfect for counting. For each thing, read its count so far, add 1, and store it back. A key that isn't there yet gives `undefined`, so `?? 0` starts it at zero.

```js run
const sold = ["CST", "AMI", "CST", "KEY", "CST"];
const counts = new Map();
for (const code of sold) {
  counts.set(code, (counts.get(code) ?? 0) + 1);
}
console.log(counts.get("CST"), counts.get("BAG"), counts.size);
```

It prints `3 undefined 3`. Three coaster sets were sold, no bags, and there are three different codes.

### Syntax: looping over a Map

```js
for (const [key, value] of map) {
  // one entry at a time
}
```

A `for...of` loop over a Map gives one entry at a time, as a `[key, value]` pair. Array destructuring unpacks it. Entries come out in the order their keys were first added.

```js run
const counts = new Map([["CST", 3], ["AMI", 1], ["KEY", 1]]);
for (const [code, count] of counts) {
  console.log(code + ": " + count);
}
console.log([...counts.values()].join(" + "));
```

It prints three lines, then `3 + 1 + 1`. `new Map([[key, value], ...])` builds a Map from a list of pairs. `keys()`, `values()` and `entries()` give the parts, and spreading them into `[...]` makes an array.

### Any value can be a key

An ordinary object turns every key into text. A Map keeps keys exactly as they are, so the number `1` and the text `"1"` are different keys.

```js run
const byNumber = {};
byNumber[1] = "one";
console.log(Object.keys(byNumber)[0] === "1");
const map = new Map();
map.set(1, "number one");
map.set("1", "text one");
console.log(map.size, map.get(1), map.get("1"));
```

It prints `true`, then `2 number one text one`. The object quietly turned `1` into `"1"`; the Map kept both.

### Syntax: Set

```js
const ids = new Set();
ids.add("DC-1042");
ids.has("DC-1042");
```

A `Set` is a collection of values with no repeats. `add` puts a value in, but adding one that is already there does nothing. `has` checks membership, `delete` removes, and `size` counts.

```js run
const picked = new Set();
picked.add("DC-1042");
picked.add("DC-1045");
picked.add("DC-1042");
console.log(picked.size);
console.log(picked.has("DC-1045"), picked.has("DC-1047"));
```

It prints `2`, then `true false`. Golu was added twice but counted once.

```callout jargon
A **Set** is a collection where every value appears at most once. Checking `has` on a Set is fast, even when it holds thousands of values.
```

### Removing duplicates

Because a Set keeps each value once, it is the shortest way to drop repeats from an array. Watch it happen to a list of colours.

```animate
title: A Set keeps each colour once
kind: code
language: js
code: |
  const colours = ["Indigo", "Marigold", "Indigo", "Mint"];
  const seen = new Set();
  for (const colour of colours) {
    seen.add(colour);
  }
  console.log(seen.size, [...seen].join(", "));
steps:
  - line: 1
    vars: { colours: '["Indigo", "Marigold", "Indigo", "Mint"]' }
    say: Four colours, one per item. Indigo appears twice.
  - line: 2
    vars: { seen: "Set {}" }
    say: An empty Set to collect them.
  - line: 4
    vars: { colour: '"Indigo"', seen: 'Set { "Indigo" }' }
    say: Indigo is new, so the Set adds it.
  - line: 4
    vars: { colour: '"Marigold"', seen: 'Set { "Indigo", "Marigold" }' }
    say: Marigold is new as well.
  - line: 4
    vars: { colour: '"Indigo"' }
    say: Indigo is already in the Set. add does nothing, and no error happens.
  - line: 4
    vars: { colour: '"Mint"', seen: 'Set { "Indigo", "Marigold", "Mint" }' }
    say: Mint is new. The Set now holds three colours, in the order they first arrived.
  - line: 6
    vars: { colour: ~ }
    out: "3 Indigo, Marigold, Mint"
    say: Spreading the Set into an array gives each colour once.
```

The one-line version is `[...new Set(colours)]`.

### Map or object?

Both hold pairs, so which one should you pick?

- Use an **object** for one thing with a fixed set of fields, like an item: `{ id, name, paise }`. You know the field names when you write the code.
- Use a **Map** for a lookup that grows while the program runs, with keys you don't know in advance: grams per colour, count per code, item per id.

One catch: `JSON.stringify` doesn't understand Maps. Turn one into a plain object first.

```js run
const yarn = new Map([["Marigold", 460], ["Indigo", 320]]);
console.log(JSON.stringify(yarn));
console.log(JSON.stringify(Object.fromEntries(yarn)));
```

It prints `{}`, then `{"Marigold":460,"Indigo":320}`. `Object.fromEntries` turns the Map's pairs into an object's properties.

## Worked example

After a day of crocheting, update the yarn register and see what to reorder.

```js run
const yarnStock = new Map([
  ["Marigold", 460],
  ["Indigo", 320],
  ["Rani pink", 150],
  ["Mint", 90],
]);
const todaysToys = [
  { name: "Golu the elephant", colour: "Indigo", grams: 40 },
  { name: "Mint turtle", colour: "Mint", grams: 40 },
  { name: "Golu the elephant", colour: "Indigo", grams: 40 },
  { name: "Rani pink bunny", colour: "Rani pink", grams: 40 },
];

const used = new Map();
for (const toy of todaysToys) {
  used.set(toy.colour, (used.get(toy.colour) ?? 0) + toy.grams);
}
for (const [colour, grams] of used) {
  yarnStock.set(colour, yarnStock.get(colour) - grams);
}

const kinds = new Set(todaysToys.map((toy) => toy.name));
console.log(kinds.size + " kinds of toy today");
for (const [colour, grams] of yarnStock) {
  const warning = grams < 100 ? " (order more)" : "";
  console.log(colour + ": " + grams + " g" + warning);
}
```

Line by line:

1. `yarnStock` is a Map built from pairs: colour to grams.
2. The first loop counts grams used per colour: Indigo 80, Mint 40, Rani pink 40.
3. The second loop takes each colour's use away from its stock. Setting an existing key keeps its place in the order.
4. `new Set(...)` of the toy names drops the second Golu, so it prints `3 kinds of toy today`.
5. The last loop prints Marigold 460 g, Indigo 240 g, Rani pink 110 g, and `Mint: 50 g (order more)`.

## Try it

Add a `"Marigold"` coaster set using 60 g to `todaysToys`, and check that Marigold drops to 400 g. Then print the list of colours used today, each once, with `[...used.keys()].join(", ")`.

```js run
const used = new Map();
const toys = [
  { colour: "Indigo", grams: 40 },
  { colour: "Mint", grams: 40 },
  { colour: "Indigo", grams: 40 },
];
for (const toy of toys) {
  used.set(toy.colour, (used.get(toy.colour) ?? 0) + toy.grams);
}
console.log(used.get("Indigo"), used.size);
```

## Recap

- A `Map` holds key-value pairs, each key once: `set`, `get`, `has`, `delete` and `size`. A missing key gives `undefined`.
- `for (const [key, value] of map)` loops over the entries in the order the keys were first added.
- A `Set` holds values with no repeats: `add`, `has`, `delete` and `size`. `[...new Set(list)]` removes duplicates.
- Use an object for a record with known fields, and a Map for a growing lookup. Map keys can be any value.
- `JSON.stringify` can't write a Map. Use `Object.fromEntries(map)` first.

## Common mistakes

**1. Using square brackets on a Map.**

```js run
const yarn = new Map();
yarn["Marigold"] = 460;
console.log(yarn.get("Marigold"), yarn.size);
```

It prints `undefined 0`. The brackets stuck an ordinary property on the Map object, outside its entries. Fix: `yarn.set("Marigold", 460)` and `yarn.get("Marigold")`.

**2. Adding to a key that isn't there yet.**

```js run
const counts = new Map();
counts.set("CST", counts.get("CST") + 1);
console.log(counts.get("CST"));
```

It prints `NaN`. `counts.get("CST")` was `undefined`, and `undefined + 1` is not a number. Fix: `(counts.get("CST") ?? 0) + 1`.

**3. Expecting a Set to spot two equal-looking objects.**

```js run
const picked = new Set();
picked.add({ id: "DC-1042" });
picked.add({ id: "DC-1042" });
console.log(picked.size);
```

It prints `2`. These are two different objects, and a Set compares objects by reference, like `===`. Fix: store the ids instead, `picked.add(item.id)`.

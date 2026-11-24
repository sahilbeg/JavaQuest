## Hook

Sana wants to add a new Mint turtle to the catalogue. Today that means asking an engineer to edit code. She'd rather edit a simple file that lists every item, and have the website read it.

That file needs a format both people and programs can read. The web's favourite one is JSON. Every online shop, weather app and bank sends data as JSON.

## Concept

### What JSON is

JSON is a way to write data as plain text. It looks almost exactly like JavaScript objects and arrays, which is why it is easy to read.

```json
[
  { "id": "DC-1042", "name": "Golu the elephant", "paise": 45000, "stock": 3, "handmade": true },
  { "id": "DC-1043", "name": "Marigold coaster set", "paise": 15000, "stock": 12, "handmade": true }
]
```

But it is text. A program can't use it until it has been turned into real arrays and objects.

```callout jargon
**JSON** (JavaScript Object Notation) is a text format for data. It is how browsers, servers and files pass structured data around.
```

### Syntax: the rules of JSON

```json
{ "text": "Golu", "number": 45000, "yes": true, "nothing": null, "list": [1, 2], "object": { "a": 1 } }
```

JSON is stricter than JavaScript:

- Every key is in **double** quotes. Text values use double quotes too. Single quotes are not allowed.
- Values can only be text, numbers, `true`, `false`, `null`, arrays and objects.
- No trailing comma after the last item, and no comments.
- No `undefined`, no functions and no dates. A date has to be written as text.

### Syntax: JSON.parse

```js
const items = JSON.parse(text);
```

`JSON.parse` reads JSON text and builds the real values it describes.

```js run
const text = '[{"id":"DC-1042","paise":45000},{"id":"DC-1045","paise":89900}]';
const items = JSON.parse(text);
console.log(typeof text, items.length);
console.log(items[1].id, items[1].paise + 100);
```

It prints `string 2`, then `DC-1045 90000`. After parsing, `paise` is a real number you can add to.

```callout jargon
To **parse** is to read text and turn it into structured values. The opposite, turning values into text, is to **serialize**.
```

### Syntax: JSON.stringify

```js
const text = JSON.stringify(value);
const pretty = JSON.stringify(value, null, 2);
```

`JSON.stringify` turns values into JSON text, ready to save in a file or send to a server. The optional third argument is the indent, which makes the text easier for people to read.

```js run
const golu = { id: "DC-1042", stock: 3 };
console.log(JSON.stringify(golu));
console.log(JSON.stringify(golu, null, 2));
```

The first line is compact: `{"id":"DC-1042","stock":3}`. The second spreads the same data over four lines, indented by 2 spaces. The `null` in the middle is a slot for a filter, which you rarely need.

### Round trip: text, values, text

Most JSON work follows one pattern. Parse the text, work with the values, then stringify the result.

```animate
title: Parse, change, stringify
kind: code
language: js
code: |
  const text = '{"id":"DC-1042","stock":3}';
  const item = JSON.parse(text);
  item.stock = item.stock - 1;
  const saved = JSON.stringify(item);
  console.log(typeof text, typeof item);
  console.log(saved);
steps:
  - line: 1
    vars: { text: "'{\"id\":\"DC-1042\",\"stock\":3}'" }
    say: The data arrives as one string of text. You can't do maths on it yet.
  - line: 2
    objects: { o1: { type: Object, fields: { id: '"DC-1042"', stock: 3 } } }
    vars: { item: "@o1" }
    say: JSON.parse reads the text and builds a real object. stock is now a number.
  - line: 3
    objects: { o1: { type: Object, fields: { id: '"DC-1042"', stock: 2 } } }
    say: Golu sold one, so the stock goes down by 1.
  - line: 4
    vars: { saved: "'{\"id\":\"DC-1042\",\"stock\":2}'" }
    say: JSON.stringify turns the object back into text, ready to be saved.
  - line: 5
    out: "string object"
    say: text is a string, and item is an object.
  - line: 6
    out: '{"id":"DC-1042","stock":2}'
    say: The saved text carries the new stock.
```

### What JSON leaves out

Some JavaScript values have no JSON form. `JSON.stringify` quietly drops them, or changes them.

```js run
const item = {
  id: "DC-1048",
  colour: undefined,
  label() {
    return "Mint turtle";
  },
  added: new Date(Date.UTC(2026, 9, 31)),
  rating: NaN,
};
console.log(JSON.stringify(item));
```

It prints `{"id":"DC-1048","added":"2026-10-31T00:00:00.000Z","rating":null}`. `colour` and `label` are gone, the date became text, and `NaN` became `null`. When you parse that text back, `added` is a string, not a date.

### When the text is broken

If the text breaks a JSON rule, `JSON.parse` throws a `SyntaxError`. One stray comma is enough. Data from a file or a server can always be broken, so parse it inside `try`.

```js run
const broken = '[{"id":"DC-1042","stock":3},]';
try {
  JSON.parse(broken);
  console.log("loaded");
} catch (error) {
  console.log(error.name);
  console.log("The catalogue file has a mistake in it");
}
```

It prints `SyntaxError`, then the friendly line. The exact wording of a `SyntaxError` message changes between browsers and Node versions, so don't show it to customers, and don't test for it.

```callout jargon
**Invalid JSON** is text that breaks a JSON rule, such as a trailing comma or single quotes. Parsing it always throws a `SyntaxError`.
```

### Where JSON text comes from

In the browser, JSON usually comes from a server, which you'll fetch in a later project. In Node, you can read a file's text first, then parse it.

```js
import { readFileSync } from "node:fs";
const text = readFileSync(new URL("./catalogue.json", import.meta.url), "utf8");
const items = JSON.parse(text);
```

Either way, the steps are the same: get the text, then parse it.

## Worked example

Loading Sana's catalogue text, checking it, and saving the stock counts.

```js run
const CATALOGUE_JSON = `[
  { "id": "DC-1042", "name": "Golu the elephant", "paise": 45000, "stock": 3 },
  { "id": "DC-1044", "name": "Mint owl keychain", "paise": 12000, "stock": 0 },
  { "id": "DC-1045", "name": "Indigo tote bag", "paise": 89900, "stock": 5 }
]`;

function loadCatalogue(text) {
  let data;
  try {
    data = JSON.parse(text);
  } catch (error) {
    throw new Error("catalogue.json is not valid JSON");
  }
  if (!Array.isArray(data)) {
    throw new Error("catalogue.json should hold a list of items");
  }
  return data;
}

const items = loadCatalogue(CATALOGUE_JSON);
const inStock = items.filter((item) => item.stock > 0).map((item) => item.name);
console.log(inStock.join(", "));

const counts = items.map((item) => ({ id: item.id, stock: item.stock }));
console.log(JSON.stringify(counts));

try {
  loadCatalogue('{"id": "DC-1042"}');
} catch (error) {
  console.log(error.message);
}
```

Line by line:

1. The JSON text sits in a template literal, the backtick string, so it can span several lines.
2. `loadCatalogue` parses inside `try`. A `SyntaxError` becomes a clear error that names the file.
3. `Array.isArray` checks that the text held a list. A single object would break the page later, so it is refused now.
4. The items are real objects, so `filter` and `map` work. It prints `Golu the elephant, Indigo tote bag`.
5. The stock counts are saved as compact JSON: `[{"id":"DC-1042","stock":3},{"id":"DC-1044","stock":0},{"id":"DC-1045","stock":5}]`.
6. Text holding one object is refused with `catalogue.json should hold a list of items`.

## Try it

Break the JSON on purpose: add a comma after the last item, or swap a double quote for a single one. Run it and see which line prints. Then add a `"colour"` to Golu and print it after parsing.

```js run
const text = '[{"id":"DC-1042","name":"Golu the elephant","stock":3}]';
try {
  const items = JSON.parse(text);
  console.log(items[0].name + ": " + items[0].stock + " left");
} catch (error) {
  console.log("Broken JSON: " + error.name);
}
```

## Recap

- JSON is a text format for data. Keys and text use double quotes, and there are no trailing commas, comments, functions or `undefined`.
- `JSON.parse(text)` turns JSON text into real arrays and objects.
- `JSON.stringify(value)` turns values into JSON text. `JSON.stringify(value, null, 2)` indents it for people.
- Stringify drops `undefined` and functions, turns dates into text and `NaN` into `null`.
- Broken text makes `JSON.parse` throw a `SyntaxError`, so parse data from outside inside `try`.

## Common mistakes

**1. Writing JSON with single quotes.**

```js run crash
const text = "{'id': 'DC-1042'}";
const item = JSON.parse(text);
```

Node stops with a `SyntaxError`. Single quotes are fine in JavaScript, but not in JSON. Fix: `'{"id": "DC-1042"}'`, or build an object and let `JSON.stringify` write the text for you.

**2. Using the text as if it were already an object.**

```js run
const text = '{"id":"DC-1042","stock":3}';
console.log(text.stock);
```

It prints `undefined`. A string has no `stock` property. Fix: parse first, `JSON.parse(text).stock`.

**3. Parsing something that is already an object.**

```js run crash
const item = { id: "DC-1042", stock: 3 };
const again = JSON.parse(item);
```

Node stops with a `SyntaxError`. `JSON.parse` turned the object into the text `[object Object]` first, which isn't JSON. Fix: only parse text. If you already have the object, just use it.

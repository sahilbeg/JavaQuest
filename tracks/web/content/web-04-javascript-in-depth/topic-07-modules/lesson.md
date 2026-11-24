## Hook

At first, Meera Aunty kept everything in one notebook: prices, orders, yarn, phone numbers. Soon nobody could find anything, and two people couldn't write in it at once. So she split it: a price book, an order book, a yarn book. Each book does one job, and you look things up in the book that owns them.

Code grows the same way. One long file becomes hard to read and hard to share. Modules let you split code into files that each own one job, and borrow from each other on purpose.

## Concept

### Each file is a module

In modern JavaScript, every file can be a module. A module keeps its own variables to itself. Nothing inside it can be seen from another file, unless the module chooses to share it.

```callout jargon
A **module** is one file of code with its own private scope. It shares chosen values with **export**, and borrows values from other modules with **import**.
```

That privacy is the point. `prices.js` can have a helper called `round` and `format.js` can have a different `round`. They never clash.

### Syntax: named exports

```js
export const PRICES = { CST: 15000, KEY: 12000 };
export function priceOf(code) {
  return PRICES[code] ?? 0;
}
```

Put `export` in front of a `const`, `let`, `function` or `class` to share it. Each one is shared under its own name, so these are called named exports. A file can have as many as it likes.

```callout jargon
A **named export** is a value a module shares under its own name. Other files must ask for it by that exact name.
```

### Syntax: named imports

```js
import { priceOf, PRICES } from "./prices.js";
```

List the names you want in braces, then say which file they come from. The names must match the exports exactly, including capital letters.

The path starts with `./`, meaning "the same folder as this file". Use `../` to go up one folder. In the browser and in Node, write the `.js` ending too.

You can rename on the way in, when a name would clash: `import { priceOf as basePrice } from "./prices.js";`.

### Syntax: default export

```js
export default function rupees(paise) {
  return "₹" + (paise / 100).toFixed(2);
}
```

A module may also have one default export: its main thing. It is imported without braces, and the importing file picks the name.

```js
import rupees from "./format.js";
import showMoney from "./format.js";   // the same function, under another name
```

```callout jargon
A **default export** is the one main value of a module, marked `export default`. A file can have at most one.
```

### Syntax: import a whole module

```js
import * as prices from "./prices.js";
```

This gives you one object, `prices`, holding every named export of the module. You then write `prices.priceOf("CST")` or `prices.PRICES`. It is handy when you use many things from one module, and it shows clearly where each name comes from.

```callout jargon
A **namespace import** (`import * as name`) gathers all of a module's named exports into one object.
```

### How the pieces connect

When the page loads `main.js`, its imports pull in the other files. Each module runs once, and every importer shares that one copy.

```animate
title: The receipt modules load each other
kind: scene
nodes:
  - { id: main, label: main.js, emoji: "▶️", row: 0, col: 1 }
  - { id: receipt, label: receipt.js, emoji: "🧾", row: 1, col: 1 }
  - { id: prices, label: prices.js, emoji: "🏷️", row: 2, col: 0 }
  - { id: format, label: format.js, emoji: "₹", row: 2, col: 2 }
steps:
  - say: The page starts main.js. Before its code runs, JavaScript reads its imports.
    focus: [main]
  - say: main.js imports receiptLine from receipt.js, so receipt.js is loaded first.
    send: { from: main, to: receipt, label: "import { receiptLine }" }
  - say: receipt.js needs two things of its own. It imports the price module and the rupee formatter.
    send:
      - { from: receipt, to: prices, label: "import * as prices" }
      - { from: receipt, to: format, label: "import rupees" }
  - say: prices.js and format.js import nothing, so they run first. They hand back only what they export.
    send:
      - { from: prices, to: receipt, label: "priceOf, PRICES", back: true }
      - { from: format, to: receipt, label: "rupees (default)", back: true }
    set: { prices: ready, format: ready }
  - say: Now receipt.js runs, and gives main.js its export.
    send: { from: receipt, to: main, label: "receiptLine", back: true }
    set: { receipt: ready }
  - say: Last, main.js runs. Each module ran exactly once, however many files import it.
    set: { main: running }
```

### Module scope: private unless exported

Anything you don't export stays inside the file. That is how you hide helpers that other files shouldn't depend on.

```js
const formatter = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });

export default function rupees(paise) {
  return formatter.format(paise / 100);
}
```

Other files can use `rupees`, but they can't see `formatter`. You can change or rename it later without breaking anyone.

Imported names are read-only. A file that imports `PRICES` can read it, but can't assign `PRICES = ...`. The module that owns a value is the only one that may change it.

### Modules in the browser

```html
<script type="module" src="main.js"></script>
```

The browser treats a script as a module only when you add `type="module"`. Without it, `import` and `export` cause a `SyntaxError`. Tools like Vite, which you'll meet later, take care of this for big projects.

## Worked example

Here are three small modules and the file that uses them.

```js
// prices.js
export const PRICES = { CST: 15000, KEY: 12000, AMI: 45000, BAG: 89900, BTY: 35000 };
export const GIFT_WRAP_PAISE = 3000;
export function priceOf(code) {
  return PRICES[code] ?? 0;
}
```

```js
// format.js
const formatter = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });
export default function rupees(paise) {
  return formatter.format(paise / 100);
}
```

```js
// gift.js
import rupees from "./format.js";
import { priceOf, GIFT_WRAP_PAISE } from "./prices.js";

export function giftLine(code) {
  const paise = priceOf(code) + GIFT_WRAP_PAISE;
  return code + " gift-wrapped: " + rupees(paise);
}
```

```js
// main.js
import { giftLine } from "./gift.js";
console.log(giftLine("AMI"));
```

Line by line:

1. `prices.js` shares the price list, the wrap price and `priceOf`, all as named exports.
2. `format.js` keeps `formatter` private and shares `rupees` as its default export.
3. `gift.js` imports `rupees` without braces, because it is a default. It imports `priceOf` and `GIFT_WRAP_PAISE` with braces, because they are named.
4. `giftLine("AMI")` adds 45000 and 3000, then formats 48000 paise.
5. Running `main.js` prints `AMI gift-wrapped: ₹480.00`.

## Try it

These lines are one file, so they run here. Imagine each comment marks a new file. Change `giftLine` to also add ₹50 for a stitched name, then decide which file that price should live in.

```js run
// prices.js
const PRICES = { CST: 15000, KEY: 12000, AMI: 45000, BAG: 89900, BTY: 35000 };
const GIFT_WRAP_PAISE = 3000;
function priceOf(code) {
  return PRICES[code] ?? 0;
}

// format.js
function rupees(paise) {
  return "₹" + (paise / 100).toFixed(2);
}

// gift.js
function giftLine(code) {
  return code + " gift-wrapped: " + rupees(priceOf(code) + GIFT_WRAP_PAISE);
}

console.log(giftLine("AMI"));
console.log(giftLine("KEY"));
```

## Recap

- Every file is a module with its own private scope. Nothing leaves it unless it is exported.
- `export` in front of a declaration makes a named export. Import it with braces and the exact name: `import { priceOf } from "./prices.js";`.
- `export default` marks a module's one main value. Import it without braces, under any name you like.
- `import * as prices from "./prices.js"` gathers all named exports into one object.
- Paths start with `./` or `../` and end in `.js`. In the browser, the script tag needs `type="module"`.

## Common mistakes

**1. Using braces for a default export.**

```js
import { rupees } from "./format.js";
```

The file fails to load with `SyntaxError: The requested module './format.js' does not provide an export named 'rupees'`. `rupees` was exported as the default, not by name. Fix: `import rupees from "./format.js";`.

**2. Leaving out the ./ or the .js.**

```js
import { priceOf } from "prices";
```

The browser says `Failed to resolve module specifier "prices"`, and Node says `Cannot find package 'prices'`. A bare name means a package, like one from npm. Fix: `import { priceOf } from "./prices.js";`.

**3. Forgetting to export.**

```js
const DELIVERY_PAISE = 6000;
```

Another file that writes `import { DELIVERY_PAISE } from "./prices.js";` fails with `does not provide an export named 'DELIVERY_PAISE'`. Fix: `export const DELIVERY_PAISE = 6000;`.

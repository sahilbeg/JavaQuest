## The design

Today every page does its own maths and its own formatting. Three copies of the same idea drift apart, and each one has its own bugs. The fix is one small toolbox that every page imports, with tests that prove each tool works.

```callout jargon
A **utility** is a small, general function that many parts of a program use, like formatting a price. A **utilities package** keeps them together in one folder, with one file that everything imports from.
```

The toolbox has four tools in three files, and one front door:

```text
src/
  money.js    formatRupees, paiseFromRupees
  text.js     slugify
  pin.js      isPunePin
  index.js    the front door: re-exports all four
```

Pages never reach into `money.js` directly. They import from the front door:

```js
import { formatRupees, isPunePin } from "./utils/index.js";
```

```callout jargon
An **entry point** is the one file other code imports a package through. Behind it, you can move or rename files freely, and nobody else's imports break. A published npm package names its entry point in `package.json`, for example `"exports": "./src/index.js"`.
```

```animate
title: One front door for the toolbox
kind: scene
nodes:
  - { id: cart, label: Cart page, emoji: "🛒", row: 0, col: 0 }
  - { id: whatsapp, label: WhatsApp link, emoji: "💬", row: 0, col: 1 }
  - { id: delivery, label: Delivery check, emoji: "🚚", row: 0, col: 2 }
  - { id: index, label: index.js, emoji: "🚪", row: 1, col: 1 }
  - { id: money, label: money.js, emoji: "₹", row: 2, col: 0 }
  - { id: pin, label: pin.js, emoji: "📮", row: 2, col: 2 }
steps:
  - say: Before, each page had its own copy of the price formatting, and each copy was a little different.
    focus: [cart, whatsapp, delivery]
  - say: Now every page imports from one file, index.js, the toolbox's front door.
    send: [{ from: cart, to: index, label: formatRupees }, { from: whatsapp, to: index, label: formatRupees }, { from: delivery, to: index, label: isPunePin }]
  - say: index.js passes each name on to the file that really holds it.
    send: [{ from: index, to: money, label: "export … from" }, { from: index, to: pin, label: "export … from" }]
  - say: Fix a bug in money.js once, and every page gets the fix. The tests check it before any page does.
    focus: [money]
```

### Syntax: re-exporting

```js
export { name } from "./file.js";
```

This line imports `name` from `./file.js` and exports it again, in one step. `index.js` is just a few of these lines.

### Syntax: rupees with Intl.NumberFormat

```js
const rupees = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });
```

`Intl.NumberFormat` knows how each country writes money. `"en-IN"` is Indian English, so it adds the ₹ sign, two decimals, and Indian grouping: lakhs, not millions.

```js run
const rupees = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" });
console.log(rupees.format(129900 / 100));
console.log(rupees.format(15000000 / 100));
```

The second line prints `₹1,50,000.00`: one lakh and fifty thousand rupees. Make the formatter once, outside the function, and reuse it. Making one is slow compared with using one.

### Reading money that people type

People type amounts in every style: `450`, `₹450`, `1,299`, `₹1,299.50`. JavaScript's own number readers are no help:

```js run
console.log(parseFloat("1,299.50"));
console.log(Number("1,299.50"));
console.log(Number("₹450"));
```

`parseFloat` stops at the comma and says `1`. `Number` gives `NaN`. So read it yourself, as text:

1. Trim the spaces. Remove a leading `₹` and every comma.
2. Check what is left with a regular expression: digits, then maybe a `.` and one or two more digits.
3. Rupees times 100, plus the decimals as paise. `"5"` after the dot means 50 paise, so pad it to two digits.

No floating point is involved, so `₹1,299.50` is always exactly 129950 paise.

```callout jargon
A **regular expression** is a pattern for text, written between slashes. `/^\d+$/` means "from start (`^`) to end (`$`), one or more (`+`) digits (`\d`)". Brackets `( )` capture a part you want to keep.
```

### Syntax: slugs

```js
"Golu the Elephant!".normalize("NFD").replace(/[̀-ͯ]/g, "");
```

```callout jargon
A **slug** is the part of a web address that names one page, in small letters, digits and dashes: `dhaaga.in/p/golu-the-elephant`. It must be safe in any address.
```

To make one from a product name:

1. `normalize("NFD")` splits a letter like `é` into `e` plus an accent mark. The `replace` above removes the marks, so `Café` becomes `Cafe`.
2. Lower-case everything.
3. Turn every run of characters that are not `a-z` or `0-9` into a single `-`.
4. Remove any `-` at the start or end.

### Pune PIN codes

Indian PIN codes are six digits. For this ticket, a Pune PIN is one that starts with `411`, like `411038` in Kothrud. People sometimes type a space after the first three digits: `411 038`.

A PIN is text, not a number. Read from a form, it is always text. And a PIN stored as a number loses any leading zeros, so treat anything that is not text as "not a Pune PIN".

## What you build

Follow the numbered TODOs. The checks are in four files in `test/`: one per tool, and one for the front door.

1. **`formatRupees(paise)`** in `src/money.js`: whole paise to text, with `Intl.NumberFormat("en-IN", ...)`.
   - `15000` gives `"₹150.00"`, `129900` gives `"₹1,299.00"`, `5` gives `"₹0.05"`, `15000000` gives `"₹1,50,000.00"`.
   - Anything that is not a whole number, like `12.5` or `"15000"`, throws a `TypeError`.
2. **`paiseFromRupees(text)`** in `src/money.js`: typed rupees to whole paise.
   - `"450"`, `"₹450"` and `"₹ 450"` give `45000`. `"₹1,299.50"` and `"1299.5"` give `129950`. `"0.05"` gives `5`.
   - Text that is not an amount (`""`, `"abc"`, `"-5"`, `"12.345"`, `"₹"`) throws a `RangeError`. Anything that is not text throws a `TypeError`.
3. **`slugify(name)`** in `src/text.js`: `"Golu the Elephant!"` gives `"golu-the-elephant"`, `"  Rani pink coaster set (4) "` gives `"rani-pink-coaster-set-4"`, `"Café Tote"` gives `"cafe-tote"`, and `"!!!"` gives `""`.
4. **`isPunePin(pin)`** in `src/pin.js`: `true` for six digits starting `411`, with an optional space after the third digit and spaces around it ignored. `false` for anything else, including numbers like `411038`.
5. **`src/index.js`** exports exactly these four functions, and nothing else.

## Recap

- A utilities package keeps small shared functions in one tested place. Fix a bug once, and every page gets the fix.
- One entry point, `index.js`, re-exports the public tools with `export { name } from "./file.js"`.
- Show money with `Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" })`, from whole paise divided by 100.
- Read typed money as text, with a regular expression, never with `parseFloat`.
- A slug is lower-case letters, digits and single dashes. A PIN is text, never a number.

## Common mistakes

**Reading money with parseFloat.** `parseFloat("1,299.50")` is `1`, and `Math.round(parseFloat(...) * 100)` brings floating point back in. Match the text with a regular expression and build the paise from the two parts.

**Forgetting to pad the decimals.** `"1299.5"` has one digit after the dot. Adding `Number("5")` gives 129905 paise, which is ₹1,299.05. Pad it: `"5".padEnd(2, "0")` is `"50"`.

**Making the formatter inside the function.** `new Intl.NumberFormat(...)` on every call works, but it is slow on a catalogue with hundreds of prices. Make it once, at the top of `money.js`.

**Exporting helpers from the front door.** If `index.js` also exports a private helper, other pages start using it, and you can never change it again. Export only the four public tools.

**Stripping only the first dash.** `.replace("-", "")` removes one dash. Use a regular expression with `+` and the `g` flag, like `/^-+|-+$/g`, to remove every one at both ends.

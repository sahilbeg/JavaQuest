## Hook

A smoke alarm with its battery taken out is very quiet. Nobody is woken at night by burnt toast. Nobody is woken by a real fire either.

TypeScript's checks are the alarm. Every `any`, `as` and `!` takes a battery out of one corner of the house. This ticket is about putting all the batteries in, keeping them in, and knowing which switches in `tsconfig.json` control them.

## Concept

### What strict mode is

```callout jargon
**tsconfig.json** is the file that tells the TypeScript compiler, `tsc`, how to check your project. Each setting in it is a **compiler option**.
```

`"strict": true` is one option that turns on a whole family of checks. The ones you'll meet most:

| Check | What it catches |
|---|---|
| `strictNullChecks` | using a value that might be `null` or `undefined` |
| `noImplicitAny` | a value whose type TypeScript can't work out, quietly becoming `any` |
| `useUnknownInCatchVariables` | treating whatever was thrown as an `Error` |
| `strictFunctionTypes` | passing a callback that can't handle every value it will get |
| `strictPropertyInitialization` | a class field that is never given a value |

New strict checks join the family in new TypeScript versions. So a project with `"strict": true` gets stricter as it upgrades. That's on purpose: each new check finds real bugs.

The checks in these tickets run with `"strict": true`. Every new project should start with it.

### strictNullChecks: missing is its own type

With `strictNullChecks`, `null` and `undefined` are not hiding inside every type. A function that might find nothing says so in its type, and you must handle it.

```ts
const item = ITEMS.find((i) => i.id === "DC-9999");
console.log(item.name);
```

```text
error TS18048: 'item' is possibly 'undefined'.
```

`find` returns `Item | undefined`. The fix is a check, which narrows the type:

```ts
const item = ITEMS.find((i) => i.id === "DC-9999");
if (item === undefined) {
  console.log("Not in the catalogue");
} else {
  console.log(item.name);
}
```

Without the option, the first version compiles, and crashes in the customer's browser.

### noImplicitAny: no silent any

```ts
function lineTotal(line) {
  return line.paise * line.qty;
}
```

```text
error TS7006: Parameter 'line' implicitly has an 'any' type.
```

```callout jargon
**any** is the type that switches checking off. Anything can go in, and you can do anything with it. **Implicit** any is an `any` you never wrote: TypeScript gave up guessing and used it quietly.
```

`noImplicitAny` turns the quiet guess into an error, so every parameter gets a real type.

### Beyond strict: noUncheckedIndexedAccess

One very useful option is **not** part of `strict`. Look at this:

```ts
const PRICES: Record<string, number> = { AMI: 45000, BAG: 89900 };
const price = PRICES["XYZ"];
```

TypeScript says `price` is a `number`. It's really `undefined`. With `"noUncheckedIndexedAccess": true`, every lookup by index, like `PRICES[code]` or `lines[0]`, has `| undefined` added, so you must check it.

Lookups by text have a second trap. Every object inherits keys like `toString` from JavaScript itself:

```animate
title: Looking up a code a customer typed
kind: code
language: ts
code: |
  const PRICES: Record<string, number> = { AMI: 45000, BAG: 89900 };
  const typed = "toString";
  console.log(typed in PRICES);
  console.log(Object.hasOwn(PRICES, typed));
  console.log(PRICES["XYZ"]);
  console.log(PRICES["XYZ"] / 100);
steps:
  - line: 1
    say: PRICES has two keys of its own, AMI and BAG. Its type says every lookup gives a number.
  - line: 2
    vars: { typed: '"toString"' }
    say: A customer types toString into the price checker.
  - line: 3
    out: "true"
    say: The in operator says yes. Every object inherits a toString function, and in looks at inherited keys too.
  - line: 4
    out: "false"
    say: Object.hasOwn looks only at PRICES' own keys. toString isn't one of them.
  - line: 5
    out: "undefined"
    say: A missing key gives undefined, though the type promised a number.
  - line: 6
    out: "NaN"
    say: Maths on undefined gives NaN, and the page shows ₹NaN. noUncheckedIndexedAccess would have forced a check here.
```

### Syntax: a type predicate

```ts
function isItemCode(text: string): text is ItemCode {
  return Object.hasOwn(PRICES, text);
}
```

You met type guards in [Type guards](/t/ts-09-typescript-in-depth/topic-05-type-guards). The return type `text is ItemCode` means: when this returns `true`, treat `text` as an `ItemCode`. After `if (isItemCode(code))`, the lookup `PRICES[code]` is safe and typed. The check and the type travel together.

### as and ! switch the checks off

```callout jargon
A **type assertion**, `value as ItemCode`, tells the compiler what a value is, without checking. A **non-null assertion**, `value!`, tells it the value is not `null` or `undefined`, without checking.
```

Both are promises you make to the compiler. If the promise is wrong, nothing warns you:

```ts
const price = PRICES[typed as ItemCode];
const name = ITEMS.find((i) => i.id === id)!.name;
```

The first line gives a function for `toString`, and `undefined` for `XYZ`. The second crashes with "Cannot read properties of undefined" when the id is missing. In each case the honest version is a check: a type predicate, or an `if`.

```callout tip
When the compiler complains, ask "could it be right?" before you reach for as or !. Usually it is.
```

### catch gives you unknown

JavaScript can `throw` anything: an `Error`, a string, a number, even `null`. So with `useUnknownInCatchVariables`, the variable in `catch (error)` is `unknown`.

```callout jargon
**unknown** is the safe opposite of `any`. Anything can go in, but you can't use it until you check what it is.
```

```ts
try {
  payByUpi();
} catch (error) {
  const message = error instanceof Error ? error.message : "Payment failed.";
  showAlert(message);
}
```

`error instanceof Error` narrows `unknown` to `Error`, so `.message` is allowed. Writing `error.message` straight away is error TS18046: `'error' is of type 'unknown'`.

### The tsconfig options that matter

Here is the tsconfig these tickets are checked with, one line per option:

```json
{
  "compilerOptions": {
    "strict": true,
    "noEmit": true,
    "target": "es2022",
    "lib": ["es2023", "dom", "dom.iterable"],
    "module": "esnext",
    "moduleResolution": "bundler",
    "jsx": "react-jsx",
    "skipLibCheck": true,
    "allowImportingTsExtensions": true
  },
  "include": ["src/**/*"]
}
```

| Option | What it means |
|---|---|
| `strict` | turn on the whole strict family |
| `noEmit` | only check; another tool, like Vite, builds the JavaScript |
| `target` | which JavaScript version the output is written for |
| `lib` | which built-in types exist: `es2023` for `Array.prototype.findLast`, `dom` for `document` |
| `module`, `moduleResolution` | how `import` lines are found; `bundler` matches Vite |
| `jsx` | how JSX is compiled; `react-jsx` means no `import React` |
| `skipLibCheck` | don't re-check the types inside `node_modules` |
| `include` | which files belong to the project |

Many teams add `noUncheckedIndexedAccess` and `noUnusedLocals` on top of `strict`.

## Worked example

Kavya turned `strict` on for an old helper from the yarn shelf. It had compiled for a year:

```ts
function gramsLeft(stock, colour) {
  return stock[colour] - 40;
}
```

Strict mode found two problems:

```text
error TS7006: Parameter 'stock' implicitly has an 'any' type.
error TS7006: Parameter 'colour' implicitly has an 'any' type.
```

Here is the honest version:

```ts
type Colour = "Marigold" | "Indigo" | "Rani pink" | "Mint" | "Off-white";

function gramsLeft(stock: Partial<Record<Colour, number>>, colour: Colour): number | null {
  const grams = stock[colour];
  if (grams === undefined) return null;
  return grams - 40;
}
```

Line by line:

- `Colour` lists the five real colours, so `"Pink"` is a type error at the call.
- `Partial<Record<Colour, number>>` says some colours may be missing from the stock. So `stock[colour]` is typed `number | undefined`.
- The check handles the missing colour. The old code returned `NaN` for it, and the stock screen said "NaN g left".
- The return type `number | null` warns every caller that there might be no answer.

## Try it

This looks items up with `find`, and handles the one that's missing. Run it.

```ts run
type Item = { id: string; name: string; paise: number };

const ITEMS: Item[] = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-1301", name: "Rani pink tote bag", paise: 89900 },
];

function priceLabel(id: string): string {
  const item = ITEMS.find((i) => i.id === id);
  if (item === undefined) return id + ": not in the catalogue";
  return item.name + ": " + item.paise + " paise";
}

console.log(priceLabel("DC-1042"));
console.log(priceLabel("DC-9999"));
```

It prints:

```text
Golu the elephant: 45000 paise
DC-9999: not in the catalogue
```

1. Delete the `if` line and write `item!.name` instead. Run it. What happens to `DC-9999`?
2. In an editor with strict on, delete the `if` line without adding `!`. Which error appears?

## Recap

- `"strict": true` turns on a family of checks, including `strictNullChecks`, `noImplicitAny` and `useUnknownInCatchVariables`. Start every project with it.
- With `strictNullChecks`, "might be missing" is part of the type, and a check narrows it away.
- `noUncheckedIndexedAccess` isn't part of `strict`, but adds `| undefined` to lookups like `PRICES[code]`. Use `Object.hasOwn`, not `in`, to test for an object's own keys.
- `as` and `!` are unchecked promises. Prefer a real check: an `if`, `instanceof`, or a type predicate.
- Know the main tsconfig options: `strict`, `noEmit`, `target`, `lib`, `module`, `moduleResolution`, `jsx`, `skipLibCheck` and `include`.

## Common mistakes

**1. Silencing a null check with !.**

```ts
const golu = ITEMS.find((i) => i.id === "DC-1042")!;
```

Symptom: it works until Golu is out of the catalogue, then the page crashes with "Cannot read properties of undefined (reading 'name')". Fix: `if (golu === undefined) { ... }`, and show something sensible.

**2. Casting what a person typed.**

```ts
const price = PRICES[typed.toUpperCase() as ItemCode];
```

Symptom: `XYZ` gives `undefined`, and the screen shows ₹NaN. Fix: `if (isItemCode(code)) return PRICES[code]; return null;`.

**3. Reading .message on a caught value.**

```ts
try { payByUpi(); } catch (error) { showAlert(error.message); }
```

Symptom: error TS18046, `'error' is of type 'unknown'`. Forcing it with `as Error` crashes when someone throws a string. Fix: `error instanceof Error ? error.message : "Something went wrong."`.

**4. Testing for a key with in.**

```ts
const price = typed in PRICES ? PRICES[typed] : null;
```

Symptom: `toString` passes the check, and the price is a function. Fix: `Object.hasOwn(PRICES, typed)`, which looks only at the object's own keys.

**5. Turning strict off to make the errors go away.**

```json
{ "compilerOptions": { "strict": false } }
```

Symptom: the error list is empty, and every bug it listed is still there, waiting for a customer. Fix: keep `strict` on and fix errors one file at a time. [Moving a JavaScript file to TypeScript](/t/tsreact-12-professional-typescript/topic-04-migrating-to-typescript) shows how teams do that safely.

## Hook

A crochet pattern book has a page at the front: "Abbreviations. sc = single crochet, ch = chain". Every pattern in the book uses them, and nobody rewrites them on every page.

A TypeScript project works the same way. One file holds the shared types, every other file imports them, and one settings file at the top, `tsconfig.json`, tells the checker how strict to be.

## Concept

### Modules work as before

`import` and `export` from web-04 work the same in `.ts` files. The new part is that **types** can be exported and imported too:

```ts
export type ItemCode = "CST" | "KEY" | "AMI" | "BAG" | "BTY";

export interface CartLine {
  code: ItemCode;
  qty: number;
}
```

Put this in `src/types.ts`, and every file can share one `ItemCode`. A file that holds only types becomes completely empty once the types are stripped.

### Syntax: import type

```ts
import type { CartLine } from "./types.ts";
```

`import type` says "this import is only a type". It's removed completely before the code runs, so no file is loaded for it. You can also mark one name inside a normal import:

```ts
import { priceOf, type ItemCode } from "./prices.ts";
```

Why bother, when TypeScript could work it out? Fast tools like Vite and Node's type stripping look at **one file at a time**. When they see `import { CartLine }`, they can't open `types.ts` to find out whether `CartLine` is a type or a value. The word `type` tells them it's safe to delete the line.

```callout jargon
A **type-only import** (`import type`) brings in names that are only types. It's erased before the code runs, so it never loads the other file at run time.
```

### File extensions in imports

In Dhaaga's projects, imports name the real file, `.ts` included: `"./types.ts"`. Node runs `.ts` files that way, and Vite understands it. Older projects often write `"./types"` or even `"./types.js"`. Which one works depends on `tsconfig.json`, so copy the style the project already uses.

Watch what happens to each import when the calculator is built:

```animate
title: Type imports vanish in the build
kind: scene
nodes:
  - { id: types, label: types.ts, emoji: "🏷️", value: "types only" }
  - { id: prices, label: prices.ts, emoji: "💰" }
  - { id: quote, label: quote.ts, emoji: "🧾" }
  - { id: tsc, label: tsc --noEmit, emoji: "🔍", value: idle }
  - { id: bundle, label: Vite bundle, emoji: "📦", value: empty }
steps:
  - say: quote.ts imports the CartLine type from types.ts, and the priceOf function from prices.ts.
    send: [{ from: types, to: quote, label: "import type { CartLine }" }, { from: prices, to: quote, label: "import { priceOf }" }]
  - say: tsc reads all three files together, so it can check every call across them.
    send: [{ from: types, to: tsc, label: read }, { from: prices, to: tsc, label: read }, { from: quote, to: tsc, label: read }]
    set: { tsc: "0 errors" }
  - say: Vite works one file at a time. In quote.ts, it deletes the import type line without opening types.ts.
    focus: [quote]
    set: { types: "not needed" }
  - say: The bundle holds the JavaScript of prices.ts and quote.ts. types.ts adds nothing, because all of it was types.
    send: [{ from: prices, to: bundle, label: "priceOf" }, { from: quote, to: bundle, label: "quoteLine" }]
    set: { bundle: "plain JS" }
```

### Syntax: tsconfig.json

```json
{
  "compilerOptions": {
    "strict": true,
    "noEmit": true,
    "target": "es2022",
    "module": "esnext",
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "lib": ["es2023", "dom"]
  },
  "include": ["src"]
}
```

`tsconfig.json` sits in the project's top folder, next to `package.json`. `tsc` reads it every time it runs, and so does your editor.

| Setting | What it means |
|---|---|
| `strict` | Switch on the full family of strict checks. Always `true` for new code. |
| `noEmit` | Only check. Don't write `.js` files, because Vite builds them. |
| `target` | Which JavaScript version the code may use, like `es2022`. |
| `module` | How files import each other. `esnext` means `import` and `export`. |
| `moduleResolution` | How an import path finds its file. `bundler` means "the way Vite does it". |
| `allowImportingTsExtensions` | Allows `"./types.ts"` in imports. It needs `noEmit`. |
| `lib` | Which built-in names exist: `es2023` for JavaScript's, `dom` for `document` and `window`. |
| `include` | Which folders to check. |

```callout jargon
**tsconfig.json** is the settings file for TypeScript in a project. Its `compilerOptions` say how strict the checker is and what kind of JavaScript the project uses.
```

### strict mode

`strict: true` switches on a group of checks you've met all through this project:

- **noImplicitAny:** a parameter without a type is an error, instead of quietly becoming `any`.
- **strictNullChecks:** `null` and `undefined` are their own types, so you get `'customer.address' is possibly 'undefined'` instead of a crash.
- A few more, like checking that function types match exactly.

With `strict` off, this compiles without a word, and crashes for any customer without an address:

```ts
function cityOf(customer) {
  return customer.address.city;
}
```

Switching `strict` off to quieten the errors is like taking the batteries out of a smoke alarm. The fire is still there.

```callout jargon
**Strict mode** is the `strict` setting in `tsconfig.json`. It turns on TypeScript's strongest checks, including `noImplicitAny` and `strictNullChecks`.
```

### Running the check in a real project

Vite strips types and builds, but it never checks them. So the check needs its own script in `package.json`:

```json
"scripts": {
  "typecheck": "tsc --noEmit",
  "build": "tsc --noEmit && vite build"
}
```

`&&` runs the second command only when the first one passes. Now `npm run build` stops at the first type error, and a broken change can't go out green. The same `npm run typecheck` belongs in the CI robot from web-07.

### Types for libraries

Packages ship their types in **declaration files**, ending in `.d.ts`. They hold only types, no code. Some libraries include them. For others, the community publishes a separate package, like `@types/react`, which you install as a devDependency. Your editor uses them to know every function a library has.

```callout jargon
A **declaration file** (`.d.ts`) describes the types of JavaScript code without holding any code itself. `@types/...` packages are collections of them.
```

## Worked example

The calculator, split into three typed modules:

```ts
// src/types.ts
export type ItemCode = "CST" | "KEY" | "AMI" | "BAG" | "BTY";
export interface CartLine {
  code: ItemCode;
  qty: number;
}
```

```ts
// src/prices.ts
import type { ItemCode } from "./types.ts";

export const PRICES = { CST: 15000, KEY: 12000, AMI: 45000, BAG: 89900, BTY: 35000 };

export function priceOf(code: ItemCode): number {
  return PRICES[code];
}
```

```ts
// src/quote.ts
import type { CartLine } from "./types.ts";
import { priceOf } from "./prices.ts";

export function quoteTotal(lines: CartLine[]): number {
  let total = 0;
  for (const line of lines) {
    total += priceOf(line.code) * line.qty;
  }
  return total;
}

export const sample = quoteTotal([{ code: "BGA", qty: 1 }]);
```

`npm run typecheck` reads all three files together and finds the typo on the last line of `quote.ts`:

```text
error TS2322: Type '"BGA"' is not assignable to type 'ItemCode'.
```

1. `types.ts` holds only types. After stripping, nothing is left of it.
2. `prices.ts` uses `import type`, because it only needs `ItemCode` for its parameter.
3. `quote.ts` imports a type and a function. Only the function import survives the build.
4. The typo `"BGA"` sits in one file, and the type it breaks lives in another. Only `tsc`, reading the whole project, can join the two.

## Try it

This file exports a type and a function, and uses both. Run it, then change `"KEY"` to `"KYE"`. Node still runs it and prints `undefined`. Which `TS` error would `npm run typecheck` give?

```ts run
export type ItemCode = "CST" | "KEY";
export const PRICES = { CST: 15000, KEY: 12000 };
export function priceOf(code: ItemCode): number {
  return PRICES[code];
}
console.log(priceOf("KEY"));
```

## Recap

- `.ts` files use `import` and `export` as before, and types can be exported and imported too.
- `import type` marks an import as types only, so file-by-file tools can delete it safely.
- `tsconfig.json` holds the checker's settings: `strict`, `noEmit`, `target`, `module`, `moduleResolution` and more.
- `strict: true` turns on checks like `noImplicitAny` and `strictNullChecks`. Keep it on.
- Vite never checks types, so add `tsc --noEmit` to the build script and to CI.

## Common mistakes

**1. Importing a name that isn't exported.**

```ts
// types.ts exports CartLine, but has: type ItemCode = "CST" | "KEY";   (no export)
import type { ItemCode } from "./types.ts";
```

```text
error TS2459: Module '"./types.ts"' declares 'ItemCode' locally, but it is not exported.
```

A type is private to its file until you export it. Write `export type ItemCode = ...` in `types.ts`.

**2. Importing a type without the type keyword.**

```ts
import { CartLine } from "./types.ts";
```

Many project templates switch on a setting called `verbatimModuleSyntax`, and then `tsc` says:

```text
error TS1484: 'CartLine' is a type and must be imported using a type-only import when 'verbatimModuleSyntax' is enabled.
```

Write `import type { CartLine } from "./types.ts";`. File-by-file tools then know the line can be deleted.

**3. Switching strict off to make the errors go away.**

```json
{ "compilerOptions": { "strict": false } }
```

The errors disappear, and so does the protection: missing addresses and untyped parameters go back to crashing for customers. Keep `strict: true`, and fix the errors one file at a time.

**4. Trusting a green build.**

```json
"scripts": { "build": "vite build" }
```

`vite build` strips the types and builds, even with forty type errors. That's what happened in the ticket. Make the build check first: `"build": "tsc --noEmit && vite build"`.

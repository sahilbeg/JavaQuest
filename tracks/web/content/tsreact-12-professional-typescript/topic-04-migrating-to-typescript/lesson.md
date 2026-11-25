## Hook

You can't rebuild a bridge by closing it for a year. Traffic must keep moving. So engineers replace it one section at a time, and the bridge stays open the whole way through.

Real projects move from JavaScript to TypeScript the same way. Nobody stops the store for a month to rewrite it. You move one file at a time, and the app keeps working after every step.

## Concept

### Why move, and why one file at a time

Most big TypeScript codebases started as JavaScript. Teams move them because every file that gets real types gets the checks you met in [Strict mode and tsconfig in depth](/t/tsreact-12-professional-typescript/topic-02-strictness).

A "big bang" rewrite, every file at once, fails in a predictable way. It takes weeks, it conflicts with everyone else's work, and the store can't ship fixes while it's going on. Small steps win:

- Each step is a small pull request that is easy to review.
- The app builds and its tests pass after every step.
- If one step goes wrong, you undo one small change.

```callout jargon
A **migration** is moving code from one way of doing things to another, such as from JavaScript to TypeScript, while the app keeps running.
```

### Step 1: let JavaScript and TypeScript live together

```json
{
  "compilerOptions": {
    "allowJs": true,
    "checkJs": false,
    "strict": true
  }
}
```

`allowJs` lets `.ts` files import `.js` files, and the other way round. So `cart.ts` can use the old `money.js` while it waits its turn.

`checkJs` asks TypeScript to check plain `.js` files too, using what it can work out from the code. Some teams switch it on early to see bugs before renaming anything. Others turn it on one file at a time with a `// @ts-check` line at the top.

### Step 2: pick the order

```callout jargon
A **leaf** file imports nothing else from your project. `money.js` is a leaf. `App.jsx`, which imports everything, is the opposite: the root.
```

Move leaves first. When you move `money.js`, nothing it imports is still untyped, so its types are real all the way down. Then every file that imports it gets real types for free.

```animate
title: Moving the store, leaves first
kind: scene
nodes:
  - { id: money, label: money.js, emoji: "💰", value: JS, row: 2, col: 0 }
  - { id: cart, label: cart.js, emoji: "🛒", value: JS, row: 1, col: 1 }
  - { id: checkout, label: Checkout.jsx, emoji: "💳", value: JS, row: 1, col: 3 }
  - { id: app, label: App.jsx, emoji: "🏠", value: JS, row: 0, col: 2 }
links:
  - { from: app, to: cart }
  - { from: app, to: checkout }
  - { from: cart, to: money }
  - { from: checkout, to: money }
steps:
  - say: Four files import each other. App imports the cart and checkout, and both of them import money.
  - say: money.js imports nothing, so it is a leaf. It moves first, and its types are real all the way down.
    set: { money: TS }
    focus: [money]
  - say: Next, cart.js. Everything it imports is typed already, so its types are honest too.
    set: { cart: TS }
    focus: [cart]
  - say: Checkout moves the same way. The app builds and the tests pass after every one of these steps.
    set: { checkout: TS }
    focus: [checkout]
  - say: The root moves last. When App.tsx is done, the whole store is typed, and allowJs can be switched off.
    set: { app: TS }
    focus: [app]
```

### Syntax: JSDoc types

```js
/** @param {number} paise @returns {string} */
function formatRupees(paise) {
  return "₹" + (paise / 100).toFixed(2);
}
```

```callout jargon
**JSDoc** is a way of writing comments above JavaScript code, starting with `/**`. TypeScript reads `@param`, `@returns` and `@type` in them as types.
```

With `checkJs` on, these comments give a `.js` file most of TypeScript's checks without renaming it. Some teams stop here for files they rarely touch. For files you work on every week, a real `.ts` file is clearer.

### Step 3: rename, then type

Moving one file is two small steps:

1. **Rename** `giftOrder.js` to `giftOrder.ts`. Where TypeScript can't work out a shape, type it `any` for now, so the file compiles exactly as before. Commit.
2. **Type** it: replace each `any` with a real interface, one at a time. Fix what the compiler finds. Commit again.

Keeping the two steps apart makes review easy. The first commit changes no behaviour at all. The second shows every real type and every bug fix on its own.

```callout tip
Type the data first: the interfaces for the objects that flow through the file. Once the data has real types, most function types follow with no extra work.
```

### The bugs types find

This is the payoff. Old JavaScript often has bugs that hide because nothing checks property names. As soon as a shape gets a real type, they light up:

```ts
interface Skein { colour: string; grams: number }

function amigurumiLeft(skein: Skein): number {
  return Math.floor(skein.gram / 40);
}
```

```text
error TS2551: Property 'gram' does not exist on type 'Skein'. Did you mean 'grams'?
```

In JavaScript, `skein.gram` was `undefined`, and the stock screen said "NaN left" for a year. A move to TypeScript is often the cheapest bug hunt a team ever runs.

### Escape hatches, with an exit

Sometimes a file has more errors than you can fix in one pull request. Two tools let you move on without hiding problems forever:

- `any`, written on purpose and only during the move. Search for it later: it's a list of work.
- `// @ts-expect-error` with a reason and a ticket: `// @ts-expect-error: TSX-1204, old orders have no gift field`. When someone fixes the problem, the comment itself becomes an error, so it can't be forgotten.

```callout warning
Don't use // @ts-ignore during a migration. It hides the error and never tells you when it's gone, so it stays forever.
```

Many teams also add a lint rule that fails CI when the number of `any`s goes up. The count may only go down.

## Worked example

Here is `yarnStock.js`, the old helper behind the yarn shelf screen:

```js
export function needsReorder(skein) {
  return skein.grams < skein.reorderAt;
}

export function shelfLabel(skein) {
  return skein.colour + ": " + skein.gram + " g";
}
```

**Commit 1, rename.** The file becomes `yarnStock.ts`, and the parameters get `any`:

```ts
export function needsReorder(skein: any): boolean {
  return skein.grams < skein.reorderAt;
}

export function shelfLabel(skein: any): string {
  return skein.colour + ": " + skein.gram + " g";
}
```

It compiles, and nothing has changed for customers.

**Commit 2, type.** Write the data's shape, and use it:

```ts
export interface Skein {
  colour: "Marigold" | "Indigo" | "Rani pink" | "Mint" | "Off-white";
  grams: number;
  reorderAt: number;
}

export function needsReorder(skein: Skein): boolean {
  return skein.grams < skein.reorderAt;
}

export function shelfLabel(skein: Skein): string {
  return skein.colour + ": " + skein.grams + " g";
}
```

The compiler flagged `skein.gram` the moment `Skein` existed. That was why the shelf screen said "Marigold: undefined g". The fix is one letter, and the reviewer can see it on its own line in commit 2.

## Try it

This is the typed yarn code. Run it.

```ts run
type Skein = { colour: string; grams: number };

const shelf: Skein[] = [
  { colour: "Marigold", grams: 120 },
  { colour: "Mint", grams: 30 },
];

function amigurumiLeft(skein: Skein): number {
  return Math.floor(skein.grams / 40);
}

for (const skein of shelf) {
  console.log(skein.colour + ": " + amigurumiLeft(skein));
}
```

It prints:

```text
Marigold: 3
Mint: 0
```

1. Change `skein.grams` to `skein.gram` and run it again. Running strips the types, so what does it print?
2. Open the same code in an editor with TypeScript. What does it say about `gram`?

## Recap

- Move to TypeScript one file at a time. The app builds and its tests pass after every step.
- `allowJs` lets `.js` and `.ts` files import each other during the move; `checkJs` or `// @ts-check` checks plain JavaScript too, with JSDoc comments as types.
- Move leaf files first, so each file's imports are already typed.
- Rename first (with `any` where needed), then type in a separate commit. Type the data before the functions.
- Expect the types to find real bugs. Mark leftovers with `any` or `// @ts-expect-error` plus a ticket, never `@ts-ignore`, and keep the count going down.

## Common mistakes

**1. Moving every file in one pull request.**

```text
Convert the whole app to TypeScript (312 files changed)
```

Symptom: nobody can review it, it conflicts with every other branch, and it sits open for weeks. Fix: one file, or one small folder, per pull request, leaves first.

**2. Renaming and changing behaviour in the same commit.**

```text
Rename giftOrder.js to .ts, add types, fix totals, tidy names
```

Symptom: the reviewer can't tell which lines are a rename and which change what customers pay. Fix: commit the rename alone, then the types and fixes.

**3. Making the errors go away with as.**

```ts
const line = oldLine as GiftLine;
```

Symptom: the file is "typed", but `oldLine` has `quantity` instead of `qty`, and totals are still NaN. Fix: let the error show you the real shape, and fix the code or the data.

**4. Leaving any behind with no plan.**

```ts
export function lineTotal(line: any): number {
  return line.paise * line.qty;
}
```

Symptom: a year later the file still has a dozen `any`s and none of the checks. Fix: track the `any` count, and treat each one as a small ticket.

**5. Hiding errors with @ts-ignore.**

```ts
// @ts-ignore
const name = order.customerName;
```

Symptom: the field was renamed long ago, and this line quietly reads `undefined`. Fix: `// @ts-expect-error` with a reason and a ticket, or better, fix it now.

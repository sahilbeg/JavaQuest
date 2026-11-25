## Hook

Before Meera Aunty starts a new amigurumi, she reads the whole pattern once. Sometimes row 12 says "40 stitches" when row 11 only made 30. She spots it with a pencil, before a single gram of yarn is used.

JavaScript doesn't read ahead like that. It finds a mistake only when that exact line runs, sometimes in front of a customer. TypeScript is the pencil check: it reads your code first and points at the rows that can't work.

## Concept

```callout jargon
**TypeScript** is JavaScript with **types** added. A type says what kind of value something may hold: a number, some text, a list of items. A file written in TypeScript ends in `.ts` (or `.tsx` when it holds JSX).
```

### The bug from the ticket

Here is the cart code with the typo. JavaScript runs it happily:

```js run
const line = { code: "KEY", paise: 12000, qty: 2 };
const total = line.paise * line.qyt;
console.log(total);
```

It prints `NaN`. `line.qyt` doesn't exist, so it is `undefined`, and `12000 * undefined` is `NaN`. Nothing stopped, nothing warned. The mistake only showed up on a customer's screen.

### The same code, read by TypeScript

Save the same two lines in `cart.ts`, and TypeScript refuses before anything runs:

```ts
const line = { code: "KEY", paise: 12000, qty: 2 };
const total = line.paise * line.qyt;
```

```text
error TS2339: Property 'qyt' does not exist on type '{ code: string; paise: number; qty: number; }'.
```

TypeScript worked out the **shape** of `line` from the object you wrote: a `code` that is text, a `paise` that is a number, a `qty` that is a number. Then it noticed that `qyt` isn't part of that shape. The message even lists the real shape, so you can spot `qty` in it.

```callout jargon
A **type error** is a message from TypeScript saying a line can't work with the types involved. Each one has a code like `TS2339`, a line number and a sentence explaining the problem.
```

### Syntax: a type annotation

```ts
let qty: number = 2;
```

A colon after a name, then a type, is a **type annotation**. It is a promise: "`qty` will only ever hold a number". Break the promise, and TypeScript says so:

```ts
let qty: number = 2;
qty = "3";
```

```text
error TS2322: Type 'string' is not assignable to type 'number'.
```

"Not assignable" means "can't be put into". Text can't go into a box labelled `number`.

```callout jargon
A **type annotation** is `: type` written after a name, like `qty: number`. It tells TypeScript what that name may hold.
```

### The type checker: tsc

TypeScript comes with a program called `tsc`, the **TypeScript compiler**. Its most useful job is checking:

```text
npm install --save-dev typescript
npx tsc --noEmit
```

`tsc` reads every `.ts` file and reports each type error, with its file and line. It does **not** run your code. `--noEmit` means "only check, don't write any files". Your editor runs the same checker while you type, so the red squiggle appears before you even save.

```callout jargon
**Static checking** means checking code by reading it, without running it. It happens at **compile time**, before the program starts. **Run time** is when the code is actually running, in Node or the browser.
```

Watch the check happen before the code ever reaches a customer:

```animate
title: tsc checks, then the build strips the types
kind: scene
nodes:
  - { id: you, label: You save cart.ts, emoji: "📝", value: "line.qyt" }
  - { id: tsc, label: tsc, emoji: "🔍", value: idle }
  - { id: vite, label: Vite build, emoji: "📦" }
  - { id: browser, label: Customer's phone, emoji: "📱" }
steps:
  - say: You save cart.ts. Line 2 still says line.qyt.
    focus: [you]
  - say: You run npx tsc --noEmit. tsc reads every line, but runs none of them.
    send: { from: you, to: tsc, label: "npx tsc --noEmit" }
    set: { tsc: checking }
  - say: It knows line has code, paise and qty. qyt isn't one of them, so it reports TS2339.
    send: { from: tsc, to: you, label: "qyt does not exist", back: true }
    set: { tsc: 1 error }
  - say: You fix the typo and check again. No errors this time.
    send: { from: you, to: tsc, label: "npx tsc --noEmit" }
    set: { you: "line.qty", tsc: 0 errors }
  - say: The build strips every type annotation away. What's left is plain JavaScript.
    send: { from: you, to: vite, label: npm run build }
    set: { vite: plain JS }
  - say: The customer's phone runs JavaScript, as always. It never sees a type, and it never sees the typo.
    send: { from: vite, to: browser, label: "index-B7x2.js" }
```

### Types disappear when the code runs

Browsers only understand JavaScript. So before TypeScript runs anywhere, the types are removed. This is called **type erasure**. Recent versions of Node can run a `.ts` file directly: they strip the types and run what's left, without checking them.

```ts run
function lineTotal(paise: number, qty: number): number {
  return paise * qty;
}
console.log(lineTotal(12000, 2));
console.log(typeof lineTotal);
```

It prints `24000`, then `function`. With the annotations stripped, this is exactly the JavaScript function you'd have written in web-03.

```callout jargon
**Type erasure** means the types are removed before the code runs. They exist only for the checker and your editor. At run time, the code is plain JavaScript.
```

### What TypeScript can't catch

Because types are gone at run time, they can't check values that arrive while the code runs: what a customer types, what a server sends, what a spreadsheet holds.

```ts run
const typed: string = "two";
const qty: number = Number(typed);
console.log(qty);
console.log(typeof qty);
```

It prints `NaN`, then `number`. TypeScript is happy: `Number(...)` always gives a `number`, and `NaN` *is* a number. The type is right, but the value is still wrong. Checking values is still your job, with `if`, as in web-03.

```callout tip
TypeScript checks that your code fits together. Your tests check that it does the right thing. You need both.
```

### Why teams use it

- **Fewer bugs reach customers.** Typos, missing arguments and text in a number box are caught while you type.
- **The editor helps more.** Type `line.` and it lists `code`, `paise` and `qty`, because it knows the shape.
- **Big changes are safer.** Rename `paise` to `pricePaise`, and every place you forgot is marked red.
- **The code explains itself.** `deliveryFee(subtotal: number): number` tells the next person what goes in and what comes out.

It costs a little: you learn the type syntax, and the project gets a type-check step. You can also start small. A project can keep its `.js` files and add `.ts` files one at a time.

## Worked example

Here is `deliveryFee` from web-03, now with types. Below it are three calls with mistakes that JavaScript would only find while running, if at all.

```ts
function deliveryFee(subtotal: number): number {
  return subtotal > 99900 ? 0 : 6000;
}

deliveryFee("1200");
deliveryFee();
const fee: string = deliveryFee(50000);
```

`npx tsc --noEmit` reports one error for each of lines 5, 6 and 7:

```text
error TS2345: Argument of type 'string' is not assignable to parameter of type 'number'.
error TS2554: Expected 1 arguments, but got 0.
error TS2322: Type 'number' is not assignable to type 'string'.
```

1. Line 1 promises that `subtotal` is a number, and the `): number` after the brackets promises a number back.
2. Line 5 passes text. In JavaScript, `"1200" > 99900` quietly compares as numbers, so the bug could hide for months. TypeScript stops it at once.
3. Line 6 forgets the argument. JavaScript would compare `undefined > 99900`, get `false`, and charge ₹60 to everyone.
4. Line 7 puts the fee into a box labelled `string`. The function's return type says it gives a number, so that can't work.

None of these lines ran. The checker found all three by reading.

## Try it

This snippet runs in Node, which strips the types without checking them. Run it, then try each change and think about what `tsc` would say:

1. Change `2` in the last line to `"2"`. Node still prints a number. Why? Which `TS` error would `tsc` give?
2. Change `qty` to `qyt` inside the function. What does Node print now?

```ts run
function lineTotal(paise: number, qty: number): number {
  return paise * qty;
}
console.log(lineTotal(35000, 2));
```

## Recap

- TypeScript is JavaScript plus types. A type annotation like `qty: number` says what a name may hold.
- The type checker, `tsc`, reads your code without running it and reports type errors with a code, a file and a line.
- It catches typos in property names, missing arguments and values of the wrong kind before any customer does.
- Types are erased before the code runs, so they can't check what users type or servers send.
- A project can move to TypeScript one file at a time, and the browser still only runs JavaScript.

## Common mistakes

**1. Thinking types check values while the code runs.**

```ts
const qty: number = Number("two");
```

TypeScript reports nothing, and `qty` is `NaN` at run time. Types only describe what kind of value a name holds. When a value comes from outside, check it yourself, for example with `Number.isInteger(qty)`.

**2. Running the code but never running tsc.**

Node and Vite strip the types and run whatever is left. Code full of type errors still runs, and the bugs reach customers. Add a script like `"typecheck": "tsc --noEmit"` and run it before every push, the same way you run the tests.

**3. Silencing an error instead of reading it.**

```ts
const line: any = { code: "KEY", paise: 12000, qty: 2 };
const total = line.paise * line.qyt;
```

`any` switches the checker off for `line`, so the typo is back and nothing complains. Read the message instead. It names the missing property and lists the shape, which usually points at the fix.

**4. Reading only the error code.**

`TS2322` alone means little. The sentence after it says exactly what went wrong: `Type 'string' is not assignable to type 'number'` means text went into a box labelled `number`. Read the sentence, then look at the line it names.

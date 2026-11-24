## Hook

A dropped stitch in a long scarf is hard to find by staring at the whole scarf. Meera Aunty runs a finger along it, one row at a time, until she feels the gap.

A bug is the same. Staring at the code and guessing is slow. Running the code one line at a time, and looking at every value as it changes, finds the gap. The tool that lets you do that is a **debugger**.

## Concept

```callout jargon
A **bug** is code that does something other than what you meant. **Debugging** is finding out why. A **debugger** is a tool that pauses your running code so you can look inside it.
```

Debugging always follows the same loop:

1. **Reproduce** it: make the bug happen on your own screen.
2. **Read** what the browser already tells you.
3. **Look** at the values while the code runs.
4. **Fix** it, then check the fix with the same steps that showed the bug.

### Reading an error

When code crashes, the browser's Console (in DevTools, from web-00) shows an error. Here is a cart that crashes on an item it does not know:

```js run crash
const PRICES = {
  CST: { name: "Coaster set", paise: 15000 },
  BTY: { name: "Baby booties", paise: 35000 },
};

function lineTotal(line) {
  const item = PRICES[line.code];
  return item.paise * line.qty;
}

function cartTotal(lines) {
  let total = 0;
  for (const line of lines) {
    total += lineTotal(line);
  }
  return total;
}

console.log(cartTotal([{ code: "CST", qty: 2 }, { code: "bty", qty: 1 }]));
```

Press Run. The error looks like this in the browser:

```text
Uncaught TypeError: Cannot read properties of undefined (reading 'paise')
    at lineTotal (cart.js:8:15)
    at cartTotal (cart.js:14:14)
    at main.js:3:13
```

```callout jargon
A **stack trace** is the list under an error. The top line is where the code crashed. Each line below it is the function that called the one above.
```

Read it in two parts:

- **What:** `Cannot read properties of undefined (reading 'paise')`. Something was `undefined`, and the code asked it for `.paise`.
- **Where:** `lineTotal`, in `cart.js`, line 8, column 15. `cartTotal` called it, and `main.js` called that.

Click `cart.js:8:15` in the Console, and DevTools opens that exact line.

### Syntax: console methods

```js
console.log(label, value);
```

The quickest look inside is a print. Give each print a label, or you will not know which number is which:

```js run
const PRICES = { CST: 15000, BTY: 35000 };
const lines = [
  { code: "CST", qty: 2 },
  { code: "bty", qty: 1 },
];

let total = 0;
for (const line of lines) {
  const price = PRICES[line.code];
  console.log(line.code, price, line.qty);
  total += price * line.qty;
}
console.log("total:", total);
```

No crash this time, because prices are plain numbers. Instead the total comes out as `NaN`.

```callout jargon
**NaN** means "Not a Number". It is what JavaScript gives for maths that makes no sense, like `undefined * 1`. Any sum with a `NaN` in it is also `NaN`.
```

A few more console methods help:

| Method | Use it for |
|---|---|
| `console.log(a, b)` | everyday prints |
| `console.table(lines)` | an array of objects, drawn as a table |
| `console.warn(...)` | a yellow warning |
| `console.error(...)` | a red error |

```callout tip
Delete your prints before you commit. ESLint's `no-console` rule, from ticket 3, warns you about any you forgot.
```

### Breakpoints

Prints mean editing, saving and reloading, again and again. A breakpoint is faster.

```callout jargon
A **breakpoint** is a mark on a line of code. When the code reaches that line, the debugger **pauses** it, before the line runs. While it is paused, you can see every variable's value.
```

In Chrome's DevTools, open the **Sources** panel and open `cart.js`. Click the line number where you want to stop. A blue marker appears. Now do the thing that causes the bug, like adding booties to the cart. The page freezes, and that line is highlighted.

While paused, three panels on the side help:

- **Scope** lists every variable the paused line can see, with its value now.
- **Watch** shows any expression you type, like `line.code` or `PRICES[line.code]`.
- **Call Stack** shows which functions called this one, like the stack trace.

### Syntax: stepping

```text
Resume       F8         run until the next breakpoint
Step over    F10        run this line, pause on the next one
Step into    F11        go inside the function this line calls
Step out     Shift+F11  finish this function, pause where it was called
```

Stepping over a loop one line at a time is like running your finger along the scarf. Watch `total` in the Scope panel:

```animate
title: Stepping through the cart loop
kind: code
language: js
code: |
  const PRICES = { CST: 15000, BTY: 35000 };
  let total = 0;
  for (const code of ["CST", "bty"]) {
    total += PRICES[code];
  }
  console.log(total);
steps:
  - line: 1
    vars: { PRICES: "{ CST: 15000, BTY: 35000 }" }
    say: The price list has two keys, CST and BTY, both in capitals.
  - line: 2
    vars: { total: 0 }
    say: total starts at 0. You step over each line with F10.
  - line: 3
    vars: { code: '"CST"' }
    say: First time round the loop, code is "CST".
  - line: 4
    vars: { total: 15000 }
    say: PRICES["CST"] is 15000, so total is 15000. All good so far.
  - line: 3
    vars: { code: '"bty"' }
    say: Second time round, code is "bty", in small letters.
  - line: 4
    vars: { total: "NaN" }
    say: There is no "bty" key, so PRICES["bty"] is undefined. 15000 plus undefined is NaN. Found the dropped stitch.
  - line: 6
    out: "NaN"
    say: Every line after this only passes the NaN along. The bug happened on line 4, not here.
```

### Conditional breakpoints

A cart with 40 lines would make you step 40 times. A **conditional breakpoint** pauses only when an expression is true.

Right-click the line number, choose "Add conditional breakpoint", and type:

```js
PRICES[line.code] === undefined
```

Now the code runs at full speed and pauses only on the line that is going to break.

### Syntax: the debugger statement

```js
debugger;
```

Write `debugger;` on any line of your code. When DevTools is open, the code pauses there, exactly like a breakpoint. When DevTools is closed, the browser ignores it.

It helps when the file is hard to find in the Sources panel. But never commit it: ESLint's recommended rules report it as `no-debugger`.

```callout tip
With Vite's dev server, the Sources panel shows your own files, with your own line numbers, so breakpoints go exactly where you expect.
```

## Worked example

Raju's ticket: two coaster sets and baby booties show "₹NaN".

**Step 1: reproduce.** Run `npm run dev`. Add two coaster sets: ₹300.00. Add booties: ₹NaN. The bug is on your screen.

**Step 2: read.** The Console has no red error. The code did not crash; it calculated nonsense. So there is no stack trace to follow, and you need to look inside.

**Step 3: look.** In Sources, open `cart.js` and put a breakpoint on `total += price * line.qty;`. Add the booties again. The page pauses.

In Scope: `line` is `{ code: "bty", qty: 1 }`, and `price` is `undefined`. The catalogue gives the booties' code in small letters, but the price list uses capitals.

**Step 4: fix both problems.** Look the code up in capitals. And if a code is truly unknown, fail loudly instead of quietly making `NaN`:

```js run
const PRICES = { CST: 15000, BTY: 35000 };

function cartTotal(lines) {
  let total = 0;
  for (const line of lines) {
    const price = PRICES[line.code.toUpperCase()];
    if (price === undefined) {
      throw new Error(`Unknown item code: ${line.code}`);
    }
    total += price * line.qty;
  }
  return total;
}

console.log(cartTotal([{ code: "CST", qty: 2 }, { code: "bty", qty: 1 }]));
```

It prints `65000`: two coaster sets at 15000 paise, plus booties at 35000. On screen, that is ₹650.00.

**Step 5: check with the same steps.** Remove the breakpoint, reload, and add the same three items. ₹650.00. Then write a test for it, so the bug can never come back silently. That is the next ticket.

## Try it

1. Press Run on the `NaN` example above. Then change `"bty"` to `"BTY"` and run it again.
2. In the crash example, change the last line's item to `"KEY"`. What does the error say now, and why? (The price list has no `KEY`.)
3. In your Vite project, add `debugger;` inside a click handler in `main.js`. Open DevTools, click, and look at the Scope panel. Then delete the line.
4. Put a normal breakpoint on the same line in Sources instead. Use F10 to step, and F8 to resume.

## Recap

- Debug in a loop: reproduce, read, look, fix, then check with the same steps.
- An error's first line says what went wrong. The stack trace below it says where: the top line crashed, and the lines below called it.
- `console.log` with a label is the quickest look. `NaN` means maths with something that is not a number, like `undefined`.
- A breakpoint pauses the code before a line runs. Step over, into and out, and watch values in Scope and Watch. Conditional breakpoints pause only when it matters.
- `debugger;` pauses like a breakpoint when DevTools is open. Never commit it, or your `console.log` lines.

## Common mistakes

**1. Reading only the bottom of the stack trace.**

```text
Uncaught TypeError: Cannot read properties of undefined (reading 'paise')
    at lineTotal (cart.js:8:15)
    at cartTotal (cart.js:14:14)
    at main.js:3:13
```

What you do: you open `main.js` line 3 and find nothing wrong there.

Fix: start at the top. `cart.js:8:15` is where it crashed. The lines below only tell you how the code got there.

**2. Fixing where the NaN shows up, not where it starts.**

```js
const shown = Number.isNaN(total) ? 0 : total;
```

What you see: the cart says ₹0.00 instead of ₹NaN. Mrs. Deshpande's booties are now free.

Fix: step back to the line where the value first became `NaN`, and fix the cause there. Hiding a bad value makes the bug quieter, not gone.

**3. Prints without labels.**

```js
console.log(price);
console.log(total);
```

What you see: a column of numbers, and `undefined`, with no way to tell which line printed what.

Fix: label every print, like `console.log("price", price)`, or print an object: `console.log({ price, total })`.

**4. Committing a debugger statement.**

What you see: ESLint reports `Unexpected 'debugger' statement  no-debugger`. If it slips through, the live site freezes for any customer who happens to have DevTools open.

Fix: delete every `debugger;` and stray `console.log` before you commit. Run `npm run lint` to catch the ones you missed.

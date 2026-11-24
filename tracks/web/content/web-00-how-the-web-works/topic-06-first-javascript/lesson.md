## Hook

Every new customer on WhatsApp gets the same three-line welcome. Typed fresh each time, it goes wrong. Some days it's "Namste", some days "Dhaga", and once the stall's hours came out wrong.

A computer never gets tired and never makes a typo, as long as the instructions are right. Write the welcome once, as a program, and it comes out perfect every time.

That's your first JavaScript. It does one job: print some lines of text, in order. Every program you'll ever write is built from small steps like these.

## Concept

A **program** is a list of instructions for a computer. JavaScript programs can run in a browser, or on their own.

### Running JavaScript outside the browser

In ticket WEB-0002 you saw JavaScript running inside a page. JavaScript can also run on its own, without any page, using a program called **Node.js**.

When you press **Run** on a ticket, Cadence starts your file with Node. Anything the program prints shows up below the editor.

```callout jargon
**Node.js**, or just **Node**, runs JavaScript outside a browser. To **run** a program means to start it, so the computer follows its instructions.
```

### Syntax: console.log

```js
console.log("Namaste!");
```

- `console.log` prints whatever is inside the brackets, then moves to a new line.
- The text goes inside quotes.
- The line ends with a semicolon `;`, like a full stop at the end of a sentence.

In Node, the text appears in the output below the editor. In a browser, it appears in the DevTools Console, which you met in ticket WEB-0003.

```callout jargon
An instruction like `console.log("Namaste!");` is called a **statement**. A program is a list of statements.
```

### Syntax: strings

```js
console.log("Handmade crochet from Pune.");
console.log('Handmade crochet from Pune.');
```

Text in quotes is called a **string**. You can use double quotes `"` or single quotes `'`. Both lines above print exactly the same thing. Just start and end each string with the same kind.

```callout jargon
A **string** is a piece of text in a program, wrapped in quotes. It's called a string because it's a string of letters, one after another, like beads on a thread.
```

### Statements run from top to bottom

JavaScript runs your statements one at a time, from the top of the file down. Watch:

```animate
title: Three lines, in order
kind: code
language: js
code: |
  console.log("Namaste!");
  console.log("Welcome to Dhaaga & Co.");
  console.log("We open at 10:00.");
steps:
  - line: 1
    out: Namaste!
    say: JavaScript starts at the top. The first console.log prints Namaste! and moves to a new line.
  - line: 2
    out: Welcome to Dhaaga & Co.
    say: Then the second line runs. Its text lands on the next line of the output.
  - line: 3
    out: We open at 10:00.
    say: The third line runs last. Swap two lines in the file and their output swaps too.
  - line: 0
    say: There are no more statements, so the program ends.
```

### Numbers are not strings

Numbers don't need quotes. JavaScript can do maths with them. But a number inside quotes is just text:

```js run
console.log(150 + 30);
console.log("150" + "30");
console.log("Gift wrap: " + 30);
```

This prints `180`, then `15030`, then `Gift wrap: 30`. With numbers, `+` adds. With strings, `+` glues them together. When one side is a string, JavaScript glues.

### Syntax: comments

```js
// This line is a note for people. JavaScript skips it.
console.log("Namaste!"); // a comment can also go at the end of a line
```

Two slashes `//` start a **comment**. Everything after them on that line is ignored. Your exercise uses comments to mark each TODO.

### Syntax: a function that returns text

```js
function shopName() {
  return "Dhaaga & Co.";
}

console.log(shopName());
```

- A **function** is a named list of statements. `function shopName() { ... }` creates one called `shopName`.
- The statements between the curly braces `{ }` run only when someone calls the function.
- `shopName()`, with brackets, **calls** it: "run that function now".
- `return` hands a value back to whoever called the function.

Watch JavaScript jump into the function and come back with the text:

```animate
title: console.log calls shopName()
kind: code
language: js
code: |
  function shopName() {
    return "Dhaaga & Co.";
  }

  console.log(shopName());
steps:
  - line: 1
    say: JavaScript reads the function and remembers it. Nothing inside it runs yet.
  - line: 5
    say: console.log needs to know what to print. So JavaScript first calls shopName() and jumps up into it.
  - line: 2
    say: The function's only statement runs. return hands the text "Dhaaga & Co." back to line 5.
  - line: 5
    out: Dhaaga & Co.
    say: Back on line 5, holding the text. console.log prints it.
```

```callout jargon
A **function** is a named list of statements. To **call** it means to run it, by writing its name with brackets. To **return** a value means to hand it back to the caller.
```

### Syntax: export and import

Your exercise has two files. `card.js` holds the functions. `main.js` runs them.

```js
export function shopName() {
  return "Dhaaga & Co.";
}
```

`export` in front of a function lets other files use it.

```js
import { shopName } from "./card.js";

console.log(shopName());
```

`import` brings it in from `card.js`. Then `main.js` can call it. The checks call your functions straight from `card.js`. Leave the `export` and `import` lines exactly as they are.

## Worked example

Here's a program that prints the stall's opening sign:

```js run
// The sign for the FC Road stall
console.log("Dhaaga & Co.");
console.log("Open Saturday and Sunday");
console.log("10:00 to 20:00");
```

1. Line 1 is a comment, so JavaScript skips it.
2. Line 2 prints `Dhaaga & Co.` and moves to a new line.
3. Line 3 prints `Open Saturday and Sunday` on the next line.
4. Line 4 prints `10:00 to 20:00`. Then there's nothing left, so the program ends.

The output is three lines, in the same order as the statements.

## Try it

Press **Run**. Then change the text to greet a customer by name, like Mrs. Joshi, and press **Run** again.

```js run
console.log("Namaste, Mrs. Joshi!");
console.log("Your tote bag is ready.");
```

Now add a third line that prints `Thank you for shopping with us.` Then try deleting one closing quote and press **Run**. Read the error, and put the quote back.

```callout tip
JavaScript cares about capital letters. `console.log` works. `Console.log` and `console.Log` don't.
```

## Recap

- A **program** is a list of **statements** that run from top to bottom.
- `console.log("...")` prints one line of text.
- Text in quotes is a **string**. Numbers need no quotes, and `+` adds numbers but glues strings.
- A **function** is a named list of statements. Calling it runs them, and `return` hands a value back.
- `//` starts a comment, and `export` and `import` share functions between files.

## Common mistakes

**1. Forgetting the quotes.**

```js run crash
console.log(Namaste);
```

Node says: `ReferenceError: Namaste is not defined`. Without quotes, JavaScript thinks `Namaste` is the name of something, and nothing has that name. Put text in quotes: `console.log("Namaste");`.

**2. Quotes that don't match.**

```js nocompile
console.log("Namaste!');
```

Node says: `SyntaxError: Invalid or unexpected token`. The string starts with `"` but tries to end with `'`, so it never ends. Use the same kind of quote at both ends.

**3. Wrong capital letters.**

```js run crash
Console.log("Namaste!");
```

Node says: `ReferenceError: Console is not defined`. JavaScript only knows `console`, with a small c.

**4. Forgetting return.**

```js run
function shopName() {
  "Dhaaga & Co.";
}

console.log(shopName());
```

This prints `undefined`. The function has the text, but never hands it back, so the caller gets nothing. `undefined` is JavaScript's word for "no value here". Write `return "Dhaaga & Co.";`.

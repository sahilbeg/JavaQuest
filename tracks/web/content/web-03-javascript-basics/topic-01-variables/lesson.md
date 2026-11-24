## Hook

Raju keeps a small stock box under the FC Road stall counter. A sticky note on the lid says "Coaster sets: 12". When a set sells, he crosses out the number and writes the new one.

The shop's name is printed on the side of the box. That never changes. The count on the note changes all day. Your ticket is to give the stall's JavaScript the same two kinds of memory.

## Concept

A program remembers things in **variables**. A variable is a box with a name on it. You put a value in the box, and later you read it back by its name.

```callout jargon
A **variable** is a named box that holds a value. A **value** is one piece of data, like the number `12` or the text `"Dhaaga & Co."`.
```

JavaScript has two words for making a box: `const` and `let`.

### Syntax: const

```js
const SHOP_NAME = "Dhaaga & Co.";
```

`const` makes a box whose value is fixed. Once something is in it, nothing else can go in. Read the line as "SHOP_NAME is always Dhaaga & Co."

```callout jargon
To **declare** a variable means to create its box. `const` and `let` are the two words that declare one.
```

Text goes inside quotes. Numbers don't: `const CITY = "Pune";` but `const tables = 1;`.

### Syntax: let

```js
let coasters = 12;
```

`let` makes a box whose value can change later. Use it for things that move during the day, like a count or a running total.

### Syntax: reassigning

```js
let coasters = 12;
coasters = 9;
```

To **reassign** means to put a new value in a box that already exists. The old value is thrown away. You write `let` only once, when you make the box.

The `=` sign means "store this", not "is equal to". So `coasters = coasters - 3;` means: read what's in `coasters`, take away 3, and put the answer back in the same box.

Watch two boxes as JavaScript runs a Sunday at the stall, one line at a time:

```animate
title: The stock box changes
kind: code
language: js
code: |
  const item = "Coaster set";
  let coasters = 12;
  coasters = coasters - 3;
  console.log(item + ": " + coasters);
  coasters = coasters + 5;
  console.log(item + ": " + coasters);
steps:
  - line: 1
    vars: { item: '"Coaster set"' }
    say: JavaScript makes a const box called item and puts the text "Coaster set" in it. It can never be replaced.
  - line: 2
    vars: { coasters: 12 }
    say: Next comes a let box called coasters, holding 12. A let box is allowed to change later.
  - line: 3
    vars: { coasters: 9 }
    say: The right side runs first. 12 - 3 is 9, and 9 goes back into the box. The old 12 is gone.
  - line: 4
    out: "Coaster set: 9"
    say: console.log reads both boxes and prints what is in them right now.
  - line: 5
    vars: { coasters: 14 }
    say: Raju brings 5 more sets from home. 9 + 5 is 14.
  - line: 6
    out: "Coaster set: 14"
    say: The same kind of line now prints 14, because it reads the box as it is now.
```

### const really means const

Try to change a `const`, and JavaScript stops the program:

```js run crash
const shop = "Dhaaga & Co.";
shop = "Dhaaga";
```

It says `TypeError: Assignment to constant variable.` That's a good thing. A value that must stay fixed can't change by accident.

### Which one to use

Start with `const`. Switch to `let` only when you really need to reassign the box. Most values in a program never change, so most boxes are `const`.

```callout tip
You may see `var` in old code online. It's the old way to make a variable, and it has some odd rules. Use `let` and `const` instead.
```

### undefined: an empty box

A `let` box can start empty. Then it holds a special value called `undefined`.

```js run
let customer;
console.log(customer);
customer = "Asha";
console.log(customer);
```

This prints `undefined`, then `Asha`. A `const` can't start empty, because it could never be filled later.

```callout jargon
**undefined** is JavaScript's value for "nothing has been put here yet". You'll meet its cousin `null` in [Booleans and comparison](/t/web-03-javascript-basics/topic-04-booleans-and-comparison).
```

### Naming rules

JavaScript has a few strict rules for names:

- A name can use letters, digits, `_` and `$`.
- It can't start with a digit. `2ndItem` breaks this rule.
- It can't contain a space or a `-`.
- It can't be a word JavaScript already uses, like `let` or `const`.
- Capitals matter. `coasters` and `Coasters` are two different boxes.

Most names use **camelCase**: the first word is lowercase, and each new word starts with a capital, like `giftWrapFee`. A fixed setting, like a shop name, is often written in capitals with `_` between words, like `SHOP_NAME`.

### The practice file

Your fix lives in `src/stall.js`. Each job there sits inside a **function**, a named block of code that hands back an answer with `return`.

```js
function coastersLeft() {
  let coasters = 12;
  coasters = coasters - 2;
  return coasters;
}
```

For now you only fill in the inside. Topic 8 explains functions properly. The word `export` in front of each one lets the checks read your code.

## Worked example

This program remembers a morning at the stall:

```js run
const shop = "Dhaaga & Co.";
const stall = "FC Road";
let coasters = 12;
console.log(shop + " at " + stall);
console.log("Coaster sets in the box: " + coasters);
coasters = coasters - 2;
console.log("After two sales: " + coasters);
```

1. Lines 1 and 2 make two `const` boxes. The shop and the stall won't change today.
2. Line 3 makes a `let` box, because the count will change.
3. Line 4 joins pieces of text with `+` and prints `Dhaaga & Co. at FC Road`.
4. Line 5 prints `Coaster sets in the box: 12`.
5. Line 6 reassigns `coasters`. It reads 12, takes away 2, and stores 10.
6. Line 7 prints `After two sales: 10`.

A `+` between text and anything else glues them into one piece of text. Topic 3 has much more on that.

## Try it

Change the stall to `"Aundh shop"`. Then sell 3 more coaster sets before the last line, and press **Run**.

```js run
const shop = "Dhaaga & Co.";
const stall = "FC Road";
let coasters = 12;
coasters = coasters - 2;
console.log(shop + " at " + stall + ": " + coasters + " coaster sets");
```

Next, add a line that changes `shop` after it's made. Read the error JavaScript gives you, then take the line out again.

## Recap

- A variable is a named box that holds a value.
- `const` makes a box you can't reassign. Use it by default.
- `let` makes a box you can reassign with `=`, like `coasters = coasters - 3;`.
- A `let` declared without a value holds `undefined`.
- Names use camelCase, can't start with a digit, and capitals matter.

## Common mistakes

**1. Reassigning a const.**

```js
const coasters = 12;
coasters = 11;
```

JavaScript says `TypeError: Assignment to constant variable.` If the value has to change, declare it with `let`.

**2. Writing let again when you reassign.**

```js nocompile
let coasters = 12;
let coasters = 9;
```

JavaScript says `SyntaxError: Identifier 'coasters' has already been declared`. Make the box once, then reassign it without `let`: `coasters = 9;`

**3. Using a box before you make it.**

```js
console.log(coasters);
let coasters = 12;
```

JavaScript says `ReferenceError: Cannot access 'coasters' before initialization`. JavaScript runs from top to bottom, so declare the box first.

**4. A typo in the name.**

```js
const giftWrapFee = 3000;
console.log(giftwrapFee);
```

JavaScript says `ReferenceError: giftwrapFee is not defined`. Capitals matter, so `giftwrapFee` is a different name that doesn't exist. Copy the exact spelling.

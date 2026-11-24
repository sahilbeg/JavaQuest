## Hook

Every item at the stall has a small paper tag: "AMI: Amigurumi". Every WhatsApp order ends with a message like "Hi Asha, order-17 is ready." All of that is text.

Customers type text too, and they're messy. One types `  ami `, another types `Bag`, a third types `BAG `. This ticket teaches you to build text, and to tidy it up before you use it.

## Concept

In JavaScript, a piece of text is called a **string**.

```callout jargon
A **string** is a piece of text: characters in a row, like beads on a thread. A **character** is one letter, digit, space or symbol.
```

### Syntax: strings

```js
const name = "Amigurumi";
```

A string goes inside quotes. JavaScript accepts three kinds:

```js
const a = "Tote bag";
const b = 'Tote bag';
const c = `Tote bag`;
```

Double quotes and single quotes mean exactly the same thing. This course uses double quotes. Backticks are special, as you'll see below. A string can also be empty: `""`.

### Syntax: joining with +

```js
const label = "AMI" + ": " + "Amigurumi";
```

`+` glues strings together. This is called **concatenation**. Spaces don't appear by magic, so put them inside the quotes yourself.

```callout jargon
**Concatenation** means joining strings end to end. `"Tote" + " bag"` gives `"Tote bag"`.
```

### Text and numbers mixed

When one side of `+` is a string, JavaScript turns the other side into text and glues. It works from left to right:

```js run
console.log(150 + 30);
console.log("150" + 30);
console.log(150 + 30 + " paise");
console.log("paise: " + 150 + 30);
```

This prints `180`, `15030`, `180 paise` and `paise: 15030`. Line 3 adds `150 + 30` first, because both are numbers, and only then meets the text. Line 4 meets the text first, so everything after it gets glued. Brackets fix it: `"paise: " + (150 + 30)`.

### Syntax: template literals

```js
const message = `Hi ${name}, your order is ready.`;
```

A **template literal** is a string in backticks. JavaScript works out whatever is inside `${...}` and drops it into the text. No more `+` and quote juggling.

```js run
const code = "AMI";
const name = "Amigurumi";
const paise = 45000;
console.log(`${code}: ${name}`);
console.log(`${name} costs ${paise / 100} rupees`);
```

This prints `AMI: Amigurumi`, then `Amigurumi costs 450 rupees`. The sum `paise / 100` runs first, and its answer goes into the text.

Watch a WhatsApp message being built:

```animate
title: Filling in a template literal
kind: code
language: js
code: |
  const name = "Asha";
  const orderId = "order-17";
  const message = `Hi ${name}, ${orderId} is ready.`;
  console.log(message);
  console.log(message.length);
steps:
  - line: 1
    vars: { name: '"Asha"' }
    say: A string box called name, holding the customer's name.
  - line: 2
    vars: { orderId: '"order-17"' }
    say: Another string box with the order id.
  - line: 3
    vars: { message: '"Hi Asha, order-17 is ready."' }
    say: JavaScript reads the backticks, swaps each ${...} for the value in that box, and stores one finished string.
  - line: 4
    out: "Hi Asha, order-17 is ready."
    say: The message prints just as Raju would type it.
  - line: 5
    out: "27"
    say: length counts every character, including the spaces, the comma and the full stop. That's 27.
```

### Syntax: length and index

```js
const first = word[0];
```

Each character in a string has a position number called an **index**. Counting starts at 0, not 1. So `word[0]` is the first character.

```callout jargon
An **index** is a character's position in a string, counting from 0. In `"Keychain"`, index 0 is `K` and index 7 is `n`.
```

| Index | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|---|---|---|
| Character | K | e | y | c | h | a | i | n |

```js run
const item = "Keychain";
console.log(item.length);
console.log(item[0]);
console.log(item[item.length - 1]);
```

This prints `8`, `K` and `n`. `length` is 8, but the last index is 7, because counting started at 0. So the last character is always at `length - 1`.

### Syntax: string methods

```js
const clean = typed.trim();
```

A **method** is a tool that a value carries around. You call it with a dot, its name and round brackets. Strings have many:

| Method | What it does | Example | Result |
|---|---|---|---|
| `toUpperCase()` | all capitals | `"ami".toUpperCase()` | `"AMI"` |
| `toLowerCase()` | all small letters | `"BAG".toLowerCase()` | `"bag"` |
| `trim()` | removes spaces at both ends | `"  ami ".trim()` | `"ami"` |
| `includes(x)` | is `x` somewhere inside? | `"order-17".includes("17")` | `true` |
| `startsWith(x)` | does it begin with `x`? | `"DC-1042".startsWith("DC-")` | `true` |
| `slice(a, b)` | the part from index `a` up to, not including, `b` | `"DC-1042".slice(0, 2)` | `"DC"` |
| `replace(a, b)` | swaps the first `a` for `b` | `"Tote bag".replace("bag", "Bag")` | `"Tote Bag"` |

`slice(a)` with one number keeps everything from index `a` to the end.

```js run
const typed = "  Tote Bag ";
console.log(typed.length);
console.log(typed.trim());
console.log(typed.trim().toUpperCase());
console.log("DC-1042".startsWith("DC-"));
console.log("DC-1042".slice(3));
```

This prints `11`, `Tote Bag`, `TOTE BAG`, `true` and `1042`. Line 3 **chains** two methods: `trim()` hands back a new string, and `toUpperCase()` runs on that.

### Strings never change

A method never changes the string it's called on. It hands back a new string instead.

```js run
let code = "ami";
code.toUpperCase();
console.log(code);
code = code.toUpperCase();
console.log(code);
```

The first `console.log` prints `ami`. The capitals were made, then thrown away, because nothing stored them. After `code = code.toUpperCase();` it prints `AMI`.

```callout jargon
**Immutable** means "can't be changed". Strings are immutable: you can only make new strings from old ones.
```

### Special characters

Inside quotes, `\n` starts a new line. Backticks can also run over several lines as they are.

```js run
console.log("Dhaaga & Co.\nFC Road stall");
console.log(`Thank you!
Come again.`);
```

Each `console.log` prints two lines. To put a double quote inside a double-quoted string, write `\"`, as in `"Say \"hi\""`.

## Worked example

A customer types her name with extra spaces, all in small letters. The message should still look neat.

```js run
const typed = "  asha  ";
const orderId = "order-17";
const trimmed = typed.trim();
const name = trimmed[0].toUpperCase() + trimmed.slice(1);
const number = orderId.slice(6);
console.log(`Hi ${name}! Order number ${number} is ready.`);
```

1. Line 3 removes the spaces at both ends, so `trimmed` is `"asha"`.
2. Line 4 takes the first character, `"a"`, and makes it `"A"`. `slice(1)` gives the rest, `"sha"`. Glued, they make `"Asha"`.
3. Line 5 skips the 6 characters of `"order-"`, so `number` is `"17"`.
4. Line 6 drops both into a template literal and prints `Hi Asha! Order number 17 is ready.`

## Try it

Change `qty` to `3`, then change `code` to `"  key "` and tidy it with `trim()` and `toUpperCase()` inside the `${...}`.

```js run
const code = "KEY";
const name = "Keychain";
const qty = 2;
const paise = 12000;
console.log(`${qty} x ${name} (${code}): ${(paise * qty) / 100} rupees`);
```

## Recap

- A string is text in quotes. This course uses double quotes, and backticks for template literals.
- `+` glues strings. With a string on one side, numbers get glued too, from left to right.
- A template literal like `` `Hi ${name}` `` drops values straight into text.
- `length` counts characters, and `text[0]` is the first one, because indexes start at 0.
- Methods like `trim()`, `toUpperCase()` and `slice()` hand back new strings. Store what they return.

## Common mistakes

**1. Using ${...} inside normal quotes.**

```js
const name = "Asha";
console.log("Hi ${name}!");
```

This prints `Hi ${name}!`, word for word. Only backticks fill in `${...}`: `` `Hi ${name}!` ``.

**2. Forgetting that methods return a new string.**

```js
let code = " ami ";
code.trim();
console.log(code.length);
```

This prints `5`, because `code` still has its spaces. Store the answer: `code = code.trim();`

**3. Glueing numbers by accident.**

```js
const qty = 2;
console.log("Total: " + qty * 12000 / 100 + 30);
```

This prints `Total: 24030`. The `*` and `/` run first and give `240`, but then the text glues `240` and `30`. Add brackets: `"Total: " + (qty * 12000 / 100 + 30)`.

**4. Leaving out the brackets on a method.**

```js
const code = "ami";
console.log(code.toUpperCase);
```

This prints `[Function: toUpperCase]`, the tool itself instead of its answer. Call it with brackets: `code.toUpperCase()`.

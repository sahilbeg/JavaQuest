## Hook

Think about the cash box at the stall. It holds notes and coins, and every one of them is a whole thing. You can't hand a customer a third of a coin.

Computers have a strange quirk with decimals, and it bites shops that store prices like `150.50`. This ticket shows the quirk, and why Dhaaga's code counts every price in **paise**, as whole numbers only.

## Concept

```callout jargon
**Paise** are the small units of the rupee: ₹1 is 100 paise. So ₹150 is `15000` paise, and ₹899 is `89900` paise.
```

### One kind of number

In JavaScript, every number has the same type: `number`. `12`, `15000`, `-3` and `150.5` are all of type `number`. There's no separate type for whole numbers.

```callout jargon
A **type** is the kind of a value: a number, a piece of text, true or false. The type decides what you can do with the value.
```

### Syntax: arithmetic

```js
const total = 15000 + 12000;
```

These are the maths operators:

| Operator | Meaning | Example | Result |
|---|---|---|---|
| `+` | add | `15000 + 12000` | `27000` |
| `-` | subtract | `15000 - 3000` | `12000` |
| `*` | multiply | `12000 * 2` | `24000` |
| `/` | divide | `45000 / 100` | `450` |
| `%` | remainder | `130 % 40` | `10` |
| `**` | power | `10 ** 3` | `1000` |

JavaScript follows school maths: `*` and `/` happen before `+` and `-`. Brackets change the order.

```js run
console.log(15000 + 12000 * 2);
console.log((15000 + 12000) * 2);
console.log(45000 / 100);
console.log(7 / 2);
```

This prints `39000`, `54000`, `450` and `3.5`. Notice that `/` keeps the decimals when the answer isn't whole.

### Syntax: Math.floor and Math.round

```js
const each = Math.floor(7 / 2);
```

Money can't be split below one paisa, so you often need a whole number. `Math` is a toolbox built into JavaScript, and three of its tools round numbers:

- `Math.floor(x)` rounds **down**: `Math.floor(3.9)` is `3`.
- `Math.round(x)` rounds to the **nearest** whole number: `Math.round(3.5)` is `4`, and `Math.round(3.4)` is `3`.
- `Math.ceil(x)` rounds **up**: `Math.ceil(3.1)` is `4`.

So `Math.floor(7 / 2)` is `3`.

### Syntax: the remainder, %

```js
const leftover = 130 % 40;
```

`%` gives what's left over after dividing. 130 grams of Indigo yarn makes 3 small amigurumi at 40 g each, with 10 g left over.

```js run
const grams = 130;
console.log(Math.floor(grams / 40));
console.log(grams % 40);
```

This prints `3`, then `10`. `Math.floor` with `/` counts the whole groups, and `%` counts what's left. The same pair splits paise into rupees and paise.

### The 0.1 + 0.2 surprise

Now the quirk. Run this:

```js run
console.log(0.1 + 0.2);
console.log(1.1 * 3);
console.log(10 + 20);
```

It prints `0.30000000000000004`, then `3.3000000000000003`, then `30`. The decimals are slightly wrong. The whole numbers are exactly right.

Computers store numbers in binary, using only 0s and 1s. Most decimals, like 0.1, have no exact binary form, just as 1/3 has no exact decimal form (0.333...). So JavaScript stores the closest number it can, which is a tiny bit off.

```callout jargon
**Floating point** is the way computers store numbers with decimals: as a very close guess. Tiny errors appear when you add or multiply those guesses.
```

### Why Dhaaga counts in paise

Whole numbers don't have this problem. So Dhaaga stores every price in paise, and only turns paise into rupees when it shows them. Watch both ways side by side:

```animate
title: Rupees as decimals vs whole paise
kind: code
language: js
code: |
  const rupees = 0.1 + 0.2;
  const paise = 10 + 20;
  console.log(rupees);
  console.log(paise);
  console.log(paise / 100);
steps:
  - line: 1
    vars: { rupees: '0.30000000000000004' }
    say: Adding 10 paise and 20 paise as rupee decimals. Both 0.1 and 0.2 are stored as close guesses, so the sum is a hair too big.
  - line: 2
    vars: { paise: 30 }
    say: The same amounts as whole paise. 10 + 20 is exactly 30, with nothing hiding after it.
  - line: 3
    out: "0.30000000000000004"
    say: The rupee version prints its tiny error. On a receipt, that's a bug a customer can see.
  - line: 4
    out: "30"
    say: The paise version is exact.
  - line: 5
    out: "0.3"
    say: When it's time to show rupees, divide by 100 once, at the very end.
```

```callout tip
Keep money in whole paise for every sum. Turn it into rupees only to show it. Topic 11 shows how to print it as `₹0.30`.
```

### Rupees in, paise out

Sometimes a price arrives in rupees, typed by a person. Multiply by 100 and round to the nearest whole paisa:

```js run
console.log(0.29 * 100);
console.log(Math.round(0.29 * 100));
console.log(Math.floor(0.29 * 100));
```

This prints `28.999999999999996`, then `29`, then `28`. `Math.round` cleans up the tiny error. `Math.floor` would lose a whole paisa, because the guess sits just below 29.

### Numbers from text

What a customer types in a form arrives as text, even when it looks like a number. `Number(...)` turns text into a number:

```js run
console.log(Number("150") + 30);
console.log("150" + 30);
console.log(Number("two"));
```

This prints `180`, then `15030`, then `NaN`. With text, `+` glues instead of adding. When the text isn't a number at all, `Number` gives `NaN`.

```callout jargon
**NaN** means "Not a Number". It's the number JavaScript gives back when a sum makes no sense, like turning `"two"` into a number.
```

### Big numbers are safe

Whole numbers stay exact up to `9007199254740991`. In paise, that's about ₹90 lakh crore. Dhaaga will be a big brand one day, but not that big.

## Worked example

Three cousins share a gift: an amigurumi with a name stitched on, a keychain, and gift wrap.

```js run
const amigurumi = 45000;   // ₹450
const nameStitched = 5000; // ₹50
const keychain = 12000;    // ₹120
const giftWrap = 3000;     // ₹30
const total = amigurumi + nameStitched + keychain + giftWrap;
console.log(total);
console.log(total / 100);
console.log(Math.floor(total / 3));
console.log(total % 3);
```

1. Lines 1 to 4 store each price in paise. The `//` starts a **comment**: a note for people that JavaScript ignores.
2. Line 5 adds the four whole numbers. `total` is `65000`, exactly.
3. Line 6 prints `65000`.
4. Line 7 divides by 100 to show rupees: `650`.
5. Line 8 splits the bill three ways and rounds down: `21666` paise each.
6. Line 9 prints `2`. Three shares of 21666 make 64998, so 2 paise are left over. Someone pays 2 paise extra.

## Try it

Four friends split a tote bag. Run it, then change `friends` to `3` and run it again. How many paise are left over now?

```js run
const bill = 89900;   // ₹899 in paise
const friends = 4;
const each = Math.floor(bill / friends);
const left = bill - each * friends;
console.log("Each pays " + each + " paise");
console.log("Paise left over: " + left);
```

## Recap

- Every number in JavaScript has the type `number`, whole or not.
- `+ - * /` work like school maths, and `%` gives the remainder.
- `Math.floor` rounds down, `Math.round` rounds to the nearest, `Math.ceil` rounds up.
- Decimals like `0.1` are stored as close guesses, so `0.1 + 0.2` is `0.30000000000000004`.
- Dhaaga keeps money in whole paise, and turns it into rupees only to show it.

## Common mistakes

**1. Keeping prices as rupee decimals.**

```js
const price = 0.1;
const wrap = 0.2;
console.log(price + wrap);
```

This prints `0.30000000000000004`, and the error grows with every sum. Store `10` and `20` paise instead, and the total is exactly `30`.

**2. Leaving decimals in a split.**

```js
const each = 100000 / 3;
console.log(each);
```

This prints a long decimal that nobody can pay. Round it to whole paise with `Math.floor(100000 / 3)`, which gives `33333`.

**3. Rounding a split up by accident.**

```js
const each = Math.round(200000 / 3);
console.log(each * 3);
```

`Math.round` gives `66667`, and three of those make `200001`: one paisa more than the bill. Round down with `Math.floor`, then deal with the few paise left over.

**4. Adding a number to text.**

```js
const typed = "150";
console.log(typed + 30);
```

This prints `15030`, because `+` glues text. Turn the text into a number first: `Number(typed) + 30` is `180`.
